package com.atlassian.plugin.connect.plugin.web.dialog;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

public class DialogModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private DialogOptions options;

    public DialogModuleDescriptor()
    {
        super(ModuleFactory.LEGACY_MODULE_FACTORY);
    }

    @Override
    public Void getModule()
    {
        return null;
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);
    }
}
