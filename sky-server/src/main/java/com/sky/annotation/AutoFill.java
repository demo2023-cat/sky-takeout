package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 自定义注解：用于标识需要进行公共字段自动填充的方法。
 * <p>
 * 作用：
 * 1. 作为一个“标记”，配合 AOP 切入点表达式定位需要拦截的 Mapper 方法。
 * 2. 传递参数：标识当前数据库操作的类型是 INSERT（插入）还是 UPDATE（更新）。
 * </p>
 */
@Target(ElementType.METHOD) // 指定该注解只能用在方法上
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME) // 指定该注解在运行期（JVM中）依然保留，这样 AOP 才能通过反射获取到它
public @interface AutoFill {
    /**
     * 数据库操作类型：INSERT, UPDATE
     * 用于切面中判断该填充哪些公共字段（新增填充4个字段，更新填充2个字段）
     */
    OperationType value();
}

