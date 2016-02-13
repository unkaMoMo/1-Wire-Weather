// 
// Decompiled by Procyon v0.5.30
// 

package SensorEngine;

import com.dalsemi.onewire.container.OneWireContainer10;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;

public class TemperatureSensor extends Sensor
{
    protected double celsius;
    
    public TemperatureSensor(final DSPortAdapter adapter, final String deviceAddress, final String localName, final SocketServer DataServer, final HubChannelMap hubChannel) throws OneWireException {
        super(adapter, deviceAddress, localName, DataServer, hubChannel);
        this.celsius = 0.0;
        if (this.sensor == null) {
            throw new OneWireException("No DS1820 Temperature chip found with deviceAddress " + deviceAddress);
        }
    }
    
    public String hardwareRead() throws OneWireException {
        byte[] state = ((OneWireContainer10)this.sensor).readDevice();
        ((OneWireContainer10)this.sensor).doTemperatureConvert(state);
        state = ((OneWireContainer10)this.sensor).readDevice();
        this.celsius = ((OneWireContainer10)this.sensor).getTemperature(state);
        return new Float(this.celsius).toString();
    }
    
    @Override
    public String getStringValue() {
        return new Float(this.celsius).toString();
    }
    
    public double getCelsius() {
        return this.celsius;
    }
}
