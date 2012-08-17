package it;

import com.atlassian.labs.remoteapps.junit.UniversalBinaries;
import com.atlassian.labs.remoteapps.junit.UniversalBinariesContainerJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(UniversalBinariesContainerJUnitRunner.class)
@UniversalBinaries(value = "${moduleDir}/target/remoteapps-universal-binary-sample.jar")
public class TestActiveObjects
{
    @Test
    public void testSomething() throws InterruptedException
    {
        assertTrue(true);
        Thread.sleep(60000);
    }
}
