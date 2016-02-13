// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.File;
import java.util.Properties;
import java.util.Calendar;
import java.io.Serializable;

public class MaxMin implements Serializable
{
    public static String Max;
    public static String Min;
    Calendar startCal;
    private int oldDay;
    private double minValue;
    private double maxValue;
    private double value;
    private String minTime;
    private String maxTime;
    private String msg;
    Properties props;
    private int newDay;
    private boolean tooOld;
    File file;
    CopyOnWriteArrayList culledReadings;
    private Calendar cal;
    private boolean skip;
    private boolean notThisRoundBaby;
    
    public MaxMin(final Properties props) {
        this.startCal = Calendar.getInstance();
        this.oldDay = this.startCal.get(5);
        this.tooOld = false;
        this.file = new File("minMaxList.dat");
        this.culledReadings = new CopyOnWriteArrayList();
        this.props = props;
    }
    
    public void updateRecord(final String[] sensorReadings, final Properties props, final CopyOnWriteArrayList minMaxList) {
        this.cal = Calendar.getInstance();
        this.newDay = this.cal.get(5);
        if (this.file.exists()) {
            final Calendar ca;
            final Calendar c = ca = Calendar.getInstance();
            synchronized (ca) {
                final Date then = new Date(this.file.lastModified());
                ca.setTime(then);
                final int dayThen = ca.get(5);
                if (dayThen != this.newDay) {
                    this.tooOld = true;
                }
            }
        }
        if (this.newDay != this.oldDay) {
            minMaxList.clear();
            System.out.println("Crossed midnight resetting all minMaxList readings to current");
            this.oldDay = this.newDay;
        }
        if (this.tooOld) {
            System.out.println("MinMax Data is not from today. Resetting MinMaxList");
            minMaxList.clear();
            this.tooOld = false;
        }
        for (int i = 0; i < sensorReadings.length; ++i) {
            this.msg = this.parseDevice(sensorReadings[i]);
            if (this.parseDevice(this.msg).trim().endsWith("1D") || this.parseDevice(this.msg).trim().endsWith("20")) {
                this.skip = true;
            }
            else {
                this.skip = false;
            }
            if (!this.skip) {
                this.culledReadings.add(sensorReadings[i]);
                this.skip = false;
            }
        }
        this.culledReadings.add("instant:" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getCurrentWindSpeed());
        this.culledReadings.add("average:" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getAverageWindSpeed());
        this.culledReadings.add("gust:" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getWindGust());
        if (minMaxList.isEmpty() || !this.file.exists()) {
            System.out.println("MinMaxList has been reset ");
            this.skip = false;
            for (int k = 0; k < sensorReadings.length; ++k) {
                this.msg = sensorReadings[k];
                if (this.parseDevice(this.msg).trim().endsWith("1D") || this.parseDevice(this.msg).trim().endsWith("20")) {
                    this.skip = true;
                }
                else {
                    this.skip = false;
                }
                if (!this.skip) {
                    final String tmp = this.parseDevice(this.msg) + ":" + this.parseTime(this.msg) + ":" + this.parseValue(this.msg) + ":" + this.parseTime(this.msg) + ":" + this.parseValue(this.msg);
                    System.out.println("Starting new min/max records for: " + this.parseDevice(this.msg));
                    minMaxList.add(tmp);
                    this.skip = false;
                }
            }
            minMaxList.add("instant:" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getCurrentWindSpeed() + ":" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getCurrentWindSpeed());
            System.out.println("Starting new min/max records for: Current Wind");
            minMaxList.add("average:" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getAverageWindSpeed() + ":" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getAverageWindSpeed());
            System.out.println("Starting new min/max records for: Average Wind");
            minMaxList.add("gust:" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getWindGust() + ":" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getWindGust());
            System.out.println("Starting new min/max records for: Wind Gust");
            return;
        }
        for (int z = 0; z < this.culledReadings.size(); ++z) {
            this.msg = this.culledReadings.get(z).toString();
            this.notThisRoundBaby = false;
            for (int j = 0; j < minMaxList.size(); ++j) {
                final String test = minMaxList.get(j).toString();
                if (test.startsWith("instant")) {
                    this.notThisRoundBaby = true;
                    if (WindSpeedProcessor.getCurrentWindSpeed() > this.parsemaxValue(test)) {
                        final String newMessage = "instant:" + WindSpeedProcessor.getLastTicks() + ":" + WindSpeedProcessor.getCurrentWindSpeed() + ":" + WindSpeedProcessor.getLastTicks() + ":" + WindSpeedProcessor.getCurrentWindSpeed();
                        minMaxList.set(j, newMessage);
                    }
                }
                if (test.startsWith("average")) {
                    this.notThisRoundBaby = true;
                    if (WindSpeedProcessor.getAverageWindSpeed() > this.parsemaxValue(test)) {
                        final String newMessage = "average:" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getAverageWindSpeed() + ":" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getAverageWindSpeed();
                        minMaxList.set(j, newMessage);
                    }
                }
                if (test.startsWith("gust")) {
                    this.notThisRoundBaby = true;
                    if (WindSpeedProcessor.getWindGust() > this.parsemaxValue(test)) {
                        final String newMessage = "gust:" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getWindGust() + ":" + WindSpeedProcessor.currentTicks + ":" + WindSpeedProcessor.getWindGust();
                        minMaxList.set(j, newMessage);
                    }
                }
                for (int m = 0; m < minMaxList.size(); ++m) {
                    if (test.startsWith("instant") || test.startsWith("average") || test.startsWith("gust")) {
                        this.notThisRoundBaby = true;
                    }
                    final String phooey = minMaxList.get(m).toString();
                    if (!this.notThisRoundBaby && this.parseDevice(this.msg).equals(this.parseDevice(phooey))) {
                        if (this.parseValue(this.msg) < this.parseminValue(phooey)) {
                            final String newMessage2 = this.parseDevice(this.msg) + ":" + this.parseTime(this.msg) + ":" + this.parseValue(this.msg) + ":" + this.parsemaxTime(phooey) + ":" + this.parsemaxValue(phooey);
                            minMaxList.set(m, newMessage2);
                        }
                        if (this.parseValue(this.msg) > this.parsemaxValue(phooey)) {
                            final String newMessage2 = this.parseDevice(this.msg) + ":" + this.parseminTime(phooey) + ":" + this.parseminValue(phooey) + ":" + this.parseTime(this.msg) + ":" + this.parseValue(this.msg);
                            minMaxList.set(m, newMessage2);
                        }
                    }
                }
            }
        }
        this.culledReadings.clear();
    }
    
