package it;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.atlassian.functest.rest.TestResults;
import com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider;

import org.junit.Test;

import static org.junit.Assert.*;

public abstract class AbstractGroupClient
{
    private final String group;
    private final int port;
    private final String contextPath;

    public AbstractGroupClient(String group)
    {
        this(group, TestedProductProvider.getTestedProduct().getProductInstance().getHttpPort(),
                TestedProductProvider.getTestedProduct().getProductInstance().getContextPath());
    }

    public AbstractGroupClient(String group, int port, String contextPath)
    {
        this.group = group;
        this.port = port;
        this.contextPath = contextPath;
    }

    @Test
    public void run() throws IOException, JAXBException
    {
        File targetDir = new File("target");
        UriBuilder builder = UriBuilder.fromUri("http://localhost/")
                                       .port(port)
                                       .path(contextPath)
                                       .path("rest")
                                       .path("functest")
                                       .path("latest")
                                       .path("junit")
                                       .path("runTests")
                                       .queryParam("outdir", targetDir.getAbsolutePath());
        if (group != null)
        {
            builder.queryParam("groups", group);
        }

        InputStream in = new URL(builder.build().toString()).openStream();
        TestResults results = (TestResults) JAXBContext.newInstance(TestResults.class).createUnmarshaller().unmarshal(in);
        assertNotNull(results);

        System.out.println("Results: " + results.output);

        assertEquals(0, results.result); // make sure that the number of failing tests is 0
    }
}
