package xyz.stupidwolf.ioc.example;

import javax.inject.Singleton;

@Singleton
public class SimpleBean {
    public void hello() {
        System.out.println("hello, my-ioc!");
    }
}
