package us.deathmarine.luyten;

import java.io.*;
import javax.swing.*;

public class Luyten
{
    public static void main(final String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        final File fileFromCommandLine = getFileFromCommandLine(args);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final MainWindow mainWindow = new MainWindow(fileFromCommandLine);
                mainWindow.setVisible(true);
            }
        });
    }
    
    public static File getFileFromCommandLine(final String[] args) {
        File fileFromCommandLine = null;
        try {
            if (args.length > 0) {
                final String realFileName = new File(args[0]).getCanonicalPath();
                fileFromCommandLine = new File(realFileName);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return fileFromCommandLine;
    }
}
