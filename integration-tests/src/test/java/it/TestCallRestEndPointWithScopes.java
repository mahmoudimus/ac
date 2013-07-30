package it;

import com.atlassian.fugue.Either;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.remotable.test.server.RunnerSignedRequestHandler;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import static com.atlassian.fugue.Either.left;
import static com.atlassian.fugue.Either.right;
import static com.atlassian.plugin.remotable.test.Utils.createSignedRequestHandler;
import static com.google.common.io.CharStreams.newReaderSupplier;
import static it.TestConstants.BETTY;
import static org.junit.Assert.assertEquals;

public final class TestCallRestEndPointWithScopes
{
    private static final Logger logger = LoggerFactory.getLogger(TestCallRestEndPointWithScopes.class);

    private static String baseUrl;
    private static AtlassianConnectAddOnRunner addOnRunner;
    private static RunnerSignedRequestHandler signedRequestHandler;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        final String appKey = RandomStringUtils.randomAlphanumeric(20);
        baseUrl = OwnerOfTestedProduct.INSTANCE.getProductInstance().getBaseUrl();
        signedRequestHandler = createSignedRequestHandler(appKey);
        addOnRunner = new AtlassianConnectAddOnRunner(baseUrl, appKey)
                .addOAuth(signedRequestHandler)
                .addPermission(Permissions.CREATE_OAUTH_LINK)
                .addPermission("resttest")
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (addOnRunner != null)
        {
            addOnRunner.stop();
        }
    }

    @Test
    public void testCallWithLoggedInUser() throws Exception
    {
        final Either<Integer, String> result = sendSignedGet(signedRequestHandler, URI.create(baseUrl + "/rest/remoteplugintest/latest/user"), BETTY);
        assertEquals(BETTY, result.fold(fail(), Functions.identity()));
    }

    @Test
    public void testCallAsAnonymousAndSigned() throws Exception
    {
        final Either<Integer, String> result = sendSignedGet(signedRequestHandler, URI.create(baseUrl + "/rest/remoteplugintest/latest/user"), "");
        assertEquals("", result.fold(fail(), Functions.identity()));
    }

    @Test
    public void testForbiddenCallForUnknownScope() throws Exception
    {
        final Either<Integer, String> result = sendSignedGet(signedRequestHandler, URI.create(baseUrl + "/rest/remoteplugintest/latest/unscoped"), BETTY);
        assertEquals(403, result.fold(Functions.identity(), fail()));
    }

    @Test
    public void testUnauthorizedCallForUnknownUser() throws Exception
    {
        final Either<Integer, String> result = sendSignedGet(signedRequestHandler, URI.create(baseUrl + "/rest/remoteplugintest/latest/unscoped"), "darkstranger");
        assertEquals(401, result.fold(Functions.identity(), fail()));
    }

    @Test
    public void testForbiddenCallForUnrequestedScope() throws Exception
    {
        final Either<Integer, String> result = sendSignedGet(signedRequestHandler, URI.create(baseUrl + "/rest/remoteplugintest/latest/unauthorisedscope"), BETTY);
        assertEquals(403, result.fold(Functions.identity(), fail()));
    }

    public static Either<Integer, String> sendSignedGet(SignedRequestHandler signedRequestHandler, URI uri, String user)
    {
        final HttpURLConnection connection = openSignedGet(signedRequestHandler, uri, user);
        try
        {
            return right(CharStreams.toString(newReaderSupplier(new InputSupplier<InputStream>()
            {
                @Override
                public InputStream getInput() throws IOException
                {
                    return connection.getInputStream();
                }
            }, Charset.forName("UTF-8"))));
        }
        catch (IOException e)
        {
            try
            {
                if (connection != null)
                {
                    return left(connection.getResponseCode());
                }
                throw new RuntimeException("No connection => no status code.");
            }
            catch (IOException e1)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private static HttpURLConnection openSignedGet(SignedRequestHandler signedRequestHandler, URI uri, String user)
    {
        try
        {
            final URL url = newUrl(uri, user);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            signedRequestHandler.sign(uri, "GET", user, connection);
            logger.debug("Opening connection to '{}'", url);
            return connection;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static URL newUrl(URI uri, String user)
    {
        final String spec = uri + "?user_id=" + urlEncode(user);
        try
        {
            return new URL(spec);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("Is that malformed? " + spec, e);
        }
    }

    private static String urlEncode(String user)
    {
        try
        {
            return URLEncoder.encode(user, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Seems like UTF-8 is not supported here!", e);
        }
    }

    private static <F, T> Function<F, T> fail()
    {
        return new FailFunction<F, T>();
    }

    private static class FailFunction<F, T> implements Function<F, T>
    {
        @Override
        public T apply(@Nullable F input)
        {
            Assert.fail("Failed with value: " + input);
            return null;
        }
    }
}
