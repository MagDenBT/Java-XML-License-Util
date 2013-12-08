package com.ilirium.license.xmllicense.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author DoDo <dopoljak@gmail.com>
 */
public class IO
{
    /**
     * Save bytes to file
     */
    public static void saveFile(String filename, byte[] bytes) throws IOException
    {
        FileOutputStream out = new FileOutputStream(filename);
        out.write(bytes);
        out.flush();
        out.close();
    }

    /**
     * Read file from full path name
     */
    public static byte[] readFile(String filename) throws IOException
    {
        File f = new File(filename);
        byte[] file = new byte[(int) f.length()];
        FileInputStream fis = new FileInputStream(f);
        fis.read(file);
        fis.close();
        return file;
    }
}
