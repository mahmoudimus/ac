package com.atlassian.plugin.connect.testsupport;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.license.LicenseHandler;

import org.springframework.beans.factory.InitializingBean;

public class LicenseUpdater implements InitializingBean
{
    public static final String JIRA_5HR_LICENSE = "AAABSA0ODAoPeNqNkk9PAjEQxe98iibeSEq66BJD0kRlGyXCssBqxNtYBq1ZCmm7G/n2lt31wF+5znud+b2ZXqU5kiEY0g5JwLos7DJGkiglbRbcNJqtnkFwaqUjcMi3NcquabvjlQitNGq9FblD60imJGqLjW9loJWAcRpNDEvk3hznyw80o8WLRWM5DSpTb6UdSCeGoDIOks6xuAOXgbUKdEuulv6l17RDDVqi+FkrsylJkvCpanG8sSggy0tuvoDMM03RFGj6EX94e2a0N3sMaXA7fqfxdNbZYSmB7/8YiK9qlI6kCFuYQRUx3ayxNEbiVQxGiZh47WBm86J8SW7kF1jcX3AJVc/bC97c3e6Oc65KAhGnYpJM+lNRyf9s8fwtR+YTtLJVtnoj9ZXPwZ/+OqdiXXQDb1AFcmfyGu+gcO5KR7P8Al1rBkcwLAIUZd2lg4jtlAgaownBgh3/tuoqZoYCFArAEt3iX0ms3fW7Td0jKKA2uEGDX02g8";
    public static final String CONFLUENCE_5HR_LICENSE = "AAABQQ0ODAoPeNqNkt9LwzAQx9/9KwK+CRnruvowCOjWgMJ+lK0OfDzjTQNdOi5pcf+9aTpFtzr3mrv73vf7uVznFbIZEBskLOqP4mSURCxLczboR8Orm96EEJwuTQoORfPG+zEf3PpKilaR3jVF4dA6VmiFxuKVKs2mlwE5gzSHLQrfPK+2L0iLzZNFsoJH/mkG2jg0YBTKj52mfViRJQ+tACinaxSOqoPitJU/6l0h1UiPqRivJxOexvGSPw9Xkq/jcWPyMJTvdxispHItp4tMLn1N1lBUIZzYQOGN+7SlcX6x9N4KAYq/Yn0HrgBrNZieKre+J6tIvYPFYyLB5ILewGjbqno1g8q1lb9B/kIVev9Bc558B+pudN9xw+b7r5jsYJvlCE3ek0NcMPXzYB3sQ/kEf/tvztG95DydF/gEMVT7ZTAsAhQq9jnB+F36ooiQOZLODmekzaaTGwIUQV5sJ8Hp1+QjSCSfubtuc7o5FMQ=X02g0";
    
    private final ApplicationProperties applicationProperties;
    private final LicenseHandler licenseHandler;
    
    public LicenseUpdater(ApplicationProperties applicationProperties, LicenseHandler licenseHandler)
    {
        this.applicationProperties = applicationProperties;
        this.licenseHandler = licenseHandler;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        String license = null;
        String appName = applicationProperties.getDisplayName();
        
        if(appName.equalsIgnoreCase("jira"))
        {
            license = JIRA_5HR_LICENSE;
        }
        else if(appName.equalsIgnoreCase("confluence"))
        {
            license = CONFLUENCE_5HR_LICENSE;
        }
        
        if(null != license)
        {
            licenseHandler.setLicense(license);
        }
    }
}
