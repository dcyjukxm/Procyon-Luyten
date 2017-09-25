package com.beust.jcommander.converters;

import com.beust.jcommander.*;

public class IntegerConverter extends BaseConverter<Integer>
{
    public IntegerConverter(final String optionName) {
        super(optionName);
    }
    
    public Integer convert(final String value) {
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ex) {
            throw new ParameterException(this.getErrorString(value, "an integer"));
        }
    }
}
