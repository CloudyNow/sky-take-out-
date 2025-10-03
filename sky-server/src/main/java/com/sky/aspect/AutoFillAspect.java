package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.apache.ibatis.ognl.OgnlRuntime.setFieldValue;

@Aspect
@Slf4j
@Component
public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFill(){
    }

    /**
     * 逻辑实现 前置通知
     * @param joinPoint
     */
    @Before("autoFill()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共数据填充");
        //获取当前被拦截的操作类型参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的数据库操作类型
        OperationType operationType = autoFill.value();//获取操作类型
        //获取当前被拦截的方法参数
        Object[] args = joinPoint.getArgs();//获取方法参数
        if (args == null || args.length == 0){
            return;
        }
        Object entity = args[0]; //获取当前方法参数对象
        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //根据不同的操作类型，为对应的属性赋值
        if (operationType == OperationType.INSERT){
            //为4个公共属性赋值
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射赋值
                setCreateTime.invoke(entity,now);
                setUpdateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (operationType == OperationType.UPDATE){
                //为2个公共字段赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
