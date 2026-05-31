package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.reflect.MethodSignature;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static com.sky.constant.AutoFillConstant.*;

/**
 * 自定义切面类，用于拦截 Mapper 方法并自动填充公共字段（createTime, createUser, updateTime, updateUser）。
 * <p>
 * AOP 的核心组件：
 * 1. @Aspect：标识这是一个切面类。
 * 2. @Component：将该类作为 Bean 注册进 Spring 容器，使 Spring 能扫描并应用 AOP 织入。
 * </p>
 */
@Aspect
@Component
@Slf4j
public class  AutoFillAspect {
    /**
     * 定义切入点 (Pointcut)
     * <p>
     * 表达式含义：
     * - execution(* com.sky.mapper.*.*(..))：拦截 com.sky.mapper 包下的任意类、任意方法、任意参数。
     * - && @annotation(com.sky.annotation.AutoFill)：要求被拦截的方法必须同时标注了 @AutoFill 注解。
     * </p>
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){
    }

    /**
     * 前置通知 (Before Advice)
     * 在目标 Mapper 方法执行前先执行该方法，填充实体类对象的公共字段。
     * 
     * @param joinPoint 连接点对象，用来获取被拦截方法的签名、注解、入参等运行期上下文数据
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("AOP拦截成功，开始进行公共字段自动填充...");
        
        // 1. 获取目标方法的签名 (用于获取方法上的 @AutoFill 注解)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        
        // 2. 通过反射获取目标方法上的 @AutoFill 注解对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        
        // 3. 获取该注解中指定的数据库操作类型 (INSERT / UPDATE)
        OperationType type = autoFill.value();
        
        // 4. 获取准备写入的公共字段的值 (当前系统时间)
        LocalDateTime time = LocalDateTime.now();
        
        // 5. 获取被拦截方法的参数列表
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0)
            return;
        
        // 6. 约定俗成：我们将要填充的实体类对象（如 Employee, Category）作为方法的第一个参数（args[0]）
        Object object = args[0];
        
        // 7. 根据操作类型的不同，为对应的属性赋值（通过 Java 反射动态调用实体类的 setter 方法）
        if(type == OperationType.INSERT){
            // 新增操作：需要同时填充 4 个公共字段（创建时间、更新时间、创建人、更新人）
            try {
                // 通过反射获取实体类的 setter 方法对象
                Method setUpdateTime = object.getClass().getDeclaredMethod(SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = object.getClass().getDeclaredMethod(SET_UPDATE_USER, Long.class);
                Method setCreateTime = object.getClass().getDeclaredMethod(SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = object.getClass().getDeclaredMethod(SET_CREATE_USER, Long.class);
                
                // 通过反射激活执行 setter 方法，完成字段属性的赋值
                // BaseContext.getCurrentId() 底层使用 ThreadLocal 获取当前登录的用户 ID
                setCreateUser.invoke(object, BaseContext.getCurrentId());
                setCreateTime.invoke(object, time);
                setUpdateUser.invoke(object, BaseContext.getCurrentId());
                setUpdateTime.invoke(object, time);
            }
            catch (Exception e) {
                log.error("自动填充（INSERT）失败：", e);
            }

        } else if (type == OperationType.UPDATE) {
            // 更新操作：只需要填充 2 个公共字段（更新时间、更新人）
            try {
                // 通过反射获取实体类的 setter 方法对象
                Method setUpdateTime = object.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = object.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                
                // 执行反射调用赋值
                setUpdateUser.invoke(object, BaseContext.getCurrentId());
                setUpdateTime.invoke(object, time);
            }
            catch (Exception e) {
                log.error("自动填充（UPDATE）失败：", e);
            }
        }
    }
}
