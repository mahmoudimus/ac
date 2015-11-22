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

    @ElementBy(cssSelector = ".create-dialog-create-button") PageElement saveButton;

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

    public EditorPage createWithBlueprint(String completeKey)
    {
        waitForBlueprint(completeKey).click();
        saveButton.click();

        Editor editor = pageBinder.bind(Editor.class);
        Poller.waitUntil("Waiting for editor to become active.",
                editor.isEditorCurrentlyActive(), Matchers.is(true), Poller.by(1, TimeUnit.HOURS));

        return pageBinder.bind(EditContentPage.class);
    }
}
