package com.atlassian.plugin.connect.test.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.jira.security.auth.trustedapps.KeyFactory;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.applinks.ApplinkJwt;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.jwt.applinks.exception.NotAJwtPeerException;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtParseException;
import com.atlassian.jwt.exception.JwtSigningException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.exception.JwtVerificationException;
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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JwtSigningInteroperabilityTest
{
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

    private static class SigningTests
    {
        private final List<Map<String, String>> tests = Lists.newArrayList();


        public void add(String description, String signature)
        {
            tests.add(ImmutableMap.of(description, signature));
        }

        public Appendable toJSON(Appendable out)
        {
            Gson gson = new GsonBuilder().create();
            gson.toJson(ImmutableMap.of("tests", tests), out);
            return out;
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
                new TestJwtService("s0m3-sh@r3d-s3cr37"),
                consumerService,
                connectApplinkManager,
                httpContentRetriever,
                userManager);
    }

    @Test
    public void generateTests() throws Exception
    {
        SigningTests tests = new SigningTests();

        tests.add("Spaces", createAndSign("param", "some spaces in this parameter"));
        tests.add("Asterisk", createAndSign("query", "connect*"));
        tests.add("Unicode", createAndSign("director", "宮崎 駿"));
        tests.add("Comma-delimited", createAndSign("ids", "10,2,20,1"));
        tests.add("Plus", createAndSign("title", "1 + 1 equals 3"));
        tests.add("JSON Object", createAndSign("json", "{\"key\":\"value\"}"));
        tests.add("JSON Array", createAndSign("json", "[\"val1\",\"val2\"]"));
        tests.add("Single Quotes", createAndSign("quote", "'quoted'"));
        tests.add("Brackets", createAndSign("param", "()"));
        tests.add("Tilde", createAndSign("eta", "in ~3 days"));
        tests.add("RFC-1738 Unsafe", createAndSign("rfc", " <>\"#%{}|\\^~[]`"));
        tests.add("RFC-1738 Reserved", createAndSign("rfc", ";/?:@=&"));
        tests.add("RFC-1738 Special", createAndSign("rfc", "$-_.+!*'(),"));
        tests.add("Empty", createAndSign("notmuch", ""));
        tests.add("Encoded", createAndSign("referrer", "http://from.net/p?x=A+%2B+B&y=%24-_.%2B%21*%27%28%29%2C"));
        tests.add("Multiple", createAndSign("ids", "1","10", "-1","20", "2"));
        tests.add("Multiple II", createAndSign("ids", ".1",":1",":2", ".2"));
        tests.add("Multiple Unicode", createAndSign("chars", "宮","崎","駿"));
        tests.add("Multiple Empty", createAndSign("c", ""," ","+", "%20"));
        tests.add("Key RFC-1738 Unsafe", createAndSign("#1", "value"));
        tests.add("Key RFC-1738 Reserved", createAndSign(":1", "value"));
        tests.add("Key RFC-1738 Special", createAndSign("$1", "value"));

        System.out.println(tests.toJSON(new StringBuilder()).toString());

    }

    private String createAndSign(String key, String... values)
    {
        return signer.signGetUrl(basePath, ImmutableMap.of(key, values));
    }
}
