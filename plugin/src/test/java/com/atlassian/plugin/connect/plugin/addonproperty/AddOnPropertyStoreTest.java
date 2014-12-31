package com.atlassian.plugin.connect.plugin.addonproperty;

import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyAO;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import com.google.common.base.Predicate;
import net.java.ao.EntityManager;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.jdbc.NonTransactional;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore.DeleteResult;
import static com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore.MAX_PROPERTIES_PER_ADD_ON;
import static com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore.PutResult;
import static org.junit.Assert.assertEquals;
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
    public void testCreateAndGetProperty() throws Exception
    {
        PutResult putResult = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE);
        assertEquals(PutResult.PROPERTY_CREATED, putResult);

        Option<AddOnProperty> propertyValue = store.getPropertyValue(ADD_ON_KEY, PROPERTY_KEY);
        assertTrue(propertyValue.exists(new Predicate<AddOnProperty>()
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
    public void testCreateAndUpdateProperty() throws Exception
    {
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE);
        Option<AddOnProperty> propertyValue = store.getPropertyValue(ADD_ON_KEY, PROPERTY_KEY);
        assertTrue(propertyValue.exists(new Predicate<AddOnProperty>()
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
    public void testMaximumPropertiesReached() throws Exception
    {
        for (int i = 0; i < MAX_PROPERTIES_PER_ADD_ON; i++)
        {
            PutResult putResult = store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY + String.valueOf(i), VALUE);
            assertEquals(PutResult.PROPERTY_CREATED, putResult);
        }
        PutResult last = store.setPropertyValue(ADD_ON_KEY, "last", VALUE);
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
        store.setPropertyValue(ADD_ON_KEY, PROPERTY_KEY, VALUE);
        DeleteResult deleteResult = store.deletePropertyValue(ADD_ON_KEY, PROPERTY_KEY);
        assertEquals(DeleteResult.PROPERTY_DELETED, deleteResult);
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