package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.labs.remoteapps.util.ServletUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * An api scope for json-rpc requests
 */
public class JsonRpcApiScope {
    private Collection<String> methods;
    private String path;

    public JsonRpcApiScope(String path, Collection<String> methods) {
        this.path = path;
        this.methods = methods;
    }

    public boolean allow(HttpServletRequest request)
    {
        final String pathInfo = ServletUtils.extractPathInfo(request);
        if (path.equals(pathInfo))
        {
            // methodName not in path so extract it from body
            String method = extractMethod(request);
            if (method == null)
            {
                return false;
            }
            else if (methods.contains(method))
            {
                return true;
            }
        }
        else
        {
            // methodName in path
            String method = request.getPathInfo().replaceAll(path + "/","");
            return methods.contains(method);
        }
        return false;
    }

    private String extractMethod(HttpServletRequest request)
    {
        try
        {
            InputStream in;
            in = request.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            JSONObject json = new JSONObject(new JSONTokener(reader));
            in.close();
            return json.get("method").toString();
        }
        catch (JSONException e)
        {
            return null;
        }
        catch (IOException e)
        {
            return null;
        }
    }

}
