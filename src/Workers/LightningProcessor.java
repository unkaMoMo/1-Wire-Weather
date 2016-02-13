// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Calendar;

public class LightningProcessor
{
    protected static long lightningYear;
    protected static long lightningToday;
    protected static long lightningHour;
    protected int lightningZero;
    protected long lightningZeroYear;
    protected long lightningZeroDay;
    protected long lastCount;
    protected long lastTime;
    protected int lastLightningDayDate;
    protected int lastLightningYearDate;
    private long count;
    private long lightningZeroHour;
    private int lastLightningHourDate;
    
    public LightningProcessor() {
        this.lightningZero = 0;
        this.lightningZeroYear = 0L;
        this.lightningZeroDay = 0L;
        this.lastCount = 0L;
        this.lastTime = 0L;
        this.lastLightningDayDate = 0;
        this.lastLightningYearDate = 0;
        final Calendar rightNow = Calendar.getInstance();
        this.lastLightningDayDate = rightNow.get(5);
        this.lastLightningYearDate = rightNow.get(1);
        this.lastLightningHourDate = rightNow.get(11);
    }
    
    public void readLightningData() {
        try {
            final BufferedReader br = new BufferedReader(new FileReader("lightningdata.txt"));
            String lineOfText = null;
            while ((lineOfText = br.readLine()) != null) {
                lineOfText.trim();
                if (lineOfText.startsWith("year")) {
                    this.lightningZeroYear = Long.parseLong(lineOfText.split("=")[1]);
                }
                else if (lineOfText.startsWith("day")) {
                    this.lightningZeroDay = Long.parseLong(lineOfText.split("=")[1]);
                }
                else {
                    if (!lineOfText.startsWith("hour")) {
                        continue;
                    }
                    this.lightningZeroHour = Long.parseLong(lineOfText.split("=")[1]);
                }
            }
            System.out.println("Read operation 'lightningdata.txt' completed");
        }
        catch (FileNotFoundException fnf) {
            System.out.println("Could not find the file lightningdata.txt");
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public String processLightning(final String message) {
        this.count = Long.parseLong(message);
        final StringBuffer msg = new StringBuffer();
        final Calendar currentCal = Calendar.getInstance();
        if (currentCal.get(11) != this.lastLightningHourDate) {
            this.lightningZeroHour = this.count;
            this.lastLightningHourDate = currentCal.get(11);
            msg.append("dayBeginCount=" + this.lightningZeroDay + "\r\n");
            msg.append("yearBeginCount=" + this.lightningZeroYear + "\r\n");
            msg.append("hourBeginCount=" + this.lightningZeroHour + "\r\n");
            Utilities.fileWriter(msg.toString(), "lightningdata.txt", false);
        }
        if (currentCal.get(5) != this.lastLightningDayDate) {
            this.lightningZeroDay = this.count;
            this.lastLightningDayDate = currentCal.get(5);
            msg.append("dayBeginCount=" + this.lightningZeroDay + "\r\n");
            msg.append("yearBeginCount=" + this.lightningZeroYear + "\r\n");
            msg.append("hourBeginCount=" + this.lightningZeroHour + "\r\n");
            Utilities.fileWriter(msg.toString(), "lightningdata.txt", false);
        }
        if (currentCal.get(1) != this.lastLightningYearDate) {
            this.lightningZeroYear = this.count;
            this.lastLightningYearDate = currentCal.get(1);
            msg.append("dayBeginCount=" + this.lightningZeroDay + "\r\n");
            msg.append("yearBeginCount=" + this.lightningZeroYear + "\r\n");
            msg.append("hourBeginCount=" + this.lightningZeroHour + "\r\n");
            Utilities.fileWriter(msg.toString(), "lightningdata.txt", false);
        }
        LightningProcessor.lightningToday = this.count - this.lightningZeroDay;
        LightningProcessor.lightningYear = this.count - this.lightningZeroYear;
        LightningProcessor.lightningHour = this.count - this.lightningZeroHour;
        return new Long(LightningProcessor.lightningHour).toString();
    }
    
    public String getStringValue() {
        return new Long(LightningProcessor.lightningHour).toString() + "," + new Long(LightningProcessor.lightningToday).toString() + "," + new Long(LightningProcessor.lightningYear).toString();
    }
    
    public long getLightningHour() {
        return LightningProcessor.lightningHour;
    }
    
    public long getLightningToday() {
        return LightningProcessor.lightningToday;
    }
    
    public long getLightningYear() {
        return LightningProcessor.lightningYear;
    }
    
    static {
        LightningProcessor.lightningYear = 0L;
        LightningProcessor.lightningToday = 0L;
        LightningProcessor.lightningHour = 0L;
    }
}
