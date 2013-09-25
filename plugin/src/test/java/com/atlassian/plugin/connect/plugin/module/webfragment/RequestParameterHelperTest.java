package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RequestParameterHelperTest
{
    @Test
    public void shouldConvertToNestedForm()
    {
        final Map<String, Object> requestParams = ImmutableMap.<String, Object>of(
                "project.id", new String[] {"10"},
                "project.key", new String[] {"myKey"},
                "other.blah", new String[] {"stuff"});

        final Map<String, Object> paramsInNestedForm = new RequestParameterHelper(requestParams).getParamsInNestedForm();
        assertThat(paramsInNestedForm.entrySet(), hasSize(2));

        final Object projectMapObj = paramsInNestedForm.get("project");
        assertThat(projectMapObj, is(notNullValue()));
        assertThat(projectMapObj, is(instanceOf(Map.class)));
        Map<String, Object> projectMap = (Map<String, Object>) projectMapObj;
        assertThat(projectMap.get("id"), is(equalTo((Object)10)));
        assertThat(projectMap.get("key"), is(equalTo((Object)"myKey")));


        final Object otherMapObj = paramsInNestedForm.get("other");
        assertThat(otherMapObj, is(notNullValue()));
        assertThat(otherMapObj, is(instanceOf(Map.class)));
        Map<String, Object> otherMap = (Map<String, Object>) otherMapObj;
        assertThat(otherMap.get("blah"), is(equalTo((Object)"stuff")));
    }
}
