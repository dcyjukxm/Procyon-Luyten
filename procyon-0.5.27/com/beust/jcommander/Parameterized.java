package com.beust.jcommander;

import java.util.*;
import com.beust.jcommander.internal.*;
import java.lang.annotation.*;
import java.lang.reflect.*;

public class Parameterized
{
    private Field m_field;
    private Method m_method;
    private Method m_getter;
    private WrappedParameter m_wrappedParameter;
    private ParametersDelegate m_parametersDelegate;
    
    public Parameterized(final WrappedParameter wp, final ParametersDelegate pd, final Field field, final Method method) {
        super();
        this.m_wrappedParameter = wp;
        this.m_method = method;
        this.m_field = field;
        if (this.m_field != null) {
            this.m_field.setAccessible(true);
        }
        this.m_parametersDelegate = pd;
    }
    
    public static List<Parameterized> parseArg(final Object arg) {
        final List<Parameterized> result = Lists.newArrayList();
        for (Class<?> cls = arg.getClass(); !Object.class.equals(cls); cls = cls.getSuperclass()) {
            for (final Field f : cls.getDeclaredFields()) {
                final Annotation annotation = f.getAnnotation(Parameter.class);
                final Annotation delegateAnnotation = f.getAnnotation(ParametersDelegate.class);
                final Annotation dynamicParameter = f.getAnnotation(DynamicParameter.class);
                if (annotation != null) {
                    result.add(new Parameterized(new WrappedParameter((Parameter)annotation), null, f, null));
                }
                else if (dynamicParameter != null) {
                    result.add(new Parameterized(new WrappedParameter((DynamicParameter)dynamicParameter), null, f, null));
                }
                else if (delegateAnnotation != null) {
                    result.add(new Parameterized(null, (ParametersDelegate)delegateAnnotation, f, null));
                }
            }
        }
        for (Class<?> cls = arg.getClass(); !Object.class.equals(cls); cls = cls.getSuperclass()) {
            for (final Method m : cls.getDeclaredMethods()) {
                final Annotation annotation = m.getAnnotation(Parameter.class);
                final Annotation delegateAnnotation = m.getAnnotation(ParametersDelegate.class);
                final Annotation dynamicParameter = m.getAnnotation(DynamicParameter.class);
                if (annotation != null) {
                    result.add(new Parameterized(new WrappedParameter((Parameter)annotation), null, null, m));
                }
                else if (dynamicParameter != null) {
                    result.add(new Parameterized(new WrappedParameter((DynamicParameter)annotation), null, null, m));
                }
                else if (delegateAnnotation != null) {
                    result.add(new Parameterized(null, (ParametersDelegate)delegateAnnotation, null, m));
                }
            }
        }
        return result;
    }
    
    public WrappedParameter getWrappedParameter() {
        return this.m_wrappedParameter;
    }
    
    public Class<?> getType() {
        if (this.m_method != null) {
            return this.m_method.getParameterTypes()[0];
        }
        return this.m_field.getType();
    }
    
    public String getName() {
        if (this.m_method != null) {
            return this.m_method.getName();
        }
        return this.m_field.getName();
    }
    
    public Object get(final Object object) {
        try {
            if (this.m_method != null) {
                if (this.m_getter == null) {
                    this.m_getter = this.m_method.getDeclaringClass().getMethod("g" + this.m_method.getName().substring(1), (Class<?>[])new Class[0]);
                }
                return this.m_getter.invoke(object, new Object[0]);
            }
            return this.m_field.get(object);
        }
        catch (SecurityException e) {
            throw new ParameterException(e);
        }
        catch (NoSuchMethodException e5) {
            final String name = this.m_method.getName();
            final String fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
            Object result = null;
            try {
                final Field field = this.m_method.getDeclaringClass().getDeclaredField(fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    result = field.get(object);
                }
            }
            catch (NoSuchFieldException ex) {}
            catch (IllegalAccessException loc_0) {}
            return result;
        }
        catch (IllegalArgumentException e2) {
            throw new ParameterException(e2);
        }
        catch (IllegalAccessException e3) {
            throw new ParameterException(e3);
        }
        catch (InvocationTargetException e4) {
            throw new ParameterException(e4);
        }
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.m_field == null) ? 0 : this.m_field.hashCode());
        result = 31 * result + ((this.m_method == null) ? 0 : this.m_method.hashCode());
        return result;
    }
    
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Parameterized other = (Parameterized)obj;
        if (this.m_field == null) {
            if (other.m_field != null) {
                return false;
            }
        }
        else if (!this.m_field.equals(other.m_field)) {
            return false;
        }
        if (this.m_method == null) {
            if (other.m_method != null) {
                return false;
            }
        }
        else if (!this.m_method.equals(other.m_method)) {
            return false;
        }
        return true;
    }
    
    public boolean isDynamicParameter(final Field field) {
        if (this.m_method != null) {
            return this.m_method.getAnnotation(DynamicParameter.class) != null;
        }
        return this.m_field.getAnnotation(DynamicParameter.class) != null;
    }
    
    public void set(final Object object, final Object value) {
        try {
            if (this.m_method != null) {
                this.m_method.invoke(object, value);
            }
            else {
                this.m_field.set(object, value);
            }
        }
        catch (IllegalArgumentException ex) {
            throw new ParameterException(ex);
        }
        catch (IllegalAccessException ex2) {
            throw new ParameterException(ex2);
        }
        catch (InvocationTargetException ex3) {
            if (ex3.getTargetException() instanceof ParameterException) {
                throw (ParameterException)ex3.getTargetException();
            }
            throw new ParameterException(ex3);
        }
    }
    
    public ParametersDelegate getDelegateAnnotation() {
        return this.m_parametersDelegate;
    }
    
    public Type getGenericType() {
        if (this.m_method != null) {
            return this.m_method.getGenericParameterTypes()[0];
        }
        return this.m_field.getGenericType();
    }
    
    public Parameter getParameter() {
        return this.m_wrappedParameter.getParameter();
    }
    
    public Type findFieldGenericType() {
        if (this.m_method != null) {
            return null;
        }
        if (this.m_field.getGenericType() instanceof ParameterizedType) {
            final ParameterizedType p = (ParameterizedType)this.m_field.getGenericType();
            final Type cls = p.getActualTypeArguments()[0];
            if (cls instanceof Class) {
                return cls;
            }
        }
        return null;
    }
    
    public boolean isDynamicParameter() {
        return this.m_wrappedParameter.getDynamicParameter() != null;
    }
}
