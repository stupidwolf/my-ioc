package xyz.stupidwolf.ioc.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.stupidwolf.ioc.annotation.Bean;
import xyz.stupidwolf.ioc.annotation.Configuration;
import xyz.stupidwolf.ioc.asm.ClassInfo;
import xyz.stupidwolf.ioc.asm.ClassInfoVisitor;
import xyz.stupidwolf.ioc.exception.BeansException;
import xyz.stupidwolf.ioc.factory.BeanDefinition;
import xyz.stupidwolf.ioc.util.BeanNameGenerator;
import xyz.stupidwolf.ioc.util.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

public class ClassPathBeanDefinitionScanner {
    private final Logger logger = LoggerFactory.getLogger(ClassPathBeanDefinitionScanner.class);

    /**
     * 扫描 base package 下面的.class文件
     * @param basePackage base package
     * @return bean 的定义信息
     * @throws BeansException bean ex
     */
    public List<BeanDefinition> scan(String basePackage) throws BeansException {
        return resolveBeanDefinitionsRecursively(basePackage);
    }


    public List<BeanDefinition> resolveBeanDefinitionsRecursively(String basePackage) throws BeansException {
        List<BeanDefinition> beanDefinitions = new LinkedList<>();
        resolveBeanDefinitionsRecursively(new File(basePackage), beanDefinitions);
        return beanDefinitions;
    }

