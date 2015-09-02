package at.marketplace;

public class MarketplaceAddonConstants
{
    public static final String WEB_ITEM_TEXT = "AC Action";

    private static final String TEST_ADDON_VERSION = "0001";
    private static final long ATLASSIAN_LABS_ID = 33202;

    public static final com.atlassian.connect.acceptance.test.ConnectAddonRepresentation ADD_ON_REPRESENTATION = com.atlassian.connect.acceptance.test.ConnectAddonRepresentation.builder()
            .withDescriptorUrl("https://bitbucket.org/atlassianlabs/ac-acceptance-test-addon/raw/addon-" + TEST_ADDON_VERSION + "/atlassian-connect.json")
            .withLogo("https://bitbucket.org/atlassianlabs/ac-acceptance-test-addon/raw/addon-" + TEST_ADDON_VERSION + "/simple-logo.png")
            .withKey("com.atlassian.connect.acceptance.test.addon." + TEST_ADDON_VERSION)
            .withName("Connect Test Addon v" + TEST_ADDON_VERSION) // Must be < 40 characters
            .withVendorId(ATLASSIAN_LABS_ID)
            .build();
}
