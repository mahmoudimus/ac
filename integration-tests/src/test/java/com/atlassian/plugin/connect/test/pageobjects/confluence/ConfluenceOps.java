package com.atlassian.plugin.connect.test.pageobjects.confluence;

import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.atlassian.fugue.Option;

import com.google.common.base.Function;

import org.apache.commons.codec.binary.Base64;

import redstone.xmlrpc.XmlRpcArray;
import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.XmlRpcStruct;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ConfluenceOps
{
    private final String baseUrl;

    public ConfluenceOps(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public ConfluencePageData setPage(Option<ConfluenceUser> user, String spaceKey, String titlePrefix, String content) throws MalformedURLException, XmlRpcFault
    {
        final Map<String, Object> struct = newXmlRpcStruct();
        struct.put("title", titlePrefix + "_" + System.nanoTime());
        struct.put("space", spaceKey);
        struct.put("content", content);
        return new ConfluencePageData(asMap(getClient(user).invoke("confluence2.storePage", new Object[]{"", struct})));
    }

    public void addEditRestrictionToPage(Option<ConfluenceUser> user, String pageId) throws MalformedURLException, XmlRpcFault
    {
        List<Object> permissions = newXmlRpcArray();

        Map<String, Object> permission = newXmlRpcStruct();
        permission.put("type", "Edit");
        permission.put("userName", user.get().username);

        permissions.add(permission);

        getClient(user).invoke("confluence2.setContentPermissions", new Object[]{"", pageId, "Edit", permissions});
    }

    public ConfluenceCommentData addComment(Option<ConfluenceUser> user, String pageId, String content) throws MalformedURLException, XmlRpcFault
    {
        Map<String, Object> struct = newXmlRpcStruct();
        struct.put("pageId", pageId);
        struct.put("content", content);
        return new ConfluenceCommentData(asMap(getClient(user).invoke("confluence2.addComment", new Object[]{"", struct})));
    }

    public int search(Option<ConfluenceUser> user, String query) throws MalformedURLException, XmlRpcFault
    {
        final int maxResults = 10;
        return asList(getClient(user).invoke("confluence2.search", new Object[]{"", query, maxResults})).size();
    }

    private XmlRpcClient getClient(Option<ConfluenceUser> user) throws MalformedURLException
    {
        final XmlRpcClient client = new XmlRpcClient(baseUrl + "/rpc/xmlrpc", false);
        Option<String> authHeader = getAuthHeader(user);
        if (authHeader.isDefined())
        {
            client.setRequestProperty("Authorization", authHeader.get());
        }
        return client;
    }

    private Option<String> getAuthHeader(Option<ConfluenceUser> user)
    {
        return user.map(new Function<ConfluenceUser, String>()
        {
            @Override
            public String apply(ConfluenceUser u)
            {
                final byte[] authBytes = String.format("%s:%s", u.username, u.password).getBytes(Charset.defaultCharset());
                return "Basic " + new String(Base64.encodeBase64(authBytes));
            }
        });
    }

    private static List<Object> newXmlRpcArray()
    {
        return asList(new XmlRpcArray());
    }

    private static Map<String, Object> newXmlRpcStruct()
    {
        return asMap(new XmlRpcStruct());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object invoke)
    {
        return (Map<String, Object>) invoke;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object invoke)
    {
        return (List<Object>) invoke;
    }

    public static final class ConfluenceUser
    {
        final public String username;
        final public String password;

        public ConfluenceUser(String username, String password)
        {
            this.username = username;
            this.password = password;
        }
    }

    public static final class ConfluencePageData
    {
        private final Map<String, Object> pageData;

        private ConfluencePageData(Map<String, Object> pageData)
        {
            this.pageData = checkNotNull(pageData);
        }

        public String getId()
        {
            return String.valueOf(pageData.get("id"));
        }

        public String getTitle()
        {
            return String.valueOf(pageData.get("title"));
        }

        public String getCreator()
        {
            return String.valueOf(pageData.get("creator"));
        }

        public String getUrl()
        {
            return String.valueOf(pageData.get("url"));
        }
    }

    public static final class ConfluenceCommentData
    {
        private final Map<String, Object> commentData;

        private ConfluenceCommentData(Map<String, Object> commentData)
        {
            this.commentData = checkNotNull(commentData);
        }

        public String getId()
        {
            return String.valueOf(commentData.get("id"));
        }
    }
}
