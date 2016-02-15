package it.jira.customfield;

import java.util.List;

import com.atlassian.jira.testkit.beans.CustomFieldResponse;
import com.atlassian.jira.testkit.client.CustomFieldsControl;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldModuleBean;
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

import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static it.jira.customfield.CustomFieldMatchers.customFieldResponse;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class TestConnectFieldAddonLifecycle extends JiraWebDriverTestBase
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
        product.backdoor().restoreBlankInstance();

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
    }

    @Test
    public void issueFieldIsAvailableAsACustomField()
    {
        List<CustomFieldResponse> customFields = customFieldsControl.getCustomFields();

        assertThat(customFields, hasItem(
                customFieldResponse(FIELD_NAME, FIELD_DESCRIPTION, getCustomFieldTypeKey(), getCustomFieldSearcherKey())));
    }

    @Test
    public void issueFieldIsNotAccessibleAfterPluginUninstall() throws Exception
    {
        addon.uninstall();

        List<CustomFieldResponse> customFields = customFieldsControl.getCustomFields();

        assertThat(customFields, not(hasItem(
                customFieldResponse(FIELD_NAME, FIELD_DESCRIPTION, getCustomFieldTypeKey(), getCustomFieldSearcherKey()))));
    }

    @Test
    public void issueFieldIsNotAccessibleAfterPluginDisabled() throws Exception
    {
        addon.setAddonEnabled(false);

        List<CustomFieldResponse> customFields = customFieldsControl.getCustomFields();

        assertThat(customFields, not(hasItem(
                customFieldResponse(FIELD_NAME, FIELD_DESCRIPTION, getCustomFieldTypeKey(), getCustomFieldSearcherKey()))));
    }

    @Test
    public void issueFieldIsAccessibleAgainAfterAddonIsReEnabled() throws Exception
    {
        addon.setAddonEnabled(false);

        addon.setAddonEnabled(true);
        List<CustomFieldResponse> customFields = customFieldsControl.getCustomFields();

        assertThat(customFields, hasSize(1));
        assertThat(customFields, contains(
                customFieldResponse(FIELD_NAME, FIELD_DESCRIPTION, getCustomFieldTypeKey(), getCustomFieldSearcherKey())));
    }

    @Test
    public void issueFieldIsAccessibleAgainAfterAddonIsReinstalled() throws Exception
    {
        addon.stopAndUninstall();

        addon.start();
        List<CustomFieldResponse> customFields = customFieldsControl.getCustomFields();

        assertThat(customFields, hasSize(1));
        assertThat(customFields, contains(
                customFieldResponse(FIELD_NAME, FIELD_DESCRIPTION, getCustomFieldTypeKey(), getCustomFieldSearcherKey())));
    }

    @Test
    public void issueFieldDoesNotRequireDescription() throws Exception
    {
        ConnectRunner addon = new ConnectRunner(product, "addonKey")
                .setAuthenticationToNone()
                .setVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .addModules("jiraIssueFields",
                        buildIssueFieldModule("fieldKey", "fieldName", null))
                .addScopes(ScopeName.READ)
                .start();

        List<CustomFieldResponse> customFields = customFieldsControl.getCustomFields();

        assertThat(customFields, hasItem(
                customFieldResponse("fieldName", null, getCustomFieldTypeKey("addonKey", "fieldKey"), getCustomFieldSearcherKey("addonKey", "fieldKey"))));

        addon.stopAndUninstall();
    }

    private static ConnectFieldModuleBean buildIssueFieldModule(String key, String title, String description)
    {
        return ConnectFieldModuleBean.newBuilder()
                .withKey(key)
                .withName(new I18nProperty(title, null))
                .withDescription(description != null ? new I18nProperty(description, null) : null)
                .withBaseType(IssueFieldType.TEXT)
                .build();
    }

    private String getCustomFieldTypeKey()
    {
        return getCustomFieldTypeKey(addonKey, FIELD_KEY);
    }

    private String getCustomFieldTypeKey(String addonKey, String fieldKey)
    {
        return "com.atlassian.plugins.atlassian-connect-plugin:" + addonKey + "__" + fieldKey;
    }

    private String getCustomFieldSearcherKey()
    {
        return getCustomFieldSearcherKey(addonKey, FIELD_KEY);
    }

    private String getCustomFieldSearcherKey(String addonKey, String fieldKey)
    {
        return "com.atlassian.plugins.atlassian-connect-plugin:" + addonKey + "__" + fieldKey + "_searcher";
    }
}
