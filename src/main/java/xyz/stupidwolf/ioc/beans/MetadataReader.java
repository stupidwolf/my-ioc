package xyz.stupidwolf.ioc.beans;

import java.lang.reflect.AnnotatedType;

public interface MetadataReader {
    AnnotatedType[] getAnnotationMetadata();
}
