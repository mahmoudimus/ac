package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.model.user.UserTemplate;

import com.google.common.base.Optional;

import org.springframework.stereotype.Component;

@Component
public class UserReconciliationImpl implements UserReconciliation
{
    @Override
    public Optional<UserTemplate> getFixes(User user, String requiredDisplayName, String requiredEmailAddress, boolean active)
    {
        if (user.getEmailAddress().equals(requiredEmailAddress) && user.getDisplayName().equals(requiredDisplayName) && user.isActive() == active)
        {
            return Optional.absent();
        }

        UserTemplate userTemplate = new UserTemplate(user);
        userTemplate.setEmailAddress(requiredEmailAddress);
        userTemplate.setDisplayName(requiredDisplayName);
        userTemplate.setActive(active);
        return Optional.of(userTemplate);
    }
}
