package com.atlassian.plugin.connect.plugin.web.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.connect.api.web.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.util.IsDevModeServiceImpl;
import com.atlassian.plugin.connect.spi.web.context.DynamicUriVariableResolver;
import com.google.common.collect.ImmutableList;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestUrlVariableSubstitutor
{
    private static final UrlVariableSubstitutor SUBSTITUTOR = new UrlVariableSubstitutorImpl(new IsDevModeServiceImpl(), Collections.<DynamicUriVariableResolver>emptyList());
    private static final Map<String, Object> CONTEXT = createContext();

    @Test
    public void testSubstitutionInSimpleCase()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("", "my_page_id={page.id}", CONTEXT), is("my_page_id=1234"));
    }

    @Test
    public void testSubstitutionWhenValueIsUsedMultipleTimes()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("", "my_page_id={page.id}&other_page_id={page.id}", CONTEXT), is("my_page_id=1234&other_page_id=1234"));
    }

    @Test
    public void testSubstitutionWhenReferencedValueIsNotInContext()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("", "thing={stuff}", CONTEXT), is("thing="));
    }

    @Test
    public void testSubstitutionWhenParameterNameIsUsedMultipleTimes()
    {
        // this is a silly URL but UrlVariableSubstitutor should still do as asked
        MatcherAssert.assertThat(SUBSTITUTOR.replace("", "my_page_id={page.id}&my_page_id={page.id}", CONTEXT), is("my_page_id=1234&my_page_id=1234"));
    }

    @Test
    public void testSubstitutionWhenContextValueIsNull()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("", "thing={uh_oh}", CONTEXT), is("thing="));
    }

    @Test
    public void testSubstitutionWhenContextValueContainsSpaceAndUnicode()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("", "thing={oh_my_encoding}", CONTEXT), is("thing=%C3%86%20%C3%A6"));
    }

    @Test
    public void testSubstitutionWhenContextValueContainsPlus()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("", "thing={life_meaning}", CONTEXT), is("thing=21%2B21"));
    }

    @Test
    public void testSubstitutionWhenTheContextValueIsAlsoTheParameterName()
    {
        // this is a silly URL but UrlVariableSubstitutor should still do as asked
        MatcherAssert.assertThat(SUBSTITUTOR.replace("", "{page.id}={page.id}", CONTEXT), is("1234=1234"));
    }

    @Test
    public void testSubstitutionInBigCombinedCase()
    {
        MatcherAssert.assertThat(SUBSTITUTOR.replace("", "http://server:3000/some/path?p={page.id}&p2={page.id}&p2={uh_oh}&does_not_exist={herpderp}", CONTEXT), is("http://server:3000/some/path?p=1234&p2=1234&p2=&does_not_exist="));
    }

    @Test
    public void testGetContextVariableMap()
    {
        Map<String, String> expected = new HashMap<String, String>(2);
        expected.put("my_page_id", "{page.id}");
        expected.put("thing", "{stuff}");
        MatcherAssert.assertThat(SUBSTITUTOR.getContextVariableMap("http://server:80/path?my_page_id={page.id}&thing={stuff}"), is(expected));
    }

    @Test
    public void testDynamicSubstitution()
    {
        DynamicUriVariableResolver resolver1 = (addOnKey, variable, context) ->
                variable.contains("addOn.key") ?
                        Optional.of(variable.replace("addOn.key", addOnKey)) :
                        Optional.<String>empty();

        DynamicUriVariableResolver resolver2 = (addOnKey, variable, context) ->
                variable.contains("context.size") ?
                        Optional.of(variable.replace("context.size", context.size() + "")) :
                        Optional.<String>empty();

        UrlVariableSubstitutor substitutor = new UrlVariableSubstitutorImpl(new IsDevModeServiceImpl(), ImmutableList.of(resolver1, resolver2));

        assertThat(substitutor.replace("myKey", "http://server:3000/some/path?page_id={page.id}&nonExistingVar={aVar}&key={addOn.key}&contextSize={context.size}", createContext()),
                equalTo("http://server:3000/some/path?page_id=1234&nonExistingVar=&key=myKey&contextSize=5"));
    }

    private static Map<String, Object> createContext()
    {
        Map<String, Object> context = new HashMap<>();
        context.put("page", Collections.singletonMap("id", 1234));
        context.put("foo", "bah");
        context.put("uh_oh", null);
        context.put("oh_my_encoding", "Æ æ");
        context.put("life_meaning", "21+21");
        return context;
    }
}
