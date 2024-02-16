package com.xuecheng.base.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MybatisPlusAutoFillHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入操作时的填充逻辑
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createDate", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "changeDate", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新操作时的填充逻辑
        LocalDateTime now = LocalDateTime.now();
        this.strictUpdateFill(metaObject, "changeDate", LocalDateTime.class, now);
    }
}
