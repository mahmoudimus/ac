package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples;
import com.atlassian.plugin.connect.modules.beans.builder.BlueprintTemplateContextBeanBuilder;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

/**
 * Defines the context of the blueprint.
 *
 * @schemaTitle Blueprint Template Context
 * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_TEMPLATE_CONTEXT_EXAMPLE}
 *
 * <h3>Example of dynamic variable substitution in blueprint</h3>
 *
 * Suppose we have a blueprint template <code>/blueprints/blueprint.xml</code> defined like this:
 * <pre><code>&lt;h2 id=&quot;static1&quot;&gt;Hello Blueprint&lt;/h2&gt;
 *&lt;h2 id=&quot;custom1&quot;&gt;&lt;at:var at:name=&quot;ContentPageTitle&quot;/&gt;&lt;/h2&gt;
 *&lt;h2 id=&quot;custom2&quot;&gt;&lt;at:var at:name=&quot;custom1&quot;/&gt;&lt;/h2&gt;
 *&lt;h2 id=&quot;custom3&quot;&gt;&lt;at:var at:rawxhtml=&quot;true&quot; at:name=&quot;custom-key2&quot;/&gt;&lt;/h2&gt;
 * </code></pre>
 *
 * And an add-on server resource <code>/blueprints/context</code> can return context bean JSON like this:
 * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_CONTEXT_RESPONSE_EXAMPLE}
 *
 * During rendering the blueprint, Confluence will send a POST request to <code>/blueprints/context</code> to
 * retrieve the context JSON object. After that it will substitute the variables in the blueprint template
 * with value supplied by the context.
 *
 * The rendered storage format result will look like this:
 * <pre><code>&lt;h2&gt;Hello Blueprint&lt;/h2&gt;
 *&lt;h2&gt;Unique Page Title 1&lt;/h2&gt;
 *&lt;h2&gt;&lt;ac:structured-macro ac:macro-id=&quot;b85c8297-ad77-410e-a747-560315a5c40e&quot; ac:name=&quot;cheese&quot; ac:schema-version=&quot;1&quot; /&gt;&lt;/h2&gt;
 * </code></pre>
 *
 * A message will be shown on the content create dialog when Confluence having problem access to the blueprint template or context resource.
 * Detailed error log and stacktrace can be found in the server log.
 * <img src="/assets/images/confluence-blueprint-context-error.png" alt="Blueprint context error" width="80%" style="border:1px solid #999;margin-top:10px;">
 */
public class BlueprintTemplateContextBean
{
    /**
     * <p>
     * A URL to which a POST request will be made during the rendering of the blueprint for which this context bean is
     * associated.<br>
     *
     * The expected return value is a JSON array of context values, for example:</p>
     * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_CONTEXT_RESPONSE_EXAMPLE}
     *
     * <p>
     * The <code>identifier</code> must be unique.<br>
     * </p>
     *
     * <p>
     * The <code>representation</code> must be one of following value. If unset, it will be default to <code>plain</code>.
     * <ul>
     *     <li><code>plain</code></li>
     *     <li><code>wiki</code></li>
     *     <li><code>storage</code></li>
     * </ul>
     * </p>
     *
     * <p>
     * The <code>value</code> must be a string that conforms to the <code>representation</code>.
     * <ul>
     *     <li><code>plain</code>: Plain text</li>
     *     <li><code>wiki</code>: Wiki markup</li>
     *     <li><code>storage</code>: Valid <a href="http://confluence.atlassian.com/display/DOC/Confluence+Storage+Format">Confluence Storage Format</a></li>
     * </ul>
     * </p>
     *
     * <h3>Example of POST body for requesting blueprint context</h3>
     *
     * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_POST_BODY_EXAMPLE}
     */
    @Required
    @StringSchemaAttributes (format = "uri")
    private String url;

    public BlueprintTemplateContextBean(BlueprintTemplateContextBeanBuilder builder)
    {
        copyFieldsByNameAndType(builder, this);
    }

    public static BlueprintTemplateContextBeanBuilder newBlueprintTemplateContextBeanBuilder()
    {
        return new BlueprintTemplateContextBeanBuilder();
    }

    public String getUrl()
    {
        return url;
    }
}
