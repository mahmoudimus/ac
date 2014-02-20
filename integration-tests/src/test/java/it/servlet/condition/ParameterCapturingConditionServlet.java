package it.servlet.condition;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class ParameterCapturingConditionServlet extends HttpServlet
{
    private static final Function<String[],String> HEAD_ARRAY = new Function<String[], String>()
    {
        @Override
        public String apply(@Nullable final String[] input)
        {
            return input != null && input.length > 0 ? input[0] : null;
        }
    };

    private volatile Map<String, String[]> paramsFromLastRequest;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        paramsFromLastRequest = req.getParameterMap();

        resp.setContentType("application/json");
        resp.getWriter().write("{\"shouldDisplay\" : true}");
        resp.getWriter().close();
    }

    public Map<String, String[]> getAllParamsFromLastRequest()
    {
        return paramsFromLastRequest;
    }

    public Map<String, String> getParamsFromLastRequest()
    {
        return Maps.transformValues(paramsFromLastRequest, HEAD_ARRAY);
    }

    public void clearParams()
    {
        paramsFromLastRequest = null;
    }
}