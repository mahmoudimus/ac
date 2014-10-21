package com.atlassian.plugin.connect.spi.scope;

import com.atlassian.plugin.connect.spi.util.ServletUtils;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

/**
 * An api scope for json-rpc requests
 */
public final class JsonRpcApiScopeHelper
{
    private final Collection<String> methods;
    private final String path;
    private final String httpMethod;
    private transient final Iterable<ApiResourceInfo> apiResourceInfo;

    public JsonRpcApiScopeHelper(final String path, Collection<String> methods)
    {
        this(path, methods, "POST");
    }

    public JsonRpcApiScopeHelper(final String path, Collection<String> methods, final String httpMethod)
    {
        this.path = path;
        this.methods = methods;
        this.httpMethod = checkNotNull(httpMethod).toUpperCase();
        this.apiResourceInfo = transform(methods, new Function<String, ApiResourceInfo>()
        {
            @Override
            public ApiResourceInfo apply(String from)
            {
                return new ApiResourceInfo(path, JsonRpcApiScopeHelper.this.httpMethod, from);
            }
        });
    }

    public boolean allow(HttpServletRequest request, UserKey user)
    {
        if (!httpMethod.equals(request.getMethod()))
        {
            return false;
        }

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
            String method = pathInfo.replaceAll(path + "/", "");
            return methods.contains(method);
        }
        return false;
    }

    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }

    public static String extractMethod(HttpServletRequest request)
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        JsonRpcApiScopeHelper that = (JsonRpcApiScopeHelper) o;
        // don't consider apiResourceInfo as it is built from path and methods
        return new EqualsBuilder()
                .append(path, that.path)
                .append(methods, that.methods)
                .build();
    }

    @Override
    public int hashCode()
    {
        // don't consider apiResourceInfo as it is built from path and methods
        return new HashCodeBuilder(19, 71)
                .append(path)
                .append(methods)
                .build();
    }

    @Override
    public String toString()
    {
        // don't consider apiResourceInfo as it is built from path and methods
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("path", path)
                .append("methods", methods)
                .toString();
    }
}
