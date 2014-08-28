package com.appdynamics.monitors.gearman;

import com.appdynamics.extensions.gearman.SimpleTelnetClient;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by abhi.pandey on 8/18/14.
 */
public class SimpleTelnetClientTest {
    @Test
    public void testSQLMonitor() throws TaskExecutionException {
        try {
            Map<String, String> gearmanServerMap = Maps.newHashMap();
            gearmanServerMap.put("host", "192.168.56.5");
            gearmanServerMap.put("port", "4730");
            gearmanServerMap.put("user", "");
            gearmanServerMap.put("password", "");
            SimpleTelnetClient telnet = mock(SimpleTelnetClient.class);
            String expected = "wc\t0\t0\t2\n" +
                    "ls\t2\t0\t0\n" +
                    ".";
            when(telnet.sendCommand("STATUS")).thenReturn(expected);
            telnet.connect();
            String value = telnet.sendCommand("STATUS");
            Assert.assertEquals(expected, value);
            telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
