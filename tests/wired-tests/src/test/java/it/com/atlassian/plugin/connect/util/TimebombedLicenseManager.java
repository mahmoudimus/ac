package it.com.atlassian.plugin.connect.util;

import java.io.IOException;
import java.io.InputStream;

import com.atlassian.sal.api.license.LicenseHandler;

import org.apache.commons.io.IOUtils;

public class TimebombedLicenseManager {
    public static final int LICENSED_ADDON_KEY_COUNT = 100;
    public static final int UNLICENSED_ADDON_KEY_COUNT = 10;
    public static final String TIMEBOMB_100_PLUGIN_LICENSE_PATH = "testfiles.licenses/timebomb-ac-test-json-0..99.license";

    private static int licensedAddonCount = 0, unlicensedAddonCount = 0;

    private final LicenseHandler licenseHandler;

    public TimebombedLicenseManager(LicenseHandler licenseHandler) {
        this.licenseHandler = licenseHandler;
    }

    public void setLicense() throws IOException {
        InputStream licenseResource = getClass().getClassLoader().getResourceAsStream(TIMEBOMB_100_PLUGIN_LICENSE_PATH);
        String license = IOUtils.lineIterator(licenseResource, "UTF-8").nextLine();
        licenseHandler.setLicense(license);
    }

    public String generateLicensedAddonKey() {
        // We license the specific add-on keys that this method may generate (ac-test-json-0 to ac-test-json-99)
        // using this license: /tests/wired-tests/src/test/resources/testfiles.licenses/timebomb-ac-test-json-0..99.license
        // This is necessary because changes for https://ecosystem.atlassian.net/browse/UPM-4851 mean
        // the wildcard license isn't returned if we ask for a specific add-on's license
        //
        // A range of 100 licenses, combined with our teardown method that uninstalls the add-on, makes conflicts very unlikely.

        if (licensedAddonCount >= LICENSED_ADDON_KEY_COUNT) {
            throw new IllegalStateException("Ran out of licensed test add-ons");
        }

        return "ac-test-json-" + licensedAddonCount++;
    }

    public String generateUnlicensedAddonKey() {
        // We license the specific add-on keys that this method may generate (ac-test-json-0 to ac-test-json-9)
        // using this license: /tests/wired-tests/src/test/resources/testfiles.licenses/timebomb-ac-test-json-0..99.license
        // This is necessary because changes for https://ecosystem.atlassian.net/browse/UPM-4851 mean
        // the wildcard license isn't returned if we ask for a specific add-on's license
        //
        // A range of 100 licenses, combined with our teardown method that uninstalls the add-on, makes conflicts very unlikely.

        if (unlicensedAddonCount >= UNLICENSED_ADDON_KEY_COUNT) {
            throw new IllegalStateException("Ran out of licensed test add-ons");
        }

        return "ac-test-json-disabled-" + unlicensedAddonCount++;
    }
}
