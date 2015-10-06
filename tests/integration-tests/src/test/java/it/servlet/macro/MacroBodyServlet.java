package it.servlet.macro;

import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import it.servlet.ContextServlet;
import it.servlet.InstallHandlerServlet;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Test servlet that can be used to test logic that requires the collection of the macro
 * body.  This servlet can collect the macro body from confluence using the macro id or the
 * macro hash.
 *
 * You need to define your macro url to include the following parameters:
 * pageId - {page.id}
 * pageVersion - {page.version}
 * macroId - {macro.id} (for collection by id)
 * macroHash - {macro.hash} (for collection by hash)
 *
 * The body handler will be called with the collected body.
 */
public class MacroBodyServlet extends ContextServlet
{
    public static enum CollectionType
    {
        BY_HASH,
        BY_ID
    }

    private final CollectionType collectionType;
    private final String baseUrl;
    private final InstallHandlerServlet installHandlerServlet;
    private final BodyHandler bodyHandler;
    private final String addonKey;

    public MacroBodyServlet(CollectionType collectionType, String baseUrl, String addonKey,
                            InstallHandlerServlet installHandlerServlet, BodyHandler bodyHandler)
    {
        this.collectionType = collectionType;
        this.baseUrl = baseUrl;
        this.addonKey = addonKey;
        this.installHandlerServlet = installHandlerServlet;
        this.bodyHandler = bodyHandler;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context)
    {
        try
        {
            String pageId = req.getParameter("pageId");
            String pageVersion = req.getParameter("pageVersion");

            URL url = null;
            switch (collectionType)
            {
                case BY_ID:
                    String macroId = req.getParameter("macroId");
                    url = URI.create(baseUrl + "/rest/api/content/" + pageId + "/history/" + pageVersion + "/macro/id/" + macroId).toURL();
                    break;
                case BY_HASH:
                    String macroHash = req.getParameter("macroHash");
                    url = URI.create(baseUrl + "/rest/api/content/" + pageId + "/history/" + pageVersion + "/macro/hash/" + macroHash).toURL();
                    break;
            }

            String sharedSecret = checkNotNull(installHandlerServlet.getInstallPayload().getSharedSecret());
            String jwt = AddonTestUtils.generateJwtSignature(HttpMethod.GET,
                    url.toURI(),
                    addonKey,
                    sharedSecret,
                    baseUrl,
                    null);

            URL authenticatedUrl = new URL(url + "?jwt=" + jwt);
            HttpURLConnection connection = (HttpURLConnection) authenticatedUrl.openConnection();
            connection.setRequestMethod("GET");
            String json = IOUtils.toString(connection.getInputStream());

            JSONObject o = (JSONObject) JSONValue.parse(json);
            String body = o.get("body").toString();

            bodyHandler.processBody(req, resp, context, body);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            // this should cause the test to fail
            throw new RuntimeException(e);
        }
    }

}