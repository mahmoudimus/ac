package com.atlassian.plugin.connect.test;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static it.util.TestConstants.ADMIN_USERNAME;

public class LicenseStatusBannerHelper
{
    private static final String DISABLE_LICENSE_BANNER_PATH = "/rest/plugins/1.0/com.atlassian.support.stp-key/modules/stp-license-status-resources-key";
    private static final LicenseStatusBannerHelper INSTANCE = new LicenseStatusBannerHelper();
    private final AtomicBoolean licenseBannerRemoved = new AtomicBoolean(false);

    private LicenseStatusBannerHelper()
    {
    }

    public static LicenseStatusBannerHelper instance()
    {
        return INSTANCE;
    }

    public synchronized void execute(TestedProduct<WebDriverTester> product) throws IOException, AuthenticationException
    {
        if (!licenseBannerRemoved.get())
        {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPut request = new HttpPut(product.getProductInstance().getBaseUrl() + DISABLE_LICENSE_BANNER_PATH);
            request.setHeader("Content-Type", "application/vnd.atl.plugins.plugin.module+json");
            request.setEntity(new StringEntity(new JSONObject(ImmutableMap.<String, Object>of("enabled", "false")).toString()));
            request.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(ADMIN_USERNAME, ADMIN_USERNAME), Charsets.UTF_8.toString(), false));
            client.execute(request, new BasicResponseHandler());
            licenseBannerRemoved.set(true);
        }
    }
}