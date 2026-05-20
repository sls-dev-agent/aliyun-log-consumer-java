package com.aliyun.openservices.loghub.client.test;

import com.aliyun.openservices.log.common.auth.CredentialsProvider;
import com.aliyun.openservices.log.common.auth.DefaultCredentials;
import com.aliyun.openservices.log.common.auth.StaticCredentialsProvider;
import com.aliyun.openservices.log.http.signer.SignVersion;
import com.aliyun.openservices.loghub.client.ClientWorker;
import com.aliyun.openservices.loghub.client.config.LogHubConfig;
import com.aliyun.openservices.loghub.client.exceptions.LogHubClientWorkerException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientWorkerIT {

    private static IntegrationEnv env;

    @BeforeClass
    public static void setUpCredentials() {
        env = IntegrationEnv.loadOrSkip();
    }

    @Test
    public void testCredentialsProvider() throws LogHubClientWorkerException, InterruptedException {
        int n = 5;
        Thread[] threads = new Thread[n];
        ClientWorker[] workers = new ClientWorker[n];
        CredentialsProvider provider = new StaticCredentialsProvider(
                new DefaultCredentials(env.getAccessKeyId(), env.getAccessKeySecret(), ""));

        for (int i = 0; i < n; i++) {
            LogHubConfig config = new LogHubConfig(
                    "consumer_group_client_worker_test", "consumer_" + i,
                    env.getEndpoint(), env.getProject(), env.getLogStore(),
                    provider, LogHubConfig.ConsumePosition.BEGIN_CURSOR);
            config.setHeartBeatIntervalMillis(20 * 1000);
            config.setSignVersion(SignVersion.V4);
            config.setRegion("cn-hangzhou");
            config.setRequestTimeout(60 * 1000);
            config.setRequestTimeoutEnabled(true);
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

    @Test
    public void testCredentialsProviderWithStartTime() throws LogHubClientWorkerException, InterruptedException {
        int n = 5;
        Thread[] threads = new Thread[n];
        ClientWorker[] workers = new ClientWorker[n];
        CredentialsProvider provider = new StaticCredentialsProvider(
                new DefaultCredentials(env.getAccessKeyId(), env.getAccessKeySecret(), ""));

        int startTime = (int) (System.currentTimeMillis() / 1000);
        for (int i = 0; i < n; i++) {
            LogHubConfig config = new LogHubConfig(
                    "consumer_group_client_worker_test", "consumer_" + i,
                    env.getEndpoint(), env.getProject(), env.getLogStore(),
                    provider, startTime);
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
