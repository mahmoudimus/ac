package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import org.openqa.selenium.By;

import java.util.concurrent.Callable;

public class ConfluenceEditorContent extends EditorContent
{
    public MacroList autocompleteMacro(final String text)
    {
        execute.onTinyMceIFrame(new Callable<Void>()
        {
            @Override
            public Void call()
            {
                page.find(By.id("tinymce")).type("{" + text);
                return null;
            }
        });
        return binder.bind(MacroList.class);
    }

}
