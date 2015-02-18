package it.servlet;

import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.RemoteRefreshIssuePageWebPanel;
import it.servlet.condition.ParameterCapturingServlet;
import it.servlet.iframe.CustomMessageServlet;
import it.servlet.iframe.MustacheServlet;
import it.servlet.macro.ExtendedMacroServlet;
import it.servlet.macro.SimpleMacroServlet;

import javax.servlet.http.HttpServlet;

/**
 * Utility methods for creating test servlets suitable for serving Connect iframes.
 */
public class ConnectAppServlets
{
    /**
     * @return a servlet that tests AMD is working correctly
     */
    public static HttpServlet amdTestServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("amd-test.mu"));
    }

    /**
     * Verify from a WebDriver test using {@link RemoteWebPanel#getApRequestStatusCode()},
     * {@link RemoteWebPanel#getApRequestUnauthorizedStatusCode()} and {@link RemoteWebPanel#getApRequestMessage()}.
     *
     * @return a servlet that makes a test AP.request()
     */
    public static HttpServlet apRequestServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("iframe-ap-request.mu"));
    }

    /**
     * Verify from a WebDriver test using {@link RemoteWebPanel#containsHelloWorld()}.
     *
     * @return a servlet that returns a "hello world" string
     */
    public static HttpServlet helloWorldServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("iframe-hello-world.mu"));
    }

    /**
     * @return a servlet that automatically resizes to the parent
     */
    public static HttpServlet sizeToParentServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("iframe-size-to-parent.mu"));
    }

    /**
     * @return a servlet with resizing disabled
     */
    public static HttpServlet noResizeServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("iframe-no-resize.mu"));
    }

    /**
     * @return a servlet with history test buttons
     */
    public static HttpServlet historyServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("iframe-history.mu"));
    }

    /**
     * @return a servlet that provides a button to trigger refreshing a JIRA issue page
     */
    public static HttpServlet refreshIssuePageButtonServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet(RemoteRefreshIssuePageWebPanel.TEMPLATE_PATH));
    }

    /**
     * @return a servlet that will create a workflow post function
     */
    public static HttpServlet workflowPostFunctionServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("jira/iframe-workflow-post-function.mu"));
    }

    /**
     * @return a servlet that will create a workflow post function that will fail validation
     */
    public static HttpServlet failValidateWorkflowPostFunctionServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("jira/iframe-fail-validate-workflow-post-function.mu"));
    }

    /**
     * Verify from a WebDriver test using {@link RemoteWebPanel#getCustomMessage()}.
     *
     * @param message the message to display
     * @param resize whether the iFrame content should automatically resize
     * @return a servlet that contains a custom message
     */
    public static HttpServlet customMessageServlet(String message, Boolean resize)
    {
        return wrapContextAwareServlet(new CustomMessageServlet(message, resize));
    }

    public static HttpServlet customMessageServlet(String message)
    {
        return wrapContextAwareServlet(new CustomMessageServlet(message, true));
    }

    /**
     * @return a servlet that tests AP.onDialogMessage()
     */
    public static HttpServlet dialogServlet()
    {
        return wrapContextAwareServlet(simpleDialogServlet());
    }

    /**
     * @return a servlet that tests AP.onDialogMessage() and captures parameters sent to it.
     */
    public static HttpServlet parameterCapturingDialogServlet(ParameterCapturingServlet captureServlet)
    {
        return new HttpContextServlet(captureServlet);
    }

    /**
     * @return a servlet that tests AP.onDialogMessage() and captures parameters sent to it.
     */
    public static ParameterCapturingServlet parameterCapturingDialogServlet()
    {
        return new ParameterCapturingServlet(simpleDialogServlet());
    }

    /**
     * @return a servlet that opens a dialog
     */
    public static HttpServlet openDialogServlet()
    {
        return openDialogServlet("my-dialog");
    }

    public static HttpServlet openDialogServlet(String dialogKey)
    {
        HttpContextServlet contextServlet = new HttpContextServlet(new MustacheServlet("iframe-open-dialog.mu"));
        contextServlet.getBaseContext().put("dialogKey", dialogKey);
        return contextServlet;
    }

    /**
     * @return a servlet that closes a dialog
     */
    public static HttpServlet closeDialogServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("iframe-close-dialog.mu"));
    }

    /**
     * @return a servlet that returns a button that opens an AUI message
     */
    public static HttpServlet openMessageServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("iframe-open-message.mu"));
    }

    /**
     * @return a servlet that returns 3 buttons for create, delete, and read a cookie
     */
    public static HttpServlet cookieServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("iframe-cookie.mu"));
    }


    public static HttpServlet macroSimple()
    {
        return wrapContextAwareServlet(new SimpleMacroServlet());
    }

    public static HttpServlet macroExtended()
    {
        return wrapContextAwareServlet(new ExtendedMacroServlet());
    }

    public static HttpServlet macroEditor()
    {
        return wrapContextAwareServlet(new MustacheServlet("confluence/macro/editor.mu"));
    }

    public static HttpServlet blueprintTemplateServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("confluence/test-blueprint.xml"));
    }

    public static HttpServlet macroBodyEditor(String newMacroBody)
    {
        HttpContextServlet contextServlet = new HttpContextServlet(new MustacheServlet("confluence/macro/editor-macro-body.mu"));
        contextServlet.getBaseContext().put("newMacroBody", newMacroBody);
        return contextServlet;
    }

    public static HttpServlet wrapContextAwareServlet(ContextServlet servlet)
    {
        return new HttpContextServlet(servlet);
    }

    public static HttpServlet echoQueryParametersServlet()
    {
        return wrapContextAwareServlet(new EchoQueryParametersServlet("echo-query.mu"));
    }

    public static HttpServlet resourceServlet(String resourcePath, String contentType)
    {
        return wrapContextAwareServlet(new ResourceServlet(resourcePath, contentType));
    }

    public static InstallHandlerServlet installHandlerServlet()
    {
        return new InstallHandlerServlet();
    }

    private static ContextServlet simpleDialogServlet()
    {
        return new MustacheServlet("dialog.mu");
    }

    public static HttpServlet dynamicMacroStaticServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("confluence/macro/dynamic-macro-static.mu"));
    }
}
