package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes Version objects.
 */
public class VersionSerializer extends AbstractJiraParameterSerializer<Version, VersionService.VersionResult>
{

    public static final String VERSION_FIELD_NAME = "version";

    public VersionSerializer(final VersionService versionService, UserManager userManager)
    {
        super(userManager, VERSION_FIELD_NAME, new ServiceLookup<VersionService.VersionResult, Version>()
        {
            @Override
            public VersionService.VersionResult lookupById(User user, Long id)
            {
                return versionService.getVersionById(user, id);
            }

            @Override
            public VersionService.VersionResult lookupByKey(User user, String key)
            {
                throw new IllegalStateException("Cannot lookup version by key");
            }

            @Override
            public Version getItem(VersionService.VersionResult result)
            {
                return result.getVersion();
            }
        }, false);
    }

    @Override
    public Map<String, Object> serialize(final Version version)
    {
        return ImmutableMap.<String, Object>of(VERSION_FIELD_NAME, ImmutableMap.of(ID_FIELD_NAME, version.getId()));
    }

}
