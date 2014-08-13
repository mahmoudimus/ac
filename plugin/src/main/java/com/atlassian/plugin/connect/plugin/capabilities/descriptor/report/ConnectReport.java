package com.atlassian.plugin.connect.plugin.capabilities.descriptor.report;

import com.atlassian.fugue.Option;
import com.atlassian.jira.plugin.report.Report;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParametersImpl;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;

import java.io.StringWriter;
import java.util.Map;

/**
 * @since 1.2
 */
public class ConnectReport implements Report
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final String addOnKey;
    private final String moduleKey;

    public ConnectReport(final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry, final String addOnKey, final String moduleKey)
    {
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.addOnKey = addOnKey;
        this.moduleKey = moduleKey;
    }

    @Override
    public void init(final ReportModuleDescriptor reportModuleDescriptor)
    {
    }

    @Override
    public void validate(final ProjectActionSupport projectActionSupport, final Map map)
    {
    }

    @Override
    public String generateReportHtml(final ProjectActionSupport projectActionSupport, final Map map) throws Exception
    {
        StringWriter sw = new StringWriter();
        IFrameRenderStrategy frameRenderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addOnKey, moduleKey);
        JiraModuleContextParameters moduleContextParameters = new JiraModuleContextParametersImpl();
        frameRenderStrategy.render(moduleContextParameters, sw, Option.<String>none());
        return sw.toString();
    }

    @Override
    public boolean isExcelViewSupported()
    {
        return false;
    }

    @Override
    public String generateReportExcel(final ProjectActionSupport projectActionSupport, final Map map) throws Exception
    {
        throw new UnsupportedOperationException("Excel view is not supported for Connect add-ons");
    }

    @Override
    public boolean showReport()
    {
        return true;
    }
}
