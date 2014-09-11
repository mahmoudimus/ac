package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.report.ConnectReportModuleDescriptor;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.TransformableResource;
import com.atlassian.plugin.webresource.transformer.TransformerUrlBuilder;
import com.atlassian.plugin.webresource.transformer.UrlReadingWebResourceTransformer;
import com.atlassian.plugin.webresource.url.UrlBuilder;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class ReportModuleCssTransformerTest
{

    public static final String MD1_KEY = "test_addon1_test-report1";
    public static final String MD2_KEY = "test_addon1_test-report2";
    public static final String MD3_KEY = "test_addon2_test-report1";
    public static final String MD1_THUMBNAIL = "/thumbnail.png";
    public static final String MD2_THUMBNAIL = "/second-thumbnail.png";

    @Mock
    private PluginAccessor pluginAccessor;

    private ReportModuleCssTransformer reportModuleCssTransformer;

    @Before
    public void setUp() throws Exception
    {
        final List<ConnectReportModuleDescriptor.ModuleDescriptorImpl> moduleDescriptors = ImmutableList.of(
                createModuleDescriptor(MD1_KEY, MD1_THUMBNAIL),
                createModuleDescriptor(MD2_KEY, MD2_THUMBNAIL),
                createModuleDescriptor(MD3_KEY, null)
        );

        when(pluginAccessor.getEnabledModuleDescriptorsByClass(ConnectReportModuleDescriptor.ModuleDescriptorImpl.class))
                .thenReturn(moduleDescriptors);

        reportModuleCssTransformer = new ReportModuleCssTransformer(pluginAccessor);
    }

    @Test
    public void testMakeUrlBuilder() throws Exception
    {
        final TransformerUrlBuilder transformerUrlBuilder = reportModuleCssTransformer.makeUrlBuilder(null);

        assertThat(transformerUrlBuilder, instanceOf(ReportModuleCssTransformer.ReportModulesUriBuilder.class));
    }

    @Test
    public void testUrlBuilder() throws Exception
    {
        final UrlBuilder urlBuilder = mock(UrlBuilder.class);
        final TransformerUrlBuilder transformerUrlBuilder = new ReportModuleCssTransformer.ReportModulesUriBuilder(pluginAccessor);
        transformerUrlBuilder.addToUrl(urlBuilder);

        final Object expectedHash = new HashCodeBuilder()
                .append(MD1_KEY)
                .append(MD2_KEY)
                .append(MD3_KEY)
                .build();

        verify(urlBuilder).addToHash(anyString(), eq(expectedHash));
    }

    @Test
    public void testMakeResourceTransformer() throws Exception
    {
        final DownloadableResource downloadableResource = mock(DownloadableResource.class);
        final TransformableResource transformableResource = mock(TransformableResource.class);
        doReturn(downloadableResource).when(transformableResource).nextResource();

        final UrlReadingWebResourceTransformer urlReadingWebResourceTransformer = reportModuleCssTransformer.makeResourceTransformer(null);
        final DownloadableResource transformer = urlReadingWebResourceTransformer.transform(transformableResource, null);

        assertThat(transformer, instanceOf(ReportModuleCssTransformer.ThumbnailCssClassesGenerator.class));
    }

    @Test
    public void testResourceTransformerThatGenerateCssClassForFirstPlugin() throws Exception
    {
        final String generatedCss = generateCss();

        assertThat(generatedCss, containsString(MD1_KEY));
        assertThat(generatedCss, containsString(MD1_THUMBNAIL));
    }

    @Test
    public void testResourceTransformerThatGenerateCssClassForSecondPlugin() throws Exception
    {
        final String generatedCss = generateCss();

        assertThat(generatedCss, containsString(MD2_KEY));
        assertThat(generatedCss, containsString(MD2_THUMBNAIL));
    }

    @Test
    public void testResourceTransformerThatNotGenerateCssClassForPluginWithEmptyThumbnailUrl() throws Exception
    {
        final String generatedCss = generateCss();

        assertThat(generatedCss, not(containsString(MD3_KEY)));
    }
    private String generateCss() {
        final ReportModuleCssTransformer.ThumbnailCssClassesGenerator thumbnailCssClassesGenerator = new ReportModuleCssTransformer.ThumbnailCssClassesGenerator(null, pluginAccessor);
        return thumbnailCssClassesGenerator.transform("").toString();
    }


    private ConnectReportModuleDescriptor.ModuleDescriptorImpl createModuleDescriptor(final String descriptorKey, final String thumbnailUrl)
    {
        final ConnectReportModuleDescriptor.ModuleDescriptorImpl moduleDescriptor = mock(ConnectReportModuleDescriptor.ModuleDescriptorImpl.class);
        doReturn(descriptorKey).when(moduleDescriptor).getKey();
        doReturn(thumbnailUrl).when(moduleDescriptor).getThumbnailUrl();
        return moduleDescriptor;
    }
}