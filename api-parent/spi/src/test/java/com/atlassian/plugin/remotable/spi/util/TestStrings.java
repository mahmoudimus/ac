package com.atlassian.plugin.remotable.spi.util;

import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static com.atlassian.plugin.remotable.spi.util.Strings.*;
import static junit.framework.Assert.assertEquals;

public class TestStrings
{
    @Test
    public void testDecapitalizeNull()
    {
        assertEquals(null, decapitalize(null));
    }

    @Test
    public void testDecapitalizeEmpty()
    {
        assertEquals("", decapitalize(""));
    }

    @Test
    public void testDecapitalizeLengthOne()
    {
        assertEquals("a", decapitalize("A"));
    }

    @Test
    public void testDecapitalizeOnlyFirstAcronym()
    {
        assertEquals("aBCdefGHI", decapitalize("ABCdefGHI"));
    }

    @Test
    public void testDecapitalizeOnlyFirstCamel()
    {
        assertEquals("abcDefGhi", decapitalize("AbcDefGhi"));
    }

    @Test
    public void testCapitalizeNull()
    {
        assertEquals(null, decapitalize(null));
    }

    @Test
    public void testCapitalizeEmpty()
    {
        assertEquals("", capitalize(""));
    }

    @Test
    public void testCapitalizeLengthOne()
    {
        assertEquals("A", capitalize("a"));
    }

    @Test
    public void testCapitalizeOnlyFirstAcronym()
    {
        assertEquals("AbcDEFghi", capitalize("abcDEFghi"));
    }

    @Test
    public void testCapitalizeOnlyFirstCamel()
    {
        assertEquals("AbcDefGhi", capitalize("abcDefGhi"));
    }

    @Test
    public void testDasherizeNull()
    {
        assertEquals(null, dasherize(null));
    }

    @Test
    public void testDasherizeEmpty()
    {
        assertEquals("", dasherize(""));
    }

    @Test
    public void testDasherizeCamel()
    {
        assertEquals("foo-bar-baz-biz", dasherize("FooBarBazBiz"));
    }

    @Test
    public void testDasherizeTrailingDash()
    {
        assertEquals("foo-bar-", dasherize("fooBar-"));
    }

    @Test
    public void testDasherizeLeadingDash()
    {
        assertEquals("-foo-bar", dasherize("-fooBar"));
    }

    @Test
    public void testCamelizeNull()
    {
        assertEquals(null, camelize(null));
    }

    @Test
    public void testCamelizeEmpty()
    {
        assertEquals("", camelize(""));
    }

    @Test
    public void testCamelizeDashed()
    {
        assertEquals("fooBarBazBiz", camelize("foo-bar-baz-biz"));
    }

    @Test
    public void testCamelizeAcronym()
    {
        assertEquals("aBC", camelize("ABC"));
    }

    @Test
    public void testTitleizeNull()
    {
        assertEquals(null, titleize(null));
    }

    @Test
    public void testTitleizeEmpty()
    {
        assertEquals("", titleize(""));
    }

    @Test
    public void testTitleize()
    {
        assertEquals("Foo Bar Baz Biz", titleize("foo-bar_baz biz"));
    }

    @Test
    public void testRemoveSuffixNull()
    {
        assertEquals(null, removeSuffix(null, "Bar"));
    }

    @Test
    public void testRemoveSuffixEmpty()
    {
        assertEquals("", removeSuffix("", "Bar"));
    }

    @Test
    public void testRemoveSuffixAll()
    {
        assertEquals("", removeSuffix("Bar", "Bar"));
    }

    @Test
    public void testRemoveSuffix()
    {
        assertEquals("Foo", removeSuffix("FooBar", "Bar"));
    }

    @Test
    public void testReplaceNull()
    {
        assertEquals(null, replace(null, ".*", new Replacer()
        {
            @Override
            public String replace(List<String> groups)
            {
                return "fail";
            }
        }));
    }

    @Test
    public void testReplaceEmpty()
    {
        assertEquals("", replace("", ".*", new Replacer()
        {
            @Override
            public String replace(List<String> groups)
            {
                return "fail";
            }
        }));
    }

    @Test
    public void testReplace()
    {
        assertEquals("abcDefGhi", replace("abc_def_ghi", "([\\w\\d])_([\\p{Lower}])", new Replacer()
        {
            @Override
            public String replace(List<String> groups)
            {
                return groups.get(1) + groups.get(2).toUpperCase(Locale.US);
            }
        }));
    }

    @Test
    public void testServletClassNameToDashedName()
    {
        assertEquals("issue-tab", dasherize(removeSuffix("IssueTabServlet", "Servlet")));
    }
}
