// 
// Decompiled by Procyon v0.5.30
// 

package SensorEngine;

import com.dalsemi.onewire.container.OneWireContainer26;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;

public class BatteryMonitor extends Sensor
{
    private static double batteryTemp;
    private static double batteryVolts;
    
    public BatteryMonitor(final DSPortAdapter adapter, final String deviceAddress, final String localName, final SocketServer DataServer, final HubChannelMap hubChannel) throws OneWireException {
        super(adapter, deviceAddress, localName, DataServer, hubChannel);
        if (this.sensor == null) {
            throw new OneWireException("No battery monitor found with deviceAddress " + deviceAddress);
        }
    }
    
    public String hardwareRead() throws OneWireException {
        final byte[] state = ((OneWireContainer26)this.sensor).readDevice();
        ((OneWireContainer26)this.sensor).doADConvert(1, state);
        final double Vad = BatteryMonitor.batteryVolts = ((OneWireContainer26)this.sensor).getADVoltage(1, state);
        this.setBatteryVolts();
        final OneWireContainer26 batt = (OneWireContainer26)this.sensor;
        final byte[] state2 = batt.readDevice();
        batt.doTemperatureConvert(state2);
        BatteryMonitor.batteryTemp = batt.getTemperature(state2);
        this.setBatteryTemp();
        return new Double(BatteryMonitor.batteryVolts).toString();
    }
    
    public void setBatteryTemp() {
        BatteryMonitor.batteryTemp = BatteryMonitor.batteryTemp;
    }
    
    public static double getBatteryTemp() {
        return BatteryMonitor.batteryTemp;
    }
    
    public void setBatteryVolts() {
        BatteryMonitor.batteryVolts = BatteryMonitor.batteryVolts;
    }
    
    public static double getBatteryVolts() {
        return BatteryMonitor.batteryVolts;
    }
}
