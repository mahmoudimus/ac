package com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationPattern;

import static com.atlassian.plugin.util.validation.ValidationPattern.test;

public class ConnectConditionClassResolverModuleDescriptor extends AbstractModuleDescriptor<ConnectConditionClassResolver> {

    private ConnectConditionClassResolver resolver;

    public ConnectConditionClassResolverModuleDescriptor(ModuleFactory moduleFactory) {
        super(moduleFactory);
    }

    @Override
    protected void provideValidationRules(ValidationPattern pattern) {
        super.provideValidationRules(pattern);
        pattern
                .rule(
                        test("@class and string-length(@class) > 0")
                                .withError("A resolver class must be specified via the 'class' attribute"));
    }

    @Override
    public void enabled() {
        super.enabled();
        assertModuleClassImplements(ConnectConditionClassResolver.class);
        resolver = moduleFactory.createModule(moduleClassName, ConnectConditionClassResolverModuleDescriptor.this);
    }

    @Override
    public ConnectConditionClassResolver getModule() {
        return resolver;
    }
}
