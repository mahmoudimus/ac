package com.atlassian.plugin.connect.plugin.condition;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Conditions annotated with this annotation will have additional parameters
 * available added automatically by the Connect framework.
 * <p>
 *  To access the additional parameters use the {@link ConnectConditionContext} class.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectCondition {}
