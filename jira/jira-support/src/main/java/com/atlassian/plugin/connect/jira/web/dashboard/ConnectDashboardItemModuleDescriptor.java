package com.atlassian.plugin.connect.jira.web.dashboard;

import com.atlassian.fugue.Option;
import com.atlassian.gadgets.plugins.DashboardItemModule;
import com.atlassian.gadgets.plugins.DashboardItemModule.DirectoryDefinition;
import com.atlassian.gadgets.plugins.DashboardItemModuleDescriptor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Optional;

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
            final ConnectDashboardItemContextParameterMapper dashboardItemContextParameterMapper,
            final Boolean configurable,
            final I18nProperty description,
            final Condition condition)
    {
        super(moduleFactory);
        this.directoryDefinition = directoryDefinition;
        this.description = description;
        this.module = new ConnectDashboardItemModule(dashboardItemContextParameterMapper, directoryDefinition, renderStrategy, configurable, condition);
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

    private static class ConnectDashboardItemModule implements DashboardItemModule
    {

        private ConnectDashboardItemContextParameterMapper dashboardItemContextParameterMapper;
        private final Boolean configurable;
        private final Condition condition;
        private final DirectoryDefinition directoryDefinition;
        private final IFrameRenderStrategy renderStrategy;

        private ConnectDashboardItemModule(final ConnectDashboardItemContextParameterMapper dashboardItemContextParameterMapper,
                final DirectoryDefinition directoryDefinition,
                final IFrameRenderStrategy renderStrategy,
                final Boolean configurable,
                final Condition condition)
        {
            this.dashboardItemContextParameterMapper = dashboardItemContextParameterMapper;
            this.directoryDefinition = directoryDefinition;
            this.renderStrategy = renderStrategy;
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
        public Option<String> getWebResourceKey() {
            return Option.none() ;
        }

        @Override
        public void renderContent(final Writer writer, final Map<String, Object> context)
        {

            Map<String, String> contextParameters = dashboardItemContextParameterMapper.extractContextParameters(context);
            try
            {
                renderStrategy.render(contextParameters, writer, Optional.empty());
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
