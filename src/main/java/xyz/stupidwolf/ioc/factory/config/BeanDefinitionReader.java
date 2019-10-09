package xyz.stupidwolf.ioc.factory.config;

import java.util.Collection;

public interface BeanDefinitionReader {
    Collection<BeanDefinition> loadBeanDefinitions(String location);
}
