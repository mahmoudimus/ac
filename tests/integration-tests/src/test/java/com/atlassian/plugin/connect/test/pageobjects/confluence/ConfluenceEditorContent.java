package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import org.openqa.selenium.By;

import java.util.concurrent.Callable;

public class ConfluenceEditorContent extends EditorContent
{

    public String getImagePlaceholderUrl()
    {
        return execute.onTinyMceIFrame(new Callable<String>()
        {
            @Override
            public String call()
            {
                return page.find(By.className("editor-inline-macro")).getAttribute("src");
            }
        });
    }
}
