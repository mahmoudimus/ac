package com.atlassian.labs.remoteapps.plugin.product;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WebSudoElevator
{
   public void startWebSudoSession(HttpServletRequest request, HttpServletResponse response);

}
