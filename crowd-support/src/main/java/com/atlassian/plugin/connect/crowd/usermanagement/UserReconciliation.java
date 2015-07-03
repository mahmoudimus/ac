package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Optional;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.model.user.UserTemplate;

public interface UserReconciliation
{
    Optional<UserTemplate> getFixes(User user, String requiredDisplayName, String requiredEmailAddress, boolean active);
}
