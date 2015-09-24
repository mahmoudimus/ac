package com.atlassian.plugin.connect.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.core.TimeUtil;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.JwtClaimsBuilder;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.plugin.util.ConfigurationUtils;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.spi.http.ReKeyableAuthorizationGenerator;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jwt.JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Set the system property {@link JwtAuthorizationGenerator#JWT_EXPIRY_SECONDS_PROPERTY} with an integer value to control the size of the expiry window
 * (default is {@link JwtAuthorizationGenerator#JWT_EXPIRY_WINDOW_SECONDS_DEFAULT}).
 */
public class JwtAuthorizationGenerator implements ReKeyableAuthorizationGenerator
{
    private static final char[] QUERY_DELIMITERS = new char[]{'&'};

    private static final String JWT_EXPIRY_SECONDS_PROPERTY = "com.atlassian.connect.jwt.expiry_seconds";
    /**
     * Default of 3 minutes.
     */
    private static final int JWT_EXPIRY_WINDOW_SECONDS_DEFAULT = 60 * 3;
    private static final int JWT_EXPIRY_WINDOW_SECONDS = ConfigurationUtils.getIntSystemProperty(JWT_EXPIRY_SECONDS_PROPERTY, JWT_EXPIRY_WINDOW_SECONDS_DEFAULT);

    private static final Logger log = LoggerFactory.getLogger(JwtAuthorizationGenerator.class);

    private final JwtService jwtService;
    private final Supplier<String> secretSupplier;
    private final ConsumerService consumerService;
    private final URI addOnBaseUrl;

    public JwtAuthorizationGenerator(JwtService jwtService, Supplier<String> secretSupplier, ConsumerService consumerService, URI addOnBaseUrl)
    {
        this.jwtService = checkNotNull(jwtService);
        this.secretSupplier = checkNotNull(secretSupplier);
        this.consumerService = checkNotNull(consumerService);
        this.addOnBaseUrl = checkNotNull(addOnBaseUrl);
    }

    @Override
    public Option<String> generate(HttpMethod httpMethod, URI url, Map<String, String[]> parameters, UserProfile remoteUser)
    {
        return Option.some(generate(httpMethod, url, parameters, remoteUser, checkNotNull(secretSupplier.get())));
    }

    @Override
    public String generate(HttpMethod httpMethod, URI url, Map<String, String[]> parameters, UserProfile remoteUser, String secret)
    {
        checkArgument(null != parameters, "Parameters Map argument cannot be null");
        return JWT_AUTH_HEADER_PREFIX + encodeJwt(httpMethod, url, addOnBaseUrl, parameters, remoteUser, consumerService.getConsumer().getKey(), jwtService, secret);
    }

    static String encodeJwt(HttpMethod httpMethod, URI targetPath, URI addOnBaseUrl, Map<String, String[]> params, UserManager userManager, String issuerId, JwtService jwtService, String secret)
    {
        UserProfile remoteUser = userManager == null ? null : userManager.getRemoteUser();

        return encodeJwt(httpMethod, targetPath, addOnBaseUrl, params, remoteUser, issuerId, jwtService, secret);
    }

    static String encodeJwt(HttpMethod httpMethod, URI targetPath, URI addOnBaseUrl, Map<String, String[]> params, UserProfile remoteUser, String issuerId, JwtService jwtService, String secret)
    {
        checkArgument(null != httpMethod, "HttpMethod argument cannot be null");
        checkArgument(null != targetPath, "URI argument cannot be null");
        checkArgument(null != addOnBaseUrl, "base URI argument cannot be null");
        checkArgument(null != secret, "secret argument cannot be null");

        JwtJsonBuilder jsonBuilder = new JsonSmartJwtJsonBuilder()
                .issuedAt(TimeUtil.currentTimeSeconds())
                .expirationTime(TimeUtil.currentTimePlusNSeconds(JWT_EXPIRY_WINDOW_SECONDS))
                .issuer(issuerId);

        Map<String, Object> jwtContextClaim = Maps.newHashMap();

        String userKeyValue = "";
        if (remoteUser != null)
        {
            userKeyValue = remoteUser.getUserKey().getStringValue();

            Map<String, String> jwtContextUser = ImmutableMap.of(
                    "userKey", userKeyValue,
                    "username", remoteUser.getUsername(),
                    "displayName", remoteUser.getFullName()
            );

            jwtContextClaim.put("user", jwtContextUser);
            jsonBuilder = jsonBuilder.subject(userKeyValue);
        }

        jsonBuilder = jsonBuilder.claim("context", jwtContextClaim);

        Map<String, String[]> completeParams = params;

        try
        {
            if (!StringUtils.isEmpty(targetPath.getQuery()))
            {
                completeParams = new HashMap<String, String[]>(params);
                completeParams.putAll(constructParameterMap(targetPath));
            }

            CanonicalHttpUriRequest request = new CanonicalHttpUriRequest(httpMethod.toString(), extractRelativePath(targetPath, addOnBaseUrl), "", completeParams);
            log.debug("Canonical request is: " + HttpRequestCanonicalizer.canonicalize(request));

            JwtClaimsBuilder.appendHttpRequestClaims(jsonBuilder, request);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }

        return jwtService.issueJwt(jsonBuilder.build(), secret);
    }

    private static String extractRelativePath(URI targetUri, URI addOnBaseUri)
    {
        String path = targetUri.getPath();
        final String targetString = targetUri.toString();
        final String baseString = addOnBaseUri.toString();

        if (!StringUtils.isEmpty(targetString) && !StringUtils.isEmpty(baseString))
        {
            if (targetString.startsWith(baseString))
            {
                path = URI.create(StringUtils.removeStart(targetString, baseString)).getPath();
            }
            else
            {
                // don't sign something intended for "example.com" that is going to "other.domain.biz"
                if (targetUri.isAbsolute())
                {
                    final String message = String.format("Do not ask for the target URL '%s' to be signed for an add-on with a base URL of '%s': an absolute target URL should begin with the base URL.",
                            targetString, baseString);
                    throw new IllegalArgumentException(message);
                }
            }
        }

        return path;
    }

    @VisibleForTesting
    public static Map<String, String[]> constructParameterMap(URI uri) throws UnsupportedEncodingException
    {
        // Do not use uri.getQuery() here, as getQuery() already decodes, and we decode again below
        final String query = uri.getRawQuery();
        if (query == null)
        {
            return Collections.emptyMap();
        }

        Map<String, String[]> queryParams = new HashMap<String, String[]>();

        CharArrayBuffer buffer = new CharArrayBuffer(query.length());
        buffer.append(query);
        ParserCursor cursor = new ParserCursor(0, buffer.length());

        while (!cursor.atEnd())
        {
            NameValuePair nameValuePair = BasicHeaderValueParser.DEFAULT.parseNameValuePair(buffer, cursor, QUERY_DELIMITERS);

            if (!StringUtils.isEmpty(nameValuePair.getName()))
            {
                String decodedName = urlDecode(nameValuePair.getName());
                String decodedValue = urlDecode(nameValuePair.getValue());
                String[] oldValues = queryParams.get(decodedName);
                String[] newValues = null == oldValues ? new String[1] : Arrays.copyOf(oldValues, oldValues.length + 1);
                newValues[newValues.length - 1] = decodedValue;
                queryParams.put(decodedName, newValues);
            }
        }

        return queryParams;
    }

    private static String urlDecode(final String content) throws UnsupportedEncodingException
    {
        return null == content ? null : URLDecoder.decode(content, "UTF-8");
    }
}
