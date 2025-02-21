package com.jianzj.ticket.service.biz.userservice.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jianzj.ticket.service.biz.userservice.common.enums.UserChainMarkEnum;
import com.jianzj.ticket.service.biz.userservice.dao.entity.*;
import com.jianzj.ticket.service.biz.userservice.dao.mapper.*;
import com.jianzj.ticket.service.biz.userservice.dto.req.UserDeletionReqDTO;
import com.jianzj.ticket.service.biz.userservice.dto.req.UserLoginReqDTO;
import com.jianzj.ticket.service.biz.userservice.dto.req.UserRegisterReqDTO;
import com.jianzj.ticket.service.biz.userservice.dto.resp.UserLoginRespDTO;
import com.jianzj.ticket.service.biz.userservice.dto.resp.UserQueryRespDTO;
import com.jianzj.ticket.service.biz.userservice.dto.resp.UserRegisterRespDTO;
import com.jianzj.ticket.service.biz.userservice.service.UserLoginService;
import com.jianzj.ticket.service.biz.userservice.service.UserService;
import com.jianzj.ticket.service.biz.userservice.tools.UserReuseUtil;
import com.jianzj.ticket.service.frameworks.starter.cache.DistributedCache;
import com.jianzj.ticket.service.frameworks.starter.common.tools.BeanUtil;
import com.jianzj.ticket.service.frameworks.starter.convention.exception.ClientException;
import com.jianzj.ticket.service.frameworks.starter.convention.exception.ServiceException;
import com.jianzj.ticket.service.frameworks.starter.designpattern.chain.AbstractChainContext;
import com.jianzj.ticket.service.frameworks.starter.user.core.UserContext;
import com.jianzj.ticket.service.frameworks.starter.user.core.UserInfoDTO;
import com.jianzj.ticket.service.frameworks.starter.user.tools.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.jianzj.ticket.service.biz.userservice.common.constant.RedisKeyConstant.*;
import static com.jianzj.ticket.service.biz.userservice.common.enums.UserRegisterErrorCodeEnum.*;

/**
 * @Author JianZJ
 * @Date 2025/2/21 9:57
 */

