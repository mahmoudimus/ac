package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.api.FormatConverter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Set;


@Component
public class FormatConverterFactoryBean implements FactoryBean
{

    @Override
    public Object getObject() throws Exception
    {
        return new FormatConverter();
    }

    @Override
    public Class getObjectType()
    {
        return FormatConverter.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
