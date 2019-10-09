package xyz.stupidwolf.ioc.context;


import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

public class ClassPathBeanDefinitionScannerTest {
    private ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner();
    @Test
    public void getAllClassFiles() throws IOException {
//        String basePackage = "xyz.stupidwolf.ioc.example";
        String basePackage = "xyz.stupidwolf.ioc";
        basePackage = basePackage.replaceAll("\\.", File.separator);
        Set<File> files = classPathBeanDefinitionScanner.getAllScanClassFiles(basePackage);
        for (File file : files) {
//            System.out.println(file.getCanonicalPath());
            System.out.println(file.getName());
//            System.out.println(file.getPath());
        }
    }


    @Test
    public void testClassPath() {
        URL url = this.getClass().getClassLoader().getResource(".");
        System.out.println(url.getFile());
        System.out.println(url.getPath());
        System.out.println(url.getFile());
    }

    @Test
    public void testClassPath2() throws IOException {
        String name = "";
        Enumeration<URL> urlEnumeration = this.getClass().getClassLoader().getResources(name);

        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();
            System.out.println(url);
        }
    }

    @Test
    public void testASM() throws IOException, ClassNotFoundException {
        String filePath = "/Users/chenmingli/IdeaProjects/my-ioc/target/classes/xyz/stupidwolf/ioc/examaple/BeanConfiguration.class";
        ClassReader classReader = new ClassReader(new FileInputStream(filePath));

        classReader.accept(new ClassVisitor(Opcodes.ASM7) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                System.out.println(descriptor + " : " + visible);
                return super.visitAnnotation(descriptor, visible);
            }
        }, ClassReader.SKIP_DEBUG);
        System.out.println(classReader.getClassName());
        Class<?> clazz = Class.forName(classReader.getClassName());
        System.out.println(clazz);
//        System.out.println();
    }


    @Test
    public void test() {
        System.out.println(String.class.getTypeName());
    }
}
