package com.atlassian.plugin.remotable.container;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerCollection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Same as {@link HandlerList} but with mutable set to true
 */
public class MutableHandlerList extends HandlerCollection
{
    public MutableHandlerList()
    {
        super(true);
    }

    /* ------------------------------------------------------------ */
    /**
     * @see org.eclipse.jetty.server.Handler#handle(String, org.eclipse.jetty.server.Request, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {
        Handler[] handlers = getHandlers();

        if (handlers!=null && isStarted())
        {
            for (int i=0;i<handlers.length;i++)
            {
                handlers[i].handle(target,baseRequest, request, response);
                if ( baseRequest.isHandled())
                    return;
            }
        }
    }
}
