package xyz.stupidwolf.ioc.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import xyz.stupidwolf.ioc.annotation.Configuration;

import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClassInfoVisitor {
    private static final Map<String, Class<? extends Annotation>> scanAnnotationMap = new HashMap<>();
    static {
        scanAnnotationMap.put("Lxyz/stupidwolf/ioc/annotation/Configuration;", Configuration.class);
        scanAnnotationMap.put("Ljavax/inject/Singleton;", Singleton.class);
    }

    /**
     *
     * @param classFilePath class file path
     * @return class 基本信息
     * @throws IOException file not found or file operation is illegal.
     */
    public static ClassInfo getClassInfoWithDeclareAnnotation(String classFilePath) throws IOException {
        ClassReader classReader = new ClassReader(new FileInputStream(classFilePath));
        final ClassInfo[] classInfo = {null};
        classReader.accept(new org.objectweb.asm.ClassVisitor(Opcodes.ASM7) {
            @Override
            public org.objectweb.asm.AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (visible && scanAnnotationMap.containsKey(descriptor)) {
                    if (classInfo[0] == null) {
                        classInfo[0] = new ClassInfo();
                    }
                    // 将类描述符转换为能被类加载器加载的class name.
                    // XXX 不太好的实现
                    String className = classReader.getClassName();
                    if (className.startsWith("L")) {
                        className = descriptor.substring(1);
                    }
                    if (className.endsWith(";")) {
                        className = descriptor.substring(0, className.length() - 1);
                    }
                    classInfo[0].setClassName(className.replaceAll("/", "\\."));

                    // 设置 class所包含的类注解
                    if (classInfo[0].getClassAnnotations() == null) {
                        classInfo[0].setClassAnnotations(new ArrayList<>());
                    }
                    classInfo[0].getClassAnnotations().add(scanAnnotationMap.get(descriptor));
                }
                return super.visitAnnotation(descriptor, visible);
            }
        }, ClassReader.SKIP_DEBUG);

        return classInfo[0];
    }

}
