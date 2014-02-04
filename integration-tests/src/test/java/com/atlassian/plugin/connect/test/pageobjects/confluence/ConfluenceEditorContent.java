package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import org.openqa.selenium.By;

import java.util.concurrent.Callable;

import static com.atlassian.pageobjects.elements.query.Poller.by;
import static org.hamcrest.Matchers.is;

public class ConfluenceEditorContent extends EditorContent
{
    public MacroList autoCompleteMacroList(final String text)
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

    public void setRichTextMacroBody(final String macroBody)
    {
        setMacroBody("<p>" + macroBody + "</p>");
    }

    public void setPlainTextMacroBody(final String macroBody)
    {
        setMacroBody("<pre>" + macroBody + "</pre>");
    }

    private void setMacroBody(final String macroBody)
    {
        execute.onTinyMceIFrame(new Callable<Void>()
        {
            @Override
            public Void call()
            {
                PageElement macroBodyElement = page.find(By.className("wysiwyg-macro-body"));
                Poller.waitUntil(macroBodyElement.timed().isPresent(), is(true), by(20000)); // this can be slow
                return null;
            }
        });

        client.executeScript("tinyMCE.activeEditor.contentDocument.getElementsByClassName(\"wysiwyg-macro-body\")[0].innerHTML=\"" + macroBody +"\"");
    }

}
