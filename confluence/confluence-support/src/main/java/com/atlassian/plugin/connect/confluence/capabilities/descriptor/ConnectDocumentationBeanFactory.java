package com.atlassian.plugin.connect.confluence.capabilities.descriptor;

import com.atlassian.confluence.util.i18n.DocumentationBean;
import com.atlassian.confluence.util.i18n.DocumentationBeanFactory;
import com.atlassian.plugin.connect.modules.beans.nested.LinkBean;

public class ConnectDocumentationBeanFactory implements DocumentationBeanFactory
{
    private final LinkBean linkBean;

    public ConnectDocumentationBeanFactory(LinkBean linkBean)
    {
        this.linkBean = linkBean;
    }

    @Override
    public DocumentationBean getDocumentationBean()
    {
        return new DocumentationBean()
        {
            @Override
            public String getLink(String docLink)
            {
                return exists(docLink) ? linkBean.getUrl() : "";
            }

            @Override
            public String getTitle(String docLink)
            {
                return exists(docLink) ? linkBean.getTitle() : "";
            }

            @Override
            public String getAlt(String docLink)
            {
                return exists(docLink) ? linkBean.getAltText() : "";
            }

            @Override
            public boolean isLocal(String docLink)
            {
                // always remote from Confluence's perspective
                return false;
            }

            @Override
            public boolean exists(String docLink)
            {
                return linkBean != null;
            }
        };
    }
}
