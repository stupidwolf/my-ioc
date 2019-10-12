package xyz.stupidwolf.ioc.example;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class MixDependBean {
    @Inject
    private SimpleBean simpleBean;

    private CircularDependBean circularDependBean;

    @Inject
    public MixDependBean(@Named("circularDependBean") CircularDependBean circularDependBean) {
        this.circularDependBean = circularDependBean;
    }

    public void hello() {
        this.simpleBean.hello();
        this.circularDependBean.hello();
    }
}