    private void resolveBeanDefinitionsRecursively(File baseFile, List<BeanDefinition> beanDefinitions) throws BeansException {
        if (baseFile.isDirectory()) {
            File[] childFiles = baseFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    File file = new File(dir, name);
                    return file.isDirectory() || file.getName().endsWith(".class");
                }
            });
            if (childFiles == null) {
                return;
            }
            for (File file : childFiles) {
                resolveBeanDefinitionsRecursively(file, beanDefinitions);
            }
        } else {
            // 识别使用注解的class文件 XXX
            ClassInfo classInfo = null;
            try {
                classInfo = ClassInfoVisitor.getClassInfoWithDeclareAnnotation(baseFile.getAbsolutePath());
            } catch (IOException e) {
                throw new BeansException("visit class file fail: ", e);
            }
            if ( classInfo != null) {
                for (Class<? extends Annotation> annotation : classInfo.getClassAnnotations()) {
                    if (annotation == Singleton.class) {
                        beanDefinitions.add(resolveSingletonBeanDefinition(classInfo));
                    } else if (annotation == Configuration.class) {
                        // java代码方式注册bean
                        beanDefinitions.addAll(resolveConfigurationBeanDefinition(classInfo));
                    }
                }
            }
        }
    }


    /**
     * 解析class文件得到bean的具体定义信息
     * @param classInfo classInfo
     * @return bean 的定义信息
     * @throws BeansException bean ex
     */
    private BeanDefinition resolveSingletonBeanDefinition(ClassInfo classInfo) throws BeansException {
        BeanDefinition beanDefinition = new BeanDefinition();
        logger.debug("resolving @Singleton class: " + classInfo.getClassName() + " as bean definition." );
        try {
            Class<?> clazz = Class.forName(classInfo.getClassName());
            beanDefinition.setBeanClass(clazz);

           // 1. 获取bean name
           String beanName = null;
           if (clazz.isAnnotationPresent(Named.class)) {
               Named named = clazz.getAnnotation(Named.class);
               String name = named.value();
               if (StringUtils.isNotEmpty(name)) {
                   beanName = name;
               }
           }

           if (beanName == null) {
               beanName = BeanNameGenerator.toLowerCamelCase(clazz.getSimpleName());
           }

           beanDefinition.setBeanName(beanName);

           // 2. 所依赖的bean
           List<String> dependBeanNames = new ArrayList<>(4);

           Field[] fields = clazz.getDeclaredFields();
           for (Field field : fields) {
               if (field.isAnnotationPresent(Inject.class)) {
                   String dependBeanName = null;
                   if (field.isAnnotationPresent(Named.class)) {
                       Named named = field.getAnnotation(Named.class);
                       String name = named.value();
                       if (StringUtils.isNotEmpty(name)) {
                           dependBeanName = name;
                       }
                   }
                   if (StringUtils.isEmpty(dependBeanName)) {
                       // 默认使用filed name 作为bean name
                       dependBeanName = field.getName();
                   }
                   dependBeanNames.add(dependBeanName);
               }
           }
           beanDefinition.setDependBeanNames(dependBeanNames);

           // 构造方法依赖的bean
           // 构造方法需要显示申明@Inject注解
           // 若存在多个构造方法，并且构造方法存在多个@Inject注解，此时只有第一个构造方法生效
           Constructor[] constructors = clazz.getDeclaredConstructors();
           for (Constructor constructor : constructors) {
               if (constructor.isAnnotationPresent(Inject.class)) {
                   String dependBeanName = null;
                   Parameter[] parameters = constructor.getParameters();
                   String[] constructorArgsBeanName = new String[parameters.length];
                   for (int i = 0; i < parameters.length; i ++) {
                       Parameter parameter = parameters[i];
                       if (parameter.isAnnotationPresent(Named.class)) {
                           Named named = parameter.getAnnotation(Named.class);
                           String name = named.value();
                           if (StringUtils.isNotEmpty(name)) {
                               dependBeanName = name;
                           }
                       }
                       if (dependBeanName == null) {
                           // 默认使用参数名作为bean name
                           dependBeanName = parameter.getName();
                       }
                       dependBeanNames.add(dependBeanName);
                       constructorArgsBeanName[i] = dependBeanName;
                   }

                   beanDefinition.setConstructor(constructor);
                   beanDefinition.setConstructorArgsBeanName(constructorArgsBeanName);
               }
            }
        } catch (ClassNotFoundException e) {
            throw new BeansException("can not load class: " + classInfo.getClassName(), e);
        }
        return beanDefinition;
    }

    private List<BeanDefinition> resolveConfigurationBeanDefinition(ClassInfo classInfo) throws BeansException {
        List<BeanDefinition> beanDefinitions = new LinkedList<>();
        BeanDefinition configurationBeanDefinition = new BeanDefinition();
        logger.debug("resolving @Configuration class: {} to bean definitions", classInfo.getClassName());
        Class<?> clazz;
        try {
            clazz = Class.forName(classInfo.getClassName());
            // 将@Configuration修饰的bean作为一种特殊的bean添加到beanDefinitions,后面需要通过它来获取java方式所定义的bean
            String configurationBeanName = null;
            if (clazz.isAnnotationPresent(Named.class)) {
                Named namedAnnotation = clazz.getAnnotation(Named.class);
                if (StringUtils.isNotEmpty(namedAnnotation.value())) {
                    configurationBeanName = namedAnnotation.value();
                }
            }
            if (configurationBeanName == null) {
                configurationBeanName = clazz.getSimpleName();
            }

            configurationBeanDefinition.setBeanName(configurationBeanName);
            configurationBeanDefinition.setBeanClass(clazz);
            beanDefinitions.add(configurationBeanDefinition);
        } catch (ClassNotFoundException e) {
            throw new BeansException("can not load class: " + classInfo.getClassName(), e);
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Bean.class)) {
                BeanDefinition beanDefinition = new BeanDefinition();
                List<String> dependOns = new LinkedList<>();
                String beanName = null;
                if (method.isAnnotationPresent(Named.class)) {
                    Named namedAnnotation = method.getAnnotation(Named.class);
                    String name = namedAnnotation.value();
                    if (StringUtils.isNotEmpty(name)) {
                        beanName = name;
                    }
                }
                if (beanName == null) {
                    // 默认使用方法名作为bean name
                    beanName = BeanNameGenerator.toLowerCamelCase(method.getName());
                }
                // 获取依赖的bean name
                Parameter[] parameters = method.getParameters();
                for (Parameter parameter : parameters) {
                    if (!parameter.isAnnotationPresent(Named.class)) {
                        throw new BeansException("method parameter need declare with @Named annotation");
                    }
                    String dependBeanName = null;
                    Named parameterNamedAnnotation = parameter.getAnnotation(Named.class);
                    if (StringUtils.isNotEmpty(parameterNamedAnnotation.value())) {
                        dependBeanName = parameterNamedAnnotation.value();
                    }

                    if (dependBeanName == null) {
                        dependBeanName = parameter.getName();
                    }
                    dependOns.add(dependBeanName);
                }
                beanDefinition.setBeanClass(method.getReturnType());
                beanDefinition.setBeanName(beanName);
                beanDefinition.setDependBeanNames(dependOns);

                beanDefinition.setMethod(method);
                beanDefinition.setConfigurationBeanName(configurationBeanDefinition.getBeanName());
                beanDefinitions.add(beanDefinition);
            }
        }
        return beanDefinitions;
    }
}
