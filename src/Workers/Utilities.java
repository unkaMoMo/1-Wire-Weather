// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import java.io.FileWriter;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.io.File;

public class Utilities
{
    private static File file;
    private static ArrayList restoredArrayList;
    private static CopyOnWriteArrayList restoredCopyOnWriteArrayList;
    Calendar cal;
    
    public static double convertCtoF(final float c) {
        final double f = c * 1.8 + 32.0;
        return f;
    }
    
    public static float roundValue2(float value) {
        value *= 100.0f;
        value = Math.round(value);
        value /= 100.0f;
        return value;
    }
    
    public static float roundValue1(float value) {
        value *= 10.0f;
        value = Math.round(value);
        value /= 10.0f;
        return value;
    }
    
    public static double roundValue2(double value) {
        value *= 100.0;
        value = Math.round(value);
        value /= 100.0;
        return value;
    }
    
    public static double roundValue1(double value) {
        value *= 10.0;
        value = Math.round(value);
        value /= 10.0;
        return value;
    }
    
    public static double getDewpointF(final double tempC, final double humidity) {
        final double es = 6.11 * Math.pow(10.0, 7.5 * tempC / (237.7 + tempC));
        final double dewptc = 237.7 * (Math.log(es * humidity / 611.0) / Math.log(10.0)) / 7.5 - Math.log(es * humidity / 611.0) / Math.log(10.0);
        final double dewptfw = roundValue1(dewptc * 1.8 + 32.0);
        return dewptfw;
    }
    
    public static double getDewpointC(final double tempC, final double humidity) {
        final double value = 0.0;
        final double es = 6.11 * Math.pow(10.0, 7.5 * tempC / (237.7 + tempC));
        final double dewptc = 237.7 * (Math.log(es * humidity / 611.0) / Math.log(10.0)) / 7.5 - Math.log(es * humidity / 611.0) / Math.log(10.0);
        return roundValue1(value);
    }
    
    public static ArrayList restoreArrayListObject(final String fileName) {
        Utilities.file = new File(fileName);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(Utilities.file));
            Utilities.restoredArrayList = (ArrayList)ois.readObject();
            ois.close();
            System.out.println("Read operation " + Utilities.file + " completed");
        }
        catch (FileNotFoundException fnf) {
            System.out.println("Could not find the file " + Utilities.file);
        }
        catch (IOException io) {
            System.out.println("An error occurred while reading  " + Utilities.file + io);
        }
        catch (ClassNotFoundException cnf) {
            System.out.println("An error occurred - class not found! " + cnf);
        }
        finally {
            if (ois != null) {
                try {
                    ois.close();
                }
                catch (Exception ex) {}
            }
        }
        return Utilities.restoredArrayList;
    }
    
    public static void saveArrayListObject(final ArrayList ArrayList, String fileName) {
        try {
            final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(ArrayList);
            out.close();
        }
        catch (IOException e) {
            System.out.println("An error occurred while trying to save the " + fileName + "\r\n" + e);
        }
        System.out.println("ArrayList was saved as " + fileName);
        fileName = null;
    }
    
    public static CopyOnWriteArrayList restoreCopyOnWriteArrayListObject(final String fileName) {
        Utilities.file = new File(fileName);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(Utilities.file));
            Utilities.restoredCopyOnWriteArrayList = (CopyOnWriteArrayList)ois.readObject();
            ois.close();
            System.out.println("Read operation " + Utilities.file + " completed");
        }
        catch (FileNotFoundException fnf) {
            System.out.println("Could not find the file " + Utilities.file);
        }
        catch (IOException io) {
            System.out.println("An error occurred while reading  " + Utilities.file + io);
        }
        catch (ClassNotFoundException cnf) {
            System.out.println("An error occurred - class not found! " + cnf);
        }
        finally {
            if (ois != null) {
                try {
                    ois.close();
                }
                catch (Exception ex) {}
            }
        }
        return Utilities.restoredCopyOnWriteArrayList;
    }
    
    public static void saveCopyOnWriteArrayListObject(final CopyOnWriteArrayList CopyOnWriteArrayList, String fileName) {
        try {
            final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(CopyOnWriteArrayList);
            out.close();
        }
        catch (IOException e) {
            System.out.println("An error occurred while trying to save the " + fileName + "\r\n" + e);
        }
        System.out.println("CopyOnWriteArrayList was saved as " + fileName);
        fileName = null;
    }
    
    public static void fileWriter(final String msg, final String fileName, final boolean append) {
        try {
            final FileWriter f = new FileWriter(fileName, append);
            f.write(msg + "\n");
            f.close();
            System.out.println("File save operation " + fileName + " completed");
        }
        catch (Exception e) {
            System.out.println("Write operation failed " + e);
        }
    }
    
    static void saveArrayListObject(final ArrayList ArrayList, File file) {
        try {
            final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(ArrayList);
            out.close();
        }
        catch (IOException e) {
            System.out.println("An error occurred while trying to save the " + file + "\r\n" + e);
        }
        System.out.println("ArrayList save operation " + file + " completed");
        file = null;
    }
}
