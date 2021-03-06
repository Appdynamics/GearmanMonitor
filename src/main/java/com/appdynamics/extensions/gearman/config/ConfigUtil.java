/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.gearman.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ConfigUtil<T> {

    public static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    /**
     * Reads the config file
     * @param configFilename
     * @return Configuration
     * @throws java.io.FileNotFoundException
     */
    public T readConfig(String configFilename,Class<T> clazz) throws FileNotFoundException {
        logger.info("Reading config file::" + configFilename);
        Yaml yaml = new Yaml(new Constructor(Configuration.class));
        T config = (T) yaml.load(new FileInputStream(configFilename));
        return config;
    }

}