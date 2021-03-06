package com.atlassian.plugin.connect.jira.search;

import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestURLHandler;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptorImpl;
import com.atlassian.jira.plugin.webfragment.conditions.UserLoggedInCondition;
import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactoryImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.web.condition.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.api.web.iframe.ConnectUriFactory;
import com.atlassian.plugin.connect.jira.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.annotation.ConvertToWiredTest;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class SearchRequestViewModuleDescriptorFactoryTest {
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
    private WebFragmentHelper webFragmentHelper;
    @Mock
    private ConnectUriFactory connectUriFactory;
    @Mock
    private ConnectConditionDescriptorFactory connectConditionDescriptorFactory;
    @Mock
    private Condition condition;

    private SearchRequestViewModuleDescriptorImpl descriptor;

    private ConnectAddonBean addon;

    @Before
    public void beforeEachTest() throws Exception {
        this.addon = newConnectAddonBean().withKey("my-plugin").build();

        when(plugin.getKey()).thenReturn("my-plugin");
        when(plugin.<UserLoggedInCondition>loadClass(eq("com.atlassian.jira.plugin.webfragment.conditions.UserLoggedInCondition"), any(Class.class)))
                .thenReturn(UserLoggedInCondition.class);

        ConditionDescriptorFactory conditionDescriptorFactory = new ConditionDescriptorFactoryImpl(webFragmentHelper);

        ConditionModuleFragmentFactory conditionModuleFragmentFactory = mock(ConditionModuleFragmentFactory.class);
        when(conditionModuleFragmentFactory.createFragment(anyString(), anyListOf(ConditionalBean.class))).thenReturn(new DOMElement("conditions"));

        when(webFragmentHelper.loadCondition(eq(UserLoggedInCondition.class.getCanonicalName()), eq(plugin))).thenReturn(new UserLoggedInCondition());

        when(componentAccessor.getComponent(SearchRequestURLHandler.class)).thenReturn(urlHandler);
        when(componentAccessor.getComponent(ConditionDescriptorFactory.class)).thenReturn(conditionDescriptorFactory);

        when(connectConditionDescriptorFactory.retrieveCondition(any(Plugin.class), any(Element.class))).thenReturn(condition);

        SearchRequestViewModuleDescriptorFactory factory = new SearchRequestViewModuleDescriptorFactory(
                authenticationContext,
                conditionModuleFragmentFactory,
                connectConditionDescriptorFactory,
                applicationProperties,
                searchRequestViewBodyWriterUtil,
                templateRenderer,
                connectUriFactory,
                componentAccessor);

        SearchRequestViewModuleBean bean = SearchRequestViewModuleBean.newSearchRequestViewModuleBean()
                .withWeight(55)
                .withUrl("http://search.example.com")
                .withName(new I18nProperty("A Search Request View", null))
                .withKey("a-search-request-view")
                .withDescription(new I18nProperty("A description", null))
                .withConditions(
                        newSingleConditionBean().withCondition("user_is_logged_in").build())
                .build();

        this.descriptor = (SearchRequestViewModuleDescriptorImpl) factory.createModuleDescriptor(bean, addon, plugin);
        this.descriptor.enabled();
    }

    @Test
    public void verifyCompleteKeyIsCorrect() {
        assertThat(descriptor.getCompleteKey(), is("my-plugin:" + addonAndModuleKey("my-plugin", "a-search-request-view")));
    }

    @Test
    public void verifyNameIsSet() throws Exception {
        assertThat(descriptor.getName(), is("A Search Request View"));
    }

    @Test
    public void verifyDescriptionIsSet() throws Exception {
        assertThat(descriptor.getDescription(), is("A description"));
    }

    @Test
    public void verifyWeightIsSet() throws Exception {
        assertThat(descriptor.getOrder(), is(55));
    }

    @Test
    public void verifyContentType() throws Exception {
        assertThat(descriptor.getContentType(), is("text/html"));
    }

    @Test
    public void verifyFileExtension() throws Exception {
        assertThat(descriptor.getFileExtension(), is("html"));
    }

    @Test
    public void verifySearchRequestViewIsCreated() throws Exception {
        assertThat(descriptor.getSearchRequestView(), is(not(nullValue())));
    }

    @Test
    public void verifyIsNoSystemModule() throws Exception {
        assertThat(descriptor.isSystemModule(), is(false));
    }

    @Test
    public void verifyIsEnabledByDefault() throws Exception {
        assertThat(descriptor.isEnabledByDefault(), is(true));
    }

    @Test
    public void verifyConditionsArePresent() throws Exception {
        assertThat(descriptor.getCondition(), is(not(nullValue())));
    }

    @Test
    public void verifyConditionsAreNotDefault() throws Exception {
        assertThat(descriptor.getCondition(), is(not(ConditionDescriptorFactory.DEFAULT_CONDITION)));
    }

}
