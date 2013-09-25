package com.atlassian.plugin.connect.plugin.module.permission;

public class UnauthorisedException extends Exception
{
    public UnauthorisedException(String message)
    {
        super(message);
    }

}
