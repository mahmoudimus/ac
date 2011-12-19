package com.atlassian.labs.remoteapps.sample.junit.jira;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceServiceLocator;
import com.atlassian.jira.rpc.soap.client.JirasoapserviceV2SoapBindingStub;
import com.atlassian.jira.rpc.soap.client.RemoteUser;
import com.atlassian.labs.remoteapps.sample.OAuthContext;
import com.atlassian.labs.remoteapps.sample.junit.XmlRpcClient;
import org.apache.axis.client.Call;
import org.apache.axis.client.Stub;
import org.apache.axis.transport.http.HTTPConstants;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcStruct;

import java.net.URL;

import static com.atlassian.labs.remoteapps.sample.HttpServer.getHostBaseUrl;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class JiraReadUsersAndGroupsScopeTest
{
    @Test
    public void testCall() throws Exception
    {
        JiraSoapServiceServiceLocator locator = new JiraSoapServiceServiceLocator();
        String url = getHostBaseUrl() + "/rpc/soap/jirasoapservice-v2?user_id=betty";
        JiraSoapService service = locator.getJirasoapserviceV2(new URL(url));

        OAuthContext.INSTANCE.sign(url, (Stub) service);
        RemoteUser user = service.getUser("", "betty");
        assertEquals("betty", user.getName());
    }
}
