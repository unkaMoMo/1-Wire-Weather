// 
// Decompiled by Procyon v0.5.30
// 

package SensorEngine;

import com.dalsemi.onewire.container.OneWireContainer26;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;

public class SolarSensor extends Sensor
{
    protected double current;
    protected double VDD;
    protected double VAD;
    protected double VSENSE;
    public static double solarTemp;
    
    public SolarSensor(final DSPortAdapter adapter, final String deviceAddress, final double senseResistorValue, final String localName, final SocketServer DataServer, final HubChannelMap hubChannel) throws OneWireException {
        super(adapter, deviceAddress, localName, DataServer, hubChannel);
        this.current = 0.0;
        this.VDD = 0.0;
        this.VAD = 0.0;
        this.VSENSE = 0.0;
        if (this.sensor == null) {
            throw new OneWireException("No solar sensor found with deviceAddress " + deviceAddress);
        }
        ((OneWireContainer26)this.sensor).setSenseResistor(senseResistorValue);
    }
    
    public String hardwareRead() throws OneWireException {
        final byte[] state = ((OneWireContainer26)this.sensor).readDevice();
        ((OneWireContainer26)this.sensor).setFlag((byte)1, true);
        ((OneWireContainer26)this.sensor).doADConvert(1, state);
        this.VAD = ((OneWireContainer26)this.sensor).getADVoltage(1, state);
        ((OneWireContainer26)this.sensor).doADConvert(0, state);
        this.VDD = ((OneWireContainer26)this.sensor).getADVoltage(0, state);
        ((OneWireContainer26)this.sensor).doADConvert(2, state);
        this.VSENSE = ((OneWireContainer26)this.sensor).getADVoltage(2, state);
        this.current = ((OneWireContainer26)this.sensor).getCurrent(state);
        final OneWireContainer26 st = (OneWireContainer26)this.sensor;
        st.doTemperatureConvert(state);
        SolarSensor.solarTemp = st.getTemperature(state);
        this.setSolarTemp();
        try {
            this.VSENSE = Sensor.roundValue(this.VSENSE);
        }
        catch (Exception e) {
            System.out.println("Exception is " + e);
        }
        return new Double(this.VSENSE).toString();
    }
    
    @Override
    public String getStringValue() {
        return new Double(this.VDD).toString() + "," + new Double(this.VAD).toString() + "," + new Double(this.VSENSE).toString() + "," + new Double(this.current).toString();
    }
    
    public void setSolarTemp() {
        SolarSensor.solarTemp = SolarSensor.solarTemp;
    }
    
    public static double getSolarTemp() {
        return SolarSensor.solarTemp;
    }
}
