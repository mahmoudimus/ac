package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroForm;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;
import java.util.List;

public class ConfluenceMacroForm extends MacroForm
{
    private static final String MACRO_PARAM = "macro-param-";

    @FindBy(className = "macro-param-input")
    private List<WebElement> parameterInputs;

    public List<String> getParameterNames()
    {
        return Lists.newArrayList(Iterables.transform(parameterInputs,
                new Function<WebElement, String>()
                {
                    @Override
                    public String apply(@Nullable WebElement input)
                    {
                        String id = input.getAttribute("id");
                        return removePrefix(id, MACRO_PARAM);
                    }
                }
        ));
    }

    private static String removePrefix(String str, String prefix)
    {
        int index = str.indexOf(prefix);
        if (index == 0)
        {
            return str.substring(prefix.length());
        }
        return str;
    }
}
