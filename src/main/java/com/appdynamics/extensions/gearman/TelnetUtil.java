package com.appdynamics.extensions.gearman;

import com.google.common.base.Strings;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Created by abhi.pandey on 8/12/14.
 */

public class TelnetUtil {
    public static final Logger logger = Logger.getLogger(GearmanMonitor.class);
    private TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;
    private static final String prompt = "\n.";

    public TelnetUtil(String server, String port, String user, String password) {
        try {
            // Connect to the specified server
            telnet.connect(server, Integer.parseInt(port));

            // Get input and output stream references
            in = telnet.getInputStream();
            out = new PrintStream(telnet.getOutputStream());

            // Log the user on
            if (!Strings.isNullOrEmpty(user) && !Strings.isNullOrEmpty(password)) {
                readUntil("login: ");
                write(user);
                readUntil("Password: ");
                write(password);
            }
        } catch (Exception e) {
            logger.error("Can't telnet to the server :" + server + e);
        }
    }

    public String readUntil(String pattern) {
        try {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuffer sb = new StringBuffer();
            char ch = (char) in.read();
            while (true) {
                sb.append(ch);
                if (ch == lastChar) {
                    return sb.toString();
                }
                ch = (char) in.read();
            }
        } catch (Exception e) {
            logger.error("Invalid output " + e);
        }
        return null;
    }

    public void write(String value) {
        out.println(value);
        out.flush();
    }

    public String sendCommand(String command) {
        try {
            write(command);
            return readUntil(prompt);
        } catch (Exception e) {
            logger.error("Invalid command for gearman server: " + command + e);
        }
        return null;
    }

    public void disconnect() {
        try {
            telnet.disconnect();
        } catch (Exception e) {
            logger.error("Can't able to disconnect from telnet server!" + e);
        }
    }

}
