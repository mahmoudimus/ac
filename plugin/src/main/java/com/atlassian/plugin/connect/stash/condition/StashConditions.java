package com.atlassian.plugin.connect.stash.condition;

import com.atlassian.plugin.connect.spi.condition.ConditionsProvider;
import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;

@StashComponent
public class StashConditions implements ConditionsProvider {
    @Override
    public ConditionClassResolver getConditions() {
        return ConditionClassResolver.builder()
                .build();
    }
}
