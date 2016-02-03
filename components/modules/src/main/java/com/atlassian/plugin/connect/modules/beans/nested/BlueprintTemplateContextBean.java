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
 * A blueprint template is static - that is, the same template produces the same Confluence page. To produce
 * Confluence pages dynamically (i.e., create a different page for a different user), the template needs to use variable
 * substitution to produce the dynamic parts. Variable substitution requires the add-on to provide the data for said
 * substitution. This data is collectively called the context for substitution.
 *
 * The context is made up of a list of objects, retrieved from the context url specified by the blueprint context <code>url</code>
 * field in this module descriptor. See the <a href="#IDENTIFIERFIELD">properties section</a> for the definition of each of the fields in the context.
 *
 * <h3>Substituting dynamic variables in a blueprint</h3>
 *
 * Suppose we have a blueprint template module <code>/blueprints/blueprint.xml</code>:
 * <pre><code>&lt;h2 id=&quot;static1&quot;&gt;Hello Blueprint&lt;/h2&gt;
 *&lt;h2 id=&quot;custom1&quot;&gt;&lt;at:var at:name=&quot;ContentPageTitle&quot;/&gt;&lt;/h2&gt;
 *&lt;h2 id=&quot;custom2&quot;&gt;&lt;at:var at:name=&quot;custom-key1&quot;/&gt;&lt;/h2&gt;
 *&lt;h2 id=&quot;custom3&quot;&gt;&lt;at:var at:rawxhtml=&quot;true&quot; at:name=&quot;custom-key2&quot;/&gt;&lt;/h2&gt;
 *</code></pre>
 *
 * And an add-on server resource <code>/blueprints/context</code> which return this JSON response as the context:
 * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_CONTEXT_RESPONSE_EXAMPLE}
 *
 * During blueprint page creation, Confluence will send a POST request to <code>/blueprints/context</code> to
 * retrieve the context. The context retrieved will be parsed as a JSON array of objects, and used in the substitute of
 * the variables in the blueprint template shown above (<code>custom-key1</code>, <code>custom-key2</code>, etc are the variables).
 *
 * The final variable substituted storage format will look like this:
 * <pre><code>&lt;h2&gt;Hello Blueprint&lt;/h2&gt;
 *&lt;h2&gt;Unique Page Title 1&lt;/h2&gt;
 *&lt;h2&gt;&lt;ac:structured-macro ac:name=&quot;cheese&quot; ac:schema-version=&quot;1&quot; /&gt;&lt;/h2&gt;
 *</code></pre>
 *
 * The above is then used as the Confluence page, to be saved to the database, and displayed to the user according to the
 * <code>createResult</code> field of the blueprint module (see <a href="../confluence/blueprint.html">Blueprint Template Module</a>).
 *
 * An error message will be shown on the Content Create Dialog if Confluence experiences any problem accessing the blueprint
 * template or context URL (e.g., your add-on server failed to respond in 10 seconds, or the JSON returned is invalid).
 * Detailed error and/or stacktrace may be obtained via Atlassian support, but the end user will see an error like the one shown below:
 * <img src="../../assets/images/confluence-blueprint-context-error.png" alt="Blueprint context error" width="80%" style="border:1px solid #999;margin-top:10px;">
 *
 * <h3 id="BACKWARDSCOMPATIBILITY">Note on backwards compatibility of the <code>identifier</code> field</h3>
 * A blueprint template containing variables, may over time, change as the add-on evolves. However, because an end user has
 * the option of customizing a blueprint template, it is possible the customized version of the template differs from
 * the version in the add-on. Such a difference would not cause an error as long as the variables used in the template is
 * still being returned as part of the context url.
 *
 * Therefore, an add-on attempting to retain backwards compatibility for their blueprint templates should ensuring that any variables
 * used in a template are always returned in the context url, even if a new version of the blueprint template no longer uses it (an end user
 * may still be relying on an old version/customized of said template). This ensures that the template continues to function
 * when add-on updates the template.
 *
 */
