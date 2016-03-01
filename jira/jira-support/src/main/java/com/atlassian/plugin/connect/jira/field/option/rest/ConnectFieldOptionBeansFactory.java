package com.atlassian.plugin.connect.jira.field.option.rest;

import com.atlassian.fugue.Either;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOption;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOptionScope;
import com.atlassian.plugin.connect.jira.util.Json;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.sal.api.message.I18nResolver;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

import static com.atlassian.jira.util.ErrorCollections.validationError;

@JiraComponent
public class ConnectFieldOptionBeansFactory {
    private final I18nResolver i18n;

    @Autowired
    public ConnectFieldOptionBeansFactory(final I18nResolver i18n) {
        this.i18n = i18n;
    }

    public ConnectFieldOptionBean toBean(final ConnectFieldOption option) {
        return new ConnectFieldOptionBean(option.getId(), option.getValue(), toBean(option.getScope()));
    }

    private ConnectFieldOptionScopeBean toBean(final ConnectFieldOptionScope scope) {
        return new ConnectFieldOptionScopeBean(scope.getProjectId().orElse(null));
    }

    public Either<ErrorCollection, ConnectFieldOption> fromBean(final Integer expectedOptionId, ConnectFieldOptionBean bean) {
        Either<ErrorCollection, ConnectFieldOption> option = fromBean(bean);
        return option.isLeft() || expectedOptionId.equals(option.right().get().getId()) ?
                option :
                Either.left(validationError("id", i18n.getText("connect.issue.field.option.rest.id.inconsistent", expectedOptionId)));
    }

    public Either<ErrorCollection, ConnectFieldOption> fromBean(ConnectFieldOptionBean bean) {
        return jsonFromBean(bean).right().flatMap(json -> {
            if (bean.getId() != null) {
                return Either.right(ConnectFieldOption.of(bean.getId(), json).withScope(fromBean(bean.getScope())));
            } else {
                return Either.left(validationError("id", i18n.getText("connect.issue.field.option.rest.id.required")));
            }
        });
    }

    public ConnectFieldOptionScope fromBean(@Nullable final ConnectFieldOptionScopeBean scope) {
        return scope == null ? ConnectFieldOptionScope.GLOBAL :
                ConnectFieldOptionScope.builder()
                        .setProjectId(scope.getProjectId())
                        .build();
    }

    public Either<ErrorCollection, JsonNode> jsonFromBean(ConnectFieldOptionBean bean) {
        if (bean.getValue() != null) {
            return Either.right(Json.toJsonNode(bean.getValue()));
        } else {
            return Either.left(validationError("value", i18n.getText("connect.issue.field.option.rest.value.required")));
        }
    }
}
