package it.servlet.macro;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface BodyHandler
{
    public void processBody(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context, String body) throws IOException;
}