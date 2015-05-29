package com.atlassian.plugin.connect.plugin.capabilities.validate;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.api.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Looks up {@link AddOnBeanValidator} implementations from the plugin system.
 */
@Component
public class DelegatingAddOnBeanValidatorService implements AddOnBeanValidatorService
{
    private final ConnectContainerUtil connectContainerUtil;

    @Autowired
    public DelegatingAddOnBeanValidatorService(ConnectContainerUtil connectContainerUtil)
    {
        this.connectContainerUtil = connectContainerUtil;
    }

    @Override
    public void validate(final ConnectAddonBean addOnBean) throws InvalidDescriptorException
    {
        for (AddOnBeanValidator validator : connectContainerUtil.getBeansOfType(AddOnBeanValidator.class))
        {
            validator.validate(addOnBean);
        }
    }
}
