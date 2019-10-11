package xyz.stupidwolf.ioc.examaple;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class CircularDependBeanByConstructor {
    private CircularDependBeanByConstructor circularDependBeanByConstructor;

    @Inject
    public CircularDependBeanByConstructor(@Named(value = "circularDependBeanByConstructor")
                                                   CircularDependBeanByConstructor circularDependBeanByConstructor) {
        this.circularDependBeanByConstructor = circularDependBeanByConstructor;
    }

    public void hello() {
        System.out.println("test for circular depend bean by constructor.");
    }
}
