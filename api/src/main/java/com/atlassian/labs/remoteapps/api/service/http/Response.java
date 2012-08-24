package com.atlassian.labs.remoteapps.api.service.http;

import java.io.InputStream;
import java.util.Map;

/**
 *
 */
public interface Response extends Message
{
    /**
     *
     *
     * @return
     */
    int getStatusCode();

    /**
     *
     *
     * @param statusCode
     * @return
     */
    Message setStatusCode(int statusCode);

    /**
     *
     *
     * @return
     */
    String getStatusText();

    /**
     *
     *
     * @param statusText
     * @return
     */
    Message setStatusText(String statusText);

    /**
     *
     *
     * @return
     */
    @Override
    String dump();

    @Override
    Response setContentType(String contentType);

    @Override
    Response setContentCharset(String contentCharset);

    @Override
    Response setHeaders(Map<String, String> headers);

    @Override
    Response setHeader(String name, String value);

    @Override
    Response setEntity(String entity);

    @Override
    Response setEntityStream(InputStream entityStream, String encoding);

    @Override
    Response setEntityStream(InputStream entityStream);
}
