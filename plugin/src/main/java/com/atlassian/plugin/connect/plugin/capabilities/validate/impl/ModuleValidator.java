package com.atlassian.plugin.connect.plugin.capabilities.validate.impl;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.validate.AddOnBeanValidator;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.installer.ModuleDeserializationException;

import javax.inject.Named;

/**
 * Delegates to each individual module for validation.
 */
@Named("module-validator")
public class ModuleValidator implements AddOnBeanValidator
{
    @Override
    public void validate(ConnectAddonBean addon)
    {
        try
        {
            addon.getModules();
        }
        catch (ModuleDeserializationException e)
        {
            throw new InvalidDescriptorException(e.getMessage());
        }
    }
}
