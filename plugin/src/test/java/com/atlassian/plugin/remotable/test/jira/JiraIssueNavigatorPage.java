package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Supplier;

/**
 *
 */
public class JiraIssueNavigatorPage extends AdvancedSearch
{
    @Override
    public TimedCondition isAt()
    {
        return Conditions.forSupplier(new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return getResults().getTotalCount() > 0;
            }
        });
    }
}
