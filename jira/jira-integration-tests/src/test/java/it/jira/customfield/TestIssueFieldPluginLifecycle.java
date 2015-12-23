package it.jira.customfield;

import com.atlassian.jira.functest.framework.RestoreBlankInstance;
import com.atlassian.jira.testkit.beans.CustomFieldResponse;
import com.atlassian.jira.testkit.client.CustomFieldsControl;
import com.atlassian.plugin.connect.modules.beans.IssueFieldModuleBean;
import com.atlassian.plugin.connect.modules.beans.IssueFieldType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import it.jira.JiraTestBase;
import it.jira.JiraWebDriverTestBase;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static it.jira.customfield.CustomFieldMatchers.customFieldResponse;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

@RestoreBlankInstance
public class TestIssueFieldPluginLifecycle  extends JiraWebDriverTestBase
{
    private String addonKey;
    private ConnectRunner addon;

    private CustomFieldsControl customFieldsControl;

    private final String FIELD_KEY = "customfieldtype-key";
    private final String FIELD_DESCRIPTION = "my description";
    private String FIELD_NAME = "custom field title" + RandomStringUtils.randomAlphabetic(4);

    @Before
    public void setUp() throws Exception
    {
        project = JiraTestBase.addProject();
        addonKey = AddonTestUtils.randomAddonKey();
        customFieldsControl = product.backdoor().customFields();

        addon = new ConnectRunner(product, addonKey)
                .setAuthenticationToNone()
                .setVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .addModules("jiraIssueFields",
                        buildIssueFieldModule(FIELD_KEY, FIELD_NAME, FIELD_DESCRIPTION))
                .addScopes(ScopeName.READ)
                .start();

        verifyFieldAvailable();
    }

    private void verifyFieldAvailable()
    {
        List<CustomFieldResponse> customFields = customFieldsControl.getCustomFields();

        assertThat(customFields, hasItem(
                customFieldResponse(FIELD_NAME, FIELD_DESCRIPTION, getCustomFieldTypeKey(), getCustomFieldSearcherKey())));
    }

    @Test
    public void fieldIsNotAccessibleAfterPluginUninstall() throws Exception
    {
        addon.uninstall();

        List<CustomFieldResponse> customFields = customFieldsControl.getCustomFields();

        assertThat(customFields, not(hasItem(
                customFieldResponse(FIELD_NAME, FIELD_DESCRIPTION, getCustomFieldTypeKey(), getCustomFieldSearcherKey()))));
    }

    private static IssueFieldModuleBean buildIssueFieldModule(String key, String title, String description)
    {
        return IssueFieldModuleBean.newBuilder()
                .withKey(key)
                .withName(new I18nProperty(title, null))
                .withDescription(new I18nProperty(description, null))
                .withBaseType(IssueFieldType.TEXT)
                .build();
    }

    private String getCustomFieldTypeKey()
    {
        return "com.atlassian.plugins.atlassian-connect-plugin:" + addonKey + "__" + FIELD_KEY;
    }

    private String getCustomFieldSearcherKey()
    {
        return "com.atlassian.plugins.atlassian-connect-plugin:" + addonKey + "__" + FIELD_KEY + "_searcher";
    }

}
