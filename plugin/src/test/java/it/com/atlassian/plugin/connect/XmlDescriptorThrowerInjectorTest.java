package it.com.atlassian.plugin.connect;

import com.atlassian.plugin.connect.XmlDescriptorThrower;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class XmlDescriptorThrowerInjectorTest
{
    private final XmlDescriptorThrower xmlDescriptorThrower;

    public XmlDescriptorThrowerInjectorTest(XmlDescriptorThrower xmlDescriptorThrower)
    {
        this.xmlDescriptorThrower = xmlDescriptorThrower;
    }

    @Test
    public void makeXmlDescriptorCodeThrowErrors()
    {
        assertEquals(Collections.<Class>emptySet(), xmlDescriptorThrower.runAndGetProxyFailures());
    }
}
