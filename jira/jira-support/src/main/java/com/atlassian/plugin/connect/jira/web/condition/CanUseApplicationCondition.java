package com.atlassian.plugin.connect.jira.web.condition;

import java.util.Map;
import java.util.Optional;

import com.atlassian.application.api.ApplicationKey;
import com.atlassian.fugue.Either;
import com.atlassian.jira.application.ApplicationAuthorizationService;
import com.atlassian.jira.application.ApplicationKeys;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;

public class CanUseApplicationCondition implements Condition
{
    private Optional<ApplicationKey> key;

    private final ApplicationAuthorizationService applicationService;
    private final JiraAuthenticationContext authenticationContext;

    public CanUseApplicationCondition(ApplicationAuthorizationService applicationService, JiraAuthenticationContext authenticationContext)
    {
        this.applicationService = applicationService;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        String keyParameter = params.get("applicationKey");
        if (keyParameter == null) {
            throw new PluginParseException("\"applicationKey\" parameter is required in the can_use_application condition");
        }
        Either<String, ApplicationKey> applicationKey = ApplicationKeys.TO_APPLICATION_KEY.apply(nullToEmpty(keyParameter));

        key = applicationKey.fold(
                param -> { throw new PluginParseException(format("invalid application key: \"%s\"", param)); },
                Optional::of);
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return key.filter(this::isApplicationInstalled)
                .map(appKey -> applicationService.canUseApplication(authenticationContext.getLoggedInUser(), appKey))
                .orElse(false);
    }

    private boolean isApplicationInstalled(final ApplicationKey key) {
        return ApplicationKeys.CORE.equals(key) ||
                applicationService.isApplicationInstalledAndLicensed(key);
    }
}
