package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.DefaultConnectWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.descriptor.WebSectionModuleDescriptorFactoryForTests;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.AndCompositeCondition;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import com.atlassian.sal.api.user.UserManager;
import org.dom4j.dom.DOMElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean.newWebSectionBean;
import static com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers.ConditionMatchers.isCompositeConditionContaining;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class ConnectWebSectionModuleDescriptorFactoryTest
{
    private static final String CONDITION_CLASSNAME = Condition.class.getName();

    private interface PluginForTests extends Plugin, ContainerManagedPlugin {}

    private WebSectionModuleDescriptor descriptor;

    @Mock private PluginForTests plugin;
    @Mock private HostContainer hostContainer;
    @Mock private WebInterfaceManager webInterfaceManager;
    @Mock private UserManager userManager;
    @Mock private IFrameRenderer iFrameRenderer;
    @Mock private UrlValidator urlValidator;
    @Mock private ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    @Mock private WebFragmentHelper webFragmentHelper;
    @Mock private Condition condition;

    @Before
    public void beforeEachTest() throws Exception
    {
        ConnectWebSectionModuleDescriptorFactory webSectionFactory = new DefaultConnectWebSectionModuleDescriptorFactory(conditionModuleFragmentFactory, new WebSectionModuleDescriptorFactoryForTests(webInterfaceManager));
        when(plugin.getKey()).thenReturn("my-awesome-plugin");
        when(plugin.getName()).thenReturn("My Plugin™");

        when(conditionModuleFragmentFactory.createFragment(eq("my-awesome-plugin"), anyList()))
                .thenReturn(conditionElement());
        when(webInterfaceManager.getWebFragmentHelper()).thenReturn(webFragmentHelper);
        when(webFragmentHelper.loadCondition(eq(CONDITION_CLASSNAME), eq(plugin))).thenReturn(condition);
        when(condition.shouldDisplay(anyMap())).thenReturn(true);

        WebSectionModuleBean bean = newWebSectionBean()
                .withName(new I18nProperty("My Web Section", "my.websection"))
                .withKey("my-web-section")
                .withLocation("com.atlassian.jira.plugin.headernav.left.context")
                .withWeight(50)
                .withConditions(new SingleConditionBeanBuilder().withCondition("unconditional").build())
                .build();

        descriptor = webSectionFactory.createModuleDescriptor(plugin, bean);
        descriptor.enabled();
    }

    private DOMElement conditionElement()
    {
        DOMElement conditions = new DOMElement("conditions");
        conditions.addAttribute("type", "and");
        DOMElement condition = new DOMElement("condition");
        condition.addAttribute("class", CONDITION_CLASSNAME);
        conditions.appendChild(condition);
        return conditions;
    }

    @Test
    public void keyIsCorrect() throws Exception
    {
        assertThat(descriptor.getKey(), is("my-web-section"));
    }

    @Test
    public void completeKeyIsCorrect() throws Exception
    {
        assertThat(descriptor.getCompleteKey(), is("my-awesome-plugin:my-web-section"));
    }

    @Test
    public void locationIsCorrect()
    {
        assertThat(descriptor.getLocation(), is("com.atlassian.jira.plugin.headernav.left.context"));
    }

    @Test
    public void weightIsCorrect()
    {
        assertThat(descriptor.getWeight(), is(50));
    }

    @Test
    public void i18nKeyIsCorrect()
    {
        assertThat(descriptor.getI18nNameKey(), is("my.websection"));
    }

    @Test
    public void conditionIsCorrect()
    {
        assertThat(descriptor.getCondition(), isCompositeConditionContaining(AndCompositeCondition.class, condition));
    }
}
