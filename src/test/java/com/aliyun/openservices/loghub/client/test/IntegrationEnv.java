package com.aliyun.openservices.loghub.client.test;

import org.junit.Assume;

/**
 * Loader for the environment variables that drive the integration test
 * suite. Mirrors {@code Credentials.loadOrSkip} from the java-sdk: when any
 * required field is missing, the test is reported as <em>Skipped</em> via
 * {@link Assume#assumeTrue(String, boolean)} rather than failing.
 *
 * <p>Canonical environment variables (matching the rest of the SLS SDK
 * harness):</p>
 * <ul>
 *   <li>{@code LOG_TEST_ENDPOINT}</li>
 *   <li>{@code LOG_TEST_PROJECT}</li>
 *   <li>{@code LOG_TEST_LOGSTORE}</li>
 *   <li>{@code LOG_TEST_ACCESS_KEY_ID}</li>
 *   <li>{@code LOG_TEST_ACCESS_KEY_SECRET}</li>
 * </ul>
 *
 * <p>For backward compatibility this class also accepts the legacy short
 * names that early consumer-java tests used: {@code ENDPOINT},
 * {@code PROJECT}, {@code LOG_STORE} / {@code LOGSTORE}, {@code ACCESS_KEY_ID},
 * {@code ACCESS_KEY_SECRET}. Canonical wins when both are set.</p>
 */
public final class IntegrationEnv {

    private static final String ENV_ENDPOINT = "LOG_TEST_ENDPOINT";
    private static final String ENV_PROJECT = "LOG_TEST_PROJECT";
    private static final String ENV_LOGSTORE = "LOG_TEST_LOGSTORE";
    private static final String ENV_AK_ID = "LOG_TEST_ACCESS_KEY_ID";
    private static final String ENV_AK_SECRET = "LOG_TEST_ACCESS_KEY_SECRET";
    private static final String ENV_REGION = "LOG_TEST_REGION";
    private static final String ENV_STS_TOKEN = "LOG_TEST_STS_TOKEN";

    private final String endpoint;
    private final String project;
    private final String logStore;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String region;
    private final String stsToken;

    private IntegrationEnv(String endpoint, String project, String logStore,
                           String accessKeyId, String accessKeySecret,
                           String region, String stsToken) {
        this.endpoint = endpoint;
        this.project = project;
        this.logStore = logStore;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.region = region;
        this.stsToken = stsToken;
    }

    /**
     * Read integration credentials from the environment, or raise a JUnit
     * {@link Assume} skip if any required field is missing. Returns a populated
     * holder when running with credentials configured.
     */
    public static IntegrationEnv loadOrSkip() {
        IntegrationEnv env = load();
        Assume.assumeTrue(
                "SKIP: requires " + ENV_ENDPOINT + " / " + ENV_PROJECT + " / "
                        + ENV_LOGSTORE + " / " + ENV_AK_ID + " / " + ENV_AK_SECRET,
                env != null);
        return env;
    }

    /**
     * Read integration credentials from the environment without raising a
     * skip. Returns {@code null} when any required field is missing.
     */
    public static IntegrationEnv load() {
        String endpoint = pick(ENV_ENDPOINT, "ENDPOINT");
        String project = pick(ENV_PROJECT, "PROJECT");
        String logStore = pick(ENV_LOGSTORE, "LOG_STORE", "LOGSTORE");
        String accessKeyId = pick(ENV_AK_ID, "ACCESS_KEY_ID");
        String accessKeySecret = pick(ENV_AK_SECRET, "ACCESS_KEY_SECRET");
        if (isBlank(endpoint) || isBlank(project) || isBlank(logStore)
                || isBlank(accessKeyId) || isBlank(accessKeySecret)) {
            return null;
        }
        String region = pick(ENV_REGION, "REGION");
        String stsToken = pick(ENV_STS_TOKEN, "STS_TOKEN");
        return new IntegrationEnv(endpoint, project, logStore, accessKeyId,
                accessKeySecret, region, stsToken);
    }

    public String getEndpoint() { return endpoint; }
    public String getProject() { return project; }
    public String getLogStore() { return logStore; }
    public String getAccessKeyId() { return accessKeyId; }
    public String getAccessKeySecret() { return accessKeySecret; }
    public String getRegion() { return region; }
    public String getStsToken() { return stsToken; }

    private static String pick(String... names) {
        for (String name : names) {
            String value = System.getenv(name);
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isEmpty();
    }
}
