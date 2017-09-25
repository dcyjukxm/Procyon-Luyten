package com.strobel.assembler.metadata;

import com.strobel.core.*;
import java.nio.*;
import com.strobel.util.*;
import java.util.*;

public class Buffer
{
    private static final int DEFAULT_SIZE = 64;
    private byte[] _data;
    private int _length;
    private int _position;
    
    public Buffer() {
        super();
        this._data = new byte[64];
        this._length = 64;
    }
    
    public Buffer(final byte[] data) {
        super();
        this._data = VerifyArgument.notNull(data, "data");
        this._length = data.length;
    }
    
    public Buffer(final int initialSize) {
        super();
        this._data = new byte[initialSize];
        this._length = initialSize;
    }
    
    public int size() {
        return this._length;
    }
    
    public int position() {
        return this._position;
    }
    
    public void position(final int position) {
        if (position > this._length) {
            throw new BufferUnderflowException();
        }
        this._position = position;
    }
    
    public void advance(final int length) {
        if (this._position + length > this._length) {
            this._position = this._length;
            throw new BufferUnderflowException();
        }
        this._position += length;
    }
    
    public void reset() {
        this.reset(64);
    }
    
    public void reset(final int initialSize) {
        if (VerifyArgument.isNonNegative(initialSize, "initialSize") == 0) {
            this._data = EmptyArrayCache.EMPTY_BYTE_ARRAY;
        }
        else if (initialSize > this._data.length || initialSize < this._data.length / 4) {
            this._data = new byte[initialSize];
        }
        this._length = initialSize;
        this._position = 0;
    }
    
    public byte[] array() {
        return this._data;
    }
    
    public int read(final byte[] buffer, final int offset, final int length) {
        if (buffer == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || length < 0 || length > buffer.length - offset) {
            throw new IndexOutOfBoundsException();
        }
        if (this._position >= this._length) {
            return -1;
        }
        final int available = this._length - this._position;
        final int actualLength = Math.min(length, available);
        if (actualLength <= 0) {
            return 0;
        }
        System.arraycopy(this._data, this._position, buffer, offset, actualLength);
        this._position += actualLength;
        return actualLength;
    }
    
    public String readUtf8() {
        final int utfLength = this.readUnsignedShort();
        final byte[] byteBuffer = new byte[utfLength];
        final char[] charBuffer = new char[utfLength];
        int count = 0;
        int charactersRead = 0;
        this.read(byteBuffer, 0, utfLength);
        while (true) {
            while (count < utfLength) {
                int ch = byteBuffer[count] & 0xFF;
                if (ch > 127) {
                    while (count < utfLength) {
                        ch = (byteBuffer[count] & 0xFF);
                        switch (ch & 0xE0) {
                            case 0:
                            case 16:
                            case 32:
                            case 48:
                            case 64:
                            case 80:
                            case 96:
                            case 112: {
                                ++count;
                                charBuffer[charactersRead++] = (char)ch;
                                continue;
                            }
                            case 192: {
                                count += 2;
                                if (count > utfLength) {
                                    throw new IllegalStateException("malformed input: partial character at end");
                                }
                                final int ch2 = byteBuffer[count - 1];
                                if ((ch2 & 0xC0) != 0x80) {
                                    throw new IllegalStateException("malformed input around byte " + count);
                                }
                                charBuffer[charactersRead++] = (char)((ch & 0x1F) << 6 | (ch2 & 0x3F));
                                continue;
                            }
                            case 224: {
                                count += 3;
                                if (count > utfLength) {
                                    throw new IllegalStateException("malformed input: partial character at end");
                                }
                                final int ch2 = byteBuffer[count - 2];
                                final int ch3 = byteBuffer[count - 1];
                                if ((ch2 & 0xC0) != 0x80 || (ch3 & 0xC0) != 0x80) {
                                    throw new IllegalStateException("malformed input around byte " + (count - 1));
                                }
                                charBuffer[charactersRead++] = (char)((ch & 0xF) << 12 | (ch2 & 0x3F) << 6 | (ch3 & 0x3F) << 0);
                                continue;
                            }
                            default: {
                                throw new IllegalStateException("malformed input around byte " + count);
                            }
                        }
                    }
                    return new String(charBuffer, 0, charactersRead);
                }
                ++count;
                charBuffer[charactersRead++] = (char)ch;
            }
            continue;
        }
    }
    
    public byte readByte() {
        this.verifyReadableBytes(1);
        return this._data[this._position++];
    }
    
    public int readUnsignedByte() {
        this.verifyReadableBytes(1);
        return this._data[this._position++] & 0xFF;
    }
    
    public short readShort() {
        this.verifyReadableBytes(2);
        return (short)((this.readUnsignedByte() << 8) + (this.readUnsignedByte() << 0));
    }
    
    public int readUnsignedShort() {
        this.verifyReadableBytes(2);
        return (this.readUnsignedByte() << 8) + (this.readUnsignedByte() << 0);
    }
    
