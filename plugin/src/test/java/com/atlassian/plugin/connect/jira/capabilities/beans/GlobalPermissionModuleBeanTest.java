package com.atlassian.plugin.connect.jira.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.GlobalPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class GlobalPermissionModuleBeanTest
{
    @Test
    public void producesCorrectJSON() throws IOException
    {
        GlobalPermissionModuleBean globalPermissionModuleBean = readTestFile();

        assertThat(globalPermissionModuleBean, equalTo(GlobalPermissionModuleBean.newGlobalPermissionModuleBean()
                .withKey("jira-global-permission")
                .withName(new I18nProperty("Name", "name.i18n"))
                .withDescription(new I18nProperty("Description", "description.i18n"))
                .withAnonymusAllowed(false)
                .build()));
    }

    private static GlobalPermissionModuleBean readTestFile() throws IOException
    {
        return ConnectModulesGsonFactory.getGson().fromJson(readAddonTestFile("globalPermission.json"), GlobalPermissionModuleBean.class);
    }
}