package it.servlet;

import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
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
        return wrapContextAwareServlet(new MustacheServlet("dialog.mu"));
    }

    /**
     * @return a servlet that opens a dialog
     */
    public static HttpServlet openDialogServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("iframe-open-dialog.mu"));
    }

    /**
     * @return a servlet that closes a dialog
     */
    public static HttpServlet closeDialogServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("iframe-close-dialog.mu"));
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

    public static HttpServlet wrapContextAwareServlet(ContextServlet servlet)
    {
        return new HttpContextServlet(servlet);
    }

    public static HttpServlet echoQueryParametersServlet()
    {
        return wrapContextAwareServlet(new EchoQueryParametersServlet());
    }

    public static HttpServlet resourceServlet(String resourcePath, String contentType)
    {
        return wrapContextAwareServlet(new ResourceServlet(resourcePath, contentType));
    }
}
