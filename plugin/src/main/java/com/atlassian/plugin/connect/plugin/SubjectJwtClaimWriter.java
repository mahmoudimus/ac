package com.atlassian.plugin.connect.plugin;

import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.jwt.writer.JwtClaimWriter;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JWT claim provider that injects a "sub" claim and a context user claim when tokens are generated
 */
@Component
@ExportAsService(JwtClaimWriter.class)
public class SubjectJwtClaimWriter implements JwtClaimWriter
{
    private final UserManager userManager;

    @Autowired
    public SubjectJwtClaimWriter(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void write(@Nonnull JwtJsonBuilder builder)
    {
        UserProfile remoteUser = userManager.getRemoteUser();

        Map<String, Object> jwtContextClaim = Maps.newHashMap();

        if (remoteUser != null)
        {
            String userKeyValue = remoteUser.getUserKey().getStringValue();

            Map<String, String> jwtContextUser = ImmutableMap.of(
                    "userKey", userKeyValue,
                    "username", remoteUser.getUsername(),
                    "displayName", remoteUser.getFullName()
            );

            jwtContextClaim.put("user", jwtContextUser);
            builder.subject(userKeyValue);
        }

        builder.claim("context", jwtContextClaim);
    }
}
