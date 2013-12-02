package com.atlassian.plugin.connect.plugin.product.jira;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.MatchResult;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.rest.v2.issue.IncludedFields;
import com.atlassian.jira.rest.v2.issue.IssueBean;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.rest.v2.issue.scope.RequestScope;
import com.atlassian.jira.rest.v2.issue.scope.RequestScopeInterceptor;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.sal.api.ApplicationProperties;

import com.atlassian.sal.api.UrlMode;
import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.uri.UriTemplate;

import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Super ugly hack to render beans with JIRA's bean marshaling code
 */
@JiraComponent
public class JiraRestBeanMarshaler implements DisposableBean
{
    private final ServiceTracker beanFactoryTracker;
    private final ApplicationProperties applicationProperties;
    private final JiraBaseUrls jiraBaseUrls;
    private final ProjectRoleManager projectRoleManager;

    @Autowired
    public JiraRestBeanMarshaler(BundleContext bundleContext,
                                 ApplicationProperties applicationProperties, JiraBaseUrls jiraBaseUrls,
                                 ProjectRoleManager projectRoleManager)
    {
        this.applicationProperties = applicationProperties;
        this.jiraBaseUrls = jiraBaseUrls;
        this.projectRoleManager = projectRoleManager;
        Filter filter;
        try
        {
            filter = bundleContext.createFilter("(&(" + Constants.OBJECTCLASS +
                    "=org.springframework.beans.factory.BeanFactory)(Bundle-SymbolicName=" +
                    "com.atlassian.jira.rest-plugin))");
        }
        catch (InvalidSyntaxException e)
        {
            throw new RuntimeException(e);
        }
        beanFactoryTracker = new ServiceTracker(bundleContext, filter, null);
        beanFactoryTracker.open();
    }

