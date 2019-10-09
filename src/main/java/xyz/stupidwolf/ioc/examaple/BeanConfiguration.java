package xyz.stupidwolf.ioc.examaple;

import xyz.stupidwolf.ioc.annotation.Bean;
import xyz.stupidwolf.ioc.annotation.Configuration;

import javax.inject.Scope;
import javax.inject.Singleton;

@Configuration
public class BeanConfiguration {
    @Bean(value = "classA1")
    public ClassA classA1() {
        return new ClassA();
    }

    @Bean(value = "classA2")
    public ClassA classA2() {
        return new ClassA();
    }

    @Bean
    public ClassB classB(ClassA classA) {
        return new ClassB(classA);
    }
}
