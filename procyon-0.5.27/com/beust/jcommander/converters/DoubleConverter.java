package com.beust.jcommander.converters;

import com.beust.jcommander.*;

public class DoubleConverter extends BaseConverter<Double>
{
    public DoubleConverter(final String optionName) {
        super(optionName);
    }
    
    public Double convert(final String value) {
        try {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException ex) {
            throw new ParameterException(this.getErrorString(value, "a double"));
        }
    }
}
