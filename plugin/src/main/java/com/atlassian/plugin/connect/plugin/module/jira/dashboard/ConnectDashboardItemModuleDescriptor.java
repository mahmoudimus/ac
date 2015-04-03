package com.atlassian.plugin.connect.plugin.module.jira.dashboard;

import com.atlassian.fugue.Option;
import com.atlassian.gadgets.plugins.DashboardItemModule;
import com.atlassian.gadgets.plugins.DashboardItemModuleDescriptor;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;

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
    private final DashboardItemModule.DirectoryDefinition directoryDefinition;
    private final DashboardItemModule module;

    public ConnectDashboardItemModuleDescriptor(final ModuleFactory moduleFactory, final DashboardItemModule.DirectoryDefinition directoryDefinition)
    {
        super(moduleFactory);
        this.directoryDefinition = directoryDefinition;
        this.module = new ConnectDirectoryModule(directoryDefinition);
    }

    @Override
    public Option<String> getGadgetSpecUriToReplace()
    {
        // Connect dashboard-item are not allowed to replace any of the existing gadgets
        return Option.none();
    }

    @Override
    public Option<DashboardItemModule.DirectoryDefinition> getDirectoryDefinition()
    {
        return Option.option(directoryDefinition);
    }

    @Override
    public DashboardItemModule getModule()
    {
        return module;
    }

    private static class ConnectDirectoryModule implements DashboardItemModule
    {
        private DirectoryDefinition directoryDefinition;

        private ConnectDirectoryModule(final DirectoryDefinition directoryDefinition)
        {
            this.directoryDefinition = directoryDefinition;
        }

        @Override
        public Option<DirectoryDefinition> getDirectoryDefinition()
        {
            return Option.some(directoryDefinition);
        }

        @Override
        public boolean isConfigurable()
        {
            return true;
        }

        @Override
        public Option<String> getAMDModule()
        {
            return Option.none();
        }

        @Override
        public void renderContent(final Writer writer, final Map<String, Object> map)
        {
            throw new UnsupportedOperationException("todo ");
        }

        @Nonnull
        @Override
        public Condition getCondition()
        {
            return ALWAYS_TRUE_CONDITION;
        }
    }

}
