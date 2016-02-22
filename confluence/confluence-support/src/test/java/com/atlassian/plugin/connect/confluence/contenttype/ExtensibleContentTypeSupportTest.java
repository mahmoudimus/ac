package com.atlassian.plugin.connect.confluence.contenttype;

import java.util.Set;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.content.Space;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.ContentTypeApiSupport;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.elasticsearch.shaded.google.common.collect.Sets;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype.APISupportBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeModuleBeanBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.confluence.api.model.content.ContentType.COMMENT;
import static com.atlassian.confluence.api.model.content.ContentType.PAGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExtensibleContentTypeSupportTest
{
    @Rule private ExpectedException exceptions = ExpectedException.none();
    @Mock private CustomContentApiSupportParams customContentApiSupportParams;
    @Mock private ApiSupportProvider apiSupportProvider;

    @Test
    public void shouldRestrictSpaceAsContainer() {
        ExtensibleContentTypeModuleBean bean1 = buildBean(Sets.newHashSet(), Sets.newHashSet());
        ExtensibleContentTypeModuleBean bean2 = buildBean(Sets.newHashSet("global"), Sets.newHashSet());

        Space space = Space.builder().build();
        Content content = Content.builder().type(ContentType.valueOf("extensible")).space(space).container(space).build();

        assertThat(buildContentTypeSupport("extensible", bean1).validateCreate(content).isValid(), is(false));
        assertThat(buildContentTypeSupport("extensible", bean2).validateCreate(content).isValid(), is(true));
    }

    @Test
    public void shouldRestrictContentAsContainer() {
        ContentType extensible = ContentType.valueOf("extensible");

        ContentTypeApiSupport pageBinding = mock(ContentTypeApiSupport.class);
        when(pageBinding.getHandledType()).thenReturn(PAGE);
        when(pageBinding.supportsChildrenOfType(extensible)).thenReturn(true);

        ContentTypeApiSupport commentBinding = mock(ContentTypeApiSupport.class);
        when(commentBinding.getHandledType()).thenReturn(COMMENT);
        when(commentBinding.supportsChildrenOfType(extensible)).thenReturn(true);

        when(apiSupportProvider.getForType(PAGE)).thenReturn(pageBinding);
        when(apiSupportProvider.getForType(COMMENT)).thenReturn(commentBinding);

        ExtensibleContentTypeModuleBean bean = buildBean(Sets.newHashSet("page"), Sets.newHashSet());

        Space space = Space.builder().build();
        Content contentWithPageAsContainer = Content.builder().type(extensible).space(space).container(Content.builder(ContentType.PAGE).build()).build();
        Content contentWithCommentAsContainer = Content.builder().type(extensible).space(space).container(Content.builder(ContentType.COMMENT).build()).build();

        assertThat(buildContentTypeSupport("extensible", bean).validateCreate(contentWithPageAsContainer).isValid(), is(true));
        assertThat(buildContentTypeSupport("extensible", bean).validateCreate(contentWithCommentAsContainer).isValid(), is(false));
    }

    @Test
    public void shouldRestrictContainedType() {
        ContentType extensible = ContentType.valueOf("extensible");
        ExtensibleContentTypeModuleBean bean = buildBean(Sets.newHashSet("extensible"), Sets.newHashSet("extensible"));

        ExtensibleContentTypeSupport extensibleTypeSupport = buildContentTypeSupport("extensible", bean);
        when(apiSupportProvider.getForType(extensible)).thenReturn(extensibleTypeSupport);


        Space space = Space.builder().build();
        Content content = Content.builder().type(extensible).space(space).container(space).build();
        Content childContent = Content.builder().type(extensible).space(space).container(content).build();

        assertThat(extensibleTypeSupport.validateCreate(childContent).isValid(), is(true));
    }

    private ExtensibleContentTypeSupport buildContentTypeSupport(String contentTypeKey, ExtensibleContentTypeModuleBean bean) {
        return new ExtensibleContentTypeSupport(
                contentTypeKey,
                bean.getApiSupport().getSupportedContainerTypes(),
                bean.getApiSupport().getSupportedContainedTypes(),
                customContentApiSupportParams,
                apiSupportProvider);
    }

    private ExtensibleContentTypeModuleBean buildBean(Set<String> supportedContainer, Set<String> supportedContained) {
        return new ExtensibleContentTypeModuleBeanBuilder()
                .withAPISupport(new APISupportBeanBuilder()
                        .withSupportedContainerTypes(supportedContainer)
                        .withSupportedContainedTypes(supportedContained)
                        .build())
                .build();
    }
}
