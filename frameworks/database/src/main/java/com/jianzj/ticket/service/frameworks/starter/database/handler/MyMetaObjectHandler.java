package com.jianzj.ticket.service.frameworks.starter.database.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.jianzj.ticket.service.frameworks.starter.common.enums.DelEnum;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;

/**
 * @Author JianZJ
 * @Date 2025/2/16 3:18
 */

/**
 * 字段填充执行器
 */
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 数据新增时填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "delFlag", Integer.class, DelEnum.NORMAL.code());
    }

    /**
     * 数据修改时填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
    }

}