package com.atlassian.plugin.remotable.plugin.module.jira.projectconfig;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.plugin.module.page.PageInfo;
import com.atlassian.plugin.remotable.plugin.module.permission.jira.IsProjectAdminCondition;
import com.atlassian.plugin.remotable.spi.module.IFrameContext;
import com.atlassian.sal.api.user.UserManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


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
				userManager.getRemoteUsername(req), out);
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
