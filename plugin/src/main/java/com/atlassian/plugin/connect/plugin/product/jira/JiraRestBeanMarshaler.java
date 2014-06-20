package com.atlassian.plugin.connect.plugin.product.jira;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.rest.v2.issue.IncludedFields;
import com.atlassian.jira.rest.v2.issue.IssueBean;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.sun.jersey.api.uri.UriBuilderImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class JiraRestBeanMarshaler
{
    private final JiraBaseUrls jiraBaseUrls;
    private final ProjectRoleManager projectRoleManager;
    private final BeanBuilderFactory beanBuilderFactory;

    @Autowired
    public JiraRestBeanMarshaler(JiraBaseUrls jiraBaseUrls,
                                 ProjectRoleManager projectRoleManager,
                                 BeanBuilderFactory beanBuilderFactory)
    {
        this.jiraBaseUrls = jiraBaseUrls;
        this.projectRoleManager = projectRoleManager;
        this.beanBuilderFactory = beanBuilderFactory;
    }

    public JSONObject getRemoteIssue(final Issue issue)
    {
        final DefaultJaxbJsonMarshaller jsonMarshaller = new DefaultJaxbJsonMarshaller();
        IssueBean issueBean = beanBuilderFactory.newIssueBeanBuilder(issue, IncludedFields.includeAllByDefault(null))
                .uriBuilder(new UriBuilderImpl())
                .build();
        String text = jsonMarshaller.marshal(issueBean);
        try
        {
            return new JSONObject(text);
        }
        catch (JSONException e)
        {
            throw new IllegalStateException(e);
        }

    }

    public JSONObject getRemoteComment(Comment comment)
    {
        final DefaultJaxbJsonMarshaller jsonMarshaller = new DefaultJaxbJsonMarshaller();
        CommentJsonBean bean = CommentJsonBean.shortBean(comment, jiraBaseUrls, projectRoleManager);
        String data = jsonMarshaller.marshal(bean);
        try
        {
            return new JSONObject(data);
        }
        catch (JSONException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
