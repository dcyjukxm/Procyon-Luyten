package com.beust.jcommander.converters;

import com.beust.jcommander.*;

public class FloatConverter extends BaseConverter<Float>
{
    public FloatConverter(final String optionName) {
        super(optionName);
    }
    
    public Float convert(final String value) {
        try {
            return Float.parseFloat(value);
        }
        catch (NumberFormatException ex) {
            throw new ParameterException(this.getErrorString(value, "a float"));
        }
    }
}
