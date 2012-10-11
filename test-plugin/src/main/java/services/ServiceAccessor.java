package services;


import com.atlassian.plugin.remotable.api.annotation.ServiceReference;
import com.atlassian.plugin.remotable.api.service.EmailSender;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.api.service.http.HostXmlRpcClient;
import org.osgi.framework.BundleContext;
import servlets.SendEmailServlet;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ServiceAccessor
{
    private static SignedRequestHandler signedRequestHandler;
    private static HostXmlRpcClient hostXmlRpcClient;
    private static HostHttpClient hostHttpClient;
    private static SendEmailServlet sendEmailServlet;
    private static EmailSender emailSender;
    private static BundleContext bundleContext;

    @Inject
    public ServiceAccessor(
            @ServiceReference SignedRequestHandler signedRequestHandler,
            @ServiceReference HostXmlRpcClient hostXmlRpcClient,
            @ServiceReference HostHttpClient hostHttpClient,
            SendEmailServlet sendEmailServlet,
            @ServiceReference EmailSender emailSender,
            BundleContext bundleContext)
    {
        ServiceAccessor.bundleContext = bundleContext;
        ServiceAccessor.signedRequestHandler = signedRequestHandler;
        ServiceAccessor.hostXmlRpcClient = hostXmlRpcClient;
        ServiceAccessor.hostHttpClient = hostHttpClient;
        ServiceAccessor.sendEmailServlet = sendEmailServlet;
        ServiceAccessor.emailSender = emailSender;
    }

    public static SignedRequestHandler getSignedRequestHandler()
    {
        return signedRequestHandler;
    }

    public static HostXmlRpcClient getHostXmlRpcClient()
    {
        return hostXmlRpcClient;
    }

    public static HostHttpClient getHostHttpClient()
    {
        return hostHttpClient;
    }

    public static SendEmailServlet getSendEmailServlet()
    {
        return sendEmailServlet;
    }

    public static EmailSender getEmailSender()
    {
        return emailSender;
    }

    public static <T> T getService(Class<T> serviceClass)
    {
        return (T) bundleContext.getService(bundleContext.getServiceReference(serviceClass.getName()));
    }
}
