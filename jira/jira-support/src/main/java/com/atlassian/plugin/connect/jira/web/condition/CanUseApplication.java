package com.atlassian.plugin.connect.jira.web.condition;

import com.atlassian.application.api.ApplicationKey;
import com.atlassian.fugue.Either;
import com.atlassian.jira.application.ApplicationAuthorizationService;
import com.atlassian.jira.application.ApplicationKeys;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;
import java.util.Optional;

public class   CanUseApplication implements Condition {

    private Optional<ApplicationKey> key;

    private final ApplicationAuthorizationService applicationService;
    private final JiraAuthenticationContext authenticationContext;

    public CanUseApplication(ApplicationAuthorizationService applicationService, JiraAuthenticationContext authenticationContext) {
        this.applicationService = applicationService;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException {
        Either<String, ApplicationKey> applicationKey = ApplicationKeys.TO_APPLICATION_KEY.apply(params.get("applicationKey"));
        if (applicationKey.isRight()) {
            key = Optional.of(applicationKey.right().get());
        } else {
            key = Optional.empty();
        }
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        return key.filter(applicationService::isApplicationInstalledAndLicensed)
                .map(appKey -> applicationService.canUseApplication(authenticationContext.getLoggedInUser(), appKey))
                .orElse(false);
    }
}
