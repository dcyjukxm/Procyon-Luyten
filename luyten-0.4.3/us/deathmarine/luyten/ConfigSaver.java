package us.deathmarine.luyten;

import com.strobel.decompiler.*;
import com.strobel.decompiler.languages.java.*;
import java.util.prefs.*;
import java.lang.reflect.*;
import com.strobel.decompiler.languages.*;
import java.util.*;

public class ConfigSaver
{
    private static final String FLATTEN_SWITCH_BLOCKS_ID = "flattenSwitchBlocks";
    private static final String FORCE_EXPLICIT_IMPORTS_ID = "forceExplicitImports";
    private static final String SHOW_SYNTHETIC_MEMBERS_ID = "showSyntheticMembers";
    private static final String EXCLUDE_NESTED_TYPES_ID = "excludeNestedTypes";
    private static final String FORCE_EXPLICIT_TYPE_ARGUMENTS_ID = "forceExplicitTypeArguments";
    private static final String RETAIN_REDUNDANT_CASTS_ID = "retainRedundantCasts";
    private static final String INCLUDE_ERROR_DIAGNOSTICS_ID = "includeErrorDiagnostics";
    private static final String LANGUAGE_NAME_ID = "languageName";
    private static final String MAIN_WINDOW_ID_PREFIX = "main";
    private static final String FIND_WINDOW_ID_PREFIX = "find";
    private static final String WINDOW_IS_FULL_SCREEN_ID = "WindowIsFullScreen";
    private static final String WINDOW_WIDTH_ID = "WindowWidth";
    private static final String WINDOW_HEIGHT_ID = "WindowHeight";
    private static final String WINDOW_X_ID = "WindowX";
    private static final String WINDOW_Y_ID = "WindowY";
    private DecompilerSettings decompilerSettings;
    private WindowPosition mainWindowPosition;
    private WindowPosition findWindowPosition;
    private LuytenPreferences luytenPreferences;
    private static ConfigSaver theLoadedInstance;
    
    public static ConfigSaver getLoadedInstance() {
        if (ConfigSaver.theLoadedInstance == null) {
            synchronized (ConfigSaver.class) {
                if (ConfigSaver.theLoadedInstance == null) {
                    (ConfigSaver.theLoadedInstance = new ConfigSaver()).loadConfig();
                }
            }
            // monitorexit(ConfigSaver.class)
        }
        return ConfigSaver.theLoadedInstance;
    }
    
