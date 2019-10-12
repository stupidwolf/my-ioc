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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegistry {
    private final static Logger logger = LoggerFactory.getLogger(DefaultBeanFactory.class);
    private InstantiationStrategy instantiationStrategy = new SimpleInstantiationStrategy();

    private final ConcurrentMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    /** 已注册的bean **/
    private final ConcurrentMap<String, Object> allBeanInfoMap = new ConcurrentHashMap<>();

    /** 已注册的单例bean,为了支持通过class type方式获取bean **/
    private final ConcurrentMap<Class<?>, Set<String>> singleBeanNamesByType = new ConcurrentHashMap<>();

    /** 正在初始化中的bean, 用来检测在使用构造方法,普通java方法实例化bean时可能出现的循环依赖问题**/
    private final Set<String> initializingBeanNames = new HashSet<>();

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        Assert.notNull(beanName, "bean name can't been null");
        Assert.notNull(beanDefinition, "bean definition can't be null");

        if (beanDefinitionMap.containsKey(beanName)) {
            throw new BeanDefinitionStoreException("repeat bean name: \"" + beanName + "\"");
        }
        beanDefinitionMap.put(beanName, beanDefinition);
        singleBeanNamesByType.putIfAbsent(beanDefinition.getBeanClass(), new LinkedHashSet<>());
        singleBeanNamesByType.get(beanDefinition.getBeanClass()).add(beanName);
    }

    @Override
    public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new NoSuchBeanDefinitionException("bean name: " + beanName + " not found.");
        }

        BeanDefinition beanDefinition = beanDefinitionMap.remove(beanName);
        singleBeanNamesByType.get(beanDefinition.getBeanClass()).remove(beanName);
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
                        if (initializingBeanNames.contains(beanName)) {
                            throw new BeansException("circle depend happen when by constructor way to inject bean: " + beanName);
                        }
                        initializingBeanNames.add(beanName);
                        args[i] = getBean(toSolveBean, beanDefinitionMap.get(toSolveBean).getBeanClass());
                        initializingBeanNames.remove(beanName);
                    }
                    // 设置构造方法的具体入参
                    beanDefinition.setConstructorArgs(args);
                }
            } else if (beanDefinition.getConfigurationBeanName() != null) {
                // 通过@Configuration注册的bean如何初始化?
                Object configurationInstance = getBean(beanDefinition.getConfigurationBeanName(),
                        getBeanDefinition(beanDefinition.getConfigurationBeanName()).getBeanClass());
                Class configurationClazz = configurationInstance.getClass();
                // 解决依赖问题
                List<String> dependOns = beanDefinition.getDependBeanNames();
                Object[] args = null;
                if (dependOns != null && dependOns.size() > 0) {
                    args = new Object[dependOns.size()];
                    int i = 0;
                    for (String dependBeanName : dependOns) {
                        initializingBeanNames.add(beanName);
                        if (initializingBeanNames.contains(beanName)) {
                            throw new BeansException("circle depend happen when by java method way to inject bean: " + beanName);
                        }
                        args[i ++] = getBean(dependBeanName, getBeanDefinition(dependBeanName).getBeanClass());
                        initializingBeanNames.remove(beanName);
                    }
                }
                Method method = beanDefinition.getMethod();
                try {
                    Object beanInstance = method.invoke(configurationInstance, args);
                    allBeanInfoMap.put(beanDefinition.getBeanName(), beanInstance);
                    // 初始化bean完成,不需要再走下面的流程
                    return (T)beanInstance;
                } catch (IllegalAccessException e) {
                    throw new BeansException("IllegalAccessException: fail to create bean by invoke method, ", e);
                } catch (InvocationTargetException e) {
                    throw new BeansException("InvocationTargetException: fail to create bean by invoke method, ", e);
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

    @Override
    public <T> T getBean(Class<T> requireType) throws BeansException {
        Set<String> beanNames = singleBeanNamesByType.get(requireType);
        if (beanNames == null) {
            throw new BeansException("not found bean type: [" + requireType + "], you can register a bean as this type.");
        } else if (beanNames.size() > 1) {
            throw new BeansException("found more than one bean of bean type [" + requireType
                    + "], you may should index the exactly bean name use @Named annotation");
        }
        String beanName = beanNames.iterator().next();
        return getBean(beanName, requireType);
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
