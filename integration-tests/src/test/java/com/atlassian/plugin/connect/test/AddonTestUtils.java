package com.atlassian.plugin.connect.test;

import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtWriter;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import org.apache.commons.lang.RandomStringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;

public class AddonTestUtils
{
    public static String randomAddOnKey()
    {
        // include underscores in add-on key: used in the separator at ModuleKeyUtils
        return "some.test_addon__" + RandomStringUtils.randomAlphanumeric(8).replaceAll("3", "4").toLowerCase();
    }

    public static String randomModuleKey()
    {
        return RandomStringUtils.randomAlphanumeric(20).replaceAll("3", "4").toLowerCase();
    }

    public static WebItemModuleBean randomWebItemBean()
    {
        return newWebItemBean()
                .withName(new I18nProperty(randomModuleKey(), ""))
                .withKey(randomModuleKey())
                .withLocation("system.nowhere")
                .withUrl("/nowhere")
                .build();
    }

    public static String escapedAddonKey(String addonKey)
    {
        return escapeJQuerySelector(addonKey);
    }

    public static String escapedAddonAndModuleKey(String addonKey, String moduleKey)
    {
        return escapeJQuerySelector(ModuleKeyUtils.addonAndModuleKey(addonKey, moduleKey));
    }

    private static Pattern regex = Pattern.compile("[(!\"#$%&'\\(\\)*+,./:;<=>?@\\[\\\\\\]^`{|}~)]");
    public static String escapeJQuerySelector(String selector)
    {
        if (selector == null)
        {
            return null;
        }
        return regex.matcher(selector).replaceAll("\\\\$0");
    }

    public static URI signWithJwt(@Nonnull final URI uri,
                                  @Nonnull final String addOnKey,
                                  @Nonnull final String secret,
                                  @Nonnull final String contextPath,
                                  @Nullable final String subject) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        JwtWriterFactory jwtWriterFactory = new NimbusJwtWriterFactory();
        JwtWriter jwtWriter = jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256, secret);
        final JwtJsonBuilder jsonBuilder = new JsonSmartJwtJsonBuilder()
                .issuer(addOnKey)
                .queryHash(HttpRequestCanonicalizer.computeCanonicalRequestHash(new CanonicalHttpUriRequest("GET", uri.getPath(), URI.create(contextPath).getPath())));

        if (null != subject)
        {
            jsonBuilder.subject(subject);
        }

        String jwtToken = jwtWriter.jsonToJwt(jsonBuilder.build());
        return URI.create(uri.toString() + "?jwt=" + jwtToken);
    }
}
