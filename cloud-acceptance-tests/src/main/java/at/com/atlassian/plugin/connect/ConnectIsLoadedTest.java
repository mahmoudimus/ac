package at.com.atlassian.plugin.connect;

import com.atlassian.test.categories.OnDemandAcceptanceTest;
import org.junit.experimental.categories.Category;

public class ConnectIsLoadedTest
{
    @Category(OnDemandAcceptanceTest.class)
    public void connectShouldBeLoaded()
    {
        System.out.println("Huzzah!");
    }
}
