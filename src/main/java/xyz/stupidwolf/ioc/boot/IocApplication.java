package xyz.stupidwolf.ioc.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.stupidwolf.ioc.annotation.IocBoot;
import xyz.stupidwolf.ioc.context.ClassPathBeanDefinitionScanner;
import xyz.stupidwolf.ioc.factory.BeanDefinition;
import xyz.stupidwolf.ioc.factory.DefaultBeanFactory;
import xyz.stupidwolf.ioc.util.StringUtils;

import java.util.List;

public class IocApplication {
    private static final Logger logger = LoggerFactory.getLogger(IocApplication.class);
    // XXX 应该更加优雅地获取 bean factory
    private static DefaultBeanFactory instance;

    public static DefaultBeanFactory run(Class<?> primarySource, String... args) {
        if (!primarySource.isAnnotationPresent(IocBoot.class)) {
            logger.warn("this application is not a ioc application, if you want to boot your application with ioc feature, " +
                    "you can use @IocBoot annotation on your class.");
            return null;
        }
        DefaultBeanFactory defaultBeanFactory = new DefaultBeanFactory();
        if (primarySource.isAnnotationPresent(IocBoot.class)) {
            ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner();
            IocBoot iocBootAnnotation = primarySource.getAnnotation(IocBoot.class);
            String scanBase = iocBootAnnotation.scanBase();
            String baseScanPackage = ".";
            if (StringUtils.isNotEmpty(scanBase)) {
                baseScanPackage = scanBase;
            }
            // 扫描指定包下包含的bean定义信息
            List<BeanDefinition> beanDefinitions = classPathBeanDefinitionScanner.scan(baseScanPackage);

            for (BeanDefinition beanDefinition : beanDefinitions) {
                defaultBeanFactory.registerBeanDefinition(beanDefinition.getBeanName(), beanDefinition);
            }
            // 实例化bean refresh

            // destroy
        }
        instance = defaultBeanFactory;
        return instance;
    }

    public DefaultBeanFactory getDefaultBeanFactory() {
        return instance;
    }
}
