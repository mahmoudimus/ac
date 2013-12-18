package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.fugue.Option;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.jwt.core.JwtUtil;
import com.atlassian.jwt.core.TimeUtil;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.JwtClaimsBuilder;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.plugin.util.ConfigurationUtils;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
  * Set the system property {@link JwtAuthorizationGenerator#JWT_EXPIRY_SECONDS_PROPERTY} with an integer value to control the size of the expiry window
  * (default is {@link JwtAuthorizationGenerator#JWT_EXPIRY_WINDOW_SECONDS_DEFAULT}).
 */
public class JwtAuthorizationGenerator extends DefaultAuthorizationGeneratorBase
{
    private static final char[] QUERY_DELIMITERS = new char[] { '&' };

    private static final String JWT_EXPIRY_SECONDS_PROPERTY = "com.atlassian.connect.jwt.expiry_seconds";
    /**
     * Default of 3 minutes.
     */
    private static final int JWT_EXPIRY_WINDOW_SECONDS_DEFAULT = 60 * 3;
    private static final int JWT_EXPIRY_WINDOW_SECONDS = ConfigurationUtils.getIntSystemProperty(JWT_EXPIRY_SECONDS_PROPERTY, JWT_EXPIRY_WINDOW_SECONDS_DEFAULT);

    private final JwtService jwtService;
    private final ApplicationLink applicationLink;
    private final ConsumerService consumerService;

    public JwtAuthorizationGenerator(JwtService jwtService, ApplicationLink applicationLink, ConsumerService consumerService)
    {
        this.jwtService = checkNotNull(jwtService);
        this.applicationLink = checkNotNull(applicationLink);
        this.consumerService = checkNotNull(consumerService);
    }

    @Override
    public Option<String> generate(HttpMethod httpMethod, URI url, Map<String, List<String>> parameters)
    {
        checkArgument(null != parameters, "Parameters Map argument cannot be null");

        Map<String, String[]> paramsAsArrays = Maps.transformValues(parameters, new Function<List<String>, String[]>()
        {
            @Override
            public String[] apply(List<String> input)
            {
                return checkNotNull(input).toArray(new String[input.size()]);
            }
        });
        return Option.some(JwtUtil.JWT_AUTH_HEADER_PREFIX + encodeJwt(httpMethod, url, paramsAsArrays, null, consumerService.getConsumer().getKey(), jwtService, applicationLink));
    }

    static String encodeJwt(HttpMethod httpMethod, URI targetPath, Map<String, String[]> params, String userKeyValue, String issuerId, JwtService jwtService, ApplicationLink appLink)
    {
        checkArgument(null != httpMethod, "HttpMethod argument cannot be null");
        checkArgument(null != targetPath, "URI argument cannot be null");

        JwtJsonBuilder jsonBuilder = new JsonSmartJwtJsonBuilder()
                .issuedAt(TimeUtil.currentTimeSeconds())
                .expirationTime(TimeUtil.currentTimePlusNSeconds(JWT_EXPIRY_WINDOW_SECONDS))
                .issuer(issuerId);

        if (null != userKeyValue)
        {
            jsonBuilder = jsonBuilder.subject(userKeyValue);
        }

        Map<String, String[]> completeParams = params;

        try
        {
            if (!StringUtils.isEmpty(targetPath.getQuery()))
            {
                completeParams = new HashMap<String, String[]>(params);
                completeParams.putAll(constructParameterMap(targetPath));
            }

            JwtClaimsBuilder.appendHttpRequestClaims(jsonBuilder, new CanonicalHttpUriRequest(httpMethod.toString(), targetPath.getPath(), "", completeParams));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }

        return jwtService.issueJwt(jsonBuilder.build(), appLink);
    }

    private static Map<String, String[]> constructParameterMap(URI uri) throws UnsupportedEncodingException
    {
        return transformParamsMultiMapIntoStringToArrayMap(parseQueryStringParameters(uri.getQuery()));
    }

    private static Map<String, String[]> transformParamsMultiMapIntoStringToArrayMap(Multimap<String, String> queryParams)
    {
        Map<String, String[]> queryParamsMap = new HashMap<String, String[]>(queryParams.size());

        // convert String -> Collection<String> to String -> String[]
        for (Map.Entry<String, Collection<String>> entry : queryParams.asMap().entrySet())
        {
            queryParamsMap.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
        }

        return queryParamsMap;
    }

    private static Multimap<String, String> parseQueryStringParameters(final String query) throws UnsupportedEncodingException
    {
        if (query == null)
        {
            return HashMultimap.create(0, 0);
        }

        Multimap<String, String> queryParams = HashMultimap.create();

        CharArrayBuffer buffer = new CharArrayBuffer(query.length());
        buffer.append(query);
        ParserCursor cursor = new ParserCursor(0, buffer.length());

        while (!cursor.atEnd())
        {
            NameValuePair nameValuePair = BasicHeaderValueParser.DEFAULT.parseNameValuePair(buffer, cursor, QUERY_DELIMITERS);

            if (nameValuePair.getName().length() > 0)
            {
                queryParams.put(urlDecode(nameValuePair.getName()), urlDecode(nameValuePair.getValue()));
            }
        }

        return queryParams;
    }

    private static String urlDecode(final String content) throws UnsupportedEncodingException
    {
        return URLDecoder.decode(content, Consts.UTF_8.displayName());
    }
}
