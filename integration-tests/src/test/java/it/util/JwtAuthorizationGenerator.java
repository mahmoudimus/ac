package it.util;

import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.TimeUtil;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.JwtClaimsBuilder;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtSigningException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtWriter;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

//Copied mostly from AC Play
public class JwtAuthorizationGenerator
{
    private static final char[] QUERY_DELIMITERS = new char[]{'&'};

    /**
     * Default of 3 minutes.
     */
    private static final int JWT_EXPIRY_WINDOW_SECONDS_DEFAULT = 60 * 3;
    private final int jwtExpiryWindowSeconds;

    private final JwtWriterFactory jwtWriterFactory;

    public JwtAuthorizationGenerator(JwtWriterFactory jwtWriterFactory)
    {
        this(jwtWriterFactory, JWT_EXPIRY_WINDOW_SECONDS_DEFAULT);
    }

    public JwtAuthorizationGenerator(JwtWriterFactory jwtWriterFactory, int jwtExpiryWindowSeconds)
    {
        this.jwtWriterFactory = checkNotNull(jwtWriterFactory);
        this.jwtExpiryWindowSeconds = jwtExpiryWindowSeconds;
    }

    public String generate(String method, String productBaseUrl, URI uri, Map<String, List<String>> parameters,
                           Optional<String> userId, String issuer, String sharedSecret)
            throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, URISyntaxException
    {
        final String path = uri.getPath();
        final URI baseUrl = new URI(productBaseUrl);
        final String productContext = baseUrl.getPath();
        final String pathWithoutProductContext = path.substring(productContext.length());

        final URI uriWithoutProductContext = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                pathWithoutProductContext, uri.getQuery(), uri.getFragment());

        return generate(method, uriWithoutProductContext, parameters, userId, issuer, sharedSecret);
    }

    public String generate(String httpMethod, URI url, Map<String, List<String>> parameters,
                           Optional<String> userId, String issuer, String sharedSecret)
            throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException
    {

        Map<String, String[]> paramsAsArrays = Maps.transformValues(parameters, new Function<List<String>, String[]>()
        {
            @Override
            public String[] apply(List<String> input)
            {
                return checkNotNull(input).toArray(new String[input.size()]);
            }
        });
        return encodeJwt(httpMethod, url, paramsAsArrays, userId.orNull(), issuer, sharedSecret);
    }

    private String encodeJwt(String httpMethod, URI targetPath, Map<String, String[]> params, String userKeyValue, String issuer, String sharedSecret)
            throws JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException
    {

        JwtJsonBuilder jsonBuilder = new JsonSmartJwtJsonBuilder()
                .issuedAt(TimeUtil.currentTimeSeconds())
                .expirationTime(TimeUtil.currentTimePlusNSeconds(jwtExpiryWindowSeconds))
                .issuer(issuer);

        if (null != userKeyValue)
        {
            jsonBuilder = jsonBuilder.subject(userKeyValue);
        }

        Map<String, String[]> completeParams = params;

        try
        {
            if (!StringUtils.isEmpty(targetPath.getQuery()))
            {
                completeParams = new HashMap(params);
                completeParams.putAll(constructParameterMap(targetPath));
            }

            CanonicalHttpUriRequest canonicalHttpUriRequest = new CanonicalHttpUriRequest(httpMethod.toString(),
                    targetPath.getPath(), "", completeParams);

            JwtClaimsBuilder.appendHttpRequestClaims(jsonBuilder, canonicalHttpUriRequest);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }

        return issueJwt(jsonBuilder.build(), sharedSecret);
    }


    private String issueJwt(String jsonPayload, String sharedSecret) throws JwtSigningException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException
    {
        return getJwtWriter(sharedSecret).jsonToJwt(jsonPayload);
    }

    private JwtWriter getJwtWriter(String sharedSecret) throws JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException
    {
        return jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256, sharedSecret);
    }

    private static Map<String, String[]> constructParameterMap(URI uri) throws UnsupportedEncodingException
    {
        final String query = uri.getQuery();
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

