package xyz.stupidwolf.ioc.context;

import xyz.stupidwolf.ioc.asm.AnnotationVisitor;
import xyz.stupidwolf.ioc.factory.config.BeanDefinition;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

public class ClassPathBeanDefinitionScanner {
    public Set<BeanDefinition> scan(String basePackage) {
        Set<BeanDefinition> beanDefinitions = new LinkedHashSet<>();
        try {
            Set<File> allScanClassFiles = getAllScanClassFiles(basePackage);

            for (File file : allScanClassFiles) {
                // 读取class
                // 将其解析为 BeanDefinition 支持的格式

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return beanDefinitions;
    }


    public Set<File> getAllScanClassFiles(String basePackage) throws IOException {
        Set<File> annotationClassFiles = new LinkedHashSet<>();
        ClassLoader classLoader = getClassLoader();
        Enumeration<URL> urlEnumeration = (classLoader != null ? classLoader.getResources("")
                :ClassLoader.getSystemResources(""));
        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();
            String filePath = url.getPath();
            File scanDir = new File(filePath, basePackage);
            // 将使用注解定义的class file添加进来
            if (scanDir.exists()) {
                getAllScanClassFile(scanDir, annotationClassFiles);
            }
        }
        return annotationClassFiles;
    }

    private void getAllScanClassFile(File baseFile, Set<File> classFiles) throws IOException {
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
                getAllScanClassFile(file, classFiles);
            }
        } else {
            // 识别使用注解的class文件 XXX
            if (AnnotationVisitor.containScanAnnotation(baseFile.getAbsolutePath())) {
                classFiles.add(baseFile);
            }
        }
    }


    public ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }
}