    public int readInt() {
        this.verifyReadableBytes(4);
        return (this.readUnsignedByte() << 24) + (this.readUnsignedByte() << 16) + (this.readUnsignedByte() << 8) + (this.readUnsignedByte() << 0);
    }
    
    public long readLong() {
        this.verifyReadableBytes(8);
        return (this.readUnsignedByte() << 56) + (this.readUnsignedByte() << 48) + (this.readUnsignedByte() << 40) + (this.readUnsignedByte() << 32) + (this.readUnsignedByte() << 24) + (this.readUnsignedByte() << 16) + (this.readUnsignedByte() << 8) + (this.readUnsignedByte() << 0);
    }
    
    public float readFloat() {
        return Float.intBitsToFloat(this.readInt());
    }
    
    public double readDouble() {
        return Double.longBitsToDouble(this.readLong());
    }
    
    public Buffer writeByte(final int b) {
        this.ensureWriteableBytes(1);
        this._data[this._position++] = (byte)(b & 0xFF);
        return this;
    }
    
    public Buffer writeShort(final int s) {
        this.ensureWriteableBytes(2);
        this._data[this._position++] = (byte)(s >>> 8 & 0xFF);
        this._data[this._position++] = (byte)(s & 0xFF);
        return this;
    }
    
    public Buffer writeInt(final int i) {
        this.ensureWriteableBytes(4);
        this._data[this._position++] = (byte)(i >>> 24 & 0xFF);
        this._data[this._position++] = (byte)(i >>> 16 & 0xFF);
        this._data[this._position++] = (byte)(i >>> 8 & 0xFF);
        this._data[this._position++] = (byte)(i & 0xFF);
        return this;
    }
    
    public Buffer writeLong(final long l) {
        this.ensureWriteableBytes(8);
        int i = (int)(l >>> 32);
        this._data[this._position++] = (byte)(i >>> 24 & 0xFF);
        this._data[this._position++] = (byte)(i >>> 16 & 0xFF);
        this._data[this._position++] = (byte)(i >>> 8 & 0xFF);
        this._data[this._position++] = (byte)(i & 0xFF);
        i = (int)l;
        this._data[this._position++] = (byte)(i >>> 24 & 0xFF);
        this._data[this._position++] = (byte)(i >>> 16 & 0xFF);
        this._data[this._position++] = (byte)(i >>> 8 & 0xFF);
        this._data[this._position++] = (byte)(i & 0xFF);
        return this;
    }
    
    public Buffer writeFloat(final float f) {
        return this.writeInt(Float.floatToRawIntBits(f));
    }
    
    public Buffer writeDouble(final double d) {
        return this.writeLong(Double.doubleToRawLongBits(d));
    }
    
    public Buffer writeUtf8(final String s) {
        final int charLength = s.length();
        this.ensureWriteableBytes(2 + charLength);
        this._data[this._position++] = (byte)(charLength >>> 8);
        this._data[this._position++] = (byte)charLength;
        for (int i = 0; i < charLength; ++i) {
            char c = s.charAt(i);
            if (c < '\u0001' || c > '\u007f') {
                int byteLength = i;
                for (int j = i; j < charLength; ++j) {
                    c = s.charAt(j);
                    if (c >= '\u0001' && c <= '\u007f') {
                        ++byteLength;
                    }
                    else if (c > '\u07ff') {
                        byteLength += 3;
                    }
                    else {
                        byteLength += 2;
                    }
                }
                this._data[this._position] = (byte)(byteLength >>> 8);
                this._data[this._position + 1] = (byte)byteLength;
                this.ensureWriteableBytes(2 + byteLength);
                for (int j = i; j < charLength; ++j) {
                    c = s.charAt(j);
                    if (c >= '\u0001' && c <= '\u007f') {
                        this._data[this._position++] = (byte)c;
                    }
                    else if (c > '\u07ff') {
                        this._data[this._position++] = (byte)('\u00e0' | (c >> 12 & '\u000f'));
                        this._data[this._position++] = (byte)('\u0080' | (c >> 6 & '?'));
                        this._data[this._position++] = (byte)('\u0080' | (c & '?'));
                    }
                    else {
                        this._data[this._position++] = (byte)('\u00c0' | (c >> 6 & '\u001f'));
                        this._data[this._position++] = (byte)('\u0080' | (c & '?'));
                    }
                }
                break;
            }
            this._data[this._position++] = (byte)c;
        }
        return this;
    }
    
    public Buffer putByteArray(final byte[] b, final int offset, final int length) {
        this.ensureWriteableBytes(length);
        if (b != null) {
            System.arraycopy(b, offset, this._data, this._position, length);
        }
        this._position += length;
        return this;
    }
    
    protected void verifyReadableBytes(final int size) {
        if (VerifyArgument.isNonNegative(size, "size") > 0 && this._position + size > this._length) {
            throw new BufferUnderflowException();
        }
    }
    
    protected void ensureWriteableBytes(final int size) {
        if (this._length + size <= this._data.length) {
            return;
        }
        final int length1 = 2 * this._data.length;
        final int length2 = this._length + size;
        this._data = Arrays.copyOf(this._data, (length1 > length2) ? length1 : length2);
    }
}
