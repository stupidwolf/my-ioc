package xyz.stupidwolf.ioc.example;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class DependBeanByField {
    @Inject
    @Named("simpleBean")
    private SimpleBean simpleBeanDelegation;

    public void hello() {
        System.out.println("call simpleBean at DependBeanByField: ");
        simpleBeanDelegation.hello();
    }
}
