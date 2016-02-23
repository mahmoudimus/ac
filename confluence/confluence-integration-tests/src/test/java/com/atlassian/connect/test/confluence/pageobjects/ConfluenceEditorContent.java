package com.atlassian.connect.test.confluence.pageobjects;

import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import org.openqa.selenium.By;

public class ConfluenceEditorContent extends EditorContent {
    public String getImagePlaceholderUrl() {
        return execute.onTinyMceIFrame(() -> page.find(By.className("editor-inline-macro")).getAttribute("src"));
    }
}
