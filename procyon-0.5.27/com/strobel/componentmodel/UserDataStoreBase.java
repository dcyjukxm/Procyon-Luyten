package com.strobel.componentmodel;

import java.util.concurrent.atomic.*;
import com.strobel.annotations.*;
import com.strobel.core.*;

public class UserDataStoreBase implements UserDataStore, Cloneable
{
    public static final Key<FrugalKeyMap> COPYABLE_USER_MAP_KEY;
    private static final AtomicReferenceFieldUpdater<UserDataStoreBase, FrugalKeyMap> UPDATER;
    @NotNull
    private volatile FrugalKeyMap _map;
    
    static {
        COPYABLE_USER_MAP_KEY = Key.create("COPYABLE_USER_MAP_KEY");
        UPDATER = AtomicReferenceFieldUpdater.newUpdater(UserDataStoreBase.class, FrugalKeyMap.class, "_map");
    }
    
    public UserDataStoreBase() {
        super();
        this._map = FrugalKeyMap.EMPTY;
    }
    
    @Override
    public <T> T getUserData(@NotNull final Key<T> key) {
        return this._map.get(key);
    }
    
    @Override
    public <T> void putUserData(@NotNull final Key<T> key, @Nullable final T value) {
        FrugalKeyMap newMap;
        FrugalKeyMap oldMap;
        do {
            oldMap = this._map;
            if (value == null) {
                newMap = oldMap.minus(key);
            }
            else {
                newMap = oldMap.plus(key, value);
            }
        } while (newMap != oldMap && !UserDataStoreBase.UPDATER.compareAndSet(this, oldMap, newMap));
    }
    
    @Override
    public <T> T putUserDataIfAbsent(@NotNull final Key<T> key, @Nullable final T value) {
        FrugalKeyMap newMap;
        FrugalKeyMap oldMap;
        do {
            oldMap = this._map;
            final T oldValue = this._map.get(key);
            if (oldValue != null) {
                return oldValue;
            }
            if (value == null) {
                newMap = oldMap.minus(key);
            }
            else {
                newMap = oldMap.plus(key, value);
            }
        } while (newMap != oldMap && !UserDataStoreBase.UPDATER.compareAndSet(this, oldMap, newMap));
        return value;
    }
    
    @Override
    public <T> boolean replace(@NotNull final Key<T> key, @Nullable final T oldValue, @Nullable final T newValue) {
        FrugalKeyMap newMap;
        FrugalKeyMap oldMap;
        do {
            oldMap = this._map;
            final T currentValue = this._map.get(key);
            if (currentValue != oldValue) {
                return false;
            }
            if (newValue == null) {
                newMap = oldMap.minus(key);
            }
            else {
                newMap = oldMap.plus(key, newValue);
            }
        } while (newMap != oldMap && !UserDataStoreBase.UPDATER.compareAndSet(this, oldMap, newMap));
        return true;
    }
    
    public final UserDataStoreBase clone() {
        try {
            return (UserDataStoreBase)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw ExceptionUtilities.asRuntimeException(e);
        }
    }
}
