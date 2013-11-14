package com.atlassian.plugin.connect.spi.permission.scope;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.connect.spi.util.ServletUtils;

import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import static com.google.common.collect.Iterables.transform;

/**
 * An api scope for json-rpc requests
 */
public final class JsonRpcApiScopeHelper
{
    private final Collection<String> methods;
    private final String path;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    public JsonRpcApiScopeHelper(final String path, Collection<String> methods)
    {
        this.path = path;
        this.methods = methods;
        this.apiResourceInfo = transform(methods, new Function<String, ApiResourceInfo>()
        {
            @Override
            public ApiResourceInfo apply(String from)
            {
                return new ApiResourceInfo(path, "POST", from);
            }
        });
    }

    public boolean allow(HttpServletRequest request, UserKey user)
    {
        final String pathInfo = ServletUtils.extractPathInfo(request);
        if (path.equals(pathInfo))
        {
            // methodName not in path so extract it from body
            String method = extractMethod(request);
            if (method == null)
            {
                return false;
            }
            else if (methods.contains(method))
            {
                return true;
            }
        }
        else
        {
            // methodName in path
            String method = pathInfo.replaceAll(path.toString() + "/", "");
            return methods.contains(method);
        }
        return false;
    }

    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }

    private String extractMethod(HttpServletRequest request)
    {
        InputStream in = null;
        try
        {
            in = request.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            JSONObject json = (JSONObject) JSONValue.parse(reader);
            return json.get("method").toString();
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }
}
