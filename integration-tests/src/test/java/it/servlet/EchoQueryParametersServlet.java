package it.servlet;

import com.atlassian.plugin.connect.test.utils.NameValuePairs;
import org.apache.http.NameValuePair;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;

import it.servlet.iframe.MustacheServlet;

public class EchoQueryParametersServlet extends MustacheServlet
{
    private volatile BlockingDeque<NameValuePairs> queryParameters = new LinkedBlockingDeque<NameValuePairs>();

    public EchoQueryParametersServlet()
    {
        this("echo-query.mu");
    }
    
    public EchoQueryParametersServlet(String templatePath)
    {
        super(templatePath);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        NameValuePairs parameters = new NameValuePairs(req.getParameterMap());
        queryParameters.push(parameters);
        
        List<Map<String,String>> nvps = new ArrayList<Map<String, String>>();
        
        for (NameValuePair pair : parameters.getNameValuePairs())
        {
            HashMap<String,String> nvp = new HashMap<String, String>();
            
            nvp.put("name",pair.getName());
            nvp.put("value",pair.getValue());
            nvps.add(nvp);
        }
        
        Map<String,Object> newContext = new HashMap<String, Object>();
        newContext.putAll(context);

        newContext.put("nvp",nvps);
        
        super.doGet(req,resp, ImmutableMap.copyOf(newContext));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        doGet(req, resp, context);
    }

    public NameValuePairs waitForQueryParameters() throws InterruptedException
    {
        return queryParameters.poll(5, TimeUnit.SECONDS);
    }

    private void render(PrintWriter writer, List<NameValuePair> nameValuePairs)
    {
        writer.write(" <p id=\"hello-world-message\">Hello world</p>\n");

        writer.write("<ul>");
        for (NameValuePair pair : nameValuePairs)
        {
            writer.write(String.format("<li id=\"%s\">", pair.getName()));
            writer.write(pair.getName());
            writer.write(": ");
            writer.write(pair.getValue());
            writer.write("</li>\n");
        }
        writer.write("</ul>");
    }
}
