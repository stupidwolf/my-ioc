package xyz.stupidwolf.ioc.example;

public class JavaTypeBeanDependOtherBeans {
    private SimpleBean simpleBean;
    private JavaTypeBean javaTypeBean;

    public JavaTypeBeanDependOtherBeans(SimpleBean simpleBean, JavaTypeBean javaTypeBean) {
        this.simpleBean = simpleBean;
        this.javaTypeBean = javaTypeBean;
    }

    public void hello() {
        simpleBean.hello();
        javaTypeBean.hello();
    }
}
