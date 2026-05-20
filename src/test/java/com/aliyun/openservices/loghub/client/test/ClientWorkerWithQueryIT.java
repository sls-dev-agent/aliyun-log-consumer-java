package com.aliyun.openservices.loghub.client.test;

import com.aliyun.openservices.loghub.client.ClientWorker;
import com.aliyun.openservices.loghub.client.config.LogHubConfig;
import com.aliyun.openservices.loghub.client.exceptions.LogHubClientWorkerException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientWorkerWithQueryIT {

    private static IntegrationEnv env;

    @BeforeClass
    public static void setUpCredentials() {
        env = IntegrationEnv.loadOrSkip();
    }

    @Test
    public void testQueryConsumer() throws LogHubClientWorkerException, InterruptedException {
        final String TEST_QUERY = "* | WHERE request_method = 'GET'";
        int n = 5;
        Thread[] threads = new Thread[n];
        ClientWorker[] workers = new ClientWorker[n];

        for (int i = 0; i < n; i++) {
            LogHubConfig config = new LogHubConfig(
                    "consumer_group_query_worker_test", "consumer_" + i,
                    env.getEndpoint(), env.getProject(), env.getLogStore(),
                    env.getAccessKeyId(), env.getAccessKeySecret(),
                    LogHubConfig.ConsumePosition.BEGIN_CURSOR, TEST_QUERY);
            config.setHeartBeatIntervalMillis(20 * 1000);
            ClientWorker worker = new ClientWorker(new LogHubProcessorTestFactory(), config);
            threads[i] = new Thread(worker);
            workers[i] = worker;
        }
        for (int i = 0; i < n; i++) {
            threads[i].start();
        }

        Thread.sleep(60 * 1000);

        for (int i = 0; i < n; i++) {
            workers[i].shutdown();
        }

        Thread.sleep(60 * 1000);
    }
}
