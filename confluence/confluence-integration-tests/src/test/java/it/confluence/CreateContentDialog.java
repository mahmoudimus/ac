package it.confluence;

import java.util.concurrent.TimeUnit;

import com.atlassian.confluence.pageobjects.component.dialog.AbstractDialog;
import com.atlassian.confluence.pageobjects.page.content.EditContentPage;
import com.atlassian.confluence.pageobjects.page.content.Editor;
import com.atlassian.confluence.pageobjects.page.content.EditorPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;

import org.hamcrest.Matchers;
import org.openqa.selenium.By;

/**
 * A working replacement for the CreateDialog page object in Confluence.
 */
public class CreateContentDialog extends AbstractDialog
{
    public CreateContentDialog()
    {
        super("create-dialog");
    }

    public PageElement waitForBlueprint(String completeKey)
    {
        By locator = By.cssSelector("[data-item-module-complete-key='" + completeKey + "']");
        Poller.waitUntilTrue("blueprint is available in dialog",
                getDialog().find(locator).timed().isPresent());
        return getDialog().find(locator);
    }

    public CreateContentDialog createWithBlueprintWizard(String completeKey)
    {
        clickBlueprintItem(completeKey);
        return this;
    }

    public CreateContentDialog clickCreateButton()
    {
        getCreateButton().click();
        return this;
    }

    public EditContentPage getEditContentPage()
    {
        Editor editor = pageBinder.bind(Editor.class);
        Poller.waitUntil("Waiting for editor to become active.",
                editor.isEditorCurrentlyActive(), Matchers.is(true), Poller.by(1, TimeUnit.HOURS));

        return pageBinder.bind(EditContentPage.class);
    }

    public EditorPage createWithBlueprint(String completeKey)
    {
        clickBlueprintItem(completeKey);
        return getEditContentPage();
    }

    private PageElement getCreateButton()
    {
        /**
         * For blueprint with wizard. The page will have multiple create button presents
         * We should choose one that isn't disabled.
         */
        By locator = By.cssSelector(".create-dialog-create-button:not([disabled])");
        return getDialog().find(locator);
    }

    private void clickBlueprintItem(String completeKey)
    {
        waitForBlueprint(completeKey).click();
        getCreateButton().click();
    }
}
