/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.gearman;

import com.google.common.base.Strings;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 * Created by abhi.pandey on 8/12/14.
 */

public class SimpleTelnetClient {
    public static final Logger logger = Logger.getLogger(GearmanMonitor.class);
    private TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;
    private static final String prompt = "\n.";
    private String server;
    private String port;
    private String user;
    private String password;

    public void connect() {
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

    public static SimpleTelnetClient newInstance(Map<String, String> argsMap) {
        try {
            if(!argsMap.containsKey("host")){
                throw new RuntimeException("host key doesn't exist in map");
            }

            if(!argsMap.containsKey("port")){
                throw new RuntimeException("port key doesn't exist in map");
            }

            return new SimpleTelnetClient.Builder(argsMap.get("host"), argsMap.get("port")).
                    user(argsMap.get("user")).password(argsMap.get("password")).build();
        }catch (Exception e){
            logger.error("Inputted argument map is not in valid format"+ e);
        }
        return null;
    }



    public static class Builder {

        //Required parameter
        private String server;
        private String port;

        // Optional parameter - initialized to default value
        private String user = "";
        private String password = "";

        public Builder(String server, String port) {
            this.server = server;
            this.port = port;
        }

        public Builder user(String val) {
            user = val;
            return this;
        }

        public Builder password(String val) {
            password = val;
            return this;
        }

        public SimpleTelnetClient build() {
            return new SimpleTelnetClient(this);
        }
    }

    private SimpleTelnetClient(Builder builder) {
        server = builder.server;
        port = builder.port;
        user = builder.user;
        password = builder.password;
    }

}
