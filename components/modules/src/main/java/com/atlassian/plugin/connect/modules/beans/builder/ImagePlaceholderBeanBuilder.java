package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.ImagePlaceholderBean;

/**
 * @since 1.0
 */
public class ImagePlaceholderBeanBuilder<T extends ImagePlaceholderBeanBuilder, B extends ImagePlaceholderBean> {
    private Integer width;
    private Integer height;
    private String url;
    private Boolean applyChrome;

    public ImagePlaceholderBeanBuilder() {
    }

    public ImagePlaceholderBeanBuilder(ImagePlaceholderBean defaultBean) {
        this.width = defaultBean.getWidth();
        this.height = defaultBean.getHeight();
        this.url = defaultBean.getUrl();
        this.applyChrome = defaultBean.applyChrome();
    }

    public ImagePlaceholderBeanBuilder withWidth(Integer width) {
        this.width = width;
        return this;
    }

    public ImagePlaceholderBeanBuilder withHeight(Integer height) {
        this.height = height;
        return this;
    }

    public ImagePlaceholderBeanBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public ImagePlaceholderBeanBuilder withApplyChrome(Boolean applyChrome) {
        this.applyChrome = applyChrome;
        return this;
    }

    public B build() {
        return (B) new ImagePlaceholderBean(this);
    }
}
