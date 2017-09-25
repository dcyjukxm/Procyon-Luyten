package us.deathmarine.luyten;

public final class Closer
{
    public static void tryClose(final AutoCloseable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        }
        catch (Throwable loc_0) {}
    }
    
    public static void tryClose(final AutoCloseable... items) {
        if (items == null) {
            return;
        }
        for (final AutoCloseable c : items) {
            tryClose(c);
        }
    }
}
