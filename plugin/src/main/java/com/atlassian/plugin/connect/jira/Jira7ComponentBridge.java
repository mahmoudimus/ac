package com.atlassian.plugin.connect.jira;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.context.ApplicationContext;

/**
 * This class is full of OSGI/Spring hacks to Conditionally ComponentImport the UserPropertyService from JIRA
 * TODO: This class should be removed when JIRA 7 will hit OD with a simple ComponentImport in JiraImports class
 * This requires the JIRA api pom dependency to be bumped to JIRA 7
 */
@ExportAsService
@JiraComponent
public class Jira7ComponentBridge implements LifecycleAware
{
    private static final Logger logger = LoggerFactory.getLogger(Jira7ComponentBridge.class);

    private final ApplicationContext applicationContext;

    @Autowired
    public Jira7ComponentBridge(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onStart()
    {
        String userPropertyClassName = "com.atlassian.jira.bc.user.UserPropertyService";
        makeComponentAvailableInPluginContainer(userPropertyClassName, "userPropertyService");
    }

    /**
     * Makes the class available in the plugin container, it has to be available in the osgi context
     * @param className className to ComponentImport
     * @param id id of the component, by default Spring
     * @return true if component is available
     */
    private boolean makeComponentAvailableInPluginContainer(String className, String id)
    {
        try
        {
            Class<?> aClass = Class.forName(className);
            Object component = ComponentAccessor.getOSGiComponentInstanceOfType(aClass);

            SingletonBeanRegistry autowireCapableBeanFactory = (SingletonBeanRegistry) applicationContext.getAutowireCapableBeanFactory();

            autowireCapableBeanFactory.registerSingleton(id, component);

            return true;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }
}
