package xyz.stupidwolf.ioc.factory;

import xyz.stupidwolf.ioc.factory.BeanDefinition;
import xyz.stupidwolf.ioc.exception.BeansException;

public interface InstantiationStrategy {
    Object instantiate(BeanDefinition bd)
            throws BeansException;
}
