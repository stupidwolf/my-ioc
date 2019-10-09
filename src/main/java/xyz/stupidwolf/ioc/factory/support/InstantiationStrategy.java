package xyz.stupidwolf.ioc.factory.support;

import xyz.stupidwolf.ioc.factory.config.BeanDefinition;
import xyz.stupidwolf.ioc.exception.BeansException;

public interface InstantiationStrategy {
    Object instantiate(BeanDefinition bd)
            throws BeansException;
}
