package com.atlassian.plugin.connect.plugin.auth.jwt;

import com.atlassian.jwt.JwtService;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.core.TimeUtil;
import com.atlassian.jwt.core.writer.JwtClaimsBuilder;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtJsonBuilderFactory;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.api.auth.ReKeyableAuthorizationGenerator;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
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
import java.util.Optional;

import static com.atlassian.jwt.JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Set the system property {@link JwtAuthorizationGenerator#JWT_EXPIRY_SECONDS_PROPERTY} with an integer value to control
 * the size of the expiry window (default is {@link JwtAuthorizationGenerator#JWT_EXPIRY_WINDOW_SECONDS_DEFAULT}).
 */
public class JwtAuthorizationGenerator implements ReKeyableAuthorizationGenerator {
    private static final char[] QUERY_DELIMITERS = new char[]{'&'};

    private static final String JWT_EXPIRY_SECONDS_PROPERTY = "com.atlassian.connect.jwt.expiry_seconds";
    /**
     * Default of 3 minutes.
     */
    private static final long JWT_EXPIRY_WINDOW_SECONDS_DEFAULT = MINUTES.toSeconds(3);
    private static final long JWT_EXPIRY_WINDOW_SECONDS = Long.getLong(JWT_EXPIRY_SECONDS_PROPERTY, JWT_EXPIRY_WINDOW_SECONDS_DEFAULT);

    private static final Logger log = LoggerFactory.getLogger(JwtAuthorizationGenerator.class);

    private final JwtJsonBuilderFactory jwtBuilderFactory;
    private final JwtService jwtService;
    private final Supplier<String> secretSupplier;
    private final ConsumerService consumerService;
    private final URI addonBaseUrl;

    public JwtAuthorizationGenerator(JwtService jwtService, JwtJsonBuilderFactory jwtBuilderFactory, Supplier<String> secretSupplier, ConsumerService consumerService, URI addonBaseUrl) {
        this.jwtBuilderFactory = jwtBuilderFactory;
        this.jwtService = checkNotNull(jwtService);
        this.secretSupplier = checkNotNull(secretSupplier);
        this.consumerService = checkNotNull(consumerService);
        this.addonBaseUrl = checkNotNull(addonBaseUrl);
    }

    @Override
    public Optional<String> generate(HttpMethod httpMethod, URI url, Map<String, String[]> parameters) {
        return Optional.of(generate(httpMethod, url, parameters, checkNotNull(secretSupplier.get())));
    }

    @Override
    public String generate(HttpMethod httpMethod, URI url, Map<String, String[]> parameters, String secret) {
        checkArgument(null != parameters, "Parameters Map argument cannot be null");
        return JWT_AUTH_HEADER_PREFIX + encodeJwt(httpMethod, url, addonBaseUrl, parameters, consumerService.getConsumer().getKey(), secret);
    }

    String encodeJwt(HttpMethod httpMethod, URI targetPath, URI addonBaseUrl, Map<String, String[]> params, String issuerId, String secret) {
        checkArgument(null != httpMethod, "HttpMethod argument cannot be null");
        checkArgument(null != targetPath, "URI argument cannot be null");
        checkArgument(null != addonBaseUrl, "base URI argument cannot be null");
        checkArgument(null != secret, "secret argument cannot be null");

        final long currentTime = TimeUtil.currentTimeSeconds();
        JwtJsonBuilder jsonBuilder = jwtBuilderFactory.jsonBuilder()
                .issuedAt(currentTime)
                .expirationTime(currentTime + JWT_EXPIRY_WINDOW_SECONDS)
                .issuer(issuerId);

        Map<String, String[]> completeParams = params;

        try {
            if (!StringUtils.isEmpty(targetPath.getQuery())) {
                completeParams = new HashMap<>(params);
                completeParams.putAll(constructParameterMap(targetPath));
            }

            CanonicalHttpUriRequest request = new CanonicalHttpUriRequest(httpMethod.toString(), extractRelativePath(targetPath, addonBaseUrl), "", completeParams);
            log.debug("Canonical request is: " + HttpRequestCanonicalizer.canonicalize(request));

            JwtClaimsBuilder.appendHttpRequestClaims(jsonBuilder, request);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return jwtService.issueJwt(jsonBuilder.build(), secret);
    }

    private static String extractRelativePath(URI targetUri, URI addonBaseUri) {
        String path = targetUri.getPath();
        final String targetString = targetUri.toString();
        final String baseString = addonBaseUri.toString();

        if (!StringUtils.isEmpty(targetString) && !StringUtils.isEmpty(baseString)) {
            if (targetString.startsWith(baseString)) {
                path = URI.create(StringUtils.removeStart(targetString, baseString)).getPath();
            } else {
                // don't sign something intended for "example.com" that is going to "other.domain.biz"
                if (targetUri.isAbsolute()) {
                    final String message = String.format("Do not ask for the target URL '%s' to be signed for an add-on with a base URL of '%s': an absolute target URL should begin with the base URL.",
                            targetString, baseString);
                    throw new IllegalArgumentException(message);
                }
            }
        }

        return path;
    }

    @VisibleForTesting
    public static Map<String, String[]> constructParameterMap(URI uri) throws UnsupportedEncodingException {
        // Do not use uri.getQuery() here, as getQuery() already decodes, and we decode again below
        final String query = uri.getRawQuery();
        if (query == null) {
            return Collections.emptyMap();
        }

        Map<String, String[]> queryParams = new HashMap<>();

        CharArrayBuffer buffer = new CharArrayBuffer(query.length());
        buffer.append(query);
        ParserCursor cursor = new ParserCursor(0, buffer.length());

        while (!cursor.atEnd()) {
            NameValuePair nameValuePair = BasicHeaderValueParser.INSTANCE.parseNameValuePair(buffer, cursor, QUERY_DELIMITERS);

            if (!StringUtils.isEmpty(nameValuePair.getName())) {
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

    private static String urlDecode(final String content) throws UnsupportedEncodingException {
        return null == content ? null : URLDecoder.decode(content, "UTF-8");
    }
}
