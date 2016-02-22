package com.atlassian.plugin.connect.test;

import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AddonTestUtilsTest {
    @Test
    public void testEscapeJQuerySelector() throws Exception {
        String input = "!\"#$%&'()*+,./:;<=>?@[\\]^`{|}~";
        String expected = "\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\.\\/\\:\\;\\<\\=\\>\\?\\@\\[\\\\\\]\\^\\`\\{\\|\\}\\~";
        assertEquals("All reserved characters are escaped", expected, AddonTestUtils.escapeJQuerySelector(input));
    }
}
