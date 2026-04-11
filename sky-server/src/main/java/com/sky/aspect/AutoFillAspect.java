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

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 拦截需要自动填充的类
     */

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){
    }
    /**
     * 逻辑：在通知方法中获取参数，根据参数创建时间进行填充
     * @param joinPoint
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行数据填充...");
        // 获取方法签名对象，用于后续获取方法上的注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType type = autoFill.value();//获得数据库操作类型
        LocalDateTime time=LocalDateTime.now();
        Object[] args =joinPoint.getArgs();
        if(args == null || args.length == 0)
            return;
        Object object = args[0];
        if(type == OperationType.INSERT){
            try {
                Method setUpdateTime = object.getClass().getDeclaredMethod(SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = object.getClass().getDeclaredMethod(SET_UPDATE_USER, Long.class);
                Method setCreateTime =object.getClass().getDeclaredMethod(SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser =object.getClass().getDeclaredMethod(SET_CREATE_USER, Long.class);
                setCreateUser.invoke(object, BaseContext.getCurrentId());
                setCreateTime.invoke(object, time);
                setUpdateUser.invoke(object, BaseContext.getCurrentId());
                setUpdateTime.invoke(object, time);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        } else if (type == OperationType.UPDATE) {
            try {
                Method setUpdateTime =object.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser =object.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                setUpdateUser.invoke(object, BaseContext.getCurrentId());
                setUpdateTime.invoke(object, time);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
