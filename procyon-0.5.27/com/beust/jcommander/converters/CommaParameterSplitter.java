package com.beust.jcommander.converters;

import java.util.*;

public class CommaParameterSplitter implements IParameterSplitter
{
    public List<String> split(final String value) {
        return Arrays.asList(value.split(","));
    }
}
