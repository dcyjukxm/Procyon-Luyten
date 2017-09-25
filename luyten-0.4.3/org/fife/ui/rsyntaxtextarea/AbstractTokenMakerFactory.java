package org.fife.ui.rsyntaxtextarea;

import java.util.*;

public abstract class AbstractTokenMakerFactory extends TokenMakerFactory
{
    private Map<String, Object> tokenMakerMap;
    
    protected AbstractTokenMakerFactory() {
        super();
        this.tokenMakerMap = new HashMap<String, Object>();
        this.initTokenMakerMap();
    }
    
    protected TokenMaker getTokenMakerImpl(final String key) {
        final TokenMakerCreator tmc = this.tokenMakerMap.get(key);
        if (tmc != null) {
            try {
                return tmc.create();
            }
            catch (RuntimeException re) {
                throw re;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    protected abstract void initTokenMakerMap();
    
    public Set<String> keySet() {
        return this.tokenMakerMap.keySet();
    }
    
    public void putMapping(final String key, final String className) {
        this.putMapping(key, className, null);
    }
    
    public void putMapping(final String key, final String className, final ClassLoader cl) {
        this.tokenMakerMap.put(key, new TokenMakerCreator(className, cl));
    }
    
    private static class TokenMakerCreator
    {
        private String className;
        private ClassLoader cl;
        
        public TokenMakerCreator(final String className, final ClassLoader cl) {
            super();
            this.className = className;
            this.cl = ((cl != null) ? cl : this.getClass().getClassLoader());
        }
        
        public TokenMaker create() throws Exception {
            return (TokenMaker)Class.forName(this.className, true, this.cl).newInstance();
        }
    }
}
