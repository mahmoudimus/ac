package junit.all;

import org.junit.Test;
import services.ServiceAccessor;

import static org.junit.Assert.assertNull;

/**
 */
public class PluginLicenseRetrieverTest
{

    @Test
    public void testRetrieveLicense()
    {
        assertNull(ServiceAccessor.getLicenseRetriever().retrieve().claim());

        // todo: test with a real license
    }
}
