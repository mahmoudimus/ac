package com.atlassian.plugin.connect.plugin.addonproperty;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyAO;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyService;
import net.java.ao.Query;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddOnPropertyServiceImplTest
{
    @Mock
    private ActiveObjects ao;

    private final String addOnKey = "testAddon";
    private final AddOnProperty property = new AddOnProperty("testProperty", "testValue");

    private AddOnPropertyService service;

    @Before
    public void init()
    {
        //service = new AddOnPropertyServiceImpl(ao);
    }

    @Ignore
    @Test
    public void testGetExistingProperty() throws Exception
    {
        AddOnPropertyAO propertyAO = mockAddOnPropertyAO("key", "value");

        when(ao.find(Matchers.eq(AddOnPropertyAO.class), Matchers.argThat(getQueryMatcher("propKey"))))
                .thenReturn(new AddOnPropertyAO[] { propertyAO });

        AddOnProperty expectedValue = fromAO(propertyAO);
        //AddOnProperty propertyValue1 = service.getPropertyValue("", "propKey");

        //assertEquals(expectedValue, propertyValue1);
    }

    private AddOnProperty fromAO(final AddOnPropertyAO propertyAO)
    {
        return new AddOnProperty(propertyAO.getPropertyKey(), propertyAO.getValue());
    }

    private AddOnPropertyAO mockAddOnPropertyAO(final String key, final String value)
    {
        AddOnPropertyAO propertyAO = mock(AddOnPropertyAO.class);
        when(propertyAO.getPropertyKey()).thenReturn(key);
        when(propertyAO.getValue()).thenReturn(value);
        return propertyAO;
    }

    private TypeSafeMatcher<Query> getQueryMatcher(final String whereParam)
    {
        return new TypeSafeMatcher<Query>() {

            @Override
            protected boolean matchesSafely(final Query query)
            {
                return query.getWhereClause().equals("KEY = ?")
                        && Arrays.equals(query.getWhereParams(), new String[] { whereParam });
            }

            @Override
            public void describeTo(final Description description)
            {
                throw new UnsupportedOperationException("Not implemented");
            }
        };
    }

    @Test
    public void testGetPropertyValue() throws Exception
    {

    }

    @Test
    public void testSetPropertyValue() throws Exception
    {

    }
}