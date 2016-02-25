package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.plugin.connect.plugin.request.ConnectHttpClientFactory;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectAddonManagerTest {
    private static final String SIMPLE_ADDON_BASE_URL = "https://example.test.com";

    @Mock
    private ConnectHttpClientFactory connectHttpClientFactory;

    @Mock
    private UserManager userManager;

    @Mock
    private UserProfile userProfile;

    @InjectMocks
    private ConnectAddonManager sut;

    @Test
    public void testGetUri__relative_path_without_query_params_without_user() {
        final URI uri = sut.getURI(SIMPLE_ADDON_BASE_URL, "/path/to/install");

        assertThat(uri.getPath(), is("/path/to/install"));
        final List<NameValuePair> queryParams = URLEncodedUtils.parse(uri, "UTF-8");
        assertTrue(queryParams.isEmpty());
    }

    @Test
    public void testGetUri__relative_path_without_query_params_with_user() {
        when(userProfile.getUserKey()).thenReturn(new UserKey("test-user-key"));
        when(userManager.getRemoteUser()).thenReturn(userProfile);

        final URI uri = sut.getURI(SIMPLE_ADDON_BASE_URL, "/path/to/install");

        assertThat(uri.getPath(), is("/path/to/install"));
        final List<NameValuePair> queryParams = URLEncodedUtils.parse(uri, "UTF-8");
        assertThat(queryParams, not(hasItem(matchName("withParam"))));
        assertThat(queryParams, hasItem(equalTo(new BasicNameValuePair("user_key", "test-user-key"))));
    }

    @Test
    public void testGetUri__relative_path_with_query_params_without_user() {
        final URI uri = sut.getURI(SIMPLE_ADDON_BASE_URL, "/path/to/install?withParam=true");

        assertThat(uri.getPath(), is("/path/to/install"));
        final List<NameValuePair> queryParams = URLEncodedUtils.parse(uri, "UTF-8");
        assertThat(queryParams, hasItem(equalTo(new BasicNameValuePair("withParam", "true"))));
        assertThat(queryParams, not(hasItem(matchName("user_key"))));
    }

    @Test
    public void testGetUri__relative_path_with_query_params_and_user_key() {
        when(userProfile.getUserKey()).thenReturn(new UserKey("test-user-key"));
        when(userManager.getRemoteUser()).thenReturn(userProfile);

        final URI uri = sut.getURI(SIMPLE_ADDON_BASE_URL, "/path/to/install?withParam=true");

        assertThat(uri.getPath(), is("/path/to/install"));
        final List<NameValuePair> queryParams = URLEncodedUtils.parse(uri, "UTF-8");
        assertThat(queryParams, hasItem(equalTo(new BasicNameValuePair("withParam", "true"))));
        assertThat(queryParams, hasItem(equalTo(new BasicNameValuePair("user_key", "test-user-key"))));
    }

    private static Matcher<NameValuePair> matchName(final String name) {
        return new BaseMatcher<NameValuePair>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("nameMatcher");
                description.appendText("[name=").appendText(name).appendText("]");
            }

            @Override
            public boolean matches(Object o) {
                if (!(o instanceof NameValuePair)) return false;
                final NameValuePair nvp = (NameValuePair) o;
                return nvp.getName().equals(name);
            }
        };
    }
}