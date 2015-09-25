package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.atlassian.plugin.connect.api.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.service.IsDevModeServiceImpl;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

public class TestUrlVariableSubstitutor
{
    @Test
    public void testSubstitutionInSimpleCase()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("my_page_id={page.id}", CONTEXT), is("my_page_id=1234"));
    }

    @Test
    public void testSubstitutionWhenValueIsUsedMultipleTimes()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("my_page_id={page.id}&other_page_id={page.id}", CONTEXT), is("my_page_id=1234&other_page_id=1234"));
    }

    @Test
    public void testSubstitutionWhenReferencedValueIsNotInContext()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("thing={stuff}", CONTEXT), is("thing="));
    }

    @Test
    public void testSubstitutionWhenParameterNameIsUsedMultipleTimes()
    {
        // this is a silly URL but UrlVariableSubstitutor should still do as asked
        MatcherAssert.assertThat(SUBSTITUTOR.replace("my_page_id={page.id}&my_page_id={page.id}", CONTEXT), is("my_page_id=1234&my_page_id=1234"));
    }

    @Test
    public void testSubstitutionWhenContextValueIsNull()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("thing={uh_oh}", CONTEXT), is("thing="));
    }

    @Test
    public void testSubstitutionWhenContextValueContainsSpaceAndUnicode()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("thing={oh_my_encoding}", CONTEXT), is("thing=%C3%86%20%C3%A6"));
    }

    @Test
    public void testSubstitutionWhenContextValueContainsPlus()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("thing={life_meaning}", CONTEXT), is("thing=21%2B21"));
    }

    @Test
    public void testSubstitutionWhenTheContextValueIsAlsoTheParameterName()
    {
        // this is a silly URL but UrlVariableSubstitutor should still do as asked
        MatcherAssert.assertThat(SUBSTITUTOR.replace("{page.id}={page.id}", CONTEXT), is("1234=1234"));
    }

    @Test
    public void testSubstitutionInBigCombinedCase()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("http://server:3000/some/path?p={page.id}&p2={page.id}&p2={uh_oh}&does_not_exist={herpderp}", CONTEXT), is("http://server:3000/some/path?p=1234&p2=1234&p2=&does_not_exist="));
    }

    @Test
    public void testGetContextVariableMap()
    {
        Map<String, String> expected = new HashMap<String, String>(2);
        expected.put("my_page_id", "{page.id}");
        expected.put("thing", "{stuff}");
        MatcherAssert.assertThat(SUBSTITUTOR.getContextVariableMap("http://server:80/path?my_page_id={page.id}&thing={stuff}"), is(expected));
    }

    private static final UrlVariableSubstitutor SUBSTITUTOR = new UrlVariableSubstitutorImpl(new IsDevModeServiceImpl());
    private static final Map<String, Object> CONTEXT = createContext();

    private static Map<String, Object> createContext()
    {
        Map<String, Object> pageContext = new HashMap<String, Object>();
        pageContext.put("id", 1234);
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("page", Collections.singletonMap("id", 1234));
        context.put("foo", "bah");
        context.put("uh_oh", null);
        context.put("oh_my_encoding", "Æ æ");
        context.put("life_meaning", "21+21");
        return context;
    }
}
