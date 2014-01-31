package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestURLHandler;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptorImpl;
import com.atlassian.jira.plugin.webfragment.conditions.UserLoggedInCondition;
import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactoryImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.JiraConditions;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ParamsModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.SearchRequestViewModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.util.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.plugin.product.jira.JiraProductAccessor;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchRequestViewModuleDescriptorFactoryTest
{
    @Mock
    private Plugin plugin;
    @Mock
    private JiraAuthenticationContext authenticationContext;
    @Mock
    private SearchRequestURLHandler urlHandler;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    @Mock
    private TemplateRenderer templateRenderer;
    @Mock
    private DelegatingComponentAccessor componentAccessor;
    @Mock
    private UserManager userManager;
    @Mock
    private MailQueue mailQueue;
    @Mock
    private HostContainer hostContainer;

    private SearchRequestViewModuleDescriptorImpl descriptor;

    @Before
    public void beforeEachTest() throws Exception
    {
        RemotablePluginAccessorFactoryForTests remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();

        when(plugin.getKey()).thenReturn("my-plugin");
        when(plugin.<UserLoggedInCondition>loadClass(eq("com.atlassian.jira.plugin.webfragment.conditions.UserLoggedInCondition"), any(Class.class)))
                .thenReturn(UserLoggedInCondition.class);

        ConditionDescriptorFactory conditionDescriptorFactory = new ConditionDescriptorFactoryImpl(hostContainer);
        ConditionModuleFragmentFactory conditionModuleFragmentFactory = new ConditionModuleFragmentFactory(
                new JiraProductAccessor(userManager, mailQueue, new JiraConditions()), new ParamsModuleFragmentFactory());
        when(hostContainer.create(UserLoggedInCondition.class)).thenReturn(new UserLoggedInCondition());

        when(componentAccessor.getComponent(SearchRequestURLHandler.class)).thenReturn(urlHandler);
        when(componentAccessor.getComponent(ConditionDescriptorFactory.class)).thenReturn(conditionDescriptorFactory);

        SearchRequestViewModuleDescriptorFactory factory = new SearchRequestViewModuleDescriptorFactory(
                authenticationContext, conditionModuleFragmentFactory, applicationProperties,
                searchRequestViewBodyWriterUtil, templateRenderer, remotablePluginAccessorFactoryForTests, componentAccessor);

        SearchRequestViewModuleBean bean = SearchRequestViewModuleBean.newSearchRequestViewModuleBean()
                .withWeight(55)
                .withUrl("http://search.example.com")
                .withName(new I18nProperty("A Search Request View", null))
                .withKey("a-search-request-view")
                .withDescription(new I18nProperty("A description", null))
                .withConditions(
                        newSingleConditionBean().withCondition("user_is_logged_in").build())
                .build();

        this.descriptor = (SearchRequestViewModuleDescriptorImpl) factory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        this.descriptor.enabled();
    }

    @Test
    public void verifyCompleteKeyIsCorrect()
    {
        assertThat(descriptor.getCompleteKey(), is("my-plugin:a-search-request-view"));
    }

    @Test
    public void verifyNameIsSet() throws Exception
    {
        assertThat(descriptor.getName(), is("A Search Request View"));
    }

    @Test
    public void verifyDescriptionIsSet() throws Exception
    {
        assertThat(descriptor.getDescription(), is("A description"));
    }

    @Test
    public void verifyWeightIsSet() throws Exception
    {
        assertThat(descriptor.getOrder(), is(55));
    }

    @Test
    public void verifyContentType() throws Exception
    {
        assertThat(descriptor.getContentType(), is("text/html"));
    }

    @Test
    public void verifyFileExtension() throws Exception
    {
        assertThat(descriptor.getFileExtension(), is("html"));
    }

    @Test
    public void verifySearchRequestViewIsCreated() throws Exception
    {
        assertThat(descriptor.getSearchRequestView(), is(not(nullValue())));
    }

    @Test
    public void verifyIsNoSystemModule() throws Exception
    {
        assertThat(descriptor.isSystemModule(), is(false));
    }

    @Test
    public void verifyIsEnabledByDefault() throws Exception
    {
        assertThat(descriptor.isEnabledByDefault(), is(true));
    }

    @Test
    public void verifyConditionsArePresent() throws Exception
    {
        assertThat(descriptor.getCondition(), is(not(nullValue())));
    }

    @Test
    public void verifyConditionsAreNotDefault() throws Exception
    {
        assertThat(descriptor.getCondition(), is(not(ConditionDescriptorFactory.DEFAULT_CONDITION)));
    }

}
