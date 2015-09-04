package com.atlassian.plugin.connect.spi.util;

/*
 * Copy-pasted to workaround for http://jira.codehaus.org/browse/MNG-3559
 * 
 * Test jars in sibling projects do not really work, once
 * they are fixed we can add a dependency to the test-jar of
 * 'atlassian-plugin-spi'. 
 */

public class APITestUtil
{
    public static String createXmlRpcPayload(String methodName)
    {
        return "<?xml version=\"1.0\"?>\n" +
                "<methodCall>\n" +
                "   <methodName>"+ methodName +"</methodName>\n" +
                "</methodCall>";
    }
    
    public static String createJsonRpcPayload(String methodName) 
    {
        return "{\"jsonrpc\" : \"2.0\",  \"method\" : \"" + methodName + "\"}";
    }
}
