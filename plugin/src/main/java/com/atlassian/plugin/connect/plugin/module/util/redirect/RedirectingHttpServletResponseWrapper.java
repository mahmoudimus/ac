package com.atlassian.plugin.connect.plugin.module.util.redirect;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.http.HttpStatus;


class RedirectingHttpServletResponseWrapper extends HttpServletResponseWrapper
{
    private PrintWriter devNullWriter;
    private ServletOutputStream devNullOutputStream;

    public RedirectingHttpServletResponseWrapper(HttpServletResponse response)
    {
        super(response);
    }

    @Override
    public void setStatus(int sc)
    {
        checkStatus(sc);
        if (!is404())
        {
            super.setStatus(sc);
        }
    }

    @Override
    public void setStatus(int sc, String sm)
    {
        checkStatus(sc);
        if (!is404())
        {
            super.setStatus(sc, sm);
        }
    }

    @Override
    public void sendError(int sc) throws IOException
    {
        checkStatus(sc);
        if (!is404())
        {
            super.sendError(sc);
        }
    }

    @Override
    public void sendError(int sc, String msg) throws IOException
    {
        checkStatus(sc);
        if (!is404())
        {
            super.sendError(sc, msg);
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        return devNullWriter != null ? devNullWriter : super.getWriter();
    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        return devNullOutputStream != null ? devNullOutputStream :
                super.getOutputStream();
    }

    boolean is404()
    {
        return devNullWriter != null;
    }

    private void checkStatus(int sc)
    {
        if (sc == HttpStatus.SC_NOT_FOUND)
        {
            devNullWriter = new PrintWriter(new CharArrayWriter());
            devNullOutputStream = new ServletOutputStream()
            {

                @Override
                public void write(int b) throws IOException
                {
                    // dev null
                }
            };
        }
    }

}