public class BlueprintTemplateContextBean
{
    /**
     *
     * A URL to which a POST request will be made during the rendering of the blueprint (see <a href="#POSTBODYEXAMPLE">'Example of the request POST body' below</a> for an example
     * of what will be POST'ed to this resource). The response is then used for blueprint variable substitution, to enable
     * blueprints to create pages that have dynamic content.
     * <br>
     * <h4>Expected response format</h4>
     * The expected response from the context URL is a JSON array of objects with certain fields:
     * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_CONTEXT_RESPONSE_EXAMPLE}
     *
     * <h4 id="IDENTIFIERFIELD">The <code>identifier</code> field</h4>
     * The <code>identifier</code> field refers to the <code>name</code> attribute of the <code>var</code> element in a blueprint
     * template. If the <code>identifier</code> is matched with a template variable, the <code>value</code> is used in the substitution.
     * If a template contains a variable, but no matching <code>identifier</code> in the context, an error is generated.
     * An identifier with no matching template variable is regarded as a no-op.
     *
     * The <code>identifier</code> field for each context object must be unique. It is an error to have more than one context object with
     * the same identifier, and it is undefined which will get picked during substitution.
     *
     *
     * There are <code>identifier</code> names that are reserved for use with special meaning during substitution. They
     * must have a <code>representation</code> field with the value <code>plain</code>. The list below describes the meaning
     * of each existing reserved identifier.
     *
     * <ul>
     *     <li>
     *         <code>ContentPageTitle</code> : allows the blueprint template to set the page title. This must not contain any
     *          characters that cannot be used as a Confluence page title. If this reserved <code>identifier</code> is not
     *          found in the context, then the page created from the blueprint will not have its title set, and
     *          will require the user to set it before it can be saved, and the blueprint module must also specify
     *          'view' as the value of the <code>createResult</code> field in this case. Note, the capitalization 'C'
     *          in the name is not a mistake or typo.
     *     </li>
     *     <li>
     *         <code>labelString</code> : A space separated list of labels. The labels will be added to the page being created
     *         by this blueprint. See <a href="https://confluence.atlassian.com/conf55/confluence-user-s-guide/organising-content/working-with-confluence-labels">
     *             the confluence documentation on labels</a> for more information.
     *     </li>
     * </ul>
     * Using reserved identifiers as part of your template is possible, but these identifiers may change in the future, so
     * for best practise, only use non-reserved identifiers in your template. See the section on <a href="#BACKWARDSCOMPATIBILITY">preserving backwards compatibility</a>
     * for more information on <code>identifier</code>s.
     *
     * <h4>The <code>representation</code> field</h4>
     *
     * The <code>representation</code> must be one of the following values. If unset, it will default to <code>plain</code>.
     * <ul>
     *     <li><code>plain</code></li>
     *     <li><code>wiki</code></li>
     *     <li><code>storage</code></li>
     * </ul>
     *
     * <h4>The <code>value</code> field</h4>
     *
     * The <code>value</code> field must be in the format matching the <code>representation</code> field, outlined above.
     * If the format is incorrect (such as mismatched tags if using <code>storage</code> format), an error message will be displayed
     * in the resulting page. Below is an explanation of what each format means:
     *
     * <ul>
     *     <li>
     *         <code>plain</code>: Plain text, which will be HTML escaped during variable substitution. Use this for simple textual substitution.
     *     </li>
     *     <li>
     *         <code>wiki</code>: Confluence Wiki Markup <a href="https://confluence.atlassian.com/doc/confluence-wiki-markup-251003035.html">
     *         See this page</a> for what's available. The wiki markup will be rendered into html during substitution into the page.
     *         The resulting page will not contain any wiki markup. Use this format when simple styling is required, and can be
     *         easily achieved using wiki markup (such as emphasis, underlines, and tables etc).
     *     </li>
     *     <li>
     *         <code>storage</code>: Valid <a href="http://confluence.atlassian.com/display/DOC/Confluence+Storage+Format">
     *             Confluence Storage Format</a>. This value is substituted in to the page directly, before rendering of the page. Use this format
     *             to insert Confluence macros dynamically as part of your blueprint. See <a href="https://confluence.atlassian.com/conf53/confluence-storage-format-for-macros-411108832.html">this page</a>
     *             for a reference on the storage format for various macros.
     *     </li>
     * </ul>
     *
     *
     * <h3 id="POSTBODYEXAMPLE">Example of the request POST body</h3>
     * The context url might need some information in order to produce a more individually suitable response. Confluence
     * will send in the body of the request some information related to the blueprint during the creation process.
     * Below is an example of what will be sent in the body of the POST request:
     * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_POST_BODY_EXAMPLE}
     * <ul>
     *     <li><code>addonKey</code>: the key of the add-on that the blueprint is part of.</li>
     *     <li><code>blueprintKey</code> : the key of the blueprint that triggered this context request</li>
     *     <li><code>spaceKey</code> : the space to create the page in. This is selected by the user in the Create Dialog.</li>
     *     <li><code>userKey</code> : the user key of the user creating the page from blueprint.</li>
     *     <li><code>userLocale</code> : the locale of the user creating the page from blueprint.</li>
     * </ul>
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
