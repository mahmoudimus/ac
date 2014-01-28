package com.atlassian.plugin.connect.test.plugin.util;

import com.atlassian.plugin.connect.modules.beans.*;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.mockito.ArgumentMatcher;

public class ConnectInstallationTestUtil
{
    public static final String SHARED_SECRET_FIELD_NAME = "sharedSecret";
    public static final String USER_KEY_FIELD_NAME = "userKey";

    public static ConnectAddonBean createBean(AuthenticationType authenticationType, String publicKey, String baseUrl)
    {
        return ConnectAddonBean.newConnectAddonBean()
                               .withAuthentication(AuthenticationBean.newAuthenticationBean()
                                                                     .withPublicKey(publicKey)
                                                                     .withType(authenticationType)
                                                                     .build())
                               .withBaseurl(baseUrl)
                               .withLifecycle(LifecycleBean.newLifecycleBean()
                                                           .withInstalled("/installed")
                                                           .build())
                               .withModule("webItems", WebItemModuleBean.newWebItemBean()
                                                                        .withUrl("/webItem")
                                                                        .withLocation("location")
                                       .withName(new I18nProperty("text", "key")) // leaving this out results in a null vs empty-string mismatch between original and serialized-then-deserialized beans
                                       .withTooltip(new I18nProperty("text", "key")) // leaving this out results in a null vs empty-string mismatch between original and serialized-then-deserialized beans
                                       .withIcon(IconBean.newIconBean()
                                                         .withWidth(16)
                                                         .withHeight(16)
                                               .withUrl("/icon") // leaving this out results in a null vs empty-string mismatch between original and serialized-then-deserialized beans
                                               .build())
                                       .build())
                               .withDescription("description")
                               .withKey("my add-on key")
                               .withLifecycle(LifecycleBean.newLifecycleBean()
                                                           .withInstalled("/installed")
                                                           .build())
                               .withName("name")
                               .build();
    }

    /**
     * @return a Mockito matcher that parses a JSON string for a non-null {@link #SHARED_SECRET_FIELD_NAME} field
     */
    public static ArgumentMatcher<String> hasSharedSecret()
    {
        return new ArgumentMatcher<String>()
        {
            @Override
            public boolean matches(Object actual)
            {
                return actual instanceof String
                        && !StringUtils.isEmpty((String) actual)
                        && new JsonParser().parse((String) actual).getAsJsonObject().has(SHARED_SECRET_FIELD_NAME);
            }
        };
    }

    /**
     * @return a Mockito matcher that parses a JSON string for a non-null {@link #USER_KEY_FIELD_NAME} field
     */
    public static ArgumentMatcher<String> hasUserKey()
    {
        return new ArgumentMatcher<String>()
        {
            @Override
            public boolean matches(Object actual)
            {
                return actual instanceof String
                        && !StringUtils.isEmpty((String) actual)
                        && new JsonParser().parse((String) actual).getAsJsonObject().has(USER_KEY_FIELD_NAME);
            }
        };
    }
}
