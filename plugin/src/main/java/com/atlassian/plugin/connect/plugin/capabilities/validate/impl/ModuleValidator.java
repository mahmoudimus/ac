package com.atlassian.plugin.connect.plugin.capabilities.validate.impl;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.validate.AddOnBeanValidator;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.installer.ModuleDeserializationException;
import com.google.common.base.Supplier;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * Delegates to each individual module for validation.
 */
@Named("module-validator")
public class ModuleValidator implements AddOnBeanValidator
{
    @Override
    public void validate(ConnectAddonBean addOn)
    {
        for (Map.Entry<String, Supplier<List<ModuleBean>>> entry : addOn.getModules().entrySet())
        {
            try
            {
                entry.getValue().get();
            }
            catch (ModuleDeserializationException e)
            {
                throw new InvalidDescriptorException(e.getMessage());
            }
        }
    }
    
}