    private void loadConfig() {
        this.decompilerSettings = new DecompilerSettings();
        if (this.decompilerSettings.getFormattingOptions() == null) {
            this.decompilerSettings.setFormattingOptions(JavaFormattingOptions.createDefault());
        }
        this.luytenPreferences = new LuytenPreferences();
        this.mainWindowPosition = new WindowPosition();
        this.findWindowPosition = new WindowPosition();
        try {
            final Preferences prefs = Preferences.userNodeForPackage(ConfigSaver.class);
            this.decompilerSettings.setFlattenSwitchBlocks(prefs.getBoolean("flattenSwitchBlocks", this.decompilerSettings.getFlattenSwitchBlocks()));
            this.decompilerSettings.setForceExplicitImports(prefs.getBoolean("forceExplicitImports", this.decompilerSettings.getForceExplicitImports()));
            this.decompilerSettings.setShowSyntheticMembers(prefs.getBoolean("showSyntheticMembers", this.decompilerSettings.getShowSyntheticMembers()));
            this.decompilerSettings.setExcludeNestedTypes(prefs.getBoolean("excludeNestedTypes", this.decompilerSettings.getExcludeNestedTypes()));
            this.decompilerSettings.setForceExplicitTypeArguments(prefs.getBoolean("forceExplicitTypeArguments", this.decompilerSettings.getForceExplicitTypeArguments()));
            this.decompilerSettings.setRetainRedundantCasts(prefs.getBoolean("retainRedundantCasts", this.decompilerSettings.getRetainRedundantCasts()));
            this.decompilerSettings.setIncludeErrorDiagnostics(prefs.getBoolean("includeErrorDiagnostics", this.decompilerSettings.getIncludeErrorDiagnostics()));
            this.decompilerSettings.setLanguage(this.findLanguageByName(prefs.get("languageName", this.decompilerSettings.getLanguage().getName())));
            this.mainWindowPosition = this.loadWindowPosition(prefs, "main");
            this.findWindowPosition = this.loadWindowPosition(prefs, "find");
            this.luytenPreferences = this.loadLuytenPreferences(prefs);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private WindowPosition loadWindowPosition(final Preferences prefs, final String windowIdPrefix) {
        final WindowPosition windowPosition = new WindowPosition();
        windowPosition.setFullScreen(prefs.getBoolean(String.valueOf(windowIdPrefix) + "WindowIsFullScreen", false));
        windowPosition.setWindowWidth(prefs.getInt(String.valueOf(windowIdPrefix) + "WindowWidth", 0));
        windowPosition.setWindowHeight(prefs.getInt(String.valueOf(windowIdPrefix) + "WindowHeight", 0));
        windowPosition.setWindowX(prefs.getInt(String.valueOf(windowIdPrefix) + "WindowX", 0));
        windowPosition.setWindowY(prefs.getInt(String.valueOf(windowIdPrefix) + "WindowY", 0));
        return windowPosition;
    }
    
    private LuytenPreferences loadLuytenPreferences(final Preferences prefs) throws Exception {
        final LuytenPreferences newLuytenPrefs = new LuytenPreferences();
        Field[] loc_1;
        for (int loc_0 = (loc_1 = LuytenPreferences.class.getDeclaredFields()).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final Field field = loc_1[loc_2];
            if (!Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                final String prefId = field.getName();
                final Object defaultVal = field.get(newLuytenPrefs);
                if (field.getType() == String.class) {
                    final String defaultStr = (String)((defaultVal == null) ? "" : defaultVal);
                    field.set(newLuytenPrefs, prefs.get(prefId, defaultStr));
                }
                else if (field.getType() == Boolean.class || field.getType() == Boolean.TYPE) {
                    final Boolean defaultBool = (Boolean)((defaultVal == null) ? new Boolean(false) : defaultVal);
                    field.setBoolean(newLuytenPrefs, prefs.getBoolean(prefId, defaultBool));
                }
                else if (field.getType() == Integer.class || field.getType() == Integer.TYPE) {
                    final Integer defaultInt = (Integer)((defaultVal == null) ? new Integer(0) : defaultVal);
                    field.setInt(newLuytenPrefs, prefs.getInt(prefId, defaultInt));
                }
            }
        }
        return newLuytenPrefs;
    }
    
    public void saveConfig() {
        try {
            final Preferences prefs = Preferences.userNodeForPackage(ConfigSaver.class);
            prefs.putBoolean("flattenSwitchBlocks", this.decompilerSettings.getFlattenSwitchBlocks());
            prefs.putBoolean("forceExplicitImports", this.decompilerSettings.getForceExplicitImports());
            prefs.putBoolean("showSyntheticMembers", this.decompilerSettings.getShowSyntheticMembers());
            prefs.putBoolean("excludeNestedTypes", this.decompilerSettings.getExcludeNestedTypes());
            prefs.putBoolean("forceExplicitTypeArguments", this.decompilerSettings.getForceExplicitTypeArguments());
            prefs.putBoolean("retainRedundantCasts", this.decompilerSettings.getRetainRedundantCasts());
            prefs.putBoolean("includeErrorDiagnostics", this.decompilerSettings.getIncludeErrorDiagnostics());
            prefs.put("languageName", this.decompilerSettings.getLanguage().getName());
            this.saveWindowPosition(prefs, "main", this.mainWindowPosition);
            this.saveWindowPosition(prefs, "find", this.findWindowPosition);
            this.saveLuytenPreferences(prefs);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveWindowPosition(final Preferences prefs, final String windowIdPrefix, final WindowPosition windowPosition) {
        prefs.putBoolean(String.valueOf(windowIdPrefix) + "WindowIsFullScreen", windowPosition.isFullScreen());
        prefs.putInt(String.valueOf(windowIdPrefix) + "WindowWidth", windowPosition.getWindowWidth());
        prefs.putInt(String.valueOf(windowIdPrefix) + "WindowHeight", windowPosition.getWindowHeight());
        prefs.putInt(String.valueOf(windowIdPrefix) + "WindowX", windowPosition.getWindowX());
        prefs.putInt(String.valueOf(windowIdPrefix) + "WindowY", windowPosition.getWindowY());
    }
    
    private void saveLuytenPreferences(final Preferences prefs) throws Exception {
        Field[] loc_1;
        for (int loc_0 = (loc_1 = LuytenPreferences.class.getDeclaredFields()).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final Field field = loc_1[loc_2];
            if (!Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                final String prefId = field.getName();
                final Object value = field.get(this.luytenPreferences);
                if (field.getType() == String.class) {
                    prefs.put(prefId, (String)((value == null) ? "" : value));
                }
                else if (field.getType() == Boolean.class || field.getType() == Boolean.TYPE) {
                    prefs.putBoolean(prefId, (boolean)((value == null) ? new Boolean(false) : value));
                }
                else if (field.getType() == Integer.class || field.getType() == Integer.TYPE) {
                    prefs.putInt(prefId, (int)((value == null) ? new Integer(0) : value));
                }
            }
        }
    }
    
    private Language findLanguageByName(final String languageName) {
        if (languageName != null) {
            if (languageName.equals(Languages.java().getName())) {
                return Languages.java();
            }
            if (languageName.equals(Languages.bytecode().getName())) {
                return Languages.bytecode();
            }
            if (languageName.equals(Languages.bytecodeAst().getName())) {
                return Languages.bytecodeAst();
            }
            for (final Language language : Languages.debug()) {
                if (languageName.equals(language.getName())) {
                    return language;
                }
            }
        }
        return Languages.java();
    }
    
    public DecompilerSettings getDecompilerSettings() {
        return this.decompilerSettings;
    }
    
    public WindowPosition getMainWindowPosition() {
        return this.mainWindowPosition;
    }
    
    public WindowPosition getFindWindowPosition() {
        return this.findWindowPosition;
    }
    
    public LuytenPreferences getLuytenPreferences() {
        return this.luytenPreferences;
    }
}
