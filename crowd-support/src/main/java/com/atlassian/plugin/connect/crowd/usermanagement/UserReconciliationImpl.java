package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Optional;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.model.user.UserTemplate;

import org.springframework.stereotype.Component;

@Component
public class UserReconciliationImpl implements UserReconciliation
{
    @Override
    public Optional<UserTemplate> getFixes(User user, String requiredDisplayName, String requiredEmailAddress, boolean active)
    {
        if (user.getEmailAddress().equals(requiredEmailAddress) && user.getDisplayName().equals(requiredDisplayName) && user.isActive() == active)
        {
            return Optional.empty();
        }

        UserTemplate userTemplate = new UserTemplate(user);
        userTemplate.setEmailAddress(requiredEmailAddress);
        userTemplate.setDisplayName(requiredDisplayName);
        userTemplate.setActive(active);
        return Optional.of(userTemplate);
    }
}
