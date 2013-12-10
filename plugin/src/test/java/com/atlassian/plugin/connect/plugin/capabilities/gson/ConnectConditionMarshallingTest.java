package com.atlassian.plugin.connect.plugin.capabilities.gson;

import java.lang.reflect.Type;
import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readAddonTestFile;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @since 1.0
 */
public class ConnectConditionMarshallingTest
{
    @Test
    public void verifyDeserializationWorks() throws Exception
    {
        String json = readAddonTestFile("conditionMarshalling.json");

        Gson gson = ConnectModulesGsonFactory.getGson();
        Type listType = new TypeToken<List<ConditionalBean>>() {}.getType();

        List<ConditionalBean> conditionList = gson.fromJson(json, listType);

        assertThat(conditionList, 
                contains(
                        instanceOf(SingleConditionBean.class), 
                        instanceOf(SingleConditionBean.class), 
                        instanceOf(CompositeConditionBean.class), 
                        instanceOf(CompositeConditionBean.class)));

        assertThat(((SingleConditionBean) conditionList.get(0)).getParams(), hasEntry("someParam","woot"));
        assertThat((CompositeConditionBean)conditionList.get(2), both(hasProperty("type", is(CompositeConditionType.AND))).and(hasProperty("conditions", hasSize(2))));
        assertThat((CompositeConditionBean)conditionList.get(3), both(hasProperty("type", is(CompositeConditionType.OR))).and(hasProperty("conditions", hasSize(2))));

    }

    @Test
    public void verifySerializationWorks() throws Exception
    {
        String expected = "[{\"condition\":\"some_condition\",\"invert\":false},{\"and\":[{\"condition\":\"some_condition2\",\"invert\":false},{\"or\":[{\"condition\":\"some_condition3\",\"invert\":false}]}]}]";

        Type conditionalType = new TypeToken<List<ConditionalBean>>() {}.getType();
        List<ConditionalBean> conditionList = newArrayList();

        conditionList.add(newSingleConditionBean().withCondition("some_condition").build());

        conditionList.add(
                newCompositeConditionBean().withConditions(
                        newSingleConditionBean().withCondition("some_condition2").build(),
                        newCompositeConditionBean().withConditions(
                                newSingleConditionBean().withCondition("some_condition3").build()
                        ).withType(CompositeConditionType.OR)
                                .build()
                ).withType(CompositeConditionType.AND)
                        .build()
        );

        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(conditionList, conditionalType);

        assertEquals(expected, json);
    }
}
