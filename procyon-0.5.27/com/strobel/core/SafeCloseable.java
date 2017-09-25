package com.strobel.core;

public interface SafeCloseable extends AutoCloseable
{
    void close();
}
