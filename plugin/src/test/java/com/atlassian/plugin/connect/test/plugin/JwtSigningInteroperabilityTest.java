package com.atlassian.plugin.connect.test.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.jira.security.auth.trustedapps.KeyFactory;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.applinks.ApplinkJwt;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.jwt.applinks.exception.NotAJwtPeerException;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtParseException;
import com.atlassian.jwt.exception.JwtSigningException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.exception.JwtVerificationException;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.reader.JwtClaimVerifier;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.JwtSigningRemotablePluginAccessor;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.util.http.HttpContentRetriever;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * This class doesn't test anything on its own. It generates a list of signed URLs as a JSON file that can be used as
 * test input in another framework like ACE (--> /test/jwt_signed_url_interoperability_test.js).
 * <p/>
 * To generate the signed URLs directly into ACE, follow these steps:
 * 1/ Set a JVM property -Djwtinteroptest.file={path to ACE}/test/resources/jwt-signed-urls.json, e.g.:
 * -Djwtinteroptest.file=~/dev/atlassian-connect-express/test/resources/jwt-signed-urls.json
 * 2/ Run this test
 * 3/ Run 'mocha test' in ACE
 */
@RunWith(MockitoJUnitRunner.class)
public class JwtSigningInteroperabilityTest
{
    public static final String SHARED_SECRET = "s0m3-sh@r3d-s3cr37";

    private static class TestJwtService implements JwtService
    {
        private final String sharedSecret;
        private JwtWriterFactory jwtWriterFactory;

        private TestJwtService(String sharedSecret)
        {
            this.sharedSecret = sharedSecret;
            this.jwtWriterFactory = new NimbusJwtWriterFactory();
        }

