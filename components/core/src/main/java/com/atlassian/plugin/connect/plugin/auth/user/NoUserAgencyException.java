package com.atlassian.plugin.connect.plugin.auth.user;

/**
 * Thrown when an add-on attempts to act as an agent on a user's behalf but lacks the necessary authorisation.
 */
public class NoUserAgencyException extends Exception {
    public NoUserAgencyException(String message) {
        super(message);
    }
}
