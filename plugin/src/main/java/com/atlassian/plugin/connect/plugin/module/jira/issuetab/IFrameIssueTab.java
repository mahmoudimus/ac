package com.atlassian.plugin.connect.plugin.module.jira.issuetab;

import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel3;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.IssueSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.web.Condition;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.module.jira.JiraTabConditionContext.createConditionContext;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An issue tab that displays an iframe but isn't included in the all tab
 */
public class IFrameIssueTab extends AbstractIssueTabPanel3
{
    private static final Logger log = LoggerFactory.getLogger(IFrameIssueTab.class);
    private final IFrameRendererImpl iFrameRenderer;
    private final Optional<Condition> condition;
    private final IFrameContext iFrameContext;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ProjectSerializer projectSerializer;
    private final IssueSerializer issueSerializer;

    public IFrameIssueTab(IFrameContext iFrameContext, IFrameRendererImpl iFrameRenderer, Optional<Condition> condition, UrlVariableSubstitutor urlVariableSubstitutor,
            ProjectSerializer projectSerializer, IssueSerializer issueSerializer)
    {
        this.projectSerializer = checkNotNull(projectSerializer);
        this.issueSerializer = checkNotNull(issueSerializer);
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
        this.iFrameContext = checkNotNull(iFrameContext);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.condition = checkNotNull(condition);
    }

    @Override
    public List<IssueAction> getActions(final GetActionsRequest request)
    {
        return ImmutableList.<IssueAction>of(new IFrameIssueAction(request, iFrameRenderer, iFrameContext));
    }

    public class IFrameIssueAction implements IssueAction
    {
        private final GetActionsRequest request;
        private final IFrameRenderer iFrameRenderer;
        private final IFrameContext iFrameContext;

        public IFrameIssueAction(GetActionsRequest request, IFrameRenderer iFrameRenderer, IFrameContext iFrameContext)
        {
            this.request = request;
            this.iFrameRenderer = iFrameRenderer;
            this.iFrameContext = iFrameContext;
        }

        @Override
        public String getHtml()
        {
            StringWriter writer = new StringWriter();
            try
            {
                writer.write(iFrameRenderer.render(substituteContext(getParams()),
                        getUserName()));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            return writer.toString();
        }

        private Map<String, Object> getParams()
        {
            ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            builder.putAll(projectSerializer.serialize(request.issue().getProjectObject()));
            builder.putAll(issueSerializer.serialize(request.issue()));
            // deprecated AC-702
            builder.put("ctx_issue_key", request.issue().getKey());
            return builder.build();
        }

        private IFrameContext substituteContext(final Map<String, Object> paramsMap)
        {
            final String urlWithSubstitutedParameters = urlVariableSubstitutor.replace(iFrameContext.getIframePath(), paramsMap);

            return new IFrameContextImpl(iFrameContext.getPluginKey(), urlWithSubstitutedParameters, iFrameContext.getNamespace(), iFrameContext.getIFrameParams());
        }

        private String getUserName()
        {
            return request.remoteUser() != null ? request.remoteUser().getName() : null;
        }

        @Override
        public Date getTimePerformed()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDisplayActionAllTab()
        {
            return false;
        }
    }

    @Override
    public boolean showPanel(final ShowPanelRequest request)
    {
        return !condition.isPresent() || condition.get().shouldDisplay(createConditionContext(request));

    }
}
