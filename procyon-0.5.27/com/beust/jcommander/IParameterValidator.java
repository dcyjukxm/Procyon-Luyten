package com.beust.jcommander;

public interface IParameterValidator
{
    void validate(String param_0, String param_1) throws ParameterException;
}
