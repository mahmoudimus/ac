package it.modules.confluence;

import java.util.List;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import com.google.common.collect.Lists;

import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ContentPropertyIndexSchemaModuleBean.newContentPropertyIndexSchemaModuleBean;
import static org.junit.Assert.assertTrue;

public class TestConfluenceContentProperty
{
    private static final String ATTACHMENT_PROPERTY_KEY = "attachment";
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final TestedProduct<?> product = OwnerOfTestedProduct.INSTANCE;
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        List<ContentPropertyIndexExtractionConfigurationBean> extractions = Lists.newArrayList(
                new ContentPropertyIndexExtractionConfigurationBean("size", "number"),
                new ContentPropertyIndexExtractionConfigurationBean("extension", "string")
        );

        ContentPropertyIndexKeyConfigurationBean keyConfigurationBean =
                new ContentPropertyIndexKeyConfigurationBean(ATTACHMENT_PROPERTY_KEY, extractions);

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(
                        "contentPropertyIndexSchema",
                        newContentPropertyIndexSchemaModuleBean()
                                .withName(new I18nProperty("Attachment indexing", "confluence.attachment.indexing"))
                                .withKey("connie-attachment-indexing")
                                .withKeyConfiguration(keyConfigurationBean)
                                .build()
                )
                .start();
    }

    @Test
    public void should()
    {
        assertTrue(true);
    }
}
