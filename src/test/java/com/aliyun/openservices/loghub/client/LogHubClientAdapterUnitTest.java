package com.aliyun.openservices.loghub.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.aliyun.openservices.log.testing.FakeServiceClient;
import com.aliyun.openservices.log.testing.RequestMatcher;
import com.aliyun.openservices.log.testing.Responses;
import com.aliyun.openservices.loghub.client.config.LogHubConfig;
import com.aliyun.openservices.loghub.client.config.LogHubCursorPosition;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for LogHubClientAdapter. Uses FakeServiceClient injected via
 * LogHubConfig.setServiceClient() — no real SLS endpoint required.
 *
 * Lives in the same package as LogHubClientAdapter to access its
 * package-private constructor.
 */
public class LogHubClientAdapterUnitTest {

    private static final String ENDPOINT = "cn-mock.example.com";
    private static final String PROJECT = "unit-project";
    private static final String LOGSTORE = "unit-ls";
    private static final String CONSUMER_GROUP = "unit-cg";
    private static final String CONSUMER = "unit-consumer";

    private FakeServiceClient fake;
    private LogHubClientAdapter adapter;

    @Before
    public void setUp() throws Exception {
        fake = new FakeServiceClient();
        // GetCursor: GET /logstores/<ls>/shards/0
        fake.stub(
                RequestMatcher.method("GET").path("/logstores/" + LOGSTORE + "/shards/0"),
                Responses.ok("{\"cursor\":\"MTAwMDAwMDAwMDAwMDAwMDAw\"}"));

        LogHubConfig config = new LogHubConfig(
                CONSUMER_GROUP, CONSUMER, ENDPOINT, PROJECT, LOGSTORE,
                "mock-id", "mock-key", LogHubConfig.ConsumePosition.BEGIN_CURSOR);
        config.setServiceClient(fake);

        adapter = new LogHubClientAdapter(config);
    }

    @Test
    public void getCursorHitsShardEndpoint() throws Exception {
        String cursor = adapter.getCursor(0, LogHubCursorPosition.BEGIN_CURSOR, 0);
        assertNotNull("cursor must not be null", cursor);
        assertTrue(
                "expected at least one GET /shards/0 request",
                fake.getReceivedRequests().stream()
                        .anyMatch(r -> r.getUri().toString().contains("/shards/0")));
    }

    @Test
    public void serviceClientInjectionPreventsRealNetwork() throws Exception {
        adapter.getCursor(0, LogHubCursorPosition.BEGIN_CURSOR, 0);
        assertTrue(
                "FakeServiceClient must have received at least one request",
                fake.getReceivedRequests().size() >= 1);
    }
}
