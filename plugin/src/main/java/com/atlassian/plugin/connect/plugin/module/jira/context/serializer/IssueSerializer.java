package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.context.ParameterDeserializer;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.atlassian.jira.bc.issue.IssueService.IssueResult;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Serializes Issue objects.
 */
public class IssueSerializer implements ParameterSerializer<Issue>, ParameterDeserializer<Issue>
{

    public static final String ISSUE_FIELD_NAME = "issue";
    public static final String ID_FIELD_NAME = "id";
    public static final String KEY_FIELD_NAME = "key";
    private final IssueService issueService;
    private final UserManager userManager;

    public IssueSerializer(IssueService issueService, UserManager userManager) {

        this.issueService = checkNotNull(issueService, "issueService is mandatory");;
        this.userManager = checkNotNull(userManager, "userManager is mandatory");
    }
    @Override
    public Map<String, Object> serialize(final Issue issue)
    {
        return ImmutableMap.<String, Object>of(ISSUE_FIELD_NAME, ImmutableMap.of(
                ID_FIELD_NAME, issue.getId(),
                KEY_FIELD_NAME, issue.getKey()
        ));
    }

    @Override
    public Optional<Issue> deserialize(Map<String, Object> params, String username)
    {
        final Optional<Map> issueMap = getParam(params, ISSUE_FIELD_NAME, Map.class);
        if (!issueMap.isPresent())
        {
            return Optional.absent();
        }

        final Optional<Number> id = getParam(issueMap.get(), ID_FIELD_NAME, Number.class);
        Optional<String> key = Optional.absent();
        if (!id.isPresent())
        {
            key = getParam(issueMap.get(), KEY_FIELD_NAME, String.class);
            if (!key.isPresent())
            {
                return Optional.absent();
            }
        }

        final ApplicationUser appUser = userManager.getUserByName(username);
        if (appUser == null)
        {
            // TODO: Should this be an exception?
            return Optional.absent();
        }

        final User user = appUser.getDirectoryUser();

        final IssueResult issue = id.isPresent() ? issueService.getIssue(user, id.get().longValue()) :
                issueService.getIssue(user, key.get());
        if (!issue.isValid())
        {
            // TODO: Should this be an exception?
            return Optional.absent();
        }

        return Optional.of((Issue)issue.getIssue());
    }

    private <T> Optional<T> getParam(Map<?, ?> params, String paramName, Class<T> type)
    {
        final Object o = params.get(paramName);
        if (o == null || !type.isInstance(o))
        {
            return Optional.absent();
        }
        else
        {
            return Optional.of((T) o);
        }
    }


}
