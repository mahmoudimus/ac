package com.atlassian.plugin.connect.api.web.condition;


import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;
import java.util.Optional;

@ConnectCondition
public abstract class AbstractConnectCondition implements Condition {
    protected String addonKey;

    @Override
    public void init(Map<String, String> params) throws PluginParseException {
        Optional<String> maybeAddonKey = ConnectConditionContext.from(params).getAddonKey();
        if (!maybeAddonKey.isPresent()) {
            throw new PluginParseException("Condition should have been invoked in the Atlassian Connect context, but apparently it was not, add-on key is missing.");
        }
        this.addonKey = maybeAddonKey.get();
    }
}