        @Override
        public boolean isJwtPeer(ApplicationLink applicationLink)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ApplinkJwt verifyJwt(String jwt, Map<String, ? extends JwtClaimVerifier> claimVerifiers) throws NotAJwtPeerException, JwtParseException, JwtVerificationException, TypeNotInstalledException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String issueJwt(String jsonPayload, ApplicationLink applicationLink) throws NotAJwtPeerException, JwtSigningException
        {
            return jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256, sharedSecret).jsonToJwt(jsonPayload);
        }
    }

    private static class SignedUrlTest
    {
        private final String name;
        private final String canonicalUrl;
        private final String signedUrl;

        private SignedUrlTest(String name, String canonicalUrl, String signedUrl)
        {
            this.name = name;
            this.canonicalUrl = canonicalUrl;
            this.signedUrl = signedUrl;
        }

        private String getName()
        {
            return name;
        }

        private String getCanonicalUrl()
        {
            return canonicalUrl;
        }

        private String getSignedUrl()
        {
            return signedUrl;
        }
    }

    private static class SigningTests
    {
        private final List<SignedUrlTest> tests = Lists.newArrayList();
        private final String sharedSecret;

        public SigningTests(String sharedSecret)
        {
            this.sharedSecret = sharedSecret;
        }

        public void add(SignedUrlTest signedUrlTest)
        {
            tests.add(signedUrlTest);
        }

        public Appendable toJSON(Appendable out)
        {
            Gson gson = new GsonBuilder().create();
            gson.toJson(ImmutableMap.of("secret", sharedSecret, "tests", tests, "comment", createComment()), out);
            return out;
        }

        private String createComment()
        {
            return "Generated by " + getClass().getCanonicalName() + " on " + new Date();
        }
    }

    private JwtSigningRemotablePluginAccessor signer;
    private Supplier<URI> baseUrlSupplier = Suppliers.ofInstance(URI.create("https://example.com"));
    private URI basePath = URI.create("/test");

    @Mock
    private ConsumerService consumerService;
    @Mock
    private ConnectApplinkManager connectApplinkManager;
    @Mock
    private HttpContentRetriever httpContentRetriever;
    @Mock
    private UserManager userManager;
    @Mock
    private ApplicationLink applicationLink;

    @Before
    public void setup()
    {
        when(consumerService.getConsumer()).thenReturn(Consumer.key("jira:1234-5678-9000").name("whatever").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).publicKey(new KeyFactory.InvalidPublicKey(new Exception())).build());
        when(userManager.getRemoteUserKey()).thenReturn(new UserKey("123456789"));
        when(connectApplinkManager.getAppLink(anyString())).thenReturn(applicationLink);

        Plugin plugin = new PluginForTests("my-plugin", "My Plugin");

        signer = new JwtSigningRemotablePluginAccessor(plugin,
                baseUrlSupplier,
                new TestJwtService(SHARED_SECRET),
                consumerService,
                connectApplinkManager,
                httpContentRetriever,
                userManager);
    }

    @Test
    public void generateTests() throws Exception
    {
        SigningTests tests = new SigningTests(SHARED_SECRET);

        tests.add(createAndSign("Simple", "param", "value"));
        tests.add(createAndSign("Spaces", "param", "some spaces in this parameter"));
        tests.add(createAndSign("Asterisk", "query", "connect*"));
        tests.add(createAndSign("Unicode", "director", "宮崎 駿"));
        tests.add(createAndSign("Comma-delimited", "ids", "10,2,20,1"));
        tests.add(createAndSign("Multi-value Comma-delimited", "tuples", "1,2,3", "6,5,4", "7,9,8"));
        tests.add(createAndSign("Plus", "title", "1 + 1 equals 3"));
        tests.add(createAndSign("JSON Object", "json", "{\"key\":\"value\"}"));
        tests.add(createAndSign("JSON Array", "json", "[\"val1\",\"val2\"]"));
        tests.add(createAndSign("Single Quotes", "quote", "'quoted'"));
        tests.add(createAndSign("Brackets", "param", "()"));
        tests.add(createAndSign("Tilde", "eta", "in ~3 days"));
        tests.add(createAndSign("RFC-1738 Unsafe", "rfc", " <>\"#%{}|\\^~[]`"));
        tests.add(createAndSign("RFC-1738 Reserved", "rfc", ";/?:@=&"));
        tests.add(createAndSign("RFC-1738 Special", "rfc", "$-_.+!*'(),"));
        tests.add(createAndSign("Empty", "notmuch", ""));
        tests.add(createAndSign("Encoded", "referrer", "http://from.net/p?x=A+%2B+B&y=%24-_.%2B%21*%27%28%29%2C"));
        tests.add(createAndSign("Multi-value", "ids", "1", "10", "-1", "20", "2"));
        tests.add(createAndSign("Multi-value II", "ids", ".1", ":1", ":2", ".2"));
        tests.add(createAndSign("Multi-value Unicode", "chars", "宮", "崎", "駿"));
        tests.add(createAndSign("Multi-value Empty", "c", "", " ", "+", "%20"));
        tests.add(createAndSign("Key RFC-1738 Unsafe", "#1", "value"));
        tests.add(createAndSign("Key RFC-1738 Reserved", ":1", "value"));
        tests.add(createAndSign("Key RFC-1738 Special", "$1", "value"));
        tests.add(createAndSign("Multiple Parameters Simple", ImmutableMap.of("a", new String[]{"x"},
                "b", new String[]{"y"})));
        tests.add(createAndSign("Multiple Multi-value Parameters", ImmutableMap.of("a", new String[]{"x10", "x1"},
                "b", new String[]{"y1", "y10"})));
        tests.add(createAndSign("Multiple Parameters Spaces", ImmutableMap.of("a", new String[]{"one string", "another one"},
                "b", new String[]{"more here", "and yet more"})));
        tests.add(createAndSign("Multiple Parameters Comma-delimited", ImmutableMap.of("a", new String[]{"1,2,3", "4,5,6"},
                "b", new String[]{"a,b,c", "d,e,f"})));
        tests.add(createAndSign("Parameter Order", ImmutableMap.of("a10", new String[]{"1"},
                "a1", new String[]{"2"},
                "b1", new String[]{"3"},
                "b10", new String[]{"4"})));
        tests.add(createAndSign("Upper- and Lower-case Parameters", ImmutableMap.of("A", new String[]{"A"},
                "a", new String[]{"a"},
                "b", new String[]{"b"},
                "B", new String[]{"B"})));

        write(tests);
    }

    private void write(SigningTests tests) throws IOException
    {
        System.out.println(tests.toJSON(new StringBuilder()));
        String filePath = System.getProperty("jwtinteroptest.file");
        if (null != filePath)
        {
            OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8");
            try
            {
                tests.toJSON(fileWriter);
            }
            finally
            {
                fileWriter.close();
            }
        }
    }

    private SignedUrlTest createAndSign(String name, String key, String... values) throws UnsupportedEncodingException
    {
        return createAndSign(name, ImmutableMap.of(key, values));
    }

    private SignedUrlTest createAndSign(String name, Map<String, String[]> params) throws UnsupportedEncodingException
    {
        String canonicalUrl = HttpRequestCanonicalizer.canonicalize(new CanonicalHttpUriRequest("GET", basePath.getPath(), "", params));
        String signedUrl = signer.signGetUrl(basePath, params);
        return new SignedUrlTest(name, canonicalUrl, signedUrl);
    }
}
