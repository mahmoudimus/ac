package com.atlassian.plugin.connect.plugin.product.confluence;

import com.atlassian.json.marshal.Jsonable;
import com.atlassian.plugin.connect.modules.beans.nested.AutoconvertBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.webresource.api.data.WebResourceDataProvider;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 */
public class AutoconvertWebResourceDataProvider implements WebResourceDataProvider
{
    @Override
    public Jsonable get()
    {
        final List<AutoconvertBean> beans = Arrays.asList(
                AutoconvertBean.newAutoconvertBean().withPattern("docs.google.com\\/.*\\/document\\/d\\/(.*?)\\/edit.*").build(),
                AutoconvertBean.newAutoconvertBean().withPattern("LOOK HERE AT AUTOCONVERT").build()
        );

        return new Jsonable()
        {
            @Override
            public void write(Writer writer) throws IOException
            {
                Gson gson = ConnectModulesGsonFactory.getGson();
                writer.write(gson.toJson(beans));
            }
        };
    }
}
