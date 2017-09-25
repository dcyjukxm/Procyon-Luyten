package com.beust.jcommander.converters;

import java.util.*;
import com.beust.jcommander.*;
import java.text.*;

public class ISO8601DateConverter extends BaseConverter<Date>
{
    private static final SimpleDateFormat DATE_FORMAT;
    
    public ISO8601DateConverter(final String optionName) {
        super(optionName);
    }
    
    public Date convert(final String value) {
        try {
            return ISO8601DateConverter.DATE_FORMAT.parse(value);
        }
        catch (ParseException pe) {
            throw new ParameterException(this.getErrorString(value, String.format("an ISO-8601 formatted date (%s)", ISO8601DateConverter.DATE_FORMAT.toPattern())));
        }
    }
    
    static {
        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    }
}
