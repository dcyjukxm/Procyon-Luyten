package com.strobel.componentmodel;

public interface UserDataStore
{
     <T> T getUserData(Key<T> param_0);
    
     <T> void putUserData(Key<T> param_0, T param_1);
    
     <T> T putUserDataIfAbsent(Key<T> param_0, T param_1);
    
     <T> boolean replace(Key<T> param_0, T param_1, T param_2);
}
