package com.atlassian.plugin.connect.testsupport;

import com.atlassian.jira.license.JiraLicenseManager;

import org.springframework.beans.factory.InitializingBean;

public class JiraLicenseUpdater implements InitializingBean
{
    public static final String JIRA_5HR_LICENSE = "AAABSA0ODAoPeNqNkk9PAjEQxe98iibeSEq66BJD0kRlGyXCssBqxNtYBq1ZCmm7G/n2lt31wF+5znud+b2ZXqU5kiEY0g5JwLos7DJGkiglbRbcNJqtnkFwaqUjcMi3NcquabvjlQitNGq9FblD60imJGqLjW9loJWAcRpNDEvk3hznyw80o8WLRWM5DSpTb6UdSCeGoDIOks6xuAOXgbUKdEuulv6l17RDDVqi+FkrsylJkvCpanG8sSggy0tuvoDMM03RFGj6EX94e2a0N3sMaXA7fqfxdNbZYSmB7/8YiK9qlI6kCFuYQRUx3ayxNEbiVQxGiZh47WBm86J8SW7kF1jcX3AJVc/bC97c3e6Oc65KAhGnYpJM+lNRyf9s8fwtR+YTtLJVtnoj9ZXPwZ/+OqdiXXQDb1AFcmfyGu+gcO5KR7P8Al1rBkcwLAIUZd2lg4jtlAgaownBgh3/tuoqZoYCFArAEt3iX0ms3fW7Td0jKKA2uEGDX02g8";
    private final JiraLicenseManager licenseManager;

    public JiraLicenseUpdater(JiraLicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        licenseManager.setLicenseNoEvent(JIRA_5HR_LICENSE);
    }
}
