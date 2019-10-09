package xyz.stupidwolf.ioc.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import xyz.stupidwolf.ioc.annotation.Configuration;

import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class AnnotationVisitor {
    private static final Map<String, Class<? extends Annotation>> scanAnnotationMap = new HashMap<>();
    static {
        scanAnnotationMap.put("Lxyz/stupidwolf/ioc/annotation/Configuration;", Configuration.class);
        scanAnnotationMap.put("Ljavax/inject/Singleton;", Singleton.class);
    }

    public static boolean containScanAnnotation(String classFilePath) throws IOException {
        ClassReader classReader = new ClassReader(new FileInputStream(classFilePath));
        final boolean[] containScanAnnotation = {false};
        classReader.accept(new ClassVisitor(Opcodes.ASM7) {
            @Override
            public org.objectweb.asm.AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (visible && scanAnnotationMap.containsKey(descriptor)) {
                    containScanAnnotation[0] = true;
                }
                return super.visitAnnotation(descriptor, visible);
            }
        }, ClassReader.SKIP_DEBUG);

        return containScanAnnotation[0];
    }

}
