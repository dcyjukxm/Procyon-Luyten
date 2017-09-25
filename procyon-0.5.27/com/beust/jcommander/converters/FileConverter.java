package com.beust.jcommander.converters;

import com.beust.jcommander.*;
import java.io.*;

public class FileConverter implements IStringConverter<File>
{
    public File convert(final String value) {
        return new File(value);
    }
}
