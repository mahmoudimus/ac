package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ReportCategory;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

/**
 * Builds report module bean.
 *
 * @since 1.2
 */
public class ReportModuleBeanBuilder extends NamedBeanBuilder<ReportModuleBeanBuilder, ReportModuleBean>
{
    private String url;
    private Integer weight;
    private I18nProperty description;
    private ReportCategory reportCategory;
    private String thumbnailUrl;

    public ReportModuleBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public ReportModuleBeanBuilder withWeight(Integer weight)
    {
        this.weight = weight;
        return this;
    }

    public ReportModuleBeanBuilder withDescription(I18nProperty description)
    {
        this.description = description;
        return this;
    }

    public ReportModuleBeanBuilder withReportCategory(ReportCategory reportCategory)
    {
        this.reportCategory = reportCategory;
        return this;
    }

    public ReportModuleBeanBuilder withThumbnailUrl(String thumbnailUrl)
    {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    public ReportModuleBean build()
    {
        return new ReportModuleBean(this);
    }
}
