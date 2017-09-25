package com.beust.jcommander;

import java.io.*;
import java.util.*;
import com.beust.jcommander.converters.*;
import java.lang.reflect.*;
import com.beust.jcommander.internal.*;

public class JCommander
{
    public static final String DEBUG_PROPERTY = "jcommander.debug";
    private Map<FuzzyMap.IKey, ParameterDescription> m_descriptions;
    private List<Object> m_objects;
    private Parameterized m_mainParameter;
    private Object m_mainParameterObject;
    private Parameter m_mainParameterAnnotation;
    private ParameterDescription m_mainParameterDescription;
    private Map<Parameterized, ParameterDescription> m_requiredFields;
    private Map<Parameterized, ParameterDescription> m_fields;
    private ResourceBundle m_bundle;
    private IDefaultProvider m_defaultProvider;
    private Map<ProgramName, JCommander> m_commands;
    private Map<FuzzyMap.IKey, ProgramName> aliasMap;
    private String m_parsedCommand;
    private String m_parsedAlias;
    private ProgramName m_programName;
    private Comparator<? super ParameterDescription> m_parameterDescriptionComparator;
    private int m_columnSize;
    private boolean m_helpWasSpecified;
    private List<String> m_unknownArgs;
    private boolean m_acceptUnknownOptions;
    private static Console m_console;
    private static LinkedList<IStringConverterFactory> CONVERTER_FACTORIES;
    private final IVariableArity DEFAULT_VARIABLE_ARITY;
    private int m_verbose;
    private boolean m_caseSensitiveOptions;
    private boolean m_allowAbbreviatedOptions;
    
    public JCommander() {
        super();
        this.m_objects = Lists.newArrayList();
        this.m_mainParameter = null;
        this.m_requiredFields = Maps.newHashMap();
        this.m_fields = Maps.newHashMap();
        this.m_commands = Maps.newLinkedHashMap();
        this.aliasMap = Maps.newLinkedHashMap();
        this.m_parameterDescriptionComparator = new Comparator<ParameterDescription>() {
            public int compare(final ParameterDescription p0, final ParameterDescription p1) {
                return p0.getLongestName().compareTo(p1.getLongestName());
            }
        };
        this.m_columnSize = 79;
        this.m_unknownArgs = Lists.newArrayList();
        this.m_acceptUnknownOptions = false;
        this.DEFAULT_VARIABLE_ARITY = new DefaultVariableArity();
        this.m_verbose = 0;
        this.m_caseSensitiveOptions = true;
        this.m_allowAbbreviatedOptions = false;
    }
    
    public JCommander(final Object object) {
        super();
        this.m_objects = Lists.newArrayList();
        this.m_mainParameter = null;
        this.m_requiredFields = Maps.newHashMap();
        this.m_fields = Maps.newHashMap();
        this.m_commands = Maps.newLinkedHashMap();
        this.aliasMap = Maps.newLinkedHashMap();
        this.m_parameterDescriptionComparator = new Comparator<ParameterDescription>() {
            public int compare(final ParameterDescription p0, final ParameterDescription p1) {
                return p0.getLongestName().compareTo(p1.getLongestName());
            }
        };
        this.m_columnSize = 79;
        this.m_unknownArgs = Lists.newArrayList();
        this.m_acceptUnknownOptions = false;
        this.DEFAULT_VARIABLE_ARITY = new DefaultVariableArity();
        this.m_verbose = 0;
        this.m_caseSensitiveOptions = true;
        this.m_allowAbbreviatedOptions = false;
        this.addObject(object);
        this.createDescriptions();
    }
    
    public JCommander(final Object object, @Nullable final ResourceBundle bundle) {
        super();
        this.m_objects = Lists.newArrayList();
        this.m_mainParameter = null;
        this.m_requiredFields = Maps.newHashMap();
        this.m_fields = Maps.newHashMap();
        this.m_commands = Maps.newLinkedHashMap();
        this.aliasMap = Maps.newLinkedHashMap();
        this.m_parameterDescriptionComparator = new Comparator<ParameterDescription>() {
            public int compare(final ParameterDescription p0, final ParameterDescription p1) {
                return p0.getLongestName().compareTo(p1.getLongestName());
            }
        };
        this.m_columnSize = 79;
        this.m_unknownArgs = Lists.newArrayList();
        this.m_acceptUnknownOptions = false;
        this.DEFAULT_VARIABLE_ARITY = new DefaultVariableArity();
        this.m_verbose = 0;
        this.m_caseSensitiveOptions = true;
        this.m_allowAbbreviatedOptions = false;
        this.addObject(object);
        this.setDescriptionsBundle(bundle);
    }
    
