package com.atlassian.plugin.connect.jira.report;

import com.atlassian.jira.plugin.report.Report;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.web.context.JiraModuleContextParameters;
import com.atlassian.plugin.connect.jira.web.context.JiraModuleContextParametersImpl;

import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;

/**
 * @since 1.2
 */
public class ConnectReport implements Report {
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final String addonKey;
    private final String moduleKey;

    public ConnectReport(final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry, final String addonKey, final String moduleKey) {
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.addonKey = addonKey;
        this.moduleKey = moduleKey;
    }

    @Override
    public void init(final ReportModuleDescriptor reportModuleDescriptor) {
    }

    @Override
    public void validate(final ProjectActionSupport projectActionSupport, final Map map) {
    }

    @Override
    public String generateReportHtml(final ProjectActionSupport projectActionSupport, final Map map) throws Exception {
        StringWriter sw = new StringWriter();
        IFrameRenderStrategy frameRenderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKey, moduleKey);
        JiraModuleContextParameters moduleContextParameters = createJiraModuleContextParameters(map);
        frameRenderStrategy.render(moduleContextParameters, sw, Optional.empty());
        return sw.toString();
    }

    @Override
    public boolean isExcelViewSupported() {
        return false;
    }

    @Override
    public String generateReportExcel(final ProjectActionSupport projectActionSupport, final Map map) throws Exception {
        throw new UnsupportedOperationException("Excel view is not supported for Connect add-ons");
    }

    @Override
    public boolean showReport() {
        return true;
    }

    @SuppressWarnings("unchecked")
    private JiraModuleContextParametersImpl createJiraModuleContextParameters(Map map) {
        return new JiraModuleContextParametersImpl(map);
    }
}
