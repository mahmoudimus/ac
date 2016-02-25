package com.atlassian.plugin.connect.plugin.webhook;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.WebHookModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebHookScopeValidatorTest {

    private WebHookScopeValidator webHookScopeValidator;

    @Mock
    private WebHookScopeService webHookScopeService;

    @Before
    public void setUp() {
        webHookScopeValidator = new WebHookScopeValidator(webHookScopeService);
    }

    @Test
    public void shouldAcceptBeanWithoutWebhooks() throws ConnectModuleValidationException {
        webHookScopeValidator.validate(ConnectAddonBean.newConnectAddonBean().build(), new ArrayList<>());
    }

    @Test(expected = ConnectModuleValidationException.class)
    public void shouldRejectBeanWithoutRequiredScopesForWebhook() throws ConnectModuleValidationException {
        validateAddonBean(ScopeName.READ, Collections.<ScopeName>emptySet());
    }

    @Test(expected = ConnectModuleValidationException.class)
    public void shouldRejectBeanWithoutImpliedRequiredScopesForWebhook() throws ConnectModuleValidationException {
        validateAddonBean(ScopeName.WRITE, ImmutableSet.of(ScopeName.READ));
    }

    @Test
    public void shouldAcceptBeanWithImpliedRequiredScopesForWebhook() throws ConnectModuleValidationException {
        validateAddonBean(ScopeName.READ, ImmutableSet.of(ScopeName.WRITE));
    }

    @Test
    public void shouldAcceptBeanWithRequiredScopesForWebhook() throws ConnectModuleValidationException {
        validateAddonBean(ScopeName.ADMIN, ImmutableSet.of(ScopeName.ADMIN));
    }

    private void validateAddonBean(ScopeName requiredScope, Set<ScopeName> definedScopes) throws ConnectModuleValidationException {
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
