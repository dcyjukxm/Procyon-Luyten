package com.beust.jcommander.converters;

import java.math.*;
import com.beust.jcommander.*;

public class BigDecimalConverter extends BaseConverter<BigDecimal>
{
    public BigDecimalConverter(final String optionName) {
        super(optionName);
    }
    
    public BigDecimal convert(final String value) {
        try {
            return new BigDecimal(value);
        }
        catch (NumberFormatException nfe) {
            throw new ParameterException(this.getErrorString(value, "a BigDecimal"));
        }
    }
}
