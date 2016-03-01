package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples;
import com.atlassian.plugin.connect.modules.beans.builder.BlueprintTemplateContextBeanBuilder;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

/**
 * Defines the context of the blueprint template.
 *
 * @schemaTitle Blueprint Template Context
 * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_TEMPLATE_CONTEXT_EXAMPLE}
 *
 * A blueprint template is static - the same template will produce the same Confluence page. To produce Confluence
 * pages dynamically (to create a different page for a different user), the template needs to
 * use variable substitution to produce the dynamic parts. Variable substitution requires the add-on to provide data
 * for substitution. Collectively, this data is called the context for substitution.
 *
 * The context is made up of a list of objects which are retrieved from the context url specified by the blueprint
 * context <code>url</code> field in this module descriptor. See <a href="#IDENTIFIERFIELD">Properties</a> for the definition of each field in the context.
 *
 * <h3>Substituting dynamic variables in a blueprint</h3>
 *
 * Let's say we have a blueprint template module <code>/blueprints/blueprint.xml</code>:
 * <pre><code>&lt;h2 id=&quot;static1&quot;&gt;Hello Blueprint&lt;/h2&gt;
 *&lt;h2 id=&quot;custom1&quot;&gt;&lt;at:var at:name=&quot;ContentPageTitle&quot;/&gt;&lt;/h2&gt;
 *&lt;h2 id=&quot;custom2&quot;&gt;&lt;at:var at:name=&quot;custom-key1&quot;/&gt;&lt;/h2&gt;
 *&lt;h2 id=&quot;custom3&quot;&gt;&lt;at:var at:rawxhtml=&quot;true&quot; at:name=&quot;custom-key2&quot;/&gt;&lt;/h2&gt;
 *</code></pre>
 *
 * And an add-on server resource <code>/blueprints/context</code> which returns this JSON response as the context:
 * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_CONTEXT_RESPONSE_EXAMPLE}
 *
 * During blueprint page creation, Confluence sends a POST request to <code>/blueprints/context</code> to retrieve
 * the context. The context retrieved is parsed as a JSON array of objects and used in the substitute of
 * the variables in the blueprint template above (<code>custom-key1</code>, <code>custom-key2</code> are the variables).
 *
 * The final, variable substituted, storage format will look like this:
 * <pre><code>&lt;h2&gt;Hello Blueprint&lt;/h2&gt;
 *&lt;h2&gt;Unique Page Title 1&lt;/h2&gt;
 *&lt;h2&gt;custom value 1&lt;/h2&gt;
 *&lt;h2&gt;&lt;ac:structured-macro ac:name=&quot;cheese&quot; ac:schema-version=&quot;1&quot; /&gt;&lt;/h2&gt;
 *</code></pre>
 *
 * This is then used as the Confluence page to be saved to the database and displayed to the user according to the
 * <code>createResult</code> field of the blueprint module (see <a href="../confluence/blueprint.html">Blueprint Template Module</a>).
 *
 * An error message appears in the Content Create Dialog if Confluence has any problems accessing the blueprint
 * template or context URL (for example if your add-on server failed to respond in 10 seconds or the JSON
 * returned is invalid). A detailed error and/or stacktrace may be accessible by Atlassian support, but the end user will see an
 * error like the one shown here:
 * <img src="../../assets/images/confluence-blueprint-context-error.png" alt="Blueprint context error" width="80%" style="border:1px solid #999;margin-top:10px;">
 *
 * <h3 id="BACKWARDSCOMPATIBILITY">Backwards compatibility of the <code>identifier</code> field</h3>
 * A blueprint template containing variables may change as the add-on evolves over time. However, because end users
 * can customize blueprint templates, it's possible for the customized version of the template to differ from
 * the version in the add-on. This difference won't cause an error as long as the variables used in the template are
 * still being returned as part of the context url.
 *
 * Add-ons wanting to retain backwards compatibility for their blueprint templates should ensure that any variables
 * used in a template are always returned in the context url, even if a new version of the blueprint template no
 * longer uses it (for example if users are relying on an old or customized version of the template). This ensures that the template
 * continues to function when the add-on updates the template.
 *
 */
