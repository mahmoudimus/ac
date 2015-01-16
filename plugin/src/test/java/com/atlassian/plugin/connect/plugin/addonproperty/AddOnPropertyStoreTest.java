package com.atlassian.plugin.connect.plugin.addonproperty;

import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyAO;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyIterable;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import com.atlassian.plugin.connect.plugin.rest.data.ETag;
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
import java.util.concurrent.Callable;

import static com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore.DeleteResult;
import static com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore.MAX_PROPERTIES_PER_ADD_ON;
import static com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore.PutResult;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
    public void testCreateAndGetProperty() throws Exception
    {
        AddOnProperty property = new AddOnProperty(PROPERTY_KEY, VALUE);
        PutResult putResult = store.setPropertyValue(ADD_ON_KEY, property.getKey(), property.getValue(), noETag());
        assertEquals(PutResult.PROPERTY_CREATED, putResult);

        Option<AddOnProperty> propertyValue = store.getPropertyValue(ADD_ON_KEY, PROPERTY_KEY);
        assertThat(propertyValue.get(), is(property));
    }

    @Test
    @NonTransactional
    public void testCreatePropertyWithBigValue()
    {
        String bigValue = StringUtils.repeat('.' , 65000);
        PutResult putResult = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, bigValue, noETag());
        assertEquals(PutResult.PROPERTY_CREATED, putResult);
    }
    
    @Test
    @NonTransactional
    public void testCreateAndUpdateProperty() throws Exception
    {
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, noETag());
        AddOnProperty property2 = new AddOnProperty(PROPERTY_KEY, VALUE + "1");
        PutResult putResult = store.setPropertyValue(ADD_ON_KEY, property2.getKey(), property2.getValue(), noETag());
        assertEquals(PutResult.PROPERTY_UPDATED, putResult);

        Option<AddOnProperty> propertyValue = store.getPropertyValue(ADD_ON_KEY, PROPERTY_KEY);
        assertThat(propertyValue.get(), is(property2));
    }

    @Test
    @NonTransactional
    public void testCreateAndUpdatePropertyWithDifferentETag() throws Exception
    {
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, noETag());

        PutResult putResult = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE + "1", Option.some(new ETag("asd")));
        assertEquals(PutResult.PROPERTY_MODIFIED, putResult);
    }

    @Test
    @NonTransactional
    public void testMaximumPropertiesReached() throws Exception
    {
        for (int i = 0; i < MAX_PROPERTIES_PER_ADD_ON; i++)
        {
            PutResult putResult = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY + String.valueOf(i), VALUE, noETag());
            assertEquals(PutResult.PROPERTY_CREATED, putResult);
        }
        PutResult last = store.setPropertyValue(ADD_ON_KEY, "last", VALUE, noETag());
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
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE, noETag());
        DeleteResult deleteResult = store.deletePropertyValue(ADD_ON_KEY, PROPERTY_KEY);
        assertEquals(DeleteResult.PROPERTY_DELETED, deleteResult);
    }
    
    private void testListProperties(final List<AddOnProperty> propertyList)
    {
        for (AddOnProperty property : propertyList)
        {
            store.setPropertyValue(ADD_ON_KEY, property.getKey(), property.getValue(), noETag());
        }

        AddOnPropertyIterable result = store.getAllPropertiesForAddOnKey(ADD_ON_KEY);

        if (Iterables.isEmpty().apply(result) && Iterables.isEmpty().apply(propertyList)) return;

        assertThat(result,
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
    public void testExecuteDeleteInTransaction() throws Exception
    {
        store.executeInTransaction(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                store.deletePropertyValue("", "");
                return null;
            }
        });
    }

    public static final class Data implements DatabaseUpdater
    {
        @Override
        public void update(final EntityManager entityManager) throws Exception
        {
            entityManager.migrate(AddOnPropertyAO.class);
        }
    }

    private static Option<ETag> noETag()
    {
        return Option.none();
    }
}