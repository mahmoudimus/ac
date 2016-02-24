package com.atlassian.plugin.connect.test.common.at;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.common.at.descriptor.AddonDescriptorParser;
import com.atlassian.plugin.connect.test.common.client.AtlassianConnectRestClient;
import com.atlassian.plugin.connect.test.common.util.TestUser;

public class AcceptanceTestHelper {

    private final AtlassianConnectRestClient connectRestClient;
    private TestUser user;
    private String descriptorUrl;
    private AddonDescriptorParser addonDescriptorParser;

    public AcceptanceTestHelper(TestUser user, String descriptorUrl, TestedProduct product) {
        this.user = user;
        this.descriptorUrl = descriptorUrl;
        addonDescriptorParser = new AddonDescriptorParser(descriptorUrl);

        connectRestClient = new AtlassianConnectRestClient(
                product.getProductInstance().getBaseUrl(),
                user.getUsername(),
                user.getPassword());
    }

    public void installAddon() throws Exception {
        connectRestClient.install(descriptorUrl);
    }

    public String getAddonKey() {
        return addonDescriptorParser.getAddonKey();
    }

    public void uninstallAddon() throws Exception {
        connectRestClient.uninstall(addonDescriptorParser.getAddonKey());
    }
}