public class BlueprintTemplateContextBean {
    /**
     *
     * A URL to which a POST request will be made during the rendering of the blueprint (see <a href="#POSTBODYEXAMPLE">'Example of the request POST body' below</a> for an example
     * of what will be POSTed to this resource). The response is then used for blueprint variable substitution, to enable
     * blueprints to create pages that have dynamic content.
     * <br>
     * <h4>Expected response format</h4>
     * The expected response from the context URL is a JSON array of objects with certain fields:
     * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_CONTEXT_RESPONSE_EXAMPLE}
     *
     * <h4 id="IDENTIFIERFIELD">The <code>identifier</code> field</h4>
     * The <code>identifier</code> field refers to the <code>name</code> attribute of the <code>var</code> element in a blueprint
     * template. If the <code>identifier</code> is matched with a template variable, the <code>value</code> is used in the substitution.
     * If a template contains a variable, but there is no matching <code>identifier</code> in the context, an error is generated.
     * An identifier with no matching template variable is regarded as a no-op.
     *
     * The <code>identifier</code> field for each context object must be unique. It is an error to have more than one context object with
     * the same identifier, and it is undefined which will get picked during substitution.
     *
     *
     * Some <code>identifier</code> names are reserved for use with special meaning during substitution. They
     * must have a <code>representation</code> field with the value <code>plain</code>. The list below describes the meaning
     * of each existing reserved identifier.
     *
     * <ul>
     *     <li>
     *         <code>ContentPageTitle</code>: allows the blueprint template to set the page title. It must not contain any
     *          characters that cannot be used as a Confluence page title. If this reserved <code>identifier</code> is not
     *          found in the context, the page created from the blueprint will not have a title set and
     *          will require the user to set it before it can be saved. The blueprint module must also specify
     *          'edit' as the value of the <code>createResult</code> field in this case. Note: the capital 'C'
     *          in the name is not a mistake or typo.
     *     </li>
     *     <li>
     *         <code>labelString</code>: A space separated list of labels. The labels will be added to the page being created
     *         by this blueprint. See <a href="https://confluence.atlassian.com/display/doc/Add%2C+Remove+and+Search+for+Labels">
     *             the Confluence docs about labels</a> for more information.
     *     </li>
     * </ul>
     * Using reserved identifiers as part of your template is possible, but these identifiers may change in the future, so
     * best practice is to only use non-reserved identifiers in your template. See <a href="#BACKWARDSCOMPATIBILITY">Backwards compatibility</a>
     * for more information on <code>identifier</code>s.
     *
     * <h4>The <code>representation</code> field</h4>
     *
     * The <code>representation</code> field must be one of the following values. If unset, it will default to <code>plain</code>.
     * <ul>
     *     <li><code>plain</code></li>
     *     <li><code>wiki</code></li>
     *     <li><code>storage</code></li>
     * </ul>
     *
     * <h4>The <code>value</code> field</h4>
     *
     * The <code>value</code> field must be in the same format as the <code>representation</code> field.
     * If the format is incorrect (such as mismatched tags in <code>storage</code> format), an error message will be displayed
     * in the resulting page. Here's an explanation of what each format means:
     *
     * <ul>
     *     <li>
     *         <code>plain</code>: Plain text, which is HTML escaped during variable substitution. Use this for simple textual substitution.
     *     </li>
     *     <li>
     *         <code>wiki</code>: Valid <a href="https://confluence.atlassian.com/display/DOC/Confluence+Wiki+Markup">
     *         Confluence Wiki Markup</a>. The wiki markup will be rendered into html during substitution into the page.
     *         The resulting page will not contain any wiki markup. Use this format when simple styling
     *         is required (such as emphasis, underlines or and tables etc).
     *     </li>
     *     <li>
     *         <code>storage</code>: Valid <a href="http://confluence.atlassian.com/display/DOC/Confluence+Storage+Format">
     *             Confluence Storage Format</a>. This value is substituted into the page directly, before the page is rendered. Use this format
     *             to insert Confluence macros dynamically as part of your blueprint. See <a href="https://confluence.atlassian.com/display/DOC/Macros">Macros</a>
     *             for a list of available Confluence macros, including storage format examples.
     *     </li>
     * </ul>
     *
     *
     * <h4 id="POSTBODYEXAMPLE">Example of the request POST body</h4>
     * The context url may need some information to produce a more individually suitable response. Confluence
     * will send some information related to the blueprint in the body of the request during the creation process.
     * Here's an example of what will be sent in the body of the POST request:
     * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_POST_BODY_EXAMPLE}
     * <ul>
     *     <li><code>addonKey</code>: the key of the add-on that the blueprint is part of.</li>
     *     <li><code>blueprintKey</code>: the key of the blueprint that triggered this context request.</li>
     *     <li><code>spaceKey</code>: the space to create the page in (this is selected by the user in the Create dialog).</li>
     *     <li><code>userKey</code>: the user key of the user creating the page from blueprint.</li>
     *     <li><code>userLocale</code>: the locale of the user creating the page from blueprint.</li>
     * </ul>
     */
    @Required
    @StringSchemaAttributes(format = "uri")
    private String url;

    public BlueprintTemplateContextBean(BlueprintTemplateContextBeanBuilder builder) {
        copyFieldsByNameAndType(builder, this);
    }

    public static BlueprintTemplateContextBeanBuilder newBlueprintTemplateContextBeanBuilder() {
        return new BlueprintTemplateContextBeanBuilder();
    }

    public String getUrl() {
        return url;
    }
}