    public JCommander(final Object object, final ResourceBundle bundle, final String... args) {
        super();
        this.m_objects = Lists.newArrayList();
        this.m_mainParameter = null;
        this.m_requiredFields = Maps.newHashMap();
        this.m_fields = Maps.newHashMap();
        this.m_commands = Maps.newLinkedHashMap();
        this.aliasMap = Maps.newLinkedHashMap();
        this.m_parameterDescriptionComparator = new Comparator<ParameterDescription>() {
            public int compare(final ParameterDescription p0, final ParameterDescription p1) {
                return p0.getLongestName().compareTo(p1.getLongestName());
            }
        };
        this.m_columnSize = 79;
        this.m_unknownArgs = Lists.newArrayList();
        this.m_acceptUnknownOptions = false;
        this.DEFAULT_VARIABLE_ARITY = new DefaultVariableArity();
        this.m_verbose = 0;
        this.m_caseSensitiveOptions = true;
        this.m_allowAbbreviatedOptions = false;
        this.addObject(object);
        this.setDescriptionsBundle(bundle);
        this.parse(args);
    }
    
    public JCommander(final Object object, final String... args) {
        super();
        this.m_objects = Lists.newArrayList();
        this.m_mainParameter = null;
        this.m_requiredFields = Maps.newHashMap();
        this.m_fields = Maps.newHashMap();
        this.m_commands = Maps.newLinkedHashMap();
        this.aliasMap = Maps.newLinkedHashMap();
        this.m_parameterDescriptionComparator = new Comparator<ParameterDescription>() {
            public int compare(final ParameterDescription p0, final ParameterDescription p1) {
                return p0.getLongestName().compareTo(p1.getLongestName());
            }
        };
        this.m_columnSize = 79;
        this.m_unknownArgs = Lists.newArrayList();
        this.m_acceptUnknownOptions = false;
        this.DEFAULT_VARIABLE_ARITY = new DefaultVariableArity();
        this.m_verbose = 0;
        this.m_caseSensitiveOptions = true;
        this.m_allowAbbreviatedOptions = false;
        this.addObject(object);
        this.parse(args);
    }
    
    public static Console getConsole() {
        if (JCommander.m_console == null) {
            try {
                final Method consoleMethod = System.class.getDeclaredMethod("console", (Class<?>[])new Class[0]);
                final Object console = consoleMethod.invoke(null, new Object[0]);
                JCommander.m_console = new JDK6Console(console);
            }
            catch (Throwable t) {
                JCommander.m_console = new DefaultConsole();
            }
        }
        return JCommander.m_console;
    }
    
    public final void addObject(final Object object) {
        if (object instanceof Iterable) {
            for (final Object o : (Iterable)object) {
                this.m_objects.add(o);
            }
        }
        else if (object.getClass().isArray()) {
            for (final Object o2 : (Object[])object) {
                this.m_objects.add(o2);
            }
        }
        else {
            this.m_objects.add(object);
        }
    }
    
    public final void setDescriptionsBundle(final ResourceBundle bundle) {
        this.m_bundle = bundle;
    }
    
    public void parse(final String... args) {
        this.parse(true, args);
    }
    
    public void parseWithoutValidation(final String... args) {
        this.parse(false, args);
    }
    
    private void parse(final boolean validate, final String... args) {
        final StringBuilder sb = new StringBuilder("Parsing \"");
        sb.append((CharSequence)this.join(args).append("\"\n  with:").append((CharSequence)this.join(this.m_objects.toArray())));
        this.p(sb.toString());
        if (this.m_descriptions == null) {
            this.createDescriptions();
        }
        this.initializeDefaultValues();
        this.parseValues(this.expandArgs(args), validate);
        if (validate) {
            this.validateOptions();
        }
    }
    
