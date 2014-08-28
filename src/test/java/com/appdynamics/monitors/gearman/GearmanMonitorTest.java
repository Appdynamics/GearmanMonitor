package com.appdynamics.monitors.gearman;

import com.appdynamics.extensions.gearman.GearmanMonitor;
import com.appdynamics.extensions.gearman.SimpleTelnetClient;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by abhi.pandey on 8/18/14.
 */

public class GearmanMonitorTest {

    private static final String CONFIG_ARG = "config-file";

    private GearmanMonitor testClass;

    @Before
    public void init() throws Exception {
        testClass = new GearmanMonitor();
    }


    @Test(expected = TaskExecutionException.class)
    public void testWithNullArgsShouldResultInException() throws Exception {
        testClass.execute(null, null);
    }

    @Test(expected = TaskExecutionException.class)
    public void testWithEmptyArgsShouldResultInException() throws Exception {
        testClass.execute(new HashMap<String, String>(), null);
    }

    @Test
    public void testGearmanMonitor() throws TaskExecutionException {
        Map<String, String> taskArgs = new HashMap();
        taskArgs.put(CONFIG_ARG, "src/test/resources/conf/config.yml");
        testClass.execute(taskArgs, null);

    }

    @Test
    public void testGetResult(){
        String expected = "wc\t0\t0\t2\n" +
                "ls\t2\t0\t0\n" +
                ".";
        SimpleTelnetClient stc = mock(SimpleTelnetClient.class);
        when(stc.sendCommand("STATUS")).thenReturn(expected);
        testClass.fetchAndProcessResult(stc);
    }
}
