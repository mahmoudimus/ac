package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.api.util.ConnectContainerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Looks up {@link AddonBeanValidator} implementations from the plugin system.
 */
@Component
public class DelegatingAddonBeanValidatorService implements AddonBeanValidatorService
{
    private final ConnectContainerUtil connectContainerUtil;

    @Autowired
    public DelegatingAddonBeanValidatorService(ConnectContainerUtil connectContainerUtil)
    {
        this.connectContainerUtil = connectContainerUtil;
    }

    @Override
    public void validate(final ConnectAddonBean addonBean) throws InvalidDescriptorException
    {
        for (AddonBeanValidator validator : connectContainerUtil.getBeansOfType(AddonBeanValidator.class))
        {
            validator.validate(addonBean);
        }
    }
}
