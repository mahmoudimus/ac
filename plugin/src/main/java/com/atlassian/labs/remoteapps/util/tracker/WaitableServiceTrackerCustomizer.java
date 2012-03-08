package com.atlassian.labs.remoteapps.util.tracker;

/**
 * Callbacks for adding and removing services to the tracker
 */
public interface WaitableServiceTrackerCustomizer<T>
{
    T adding(T service);
    
    void removed(T service);
}
