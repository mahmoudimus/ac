package com.atlassian.plugin.connect.jira.field.option.rest;

import java.util.Optional;

import com.atlassian.fugue.Either;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollections;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOption;
import com.atlassian.plugin.connect.jira.field.option.Json;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.sal.api.message.I18nResolver;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED;
import static com.atlassian.jira.util.ErrorCollections.validationError;

@JiraComponent
public class ConnectFieldOptionBeansFactory
{
    private final I18nResolver i18n;

    @Autowired
    public ConnectFieldOptionBeansFactory(final I18nResolver i18n)
    {
        this.i18n = i18n;
    }

    public Either<ErrorCollection, JsonNode> parseJson(String json)
    {
        return Json.parse(json)
                .<Either<ErrorCollection, JsonNode>>map(Either::right)
                .orElseGet(() -> Either.left(ErrorCollections.create(invalidJsonMessage(), VALIDATION_FAILED)));
    }

    public ConnectFieldOptionBean toBean(final ConnectFieldOption option)
    {
        return new ConnectFieldOptionBean(option.getId(), option.getValue().toString());
    }

    public Either<ErrorCollection, ConnectFieldOption> fromBean(final Integer expectedOptionId, ConnectFieldOptionBean bean)
    {
        Either<ErrorCollection, ConnectFieldOption> option = fromBean(bean);
        return option.isLeft() || expectedOptionId.equals(option.right().get().getId()) ?
                option :
                Either.left(validationError("id", i18n.getText("connect.issue.field.option.rest.id.inconsistent", expectedOptionId)));
    }

    public Either<ErrorCollection, ConnectFieldOption> fromBean(ConnectFieldOptionBean bean)
    {
        Optional<JsonNode> json = Json.parse(bean.getValue());
        if (json.isPresent())
        {
            if (bean.getId() != null)
            {
                return Either.right(ConnectFieldOption.of(bean.getId(), json.get()));
            }
            else
            {
                return Either.left(validationError("id", i18n.getText("connect.issue.field.option.rest.id.required")));
            }
        }
        else
        {
            return Either.left(validationError("value", invalidJsonMessage()));
        }
    }

    private String invalidJsonMessage()
    {
        return i18n.getText("connect.issue.field.option.rest.json.invalid");
    }
}
