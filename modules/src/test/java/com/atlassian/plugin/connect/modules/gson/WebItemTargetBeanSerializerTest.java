package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.google.gson.Gson;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
        System.out.println(json);
        final WebItemTargetBean roundTripItem = gson.fromJson(json, WebItemTargetBean.class);
        System.out.println(roundTripItem);
        System.out.println(gson.toJson(roundTripItem));

        assertThat(roundTripItem, is(bean));
    }
}