package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.WebHookModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.WebHookScopeService;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebHookScopeValidator;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebHookScopeValidatorTest
{

    private WebHookScopeValidator webHookScopeValidator;

    @Mock
    private WebHookScopeService webHookScopeService;

    @Before
    public void setUp()
    {
        webHookScopeValidator = new WebHookScopeValidator(webHookScopeService);
    }

    @Test
    public void shouldAcceptBeanWithoutWebhooks()
    {
        webHookScopeValidator.validate(ConnectAddonBean.newConnectAddonBean().build(), new ArrayList<>());
    }

    @Test(expected = InvalidDescriptorException.class)
    public void shouldRejectBeanWithoutRequiredScopesForWebhook()
    {
        validateAddonBean(ScopeName.READ, Collections.<ScopeName>emptySet());
    }

    @Test(expected = InvalidDescriptorException.class)
    public void shouldRejectBeanWithoutImpliedRequiredScopesForWebhook()
    {
        validateAddonBean(ScopeName.WRITE, ImmutableSet.of(ScopeName.READ));
    }

    @Test
    public void shouldAcceptBeanWithImpliedRequiredScopesForWebhook()
    {
        validateAddonBean(ScopeName.READ, ImmutableSet.of(ScopeName.WRITE));
    }

    @Test
    public void shouldAcceptBeanWithRequiredScopesForWebhook()
    {
        validateAddonBean(ScopeName.ADMIN, ImmutableSet.of(ScopeName.ADMIN));
    }

    private void validateAddonBean(ScopeName requiredScope, Set<ScopeName> definedScopes)
    {
        String event = "fooCreated";
        WebHookModuleBean webHookModuleBean = new WebHookModuleBeanBuilder().withEvent(event).build();
        ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
                .withModules("webhooks", webHookModuleBean)
                .withScopes(definedScopes)
                .build();

        when(webHookScopeService.getRequiredScope(event)).thenReturn(requiredScope);

        List<WebHookModuleBean> beanList = Collections.singletonList(webHookModuleBean);
        webHookScopeValidator.validate(addonBean, beanList);
    }
}
