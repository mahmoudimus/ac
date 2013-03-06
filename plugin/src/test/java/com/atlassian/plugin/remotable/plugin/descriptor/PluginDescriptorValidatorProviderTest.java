package com.atlassian.plugin.remotable.plugin.descriptor;

import com.atlassian.plugin.RequirePermission;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.spi.permission.PermissionsReader;
import com.atlassian.plugin.remotable.spi.product.ProductAccessor;
import com.atlassian.plugin.schema.descriptor.DescribedModuleDescriptorFactory;
import com.atlassian.plugin.schema.spi.Schema;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PluginDescriptorValidatorProviderTest
{
    private PluginDescriptorValidatorProvider pluginDescriptorValidatorProvider;

    @Mock
    private PluginRetrievalService pluginRetrievalService;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private PermissionsReader permissionReader;

    @Mock
    private DescribedModuleDescriptorFactoryAccessor describedModuleDescriptorFactoryAccessor;

    @Mock
    private ProductAccessor productAccessor;

    @Before
    public void setUp()
    {
        pluginDescriptorValidatorProvider = new PluginDescriptorValidatorProvider(pluginRetrievalService, applicationProperties, describedModuleDescriptorFactoryAccessor, permissionReader, productAccessor);
    }

    @Test
    public void testGetModuleSchemasWithNoDescribedModuleFactory()
    {
        when(describedModuleDescriptorFactoryAccessor.getDescribedModuleDescriptorFactories()).thenReturn(ImmutableList.<DescribedModuleDescriptorFactory>of());
        Iterable<Schema> moduleSchemas = pluginDescriptorValidatorProvider.getModuleSchemas(InstallationMode.LOCAL);
        assertTrue(Iterables.isEmpty(moduleSchemas));
    }

    @Test
    public void testGetModuleSchemasWithDescribedModuleFactoryButNoSchema()
    {
        final DescribedModuleDescriptorFactory describedModuleDescriptorFactory = mock(DescribedModuleDescriptorFactory.class);
        when(describedModuleDescriptorFactoryAccessor.getDescribedModuleDescriptorFactories()).thenReturn(ImmutableList.of(describedModuleDescriptorFactory));
        when(describedModuleDescriptorFactory.getModuleDescriptorKeys()).thenReturn(ImmutableList.<String>of());
        Iterable<Schema> moduleSchemas = pluginDescriptorValidatorProvider.getModuleSchemas(InstallationMode.LOCAL);
        assertTrue(Iterables.isEmpty(moduleSchemas));
    }

    @Test
    public void testGetModuleSchemasWithDescribedModuleFactoryButNullSchema()
    {
        final DescribedModuleDescriptorFactory factory = mock(DescribedModuleDescriptorFactory.class);
        when(describedModuleDescriptorFactoryAccessor.getDescribedModuleDescriptorFactories()).thenReturn(ImmutableList.of(factory));
        when(factory.getModuleDescriptorKeys()).thenReturn(ImmutableList.<String>of("key"));

        mockModuleDescriptorWithRequiredPermission(factory, "key", null, ModuleDescriptorWithNoPermission.class);

        Iterable<Schema> moduleSchemas = pluginDescriptorValidatorProvider.getModuleSchemas(InstallationMode.LOCAL);
        assertTrue(Iterables.isEmpty(moduleSchemas));
    }

    @Test
    public void testGetModuleSchemasWithDescribedModuleFactoryAndNonNullSchema()
    {
        final DescribedModuleDescriptorFactory factory = mock(DescribedModuleDescriptorFactory.class);
        when(describedModuleDescriptorFactoryAccessor.getDescribedModuleDescriptorFactories()).thenReturn(ImmutableList.of(factory));
        when(factory.getModuleDescriptorKeys()).thenReturn(ImmutableList.<String>of("key"));
        final Schema schema = mock(Schema.class);

        mockModuleDescriptorWithRequiredPermission(factory, "key", schema, ModuleDescriptorWithNoPermission.class);

        Iterable<Schema> moduleSchemas = pluginDescriptorValidatorProvider.getModuleSchemas(InstallationMode.LOCAL);

        assertEquals(1, Iterables.size(moduleSchemas));
        assertEquals(schema, Iterables.get(moduleSchemas, 0));
    }

    @Test
    public void testGetModuleSchemasWithNonMatchingPermissions()
    {
        final DescribedModuleDescriptorFactory factory = mock(DescribedModuleDescriptorFactory.class);
        when(describedModuleDescriptorFactoryAccessor.getDescribedModuleDescriptorFactories()).thenReturn(ImmutableList.of(factory));
        when(factory.getModuleDescriptorKeys()).thenReturn(ImmutableList.<String>of("key"));
        final Schema schema = mock(Schema.class);

        mockModuleDescriptorWithRequiredPermission(factory, "key", schema, ModuleDescriptorWithPermission.class);

        when(productAccessor.getAllowedPermissions(InstallationMode.LOCAL)).thenReturn(ImmutableSet.<String>of("non_matching_permission"));

        Iterable<Schema> moduleSchemas = pluginDescriptorValidatorProvider.getModuleSchemas(InstallationMode.LOCAL);

        assertTrue(Iterables.isEmpty(moduleSchemas));
    }

    @Test
    public void testGetModuleSchemasWithAllPermissions()
    {
        final DescribedModuleDescriptorFactory factory = mock(DescribedModuleDescriptorFactory.class);
        when(describedModuleDescriptorFactoryAccessor.getDescribedModuleDescriptorFactories()).thenReturn(ImmutableList.of(factory));
        when(factory.getModuleDescriptorKeys()).thenReturn(ImmutableList.<String>of("key"));
        final Schema schema = mock(Schema.class);

        mockModuleDescriptorWithRequiredPermission(factory, "key", schema, ModuleDescriptorWithPermission.class);

        Set<String> allPermissions = ImmutableSet.of();
        when(productAccessor.getAllowedPermissions(InstallationMode.LOCAL)).thenReturn(allPermissions);

        Iterable<Schema> moduleSchemas = pluginDescriptorValidatorProvider.getModuleSchemas(InstallationMode.LOCAL);

        assertEquals(1, Iterables.size(moduleSchemas));
        assertEquals(schema, Iterables.get(moduleSchemas, 0));
    }

    @Test
    public void testGetModuleSchemasWithMatchingPermissions()
    {
        final DescribedModuleDescriptorFactory factory = mock(DescribedModuleDescriptorFactory.class);
        when(describedModuleDescriptorFactoryAccessor.getDescribedModuleDescriptorFactories()).thenReturn(ImmutableList.of(factory));
        when(factory.getModuleDescriptorKeys()).thenReturn(ImmutableList.<String>of("key"));
        final Schema schema = mock(Schema.class);

        mockModuleDescriptorWithRequiredPermission(factory, "key", schema, ModuleDescriptorWithPermission.class);

        when(productAccessor.getAllowedPermissions(InstallationMode.LOCAL)).thenReturn(ImmutableSet.<String>of("required_permission"));

        Iterable<Schema> moduleSchemas = pluginDescriptorValidatorProvider.getModuleSchemas(InstallationMode.LOCAL);

        assertEquals(1, Iterables.size(moduleSchemas));
        assertEquals(schema, Iterables.get(moduleSchemas, 0));
    }

    @SuppressWarnings("unchecked")
    private void mockModuleDescriptorWithRequiredPermission(DescribedModuleDescriptorFactory factory, String moduleKey, Schema schema, Class moduleDescriptorClass)
    {
        when(factory.getSchema(moduleKey)).thenReturn(schema);
        when(factory.getModuleDescriptorClass(moduleKey)).thenReturn(moduleDescriptorClass);
    }

    private static final class ModuleDescriptorWithNoPermission extends AbstractModuleDescriptor<Void>
    {
        public ModuleDescriptorWithNoPermission(ModuleFactory moduleFactory)
        {
            super(moduleFactory);
        }

        @Override
        public Void getModule()
        {
            return null;
        }
    }

    @RequirePermission("required_permission")
    private static final class ModuleDescriptorWithPermission extends AbstractModuleDescriptor<Void>
    {
        public ModuleDescriptorWithPermission(ModuleFactory moduleFactory)
        {
            super(moduleFactory);
        }

        @Override
        public Void getModule()
        {
            return null;
        }
    }
}
