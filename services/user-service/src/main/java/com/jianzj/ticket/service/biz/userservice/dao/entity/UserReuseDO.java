package com.jianzj.ticket.service.biz.userservice.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jianzj.ticket.service.frameworks.starter.database.base.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author JianZJ
 * @Date 2025/2/21 10:15
 */

/**
 * 用户名复用表实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
@TableName("t_user_reuse")
public class UserReuseDO extends BaseDO {

    /**
     * 用户名
     */
    private String username;

}
