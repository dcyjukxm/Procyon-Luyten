package us.deathmarine.luyten;

import java.text.*;

public class TooLargeFileException extends Exception
{
    private static final long serialVersionUID = 1L;
    private long size;
    
    public TooLargeFileException(final long size) {
        super();
        this.size = size;
    }
    
    public String getReadableFileSize() {
        if (this.size <= 0L) {
            return "0";
        }
        final String[] units = { "B", "KB", "MB", "GB", "TB" };
        final int digitGroups = (int)(Math.log10(this.size) / Math.log10(1024.0));
        return String.valueOf(new DecimalFormat("#,##0.#").format(this.size / Math.pow(1024.0, digitGroups))) + " " + units[digitGroups];
    }
}
