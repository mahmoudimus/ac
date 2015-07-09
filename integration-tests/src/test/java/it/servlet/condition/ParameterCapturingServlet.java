package it.servlet.condition;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import it.servlet.ContextServlet;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class ParameterCapturingServlet extends ContextServlet
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
    private ContextServlet delegate;

    public ParameterCapturingServlet(ContextServlet delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        paramsFromLastRequest = req.getParameterMap();
        delegate.doGet(req, resp, context);
    }

    public void doPost(final HttpServletRequest req, final HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        paramsFromLastRequest = req.getParameterMap();
        delegate.doGet(req, resp, context);
    }

    public Map<String, String> getParamsFromLastRequest()
    {
        return Maps.transformValues(paramsFromLastRequest, HEAD_ARRAY);
    }
}