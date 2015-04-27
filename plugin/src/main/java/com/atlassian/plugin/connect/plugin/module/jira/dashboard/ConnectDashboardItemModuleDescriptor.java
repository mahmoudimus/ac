package com.atlassian.plugin.connect.plugin.module.jira.dashboard;

import com.atlassian.fugue.Option;
import com.atlassian.gadgets.plugins.DashboardItemModule;
import com.atlassian.gadgets.plugins.DashboardItemModule.DirectoryDefinition;
import com.atlassian.gadgets.plugins.DashboardItemModuleDescriptor;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.PluggableParametersExtractor;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Connect version of dashboard-item.
 */
public class ConnectDashboardItemModuleDescriptor extends AbstractModuleDescriptor<DashboardItemModule>
        implements DashboardItemModuleDescriptor
{
    private static final Logger log = LoggerFactory.getLogger(ConnectDashboardItemModuleDescriptor.class);
    private final DirectoryDefinition directoryDefinition;
    private final I18nProperty description;
    private final DashboardItemModule module;

    public ConnectDashboardItemModuleDescriptor(final ModuleFactory moduleFactory,
            final DirectoryDefinition directoryDefinition,
            final IFrameRenderStrategy renderStrategy,
            final ModuleContextFilter moduleContextFilter,
            final PluggableParametersExtractor parametersExtractor,
            final Boolean configurable,
            final I18nProperty description,
            final Condition condition)
    {
        super(moduleFactory);
        this.directoryDefinition = directoryDefinition;
        this.description = description;
        this.module = new ConnectDirectoryModule(parametersExtractor, directoryDefinition, renderStrategy, moduleContextFilter, configurable, condition);
    }

    @Override
    public Option<String> getGadgetSpecUriToReplace()
    {
        // Connect dashboard-item are not allowed to replace any of the existing gadgets
        return Option.none();
    }

    @Override
    public Option<DirectoryDefinition> getDirectoryDefinition()
    {
        return Option.option(directoryDefinition);
    }

    @Override
    public DashboardItemModule getModule()
    {
        return module;
    }

    @Override
    public String getDescription()
    {
        return description.getValue();
    }

    @Override
    public String getDescriptionKey()
    {
        return description.getI18n();
    }

    private static class ConnectDirectoryModule implements DashboardItemModule
    {
        private final Boolean configurable;
        private final Condition condition;
        private final DirectoryDefinition directoryDefinition;
        private final ModuleContextFilter moduleContextFilter;
        private final PluggableParametersExtractor moduleContextExtractor;
        private final IFrameRenderStrategy renderStrategy;

        private ConnectDirectoryModule(final PluggableParametersExtractor moduleContextExtractor,
                final DirectoryDefinition directoryDefinition,
                final IFrameRenderStrategy renderStrategy,
                final ModuleContextFilter moduleContextFilter,
                final Boolean configurable,
                final Condition condition)
        {
            this.moduleContextExtractor = moduleContextExtractor;
            this.directoryDefinition = directoryDefinition;
            this.renderStrategy = renderStrategy;
            this.moduleContextFilter = moduleContextFilter;
            this.configurable = configurable;
            this.condition = condition;
        }

        @Override
        public Option<DirectoryDefinition> getDirectoryDefinition()
        {
            return Option.some(directoryDefinition);
        }

        @Override
        public boolean isConfigurable()
        {
            return configurable;
        }

        @Override
        public Option<String> getAMDModule()
        {
            return Option.some("atlassian-connect/connect-dashboard-item");
        }

        @Override
        public void renderContent(final Writer writer, final Map<String, Object> context)
        {
            ModuleContextParameters unfilteredContext = moduleContextExtractor.extractParameters(context);
            ModuleContextParameters filteredContext = moduleContextFilter.filter(unfilteredContext);
            try
            {
                renderStrategy.render(filteredContext, writer, Option.<String>none());
            }
            catch (IOException e)
            {
                log.error("Error rendering dashboard item {} {}", directoryDefinition.getTitle(), e);
                try
                {
                    writer.write("Error rendering " + directoryDefinition.getTitle());
                }
                catch (IOException ex)
                {
                    log.error("Error rendering dashboard item error message {} {}", directoryDefinition.getTitle(), ex);
                }
            }
        }

        @Nonnull
        @Override
        public Condition getCondition()
        {
            return condition;
        }
    }

}
