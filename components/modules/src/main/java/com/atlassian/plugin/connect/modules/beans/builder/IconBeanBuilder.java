package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.IconBean;

/**
 * @since 1.0
 */
public class IconBeanBuilder {
    private int width;
    private int height;
    private String url;

    public IconBeanBuilder() {
    }

    public IconBeanBuilder(IconBean defaultBean) {
        this.width = defaultBean.getWidth();
        this.height = defaultBean.getHeight();
        this.url = defaultBean.getUrl();
    }

    public IconBeanBuilder withWidth(int width) {
        this.width = width;
        return this;
    }

    public IconBeanBuilder withHeight(int height) {
        this.height = height;
        return this;
    }

    public IconBeanBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public IconBean build() {
        return new IconBean(this);
    }
}
