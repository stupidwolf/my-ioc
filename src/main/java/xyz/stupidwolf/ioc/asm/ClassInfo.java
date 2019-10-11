package xyz.stupidwolf.ioc.asm;

import java.lang.annotation.Annotation;
import java.util.List;

public class ClassInfo {
    private String className;

    private List<Class<? extends Annotation>> classAnnotations;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<Class<? extends Annotation>> getClassAnnotations() {
        return classAnnotations;
    }

    public void setClassAnnotations(List<Class<? extends Annotation>> classAnnotations) {
        this.classAnnotations = classAnnotations;
    }
}
