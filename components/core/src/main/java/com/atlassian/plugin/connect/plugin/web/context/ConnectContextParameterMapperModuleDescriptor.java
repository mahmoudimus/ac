package com.atlassian.plugin.connect.plugin.web.context;

import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationPattern;

import static com.atlassian.plugin.util.validation.ValidationPattern.test;

public class ConnectContextParameterMapperModuleDescriptor extends AbstractModuleDescriptor<ConnectContextParameterMapper>
{

    private ConnectContextParameterMapper contextParameterMapper;

    public ConnectContextParameterMapperModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    protected void provideValidationRules(ValidationPattern pattern)
    {
        super.provideValidationRules(pattern);
        pattern
                .rule(
                        test("@class and string-length(@class) > 0")
                                .withError("A resolver class must be specified via the 'class' attribute"));
    }

    @Override
    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(ConnectContextParameterMapper.class);
        contextParameterMapper = moduleFactory.createModule(moduleClassName, this);
    }

    @Override
    public ConnectContextParameterMapper getModule()
    {
        return contextParameterMapper;
    }
}
