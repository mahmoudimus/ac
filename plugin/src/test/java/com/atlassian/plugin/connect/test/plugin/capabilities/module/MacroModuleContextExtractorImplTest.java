package com.atlassian.plugin.connect.test.plugin.capabilities.module;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.ContentEntityForTests;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserProfile;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit Test for {@link com.atlassian.plugin.connect.plugin.capabilities.module.macro.MacroModuleContextExtractor}
 */
public class MacroModuleContextExtractorImplTest
{

    public static final String PAGE_TYPE = "page";
    public static final String PAGE_ID = "56789";
    public static final String PAGE_TITLE = "Test Page";
    public static final String PAGE_VERSION = "8";
    public static final String SPACE_KEY = "space";
    public static final String SPACE_ID = "893646";
    public static final String USER_ID = "admin";
    public static final String USER_KEY = "f80808143087d180143087d3a910004";
    public static final String OUTPUT_TYPE = "display";
    private ConversionContext conversionContext;
    private UserProfile userProfile;

    @Before
    public void beforeEachTest()
    {
        conversionContext = mock(ConversionContext.class);

        ContentEntityForTests contentEntity = new ContentEntityForTests(PAGE_TYPE, PAGE_ID, PAGE_TITLE, PAGE_VERSION, SPACE_KEY, SPACE_ID);
        userProfile = mock(UserProfile.class);
        UserKey userKey = new UserKey(USER_KEY);

        when(conversionContext.getEntity()).thenReturn(contentEntity);
        when(conversionContext.getOutputType()).thenReturn(OUTPUT_TYPE);

        when(userProfile.getUsername()).thenReturn(USER_ID);
        when(userProfile.getUserKey()).thenReturn(userKey);
    }

    @Test
    @Ignore
    public void testMacroBodyNotTruncated() {
        // TODO
//        String shortBody="short";
//        MacroContext macroContext = new MacroContext(conversionContext, shortBody, userProfile);
//        assertEquals(shortBody, String.valueOf(macroContext.getParameters().get("macro.body")));
//        assertEquals(DigestUtils.md5Hex("short"),macroContext.getParameters().get("macro.hash"));
//        assertEquals(false,Boolean.valueOf(String.valueOf(macroContext.getParameters().get("macro.truncated"))));
    }

    @Test
    @Ignore
    public void testMacroBodyTruncated() {
        // TODO

//        String longBody = StringUtils.repeat("abc",128); // a long enough body to trigger truncation
//        MacroContext macroContext = new MacroContext(conversionContext, longBody, userProfile);
//        String bodyParameter = String.valueOf(macroContext.getParameters().get("macro.body"));
//        assertTrue(bodyParameter.length() == 128);
//        assertEquals(longBody.substring(0,128), bodyParameter);
//        assertEquals(DigestUtils.md5Hex(longBody),macroContext.getParameters().get("macro.hash"));
//        assertEquals(true,Boolean.valueOf(String.valueOf(macroContext.getParameters().get("macro.truncated"))));
    }

}
