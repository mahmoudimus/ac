package com.atlassian.plugin.connect.plugin.request.url;

import com.atlassian.plugin.connect.plugin.request.url.RelativeAddonUrl;
import com.atlassian.plugin.connect.plugin.request.url.RelativeAddonUrlConverter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RelativeAddonUrlConverterTest {
    private static final String URL = "/irwi?issue_id={issue.id}&project_key={project.key}&pid={project.id}";

    @Test
    public void prependsACContextToPath() {
        RelativeAddonUrlConverter urlConverter = new RelativeAddonUrlConverter();
        RelativeAddonUrl localUrl = urlConverter.addonUrlToLocalServletUrl("my-plugin", URL);
        assertThat(localUrl.getRelativeUri(), is("/plugins/servlet/ac/my-plugin" + URL));
    }
}
