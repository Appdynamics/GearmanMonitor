package com.appdynamics.monitors.gearman;

import com.appdynamics.extensions.gearman.TelnetUtil;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by abhi.pandey on 8/18/14.
 */
public class TelnetUtilTest {
    @Test
    public void testSQLMonitor() throws TaskExecutionException {
        try {
            TelnetUtil telnet = new TelnetUtil("192.168.56.5", "4730", "", "");
            String expected = "wc\t0\t0\t2\n" +
                    "ls\t2\t0\t0\n" +
                    ".";
            String value = telnet.sendCommand("STATUS");
            Assert.assertEquals(expected, value);
            telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
