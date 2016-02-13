// 
// Decompiled by Procyon v0.5.30
// 

package SensorEngine;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;

public class PressureSensor extends Sensor
{
    protected double pressuremB;
    protected double intercept;
    
    public PressureSensor(final DSPortAdapter adapter, final String deviceAddress, final double intercept, final String localName, final SocketServer DataServer, final HubChannelMap hubChannel) throws OneWireException {
        super(adapter, deviceAddress, localName, DataServer, hubChannel);
        this.pressuremB = 0.0;
        this.intercept = 0.0;
        this.intercept = intercept;
        if (this.sensor == null) {
            throw new OneWireException("No pressure sensor found with deviceAddress " + deviceAddress);
        }
    }
    
    public String hardwareRead() throws OneWireException {
        return "Base class implementation";
    }
    
    @Override
    public String getStringValue() {
        return new Double(this.pressuremB).toString();
    }
    
    public double getPressure() {
        return this.pressuremB;
    }
    
    public double getIntercept() {
        return this.intercept;
    }
    
    public void setIntercept(final double intercept) {
        this.intercept = intercept;
    }
}
