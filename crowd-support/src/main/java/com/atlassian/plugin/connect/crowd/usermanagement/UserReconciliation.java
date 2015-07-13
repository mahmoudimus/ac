package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.model.user.UserTemplate;

import com.google.common.base.Optional;

public interface UserReconciliation
{
    Optional<UserTemplate> getFixes(User user, String requiredDisplayName, String requiredEmailAddress, boolean active);
}
