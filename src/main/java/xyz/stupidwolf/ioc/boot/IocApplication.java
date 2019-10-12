package xyz.stupidwolf.ioc.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.stupidwolf.ioc.annotation.IocBoot;
import xyz.stupidwolf.ioc.context.ClassPathBeanDefinitionScanner;
import xyz.stupidwolf.ioc.factory.BeanDefinition;
import xyz.stupidwolf.ioc.factory.DefaultBeanFactory;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            if (scanBase.length() > 0) {
                scanBase = File.separator + scanBase.replaceAll("\\.", File.separator);
            }

            Set<String> scanPaths = new HashSet<>();
            // 扫描使用了@IocBoot注解下class文件
            URL url = primarySource.getResource(scanBase);
            if (url == null) {
                logger.warn("class dir: {} not exist!", scanBase);
            } else {
                scanPaths.add(primarySource.getResource(scanBase).getFile());
            }

//            try {
//                Enumeration<URL> urlEnumeration = primarySource.getClassLoader().getResources(scanBase);
//                while (urlEnumeration.hasMoreElements()) {
//                    scanPaths.add(urlEnumeration.nextElement().getPath());
//                }
//            } catch (IOException e) {
//               // ignore
//               logger.warn("get resource by class loader fail: ", e);
//            }

            for (String baseScanPackage : scanPaths) {
                // 扫描指定包下包含的bean定义信息
                logger.debug("scan bean definition at path: {}", baseScanPackage);
                List<BeanDefinition> beanDefinitions = classPathBeanDefinitionScanner.scan(baseScanPackage);
                for (BeanDefinition beanDefinition : beanDefinitions) {
                    defaultBeanFactory.registerBeanDefinition(beanDefinition.getBeanName(), beanDefinition);
                }
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
