package it;

import com.atlassian.labs.remoteapps.test.RemoteAppRunner;
import com.atlassian.labs.remoteapps.test.RunnerSignedRequestHandler;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import static com.atlassian.labs.remoteapps.test.Utils.createSignedRequestHandler;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestOAuth extends AbstractBrowserlessTest
{
    @Test
    public void testAuthorizeRequestWorks() throws Exception
    {
        RunnerSignedRequestHandler requestHandler = createSignedRequestHandler(
                "authorizeRequestWorks");
        RemoteAppRunner runner = new RemoteAppRunner(baseUrl,
                "authorizeRequestWorks")
                .addOAuth(requestHandler)
                .start();

        URL url = new URL(baseUrl + "/plugins/servlet/oauth/request-token");
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        yc.setDoOutput(true);
        yc.setDoInput(true);
        yc.setRequestMethod("POST");
        requestHandler.sign(url.toString(), "POST", null, yc);
        yc.getOutputStream().close();
        assertEquals(200, yc.getResponseCode());

        runner.stop();
    }

}
