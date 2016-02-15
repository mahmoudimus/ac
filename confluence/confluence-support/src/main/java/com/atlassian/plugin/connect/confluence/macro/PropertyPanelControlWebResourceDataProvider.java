package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.json.marshal.Jsonable;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ControlBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroPropertyPanelBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.webresource.api.data.WebResourceDataProvider;
import com.google.gson.Gson;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * A {@link com.atlassian.webresource.api.data.WebResourceDataProvider} that defines all property panel controls.
 *
 * This data provider is used by the autoconvert javascript to link the autoconvert configuration in all connect add-ons
 * and the autoconvert logic in the tiny mce editor.
 */
public class PropertyPanelControlWebResourceDataProvider implements WebResourceDataProvider
{
    private final ConnectAddonAccessor addonAccessor;

    public PropertyPanelControlWebResourceDataProvider(ConnectAddonAccessor addonAccessor)
    {
        this.addonAccessor = addonAccessor;
    }

    @Override
    public Jsonable get()
    {
        return writer -> {
            Map<String, Map<String, List<ControlBean>>> beans = addonAccessor.getAllAddons().stream()
                    .collect(toMap(ConnectAddonBean::getKey,
                            addon -> retrieveMacros(addon)
                                    .collect(toMap(DynamicContentMacroModuleBean::getRawKey,
                                            macro -> retrievePropertyPanelControls(macro)
                                                    .collect(Collectors.toList())))));

            Gson gson = ConnectModulesGsonFactory.getGson();

            writer.write(gson.toJson(beans));
        };
    }

    private Stream<DynamicContentMacroModuleBean> retrieveMacros(ConnectAddonBean addon)
    {
        return optionalListToStream(addon
                .getModules()
                .getValidModuleListOfType(new DynamicContentMacroModuleMeta().getDescriptorKey(), (ex) -> {}))
                .map(DynamicContentMacroModuleBean.class::cast);
    }

    private Stream<ControlBean> retrievePropertyPanelControls(DynamicContentMacroModuleBean macro)
    {
        return optionalListToStream(getControls(getPropertyPanel(macro)));
    }

    private Optional<MacroPropertyPanelBean> getPropertyPanel(DynamicContentMacroModuleBean macroBean)
    {
        return Optional.of(macroBean.getPropertyPanel());
    }

    private Optional<List<ControlBean>> getControls(Optional<MacroPropertyPanelBean> propertyPanelBean)
    {
        return propertyPanelBean.map(MacroPropertyPanelBean::getControls);
    }

    public static <T> Stream<T> optionalListToStream(Optional<List<T>> optional) {
        return optional.map(List::stream).orElse(Stream.empty());
    }
}
