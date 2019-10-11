package xyz.stupidwolf.ioc.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        basePackage = basePackage.replaceAll("\\.", File.separator);
        return resolveBeanDefinitionsRecursively(basePackage);
    }


    public List<BeanDefinition> resolveBeanDefinitionsRecursively(String basePackage) throws BeansException {
        List<BeanDefinition> beanDefinitions = new LinkedList<>();
        ClassLoader classLoader = getClassLoader();
        Enumeration<URL> urlEnumeration;
        try {
            urlEnumeration = (classLoader != null ? classLoader.getResources("")
                    : ClassLoader.getSystemResources(""));
            while (urlEnumeration.hasMoreElements()) {
                URL url = urlEnumeration.nextElement();
                String filePath = url.getPath();
                File scanDir = new File(filePath, basePackage);
                // 将使用注解定义的class file添加进来
                // class -> bean definition
                if (scanDir.exists()) {
                    resolveBeanDefinitionsRecursively(scanDir, beanDefinitions);
                }
            }
        } catch (IOException e) {
            throw new BeansException("I/O error happen when get class path: ", e);
        }

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
                beanDefinitions.add(resolveBeanDefinition(classInfo));
            }
        }
    }


    /**
     * 解析class文件得到bean的具体定义信息
     * @param classInfo classInfo
     * @return bean 的定义信息
     * @throws BeansException bean ex
     */
    private BeanDefinition resolveBeanDefinition(ClassInfo classInfo) throws BeansException {
        BeanDefinition beanDefinition = new BeanDefinition();
        logger.debug("converting class: " + classInfo.getClassName() + " as bean definition." );
        try {
            Class<?> clazz = Class.forName(classInfo.getClassName());
            beanDefinition.setBeanClass(clazz);

            for (Class<? extends Annotation> annotation : classInfo.getClassAnnotations()) {
               if (annotation == Configuration.class) {
                   // TODO 支持java代码方式注册bean
               } else if (annotation == Singleton.class) {
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
                           break;
                       }
                   }
               }
            }
        } catch (ClassNotFoundException e) {
            throw new BeansException("can not load class: " + classInfo.getClassName(), e);
        }
        return beanDefinition;
    }

    public ClassLoader getClassLoader() {
        // XXX 以这种方式获取class path下的文件是否合适?
        return this.getClass().getClassLoader();
    }
}
