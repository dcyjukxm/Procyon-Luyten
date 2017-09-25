package com.beust.jcommander.validators;

import com.beust.jcommander.*;

public class PositiveInteger implements IParameterValidator
{
    public void validate(final String name, final String value) throws ParameterException {
        final int n = Integer.parseInt(value);
        if (n < 0) {
            throw new ParameterException("Parameter " + name + " should be positive (found " + value + ")");
        }
    }
}
