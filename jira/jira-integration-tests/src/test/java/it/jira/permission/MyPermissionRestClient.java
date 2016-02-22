package it.jira.permission;

import com.atlassian.httpclient.api.Request.Method;
import com.atlassian.pageobjects.TestedProduct;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;

public class MyPermissionRestClient {
    private final URI resourcePath;

    public MyPermissionRestClient(TestedProduct testedProduct) {
        String baseUrl = testedProduct.getProductInstance().getBaseUrl();
        resourcePath = UriBuilder.fromPath(baseUrl).path("rest").path("api").path("2").path("mypermissions").build();
    }

    public Map<String, PermissionJsonBean> getMyPermissions() throws Exception {
        Type type = new TypeToken<Map<String, Map<String, PermissionJsonBean>>>() {
        }.getType();
        HttpURLConnection connection = (HttpURLConnection) resourcePath.toURL().openConnection();
        connection.setRequestMethod(Method.GET.name());
        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        Map<String, Map<String, PermissionJsonBean>> stringUserPermissionJsonBeanMap = new Gson().fromJson(reader, type);
        return stringUserPermissionJsonBeanMap.get("permissions");
    }
}
