package com.beust.jcommander;

import com.beust.jcommander.validators.*;
import java.util.*;

public class ParameterDescription
{
    private Object m_object;
    private WrappedParameter m_wrappedParameter;
    private Parameter m_parameterAnnotation;
    private DynamicParameter m_dynamicParameterAnnotation;
    private Parameterized m_parameterized;
    private boolean m_assigned;
    private ResourceBundle m_bundle;
    private String m_description;
    private JCommander m_jCommander;
    private Object m_default;
    private String m_longestName;
    
    public ParameterDescription(final Object object, final DynamicParameter annotation, final Parameterized parameterized, final ResourceBundle bundle, final JCommander jc) {
        super();
        this.m_assigned = false;
        this.m_longestName = "";
        if (!Map.class.isAssignableFrom(parameterized.getType())) {
            throw new ParameterException("@DynamicParameter " + parameterized.getName() + " should be of type " + "Map but is " + parameterized.getType().getName());
        }
        this.m_dynamicParameterAnnotation = annotation;
        this.m_wrappedParameter = new WrappedParameter(this.m_dynamicParameterAnnotation);
        this.init(object, parameterized, bundle, jc);
    }
    
    public ParameterDescription(final Object object, final Parameter annotation, final Parameterized parameterized, final ResourceBundle bundle, final JCommander jc) {
        super();
        this.m_assigned = false;
        this.m_longestName = "";
        this.m_parameterAnnotation = annotation;
        this.m_wrappedParameter = new WrappedParameter(this.m_parameterAnnotation);
        this.init(object, parameterized, bundle, jc);
    }
    
    private ResourceBundle findResourceBundle(final Object o) {
        ResourceBundle result = null;
        final Parameters p = o.getClass().getAnnotation(Parameters.class);
        if (p != null && !this.isEmpty(p.resourceBundle())) {
            result = ResourceBundle.getBundle(p.resourceBundle(), Locale.getDefault());
        }
        else {
            final com.beust.jcommander.ResourceBundle a = o.getClass().getAnnotation(com.beust.jcommander.ResourceBundle.class);
            if (a != null && !this.isEmpty(a.value())) {
                result = ResourceBundle.getBundle(a.value(), Locale.getDefault());
            }
        }
        return result;
    }
    
    private boolean isEmpty(final String s) {
        return s == null || "".equals(s);
    }
    
    private void initDescription(final String description, final String descriptionKey, final String[] names) {
        this.m_description = description;
        if (!"".equals(descriptionKey) && this.m_bundle != null) {
            this.m_description = this.m_bundle.getString(descriptionKey);
        }
        for (final String name : names) {
            if (name.length() > this.m_longestName.length()) {
                this.m_longestName = name;
            }
        }
    }
    
    private void init(final Object object, final Parameterized parameterized, final ResourceBundle bundle, final JCommander jCommander) {
        this.m_object = object;
        this.m_parameterized = parameterized;
        this.m_bundle = bundle;
        if (this.m_bundle == null) {
            this.m_bundle = this.findResourceBundle(object);
        }
        this.m_jCommander = jCommander;
        if (this.m_parameterAnnotation != null) {
            String description;
            if (Enum.class.isAssignableFrom(parameterized.getType()) && this.m_parameterAnnotation.description().isEmpty()) {
                description = "Options: " + EnumSet.allOf(parameterized.getType());
            }
            else {
                description = this.m_parameterAnnotation.description();
            }
            this.initDescription(description, this.m_parameterAnnotation.descriptionKey(), this.m_parameterAnnotation.names());
        }
        else {
            if (this.m_dynamicParameterAnnotation == null) {
                throw new AssertionError((Object)"Shound never happen");
            }
            this.initDescription(this.m_dynamicParameterAnnotation.description(), this.m_dynamicParameterAnnotation.descriptionKey(), this.m_dynamicParameterAnnotation.names());
        }
        try {
            this.m_default = parameterized.get(object);
        }
        catch (Exception loc_0) {}
        if (this.m_default != null && this.m_parameterAnnotation != null) {
            this.validateDefaultValues(this.m_parameterAnnotation.names());
        }
    }
    
    private void validateDefaultValues(final String[] names) {
        final String name = (names.length > 0) ? names[0] : "";
        this.validateValueParameter(name, this.m_default);
    }
    
    public String getLongestName() {
        return this.m_longestName;
    }
    
    public Object getDefault() {
        return this.m_default;
    }
    
    public String getDescription() {
        return this.m_description;
    }
    
    public Object getObject() {
        return this.m_object;
    }
    
    public String getNames() {
        final StringBuilder sb = new StringBuilder();
        final String[] names = this.m_wrappedParameter.names();
        for (int i = 0; i < names.length; ++i) {
            if (i > 0) {
                sb.append(", ");
            }
            if (names.length == 1 && names[i].startsWith("--")) {
                sb.append("    ");
            }
            sb.append(names[i]);
        }
        return sb.toString();
    }
    
    public WrappedParameter getParameter() {
        return this.m_wrappedParameter;
    }
    
