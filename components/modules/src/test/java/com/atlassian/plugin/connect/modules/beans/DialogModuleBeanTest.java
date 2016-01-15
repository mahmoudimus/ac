package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DialogModuleBeanTest
{
    @Test
    public void shouldHaveDialogOptions()
    {
        // Trivial test - the incoming immutable DialogOptions instance should be retained by the bean.
        DialogOptions options = DialogOptions.newDialogOptions()
                .withSize("fullscreen")
                .build();

        DialogModuleBean bean = DialogModuleBean.newDialogBean().withOptions(options).build();

        assertThat(bean.getOptions(), is(options));
    }
}