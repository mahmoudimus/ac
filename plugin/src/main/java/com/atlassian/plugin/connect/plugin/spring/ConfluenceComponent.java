package com.atlassian.plugin.connect.plugin.spring;

import java.lang.annotation.*;

import org.springframework.stereotype.Component;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ConfluenceComponent
{

}
