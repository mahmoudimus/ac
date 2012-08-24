package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.http.Message;
import com.atlassian.labs.remoteapps.api.service.http.Response;

import java.io.InputStream;
import java.util.Map;

public class DefaultResponse extends DefaultMessage implements Response
{
    private int statusCode;
    private String statusText;

    @Override
    public int getStatusCode()
    {
        return statusCode;
    }

    @Override
    public Response setStatusCode(int statusCode)
    {
        checkMutable();
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public String getStatusText()
    {
        return statusText;
    }

    @Override
    public Response setStatusText(String statusText)
    {
        checkMutable();
        this.statusText = statusText;
        return this;
    }

    @Override
    public boolean isInformational()
    {
        return statusCode >= 100 && statusCode < 200;
    }

    @Override
    public boolean isSuccessful()
    {
        return statusCode >= 200 && statusCode < 300;
    }

    @Override
    public boolean isOk()
    {
        return statusCode == 200;
    }

    @Override
    public boolean isCreated()
    {
        return statusCode == 201;
    }

    @Override
    public boolean isNoContent()
    {
        return statusCode == 204;
    }

    @Override
    public boolean isRedirection()
    {
        return statusCode >= 300 && statusCode < 400;
    }

    @Override
    public boolean isSeeOther()
    {
        return statusCode == 303;
    }

    @Override
    public boolean isNotModified()
    {
        return statusCode == 304;
    }

    @Override
    public boolean isClientError()
    {
        return statusCode >= 400 && statusCode < 500;
    }

    @Override
    public boolean isBadRequest()
    {
        return statusCode == 400;
    }

    @Override
    public boolean isUnauthorized()
    {
        return statusCode == 401;
    }

    @Override
    public boolean isForbidden()
    {
        return statusCode == 403;
    }

    @Override
    public boolean isNotFound()
    {
        return statusCode == 404;
    }

    @Override
    public boolean isConflict()
    {
        return statusCode == 409;
    }

    @Override
    public boolean isServerError()
    {
        return statusCode >= 500 && statusCode < 600;
    }

    @Override
    public boolean isInternalServerError()
    {
        return statusCode == 500;
    }

    @Override
    public boolean isServiceUnavailable()
    {
        return statusCode == 503;
    }

    @Override
    public boolean isError()
    {
        return isClientError() || isServerError();
    }

    @Override
    public boolean isNotSuccessful()
    {
        return isInformational() || isRedirection() || isError();
    }

    @Override
    public Response setContentType(String contentType)
    {
        checkMutable();
        super.setContentType(contentType);
        return this;
    }

    @Override
    public Response setContentCharset(String contentCharset)
    {
        checkMutable();
        super.setContentCharset(contentCharset);
        return this;
    }

    @Override
    public Response setHeaders(Map<String, String> headers)
    {
        checkMutable();
        super.setHeaders(headers);
        return this;
    }

    @Override
    public Response setHeader(String name, String value)
    {
        checkMutable();
        super.setHeader(name, value);
        return this;
    }

    @Override
    public Response setEntity(String entity)
    {
        checkMutable();
        super.setEntity(entity);
        return this;
    }

    @Override
    public Response setEntityStream(InputStream entityStream, String encoding)
    {
        checkMutable();
        super.setEntityStream(entityStream, encoding);
        return this;
    }

    @Override
    public Response setEntityStream(InputStream entityStream)
    {
        checkMutable();
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

    @Override
    protected Response freeze()
    {
        super.freeze();
        return this;
    }
}
