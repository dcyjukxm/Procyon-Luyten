package com.beust.jcommander;

public interface IValueValidator<T>
{
    void validate(String param_0, T param_1) throws ParameterException;
}
