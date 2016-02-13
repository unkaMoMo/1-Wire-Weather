// 
// Decompiled by Procyon v0.5.30
// 

package SensorEngine;

import com.dalsemi.onewire.container.OneWireContainer1F;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer;

public class Sensor
{
    protected OneWireContainer sensor;
    protected HubChannelMap hubChannel;
    protected DSPortAdapter adapter;
    protected String deviceAddress;
    protected long lastReadTime;
    protected long errorCount;
    protected long maxErrors;
    protected boolean debugFlag;
    protected final int MAX_RETRY_COUNT = 5;
    protected String localName;
    protected SocketServer DataServer;
    protected static HubChannelMap lastChannel;
    
    public Sensor() {
        this.sensor = null;
        this.hubChannel = null;
        this.lastReadTime = 0L;
        this.errorCount = 0L;
        this.maxErrors = 100000L;
        this.debugFlag = true;
    }
    
    public Sensor(final DSPortAdapter adapter, final String address, final String localName, final SocketServer DataServer, final HubChannelMap hubChannel) throws OneWireException {
        this.sensor = null;
        this.hubChannel = null;
        this.lastReadTime = 0L;
        this.errorCount = 0L;
        this.maxErrors = 100000L;
        this.debugFlag = true;
        this.adapter = adapter;
        this.deviceAddress = address;
        this.localName = localName;
        this.DataServer = DataServer;
        this.hubChannel = hubChannel;
        this.sensor = adapter.getDeviceContainer(this.deviceAddress);
        this.openChannel();
        if (this.sensor.isPresent()) {
            this.runInitialize();
        }
        if (this.sensor == null) {
            this.closeChannel();
            throw new OneWireException("No device found with deviceAddress " + this.deviceAddress);
        }
        this.closeChannel();
    }
    
    public String readSensor() throws OneWireException {
        String value = "";
        int tryCount = 0;
        boolean gotGoodRead = false;
        do {
            try {
                this.openChannel();
                value = this.hardwareRead();
                gotGoodRead = true;
            }
            catch (Exception e) {
                ++tryCount;
                if (!this.sensor.isPresent()) {
                    ++this.errorCount;
                    System.out.println("Sensor " + this.deviceAddress + " Not Present for hardwareRead. ErrorCount=  " + this.errorCount);
                }
                try {
                    Thread.sleep(1000L);
                }
                catch (Exception ex) {}
            }
        } while (tryCount < 5 && !gotGoodRead);
        if (tryCount >= 5) {
            System.out.println(this.deviceAddress + " Exceeded Max read attemps of " + 5 + " Resetting Bus");
            System.out.println("Skipping " + this.deviceAddress);
            this.adapter.reset();
            this.runInitialize();
            return "-999";
        }
        return value;
    }
    
    protected String hardwareRead() throws OneWireException {
        return "Base class implementation";
    }
    
    public String getStringValue() {
        return "Base class implementation";
    }
    
    public static float roundValue(final float value) {
        return (int)(value * 100.0f + 0.5) / 100.0f;
    }
    
    public static double roundValue(final double value) {
        return (int)(value * 100.0 + 0.5) / 100.0;
    }
    
    public boolean isPresent() throws OneWireException, OneWireIOException {
        boolean present = false;
        this.openChannel();
        present = this.sensor.isPresent();
        this.closeChannel();
        return this.isPresent();
    }
    
    public OneWireContainer getSensor() {
        return this.sensor;
    }
    
    public long getErrorCount() {
        return this.errorCount;
    }
    
    public long getMaxErrors() {
        return this.maxErrors;
    }
    
    public void setErrorCount(final long errorCount) {
        this.errorCount = errorCount;
    }
    
    public long getLastReadTime() {
        return this.lastReadTime;
    }
    
    public String getDeviceAddress() {
        return this.deviceAddress;
    }
    
    public DSPortAdapter getAdapter() {
        return this.adapter;
    }
    
    public void setAdapter(final DSPortAdapter adapter) {
        this.adapter = adapter;
    }
    
    public void setDeviceAddress(final String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }
    
    public String getAdapterName() {
        return this.adapter.getAdapterName();
    }
    
    public String getLocalName() {
        return this.localName;
    }
    
    protected void openChannel() throws OneWireException, OneWireIOException {
        if (this.hubChannel != null && Sensor.lastChannel != this.hubChannel) {
            this.closeChannel(Sensor.lastChannel);
            final OneWireContainer1F switchcontainer = this.hubChannel.getHubDevice();
            final int channel = this.hubChannel.getChannel();
            final byte[] state = switchcontainer.readDevice();
            switchcontainer.setLatchState(channel, true, false, state);
            switchcontainer.writeDevice(state);
            Sensor.lastChannel = this.hubChannel;
        }
    }
    
    protected void closeChannel() throws OneWireException, OneWireIOException {
        if (this.hubChannel != null) {
            this.closeChannel(this.hubChannel);
        }
    }
    
    protected void closeChannel(final HubChannelMap channelSelect) throws OneWireException, OneWireIOException {
        if (channelSelect != null) {
            final OneWireContainer1F switchcontainer = channelSelect.getHubDevice();
            final int channel = channelSelect.getChannel();
            final byte[] state = switchcontainer.readDevice();
            switchcontainer.setLatchState(channel, false, false, state);
            switchcontainer.writeDevice(state);
            Sensor.lastChannel = null;
        }
    }
    
    protected void runInitialize() throws OneWireException {
        boolean completed = false;
        int count = 0;
        OneWireException lastException = null;
        while (!completed && count < 10) {
            try {
                this.initialize();
                completed = true;
            }
            catch (OneWireException e) {
                ++count;
                lastException = e;
                try {
                    Thread.sleep(500L);
                }
                catch (Exception ex) {}
            }
        }
        if (count == 10) {
            throw lastException;
        }
    }
    
    protected void initialize() throws OneWireException {
    }
    
    static {
        Sensor.lastChannel = null;
    }
}
