package com.atlassian.plugin.connect.plugin.property;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.fugue.Iterables;

import com.atlassian.plugin.connect.api.plugin.property.AddonProperty;
import com.atlassian.plugin.connect.api.plugin.property.AddonPropertyIterable;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import net.java.ao.EntityManager;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.jdbc.NonTransactional;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;

import static com.atlassian.plugin.connect.plugin.property.AddonPropertyStore.MAX_PROPERTIES_PER_ADD_ON;
import static com.atlassian.plugin.connect.plugin.property.AddonPropertyStore.PutResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@RunWith (ActiveObjectsJUnitRunner.class)
@Data (AddonPropertyStoreTest.Data.class)
public class AddonPropertyStoreTest
{
    private static final String ADD_ON_KEY = "addonKey";
    private static final String PROPERTY_KEY = "propertyKey";
    private static final String RAW_VALUE = quote("value");
    private static final JsonNode VALUE = JsonCommon.parseStringToJson(RAW_VALUE).get();

    private EntityManager entityManager;
    private AddonPropertyStore store;

    @Before
    public void setUp() throws Exception
    {
        store = new AddonPropertyStore(new TestActiveObjects(entityManager));
    }

    @Test
    @NonTransactional
    public void testCreateAndGetProperty() throws Exception
    {
        AddonProperty property = new AddonProperty(PROPERTY_KEY, VALUE, 0);
        PutResult putResult = store.setPropertyValue(ADD_ON_KEY, property.getKey(), RAW_VALUE).getResult();
        assertEquals(PutResult.PROPERTY_CREATED, putResult);

        Optional<AddonProperty> propertyValue = store.getPropertyValue(ADD_ON_KEY, PROPERTY_KEY);
        assertThat(propertyValue.get(), isSameProperty(property));
    }

    @Test
    @NonTransactional
    public void testCreatePropertyWithBigValue()
    {
        String bigValue = quote(StringUtils.repeat('.', 65000));
        PutResult putResult = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, bigValue).getResult();
        assertEquals(PutResult.PROPERTY_CREATED, putResult);
    }

    @Test
    @NonTransactional
    public void testCreateAndUpdateProperty() throws Exception
    {
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, RAW_VALUE);
        final String newRawValue = RAW_VALUE + "1";
        AddonProperty property2 = new AddonProperty(PROPERTY_KEY, JsonCommon.parseStringToJson(newRawValue).get(), 0);

        AddonPropertyStore.PutResultWithOptionalProperty putResult = store.setPropertyValue(ADD_ON_KEY, property2.getKey(), newRawValue);
        assertEquals(PutResult.PROPERTY_UPDATED, putResult.getResult());

        Optional<AddonProperty> propertyValue = putResult.getProperty();
        assertThat(propertyValue.get(), isSameProperty(property2));
    }

    @Test
    @NonTransactional
    public void testMaximumPropertiesReached() throws Exception
    {
        for (int i = 0; i < MAX_PROPERTIES_PER_ADD_ON; i++)
        {
            AddonPropertyStore.PutResultWithOptionalProperty storeResultWithOptionalProperty = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY + String.valueOf(i), RAW_VALUE);
            assertEquals(PutResult.PROPERTY_CREATED, storeResultWithOptionalProperty.getResult());
            assertEquals(PutResult.PROPERTY_CREATED, storeResultWithOptionalProperty.getResult());
        }
        PutResult last = store.setPropertyValue(ADD_ON_KEY, "last", RAW_VALUE).getResult();
        assertEquals(PutResult.PROPERTY_LIMIT_EXCEEDED, last);
    }

    @Test
    @NonTransactional
    public void testDeleteExistingProperty() throws Exception
    {
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, RAW_VALUE);
        store.deletePropertyValue(ADD_ON_KEY, PROPERTY_KEY);
        Optional<AddonProperty> propertyValue = store.getPropertyValue(ADD_ON_KEY, PROPERTY_KEY);
        assertFalse(propertyValue.isPresent());
    }
    
    @Test
    @NonTransactional
    public void testNonEmptyListProperties() throws Exception
    {
        List<AddonProperty> propertyList = Arrays.asList(
                  new AddonProperty("1", VALUE, 0),
                  new AddonProperty("2", VALUE, 1),
                  new AddonProperty("3", VALUE, 2)
        );
        testListProperties(propertyList);
    }

    @Test
    @NonTransactional
    public void testEmptyListProperties() throws Exception
    {
        testListProperties(Collections.<AddonProperty>emptyList());
    }

    @Test
    @NonTransactional
    public void testExecuteSetInTransaction() throws Exception
    {
        store.executeInTransaction(new AddonPropertyStore.TransactionAction<Void>()
        {
            @Override
            public Void call()
            {
                store.setPropertyValue("a", "a", quote("a"));
                return null;
            }
        });
    }

    private void testListProperties(final List<AddonProperty> propertyList)
    {
        for (AddonProperty property : propertyList)
        {
            store.setPropertyValue(ADD_ON_KEY, property.getKey(), RAW_VALUE);
        }

        AddonPropertyIterable result = store.getAllPropertiesForAddonKey(ADD_ON_KEY);

        if (Iterables.isEmpty().apply(result) && Iterables.isEmpty().apply(propertyList)) return;

        List<Matcher<? super AddonProperty>> matchers = Lists.transform(propertyList, new Function<AddonProperty, Matcher<? super AddonProperty>>()
        {
            @Override
            public Matcher<AddonProperty> apply(final AddonProperty input)
            {
                return isSameProperty(input);
            }
        });
        assertThat(result, Matchers.contains(matchers));
    }

    public static final class Data implements DatabaseUpdater
    {
        @Override
        public void update(final EntityManager entityManager) throws Exception
        {
            entityManager.migrate(AddonPropertyAO.class);
        }
    }

    private TypeSafeMatcher<AddonProperty> isSameProperty(final AddonProperty property)
    {
        return new TypeSafeMatcher<AddonProperty>()
        {
            @Override
            protected boolean matchesSafely(final AddonProperty item)
            {
                return new EqualsBuilder()
                        .append(property.getKey(), item.getKey())
                        .append(property.getValue(), item.getValue())
                        .isEquals();
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("[key=" + property.getKey() + ",value=" + property.getValue() + "]");
            }
        };
    }

    private static String quote(String unquoted)
    {
        return '"' + unquoted + '"';
    }
}
