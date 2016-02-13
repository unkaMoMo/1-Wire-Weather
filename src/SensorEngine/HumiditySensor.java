// 
// Decompiled by Procyon v0.5.30
// 

package SensorEngine;

import com.dalsemi.onewire.container.OneWireContainer26;
import com.dalsemi.onewire.container.HumidityContainer;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;

public class HumiditySensor extends Sensor
{
    protected double humidity;
    protected static double humidityTemp;
    protected double vdd;
    protected double slope;
    public double cal;
    protected double intercept;
    
    public HumiditySensor(final DSPortAdapter adapter, final String deviceAddress, final double slope, final double intercept, final String localName, final SocketServer DataServer, final HubChannelMap hubChannel) throws OneWireException {
        super(adapter, deviceAddress, localName, DataServer, hubChannel);
        this.humidity = 0.0;
        this.vdd = 0.0;
        this.slope = 1.0;
        this.cal = 1.0;
        this.intercept = 0.0;
        this.slope = slope;
        this.intercept = intercept;
        if (this.sensor == null) {
            throw new OneWireException("No humidity sensor found with deviceAddress " + deviceAddress);
        }
    }
    
    public String hardwareRead() throws OneWireException {
        final HumidityContainer hc = (HumidityContainer)this.sensor;
        final byte[] state = hc.readDevice();
        hc.doHumidityConvert(state);
        this.humidity = hc.getHumidity(state) * this.slope + this.intercept;
        final OneWireContainer26 ht = (OneWireContainer26)this.sensor;
        final byte[] state2 = ht.readDevice();
        ht.doTemperatureConvert(state2);
        HumiditySensor.humidityTemp = ht.getTemperature(state2);
        this.setHumidityTemp();
        this.humidity = this.humidity * this.cal + this.intercept;
        this.humidity = Sensor.roundValue(this.humidity);
        return new Double(this.humidity).toString();
    }
    
    @Override
    public String getStringValue() {
        return new Double(this.humidity).toString();
    }
    
    public double getHumidityIntercept() {
        return this.intercept;
    }
    
    public void setHumidityTemp() {
        HumiditySensor.humidityTemp = HumiditySensor.humidityTemp;
    }
    
    public double getHumidityTemp() {
        return HumiditySensor.humidityTemp;
    }
    
    public void setHumidityIntercept(final double humidityIntercept) {
        this.intercept = humidityIntercept;
    }
    
    public void setHumiditySlope(final double humiditySlope) {
        this.slope = humiditySlope;
    }
    
    public double getHumiditySlope() {
        return this.slope;
    }
    
    public double getHumidity() {
        return this.humidity;
    }
    
    static {
        HumiditySensor.humidityTemp = 0.0;
    }
}
