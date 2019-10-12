package xyz.stupidwolf.ioc.examaple;

public class JavaTypeBean {
    public void hello() {
        System.out.println("hello, i'm a bean define with @Configuration Annotation, hashCode@" + this.hashCode());
    }
}