    private String parseDevice(final String msg) {
        final String device = msg.split(":")[0].trim();
        return device;
    }
    
    private long parseTime(final String msg) {
        final long time = Long.parseLong(msg.split(":")[1].trim());
        return time;
    }
    
    private String parseminTime(final String msg) {
        return this.minTime = msg.split(":")[1].trim();
    }
    
    private String parsemaxTime(final String msg) {
        return this.maxTime = msg.split(":")[3].trim();
    }
    
    private double parseValue(final String msg) {
        return this.value = Double.parseDouble(msg.split(":")[2].trim());
    }
    
    private double parsemaxValue(final String msg) {
        return this.maxValue = Double.parseDouble(msg.split(":")[4].trim());
    }
    
    private double parseminValue(final String msg) {
        return this.minValue = Double.parseDouble(msg.split(":")[2].trim());
    }
    
    public void saveminMax(final CopyOnWriteArrayList minMaxList) {
        Utilities.saveCopyOnWriteArrayListObject(minMaxList, "minMaxList.dat");
    }
    
    public String getMaxValue(final String msg, final CopyOnWriteArrayList minMaxList) {
        for (int i = 0; i < minMaxList.size(); ++i) {
            if (this.parseDevice(minMaxList.get(i).toString()).equals(this.parseDevice(msg))) {
                MaxMin.Max = minMaxList.get(i).toString().split(":")[4];
            }
        }
        return MaxMin.Max;
    }
    
    public String getMaxTime(final String msg, final CopyOnWriteArrayList minMaxList) {
        for (int i = 0; i < minMaxList.size(); ++i) {
            if (this.parseDevice(minMaxList.get(i).toString()).equals(this.parseDevice(msg))) {
                this.maxTime = minMaxList.get(i).toString().split(":")[3];
            }
        }
        return this.maxTime;
    }
    
    public String getMinValue(final String msg, final CopyOnWriteArrayList minMaxList) {
        for (int i = 0; i < minMaxList.size(); ++i) {
            if (this.parseDevice(minMaxList.get(i).toString()).equals(this.parseDevice(msg))) {
                MaxMin.Min = minMaxList.get(i).toString().split(":")[2];
            }
        }
        return MaxMin.Min;
    }
    
    public String getMinTime(final String msg, final CopyOnWriteArrayList minMaxList) {
        for (int i = 0; i < minMaxList.size(); ++i) {
            if (this.parseDevice(minMaxList.get(i).toString()).equals(this.parseDevice(msg))) {
                this.minTime = minMaxList.get(i).toString().split(":")[1];
            }
        }
        return this.minTime;
    }
}
