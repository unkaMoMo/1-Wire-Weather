// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import java.util.Iterator;
import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.Serializable;

public class BaroTrend implements Serializable
{
    protected long currentTicks;
    FloatRecord barorecord;
    protected long lastTicks;
    protected CopyOnWriteArrayList baroList;
    protected float currentBarometer;
    protected long trendInterval;
    String temp;
    public static String trend;
    String tmp;
    double rate;
    float oldBarometer;
    long oldBaroTime;
    
    public BaroTrend() {
        this.currentTicks = 0L;
        this.barorecord = null;
        this.lastTicks = 0L;
        this.baroList = new CopyOnWriteArrayList();
        this.currentBarometer = 0.0f;
        this.trendInterval = 10800000L;
        this.temp = "";
        this.tmp = "";
        this.rate = 0.0;
        this.oldBarometer = 0.0f;
        this.oldBaroTime = 0L;
        if (this.baroList.isEmpty()) {
            System.out.println("Barometer trend ArrayList not loaded.Trying to load from file..");
            try {
                final File file = new File("barotrend.dat");
                if (file.exists()) {
                    this.baroList = Utilities.restoreCopyOnWriteArrayListObject("barotrend.dat");
                }
            }
            catch (Exception e) {
                System.err.println("Got an error in Barotrend file loading process ");
                e.printStackTrace();
            }
        }
    }
    
    public void updateRecords(final String msg) {
        this.currentBarometer = Float.parseFloat(msg.split(":")[2]);
        this.currentTicks = System.currentTimeMillis();
        this.barorecord = new FloatRecord(this.currentTicks, this.currentBarometer);
        System.out.println("New Barometer record added to ArrayList");
        this.baroList.add(this.barorecord);
        if (this.baroList.size() > 0) {
            this.tmp = " *";
            try {
                this.currentTicks = System.currentTimeMillis();
                boolean removedRecords = false;
                final Iterator e = this.baroList.iterator();
                while (e.hasNext()) {
                    this.barorecord = (FloatRecord) e.next();
                    if (this.currentTicks - this.barorecord.getMeasurementTime() > this.trendInterval) {
                        this.baroList.remove(this.barorecord);
                        this.tmp = "";
                        System.out.println("Removing old record from baroTrend list ");
                        this.oldBarometer = this.barorecord.getMeasurementValue();
                        removedRecords = true;
                    }
                }
                if (!removedRecords) {
                    for (int i = 0; i < this.baroList.size() - 1; ++i) {
                        this.barorecord = (FloatRecord) this.baroList.get(i);
                        if (this.barorecord.getMeasurementTime() < this.currentTicks) {
                            this.oldBarometer = this.barorecord.getMeasurementValue();
                            this.currentTicks = this.barorecord.getMeasurementTime();
                        }
                    }
                }
                if (this.oldBarometer != 0.0f) {
                    if (this.oldBarometer > this.currentBarometer) {
                        this.temp = "Falling ";
                        this.rate = this.oldBarometer - this.currentBarometer;
                    }
                    else if (this.oldBarometer < this.currentBarometer) {
                        this.temp = "Rising ";
                        this.rate = this.currentBarometer - this.oldBarometer;
                    }
                    if (this.rate >= 6.0) {
                        BaroTrend.trend = this.temp + " very rapidly" + this.tmp;
                    }
                    else if (this.rate >= 3.5 && this.rate < 6.0) {
                        BaroTrend.trend = this.temp + " rapidly" + this.tmp;
                    }
                    else if (this.rate >= 1.5 && this.rate < 3.5) {
                        BaroTrend.trend = this.temp;
                    }
                    else if (this.rate > 0.4 && this.rate < 1.5) {
                        BaroTrend.trend = this.temp + " slowly" + this.tmp;
                    }
                    else {
                        BaroTrend.trend = "Steady" + this.tmp;
                    }
                }
                this.setTrend(BaroTrend.trend);
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        Utilities.saveCopyOnWriteArrayListObject(this.baroList, "barotrend.dat");
        this.barorecord = null;
    }
    
    public void setTrend(final String trend) {
        BaroTrend.trend = trend;
    }
    
    public static String getTrend() {
        return BaroTrend.trend;
    }
    
    static {
        BaroTrend.trend = "";
    }
}
