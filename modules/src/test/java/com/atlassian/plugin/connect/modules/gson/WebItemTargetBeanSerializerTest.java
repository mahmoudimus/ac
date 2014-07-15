package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.InlineDialogOptions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class WebItemTargetBeanSerializerTest
{

    private Gson gson = ConnectModulesGsonFactory.getGson();


    @Test
    public void dialogTargetRoundtripSerialisationMatches()
    {
        final WebItemTargetBean bean = WebItemTargetBean.newWebItemTargetBean()
                .withType(WebItemTargetType.dialog)
                .withOptions(DialogOptions.newDialogOptions()
                                .withHeight("100px")
                                .withWidth("200%")
                                .build()
                )
                .build();

        final String json = gson.toJson(bean);
        final WebItemTargetBean roundTripItem = gson.fromJson(json, WebItemTargetBean.class);

        assertThat(roundTripItem, is(bean));
    }

    @Test
    public void inlineDialogTargetRoundtripSerialisationMatches()
    {
        final WebItemTargetBean bean = WebItemTargetBean.newWebItemTargetBean()
                .withType(WebItemTargetType.inlineDialog)
                .withOptions(InlineDialogOptions.newInlineDialogOptions()
                                .withWidth("200%")
                                .withCloseOthers(false)
                                .withIsRelativeToMouse(true)
                                .withOffsetX("100%")
                                .withOffsetY("12px")
                                .withOnHover(true)
                                .withOnTop(false)
                                .withPersistent(true)
                                .withShowDelay(99)
                                .build()
                )
                .build();

        final String json = gson.toJson(bean);
        final WebItemTargetBean roundTripItem = gson.fromJson(json, WebItemTargetBean.class);

        assertThat(roundTripItem, is(bean));
    }

    @Test(expected = JsonSyntaxException.class)
    public void rejectsInvalidJson()
    {
        gson.fromJson("bad json", WebItemTargetBean.class);
    }

    @Test
    public void acceptsBooleansAsStrings()
    {
        final WebItemTargetBean webItemTargetBean = gson.fromJson("{\"type\":\"inlinedialog\",\"options\":{\"onHover\":\"true\"}}",
                WebItemTargetBean.class);

        assertTrue(webItemTargetBean.getOptions() instanceof InlineDialogOptions);
        final InlineDialogOptions options = (InlineDialogOptions) webItemTargetBean.getOptions();

        assertThat(options.getOnHover(), is(true));
    }

}