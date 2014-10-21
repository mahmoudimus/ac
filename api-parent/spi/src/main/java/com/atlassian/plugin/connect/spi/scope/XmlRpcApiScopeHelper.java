package com.atlassian.plugin.connect.spi.scope;

import com.atlassian.plugin.connect.spi.util.ServletUtils;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.readDocument;
import static com.google.common.collect.Iterables.transform;

/**
 * An api scope for xml-rpc requests
 */
public class XmlRpcApiScopeHelper
{
    private final String path;
    private final Collection<String> methods;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    public XmlRpcApiScopeHelper(final String path, Collection<String> methods)
    {
        this.path = path;
        // This is horrific: for transformed collections being passed in,
        // EqualsBuilder will no longer return true, even though
        // inspection or 'toString' show exactly the same contents for both sides of the comparison...
        // So copying the collection to make EqualsBuilder happy.
        this.methods = Lists.newArrayList(methods);
        this.apiResourceInfo = transform(methods, new Function<String, ApiResourceInfo>()
        {
            @Override
            public ApiResourceInfo apply(String from)
            {
                return new ApiResourceInfo(path, "POST", from);
            }
        });
    }
    
    @Nullable
    public static String extractMethod(HttpServletRequest rq)
    {
        Document doc = readDocument(rq);
        if(doc == null)
        {
            return null;
        }
        Element root = doc.getRootElement();
        if(root == null)
        {
            return null;
        }
        Element methodName = root.element("methodName");
        if(null == methodName)
        {
            return null;
        }
        return methodName.getTextTrim();
    }

    public boolean allow(HttpServletRequest request, UserKey user)
    {
        final String pathInfo = ServletUtils.extractPathInfo(request);
        if (path.equals(pathInfo))
        {
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
        return false;
    }

    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
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

        XmlRpcApiScopeHelper that = (XmlRpcApiScopeHelper) o;
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
