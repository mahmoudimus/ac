package com.atlassian.plugin.connect.plugin.addonproperty;

import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.fugue.Either;
import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyAO;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyIterable;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import com.atlassian.plugin.connect.plugin.rest.data.ETag;
import com.google.common.base.Predicate;
import net.java.ao.EntityManager;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.jdbc.NonTransactional;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore.DeleteResult;
import static com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore.MAX_PROPERTIES_PER_ADD_ON;
import static com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore.PutResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith (ActiveObjectsJUnitRunner.class)
@Data (AddOnPropertyStoreTest.Data.class)
public class AddOnPropertyStoreTest
{
    private static final String ADD_ON_KEY = "addOnKey";
    private static final String PROPERTY_KEY = "propertyKey";
    private static final String VALUE = "value";

    private EntityManager entityManager;
    private AddOnPropertyStore store;

    @Before
    public void setUp() throws Exception
    {
        store = new AddOnPropertyStore(new TestActiveObjects(entityManager));
    }

    @Test
    @NonTransactional
    public void testCreateAndGetPropertyWithNoETag() throws Exception
    {
        PutResult putResult = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, ETag.emptyETag());
        assertEquals(PutResult.PROPERTY_CREATED, putResult);

        Either<AddOnPropertyStore.GetResult, AddOnProperty> propertyValue = store.getPropertyValue(ADD_ON_KEY, PROPERTY_KEY, ETag.emptyETag());
        assertTrue(propertyValue.right().toOption().exists(new Predicate<AddOnProperty>()
        {
            @Override
            public boolean apply(final AddOnProperty input)
            {
                return input.getKey().equals(PROPERTY_KEY) && input.getValue().equals(VALUE);
            }
        }));
    }

    @Test
    @NonTransactional
    public void testCreatePropertyWithBigValue()
    {
        String bigValue = StringUtils.repeat('.' , 65000);
        PutResult putResult = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, bigValue, ETag.emptyETag());
        assertEquals(PutResult.PROPERTY_CREATED, putResult);
    }
    
    @Test
    @NonTransactional
    public void testCreateAndUpdateProperty() throws Exception
    {
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, ETag.emptyETag());
        PutResult putResult = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE + "1", ETag.emptyETag());
        assertEquals(PutResult.PROPERTY_UPDATED, putResult);
    }

    @Test
    @NonTransactional
    public void testCreateAndUpdatePropertyWithSameETag() throws Exception
    {
        AddOnProperty property = new AddOnProperty(PROPERTY_KEY, VALUE);
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, ETag.emptyETag());
        PutResult putResult = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE + "1", property.getETag());
        assertEquals(PutResult.PROPERTY_UPDATED, putResult);

        Either<AddOnPropertyStore.GetResult, AddOnProperty> propertyValue = store.getPropertyValue(ADD_ON_KEY, PROPERTY_KEY, ETag.emptyETag());
        assertTrue(propertyValue.right().toOption().exists(new Predicate<AddOnProperty>()
        {
            @Override
            public boolean apply(final AddOnProperty input)
            {
                return input.getKey().equals(PROPERTY_KEY) && input.getValue().equals(VALUE + "1");
            }
        }));
    }

    @Test
    @NonTransactional
    public void testCreateAndUpdatePropertyWithDifferentETag() throws Exception
    {
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, ETag.emptyETag());

        PutResult putResult = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE + "1", new ETag("asd"));
        assertEquals(PutResult.PROPERTY_MODIFIED, putResult);
    }

    @Test
    @NonTransactional
    public void testMaximumPropertiesReached() throws Exception
    {
        for (int i = 0; i < MAX_PROPERTIES_PER_ADD_ON; i++)
        {
            PutResult putResult = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY + String.valueOf(i), VALUE, ETag.emptyETag());
            assertEquals(PutResult.PROPERTY_CREATED, putResult);
        }
        PutResult last = store.setPropertyValue(ADD_ON_KEY, "last", VALUE, ETag.emptyETag());
        assertEquals(PutResult.PROPERTY_LIMIT_EXCEEDED, last);
    }

    @Test
    @NonTransactional
    public void testDeleteNonExistentProperty() throws Exception
    {
        DeleteResult deleteResult = store.deletePropertyValue(ADD_ON_KEY, PROPERTY_KEY);
        assertEquals(DeleteResult.PROPERTY_NOT_FOUND, deleteResult);
    }

    @Test
    @NonTransactional
    public void testDeleteExistingProperty() throws Exception
    {
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, ETag.emptyETag());
        DeleteResult deleteResult = store.deletePropertyValue(ADD_ON_KEY, PROPERTY_KEY);
        assertEquals(DeleteResult.PROPERTY_DELETED, deleteResult);
    }
    
    private void testListProperties(final List<AddOnProperty> propertyList)
    {
        for (AddOnProperty property : propertyList)
        {
            store.setPropertyValue(ADD_ON_KEY, property.getKey(), property.getValue(), ETag.emptyETag());
        }

        Either<AddOnPropertyStore.ListResult, AddOnPropertyIterable> result = store.getAllPropertiesForAddOnKey(ADD_ON_KEY, ETag.emptyETag());

        if (Iterables.isEmpty().apply(result.right().get()) && Iterables.isEmpty().apply(propertyList)) return;

        assertThat(result.right().get(),
                IsIterableContainingInOrder.contains(propertyList.toArray()));
    }

    @Test
    @NonTransactional
    public void testNonEmptyListProperties() throws Exception
    {
        List<AddOnProperty> propertyList = Arrays.asList(
                  new AddOnProperty("1", VALUE),
                  new AddOnProperty("2", VALUE),
                  new AddOnProperty("3", VALUE)
        );
        testListProperties(propertyList);
    }

    @Test
    @NonTransactional
    public void testEmptyListProperties() throws Exception
    {
        testListProperties(Collections.<AddOnProperty>emptyList());
    }

    @Test
    @NonTransactional
    public void testCreateAndGetPropertyWithSameETag() throws Exception
    {
        AddOnProperty property = new AddOnProperty(PROPERTY_KEY, VALUE);
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, ETag.emptyETag());

        Either<AddOnPropertyStore.GetResult, AddOnProperty> propertyValue = store.getPropertyValue(ADD_ON_KEY, PROPERTY_KEY, property.getETag());
        assertTrue(propertyValue.left().toOption().exists(new Predicate<AddOnPropertyStore.GetResult>()
        {
            @Override
            public boolean apply(final AddOnPropertyStore.GetResult input)
            {
                return input.equals(AddOnPropertyStore.GetResult.PROPERTY_NOT_MODIFIED);
            }
        }));
    }

    @Test
    @NonTransactional
    public void testListPropertiesWithNoETag() throws Exception
    {
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, ETag.emptyETag());
        Iterable<String> keys = store.getAllPropertiesForAddOnKey(ADD_ON_KEY, ETag.emptyETag()).right().get().getPropertyKeys();
        Option<String> first = Iterables.first(keys);
        assertTrue(first.isDefined());
        assertEquals(PROPERTY_KEY, first.get());
    }

    @Test
    @NonTransactional
    public void testListPropertiesWithSameETag() throws Exception
    {
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, ETag.emptyETag());
        AddOnPropertyIterable addOnProperties = store.getAllPropertiesForAddOnKey(ADD_ON_KEY, ETag.emptyETag()).right().get();

        AddOnPropertyStore.ListResult listResult = store.getAllPropertiesForAddOnKey(ADD_ON_KEY, addOnProperties.getETag()).left().get();
        assertEquals(AddOnPropertyStore.ListResult.PROPERTIES_NOT_MODIFIED, listResult);
    }

    @Test
    @NonTransactional
    public void testListPropertiesWithDifferentETag() throws Exception
    {
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, ETag.emptyETag());
        Iterable<String> keys = store.getAllPropertiesForAddOnKey(ADD_ON_KEY, new ETag("1")).right().get().getPropertyKeys();
        Option<String> first = Iterables.first(keys);
        assertTrue(first.isDefined());
        assertEquals(PROPERTY_KEY, first.get());
    }

    @Test
    @NonTransactional
    public void testCreateAndGetPropertyWithDifferentETag() throws Exception
    {
        AddOnProperty property = new AddOnProperty(PROPERTY_KEY, VALUE + "1");
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, ETag.emptyETag());
        Either<AddOnPropertyStore.GetResult, AddOnProperty> propertyValue = store.getPropertyValue(ADD_ON_KEY, PROPERTY_KEY, property.getETag());
        assertTrue(propertyValue.right().toOption().exists(new Predicate<AddOnProperty>()
        {
            @Override
            public boolean apply(final AddOnProperty input)
            {
                return input.getKey().equals(PROPERTY_KEY) && input.getValue().equals(VALUE);
            }
        }));
    }

    public static final class Data implements DatabaseUpdater
    {
        @Override
        public void update(final EntityManager entityManager) throws Exception
        {
            entityManager.migrate(AddOnPropertyAO.class);
        }
    }
}