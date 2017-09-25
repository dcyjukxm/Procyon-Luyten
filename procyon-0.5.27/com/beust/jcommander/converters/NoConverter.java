package com.beust.jcommander.converters;

import com.beust.jcommander.*;

public class NoConverter implements IStringConverter<String>
{
    public String convert(final String value) {
        throw new UnsupportedOperationException();
    }
}
