// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.Serializable;

public class RainProcessor implements Serializable
{
    protected static float rainSinceMidnight;
    protected static long rainWeek;
    protected static long rainZeroMidnight;
    protected static long rainZeroWeek;
    protected static long rainMonth;
    protected static long rainZeroMonth;
    protected static long rainYear;
    protected static long rainZeroYear;
    protected static long rainHour;
    static float lastCount;
    static long lastTime;
    protected static final long RAIN_HOUR_INTERVAL = 3600000L;
    protected static final long RAIN_DAY_INTERVAL = 86400000L;
    public static long count;
    static Calendar cal;
    private static int currentDay;
    private static int currentYear;
    private static int currentHour;
    private static int currentMonth;
    private static long currentTicks;
    public ArrayList precipReadingList;
    static String precipRecord;
    private static boolean hoursUp;
    private static float rain24Hours;
    
    public ArrayList restoreRainReadingList() {
        return this.precipReadingList;
    }
    
    public ArrayList createArrayList() {
        return this.precipReadingList = new ArrayList();
    }
    
    public void process(final long count) {
        StringBuffer sb = new StringBuffer();
        final Calendar currentCal = Calendar.getInstance();
        RainProcessor.currentTicks = System.currentTimeMillis();
        if (count > 0L) {
            if (currentCal.get(1) != RainProcessor.currentYear) {
                RainProcessor.rainZeroYear = count;
                RainProcessor.currentYear = currentCal.get(1);
            }
            if (currentCal.get(2) != RainProcessor.currentMonth) {
                RainProcessor.rainZeroMonth = count;
                RainProcessor.currentMonth = currentCal.get(2);
            }
            if (currentCal.get(7) == 1 && currentCal.get(6) != RainProcessor.currentDay) {
                RainProcessor.rainZeroWeek = count;
            }
            if (currentCal.get(6) != RainProcessor.currentDay) {
                RainProcessor.rainZeroMidnight = count;
                RainProcessor.currentDay = currentCal.get(6);
            }
            if (currentCal.get(11) != RainProcessor.currentHour) {
                RainProcessor.hoursUp = true;
                RainProcessor.currentHour = currentCal.get(11);
                sb.append("yearBeginCount=" + RainProcessor.rainZeroYear + "\r\n");
                sb.append("monthBeginCount=" + RainProcessor.rainZeroMonth + "\r\n");
                sb.append("weekBeginCount=" + RainProcessor.rainZeroWeek + "\r\n");
                sb.append("dayBeginCount=" + RainProcessor.rainZeroMidnight + "\r\n");
                Utilities.fileWriter(sb.toString(), "rainsettings.txt", false);
                sb = null;
            }
            if (count != RainProcessor.lastCount || RainProcessor.hoursUp) {
                RainProcessor.precipRecord = RainProcessor.currentTicks + ":" + count;
                try {
                    this.precipReadingList.add(RainProcessor.precipRecord);
                }
                catch (Exception ex) {}
                System.out.println("Record added to RainReadingList");
                this.saveRainReadingList();
                RainProcessor.hoursUp = false;
            }
            this.findRain24Hours(count);
            processRain(count, RainProcessor.currentTicks);
            RainProcessor.lastCount = count;
        }
    }
    
