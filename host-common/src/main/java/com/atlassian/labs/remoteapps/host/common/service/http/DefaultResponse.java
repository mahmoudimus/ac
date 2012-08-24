package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.http.Response;

import java.io.InputStream;
import java.util.Map;

/**
 *
 */
public class DefaultResponse extends DefaultMessage implements Response
{
    private int statusCode;
    private String statusText;

    /**
     *
     *
     * @return
     */
    public int getStatusCode()
    {
        return statusCode;
    }

    /**
     *
     *
     * @param statusCode
     * @return
     */
    public Response setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
        return this;
    }

    /**
     *
     *
     * @return
     */
    public String getStatusText()
    {
        return statusText;
    }

    /**
     *
     *
     * @param statusText
     * @return
     */
    public Response setStatusText(String statusText)
    {
        this.statusText = statusText;
        return this;
    }

    @Override
    public Response setContentType(String contentType)
    {
        super.setContentType(contentType);
        return this;
    }

    @Override
    public Response setContentCharset(String contentCharset)
    {
        super.setContentCharset(contentCharset);
        return this;
    }

    @Override
    public Response setHeaders(Map<String, String> headers)
    {
        super.setHeaders(headers);
        return this;
    }

    @Override
    public Response setHeader(String name, String value)
    {
        super.setHeader(name, value);
        return this;
    }

    @Override
    public Response setEntity(String entity)
    {
        super.setEntity(entity);
        return this;
    }

    @Override
    public Response setEntityStream(InputStream entityStream, String encoding)
    {
        super.setEntityStream(entityStream, encoding);
        return this;
    }

    @Override
    public Response setEntityStream(InputStream entityStream)
    {
        super.setEntityStream(entityStream);
        return this;
    }

    @Override
    public String dump()
    {
        StringBuilder buf = new StringBuilder();
        String lf = System.getProperty("line.separator");
        buf.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append(lf);
        buf.append(super.dump());
        return buf.toString();
    }
}
