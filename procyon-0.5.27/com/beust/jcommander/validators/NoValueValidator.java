package com.beust.jcommander.validators;

import com.beust.jcommander.*;

public class NoValueValidator<T> implements IValueValidator<T>
{
    public void validate(final String parameterName, final T parameterValue) throws ParameterException {
    }
}
