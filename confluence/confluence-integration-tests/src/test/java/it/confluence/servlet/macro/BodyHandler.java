package it.confluence.servlet.macro;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface BodyHandler {
    void processBody(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context, String body) throws IOException;
}