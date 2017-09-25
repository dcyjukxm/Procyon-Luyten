package com.beust.jcommander.converters;

import com.beust.jcommander.*;

public class StringConverter implements IStringConverter<String>
{
    public String convert(final String value) {
        return value;
    }
}
