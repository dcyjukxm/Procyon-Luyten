package com.strobel.assembler.metadata;

public enum MethodHandleType
{
    GetField("GetField", 0), 
    GetStatic("GetStatic", 1), 
    PutField("PutField", 2), 
    PutStatic("PutStatic", 3), 
    InvokeVirtual("InvokeVirtual", 4), 
    InvokeStatic("InvokeStatic", 5), 
    InvokeSpecial("InvokeSpecial", 6), 
    NewInvokeSpecial("NewInvokeSpecial", 7), 
    InvokeInterface("InvokeInterface", 8);
}
