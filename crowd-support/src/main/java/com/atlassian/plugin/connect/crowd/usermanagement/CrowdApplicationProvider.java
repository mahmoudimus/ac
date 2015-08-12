package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.model.application.Application;

/**
 * An injectable means of obtaining a crowd application for use with the embedded Crowd {@link com.atlassian.crowd.manager.application.ApplicationService}
 */
public interface CrowdApplicationProvider
{
    /**
     * Get an instance of the crowd application to work with
     *
     * @return an application for use with the embedded Crowd {@link com.atlassian.crowd.manager.application.ApplicationService}
     */
    Application getCrowdApplication() throws ApplicationNotFoundException;

    /**
     * Get the name of the crowd application we're working with
     *
     * @return the name of the crowd application we're working with
     */
    String getCrowdApplicationName();
}
