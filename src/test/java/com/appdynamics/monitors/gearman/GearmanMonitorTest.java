package com.appdynamics.monitors.gearman;

import com.appdynamics.extensions.gearman.GearmanMonitor;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by abhi.pandey on 8/18/14.
 */
public class GearmanMonitorTest {
    public static final String CONFIG_ARG = "config-file";
    @Test
    public void testSQLMonitor() throws TaskExecutionException {
        GearmanMonitor monitor = new GearmanMonitor();
        Map<String, String> taskArgs = new HashMap();
        taskArgs.put(CONFIG_ARG, "src/test/resources/conf/config.yml");
        monitor.execute(taskArgs, null);

    }
}
