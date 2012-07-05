package it;

import com.atlassian.functest.rest.TestResults;
import com.atlassian.labs.remoteapps.test.OwnerOfTestedProduct;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.URI;

import static junit.framework.Assert.*;

public abstract class AbstractGroupClient
{
    private final String group;
    private final int port;
    private final String contextPath;

    public AbstractGroupClient(String group)
    {
        this(group, OwnerOfTestedProduct.INSTANCE.getProductInstance().getHttpPort(),
                OwnerOfTestedProduct.INSTANCE.getProductInstance().getContextPath());
    }
    public AbstractGroupClient(String group, int port, String contextPath)
    {
        this.group = group;
        this.port = port;
        this.contextPath = contextPath;
    }

    @Test
    public void run()
    {
        File targetDir = new File("target");
        URI uri = UriBuilder.fromUri("http://localhost/")
                            .port(port)
                            .path(contextPath)
                            .path("rest")
                            .path("functest")
                            .path("latest")
                            .path("junit")
                            .path("runTests")
                            .build();

        final WebResource client = Client.create().resource(uri).queryParam("outdir", targetDir.getAbsolutePath());
        if (group != null)
        {
            client.queryParam("groups", group);
        }
        TestResults results = client.get(TestResults.class);
        assertNotNull(results);

        System.out.println("Results: " + results.output);

        assertEquals(0, results.result); // make sure that the number of failing tests is 0
    }
}
