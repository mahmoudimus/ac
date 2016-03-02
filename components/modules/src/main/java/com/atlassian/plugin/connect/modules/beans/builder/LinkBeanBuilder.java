package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.LinkBean;

public class LinkBeanBuilder {
    private String url;
    private String title;
    private String altText;

    public LinkBeanBuilder() {
    }

    public LinkBeanBuilder(LinkBean defaultBean) {
        this.url = defaultBean.getUrl();
        this.title = defaultBean.getTitle();
        this.altText = defaultBean.getAltText();
    }

    public LinkBeanBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public LinkBeanBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public LinkBeanBuilder withAltText(String altText) {
        this.altText = altText;
        return this;
    }

    public LinkBean build() {
        return new LinkBean(this);
    }
}
