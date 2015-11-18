package com.atlassian.plugin.connect.plugin.descriptor.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.spi.descriptor.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ModuleMultimap;
import com.atlassian.plugin.connect.plugin.descriptor.LoggingModuleValidationExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * An exception handlers for use with {@link ModuleMultimap#getValidModuleLists(Consumer)} that publishes an event for
 * validation exceptions.
 */
@Component
public class EventPublishingModuleValidationExceptionHandler extends LoggingModuleValidationExceptionHandler
{

    private EventPublisher eventPublisher;

    @Autowired
    public EventPublishingModuleValidationExceptionHandler(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected void handleModuleValidationCause(ConnectModuleValidationException cause)
    {
        eventPublisher.publish(new ConnectAddonModuleValidationFailedAfterInstallEvent(cause));
    }
}
