package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.APISupportBean;

import java.util.regex.Pattern;

/**
 * Extensible Content Type allow your Connect add-on to provide customized content type that can integrates with Confluence.
 *
 * @schemaTitle Extensible Content Type
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#EXTENSIBLE_CONTENT_TYPE_EXAMPLE}
 *
 *
 * <h3>Extensible Content Type</h3>
 *
 * <p>Extensible Content Type allows Connect add-on developer to declare customized content type which behavior like
 * existing built in content types: Page, BlogPost, Comment, etc.</p>
 * <p>An Extensible Content Type can:</p>
 *
 * <ul>
 *     <li>Created, deleted or removed by using <a href="https://docs.atlassian.com/confluence/REST/latest/">Confluence REST API</a></li>
 *     <li>Get indexed as normal content type and rendered in search result. </li>
 *     <li>Have full screen viewer or dialog as the view component.</li>
 * </ul>
 *
 * <h3>Create an Extensible Content Type via Confluence REST API</h3>
 *
 * <p>The above module snippet defined an Extensible Content Type "myExtensibleContentType".
 * You can create a new piece of Content with this type by posting the following JSON to Confluence <code>/rest/api/content</code> endpoint.</p>
 *
 * <pre><code>
 * {
 *     "type":"myExtensibleContentType",
 *     "title":"My content",
 *     "space":{
 *         "key":"ds"
 *     },
 *     "body":{
 *         "storage":{
 *             "value":"This is my content body",
 *             "representation":"storage"
 *         }
 *     }
 * }
 * </code></pre>
 *
 * @since 1.1.77
 */
public class ExtensibleContentTypeModuleBean extends RequiredKeyBean {
    private static final Pattern MODULE_KEY_MATCHER = Pattern.compile("\\W");

    /**
     * Declares information related for rendering the content in the UI.
     */
    @Required
    private UISupportBean uiSupport;

    /**
     * Captures business logic for this Extensible Content Type.
     */
    @Required
    private APISupportBean apiSupport;

    public ExtensibleContentTypeModuleBean() {
        initialise();
    }

    public ExtensibleContentTypeModuleBean(ExtensibleContentTypeModuleBeanBuilder builder) {
        super(builder);
        initialise();
    }

    private void initialise() {
    }

    public UISupportBean getUiSupport() {
        return uiSupport;
    }

    public APISupportBean getApiSupport() {
        return apiSupport;
    }

    public String getModuleKey() {
        return getRawKey();
    }

    public String getContentTypeName(ConnectAddonBean addon) {
        return getEncodedContentTypeName(addon);
    }

    public String getSearchBodyPropertyModuleKey(ConnectAddonBean addon) {
        return "search-body-property-" + getEncodedContentTypeName(addon);
    }

    private String getEncodedContentTypeName(ConnectAddonBean addon) {
        return MODULE_KEY_MATCHER.matcher(addon.getKey() + "-" + getRawKey()).replaceAll("-");
    }
}
