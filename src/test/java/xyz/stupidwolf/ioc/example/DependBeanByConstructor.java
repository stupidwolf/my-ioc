package xyz.stupidwolf.ioc.example;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class DependBeanByConstructor {
    private SimpleBean simpleBean;

    @Inject
    public DependBeanByConstructor(@Named("simpleBean") SimpleBean simpleBean) {
        this.simpleBean = simpleBean;
    }

    public void hello() {
        System.out.println("test for depend bean by constructor");
        simpleBean.hello();
    }
}
