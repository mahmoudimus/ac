package com.atlassian.plugin.connect.test.jira.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Optional;
import org.openqa.selenium.By;

import java.util.List;
import java.util.function.Predicate;

public class AssociateCustomFieldToScreenPage2 extends AbstractJiraPage
{
    private final Optional<Long> customFieldId;

    @ElementBy (id = "add-field-to-screen")
    private PageElement element;

    @ElementBy (id = "update_submit")
    private PageElement submit;


    public AssociateCustomFieldToScreenPage2(final long customFieldId) {
        this.customFieldId = Optional.of(customFieldId);
    }

    public AssociateCustomFieldToScreenPage2() {
        this.customFieldId = Optional.absent();
    }

    @Override
    public TimedCondition isAt() {
        return element.timed().isPresent();
    }

    @Override
    public String getUrl() {
        if (!customFieldId.isPresent()) {
            throw new IllegalStateException("No CustomFieldId passed when created.");
        }
        return String.format("/secure/admin/AssociateFieldToScreens!default.jspa?fieldId=customfield_%d", customFieldId.get());
    }

    public AssociateCustomFieldToScreenPage2 selectRow(Predicate<String> fieldPredicate) {
        final List<PageElement> tableRows = element.findAll(By.cssSelector("#screenAssociations tr"));

        for (PageElement row : tableRows) {
            List<PageElement> columns = row.findAll(By.tagName("td"));
            if (columns.size() == 0) {
                continue;
            }
            String name = columns.get(0).getText();
            if (fieldPredicate.test(name)) {
                columns.get(2).find(By.tagName("input")).toggle();
            }
        }
        return this;
    }

    public Optional<Long> getCustomFieldId()
    {
        return customFieldId;
    }

    public void submit()
    {
        submit.click();
    }
}
