package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;

public class AuthenticationBeanBuilder {
    private AuthenticationType type;
    private String publicKey;

    public AuthenticationBeanBuilder() {
        this.type = AuthenticationType.JWT;
        this.publicKey = "";
    }

    public AuthenticationBeanBuilder(AuthenticationBean defaultBean) {
        this.publicKey = defaultBean.getPublicKey();
        this.type = defaultBean.getType();
    }

    public AuthenticationBeanBuilder withType(AuthenticationType type) {
        this.type = type;
        return this;
    }

    public AuthenticationBeanBuilder withPublicKey(String key) {
        this.publicKey = key;
        return this;
    }

    public AuthenticationBean build() {
        return new AuthenticationBean(this);
    }
}
