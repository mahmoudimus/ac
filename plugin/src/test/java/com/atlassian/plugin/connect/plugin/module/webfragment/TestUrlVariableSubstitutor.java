package com.atlassian.plugin.connect.plugin.module.webfragment;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestUrlVariableSubstitutor
{
    @Test
    public void testSubstitution()
    {
        Map<String, Object> pageContext = new HashMap<String, Object>();
        pageContext.put("id", 1234);
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("page", Collections.singletonMap("id", 1234));
        context.put("foo", "bah");
        assertThat(new UrlVariableSubstitutor().replace("my_page_id=${page.id}&thing=${stuff}", context), is("my_page_id=1234&thing="));
    }
}
