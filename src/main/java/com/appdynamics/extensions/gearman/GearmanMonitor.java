/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.gearman;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.gearman.config.Configuration;
import com.appdynamics.extensions.gearman.config.Server;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

import java.io.File;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * Created by abhi.pandey on 5/16/14.
 */
public class GearmanMonitor extends AManagedMonitor {

    protected final Logger logger = Logger.getLogger(GearmanMonitor.class.getName());
    private String metricPrefix;
    public static final String CONFIG_ARG = "config-file";
    public static final String LOG_PREFIX = "log-prefix";
    private static String logPrefix;
    public static final String METRIC_SEPARATOR = "|";
    private List<GearmanMetric> listOfMetricPerFunction;

    /**
     * This is the entry point to the monitor called by the Machine Agent
     *
     * @param taskArguments
     * @param taskContext
     * @return
     * @throws TaskExecutionException
     */
    public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext taskContext) throws TaskExecutionException {
        if (taskArguments != null && !taskArguments.isEmpty()) {
            setLogPrefix(taskArguments.get(LOG_PREFIX));
            logger.info("Using Monitor Version [" + getImplementationVersion() + "]");
            logger.info(getLogPrefix() + "Starting the Gearman Monitoring task.");
            if (logger.isDebugEnabled()) {
                logger.debug(getLogPrefix() + "Task Arguments Passed ::" + taskArguments);
            }
            String status = "Success";

            String configFilename = getConfigFilename(taskArguments.get(CONFIG_ARG));

            try {
                //read the config.
                Configuration config = YmlReader.readFromFile(configFilename, Configuration.class);

                // no point continuing if we don't have this
                if (config.getServers().isEmpty()) {
                    return new TaskOutput("Failure");
                }
                processMetricPrefix(config.getMetricPrefix());

                status = getStatus(config, status);
            } catch (Exception e) {
                logger.error("Exception", e);
            }

            return new TaskOutput(status);
        }
        throw new TaskExecutionException(getLogPrefix() + "Gearman monitoring task completed with failures.");

    }

    private String getStatus(Configuration config, String status) {
        // loop through the servers
        try {
            for (Server server : config.getServers()) {

                Map<String, String> gearmanServerMap = Maps.newHashMap();
                gearmanServerMap.put("host", server.getHostName());
                gearmanServerMap.put("port", server.getPort());
                gearmanServerMap.put("user", server.getUserName());
                gearmanServerMap.put("password", server.getPassword());

                SimpleTelnetClient telnet = buildTelnetClient(gearmanServerMap);
                fetchAndProcessResult(telnet);

                printMetric(listOfMetricPerFunction);

            }
        } catch (Exception e) {
            logger.error("Error telnetting into the server:" + e);
            status = "Failure";
        }
        return status;
    }

    public void fetchAndProcessResult(SimpleTelnetClient telnet) {
        telnet.connect();
        logger.info("Got Connection to gearman server");
        String result = telnet.sendCommand("STATUS");

        logger.debug("Results from the server: " + result);

        if (!Strings.isNullOrEmpty(result)) {
            listOfMetricPerFunction = processResult(result);
        }

        telnet.disconnect();
        logger.info("Disconnected from gearman server");
    }

    private List<GearmanMetric> processResult(String result) {

        listOfMetricPerFunction = new ArrayList<GearmanMetric>();
        String[] jobs = result.split("\\n");

        for (int i = 0; i < jobs.length; i++) {
            if (!jobs[i].equals(".")) {
                String[] results = jobs[i].split("\\t");
                GearmanMetric gm = new GearmanMetric();
                Map<String, String> metric = new HashMap<String, String>();
                String functionName = results[0];
                String numberOfJobs = results[1];
                metric.put("Number of jobs", numberOfJobs);
                String numberOfRunning = results[2];
                metric.put("Number of running", numberOfRunning);
                String numberOfWorkers = results[3];
                metric.put("Number of workers", numberOfWorkers);
                gm.setFunctionName(functionName);
                gm.setMetric((HashMap<String, String>) metric);
                listOfMetricPerFunction.add(gm);
            }
        }
        return listOfMetricPerFunction;
    }

    public SimpleTelnetClient buildTelnetClient(Map<String, String> gearmanServerMap) {
        return SimpleTelnetClient.newInstance(gearmanServerMap);
    }

    private void printMetric(List<GearmanMetric> listOfMetricPerFunction) {
        for (GearmanMetric metric : listOfMetricPerFunction) {
            StringBuffer metricPath = new StringBuffer();
            metricPath.append(metricPrefix).append(metric.getFunctionName()).append(METRIC_SEPARATOR);

            Map<String, String> metricsForAServer = metric.getMetric();
            Iterator<String> it = metricsForAServer.keySet().iterator();
            while (it.hasNext()) {
                String metricName = it.next();
                String metricValue = metricsForAServer.get(metricName);
                printCollectiveObservedCurrent(metricPath.toString() + metricName, metricValue);
            }
        }
    }

    private void printCollectiveObservedCurrent(String metricPath, String metricValue) {
        printMetric(metricPath, metricValue,
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL
        );
    }

    /**
     * A helper method to report the metrics.
     *
     * @param metricPath
     * @param metricValue
     * @param aggType
     * @param timeRollupType
     * @param clusterRollupType
     */
    public void printMetric(String metricPath, String metricValue, String aggType, String timeRollupType, String clusterRollupType) {
        MetricWriter metricWriter = getMetricWriter(metricPath,
                aggType,
                timeRollupType,
                clusterRollupType
        );

        if (logger.isDebugEnabled()) {
            logger.debug(getLogPrefix() + "Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
                    + "] metric = " + metricPath + " = " + metricValue);
        }
        metricWriter.printMetric(metricValue);
    }

    /**
     * Returns a config file name,
     *
     * @param filename
     * @return String
     */

    private String getConfigFilename(String filename) {
        if (filename == null) {
            return "";
        }
        //for absolute paths
        if (new File(filename).exists()) {
            return filename;
        }
        //for relative paths
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = "";
        if (!Strings.isNullOrEmpty(filename)) {
            configFileName = jarPath + File.separator + filename;
        }
        return configFileName;
    }

    private void processMetricPrefix(String metricPrefix) {

        if (!metricPrefix.endsWith("|")) {
            metricPrefix = metricPrefix + "|";
        }
        if (!metricPrefix.startsWith("Custom Metrics|")) {
            metricPrefix = "Custom Metrics|" + metricPrefix;
        }

        this.metricPrefix = metricPrefix;
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public void setLogPrefix(String logPrefix) {
        this.logPrefix = (logPrefix != null) ? logPrefix : "";
    }

    private static String getImplementationVersion() {
        return GearmanMonitor.class.getPackage().getImplementationTitle();
    }

}
