package com.atlassian.labs.remoteapps.api.service.http;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * An interface for building HTTP requests.
 */
public interface Request extends Message
{
    /**
     *
     *
     * @return
     */
    String getUri();

    /**
     *
     *
     * @param uri
     * @return
     */
    Request setUri(String uri);

    /**
     *
     *
     * @return
     */
    String getAccept();

    /**
     *
     *
     * @param mediaType
     * @return
     */
    Request setAccept(String mediaType);

    /**
     *
     *
     * @param name
     * @param value
     * @return
     */
    Request setAttribute(String name, String value);

    /**
     *
     *
     * @param name
     * @return
     */
    String getAttribute(String name);

    /**
     *
     *
     * @return
     */
    Map<String, String> getAttributes();

    /**
     *
     *
     * @param formBuilder
     * @return
     */
    Request setEntity(FormBuilder formBuilder);

    /**
     *
     *
     * @param formBuilder
     * @return
     */
    Request setFormEntity(FormBuilder formBuilder);

    /**
     *
     *
     * @param params
     * @return
     */
    Request setFormEntity(Map<String, String> params);

    /**
     *
     *
     * @param params
     * @return
     */
    Request setMultiValuedFormEntity(Map<String, List<String>> params);

    /**
     *
     *
     * @return
     */
    ChainingFormBuilder buildFormEntity();

    /**
     *
     *
     * @return
     */
    boolean isFrozen();

    /**
     *
     *
     * @return
     */
    @Override
    String dump();

    @Override
    Request setContentType(String contentType);

    @Override
    Request setContentCharset(String contentCharset);

    @Override
    Request setHeaders(Map<String, String> headers);

    @Override
    Request setHeader(String name, String value);

    @Override
    Request setEntity(String entity);

    @Override
    Request setEntityStream(InputStream entityStream, String encoding);

    @Override
    Request setEntityStream(InputStream entityStream);

    /**
     * (mention no entity)
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise get();

    /**
     * (mention entity)
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise post();

    /**
     * (mention entity)
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise put();

    /**
     * (mention no entity)
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise delete();

    /**
     * (mention optional entity)
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise options();

    /**
     * (mention no entity)
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise head();

    /**
     * (mention entity)
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise trace();
}
