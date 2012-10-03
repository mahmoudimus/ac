package com.atlassian.labs.remoteapps.plugin.loader.universalbinary;

import com.atlassian.plugin.Plugin;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * A wrapper around servlet context that allows plugin servlets to add
 * attributes which will not be shared/clobbered by other plugins.
 */
public class UBServletContextWrapper implements ServletContext
{
    private final Plugin plugin;
    private final ServletContext context;
    private final ConcurrentMap<String, Object> attributes;
    private final Map<String, String> initParams;
    
    private final String contextPath;

    public UBServletContextWrapper(String contextPath,
                                   Plugin plugin,
                                   ServletContext context,
                                   ConcurrentMap<String, Object> attributes,
                                   Map<String, String> initParams
    )
    {
        this.contextPath = contextPath;
        this.plugin = plugin;
        this.context = context;
        this.attributes = attributes;
        this.initParams = initParams;
    }

    /**
     * <p>Gets the named attribute.  The attribute is first looked for in the local
     * attribute map, if it is not found there it is looked for in the wrapped
     * contexts attribute map.  If it is not there, null is returned.</p>
     * 
     * <p>A consequence of this ordering is that servlets may, in their own
     * context, override but not overwrite attributes from the wrapped context.</p>
     */
    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    /**
     * @return an enumeration of all the attributes from the wrapped 
     *         context as well as the local attributes.
     */
    public Enumeration getAttributeNames()
    {
        Collection<String> names = new HashSet<String>();
        names.addAll(attributes.keySet());
        return Collections.enumeration(names);
    }

    /**
     * Removes an attribute from the local context.  Leaves the wrapped context
     * completely untouched.
     */
    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    @Override
    public String getServletContextName()
    {
        return context.getServletContextName();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, String s1)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, Servlet servlet)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> aClass)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> tClass) throws ServletException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServletRegistration getServletRegistration(String s)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, String s1)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, Filter filter)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, Class<? extends Filter> aClass)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> tClass) throws ServletException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public FilterRegistration getFilterRegistration(String s)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
    {
        return context.getDefaultSessionTrackingModes();
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
    {
        return context.getEffectiveSessionTrackingModes();
    }

    @Override
    public void addListener(String s)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T extends EventListener> void addListener(T t)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addListener(Class<? extends EventListener> aClass)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> tClass) throws ServletException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor()
    {
        return context.getJspConfigDescriptor();
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return context.getClassLoader();
    }

    @Override
    public void declareRoles(String... strings)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * <p>Sets an attribute in the local attribute map, leaving the wrapped
     * context untouched.</p>
     * 
     * <p>Servlets may use this and the lookup ordering of the
     * <code>getAttribute()</code> method to effectively override the value
     * of an attribute in the wrapped servlet context with their own value and 
     * this overridden value will only be seen in the plugins own scope.</p>
     */
    public void setAttribute(String name, Object object)
    {
        attributes.put(name, object);
    }

    /**
     * @return the init parameter from the servlet module descriptor.
     */
    public String getInitParameter(String name)
    {
        return initParams.get(name);
    }

    /**
     * @return an enumeration of the init parameters from the servlet module
     * descriptor.
     */
    public Enumeration getInitParameterNames()
    {
        return Collections.enumeration(initParams.keySet());
    }

    @Override
    public boolean setInitParameter(String s, String s1)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @return the resource from the plugin classloader if it exists, otherwise the 
     *         resource is looked up from the wrapped context and returned
     */
    public URL getResource(String path) throws MalformedURLException
    {
        URL url = plugin.getResource(path);
        return url;
    }

    /**
     * @return the resource stream from the plugin classloader if it exists, otherwise
     *         the resource stream is attempted to be retrieved from the wrapped context
     */
    public InputStream getResourceAsStream(String path)
    {
        InputStream in = plugin.getResourceAsStream(path);
        return in;
    }

    /**
     * @return null so that servlet plugins can't escape their box
     */
    public ServletContext getContext(String uripath)
    {
        return null;
    }

    @Override
    public int getMajorVersion()
    {
        return context.getMajorVersion();
    }

    @Override
    public int getMinorVersion()
    {
        return context.getMinorVersion();
    }

    @Override
    public int getEffectiveMajorVersion()
    {
        return context.getEffectiveMajorVersion();
    }

    @Override
    public int getEffectiveMinorVersion()
    {
        return context.getEffectiveMinorVersion();
    }

    @Override
    public String getMimeType(String s)
    {
        return context.getMimeType(s);
    }

    public String getContextPath() {

        return contextPath;
    }

    public RequestDispatcher getNamedDispatcher(String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getRealPath(String path)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getServerInfo()
    {
        return context.getServerInfo();
    }

    public RequestDispatcher getRequestDispatcher(String path)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Set getResourcePaths(String arg0)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Servlet getServlet(String name) throws ServletException
    {
        return null;
    }

    public Enumeration getServletNames()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void log(String s)
    {
        context.log(s);
    }

    @Override
    public void log(Exception e, String s)
    {
        context.log(e, s);
    }

    @Override
    public void log(String s, Throwable throwable)
    {
        context.log(s, throwable);
    }

    public Enumeration getServlets()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    //---- All methods below simply delegate to the wrapped servlet context ----



}
