package com.atlassian.labs.remoteapps.util.http;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/26/12 Time: 3:08 PM To change this template use
 * File | Settings | File Templates.
 */
public interface AuthorizationGenerator
{
    String generate(String method, String url, Map<String,List<String>> parameters);
}
