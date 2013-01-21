package com.atlassian.plugin.remotable.host.common.service;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class AuthenticationInfo
{
    private static final String UTF_8 = "UTF-8";

    private String clientKey;
    private String userId;

    public AuthenticationInfo(String clientKey, String userId)
    {
        this.clientKey = clientKey;
        this.userId = userId;
    }

    public String getClientKey()
    {
        return clientKey;
    }

    public String getUserId()
    {
        return userId;
    }

    public String encode()
    {
        return AuthenticationInfo.encode(this);
    }

    public static String encode(AuthenticationInfo info)
    {
        String params
            = "clientKey=" + encodeValue(info.clientKey)
            + "&userId=" + (info.userId != null ? encodeValue(info.userId) : "");
        return DatatypeConverter.printBase64Binary(params.getBytes(Charset.forName(UTF_8)));
    }

    public static AuthenticationInfo decode(String data)
    {
        AuthenticationInfo info = null;
        String params = new String(DatatypeConverter.parseBase64Binary(data), Charset.forName(UTF_8));
        String[] nvPairs = params.split("&");
        if (nvPairs.length > 0)
        {
            String clientKey = null;
            String userId = null;
            for (String nv : nvPairs)
            {
                String[] nvParts = nv.split("=");
                if (nvParts[0].equals("clientKey"))
                {
                    clientKey = decodeValue(nvParts.length == 2 ? nvParts[1] : "");
                }
                else if (nvParts[0].equals("userId"))
                {
                    userId = decodeValue(nvParts.length == 2 ? nvParts[1] : "");
                }
                else
                {
                    throw new IllegalArgumentException("Unexpected param: " + nv);
                }
            }
            info = new AuthenticationInfo(clientKey, userId);
        }
        return info;
    }

    private static String encodeValue(String value)
    {
        try
        {
            return URLEncoder.encode(value, UTF_8);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static String decodeValue(String value)
    {
        try
        {
            return URLDecoder.decode(value, UTF_8);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
