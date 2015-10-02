package it.common.upm;

import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.upm.pageobjects.PluginManager;
import it.common.iframe.AbstractPageTestBase;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public abstract class AbstractUpmPageTest extends AbstractPageTestBase<PluginManager>
{

    @Override
    protected void revealLinkIfNecessary(PluginManager pluginManager)
    {
        pluginManager.getPlugin(addonKey).openPluginDetails();
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition()
    {
        runner.setToggleableConditionShouldDisplay(false);

        login(testUserFactory.basicUser());

        // note we don't check that the link isn't displayed due to AC-973

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class,
                addonKey, MY_AWESOME_PAGE_KEY);
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString(MY_AWESOME_PAGE));
    }
}
