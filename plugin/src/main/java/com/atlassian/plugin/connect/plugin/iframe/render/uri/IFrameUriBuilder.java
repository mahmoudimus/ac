package com.atlassian.plugin.connect.plugin.iframe.render.uri;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Builds URIs to be used as the src attribute for iframes targeting Connect apps.
 */
@NotThreadSafe
public interface IFrameUriBuilder
{
    /**
     * @param key the addon key
     */
    AddOnUriBuilder addOn(String key);

    interface AddOnUriBuilder
    {
        /**
         * @param namespace the namespace, used for the context iframe (if there is a context iframe). The usual pattern
         * is to pass the module key in here.
         */
        NamespacedUriBuilder namespace(String namespace);
    }

    interface NamespacedUriBuilder
    {
        /**
         * @param uri a <a href="http://en.wikipedia.org/wiki/URL_Template">templated url</a>, containing template
         * variables that will be substituted for context parameters.
         */
        TemplatedBuilder urlTemplate(String uri);
    }

    interface TemplatedBuilder
    {
        /**
         * @param context the {@link ModuleContextParameters} containing the context issue, project, space, etc. This
         * builder does not do any permission checking, so it is up to the caller to apply the {@link ModuleContextFilter}
         * if necessary.
         */
        InitializedBuilder context(ModuleContextParameters context);
    }

    interface InitializedBuilder
    {
        /**
         * Adds an additional query parameter to the url.
         */
        InitializedBuilder param(String key, String value);
        /**
         * Adds query parameters to the url that mark it as a dialog.
         */
        InitializedBuilder dialog(boolean isDialog);
        /**
         * Toggles whether to sign this url using the signature mechanism configured for the specified add-on.
         * Signature generation defaults to {@code true}, so you only need call this method if you wish to disable it.
         */
        InitializedBuilder sign(boolean sign);
        /**
         * Toggles whether to include the standard xdm, user, licensing and l10n parameters in the url. Standard
         * parameter generation defaults to {@code true}, so you only need call this method if you wish to disable it.
         */
        InitializedBuilder includeStandardParams(boolean includeStandardParams);

        /**
         * uiParameters is an opaque (to the server) object where the client code can pass in the request and have the server
         * return it as part of the signed iFrame url. As it has it's own query parameter "ui-params" the contents should not
         * be confused with the resource keys (e.g. issue.key) so we don't need to validate them even though they end up
         * in the signed url
         */
        InitializedBuilder uiParams(Option<String> uiParameters);

        /**
         * @return the constructed (and signed, if requested) URL.
         */
        String build();
    }
}