    private StringBuilder join(final Object[] args) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < args.length; ++i) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(args[i]);
        }
        return result;
    }
    
    private void initializeDefaultValues() {
        if (this.m_defaultProvider != null) {
            for (final ParameterDescription pd : this.m_descriptions.values()) {
                this.initializeDefaultValue(pd);
            }
            for (final Map.Entry<ProgramName, JCommander> entry : this.m_commands.entrySet()) {
                entry.getValue().initializeDefaultValues();
            }
        }
    }
    
    private void validateOptions() {
        if (this.m_helpWasSpecified) {
            return;
        }
        if (!this.m_requiredFields.isEmpty()) {
            final StringBuilder missingFields = new StringBuilder();
            for (final ParameterDescription pd : this.m_requiredFields.values()) {
                missingFields.append(pd.getNames()).append(" ");
            }
            throw new ParameterException("The following " + pluralize(this.m_requiredFields.size(), "option is required: ", "options are required: ") + (Object)missingFields);
        }
        if (this.m_mainParameterDescription != null && this.m_mainParameterDescription.getParameter().required() && !this.m_mainParameterDescription.isAssigned()) {
            throw new ParameterException("Main parameters are required (\"" + this.m_mainParameterDescription.getDescription() + "\")");
        }
    }
    
    private static String pluralize(final int quantity, final String singular, final String plural) {
        return (quantity == 1) ? singular : plural;
    }
    
    private String[] expandArgs(final String[] originalArgv) {
        final List<String> vResult1 = Lists.newArrayList();
        for (final String arg : originalArgv) {
            if (arg.startsWith("@")) {
                final String fileName = arg.substring(1);
                vResult1.addAll(readFile(fileName));
            }
            else {
                final List<String> expanded = this.expandDynamicArg(arg);
                vResult1.addAll(expanded);
            }
        }
        final List<String> vResult2 = Lists.newArrayList();
        for (int i = 0; i < vResult1.size(); ++i) {
            final String arg2 = vResult1.get(i);
            final String[] v1 = vResult1.toArray(new String[0]);
            if (this.isOption(v1, arg2)) {
                final String sep = this.getSeparatorFor(v1, arg2);
                if (!" ".equals(sep)) {
                    final String[] arr$;
                    final String[] sp = arr$ = arg2.split("[" + sep + "]", 2);
                    for (final String ssp : arr$) {
                        vResult2.add(ssp);
                    }
                }
                else {
                    vResult2.add(arg2);
                }
            }
            else {
                vResult2.add(arg2);
            }
        }
        return vResult2.toArray(new String[vResult2.size()]);
    }
    
    private List<String> expandDynamicArg(final String arg) {
        for (final ParameterDescription pd : this.m_descriptions.values()) {
            if (pd.isDynamicParameter()) {
                for (final String name : pd.getParameter().names()) {
                    if (arg.startsWith(name) && !arg.equals(name)) {
                        return Arrays.asList(name, arg.substring(name.length()));
                    }
                }
            }
        }
        return Arrays.asList(arg);
    }
    
    private boolean isOption(final String[] args, final String arg) {
        final String prefixes = this.getOptionPrefixes(args, arg);
        return arg.length() > 0 && prefixes.indexOf(arg.charAt(0)) >= 0;
    }
    
    private ParameterDescription getPrefixDescriptionFor(final String arg) {
        for (final Map.Entry<FuzzyMap.IKey, ParameterDescription> es : this.m_descriptions.entrySet()) {
            if (arg.startsWith(es.getKey().getName())) {
                return es.getValue();
            }
        }
        return null;
    }
    
    private ParameterDescription getDescriptionFor(final String[] args, final String arg) {
        ParameterDescription result = this.getPrefixDescriptionFor(arg);
        if (result != null) {
            return result;
        }
        for (final String a : args) {
            final ParameterDescription pd = this.getPrefixDescriptionFor(arg);
            if (pd != null) {
                result = pd;
            }
            if (a.equals(arg)) {
                return result;
            }
        }
        throw new ParameterException("Unknown parameter: " + arg);
    }
    
    private String getSeparatorFor(final String[] args, final String arg) {
        final ParameterDescription pd = this.getDescriptionFor(args, arg);
        if (pd != null) {
            final Parameters p = pd.getObject().getClass().getAnnotation(Parameters.class);
            if (p != null) {
                return p.separators();
            }
        }
        return " ";
    }
    
    private String getOptionPrefixes(final String[] args, final String arg) {
        final ParameterDescription pd = this.getDescriptionFor(args, arg);
        if (pd != null) {
            final Parameters p = pd.getObject().getClass().getAnnotation(Parameters.class);
            if (p != null) {
                return p.optionPrefixes();
            }
        }
        String result = "-";
        final StringBuilder sb = new StringBuilder();
        for (final Object o : this.m_objects) {
            final Parameters p2 = o.getClass().getAnnotation(Parameters.class);
            if (p2 != null && !"-".equals(p2.optionPrefixes())) {
                sb.append(p2.optionPrefixes());
            }
        }
        if (!Strings.isStringEmpty(sb.toString())) {
            result = sb.toString();
        }
        return result;
    }
    
    private static List<String> readFile(final String fileName) {
        final List<String> result = Lists.newArrayList();
        try {
            final BufferedReader bufRead = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = bufRead.readLine()) != null) {
                if (line.length() > 0) {
                    result.add(line);
                }
            }
            bufRead.close();
        }
        catch (IOException e) {
            throw new ParameterException("Could not read file " + fileName + ": " + e);
        }
        return result;
    }
    
    private static String trim(final String string) {
        String result = string.trim();
        if (result.startsWith("\"") && result.endsWith("\"") && result.length() > 1) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }
    
    private void createDescriptions() {
        this.m_descriptions = Maps.newHashMap();
        for (final Object object : this.m_objects) {
            this.addDescription(object);
        }
    }
    
    private void addDescription(final Object object) {
        final Class<?> cls = object.getClass();
        final List<Parameterized> parameterizeds = Parameterized.parseArg(object);
        for (final Parameterized parameterized : parameterizeds) {
            final WrappedParameter wp = parameterized.getWrappedParameter();
            if (wp != null && wp.getParameter() != null) {
                final Parameter p;
                final Parameter annotation = p = wp.getParameter();
                if (p.names().length == 0) {
                    this.p("Found main parameter:" + parameterized);
                    if (this.m_mainParameter != null) {
                        throw new ParameterException("Only one @Parameter with no names attribute is allowed, found:" + this.m_mainParameter + " and " + parameterized);
                    }
                    this.m_mainParameter = parameterized;
                    this.m_mainParameterObject = object;
                    this.m_mainParameterAnnotation = p;
                    this.m_mainParameterDescription = new ParameterDescription(object, p, parameterized, this.m_bundle, this);
                }
                else {
                    for (final String name : p.names()) {
                        if (this.m_descriptions.containsKey(new StringKey(name))) {
                            throw new ParameterException("Found the option " + name + " multiple times");
                        }
                        this.p("Adding description for " + name);
                        final ParameterDescription pd = new ParameterDescription(object, p, parameterized, this.m_bundle, this);
                        this.m_fields.put(parameterized, pd);
                        this.m_descriptions.put(new StringKey(name), pd);
                        if (p.required()) {
                            this.m_requiredFields.put(parameterized, pd);
                        }
                    }
                }
            }
            else if (parameterized.getDelegateAnnotation() != null) {
                final Object delegateObject = parameterized.get(object);
                if (delegateObject == null) {
                    throw new ParameterException("Delegate field '" + parameterized.getName() + "' cannot be null.");
                }
                this.addDescription(delegateObject);
            }
            else {
                if (wp == null || wp.getDynamicParameter() == null) {
                    continue;
                }
                final DynamicParameter dp = wp.getDynamicParameter();
                for (final String name2 : dp.names()) {
                    if (this.m_descriptions.containsKey(name2)) {
                        throw new ParameterException("Found the option " + name2 + " multiple times");
                    }
                    this.p("Adding description for " + name2);
                    final ParameterDescription pd2 = new ParameterDescription(object, dp, parameterized, this.m_bundle, this);
                    this.m_fields.put(parameterized, pd2);
                    this.m_descriptions.put(new StringKey(name2), pd2);
                    if (dp.required()) {
                        this.m_requiredFields.put(parameterized, pd2);
                    }
                }
            }
        }
    }
    
    private void initializeDefaultValue(final ParameterDescription pd) {
        for (final String optionName : pd.getParameter().names()) {
            final String def = this.m_defaultProvider.getDefaultValueFor(optionName);
            if (def != null) {
                this.p("Initializing " + optionName + " with default value:" + def);
                pd.addValue(def, true);
                return;
            }
        }
    }
    
    private void parseValues(final String[] args, final boolean validate) {
        boolean commandParsed = false;
        int increment;
        for (int i = 0; i < args.length && !commandParsed; i += increment) {
            final String arg = args[i];
            final String a = trim(arg);
            this.p("Parsing arg: " + a);
            final JCommander jc = this.findCommandByAlias(arg);
            increment = 1;
            if (this.isOption(args, a) && jc == null) {
                final ParameterDescription pd = this.findParameterDescription(a);
                if (pd != null) {
                    if (pd.getParameter().password()) {
                        final char[] password = this.readPassword(pd.getDescription(), pd.getParameter().echoInput());
                        pd.addValue(new String(password));
                        this.m_requiredFields.remove(pd.getParameterized());
                    }
                    else if (pd.getParameter().variableArity()) {
                        increment = this.processVariableArity(args, i, pd);
                    }
                    else {
                        final Class<?> fieldType = pd.getParameterized().getType();
                        if ((fieldType == Boolean.TYPE || fieldType == Boolean.class) && pd.getParameter().arity() == -1) {
                            pd.addValue("true");
                            this.m_requiredFields.remove(pd.getParameterized());
                        }
                        else {
                            increment = this.processFixedArity(args, i, pd, fieldType);
                        }
                        if (pd.isHelp()) {
                            this.m_helpWasSpecified = true;
                        }
                    }
                }
                else {
                    if (!this.m_acceptUnknownOptions) {
                        throw new ParameterException("Unknown option: " + arg);
                    }
                    this.m_unknownArgs.add(arg);
                    ++i;
                    while (i < args.length && !this.isOption(args, args[i])) {
                        this.m_unknownArgs.add(args[i++]);
                    }
                    increment = 0;
                }
            }
            else if (!Strings.isStringEmpty(arg)) {
                if (this.m_commands.isEmpty()) {
                    final List mp = this.getMainParameter(arg);
                    Object convertedValue;
                    final String value = (String)(convertedValue = arg);
                    if (this.m_mainParameter.getGenericType() instanceof ParameterizedType) {
                        final ParameterizedType p = (ParameterizedType)this.m_mainParameter.getGenericType();
                        final Type cls = p.getActualTypeArguments()[0];
                        if (cls instanceof Class) {
                            convertedValue = this.convertValue(this.m_mainParameter, (Class)cls, value);
                        }
                    }
                    ParameterDescription.validateParameter(this.m_mainParameterDescription, this.m_mainParameterAnnotation.validateWith(), "Default", value);
                    this.m_mainParameterDescription.setAssigned(true);
                    mp.add(convertedValue);
                }
                else {
                    if (jc == null && validate) {
                        throw new MissingCommandException("Expected a command, got " + arg);
                    }
                    if (jc != null) {
                        this.m_parsedCommand = ProgramName.access$000(jc.m_programName);
                        this.m_parsedAlias = arg;
                        jc.parse(this.subArray(args, i + 1));
                        commandParsed = true;
                    }
                }
            }
        }
        for (final ParameterDescription parameterDescription : this.m_descriptions.values()) {
            if (parameterDescription.isAssigned()) {
                this.m_fields.get(parameterDescription.getParameterized()).setAssigned(true);
            }
        }
    }
    
    private int processVariableArity(final String[] args, final int index, final ParameterDescription pd) {
        final Object arg = pd.getObject();
        IVariableArity va;
        if (!(arg instanceof IVariableArity)) {
            va = this.DEFAULT_VARIABLE_ARITY;
        }
        else {
            va = (IVariableArity)arg;
        }
        final List<String> currentArgs = Lists.newArrayList();
        for (int j = index + 1; j < args.length; ++j) {
            currentArgs.add(args[j]);
        }
        final int arity = va.processVariableArity(pd.getParameter().names()[0], currentArgs.toArray(new String[0]));
        final int result = this.processFixedArity(args, index, pd, List.class, arity);
        return result;
    }
    
    private int processFixedArity(final String[] args, final int index, final ParameterDescription pd, final Class<?> fieldType) {
        final int arity = pd.getParameter().arity();
        final int n = (arity != -1) ? arity : 1;
        return this.processFixedArity(args, index, pd, fieldType, n);
    }
    
    private int processFixedArity(final String[] args, final int originalIndex, final ParameterDescription pd, final Class<?> fieldType, final int arity) {
        final String arg = args[originalIndex];
        if (arity == 0 && (Boolean.class.isAssignableFrom(fieldType) || Boolean.TYPE.isAssignableFrom(fieldType))) {
            pd.addValue("true");
            this.m_requiredFields.remove(pd.getParameterized());
        }
        else {
            if (originalIndex >= args.length - 1) {
                throw new ParameterException("Expected a value after parameter " + arg);
            }
            final int offset = "--".equals(args[originalIndex + 1]) ? 1 : 0;
            if (originalIndex + arity >= args.length) {
                throw new ParameterException("Expected " + arity + " values after " + arg);
            }
            for (int j = 1; j <= arity; ++j) {
                pd.addValue(trim(args[originalIndex + j + offset]));
                this.m_requiredFields.remove(pd.getParameterized());
            }
            final int index = originalIndex + (arity + offset);
        }
        return arity + 1;
    }
    
    private char[] readPassword(final String description, final boolean echoInput) {
        getConsole().print(description + ": ");
        return getConsole().readPassword(echoInput);
    }
    
    private String[] subArray(final String[] args, final int index) {
        final int l = args.length - index;
        final String[] result = new String[l];
        System.arraycopy(args, index, result, 0, l);
        return result;
    }
    
    private List<?> getMainParameter(final String arg) {
        if (this.m_mainParameter == null) {
            throw new ParameterException("Was passed main parameter '" + arg + "' but no main parameter was defined");
        }
        List<?> result = (List<?>)this.m_mainParameter.get(this.m_mainParameterObject);
        if (result == null) {
            result = Lists.newArrayList();
            if (!List.class.isAssignableFrom(this.m_mainParameter.getType())) {
                throw new ParameterException("Main parameter field " + this.m_mainParameter + " needs to be of type List, not " + this.m_mainParameter.getType());
            }
            this.m_mainParameter.set(this.m_mainParameterObject, result);
        }
        return result;
    }
    
    public String getMainParameterDescription() {
        if (this.m_descriptions == null) {
            this.createDescriptions();
        }
        return (this.m_mainParameterAnnotation != null) ? this.m_mainParameterAnnotation.description() : null;
    }
    
    public void setProgramName(final String name) {
        this.setProgramName(name, new String[0]);
    }
    
    public void setProgramName(final String name, final String... aliases) {
        this.m_programName = new ProgramName(name, Arrays.asList(aliases));
    }
    
    public void usage(final String commandName) {
        final StringBuilder sb = new StringBuilder();
        this.usage(commandName, sb);
        getConsole().println(sb.toString());
    }
    
    public void usage(final String commandName, final StringBuilder out) {
        this.usage(commandName, out, "");
    }
    
    public void usage(final String commandName, final StringBuilder out, final String indent) {
        final String description = this.getCommandDescription(commandName);
        final JCommander jc = this.findCommandByAlias(commandName);
        if (description != null) {
            out.append(indent).append(description);
            out.append("\n");
        }
        jc.usage(out, indent);
    }
    
    public String getCommandDescription(final String commandName) {
        final JCommander jc = this.findCommandByAlias(commandName);
        if (jc == null) {
            throw new ParameterException("Asking description for unknown command: " + commandName);
        }
        final Object arg = jc.getObjects().get(0);
        final Parameters p = arg.getClass().getAnnotation(Parameters.class);
        ResourceBundle bundle = null;
        String result = null;
        if (p != null) {
            result = p.commandDescription();
            final String bundleName = p.resourceBundle();
            if (!"".equals(bundleName)) {
                bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
            }
            else {
                bundle = this.m_bundle;
            }
            if (bundle != null) {
                result = this.getI18nString(bundle, p.commandDescriptionKey(), p.commandDescription());
            }
        }
        return result;
    }
    
    private String getI18nString(final ResourceBundle bundle, final String key, final String def) {
        final String s = (bundle != null) ? bundle.getString(key) : null;
        return (s != null) ? s : def;
    }
    
    public void usage() {
        final StringBuilder sb = new StringBuilder();
        this.usage(sb);
        getConsole().println(sb.toString());
    }
    
    public void usage(final StringBuilder out) {
        this.usage(out, "");
    }
    
    public void usage(final StringBuilder out, final String indent) {
        if (this.m_descriptions == null) {
            this.createDescriptions();
        }
        final boolean hasCommands = !this.m_commands.isEmpty();
        final String programName = (this.m_programName != null) ? ProgramName.access$300(this.m_programName) : "<main class>";
        out.append(indent).append("Usage: " + programName + " [options]");
        if (hasCommands) {
            out.append(indent).append(" [command] [command options]");
        }
        if (this.m_mainParameterDescription != null) {
            out.append(" " + this.m_mainParameterDescription.getDescription());
        }
        out.append("\n");
        int longestName = 0;
        final List<ParameterDescription> sorted = Lists.newArrayList();
        for (final ParameterDescription pd : this.m_fields.values()) {
            if (!pd.getParameter().hidden()) {
                sorted.add(pd);
                final int length = pd.getNames().length() + 2;
                if (length <= longestName) {
                    continue;
                }
                longestName = length;
            }
        }
        Collections.sort(sorted, this.getParameterDescriptionComparator());
        final int descriptionIndent = 6;
        if (sorted.size() > 0) {
            out.append(indent).append("  Options:\n");
        }
        for (final ParameterDescription pd2 : sorted) {
            final WrappedParameter parameter = pd2.getParameter();
            out.append(indent).append("  " + (parameter.required() ? "* " : "  ") + pd2.getNames() + "\n" + indent + this.s(descriptionIndent));
            final int indentCount = indent.length() + descriptionIndent;
            this.wrapDescription(out, indentCount, pd2.getDescription());
            final Object def = pd2.getDefault();
            if (pd2.isDynamicParameter()) {
                out.append("\n" + this.s(indentCount + 1)).append("Syntax: " + parameter.names()[0] + "key" + parameter.getAssignment() + "value");
            }
            if (def != null) {
                final String displayedDef = Strings.isStringEmpty(def.toString()) ? "<empty string>" : def.toString();
                out.append("\n" + this.s(indentCount + 1)).append("Default: " + (parameter.password() ? "********" : displayedDef));
            }
            out.append("\n");
        }
        if (hasCommands) {
            out.append("  Commands:\n");
            for (final Map.Entry<ProgramName, JCommander> commands : this.m_commands.entrySet()) {
                final ProgramName progName = commands.getKey();
                final String dispName = ProgramName.access$300(progName);
                out.append(indent).append("    " + dispName);
                this.usage(progName.getName(), out, "      ");
                out.append("\n");
            }
        }
    }
    
    private Comparator<? super ParameterDescription> getParameterDescriptionComparator() {
        return this.m_parameterDescriptionComparator;
    }
    
    public void setParameterDescriptionComparator(final Comparator<? super ParameterDescription> c) {
        this.m_parameterDescriptionComparator = c;
    }
    
    public void setColumnSize(final int columnSize) {
        this.m_columnSize = columnSize;
    }
    
    public int getColumnSize() {
        return this.m_columnSize;
    }
    
    private void wrapDescription(final StringBuilder out, final int indent, final String description) {
        final int max = this.getColumnSize();
        final String[] words = description.split(" ");
        int current = indent;
        for (int i = 0; i < words.length; ++i) {
            final String word = words[i];
            if (word.length() > max || current + word.length() <= max) {
                out.append(" ").append(word);
                current += word.length() + 1;
            }
            else {
                out.append("\n").append(this.s(indent + 1)).append(word);
                current = indent;
            }
        }
    }
    
    public List<ParameterDescription> getParameters() {
        return new ArrayList<ParameterDescription>(this.m_fields.values());
    }
    
    public ParameterDescription getMainParameter() {
        return this.m_mainParameterDescription;
    }
    
    private void p(final String string) {
        if (this.m_verbose > 0 || System.getProperty("jcommander.debug") != null) {
            getConsole().println("[JCommander] " + string);
        }
    }
    
    public void setDefaultProvider(final IDefaultProvider defaultProvider) {
        this.m_defaultProvider = defaultProvider;
        for (final Map.Entry<ProgramName, JCommander> entry : this.m_commands.entrySet()) {
            entry.getValue().setDefaultProvider(defaultProvider);
        }
    }
    
    public void addConverterFactory(final IStringConverterFactory converterFactory) {
        JCommander.CONVERTER_FACTORIES.addFirst(converterFactory);
    }
    
    public <T> Class<? extends IStringConverter<T>> findConverter(final Class<T> cls) {
        for (final IStringConverterFactory f : JCommander.CONVERTER_FACTORIES) {
            final Class<? extends IStringConverter<T>> result = f.getConverter(cls);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    
    public Object convertValue(final ParameterDescription pd, final String value) {
        return this.convertValue(pd.getParameterized(), pd.getParameterized().getType(), value);
    }
    
    public Object convertValue(final Parameterized parameterized, final Class type, final String value) {
        final Parameter annotation = parameterized.getParameter();
        if (annotation == null) {
            return value;
        }
        Class<? extends IStringConverter<?>> converterClass = annotation.converter();
        final boolean listConverterWasSpecified = annotation.listConverter() != NoConverter.class;
        if (converterClass == null || converterClass == NoConverter.class) {
            if (type.isEnum()) {
                converterClass = type;
            }
            else {
                converterClass = this.findConverter(type);
            }
        }
        if (converterClass == null) {
            final Type elementType = parameterized.findFieldGenericType();
            converterClass = (Class<? extends IStringConverter<?>>)((elementType != null) ? this.findConverter((Class)elementType) : StringConverter.class);
            if (converterClass == null && Enum.class.isAssignableFrom((Class)elementType)) {
                converterClass = (Class)elementType;
            }
        }
        Object result = null;
        try {
            final String[] names = annotation.names();
            final String optionName = (names.length > 0) ? names[0] : "[Main class]";
            if (converterClass != null && converterClass.isEnum()) {
                try {
                    result = Enum.valueOf(converterClass, value.toUpperCase());
                    return result;
                }
                catch (Exception e4) {
                    throw new ParameterException("Invalid value for " + optionName + " parameter. Allowed values:" + EnumSet.allOf(converterClass));
                }
            }
            final IStringConverter<?> converter = this.instantiateConverter(optionName, converterClass);
            if (type.isAssignableFrom(List.class) && parameterized.getGenericType() instanceof ParameterizedType) {
                if (listConverterWasSpecified) {
                    final IStringConverter<?> listConverter = this.instantiateConverter(optionName, annotation.listConverter());
                    result = listConverter.convert(value);
                }
                else {
                    result = this.convertToList(value, converter, annotation.splitter());
                }
            }
            else {
                result = converter.convert(value);
            }
        }
        catch (InstantiationException e) {
            throw new ParameterException(e);
        }
        catch (IllegalAccessException e2) {
            throw new ParameterException(e2);
        }
        catch (InvocationTargetException e3) {
            throw new ParameterException(e3);
        }
        return result;
    }
    
    private Object convertToList(final String value, final IStringConverter<?> converter, final Class<? extends IParameterSplitter> splitterClass) throws InstantiationException, IllegalAccessException {
        final IParameterSplitter splitter = (IParameterSplitter)splitterClass.newInstance();
        final List<Object> result = Lists.newArrayList();
        for (final String param : splitter.split(value)) {
            result.add(converter.convert(param));
        }
        return result;
    }
    
    private IStringConverter<?> instantiateConverter(final String optionName, final Class<? extends IStringConverter<?>> converterClass) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<IStringConverter<?>> ctor = null;
        Constructor<IStringConverter<?>> stringCtor = null;
        final Constructor[] arr$;
        final Constructor<IStringConverter<?>>[] ctors = (Constructor<IStringConverter<?>>[])(arr$ = converterClass.getDeclaredConstructors());
        for (final Constructor<IStringConverter<?>> c : arr$) {
            final Class<?>[] types = c.getParameterTypes();
            if (types.length == 1 && types[0].equals(String.class)) {
                stringCtor = c;
            }
            else if (types.length == 0) {
                ctor = c;
            }
        }
        final IStringConverter<?> result = (stringCtor != null) ? stringCtor.newInstance(optionName) : ((ctor != null) ? ctor.newInstance(new Object[0]) : null);
        return result;
    }
    
    public void addCommand(final String name, final Object object) {
        this.addCommand(name, object, new String[0]);
    }
    
    public void addCommand(final Object object) {
        final Parameters p = object.getClass().getAnnotation(Parameters.class);
        if (p != null && p.commandNames().length > 0) {
            for (final String commandName : p.commandNames()) {
                this.addCommand(commandName, object);
            }
            return;
        }
        throw new ParameterException("Trying to add command " + object.getClass().getName() + " without specifying its names in @Parameters");
    }
    
    public void addCommand(final String name, final Object object, final String... aliases) {
        final JCommander jc = new JCommander(object);
        jc.setProgramName(name, aliases);
        jc.setDefaultProvider(this.m_defaultProvider);
        final ProgramName progName = jc.m_programName;
        this.m_commands.put(progName, jc);
        this.aliasMap.put(new StringKey(name), progName);
        for (final String a : aliases) {
            final FuzzyMap.IKey alias = new StringKey(a);
            if (!alias.equals(name)) {
                final ProgramName mappedName = this.aliasMap.get(alias);
                if (mappedName != null && !mappedName.equals(progName)) {
                    throw new ParameterException("Cannot set alias " + alias + " for " + name + " command because it has already been defined for " + ProgramName.access$000(mappedName) + " command");
                }
                this.aliasMap.put(alias, progName);
            }
        }
    }
    
    public Map<String, JCommander> getCommands() {
        final Map<String, JCommander> res = Maps.newLinkedHashMap();
        for (final Map.Entry<ProgramName, JCommander> entry : this.m_commands.entrySet()) {
            res.put(ProgramName.access$000(entry.getKey()), entry.getValue());
        }
        return res;
    }
    
    public String getParsedCommand() {
        return this.m_parsedCommand;
    }
    
    public String getParsedAlias() {
        return this.m_parsedAlias;
    }
    
    private String s(final int count) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; ++i) {
            result.append(" ");
        }
        return result.toString();
    }
    
    public List<Object> getObjects() {
        return this.m_objects;
    }
    
    private ParameterDescription findParameterDescription(final String arg) {
        return FuzzyMap.findInMap(this.m_descriptions, new StringKey(arg), this.m_caseSensitiveOptions, this.m_allowAbbreviatedOptions);
    }
    
    private JCommander findCommand(final ProgramName name) {
        return FuzzyMap.findInMap(this.m_commands, name, this.m_caseSensitiveOptions, this.m_allowAbbreviatedOptions);
    }
    
    private ProgramName findProgramName(final String name) {
        return FuzzyMap.findInMap(this.aliasMap, new StringKey(name), this.m_caseSensitiveOptions, this.m_allowAbbreviatedOptions);
    }
    
    private JCommander findCommandByAlias(final String commandOrAlias) {
        final ProgramName progName = this.findProgramName(commandOrAlias);
        if (progName == null) {
            return null;
        }
        final JCommander jc = this.findCommand(progName);
        if (jc == null) {
            throw new IllegalStateException("There appears to be inconsistency in the internal command database.  This is likely a bug. Please report.");
        }
        return jc;
    }
    
    public void setVerbose(final int verbose) {
        this.m_verbose = verbose;
    }
    
    public void setCaseSensitiveOptions(final boolean b) {
        this.m_caseSensitiveOptions = b;
    }
    
    public void setAllowAbbreviatedOptions(final boolean b) {
        this.m_allowAbbreviatedOptions = b;
    }
    
    public void setAcceptUnknownOptions(final boolean b) {
        this.m_acceptUnknownOptions = b;
    }
    
    public List<String> getUnknownOptions() {
        return this.m_unknownArgs;
    }
    
    static /* synthetic */ boolean access$100(final JCommander x0, final String[] x1, final String x2) {
        return x0.isOption(x1, x2);
    }
    
    static {
        (JCommander.CONVERTER_FACTORIES = Lists.newLinkedList()).addFirst(new DefaultConverterFactory());
    }
    
    private class DefaultVariableArity implements IVariableArity
    {
        public int processVariableArity(final String optionName, final String[] options) {
            int i;
            for (i = 0; i < options.length && !JCommander.access$100(JCommander.this, options, options[i]); ++i) {}
            return i;
        }
    }
    
    private static final class ProgramName implements FuzzyMap.IKey
    {
        private final String m_name;
        private final List<String> m_aliases;
        
        ProgramName(final String name, final List<String> aliases) {
            super();
            this.m_name = name;
            this.m_aliases = aliases;
        }
        
        public String getName() {
            return this.m_name;
        }
        
        private String getDisplayName() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.m_name);
            if (!this.m_aliases.isEmpty()) {
                sb.append("(");
                final Iterator<String> aliasesIt = this.m_aliases.iterator();
                while (aliasesIt.hasNext()) {
                    sb.append(aliasesIt.next());
                    if (aliasesIt.hasNext()) {
                        sb.append(",");
                    }
                }
                sb.append(")");
            }
            return sb.toString();
        }
        
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 * result + ((this.m_name == null) ? 0 : this.m_name.hashCode());
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
            final ProgramName other = (ProgramName)obj;
            if (this.m_name == null) {
                if (other.m_name != null) {
                    return false;
                }
            }
            else if (!this.m_name.equals(other.m_name)) {
                return false;
            }
            return true;
        }
        
        public String toString() {
            return this.getDisplayName();
        }
        
        static /* synthetic */ String access$000(final ProgramName x0) {
            return x0.m_name;
        }
        
        static /* synthetic */ String access$300(final ProgramName x0) {
            return x0.getDisplayName();
        }
    }
}