    public JSONObject getRemoteIssue(final Issue issue)
    {
        final DefaultJaxbJsonMarshaller m = new DefaultJaxbJsonMarshaller();
        try
        {
            final BeanFactory beanFactory = (BeanFactory) beanFactoryTracker.waitForService(
                    TimeUnit.SECONDS.toMillis(30));

            final AtomicReference<JSONObject> result = new AtomicReference<JSONObject>();
            runInRequest(beanFactory, new Runnable()
            {
                @Override
                public void run()
                {
                    BeanBuilderFactory beanBuilderFactory = (BeanBuilderFactory) wrapService(
                            new Class[]{BeanBuilderFactory.class},
                            beanFactory.getBean("beanBuilderFactory", BeanBuilderFactory.class));
                    IssueBean issueBean = beanBuilderFactory.newIssueBeanBuilder(issue,
                            IncludedFields.includeAllByDefault(null)).build();
                    String text = m.marshal(issueBean);
                    try
                    {
                        result.set(new JSONObject(text));
                    }
                    catch (JSONException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            });
            return result.get();

        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException("Cannot find bean factory for JIRA rest plugin");
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public JSONObject getRemoteComment(Comment comment)
    {
        final DefaultJaxbJsonMarshaller m = new DefaultJaxbJsonMarshaller();
        CommentJsonBean bean = CommentJsonBean.shortBean(comment, jiraBaseUrls, projectRoleManager);
        String data = m.marshal(bean);
        try
        {
            return new JSONObject(data);
        }
        catch (JSONException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    private void runInRequest(BeanFactory beanFactory, Runnable runnable) throws IllegalAccessException,
            InvocationTargetException
    {
        RequestScope requestScope = (RequestScope) beanFactory.getBean("requestScope");
        RequestScopeInterceptor interceptor = new RequestScopeInterceptor(requestScope);
        interceptor.intercept(new MockMethodInvocation(runnable));
    }


    @Override
    public void destroy() throws Exception
    {
        beanFactoryTracker.close();
    }

    /**
     * Wraps the service in a dynamic proxy that ensures all methods are executed with the object class's class loader
     * as the context class loader
     *
     * @param interfaces The interfaces to proxy
     * @param service    The instance to proxy
     * @return A proxy that wraps the service
     */
    protected Object wrapService(final Class<?>[] interfaces, final Object service)
    {
        return Proxy.newProxyInstance(service.getClass().getClassLoader(), interfaces, new ContextClassLoaderSettingInvocationHandler(service));
    }

    /**
     * InvocationHandler for a dynamic proxy that ensures all methods are executed with the object
     * class's class loader as the context class loader.
     */
    private static class ContextClassLoaderSettingInvocationHandler implements InvocationHandler
    {
        private final Object service;

        ContextClassLoaderSettingInvocationHandler(final Object service)
        {
            this.service = service;
        }

        public Object invoke(final Object o, final Method method, final Object[] objects) throws
                Throwable
        {
            final Thread thread = Thread.currentThread();
            final ClassLoader ccl = thread.getContextClassLoader();
            try
            {
                thread.setContextClassLoader(service.getClass().getClassLoader());
                return method.invoke(service, objects);
            }
            catch (final InvocationTargetException e)
            {
                throw e.getTargetException();
            }
            finally
            {
                thread.setContextClassLoader(ccl);
            }
        }
    }

    private class MockMethodInvocation implements MethodInvocation
    {
        private final Runnable runnable;

        public MockMethodInvocation(Runnable runnable)
        {
            this.runnable = runnable;
        }

        @Override
        public Object getResource()
        {
            return null;
        }

        @Override
        public HttpContext getHttpContext()
        {
            return new MockHttpContext();
        }

        @Override
        public AbstractResourceMethod getMethod()
        {
            return null;
        }

        @Override
        public Object[] getParameters()
        {
            return new Object[0];
        }

        @Override
        public void invoke() throws IllegalAccessException, InvocationTargetException
        {
            runnable.run();
        }
    }

    private class MockHttpContext implements HttpContext
    {

        @Override
        public ExtendedUriInfo getUriInfo()
        {
            return new MockExtendedUriInfo();
        }

        @Override
        public HttpRequestContext getRequest()
        {
            return null;
        }

        @Override
        public HttpResponseContext getResponse()
        {
            return null;
        }

        @Override
        public Map<String, Object> getProperties()
        {
            return null;
        }

        @Override
        public boolean isTracingEnabled()
        {
            return false;
        }

        @Override
        public void trace(String s)
        {
        }
    }

    private class MockExtendedUriInfo implements ExtendedUriInfo
    {

        @Override
        public AbstractResourceMethod getMatchedMethod()
        {
            return null;
        }

        @Override
        public Throwable getMappedThrowable()
        {
            return null;
        }

        @Override
        public List<MatchResult> getMatchedResults()
        {
            return null;
        }

        @Override
        public List<UriTemplate> getMatchedTemplates()
        {
            return null;
        }

        @Override
        public List<PathSegment> getPathSegments(String name)
        {
            return null;
        }

        @Override
        public List<PathSegment> getPathSegments(String name, boolean decode)
        {
            return null;
        }

        @Override
        public String getPath()
        {
            return null;
        }

        @Override
        public String getPath(boolean decode)
        {
            return null;
        }

        @Override
        public List<PathSegment> getPathSegments()
        {
            return null;
        }

        @Override
        public List<PathSegment> getPathSegments(boolean decode)
        {
            return null;
        }

        @Override
        public URI getRequestUri()
        {
            return null;
        }

        @Override
        public UriBuilder getRequestUriBuilder()
        {
            return UriBuilder.fromUri(applicationProperties.getBaseUrl(UrlMode.CANONICAL));
        }

        @Override
        public URI getAbsolutePath()
        {
            return null;
        }

        @Override
        public UriBuilder getAbsolutePathBuilder()
        {
            return null;
        }

        @Override
        public URI getBaseUri()
        {
            return null;
        }

        @Override
        public UriBuilder getBaseUriBuilder()
        {
            return UriBuilder.fromUri(applicationProperties.getBaseUrl(UrlMode.CANONICAL));
        }

        @Override
        public MultivaluedMap<String, String> getPathParameters()
        {
            return null;
        }

        @Override
        public MultivaluedMap<String, String> getPathParameters(boolean decode)
        {
            return null;
        }

        @Override
        public MultivaluedMap<String, String> getQueryParameters()
        {
            return null;
        }

        @Override
        public MultivaluedMap<String, String> getQueryParameters(boolean decode)
        {
            return null;
        }

        @Override
        public List<String> getMatchedURIs()
        {
            return null;
        }

        @Override
        public List<String> getMatchedURIs(boolean decode)
        {
            return null;
        }

        @Override
        public List<Object> getMatchedResources()
        {
            return null;
        }
    }
}
