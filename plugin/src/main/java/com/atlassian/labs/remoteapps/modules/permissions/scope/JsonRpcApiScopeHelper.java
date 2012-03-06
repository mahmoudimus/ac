package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.labs.remoteapps.util.ServletUtils;
import com.google.common.base.Function;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import static com.google.common.collect.Iterables.transform;

/**
 * An api scope for json-rpc requests
 */
public class JsonRpcApiScopeHelper
{
    private final Collection<String> methods;
    private final String path;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    public JsonRpcApiScopeHelper(final String path, Collection<String> methods) {
        this.path = path;
        this.methods = methods;
        this.apiResourceInfo = transform(methods, new Function<String,ApiResourceInfo>()
        {
            @Override
            public ApiResourceInfo apply(@Nullable String from)
            {
                return new ApiResourceInfo(path, HttpMethod.POST, from);
            }
        });
    }

    public boolean allow(HttpServletRequest request, String user)
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
            String method = pathInfo.replaceAll(path.toString() + "/","");
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
            JSONObject json = new JSONObject(new JSONTokener(reader));
            return json.get("method").toString();
        }
        catch (JSONException e)
        {
            return null;
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
