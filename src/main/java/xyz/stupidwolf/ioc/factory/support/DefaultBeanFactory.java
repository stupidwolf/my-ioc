package xyz.stupidwolf.ioc.factory.support;

import xyz.stupidwolf.ioc.factory.config.BeanDefinition;
import xyz.stupidwolf.ioc.exception.BeanDefinitionStoreException;
import xyz.stupidwolf.ioc.exception.BeansException;
import xyz.stupidwolf.ioc.exception.NoSuchBeanDefinitionException;
import xyz.stupidwolf.ioc.util.Assert;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegistry {
    private final ConcurrentMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Object> registeredSingleBeanMap = new ConcurrentHashMap<>();

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        Assert.notNull(beanName, "bean name can't been null");
        Assert.notNull(beanDefinition, "bean definition can't be null");

        if (beanDefinitionMap.containsKey(beanName)) {
            throw new BeanDefinitionStoreException("repeat bean name: \"" + beanName + "\"");
        }
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    @Override
    public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new NoSuchBeanDefinitionException("bean name: " + beanName + " not found.");
        }

        beanDefinitionMap.remove(beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new NoSuchBeanDefinitionException("bean name: " + beanName + " not found.");
        }

        return beanDefinitionMap.get(beanName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName, Class<T> requireType) throws BeansException {
        if (!registeredSingleBeanMap.containsKey(beanName)) {
            // bean definition -> instance TODO
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            //
            String[] dependBeanNames = beanDefinition.getDependsOn();
            if (dependBeanNames != null) {
                for (String dependBeanName : dependBeanNames) {
                    BeanDefinition dependBeanDefinition = getBeanDefinition(dependBeanName);
                    if (!registeredSingleBeanMap.containsKey(dependBeanName)) {
                        getBean(dependBeanName, dependBeanDefinition.getBeanClass());
                    }
                }
            }
        }
        return (T)registeredSingleBeanMap.get(beanName);
    }
}
