package com.atlassian.plugin.connect.plugin.dialog;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.AbstractWebPanel;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogOptionsWebPanel extends AbstractWebPanel implements WebPanel
{
    private static final Logger log = LoggerFactory.getLogger(DialogOptionsWebPanel.class);

    private static final String OPTION_TEMPLATE = "/velocity/dialog-options.vm";
    private final PluginAccessor pluginAccessor;
    private final TemplateRenderer templateRenderer;

    public DialogOptionsWebPanel(PluginAccessor pluginAccessor, TemplateRenderer templateRenderer)
    {
        super(pluginAccessor);

        this.pluginAccessor = pluginAccessor;
        this.templateRenderer = templateRenderer;
    }

    @Override
    public String getHtml(Map<String, Object> context)
    {
        List<WebItemModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(WebItemModuleDescriptor.class);
        Map<String, Map<String, String>> keyedDialogOptions = new HashMap<String, Map<String, String>>();
        Map<String, Map<String, String>> keyedInlineDialogOptions = new HashMap<String, Map<String, String>>();

        for (WebItemModuleDescriptor descriptor : descriptors)
        {
            if (descriptor.getStyleClass().contains("ap-dialog"))
            {
                addDialogOptions(descriptor, keyedDialogOptions);
            }
            else if (descriptor.getStyleClass().contains("ap-inline-dialog"))
            {
                addDialogOptions(descriptor, keyedInlineDialogOptions);
            }
        }
        context.put("acDialogOptions", keyedDialogOptions);
        context.put("acInlineDialogOptions", keyedInlineDialogOptions);

        StringWriter output = new StringWriter();

        try
        {
            templateRenderer.render(OPTION_TEMPLATE, context, output);

            return output.toString();
        }
        catch (IOException e)
        {
            log.error("Failed to render web panel: " + getClass().getSimpleName(), e);
            return "";
        }
    }

    private void addDialogOptions(WebItemModuleDescriptor descriptor, Map<String, Map<String, String>> keyedOptions)
    {
        for (Map.Entry<String, String> param : descriptor.getParams().entrySet())
        {
            if (param.getKey().startsWith(WebItemModuleDescriptorFactory.DIALOG_OPTION_PREFIX))
            {
                addOption(keyedOptions, descriptor.getKey(), param.getKey(), param.getValue());
            }
        }
    }

    private void addOption(Map<String, Map<String, String>> keyedOptions, String moduleKey, String key, String value)
    {
        if (!keyedOptions.containsKey(moduleKey))
        {
            keyedOptions.put(moduleKey, new HashMap<String, String>());
        }

        keyedOptions.get(moduleKey).put(StringUtils.substringAfter(key, WebItemModuleDescriptorFactory.DIALOG_OPTION_PREFIX), value);
    }

}
