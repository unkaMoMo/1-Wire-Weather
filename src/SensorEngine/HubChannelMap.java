// 
// Decompiled by Procyon v0.5.30
// 

package SensorEngine;

import com.dalsemi.onewire.container.OneWireContainer1F;

public class HubChannelMap
{
    private OneWireContainer1F hubDevice;
    private int channel;
    
    public HubChannelMap(final OneWireContainer1F hubDevice, final int channel) {
        this.hubDevice = null;
        this.hubDevice = hubDevice;
        this.channel = channel;
    }
    
    protected int getChannel() {
        return this.channel;
    }
    
    protected OneWireContainer1F getHubDevice() {
        return this.hubDevice;
    }
}
