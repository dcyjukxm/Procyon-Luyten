package com.beust.jcommander.converters;

import com.beust.jcommander.*;

public class LongConverter extends BaseConverter<Long>
{
    public LongConverter(final String optionName) {
        super(optionName);
    }
    
    public Long convert(final String value) {
        try {
            return Long.parseLong(value);
        }
        catch (NumberFormatException ex) {
            throw new ParameterException(this.getErrorString(value, "a long"));
        }
    }
}
