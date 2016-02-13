// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import java.io.Serializable;

public class FloatRecord implements Serializable
{
    private long measurementTime;
    private float measurementValue;
    
    public FloatRecord() {
    }
    
    public FloatRecord(final long measurementTime, final float measurementValue) {
        this.measurementTime = measurementTime;
        this.measurementValue = measurementValue;
    }
    
    public synchronized long getMeasurementTime() {
        this.notifyAll();
        return this.measurementTime;
    }
    
    public void setMeasurementTime(final long measurementTime) {
        synchronized (this) {
            this.measurementTime = measurementTime;
            this.notifyAll();
        }
    }
    
    public void setMeasurementValue(final long measurementValue) {
        synchronized (this) {
            this.measurementValue = measurementValue;
            this.notifyAll();
        }
    }
    
    public void setMeasuementValue(final String measurementValue) {
    }
    
    public synchronized float getMeasurementValue() {
        this.notifyAll();
        return this.measurementValue;
    }
}
