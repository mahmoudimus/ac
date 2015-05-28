package com.atlassian.plugin.connect.core.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import org.json.JSONException;
import org.json.JSONObject;

import static com.google.common.base.Strings.nullToEmpty;

/**
 */
@Path("/oauth")
public class OAuthResource
{
    private final ConsumerService consumerService;

    public OAuthResource(ConsumerService consumerService)
    {
        this.consumerService = consumerService;
    }

    @GET
    @AnonymousAllowed
    @Produces("application/json")
    public Response getOauthInfo() throws JSONException
    {
        Consumer consumer = consumerService.getConsumer();
        return Response.ok(new JSONObject()
                .put("key", nullToEmpty(consumer.getKey()))
                .put("name", nullToEmpty(consumer.getName()))
                .put("publicKey", nullToEmpty(RSAKeys.toPemEncoding(consumer.getPublicKey())))
                .put("description", nullToEmpty(consumer.getDescription())).toString(2))
            .build();
    }
}
