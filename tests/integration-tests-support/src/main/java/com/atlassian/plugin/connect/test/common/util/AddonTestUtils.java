package com.atlassian.plugin.connect.test.common.util;

import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtWriter;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;

public class AddonTestUtils {
    public static String randomAddonKey() {
        // include underscores in add-on key: used in the separator at ModuleKeyUtils
        return "some.test_addon__" + RandomStringUtils.randomAlphanumeric(8).replaceAll("3", "4").toLowerCase();
    }

    public static String randomModuleKey() {
        return RandomStringUtils.randomAlphanumeric(20).replaceAll("3", "4").toLowerCase();
    }

    public static WebItemModuleBean randomWebItemBean() {
        return newWebItemBean()
                .withName(new I18nProperty(randomModuleKey(), null))
                .withKey(randomModuleKey())
                .withLocation("system.nowhere")
                .withUrl("/nowhere")
                .build();
    }

    private static Pattern regex = Pattern.compile("[(!\"#$%&'\\(\\)*+,./:;<=>?@\\[\\\\\\]^`{|}~)]");

    public static String escapeJQuerySelector(String selector) {
        if (selector == null) {
            return null;
        }
        return regex.matcher(selector).replaceAll("\\\\$0");
    }

    public static String generateJwtSignature(HttpMethod httpMethod, URI uri, String addonKey, String secret, String contextPath, String subject) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        JwtWriterFactory jwtWriterFactory = new NimbusJwtWriterFactory();
        JwtWriter jwtWriter = jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256, secret);

        // Parse param values and build a map
        final List<NameValuePair> rawParams = URLEncodedUtils.parse(uri, "UTF-8");
        final ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        for (NameValuePair rawParam : rawParams) {
            builder.put(rawParam.getName(), rawParam.getValue());
        }

        final ImmutableMap.Builder<String, String[]> paramsMap = ImmutableMap.builder();
        for (Map.Entry<String, Collection<String>> stringCollectionEntry : builder.build().asMap().entrySet()) {
            final Collection<String> collection = stringCollectionEntry.getValue();
            paramsMap.put(stringCollectionEntry.getKey(), collection.toArray(new String[collection.size()]));
        }

        final JwtJsonBuilder jsonBuilder = new JsonSmartJwtJsonBuilder()
                .issuer(addonKey)
                .queryHash(HttpRequestCanonicalizer.computeCanonicalRequestHash(new CanonicalHttpUriRequest(httpMethod.name(), uri.getPath(), URI.create(contextPath).getPath(), paramsMap.build())));

        if (null != subject) {
            jsonBuilder.subject(subject);
        }

        return jwtWriter.jsonToJwt(jsonBuilder.build());
    }
}
