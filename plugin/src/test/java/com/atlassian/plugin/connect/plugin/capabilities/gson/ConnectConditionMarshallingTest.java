package com.atlassian.plugin.connect.plugin.capabilities.gson;

import java.lang.reflect.Type;
import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since version
 */
public class ConnectConditionMarshallingTest
{
    @Test
    public void verifyDeserializationWorks() throws Exception
    {
        String json = "[\n" +
                "    {\n" +
                "        \"condition\": \"never_display\",\n" +
                "        \"invert\": true,\n" +
                "        \"params\":\n" +
                "            {\n" +
                "                \"someParam\": \"woot\"\n" +
                "            }\n" +
                "    },\n" +
                "    {\n" +
                "        \"condition\": \"http://example.com/condition/got_woot\",\n" +
                "        \"invert\": false\n" +
                "    },     \n" +
                "    {\n" +
                "        \"and\": [\n" +
                "            {\n" +
                "                \"condition\": \"http://example.com/condition/got_woot\",\n" +
                "                \"invert\": false\n" +
                "            },\n" +
                "            {\n" +
                "                \"condition\": \"http://example.com/condition/got_woot\",\n" +
                "                \"invert\": false\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"or\" : [\n" +
                "            {\n" +
                "                \"condition\": \"http://example.com/condition/got_woot\",\n" +
                "                \"invert\": false\n" +
                "            },\n" +
                "            {\n" +
                "                \"condition\": \"http://example.com/condition/got_woot\",\n" +
                "                \"invert\": false\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "]";

        Gson gson = CapabilitiesGsonFactory.getGson();
        Type listType = new TypeToken<List<ConditionalBean>>(){}.getType();
        
        List<ConditionalBean> conditionList = gson.fromJson(json,listType);

        assertEquals(4,conditionList.size());
        assertEquals(SingleConditionBean.class,conditionList.get(0).getClass());
        assertEquals(SingleConditionBean.class,conditionList.get(1).getClass());
        assertEquals(CompositeConditionBean.class,conditionList.get(2).getClass());
        assertEquals(CompositeConditionBean.class,conditionList.get(3).getClass());
        
        assertTrue(((SingleConditionBean)conditionList.get(0)).getParams().containsKey("someParam"));
        assertEquals("woot", ((SingleConditionBean) conditionList.get(0)).getParams().get("someParam"));

        assertEquals(CompositeConditionType.and, ((CompositeConditionBean)conditionList.get(2)).getType());
        assertEquals(2, ((CompositeConditionBean)conditionList.get(2)).getConditions().size());

        assertEquals(CompositeConditionType.or, ((CompositeConditionBean)conditionList.get(3)).getType());
        assertEquals(2, ((CompositeConditionBean)conditionList.get(3)).getConditions().size());
        
    }

    @Test
    public void verifySerializationWorks() throws Exception
    {
        String expected = "[{\"condition\":\"some_condition\",\"invert\":false,\"params\":{}},{\"and\":[{\"condition\":\"some_condition2\",\"invert\":false,\"params\":{}},{\"or\":[{\"condition\":\"some_condition3\",\"invert\":false,\"params\":{}}]}]}]";
        
        Type conditionalType = new TypeToken<List<ConditionalBean>>(){}.getType();
        List<ConditionalBean> conditionList = newArrayList();
        
        conditionList.add(newSingleConditionBean().withCondition("some_condition").build());
        
        conditionList.add(
                newCompositeConditionBean().withConditions(
                    newSingleConditionBean().withCondition("some_condition2").build(),
                    newCompositeConditionBean().withConditions(
                        newSingleConditionBean().withCondition("some_condition3").build()
                    ).withType(CompositeConditionType.or)
                    .build()
                ).withType(CompositeConditionType.and)
                .build()
        );
        
        Gson gson = CapabilitiesGsonFactory.getGson();
        String json = gson.toJson(conditionList,conditionalType);

        assertEquals(expected,json);
    }
}
