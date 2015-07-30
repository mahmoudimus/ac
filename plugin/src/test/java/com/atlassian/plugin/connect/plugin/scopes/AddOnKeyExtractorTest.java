package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.jwt.JwtConstants;
import com.atlassian.plugin.connect.api.scopes.AddOnKeyExtractor;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
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

@RunWith (MockitoJUnitRunner.class)
public class AddOnKeyExtractorTest
{
    private static final String ADD_ON_KEY = "my-add-on";

    private AddOnKeyExtractor addOnKeyExtractor;

    @Mock
    private JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    @Mock
    private HttpServletRequest request;

    @Before
    public void setUp() throws Exception
    {
        when(request.getRequestURI()).thenReturn("/confluence/rest/xyz");
        when(request.getContextPath()).thenReturn("/confluence");

        addOnKeyExtractor = new AddOnKeyExtractorImpl(jsonConnectAddOnIdentifierService);
    }

    @Test
    public void testIsPluginRequestForAddOnJwtRequest() throws Exception
    {
        when(jsonConnectAddOnIdentifierService.isConnectAddOn(ADD_ON_KEY)).thenReturn(true);
        when(request.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(ADD_ON_KEY);
        assertTrue(addOnKeyExtractor.isAddOnRequest(request));
    }

    @Test
    public void testIsPluginRequestForClientKey() throws Exception
    {
        when(jsonConnectAddOnIdentifierService.isConnectAddOn(ADD_ON_KEY)).thenReturn(true);
        when(request.getHeader(AddOnKeyExtractor.AP_REQUEST_HEADER)).thenReturn(ADD_ON_KEY);
        assertTrue(addOnKeyExtractor.isAddOnRequest(request));
    }

    @Test
    public void testNoKeyForNotConnectAddon() throws Exception
    {
        when(jsonConnectAddOnIdentifierService.isConnectAddOn(ADD_ON_KEY)).thenReturn(false);
        when(request.getHeader(AddOnKeyExtractor.AP_REQUEST_HEADER)).thenReturn(ADD_ON_KEY);
        assertNull(addOnKeyExtractor.getAddOnKeyFromHttpRequest(request));
    }

    @Test
    public void testIsNotPluginRequestForOtherJwtRequest() throws Exception
    {
        when(jsonConnectAddOnIdentifierService.isConnectAddOn(ADD_ON_KEY)).thenReturn(false);
        when(request.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(ADD_ON_KEY);
        assertFalse(addOnKeyExtractor.isAddOnRequest(request));
    }
}