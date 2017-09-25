package org.fife.ui.rsyntaxtextarea;

import java.security.*;
import org.fife.ui.rsyntaxtextarea.modes.*;
import java.util.*;

public abstract class TokenMakerFactory
{
    public static final String PROPERTY_DEFAULT_TOKEN_MAKER_FACTORY = "TokenMakerFactory";
    private static TokenMakerFactory DEFAULT_INSTANCE;
    
    public static synchronized TokenMakerFactory getDefaultInstance() {
        if (TokenMakerFactory.DEFAULT_INSTANCE == null) {
            String clazz = null;
            try {
                clazz = System.getProperty("TokenMakerFactory");
            }
            catch (AccessControlException ace) {
                clazz = null;
            }
            if (clazz == null) {
                clazz = "org.fife.ui.rsyntaxtextarea.DefaultTokenMakerFactory";
            }
            try {
                TokenMakerFactory.DEFAULT_INSTANCE = (TokenMakerFactory)Class.forName(clazz).newInstance();
            }
            catch (RuntimeException re) {
                throw re;
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new InternalError("Cannot find TokenMakerFactory: " + clazz);
            }
        }
        return TokenMakerFactory.DEFAULT_INSTANCE;
    }
    
    public final TokenMaker getTokenMaker(final String key) {
        TokenMaker tm = this.getTokenMakerImpl(key);
        if (tm == null) {
            tm = new PlainTextTokenMaker();
        }
        return tm;
    }
    
    protected abstract TokenMaker getTokenMakerImpl(final String param_0);
    
    public abstract Set<String> keySet();
    
    public static synchronized void setDefaultInstance(final TokenMakerFactory tmf) {
        if (tmf == null) {
            throw new IllegalArgumentException("tmf cannot be null");
        }
        TokenMakerFactory.DEFAULT_INSTANCE = tmf;
    }
}
