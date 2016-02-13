// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import java.util.ArrayList;

public class WindSpeedProcessor
{
    protected static long currentCount;
    protected static long currentTicks;
    FloatRecord windRecord;
    protected static long lastTicks;
    protected static long lastCount;
    protected static float currentWindSpeed;
    protected static float windGust;
    protected static ArrayList windSpeedList;
    public static long WIND_SPEED_AVERAGE_INTERVAL;
    public static long WIND_SPEED_GUST_INTERVAL;
    private static float averageWindSpeed;
    private static long averageStartCount;
    
    public WindSpeedProcessor() {
        this.windRecord = null;
    }
    
    public void processWind(final Long windcount, final boolean sendMessageNow) {
        WindSpeedProcessor.currentCount = windcount;
        this.hardwareRead();
        if (sendMessageNow) {
            findAverage();
            this.startNewAverage();
        }
    }
    
    public String hardwareRead() {
        WindSpeedProcessor.currentTicks = System.currentTimeMillis();
        if (WindSpeedProcessor.averageStartCount == 0L) {
            WindSpeedProcessor.averageStartCount = WindSpeedProcessor.currentCount;
        }
        if (WindSpeedProcessor.lastTicks != 0L) {
            WindSpeedProcessor.currentWindSpeed = (WindSpeedProcessor.currentCount - WindSpeedProcessor.lastCount) / ((WindSpeedProcessor.currentTicks - WindSpeedProcessor.lastTicks) / 1000.0f) / 2.0f * 2.453f;
            if (WindSpeedProcessor.currentWindSpeed > 130.0f) {
                WindSpeedProcessor.currentWindSpeed = 0.0f;
            }
        }
        if (WindSpeedProcessor.currentWindSpeed > 0.0f) {
            this.windRecord = new FloatRecord(WindSpeedProcessor.currentTicks, WindSpeedProcessor.currentWindSpeed);
            WindSpeedProcessor.windSpeedList.add(this.windRecord);
        }
        WindSpeedProcessor.lastCount = WindSpeedProcessor.currentCount;
        WindSpeedProcessor.lastTicks = WindSpeedProcessor.currentTicks;
        this.windRecord = null;
        return new Float(WindSpeedProcessor.currentWindSpeed).toString();
    }
    
    public static float findAverage() {
        int count = 0;
        long startingTime = 0L;
        long endingTime = 0L;
        boolean startingAvgMeasure = true;
        WindSpeedProcessor.currentTicks = System.currentTimeMillis();
        WindSpeedProcessor.windGust = 0.0f;
        if (WindSpeedProcessor.windSpeedList.size() > 0) {
            final FloatRecord floatRecord = (FloatRecord) WindSpeedProcessor.windSpeedList.get(WindSpeedProcessor.windSpeedList.size() - 1);
        }
        while (count < WindSpeedProcessor.windSpeedList.size()) {
            final FloatRecord windRecord = (FloatRecord) WindSpeedProcessor.windSpeedList.get(count);
            if (WindSpeedProcessor.currentTicks - windRecord.getMeasurementTime() > WindSpeedProcessor.WIND_SPEED_GUST_INTERVAL) {
                WindSpeedProcessor.windSpeedList.remove(windRecord);
            }
            else {
                if (windRecord.getMeasurementValue() > WindSpeedProcessor.windGust) {
                    System.out.print("New gust value :" + windRecord.getMeasurementValue() + "             \r");
                    WindSpeedProcessor.windGust = windRecord.getMeasurementValue();
                    WindSpeedProcessor.windGust = Utilities.roundValue2(WindSpeedProcessor.windGust);
                    if (WindSpeedProcessor.windGust > 140.0f) {
                        WindSpeedProcessor.windGust = 0.0f;
                    }
                }
                if (WindSpeedProcessor.currentTicks - windRecord.getMeasurementTime() <= WindSpeedProcessor.WIND_SPEED_AVERAGE_INTERVAL) {
                    if (startingAvgMeasure) {
                        startingAvgMeasure = false;
                        startingTime = windRecord.getMeasurementTime();
                    }
                    endingTime = windRecord.getMeasurementTime();
                }
                ++count;
            }
        }
        WindSpeedProcessor.averageWindSpeed = (WindSpeedProcessor.lastCount - WindSpeedProcessor.averageStartCount) / ((endingTime - startingTime) / 1000.0f) / 2.0f * 2.453f;
        WindSpeedProcessor.averageWindSpeed = Utilities.roundValue2(WindSpeedProcessor.averageWindSpeed);
        if (WindSpeedProcessor.averageWindSpeed > 130.0f) {
            WindSpeedProcessor.averageWindSpeed = 0.0f;
        }
        final FloatRecord windRecord = null;
        return WindSpeedProcessor.averageWindSpeed;
    }
    
    public void startNewAverage() {
        WindSpeedProcessor.averageStartCount = WindSpeedProcessor.lastCount;
    }
    
    public String getStringValue() {
        return new Float(WindSpeedProcessor.currentWindSpeed).toString() + "," + new Float(WindSpeedProcessor.averageWindSpeed).toString() + "," + new Float(WindSpeedProcessor.windGust).toString();
    }
    
    public static synchronized float getCurrentWindSpeed() {
        return Utilities.roundValue2(WindSpeedProcessor.currentWindSpeed);
    }
    
    public static long getLastTicks() {
        return WindSpeedProcessor.lastTicks;
    }
    
    public static long getLastCount() {
        return WindSpeedProcessor.lastCount;
    }
    
    public static synchronized float getAverageWindSpeed() {
        return WindSpeedProcessor.averageWindSpeed;
    }
    
    public static synchronized float getWindGust() {
        return WindSpeedProcessor.windGust;
    }
    
    static {
        WindSpeedProcessor.currentCount = 0L;
        WindSpeedProcessor.currentTicks = 0L;
        WindSpeedProcessor.lastTicks = 0L;
        WindSpeedProcessor.lastCount = 0L;
        WindSpeedProcessor.currentWindSpeed = 0.0f;
        WindSpeedProcessor.windGust = 0.0f;
        WindSpeedProcessor.windSpeedList = new ArrayList();
        WindSpeedProcessor.WIND_SPEED_AVERAGE_INTERVAL = 120000L;
        WindSpeedProcessor.WIND_SPEED_GUST_INTERVAL = 600000L;
        WindSpeedProcessor.averageWindSpeed = 0.0f;
        WindSpeedProcessor.averageStartCount = 0L;
    }
}
