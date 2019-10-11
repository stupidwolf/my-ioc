package xyz.stupidwolf.ioc.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.stupidwolf.ioc.exception.BeanDefinitionStoreException;
import xyz.stupidwolf.ioc.exception.BeansException;
import xyz.stupidwolf.ioc.exception.NoSuchBeanDefinitionException;
import xyz.stupidwolf.ioc.util.Assert;
import xyz.stupidwolf.ioc.util.BeanNameGenerator;
import xyz.stupidwolf.ioc.util.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegistry {
    private final static Logger logger = LoggerFactory.getLogger(DefaultBeanFactory.class);
    private InstantiationStrategy instantiationStrategy = new SimpleInstantiationStrategy();

    private final ConcurrentMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    /** 已注册的bean **/
    private final ConcurrentMap<String, Object> allBeanInfoMap = new ConcurrentHashMap<>();

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
        Assert.notNull(beanName, "bean name can not be null.");
        Assert.notNull(requireType, "require type can not be null.");

        if (!allBeanInfoMap.containsKey(beanName)) {
            // bean definition -> instance
            // 获取bean的定义
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            Object instance;
            if (beanDefinition.getConstructor() != null) {
                String[] constructorArgsBeanName = beanDefinition.getConstructorArgsBeanName();
                if (constructorArgsBeanName != null) {
                    Object[] args = new Object[constructorArgsBeanName.length];
                    for (int i = 0; i < constructorArgsBeanName.length; i ++) {
                        String toSolveBean = constructorArgsBeanName[i];
                        if (allBeanInfoMap.containsKey(toSolveBean)) {
                            continue;
                        }
                        if (toSolveBean.equals(beanName)) {
                            throw new BeansException("circle depend happen when use constructor way to inject bean: " + beanName);
                        }
                        args[i] = getBean(toSolveBean, beanDefinitionMap.get(toSolveBean).getBeanClass());
                    }
                    // 设置构造方法的具体入参
                    beanDefinition.setConstructorArgs(args);
                }
            }
            instance = instantiationStrategy.instantiate(beanDefinition);
            allBeanInfoMap.put(beanDefinition.getBeanName(), instance);
            List<String> dependBeanNames = beanDefinition.getDependBeanNames();
            logger.debug("bean name: {}, depend on other beans: {}", beanName, dependBeanNames);
            // 确保所需要bean已全都初始化完成
            if (dependBeanNames != null) {
                for (String dependBeanName : dependBeanNames) {
                    if (allBeanInfoMap.containsKey(dependBeanName)) {
                        continue;
                    }
                    // 初始化所需要的bean
                    BeanDefinition dependBeanDefinition = getBeanDefinition(dependBeanName);
                    getBean(dependBeanName, dependBeanDefinition.getBeanClass());
                }
            }
            // 注入依赖的bean
            if (dependBeanNames != null && !dependBeanNames.isEmpty()) {
                // a.属性方式
                Field[] fields = instance.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        String dependBeanName = getBeanName(field);
                        field.setAccessible(true);
                        try {
                            field.set(instance, allBeanInfoMap.get(dependBeanName));
                        } catch (IllegalAccessException e) {
                            throw new BeansException(String.format("can't set bean instance filed[%s]: ", field.getName()), e);
                        }
                    }
                }
            }

        }
        return (T) allBeanInfoMap.get(beanName);
    }

    private String getBeanName(Field field) {
        String beanName = null;
        if (field.isAnnotationPresent(Named.class)) {
            Named namedAnnotation = field.getAnnotation(Named.class);
            String name = namedAnnotation.value();
            if (StringUtils.isNotEmpty(name)) {
                beanName = name;
            }
        }

        if (beanName == null) {
            beanName = BeanNameGenerator.toLowerCamelCase(field.getName());
        }
        return beanName;
    }
}
