package com.atlassian.plugin.connect.test.common.servlet;

import javax.servlet.http.HttpServlet;

import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingServlet;

import com.google.common.collect.Lists;

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
        return mustacheServlet("amd-test.mu");
    }

    /**
     * Verify from a WebDriver test using {@link RemoteWebPanel#getApRequestStatusCode()},
     * {@link RemoteWebPanel#getApRequestUnauthorizedStatusCode()} and {@link RemoteWebPanel#getApRequestMessage()}.
     *
     * @return a servlet that makes a test AP.request()
     */
    public static HttpServlet apRequestServlet()
    {
        return mustacheServlet("iframe-ap-request.mu");
    }

    /**
     * Verify from a WebDriver test using {@link RemoteWebPanel#containsHelloWorld()}.
     *
     * @return a servlet that returns a "hello world" string
     */
    public static HttpServlet helloWorldServlet()
    {
        return mustacheServlet("iframe-hello-world.mu");
    }

    /**
     * @return a servlet that automatically resizes to the parent
     */
    public static HttpServlet sizeToParentServlet()
    {
        return mustacheServlet("iframe-size-to-parent.mu");
    }

    /**
     * @return a servlet with resizing disabled
     */
    public static HttpServlet noResizeServlet()
    {
        return mustacheServlet("iframe-no-resize.mu");
    }

    /**
     * @return a servlet with history test buttons
     */
    public static HttpServlet historyServlet()
    {
        return mustacheServlet("iframe-history.mu");
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
     * @param delegate
     */
    public static ParameterCapturingServlet parameterCapturingServlet(ContextServlet delegate)
    {
        return new ParameterCapturingServlet(delegate);
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
        return mustacheServlet("iframe-close-dialog.mu");
    }

    /**
     * @return a servlet that returns a button that opens an AUI message
     */
    public static HttpServlet openMessageServlet()
    {
        return mustacheServlet("iframe-open-message.mu");
    }

    /**
     * @return a servlet that returns 3 buttons for create, delete, and read a cookie
     */
    public static HttpServlet cookieServlet()
    {
        return mustacheServlet("iframe-cookie.mu");
    }

    public static HttpServlet wrapContextAwareServlet(ContextServlet servlet)
    {
        return wrapContextAwareServlet(servlet, Lists.<TestServletContextExtractor>newArrayList());
    }

    public static HttpServlet wrapContextAwareServlet(ContextServlet servlet, Iterable<TestServletContextExtractor> extractors)
    {
        return new HttpContextServlet(servlet, extractors);
    }

    public static HttpServlet echoQueryParametersServlet()
    {
        return wrapContextAwareServlet(new EchoQueryParametersServlet("echo-query.mu"));
    }

    public static HttpServlet resourceServlet(String resourcePath, String contentType)
    {
        return wrapContextAwareServlet(new ResourceServlet(resourcePath, contentType));
    }

    public static HttpServlet mustacheServlet(String templatePath)
    {
        return wrapContextAwareServlet(new MustacheServlet(templatePath));
    }

    public static InstallHandlerServlet installHandlerServlet()
    {
        return new InstallHandlerServlet();
    }

    public static ContextServlet simpleDialogServlet()
    {
        return new MustacheServlet("dialog.mu");
    }

    public static ContextServlet simpleInlineDialogServlet()
    {
        return new MustacheServlet("iframe-inline-dialog.mu");
    }

    public static ContextServlet simplePageServlet()
    {
        return new MustacheServlet("iframe-hello-world.mu");
    }

    public static ContextServlet channelConnectionVerifyServlet()
    {
        return new MustacheServlet("iframe-channel-connection-verify.mu");
    }
}
