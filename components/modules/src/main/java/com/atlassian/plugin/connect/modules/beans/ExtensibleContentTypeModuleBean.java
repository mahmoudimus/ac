package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.APISupportBean;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.OperationSupportBean;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.UISupportBean;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Extensible Content Type allow your Connect add-on to provide customized content type that can integrates with Confluence.
 *
 * @schemaTitle Extensible Content Type
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#EXTENSIBLE_CONTENT_TYPE_EXAMPLE}
 *
 * <h3>Extensible Content Type</h3>
 *
 * Extensible Content Type allows Connect add-on developer to declare customized content type which behavior like
 * existing built in content types: Page, Blog, Comment, etc.
 *
 * An Extensible Content Type can:
 * <ul>
 *     <li>Created, deleted or removed by using <a href="https://docs.atlassian.com/confluence/REST/latest/">Confluence REST API</a></li>
 *     <li>Get indexed as normal content type and rendered in search result. </li>
 *     <li>Have full screen viewer or dialog as the view component.</li>
 * </ul>
 *
 * <h3>Create an Extensible Content Type via Confluence REST API</h3>
 *
 *
 * @since 1.1.77
 */
public class ExtensibleContentTypeModuleBean extends RequiredKeyBean
{
    /**
     * Declares information related for rendering the content in the UI.
     */
    @Required
    private UISupportBean uiSupport;

    /**
     * Declares permission for operating this Extensible Content Type.
     */
    private OperationSupportBean operationSupport;

    /**
     * Captures business logic for this Extensible Content Type.
     */
    @Required
    private APISupportBean apiSupport;

    public ExtensibleContentTypeModuleBean()
    {
        initialise();
    }

    public ExtensibleContentTypeModuleBean(ExtensibleContentTypeModuleBeanBuilder builder)
    {
        super(builder);
        initialise();
    }

    private void initialise()
    {
        operationSupport = ObjectUtils.defaultIfNull(operationSupport, new OperationSupportBean());
    }

    public UISupportBean getUiSupport()
    {
        return uiSupport;
    }

    public OperationSupportBean getOperationSupport()
    {
        return operationSupport;
    }

    public APISupportBean getApiSupport()
    {
        return apiSupport;
    }

    public String getModuleKey()
    {
        return getRawKey();
    }
}
