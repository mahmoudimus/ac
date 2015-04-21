package com.atlassian.plugin.connect.test.plugin.module;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.confluence.capabilities.provider.XWorkPackageCreator;
import com.atlassian.xwork.interceptors.XsrfTokenInterceptor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.webwork.dispatcher.VelocityResult;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.Result;
import com.opensymphony.xwork.config.Configuration;
import com.opensymphony.xwork.config.entities.*;
import com.opensymphony.xwork.interceptor.Interceptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean.newXWorkActionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.XWorkInterceptorBean.newXWorkInterceptorBean;
import static com.atlassian.plugin.connect.modules.beans.nested.XWorkResultBean.newXWorkResultBean;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class XWorkPackageCreatorTest
{
    @Mock private Plugin plugin;
    @Mock private Configuration configuration;
    @Mock private PackageConfig defaultParentPackage;
    @Mock private InterceptorStackConfig validatingStack;
    @Mock private XsrfTokenInterceptor validatingStackInterceptor;

    private XWorkPackageCreator packageCreator;

    private PackageConfig packageConfig;
    private ActionConfig actionConfig;
    
    private ConnectAddonBean addon;

    @Before
    public void setup()
    {
        this.addon = newConnectAddonBean().withKey("test-plugin").build();
        
        when(plugin.getKey()).thenReturn("test-plugin");
        when(configuration.getPackageConfig("default")).thenReturn(defaultParentPackage);

        when(validatingStack.getInterceptors()).thenReturn(ImmutableList.of(validatingStackInterceptor));
        when(defaultParentPackage.getAllInterceptorConfigs()).thenReturn(ImmutableMap.of("validatingStack", validatingStack));
        when(defaultParentPackage.getAllResultTypeConfigs()).thenReturn(ImmutableMap.of("velocity", new ResultTypeConfig("velocity", VelocityResult.class)));

        XWorkActionModuleBean actionModuleBean = newXWorkActionBean()
                .withName(new I18nProperty("Test Action", ""))
                .withKey("test-action")
                .withNamespace("/test/namespace")
                .withClazz(TestAction.class)
                .withDefaultValidatingInterceptorStack()
                .withResultType("test-result", TestResult.class)
                .withInterceptor(newXWorkInterceptorBean()
                        .withName("test-interceptor")
                        .withClazz(TestInterceptor.class)
                        .withParam("test", "param")
                        .build())
                .withResult(newXWorkResultBean()
                        .withName("success")
                        .withType("velocity")
                        .build())
                .withResult(newXWorkResultBean()
                        .withName("fail")
                        .withType("test-result")
                        .withParam("test", "param")
                        .build())
                .build();

        packageCreator = new XWorkPackageCreator(addon, plugin, actionModuleBean);

        packageCreator.createAndRegister(configuration);

        ArgumentCaptor<PackageConfig> argumentCaptor = ArgumentCaptor.forClass(PackageConfig.class);
        verify(configuration).addPackageConfig(anyString(), argumentCaptor.capture());
        packageConfig = argumentCaptor.getValue();
        actionConfig = (ActionConfig)packageConfig.getActionConfigs().get("test-action");
    }

    @Test
    public void testPackageName()
    {
        assertEquals("atlassian-connect-test-plugin-test-action", packageConfig.getName());
    }

    @Test
    public void testNamespace()
    {
        assertEquals("/test/namespace", packageConfig.getNamespace());
    }

    @Test
    public void testParents()
    {
        assertEquals(1, packageConfig.getParents().size());
        assertSame(defaultParentPackage, packageConfig.getParents().get(0));
    }

    @Test
    public void testInterceptorRegistered()
    {
        assertEquals(1, packageConfig.getInterceptorConfigs().size());
        assertTrue(packageConfig.getInterceptorConfigs().containsKey("test-interceptor"));

        InterceptorConfig interceptorConfig = (InterceptorConfig)packageConfig.getInterceptorConfigs().get("test-interceptor");
        assertEquals("test-interceptor", interceptorConfig.getName());
        assertEquals(TestInterceptor.class.getName(), interceptorConfig.getClassName());
        assertEquals(1, interceptorConfig.getParams().size());
        assertEquals("param", interceptorConfig.getParams().get("test"));
    }

    @Test
    public void testResultTypesRegistered()
    {
        assertEquals(1, packageConfig.getResultTypeConfigs().size());
        assertTrue(packageConfig.getResultTypeConfigs().containsKey("test-result"));

        ResultTypeConfig resultConfig = (ResultTypeConfig)packageConfig.getResultTypeConfigs().get("test-result");
        assertEquals("test-result", resultConfig.getName());
        assertSame(TestResult.class, resultConfig.getClazz());
    }

    @Test
    public void testActionCreatedWithCorrectKey()
    {
        assertEquals(1, packageConfig.getActionConfigs().size());
        assertTrue(packageConfig.getActionConfigs().containsKey("test-action"));
    }

    @Test
    public void testActionClass()
    {
        assertEquals(TestAction.class.getName(), actionConfig.getClassName());
    }

    @Test
    public void testActionInterceptorStack()
    {
        List<Interceptor> interceptors = (List<Interceptor>)actionConfig.getInterceptors();
        assertEquals(2, interceptors.size());
        // Testing that the default interceptor ref "validatingStack" was added properly.
        assertSame(validatingStackInterceptor, interceptors.get(0));
        assertThat(interceptors.get(1), instanceOf(TestInterceptor.class));
    }

    @Test
    public void testActionResults()
    {
        assertEquals(2, actionConfig.getResults().size());
        assertTrue(actionConfig.getResults().keySet().contains("success"));
        assertTrue(actionConfig.getResults().keySet().contains("fail"));
    }

    @Test
    public void testActionResultClass()
    {
        ResultConfig resultConfig = (ResultConfig) actionConfig.getResults().get("fail");
        assertEquals(TestResult.class.getName(), resultConfig.getClassName());
        assertEquals(1, resultConfig.getParams().size());
        assertEquals("param", resultConfig.getParams().get("test"));
    }

    @Test
    public void testActionResultParams()
    {
        ResultConfig resultConfig = (ResultConfig) actionConfig.getResults().get("fail");
        assertEquals(1, resultConfig.getParams().size());
        assertEquals("param", resultConfig.getParams().get("test"));
    }

    @Test
    public void testActionResultFromParent()
    {
        // Ensures that the "velocity" result name we referenced comes from parent config.
        ResultConfig resultConfig = (ResultConfig) actionConfig.getResults().get("success");
        assertEquals(VelocityResult.class.getName(), resultConfig.getClassName());
    }

    public static class TestAction implements Action
    {
        @Override
        public String execute() throws Exception
        {
            return null;
        }
    }

    public static class TestInterceptor implements Interceptor
    {
        public String test;

        @Override
        public void destroy()
        {

        }

        @Override
        public void init()
        {

        }

        @Override
        public String intercept(final ActionInvocation invocation) throws Exception
        {
            return null;
        }
    }

    public static class TestResult implements Result
    {
        @Override
        public void execute(final ActionInvocation invocation) throws Exception
        {

        }
    }
}
