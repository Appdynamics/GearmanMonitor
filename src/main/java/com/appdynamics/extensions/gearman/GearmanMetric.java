/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.gearman;

import java.util.HashMap;

/**
 * Created by abhi.pandey on 8/14/14.
 */
public class GearmanMetric {

    private String functionName;

    private HashMap<String, String> metric;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public HashMap<String, String> getMetric() {
        return metric;
    }

    public void setMetric(HashMap<String, String> metric) {
        this.metric = metric;
    }

}
