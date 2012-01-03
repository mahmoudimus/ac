package com.atlassian.labs.remoteapps.sample;

import org.eclipse.jetty.util.ajax.JSONObjectConvertor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 15/12/11
 * Time: 10:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebHookServlet extends HttpServlet
{
    private final List<Publication> publications = new CopyOnWriteArrayList<Publication>();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String eventIdentifier = req.getPathInfo().substring(req.getPathInfo().lastIndexOf('/') + 1);
        StringBuilder body = new StringBuilder();
        char[] buffer = new char[1024];
        int len = 0;
        while ((len = req.getReader().read(buffer)) > -1)
        {
            body.append(buffer, 0, len);
        }
        System.out.println("Receiving web hook '" + eventIdentifier + "' with body\n" + body.toString());
        publications.add(new Publication(eventIdentifier, body.toString()));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("application/json");
        JSONArray result = new JSONArray();
        for (final Publication pub : publications)
        {
            result.put(new JSONObject(new HashMap<String, Object>()
            {{
                    put("event", pub.eventIdentifier);
                    try
                    {
                        put("body", new JSONObject(pub.body));
                    } catch (JSONException e)
                    {
                        throw new ServletException(e);
                    }
                }}));
        }
        try
        {
            resp.getWriter().write(result.toString(2));
        } catch (JSONException e)
        {
            throw new ServletException(e);
        }
        publications.clear();
    }

    private static class Publication
    {
        private final String eventIdentifier;
        private final String body;

        public Publication(String eventIdentifier, String body)
        {
            this.eventIdentifier = eventIdentifier;
            this.body = body;
        }
    }
}
