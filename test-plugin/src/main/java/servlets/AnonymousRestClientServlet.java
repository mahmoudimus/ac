package servlets;

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.kit.servlet.AbstractPageServlet;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Named
public class AnonymousRestClientServlet extends AbstractPageServlet
{

    @Inject
    @ComponentImport
    SearchRestClient searchRestClient;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException
    {
        try
        {
            searchRestClient.searchJql("").claim();
            render(req, resp, ImmutableMap.of("status", "Success"));
        }
        catch (Exception e)
        {
            render(req, resp, ImmutableMap.of("status", "Failed"));
        }
    }
}
