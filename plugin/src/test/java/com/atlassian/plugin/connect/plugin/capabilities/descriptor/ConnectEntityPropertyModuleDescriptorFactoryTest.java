package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.index.IndexDocumentConfiguration;
import com.atlassian.jira.index.IndexDocumentConfigurationFactory;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.plugin.index.EntityPropertyIndexDocumentModuleDescriptor;
import com.atlassian.jira.plugin.index.EntityPropertyIndexDocumentModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.*;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.atlassian.jira.index.IndexDocumentConfiguration.ExtractConfiguration;
import static com.atlassian.jira.index.IndexDocumentConfiguration.KeyConfiguration;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean.newEntityPropertyModuleBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class ConnectEntityPropertyModuleDescriptorFactoryTest
{
    public static final String PROPERTY_KEY = "attachment";
    public static final String SIZE_EXTRACTOR = "size";
    public static final String EXTENSION_EXTRACTOR = "extension";
    public static final String I18N_KEY = "attachment.indexing";

    @Rule
    public RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock private ConnectContainerUtil autowireUtil;
    @Mock private Plugin plugin;
    @Mock private BundleContext bundleContext;
    @Mock private JiraAuthenticationContext authContext;
    @Mock private ModuleFactory moduleFactory;
    @AvailableInContainer private final IndexDocumentConfigurationFactory indexDocumentConfigurationFactory = new IndexDocumentConfigurationFactory();

    private EntityPropertyIndexDocumentModuleDescriptor moduleDescriptor;
    private ConnectAddonBean addon;
    
    @Before
    public void setUp()
    {
        this.addon = newConnectAddonBean().withKey("com.atlassian.plugin.key").build();
        when(plugin.getKey()).thenReturn("com.atlassian.plugin.key");
        ConnectEntityPropertyModuleDescriptorFactory factory = new ConnectEntityPropertyModuleDescriptorFactory(autowireUtil);
        when(autowireUtil.createBean(eq(EntityPropertyIndexDocumentModuleDescriptorImpl.class)))
                .thenReturn(new EntityPropertyIndexDocumentModuleDescriptorImpl(authContext, moduleFactory));
        EntityPropertyModuleBean bean = createBean();
        this.moduleDescriptor = factory.createModuleDescriptor(addon, plugin, bean);
    }

    @Test
    public void entityKeyIsCorrect()
    {
        assertThat(moduleDescriptor.getIndexDocumentConfiguration().getEntityKey(), is(EntityPropertyType.issue.getValue()));
    }

    @Test
    public void completeKeyIsCorrect()
    {
        assertThat(moduleDescriptor.getCompleteKey(), startsWith(addon.getKey() + ":" + addonAndModuleKey("com-atlassian-plugin-key","attachment-indexing")));
    }

    @Test
    public void keyConfigurationIsCorrect()
    {
        List<KeyConfiguration> keyConfigurations =
                moduleDescriptor.getIndexDocumentConfiguration().getKeyConfigurations();

        assertThat(keyConfigurations, hasSize(1));
        assertThat(keyConfigurations, Matchers.<KeyConfiguration>hasItem(hasProperty("propertyKey", is(PROPERTY_KEY))));
    }

    @Test
    public void extractorsAreCorrect()
    {
        List<ExtractConfiguration> extractors =
                moduleDescriptor.getIndexDocumentConfiguration().getKeyConfigurations().get(0).getExtractorConfigurations();

        assertThat(extractors, hasSize(2));
        assertThat(extractors, Matchers.<ExtractConfiguration>hasItem(hasProperty("path", is(SIZE_EXTRACTOR))));
        assertThat(extractors, Matchers.<ExtractConfiguration>hasItem(hasProperty("path", is(EXTENSION_EXTRACTOR))));

        assertThat(extractors, Matchers.<ExtractConfiguration>hasItem(hasProperty("type", is(IndexDocumentConfiguration.Type.NUMBER))));
        assertThat(extractors, Matchers.<ExtractConfiguration>hasItem(hasProperty("type", is(IndexDocumentConfiguration.Type.STRING))));
    }

    @Test
    public void i18nKeyIsCorrect()
    {
        assertThat(moduleDescriptor.getI18nNameKey(), is(I18N_KEY));
    }

    private EntityPropertyModuleBean createBean()
    {
        ImmutableList<EntityPropertyIndexExtractionConfigurationBean> extractionConfigurationBeans = ImmutableList.of(
                new EntityPropertyIndexExtractionConfigurationBean(SIZE_EXTRACTOR, EntityPropertyIndexType.number),
                new EntityPropertyIndexExtractionConfigurationBean(EXTENSION_EXTRACTOR, EntityPropertyIndexType.string));
        EntityPropertyIndexKeyConfigurationBean keyConfigurationBean =
                new EntityPropertyIndexKeyConfigurationBean(extractionConfigurationBeans, PROPERTY_KEY);

        return newEntityPropertyModuleBean()
                .withKeyConfiguration(keyConfigurationBean)
                .withEntityType(EntityPropertyType.issue)
                .withName(new I18nProperty("Attachment Indexing", I18N_KEY))
                .withKey("attachment-indexing")
                .build();
    }
}
