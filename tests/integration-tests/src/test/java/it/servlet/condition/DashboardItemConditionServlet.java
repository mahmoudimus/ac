package it.servlet.condition;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;

public class DashboardItemConditionServlet extends HttpServlet
{
    public static final String DASHBOARD_ITEM_CONDITION_URL = "/dashboardItemConditon";

    private final String expectedUser;
    private final List<String> expectedMode;
    private final String expectedKey;

    public static ConditionalBean conditionBean()
    {
        return newSingleConditionBean().withCondition(DASHBOARD_ITEM_CONDITION_URL + "?dashboardItemViewType={dashboardItem.viewType}&key={dashboardItem.key}").build();
    }
    
    public DashboardItemConditionServlet(final String expectedUser, final List<String> expectedMode, final String expectedKey)
    {
        this.expectedUser = expectedUser;
        this.expectedMode = expectedMode;
        this.expectedKey = expectedKey;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String mode = req.getParameter("dashboardItemViewType");
        String userKey = req.getParameter("user_key");
        String key = req.getParameter("key");
        resp.setContentType("application/json");
        resp.getWriter().write("{\"shouldDisplay\" : " + shouldDisplay(userKey, mode, key) + "}");
        resp.getWriter().close();
    }

    private boolean shouldDisplay(final String user, final String mode, final String key)
    {
        return expectedMode.contains(mode) && expectedUser.equals(user) && key.contains(expectedKey);
    }

    public void setSupportedViewMode(final String mode)
    {
        expectedMode.clear();
        expectedMode.add(mode);
    }
}
