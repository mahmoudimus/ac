package com.atlassian.plugin.connect.jira.util;

import com.atlassian.fugue.Either;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollections;

public final class ServiceOutcomes {
    private ServiceOutcomes() {}

    public static <T> Either<ErrorCollection, T> toEither(ServiceOutcome<T> outcome) {
        return outcome.isValid() ? Either.right(outcome.get()) : Either.left(outcome.getErrorCollection());
    }

    public static ServiceResult errorResult(ErrorCollection collection) {
        return new ServiceResultImpl(collection);
    }

    public static <T> ServiceOutcome<T> errorOutcome(ErrorCollection collection) {
        return new ServiceOutcomeImpl<>(collection);
    }

    public static ServiceResult successResult() {
        return new ServiceResultImpl(ErrorCollections.empty());
    }

    public static <T> ServiceOutcome<T> successOutcome(T value) {
        return new ServiceOutcomeImpl<>(ErrorCollections.empty(), value);
    }
}
