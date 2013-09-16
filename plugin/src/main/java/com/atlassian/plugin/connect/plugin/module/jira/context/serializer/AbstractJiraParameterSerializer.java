package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.context.ParameterDeserializer;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.google.common.base.Optional;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractJiraParameterSerializer<T, C extends ServiceResult> implements ParameterSerializer<T>, ParameterDeserializer<T>
{
    public static final String ID_FIELD_NAME = "id";
    public static final String KEY_FIELD_NAME = "key";
    private final UserManager userManager;
    private final String containerFieldName;
    private final ServiceLookup<C, T> serviceLookup;

    public AbstractJiraParameterSerializer(UserManager userManager, String containerFieldName, ServiceLookup<C, T> serviceLookup)
    {
        this.serviceLookup = serviceLookup;
        this.userManager = checkNotNull(userManager, "userManager is mandatory");
        this.containerFieldName = checkNotNull(containerFieldName);
    }

//    @Override
//    public Map<String, Object> serialize(final T issue)
//    {
//        return ImmutableMap.<String, Object>of(containerFieldName, ImmutableMap.of(
//                ID_FIELD_NAME, issue.getId(),
//                KEY_FIELD_NAME, issue.getKey()
//        ));
//    }

    @Override
    public Optional<T> deserialize(Map<String, Object> params, String username)
    {
        final Optional<Map> issueMap = getParam(params, containerFieldName, Map.class);
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

        final C serviceResult = id.isPresent() ? serviceLookup.lookupById(user, id.get().longValue()) :
                serviceLookup.lookupByKey(user, key.get());
        if (!serviceResult.isValid())
        {
            // TODO: Should this be an exception?
            return Optional.absent();
        }

        return Optional.of((T)serviceLookup.getItem(serviceResult));
    }

    private <C> Optional<C> getParam(Map<?, ?> params, String paramName, Class<C> type)
    {
        final Object o = params.get(paramName);
        if (o == null || !type.isInstance(o))
        {
            return Optional.absent();
        }
        else
        {
            return Optional.of((C) o);
        }
    }

    public static interface ServiceLookup<C extends ServiceResult, T>
    {
        C lookupById(User user, Long id);
        C lookupByKey(User user, String key);
        T getItem(C result);
    }
}
