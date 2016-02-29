package com.atlassian.plugin.connect.plugin.auth;

import com.atlassian.jwt.JwtConstants;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AuthenticationMethodTest {
    @Test
    public void jwtAuthMethodHasExpectedToStringValue() {
        assertThat(AuthenticationMethod.JWT.toString(), is(JwtConstants.AppLinks.JWT_AUTH_METHOD_NAME));
    }
}
