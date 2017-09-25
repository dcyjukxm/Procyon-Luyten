package com.beust.jcommander.defaultprovider;

import java.util.*;
import com.beust.jcommander.*;
import java.io.*;
import java.net.*;

public class PropertyFileDefaultProvider implements IDefaultProvider
{
    public static final String DEFAULT_FILE_NAME = "jcommander.properties";
    private Properties m_properties;
    
    public PropertyFileDefaultProvider() {
        super();
        this.init("jcommander.properties");
    }
    
    public PropertyFileDefaultProvider(final String fileName) {
        super();
        this.init(fileName);
    }
    
    private void init(final String fileName) {
        try {
            this.m_properties = new Properties();
            final URL url = ClassLoader.getSystemResource(fileName);
            if (url == null) {
                throw new ParameterException("Could not find property file: " + fileName + " on the class path");
            }
            this.m_properties.load(url.openStream());
        }
        catch (IOException e) {
            throw new ParameterException("Could not open property file: " + fileName);
        }
    }
    
    public String getDefaultValueFor(final String optionName) {
        int index;
        for (index = 0; index < optionName.length() && !Character.isLetterOrDigit(optionName.charAt(index)); ++index) {}
        final String key = optionName.substring(index);
        return this.m_properties.getProperty(key);
    }
}
