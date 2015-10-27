package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.api.web.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameContext;
import com.atlassian.plugin.connect.api.web.iframe.PageInfo;
import com.atlassian.plugin.connect.api.web.page.IFramePageRenderer;
import com.atlassian.plugin.connect.spi.module.page.IFramePageServlet;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.annotations.VisibleForTesting;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


/**
 * A servlet that loads a plugin config tab from a remote plugin's iframe.
 *
 * This differs slightly from the regular {@link IFramePageServlet} in that it includes nasty workaround for JRA-16407.
 */
public class IFrameProjectConfigTabServlet extends IFramePageServlet
{

	public IFrameProjectConfigTabServlet(PageInfo pageInfo,
                                         IFramePageRenderer iFramePageRenderer,
                                         IFrameContext iframeContext,
                                         UserManager userManager,
                                         UrlVariableSubstitutor urlVariableSubstitutor,
                                         Map<String, String> contextParamNameToSymbolicName)
	{
		super(pageInfo, iFramePageRenderer, iframeContext, userManager, urlVariableSubstitutor, contextParamNameToSymbolicName);
	}

	@Override
    @VisibleForTesting
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
        final Project project = getProject(req);
        req.setAttribute("com.atlassian.jira.projectconfig.util.ServletRequestProjectConfigRequestCache:project", project);

        super.doGet(req, resp);
	}

	private Project getProject(final HttpServletRequest request)
	{
		Object projectKey = request.getParameterMap().get("projectKey");
		if (projectKey instanceof String[])
		{
			final String key = ((String[]) projectKey)[0];
			return ComponentAccessor.getProjectManager().getProjectObjByKey(key);
		}
		return null;
	}
}
