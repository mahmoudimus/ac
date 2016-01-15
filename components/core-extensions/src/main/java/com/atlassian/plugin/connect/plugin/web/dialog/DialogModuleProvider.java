package com.atlassian.plugin.connect.plugin.web.dialog;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.DialogModuleBean;
import com.atlassian.plugin.connect.modules.beans.DialogModuleMeta;
import com.atlassian.plugin.connect.plugin.AbstractConnectCoreModuleProvider;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class DialogModuleProvider extends AbstractConnectCoreModuleProvider<DialogModuleBean>
{
    private static final DialogModuleMeta META = new DialogModuleMeta();

    @Autowired
    public DialogModuleProvider(PluginRetrievalService pluginRetrievalService,
                                ConnectJsonSchemaValidator schemaValidator)
    {
        super(pluginRetrievalService, schemaValidator);
    }

    @Override
    public ConnectModuleMeta<DialogModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<DialogModuleBean> modules, ConnectAddonBean addon)
    {
        return modules.stream().map(module ->
        {
            ModuleDescriptor descriptor = new DialogModuleDescriptor();
            descriptor.init(pluginRetrievalService.getPlugin(), buildModuleElement(module, addon));
            return descriptor;
        }).collect(toList());
    }

    private Element buildModuleElement(DialogModuleBean module, ConnectAddonBean addon) {
        // TODO - how much of this is required if the clients of this module use the bean directly? dT
        return new DOMElement("connectDialog")
                .addAttribute("key", module.getKey(addon));
    }
}
