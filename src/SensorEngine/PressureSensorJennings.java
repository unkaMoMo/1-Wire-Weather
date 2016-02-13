// 
// Decompiled by Procyon v0.5.30
// 

package SensorEngine;

import com.dalsemi.onewire.container.OneWireContainer26;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;

public class PressureSensorJennings extends PressureSensor
{
    protected double slope;
    private static double barometerTemp;
    
    public PressureSensorJennings(final DSPortAdapter adapter, final String deviceAddress, final double intercept, final String localName, final SocketServer DataServer, final HubChannelMap hubChannel) throws OneWireException {
        super(adapter, deviceAddress, intercept, localName, DataServer, hubChannel);
        this.slope = 0.0;
        if (this.sensor == null) {
            throw new OneWireException("No pressure sensor found with deviceAddress " + deviceAddress);
        }
    }
    
    @Override
    public String hardwareRead() throws OneWireException {
        final byte[] state = ((OneWireContainer26)this.sensor).readDevice();
        ((OneWireContainer26)this.sensor).doADConvert(1, state);
        final double Vad = ((OneWireContainer26)this.sensor).getADVoltage(1, state);
        ((OneWireContainer26)this.sensor).doADConvert(0, state);
        final double Vdd = ((OneWireContainer26)this.sensor).getADVoltage(0, state);
        final OneWireContainer26 bt = (OneWireContainer26)this.sensor;
        bt.doTemperatureConvert(state);
        PressureSensorJennings.barometerTemp = bt.getTemperature(state);
        this.setBaroTemp();
        this.pressuremB = ((Vad - 1.25) / 7.0 * 3.0 + 28.0) * 33.863882 + this.intercept;
        return new Double(this.pressuremB).toString();
    }
    
    public double getSlope() {
        return this.slope;
    }
    
    public void setSlope(final double slope) {
        this.slope = slope;
    }
    
    public void setBaroTemp() {
        PressureSensorJennings.barometerTemp = PressureSensorJennings.barometerTemp;
    }
    
    public static double getBaroTemp() {
        return PressureSensorJennings.barometerTemp;
    }
}
