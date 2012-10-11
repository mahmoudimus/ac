package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.plugin.remotable.host.common.util.FormatConverter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;


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
