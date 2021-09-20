package io.vepo.kafka.load.engine.config;

import io.vepo.kafka.load.engine.exceptions.ExecutorException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.util.Properties;

public class Configuration {
    private Properties configs;

    private Configuration(Properties configs) {
        this.configs = configs;
    }

    public static Configuration empty() {
        return new Configuration(new Properties());
    }

    public static Configuration fromFile(File file) {
        try {
            Properties configs = new Properties();
            configs.load(new FileReader(file));
            return new Configuration(configs);
        } catch (IOException ioe) {
            throw new ExecutorException("Could not load configuration file!", ioe);
        }
    }
}
