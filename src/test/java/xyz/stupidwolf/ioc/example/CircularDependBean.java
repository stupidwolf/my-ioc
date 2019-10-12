package xyz.stupidwolf.ioc.example;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CircularDependBean {
    @Inject
    private CircularDependBean circularDependBean;

    public void hello() {
        System.out.println("hello,my-ioc! for a circular depend test.");
    }
}