    public Parameterized getParameterized() {
        return this.m_parameterized;
    }
    
    private boolean isMultiOption() {
        final Class<?> fieldType = this.m_parameterized.getType();
        return fieldType.equals(List.class) || fieldType.equals(Set.class) || this.m_parameterized.isDynamicParameter();
    }
    
    public void addValue(final String value) {
        this.addValue(value, false);
    }
    
    public boolean isAssigned() {
        return this.m_assigned;
    }
    
    public void setAssigned(final boolean b) {
        this.m_assigned = b;
    }
    
    public void addValue(final String value, final boolean isDefault) {
        p("Adding " + (isDefault ? "default " : "") + "value:" + value + " to parameter:" + this.m_parameterized.getName());
        final String name = this.m_wrappedParameter.names()[0];
        if (this.m_assigned && !this.isMultiOption()) {
            throw new ParameterException("Can only specify option " + name + " once.");
        }
        this.validateParameter(name, value);
        final Class<?> type = this.m_parameterized.getType();
        final Object convertedValue = this.m_jCommander.convertValue(this, value);
        this.validateValueParameter(name, convertedValue);
        final boolean isCollection = Collection.class.isAssignableFrom(type);
        if (isCollection) {
            Collection<Object> l = (Collection<Object>)this.m_parameterized.get(this.m_object);
            if (l == null || this.fieldIsSetForTheFirstTime(isDefault)) {
                l = this.newCollection(type);
                this.m_parameterized.set(this.m_object, l);
            }
            if (convertedValue instanceof Collection) {
                l.addAll((Collection)convertedValue);
            }
            else {
                l.add(convertedValue);
            }
        }
        else {
            this.m_wrappedParameter.addValue(this.m_parameterized, this.m_object, convertedValue);
        }
        if (!isDefault) {
            this.m_assigned = true;
        }
    }
    
    private void validateParameter(final String name, final String value) {
        final Class<? extends IParameterValidator> validator = this.m_wrappedParameter.validateWith();
        if (validator != null) {
            validateParameter(this, validator, name, value);
        }
    }
    
    private void validateValueParameter(final String name, final Object value) {
        final Class<? extends IValueValidator> validator = this.m_wrappedParameter.validateValueWith();
        if (validator != null) {
            validateValueParameter(validator, name, value);
        }
    }
    
    public static void validateValueParameter(final Class<? extends IValueValidator> validator, final String name, final Object value) {
        try {
            if (validator != NoValueValidator.class) {
                p("Validating value parameter:" + name + " value:" + value + " validator:" + validator);
            }
            ((IValueValidator)validator.newInstance()).validate(name, value);
        }
        catch (InstantiationException e) {
            throw new ParameterException("Can't instantiate validator:" + e);
        }
        catch (IllegalAccessException e2) {
            throw new ParameterException("Can't instantiate validator:" + e2);
        }
    }
    
    public static void validateParameter(final ParameterDescription pd, final Class<? extends IParameterValidator> validator, final String name, final String value) {
        try {
            if (validator != NoValidator.class) {
                p("Validating parameter:" + name + " value:" + value + " validator:" + validator);
            }
            ((IParameterValidator)validator.newInstance()).validate(name, value);
            if (IParameterValidator2.class.isAssignableFrom(validator)) {
                final IParameterValidator2 instance = (IParameterValidator2)validator.newInstance();
                instance.validate(name, value, pd);
            }
        }
        catch (InstantiationException e) {
            throw new ParameterException("Can't instantiate validator:" + e);
        }
        catch (IllegalAccessException e2) {
            throw new ParameterException("Can't instantiate validator:" + e2);
        }
        catch (ParameterException ex) {
            throw ex;
        }
        catch (Exception ex2) {
            throw new ParameterException(ex2);
        }
    }
    
    private Collection<Object> newCollection(final Class<?> type) {
        if (SortedSet.class.isAssignableFrom(type)) {
            return new TreeSet<Object>();
        }
        if (LinkedHashSet.class.isAssignableFrom(type)) {
            return new LinkedHashSet<Object>();
        }
        if (Set.class.isAssignableFrom(type)) {
            return new HashSet<Object>();
        }
        if (List.class.isAssignableFrom(type)) {
            return new ArrayList<Object>();
        }
        throw new ParameterException("Parameters of Collection type '" + type.getSimpleName() + "' are not supported. Please use List or Set instead.");
    }
    
    private boolean fieldIsSetForTheFirstTime(final boolean isDefault) {
        return !isDefault && !this.m_assigned;
    }
    
    private static void p(final String string) {
        if (System.getProperty("jcommander.debug") != null) {
            JCommander.getConsole().println("[ParameterDescription] " + string);
        }
    }
    
    public String toString() {
        return "[ParameterDescription " + this.m_parameterized.getName() + "]";
    }
    
    public boolean isDynamicParameter() {
        return this.m_dynamicParameterAnnotation != null;
    }
    
    public boolean isHelp() {
        return this.m_wrappedParameter.isHelp();
    }
}
