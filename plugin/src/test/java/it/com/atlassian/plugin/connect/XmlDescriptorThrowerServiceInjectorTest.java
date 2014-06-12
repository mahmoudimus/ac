package it.com.atlassian.plugin.connect;

import com.atlassian.plugin.connect.xmldescriptor.XmlDescriptorThrowerService;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class XmlDescriptorThrowerServiceInjectorTest
{
    private final XmlDescriptorThrowerService xmlDescriptorThrowerService;

    public XmlDescriptorThrowerServiceInjectorTest(XmlDescriptorThrowerService xmlDescriptorThrowerService)
    {
        this.xmlDescriptorThrowerService = xmlDescriptorThrowerService;
    }

    @Test
    public void makeXmlDescriptorCodeThrowErrors()
    {
        assertEquals(Collections.<String>emptySet(), xmlDescriptorThrowerService.runAndGetProxyFailures());
    }
}
