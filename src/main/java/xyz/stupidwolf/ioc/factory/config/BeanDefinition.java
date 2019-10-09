package xyz.stupidwolf.ioc.factory.config;

import xyz.stupidwolf.ioc.beans.PropertyValue;

import java.util.Collection;

public interface BeanDefinition {

    Class getBeanClass();

    void setBeanClass();

    boolean isSingleton();

    boolean isPrototype();

    void setLazyInit(boolean lazyInit);

    boolean isLazyInit();

    void setDependsOn(String... dependsOn);

    String[] getDependsOn();

    // TODO
    Collection<PropertyValue> getPropertyValues();
}
