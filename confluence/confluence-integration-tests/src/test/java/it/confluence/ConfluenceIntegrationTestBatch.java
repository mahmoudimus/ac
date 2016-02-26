package it.confluence;

import com.atlassian.test.batching.BatchedTests;
import com.atlassian.test.batching.TestBatchRunner;
import com.atlassian.test.batching.junitreport.JUnitTestTimings;
import org.junit.runner.RunWith;

@RunWith(TestBatchRunner.class)
@BatchedTests(
    includeTestPattern = "*.Test*",
    excludeTestPatterns = {
        "*$*"
    },
    basePackages = "it.confluence"
)
public class ConfluenceIntegrationTestBatch
{
}