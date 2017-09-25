package com.beust.jcommander.converters;

import com.beust.jcommander.*;

public class BooleanConverter extends BaseConverter<Boolean>
{
    public BooleanConverter(final String optionName) {
        super(optionName);
    }
    
    public Boolean convert(final String value) {
        if ("false".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        throw new ParameterException(this.getErrorString(value, "a boolean"));
    }
}
