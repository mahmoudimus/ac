package com.atlassian.plugin.connect.plugin.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.ProductFilter;

import org.atteo.evo.classindex.IndexAnnotated;
import org.springframework.stereotype.Component;
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface JiraComponent
{

}
