package xyz.stupidwolf.ioc.factory;

import xyz.stupidwolf.ioc.exception.BeansException;

import java.util.List;
import java.util.Map;

public interface BeanFactory {
    /**
     * 通过bean name来获取bean
     * @param beanName bean name
     * @param requireType require type
     * @param <T> class type of bean
     * @return bean instance
     * @throws BeansException 无法找到对应的bean时抛异常
     */
    <T> T getBean(String beanName, Class<T> requireType) throws BeansException;

    /**
     * 获取requireType类型的bean
     * @param requireType require type
     * @param <T> class type of bean
     * @return bean instance
     * @throws BeansException 无法找到bean或者找到多个bean抛异常
     */
    <T> T getBean(Class<T> requireType) throws BeansException;

}
