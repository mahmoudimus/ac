package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.jwt.JwtConstants;
import com.atlassian.plugin.connect.api.auth.scope.AddonKeyExtractor;
import com.atlassian.plugin.connect.plugin.request.AddonKeyExtractorImpl;
import com.atlassian.plugin.connect.plugin.JsonConnectAddonIdentifierService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddonKeyExtractorTest {
    private static final String ADD_ON_KEY = "my-add-on";

    private AddonKeyExtractor addonKeyExtractor;

    @Mock
    private JsonConnectAddonIdentifierService jsonConnectAddonIdentifierService;
    @Mock
    private HttpServletRequest request;

    @Before
    public void setUp() throws Exception {
        when(request.getRequestURI()).thenReturn("/confluence/rest/xyz");
        when(request.getContextPath()).thenReturn("/confluence");

        addonKeyExtractor = new AddonKeyExtractorImpl(jsonConnectAddonIdentifierService);
    }

    @Test
    public void testIsPluginRequestForAddonJwtRequest() throws Exception {
        when(jsonConnectAddonIdentifierService.isConnectAddon(ADD_ON_KEY)).thenReturn(true);
        when(request.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(ADD_ON_KEY);
        assertTrue(addonKeyExtractor.isAddonRequest(request));
    }

    @Test
    public void testIsPluginRequestForClientKey() throws Exception {
        when(jsonConnectAddonIdentifierService.isConnectAddon(ADD_ON_KEY)).thenReturn(true);
        when(request.getHeader(AddonKeyExtractor.AP_REQUEST_HEADER)).thenReturn(ADD_ON_KEY);
        assertTrue(addonKeyExtractor.isAddonRequest(request));
    }

    @Test
    public void testNoKeyForNotConnectAddon() throws Exception {
        when(jsonConnectAddonIdentifierService.isConnectAddon(ADD_ON_KEY)).thenReturn(false);
        when(request.getHeader(AddonKeyExtractor.AP_REQUEST_HEADER)).thenReturn(ADD_ON_KEY);
        assertNull(addonKeyExtractor.getAddonKeyFromHttpRequest(request));
    }

    @Test
    public void testIsNotPluginRequestForOtherJwtRequest() throws Exception {
        when(jsonConnectAddonIdentifierService.isConnectAddon(ADD_ON_KEY)).thenReturn(false);
        when(request.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(ADD_ON_KEY);
        assertFalse(addonKeyExtractor.isAddonRequest(request));
    }
}
