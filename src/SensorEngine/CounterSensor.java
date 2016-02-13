// 
// Decompiled by Procyon v0.5.30
// 

package SensorEngine;

import com.dalsemi.onewire.container.OneWireContainer1D;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;

public class CounterSensor extends Sensor
{
    public long count;
    
    public CounterSensor(final DSPortAdapter adapter, final String deviceAddress, final String localName, final SocketServer DataServer, final HubChannelMap hubChannel) throws OneWireException {
        super(adapter, deviceAddress, localName, DataServer, hubChannel);
        this.count = 0L;
        if (this.sensor == null) {
            throw new OneWireException("No counter sensor found with deviceAddress " + deviceAddress);
        }
    }
    
    public String counterRead() throws OneWireException {
        this.count = ((OneWireContainer1D)this.sensor).readCounter(15);
        return new Long(this.count).toString();
    }
    
    public String hardwareRead() throws OneWireException {
        return this.counterRead();
    }
    
    @Override
    public String getStringValue() {
        return new Long(this.count).toString();
    }
    
    public long getCount() {
        return this.count;
    }
}