/**
 * 用户登录接口实现层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserLoginServiceImpl implements UserLoginService {

    private final UserService userService;
    private final UserMailMapper userMailMapper;
    private final UserPhoneMapper userPhoneMapper;
    private final UserMapper userMapper;
    private final UserReuseMapper userReuseMapper;
    private final UserDeletionMapper userDeletionMapper;
    private final DistributedCache distributedCache;
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final AbstractChainContext<UserRegisterReqDTO> abstractChainContext;
    private final RedissonClient redissonClient;

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        String usernameOrMailOrPhone = requestParam.getUsernameOrMailOrPhone();
        boolean mailFlag = false;
        // 判断是否为邮箱
        for (char c : usernameOrMailOrPhone.toCharArray()) {
            if (c == '@') {
                mailFlag = true;
                break;
            }
        }
        String username;
        if (mailFlag) {
            // 根据邮箱路由查询用户名
            LambdaQueryWrapper<UserMailDO> queryWrapper = Wrappers.lambdaQuery(UserMailDO.class)
                    .eq(UserMailDO::getMail, usernameOrMailOrPhone);
            username = Optional.ofNullable(userMailMapper.selectOne(queryWrapper))
                    .map(UserMailDO::getUsername)
                    .orElseThrow(() -> new ClientException("用户名/手机号/邮箱不存在"));
        } else {
            // 根据手机号查询用户名
            LambdaQueryWrapper<UserPhoneDO> queryWrapper = Wrappers.lambdaQuery(UserPhoneDO.class)
                    .eq(UserPhoneDO::getPhone, usernameOrMailOrPhone);
            username = Optional.ofNullable(userPhoneMapper.selectOne(queryWrapper))
                    .map(UserPhoneDO::getUsername)
                    .orElse(null);
        }
        username = Optional.ofNullable(username).orElse(requestParam.getUsernameOrMailOrPhone());
        // 密码验证
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username)
                .eq(UserDO::getPassword, requestParam.getPassword())
                .select(UserDO::getId, UserDO::getUsername, UserDO::getRealName);
        UserDO userDO = userMapper.selectOne(queryWrapper);
        // 生成token
        if (userDO != null) {
            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .userId(String.valueOf(userDO.getId()))
                    .username(userDO.getUsername())
                    .realName(userDO.getRealName())
                    .build();
            String accessToken = JWTUtil.generateAccessToken(userInfo);
            UserLoginRespDTO actual = new UserLoginRespDTO(userInfo.getUserId(), requestParam.getUsernameOrMailOrPhone(), userDO.getRealName(), accessToken);
            distributedCache.put(accessToken, JSON.toJSONString(actual), 30, TimeUnit.MINUTES);
            return actual;
        }
        throw new ServiceException("账号不存在或密码错误");
    }

    @Override
    public UserLoginRespDTO checkLogin(String accessToken) {
        return distributedCache.get(accessToken, UserLoginRespDTO.class);
    }

    @Override
    public void logout(String accessToken) {
        if (StrUtil.isNotBlank(accessToken)) {
            distributedCache.delete(accessToken);
        }
    }

    @Override
    public Boolean hasUsername(String username) {
        // 判断布隆过滤器是否存在该用户名
        boolean hasUsername = userRegisterCachePenetrationBloomFilter.contains(username);
        // 存在 需要进行二次判断是否已经注销
        if (hasUsername) {
            // 获取redis实例
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            // 通过查询redis存储注销用户名的set来判断是否可以使用
            return instance.opsForSet().isMember(USER_REGISTER_REUSE_SHARDING + UserReuseUtil.hashShardingIdx(username), username);
        }
        // 不存在 可直接使用
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserRegisterRespDTO register(UserRegisterReqDTO requestParam) {
        // 调用注册责任链进行数据检验
        abstractChainContext.handler(UserChainMarkEnum.USER_REGISTER_FILTER.name(), requestParam);
        // 用户注册锁
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER + requestParam.getUsername());
        // 尝试获取锁
        boolean tryLock = lock.tryLock();
        // 获取失败
        if (!tryLock) {
            throw new ServiceException(HAS_USERNAME_NOTNULL);
        }
        // 成功
        try {
            try {
                // 创建新用户
                int inserted = userMapper.insert(BeanUtil.convert(requestParam, UserDO.class));
                if (inserted < 1) {
                    throw new ServiceException(USER_REGISTER_FAIL);
                }
            } catch (DuplicateKeyException dke) {
                log.error("用户名 [{}] 重复注册", requestParam.getUsername());
                throw new ServiceException(HAS_USERNAME_NOTNULL);
            }
            // 手机号登陆
            UserPhoneDO userPhoneDO = UserPhoneDO.builder()
                    .phone(requestParam.getPhone())
                    .username(requestParam.getUsername())
                    .build();
            try {
                // 新建手机号
                userPhoneMapper.insert(userPhoneDO);
            } catch (DuplicateKeyException dke) {
                log.error("用户 [{}] 注册手机号 [{}] 重复", requestParam.getUsername(), requestParam.getPhone());
                throw new ServiceException(PHONE_REGISTERED);
            }
            // 邮箱登陆
            if (StrUtil.isNotBlank(requestParam.getMail())) {
                UserMailDO userMailDO = UserMailDO.builder()
                        .mail(requestParam.getMail())
                        .username(requestParam.getUsername())
                        .build();
                try {
                    // 新建邮箱
                    userMailMapper.insert(userMailDO);
                } catch (DuplicateKeyException dke) {
                    log.error("用户 [{}] 注册邮箱 [{}] 重复", requestParam.getUsername(), requestParam.getMail());
                    throw new ServiceException(MAIL_REGISTERED);
                }
            }
            String username = requestParam.getUsername();
            // 删除名称复用表中相关数据
            userReuseMapper.delete(Wrappers.update(new UserReuseDO(username)));
            // 获取redis实例
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            // 将redis中存储注销用户名的set的移除该username
            instance.opsForSet().remove(USER_REGISTER_REUSE_SHARDING + UserReuseUtil.hashShardingIdx(username), username);
            // 添加进布隆过滤器
            userRegisterCachePenetrationBloomFilter.add(username);
        } finally {
            lock.unlock();
        }
        return BeanUtil.convert(requestParam, UserRegisterRespDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletion(UserDeletionReqDTO requestParam) {
        String username = UserContext.getUsername();
        if (!Objects.equals(username, requestParam.getUsername())) {
            throw new ClientException("注销账号与登录账号不一致");
        }
        RLock lock = redissonClient.getLock(USER_DELETION + requestParam.getUsername());
        lock.lock();
        try {
            UserQueryRespDTO userQueryRespDTO = userService.queryUserByUsername(username);
            UserDeletionDO userDeletionDO = UserDeletionDO.builder()
                    .idType(userQueryRespDTO.getIdType())
                    .idCard(userQueryRespDTO.getIdCard())
                    .build();
            // 添加进注销身份证号表
            userDeletionMapper.insert(userDeletionDO);
            UserDO userDO = new UserDO();
            userDO.setDeletionTime(System.currentTimeMillis());
            userDO.setUsername(username);
            // 删除用户表
            userMapper.deletionUser(userDO);
            UserPhoneDO userPhoneDO = UserPhoneDO.builder()
                    .phone(userQueryRespDTO.getPhone())
                    .deletionTime(System.currentTimeMillis())
                    .build();
            // 删除手机号表
            userPhoneMapper.deletionUser(userPhoneDO);
            if (StrUtil.isNotBlank(userQueryRespDTO.getMail())) {
                UserMailDO userMailDO = UserMailDO.builder()
                        .mail(userQueryRespDTO.getMail())
                        .deletionTime(System.currentTimeMillis())
                        .build();
                // 删除邮箱表
                userMailMapper.deletionUser(userMailDO);
            }
            // 缓存中删除该用户的登陆信息
            distributedCache.delete(UserContext.getToken());
            // 新增可复用用户名信息
            userReuseMapper.insert(new UserReuseDO(username));
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            // 向redis的注销用户名set中添加
            instance.opsForSet().add(USER_REGISTER_REUSE_SHARDING + UserReuseUtil.hashShardingIdx(username), username);
        } finally {
            lock.unlock();
        }
    }
}
