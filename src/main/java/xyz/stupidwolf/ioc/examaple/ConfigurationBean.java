package xyz.stupidwolf.ioc.examaple;

import xyz.stupidwolf.ioc.annotation.Bean;
import xyz.stupidwolf.ioc.annotation.Configuration;

import javax.inject.Named;

@Configuration
public class ConfigurationBean {
    @Bean
    public JavaTypeBean javaTypeBean1() {
        return new JavaTypeBean();
    }

    @Bean
    public JavaTypeBean javaTypeBean2() {
        return new JavaTypeBean();
    }

    @Bean
    @Named("javaTypeBeanCustom")
    public JavaTypeBean javaTypeBean3() {
        return new JavaTypeBean();
    }

    @Bean
    public JavaTypeBean javaTypeBean() {
        return new JavaTypeBean();
    }

    @Bean
    public JavaTypeBeanDependOtherBeans javaTypeBeanDependOtherBeans(@Named("simpleBean") SimpleBean simpleBean,
                                                                     @Named("javaTypeBean") JavaTypeBean javaTypeBean) {
        return new JavaTypeBeanDependOtherBeans(simpleBean, javaTypeBean);
    }

    @Bean
    public JavaTypeBean javaTypeBeanCircle(@Named("javaTypeBeanCircle") JavaTypeBean javaTypeBeanCircle) {
        return new JavaTypeBean();
    }
}
