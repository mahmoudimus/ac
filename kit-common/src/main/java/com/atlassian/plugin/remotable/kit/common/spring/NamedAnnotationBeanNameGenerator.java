package com.atlassian.plugin.remotable.kit.common.spring;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.inject.Named;
import java.beans.Introspector;
import java.util.Map;
import java.util.Set;

/**
 * Supports generating the component name with @Named instead of Component
 */
public class NamedAnnotationBeanNameGenerator implements BeanNameGenerator
{
    private static final String COMPONENT_ANNOTATION_CLASSNAME = Named.class.getName();


    	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
    		if (definition instanceof AnnotatedBeanDefinition) {
    			String beanName = determineBeanNameFromAnnotation((AnnotatedBeanDefinition) definition);
    			if (StringUtils.hasText(beanName)) {
    				// Explicit bean name found.
    				return beanName;
    			}
    		}
    		// Fallback: generate a unique default bean name.
    		return buildDefaultBeanName(definition);
    	}

    	/**
    	 * Derive a bean name from one of the annotations on the class.
    	 * @param annotatedDef the annotation-aware bean definition
    	 * @return the bean name, or <code>null</code> if none is found
    	 */
    	protected String determineBeanNameFromAnnotation(AnnotatedBeanDefinition annotatedDef) {
    		AnnotationMetadata amd = annotatedDef.getMetadata();
    		Set<String> types = amd.getAnnotationTypes();
    		String beanName = null;
    		for (String type : types) {
    			Map<String, Object> attributes = amd.getAnnotationAttributes(type);
    			if (isStereotypeWithNameValue(type, amd.getMetaAnnotationTypes(type), attributes)) {
    				String value = (String) attributes.get("value");
    				if (StringUtils.hasLength(value)) {
    					if (beanName != null && !value.equals(beanName)) {
    						throw new IllegalStateException("Stereotype annotations suggest inconsistent " +
    								"component names: '" + beanName + "' versus '" + value + "'");
    					}
    					beanName = value;
    				}
    			}
    		}
    		return beanName;
    	}

    	/**
    	 * Check whether the given annotation is a stereotype that is allowed
    	 * to suggest a component name through its annotation <code>value()</code>.
    	 * @param annotationType the name of the annotation class to check
    	 * @param metaAnnotationTypes the names of meta-annotations on the given annotation
    	 * @param attributes the map of attributes for the given annotation
    	 * @return whether the annotation qualifies as a stereotype with component name
    	 */
    	protected boolean isStereotypeWithNameValue(String annotationType,
    			Set<String> metaAnnotationTypes, Map<String, Object> attributes) {

    		boolean isStereotype = annotationType.equals(COMPONENT_ANNOTATION_CLASSNAME) ||
    				(metaAnnotationTypes != null && metaAnnotationTypes.contains(COMPONENT_ANNOTATION_CLASSNAME));
    		return (isStereotype && attributes != null && attributes.containsKey("value"));
    	}

    	/**
    	 * Derive a default bean name from the given bean definition.
    	 * <p>The default implementation simply builds a decapitalized version
    	 * of the short class name: e.g. "mypackage.MyJdbcDao" -> "myJdbcDao".
    	 * <p>Note that inner classes will thus have names of the form
    	 * "outerClassName.innerClassName", which because of the period in the
    	 * name may be an issue if you are autowiring by name.
    	 * @param definition the bean definition to build a bean name for
    	 * @return the default bean name (never <code>null</code>)
    	 */
    	protected String buildDefaultBeanName(BeanDefinition definition) {
    		String shortClassName = ClassUtils.getShortName(definition.getBeanClassName());
    		return Introspector.decapitalize(shortClassName);
    	}
}
