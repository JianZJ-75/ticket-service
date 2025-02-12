package com.jianzj.ticket.service.frameworks.starter.base;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @Author JianZJ
 * @Date 2025/2/12 2:40
 */

/**
 * 自定义上下文
 */
public class ApplicationContextHolder implements ApplicationContextAware {

    public static ApplicationContext CONTEXT;

    /**
     * 创建该类实例时自动调用
     * @param applicationContext the ApplicationContext object to be used by this object
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.CONTEXT = applicationContext;
    }

    /**
     * 根据类型获取
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> T getBean(Class<T> clazz) {
        return CONTEXT.getBean(clazz);
    }

    /**
     * 根据类型和名称获取
     * @param name
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return CONTEXT.getBean(name, clazz);
    }

    /**
     * 根据类型获取多个
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return CONTEXT.getBeansOfType(clazz);
    }

    /**
     * 根据名称和注解获取
     * @param beanName
     * @param annotationType
     * @return
     * @param <A>
     */
    public static <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) {
        return CONTEXT.getBean(beanName, annotationType);
    }

    /**
     * 获取应用上下文实例
     * @return
     */
    public static ApplicationContext getInstance() {
        return CONTEXT;
    }

}