/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.atlassian.labs.remoteapps.plugin.util.http;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Static helpers for dealing with {@link org.apache.http.HttpEntity}s.
 *
 * Copied from {@link org.apache.http.util.EntityUtils} v4.2.2 to add a hard limit on responses consumed
 *
 *
 * @since 4.0
 */
public final class LimitedEntityUtils
{
    /** Max response size limit of 100 megabytes */
    public static final long MAX_RESPONSE_SIZE = 1024 * 1024 * 100;

    private LimitedEntityUtils() {
    }

    /**
     * Get the entity content as a String, using the provided default character set
     * if none is found in the entity.
     * If defaultCharset is null, the default "ISO-8859-1" is used.
     *
     * @param entity must not be null
     * @param defaultCharset character set to be applied if none found in the entity
     * @return the entity content as a String. May be null if
     *   {@link org.apache.http.HttpEntity#getContent()} is null.
     * @throws org.apache.http.ParseException if header elements cannot be parsed
     * @throws IllegalArgumentException if entity is null or if content length > Integer.MAX_VALUE
     * @throws java.io.IOException if an error occurs reading the input stream
     */
    public static String toString(
            final HttpEntity entity, final Charset defaultCharset) throws IOException, ParseException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        InputStream instream = entity.getContent();
        if (instream == null) {
            return null;
        }
        try {
            if (entity.getContentLength() > MAX_RESPONSE_SIZE) {
                throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
            }
            int i = (int)entity.getContentLength();
            if (i < 0) {
                i = 4096;
            }
            ContentType contentType = ContentType.getOrDefault(entity);
            Charset charset = contentType.getCharset();
            if (charset == null) {
                charset = defaultCharset;
            }
            if (charset == null) {
                charset = HTTP.DEF_CONTENT_CHARSET;
            }
            Reader reader = new InputStreamReader(instream, charset);
            CharArrayBuffer buffer = new CharArrayBuffer(i);
            char[] tmp = new char[1024];
            int l;
            while((l = reader.read(tmp)) != -1) {
                if (buffer.length() + l > MAX_RESPONSE_SIZE)
                {
                    throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
                }
                buffer.append(tmp, 0, l);
            }
            return buffer.toString();
        } finally {
            instream.close();
        }
    }

    /**
     * Get the entity content as a String, using the provided default character set
     * if none is found in the entity.
     * If defaultCharset is null, the default "ISO-8859-1" is used.
     *
     * @param entity must not be null
     * @param defaultCharset character set to be applied if none found in the entity
     * @return the entity content as a String. May be null if
     *   {@link org.apache.http.HttpEntity#getContent()} is null.
     * @throws org.apache.http.ParseException if header elements cannot be parsed
     * @throws IllegalArgumentException if entity is null or if content length > Integer.MAX_VALUE
     * @throws java.io.IOException if an error occurs reading the input stream
     */
    public static String toString(
            final HttpEntity entity, final String defaultCharset) throws IOException, ParseException {
        return toString(entity, Charset.forName(defaultCharset));
    }

    /**
     * Read the contents of an entity and return it as a String.
     * The content is converted using the character set from the entity (if any),
     * failing that, "ISO-8859-1" is used.
     *
     * @param entity
     * @return String containing the content.
     * @throws org.apache.http.ParseException if header elements cannot be parsed
     * @throws IllegalArgumentException if entity is null or if content length > Integer.MAX_VALUE
     * @throws java.io.IOException if an error occurs reading the input stream
     */
    public static String toString(final HttpEntity entity)
        throws IOException, ParseException {
        return toString(entity, (Charset)null);
    }

}
