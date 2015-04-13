package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.ReportModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Allow add-ons to define a new report, which is linked from a project page.
 *
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#REPORT_EXAMPLE}
 * @since 1.2
 */
@SchemaDefinition("report")
public class ReportModuleBean extends RequiredKeyBean
{
    @Required
    @StringSchemaAttributes (format = "uri-template")
    private String url;

    @CommonSchemaAttributes (defaultValue = "100")
    private Integer weight;

    @Required
    private I18nProperty description;

    @CommonSchemaAttributes (defaultValue = "other")
    private ReportCategory reportCategory;

    @StringSchemaAttributes (format = "uri-template")
    private String thumbnailUrl;

    public ReportModuleBean(final ReportModuleBeanBuilder reportModuleBeanBuilder)
    {
        super(reportModuleBeanBuilder);
    }

    /**
     * Specifies the URL targeted by the report. The URL is relative to the add-on's base URL.
     *
     * @return the URL of the report
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * <p>Determines the order in which the report's link appears in the list.</p>
     *
     * <p>The "lightest" weight (i.e., lowest number) appears first, rising relative to other items,
     * while the "heaviest" weights sink to the bottom of the list.</p>
     *
     * @return the weight of the report
     */
    public Integer getWeight()
    {
        return weight;
    }

    /**
     * A human-readable description of this report module. This description is displayed on the reports list.
     *
     * @return the description of the report
     */
    public I18nProperty getDescription()
    {
        return description;
    }

    /**
     * The category of the report. The default category is other.
     *
     * @return the category of the report
     */
    public ReportCategory getReportCategory() {
        return (reportCategory != null) ? reportCategory : ReportCategory.OTHER;
    }

    /**
     * Specifies the URL of the report thumbnail. The URL is relative to the add-on's base URL.
     *
     * @return the URL of the report thumbnail
     */
    public String getThumbnailUrl()
    {
        return thumbnailUrl;
    }

    public static ReportModuleBeanBuilder newBuilder()
    {
        return new ReportModuleBeanBuilder();
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof ReportModuleBean))
        {
            return false;
        }

        ReportModuleBean other = (ReportModuleBean) otherObj;

        return new EqualsBuilder()
                .append(this.url, other.url)
                .append(this.weight, other.weight)
                .append(this.description, other.description)
                .append(this.reportCategory, other.reportCategory)
                .append(this.thumbnailUrl, other.thumbnailUrl)
                .appendSuper(super.equals(otherObj))
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(53, 11)
                .append(url)
                .append(weight)
                .append(description)
                .append(reportCategory)
                .append(thumbnailUrl)
                .append(super.hashCode())
                .build();
    }
}
