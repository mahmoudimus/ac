package com.atlassian.plugin.connect.plugin.module.jira.projectconfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.plugin.module.jira.conditions.IsProjectAdminCondition;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.sal.api.user.UserManager;


/**
 * A servlet that loads a plugin config tab from a remote plugin's iframe.
 */
public class IFrameProjectConfigTabServlet extends HttpServlet
{

	private final UserManager userManager;
	private final PageInfo pageInfo;
	private final IFrameContext iframeContext;
	private final IFrameRendererImpl iFrameRenderer;

	public IFrameProjectConfigTabServlet(PageInfo pageInfo,
			IFrameRendererImpl iFrameRenderer,
			IFrameContext iframeContext,
			UserManager userManager)
	{
		this.userManager = userManager;
		this.pageInfo = pageInfo;
		this.iframeContext = iframeContext;
		this.iFrameRenderer = iFrameRenderer;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		PrintWriter out = resp.getWriter();
		resp.setContentType("text/html");

		final Project project = getProject(req);
		req.setAttribute("com.atlassian.jira.projectconfig.util.ServletRequestProjectConfigRequestCache:project", project);

		// This is a workaround for JRA-26407.
		((IsProjectAdminCondition)pageInfo.getCondition()).setProject(project);

		iFrameRenderer.renderPage(iframeContext, pageInfo, req.getPathInfo(), req.getParameterMap(),
				userManager.getRemoteUsername(req), Collections.<String, Object>emptyMap(), out);
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
