package com.atlassian.plugin.connect.bitbucket.condition;

import com.atlassian.plugin.connect.spi.condition.ConditionsProvider;
import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;

@BitbucketComponent
public class BitbucketConditions implements ConditionsProvider {
    @Override
    public ConditionClassResolver getConditions() {
        return ConditionClassResolver.builder()
                .build();
    }
}
