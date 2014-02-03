package com.atlassian.plugin.connect.plugin.installer;

public interface ConnectAddOnUserService
{
    String getOrCreateUserKey(String addOnKey) throws ConnectAddOnUserInitException;
}
