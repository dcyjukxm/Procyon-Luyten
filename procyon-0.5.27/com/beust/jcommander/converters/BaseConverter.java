package com.beust.jcommander.converters;

import com.beust.jcommander.*;

public abstract class BaseConverter<T> implements IStringConverter<T>
{
    private String m_optionName;
    
    public BaseConverter(final String optionName) {
        super();
        this.m_optionName = optionName;
    }
    
    public String getOptionName() {
        return this.m_optionName;
    }
    
    protected String getErrorString(final String value, final String to) {
        return "\"" + this.getOptionName() + "\": couldn't convert \"" + value + "\" to " + to;
    }
}
