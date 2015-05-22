package com.atlassian.plugin.connect.plugin.scopes;

/*
 * Copy-pasted to workaround for http://jira.codehaus.org/browse/MNG-3559
 * 
 * Test jars in sibling projects do not really work, once
 * they are fixed we can add a dependency to the test-jar of
 * 'atlassian-plugin-spi'. 
 */

public class APITestUtil
{
    public static String createSoapRpcPayload(String methodName)
    {
        return "<?xml version=\"1.0\"?>\n" +
                "<soap:Envelope\n" +
                "xmlns:soap=\"http://www.w3.org/2001/12/soap-envelope\"\n" +
                "soap:encodingStyle=\"http://www.w3.org/2001/12/soap-encoding\">\n" +
                "\n" +
                "<soap:Body xmlns:m=\"http://soap.rpc.jira.atlassian.com\">" +
                "    <m:" + methodName + ">" +
                "    </m:" + methodName + ">" +
                "</soap:Body>\n" +
                "\n" +
                "</soap:Envelope>";
    }
    
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