    public static void readRainData() {
        try {
            final BufferedReader br = new BufferedReader(new FileReader("rainsettings.txt"));
            String lineOfText = null;
            while ((lineOfText = br.readLine()) != null) {
                lineOfText.trim();
                if (lineOfText.startsWith("year")) {
                    RainProcessor.rainZeroYear = Long.parseLong(lineOfText.split("=")[1]);
                }
                else if (lineOfText.startsWith("month")) {
                    RainProcessor.rainZeroMonth = Long.parseLong(lineOfText.split("=")[1]);
                }
                else if (lineOfText.startsWith("week")) {
                    RainProcessor.rainZeroWeek = Long.parseLong(lineOfText.split("=")[1]);
                }
                else if (lineOfText.startsWith("day")) {
                    RainProcessor.rainZeroMidnight = Long.parseLong(lineOfText.split("=")[1]);
                }
                else {
                    if (lineOfText.startsWith("hour")) {
                        continue;
                    }
                    continue;
                }
            }
            System.out.println("Read operation 'rainsettings' completed");
        }
        catch (FileNotFoundException fnf) {
            System.out.println("Could not find the file rainsettings.txt" + fnf);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void saveRainReadingList() {
        final StringBuffer sb = new StringBuffer();
        try {
            final Iterator e = this.precipReadingList.iterator();
            while (e.hasNext()) {
                sb.append(e.next() + "\r\n");
            }
            sb.append("-1");
            final File file = new File("24hourrain.txt");
            if (file.exists()) {
                file.delete();
            }
            Utilities.fileWriter(sb.toString(), "24hourrain.txt", false);
            System.out.println("File 24hourrain.txt has been saved");
        }
        catch (Exception exception) {
            System.err.println("Error saving ArrayList info " + exception);
        }
    }
    
    public static String processRain(final long count, final long currentTicks) {
        RainProcessor.rainSinceMidnight = count - RainProcessor.rainZeroMidnight;
        RainProcessor.rainWeek = count - RainProcessor.rainZeroWeek;
        RainProcessor.rainMonth = count - RainProcessor.rainZeroMonth;
        RainProcessor.rainYear = count - RainProcessor.rainZeroYear;
        return new Float(RainProcessor.rainSinceMidnight).toString();
    }
    
    public void populateArrayList() {
        final File file = new File("24hourrain.txt");
        if (file.exists()) {
            this.precipReadingList.clear();
            try {
                final BufferedReader br = new BufferedReader(new FileReader(file));
                String line = null;
                while (!(line = br.readLine()).contains("-1")) {
                    if (line != null) {
                        this.precipReadingList.add(line);
                    }
                }
            }
            catch (Exception ex) {
                System.err.println("Error populating ArrayList " + ex);
            }
        }
    }
    
    public float findRain24Hours(final long count) {
        RainProcessor.precipRecord = null;
        System.out.println("In findRain24Hours() method");
        String previousRecord = null;
        int index = 0;
        boolean removedRecords = false;
        try {
            if (this.precipReadingList != null) {
                RainProcessor.precipRecord = this.precipReadingList.get(0).toString();
                while (RainProcessor.currentTicks - Long.parseLong(RainProcessor.precipRecord.split(":")[0]) > 86400000L) {
                    removedRecords = true;
                    this.precipReadingList.remove(index);
                    RainProcessor.precipRecord = this.precipReadingList.get(index).toString();
                    ++index;
                }
                setRain24Hours(RainProcessor.rain24Hours = count - Float.parseFloat(RainProcessor.precipRecord.split(":")[1]));
                index = 0;
                while (index < this.precipReadingList.size()) {
                    RainProcessor.precipRecord = this.precipReadingList.get(index).toString();
                    if (this.precipReadingList.get(index).toString().isEmpty()) {
                        this.precipReadingList.remove(index);
                    }
                    if (RainProcessor.currentTicks - Long.parseLong(RainProcessor.precipRecord.split(":")[0]) <= 3600000L) {
                        if (index > 0) {
                            previousRecord = this.precipReadingList.get(index - 1).toString();
                        }
                        else {
                            previousRecord = RainProcessor.precipRecord;
                        }
                        if (count == Long.parseLong(RainProcessor.precipRecord.split(":")[1])) {
                            RainProcessor.rainHour = count - Long.parseLong(previousRecord.split(":")[1]);
                            break;
                        }
                        RainProcessor.rainHour = count - Long.parseLong(RainProcessor.precipRecord.split(":")[1]);
                        break;
                    }
                    else {
                        ++index;
                    }
                }
            }
            if (removedRecords) {
                System.out.println("Removed old rain record");
                this.saveRainReadingList();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            RainProcessor.precipRecord = null;
            return new Float(RainProcessor.rain24Hours);
        }
        RainProcessor.precipRecord = null;
        return new Float(RainProcessor.rain24Hours);
    }
    
    public String getStringValue() {
        return new Float(RainProcessor.rainSinceMidnight / 100.0f).toString() + "," + new Float(getRain24Hours()).toString() + "," + new Float(RainProcessor.rainHour / 100L).toString() + "," + new Float(RainProcessor.rainWeek / 100L).toString() + "," + new Float(RainProcessor.rainMonth / 100L).toString() + "," + new Float(RainProcessor.rainYear / 100L).toString() + "," + "," + RainProcessor.count;
    }
    
    public static float getRainHour() {
        return RainProcessor.rainHour / 100.0f;
    }
    
    public static float getRainYear() {
        return RainProcessor.rainYear / 100.0f;
    }
    
    public static float getRainMonth() {
        return RainProcessor.rainMonth / 100.0f;
    }
    
    public static float getRain24Hours() {
        return RainProcessor.rain24Hours / 100.0f;
    }
    
    public static float getRainWeek() {
        return RainProcessor.rainWeek / 100.0f;
    }
    
    public static float getRainSinceMidnight() {
        return RainProcessor.rainSinceMidnight / 100.0f;
    }
    
    private static void setRain24Hours(final float rain24Hours) {
        RainProcessor.rain24Hours = rain24Hours;
        System.out.println("Setting rain24hours to " + rain24Hours);
    }
    
    private static void setRainHour(final long rainHour) {
        RainProcessor.rainHour = rainHour;
    }
    
    static {
        RainProcessor.rainSinceMidnight = 0.0f;
        RainProcessor.rainWeek = 0L;
        RainProcessor.rainZeroMidnight = 0L;
        RainProcessor.rainZeroWeek = 0L;
        RainProcessor.rainMonth = 0L;
        RainProcessor.rainZeroMonth = 0L;
        RainProcessor.rainYear = 0L;
        RainProcessor.rainZeroYear = 0L;
        RainProcessor.rainHour = 0L;
        RainProcessor.lastCount = Dispatcher.getRainCount();
        RainProcessor.lastTime = System.currentTimeMillis();
        RainProcessor.cal = Calendar.getInstance();
        RainProcessor.currentDay = RainProcessor.cal.get(6);
        RainProcessor.currentYear = RainProcessor.cal.get(1);
        RainProcessor.currentHour = RainProcessor.cal.get(11);
        RainProcessor.currentMonth = RainProcessor.cal.get(2);
        RainProcessor.hoursUp = true;
    }
}
