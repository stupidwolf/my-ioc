package xyz.stupidwolf.ioc.examaple;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class UserService {
    @Inject
    @Named("classA1")
    private ClassA classA1;
    @Named("classA2")
    private ClassA classA2;
    @Inject
    private ClassB classB;


}
