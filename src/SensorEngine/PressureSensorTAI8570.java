// 
// Decompiled by Procyon v0.5.30
// 

package SensorEngine;

import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer12;

public class PressureSensorTAI8570 extends PressureSensor
{
    private static final String SEC_RESET = "101010101010101000000";
    private static final String SEC_READW1 = "111010101000";
    private static final String SEC_READW2 = "111010110000";
    private static final String SEC_READW3 = "111011001000";
    private static final String SEC_READW4 = "111011010000";
    private static final String SEC_READD1 = "11110100000";
    private static final String SEC_READD2 = "11110010000";
    private static final byte CHANNEL_ACCESS = -11;
    private static final byte CFG_LECTURA = -20;
    private static final byte CFG_ESCRITURA = -116;
    private static final byte CFG_LECPULSO = -56;
    private String deviceAddress2;
    private OneWireContainer12 IDWrite;
    private OneWireContainer12 IDRead;
    private int C1;
    private int C2;
    private int C3;
    private int C4;
    private int C5;
    private int C6;
    private int D1;
    private int D2;
    private boolean validCFG;
    private DSPortAdapter pto;
    private double height_meters;
    private double Presion_mbar;
    private static double Temp_C;
    
    public PressureSensorTAI8570(final DSPortAdapter adapter, final String deviceAddress1, final String deviceAddress2, final double intercept, final String localName, final SocketServer DataServer, final HubChannelMap hubChannel) throws OneWireException {
        super(adapter, deviceAddress1, intercept, localName, DataServer, hubChannel);
        this.C1 = 0;
        this.C2 = 0;
        this.C3 = 0;
        this.C4 = 0;
        this.C5 = 0;
        this.C6 = 0;
        this.D1 = 0;
        this.D2 = 0;
        this.validCFG = false;
        this.height_meters = 0.0;
        this.Presion_mbar = 0.0;
        this.deviceAddress2 = deviceAddress2;
        if (this.sensor == null) {
            throw new OneWireException("No pressure sensor found with deviceAddress " + this.deviceAddress);
        }
        this.openChannel();
        System.out.println("Now creating the internal objects");
        this.create_object(adapter, new OneWireContainer(adapter, deviceAddress1).getAddress(), new OneWireContainer(adapter, deviceAddress2).getAddress());
        System.out.println("Done creating the internal objects");
        this.closeChannel();
    }
    
    private void create_object(final DSPortAdapter pto, final byte[] adUno, final byte[] adDos) throws OneWireException {
        this.pto = pto;
        final OneWireContainer12 tmp1 = new OneWireContainer12(pto, adUno);
        final OneWireContainer12 tmp2 = new OneWireContainer12(pto, adDos);
        System.out.println("Reading device 1");
        final byte[] state1 = tmp1.readDevice();
        System.out.println("Reading device 2");
        final byte[] state2 = tmp2.readDevice();
        if (tmp1.isPowerSupplied(state1) == tmp2.isPowerSupplied(state2)) {
            throw new OneWireException("PressureSensorTAI8570 not valid");
        }
        if (tmp1.isPowerSupplied(state1)) {
            this.IDWrite = tmp1;
            this.IDRead = tmp2;
        }
        else {
            this.IDWrite = tmp2;
            this.IDRead = tmp1;
        }
    }
    
    @Override
    public String hardwareRead() throws OneWireException {
        try {
            this.readDevice();
            this.pressuremB = this.getPressure_mbar() + this.intercept;
        }
        catch (Exception e) {
            throw new OneWireException(e.toString());
        }
        return new Double(this.pressuremB).toString();
    }
    
    private void PrepPIOS2Write() {
        final boolean rslt = false;
        try {
            final byte[] state1 = this.IDWrite.readDevice();
            this.IDWrite.setLatchState(0, true, false, state1);
            this.IDWrite.setLatchState(1, false, false, state1);
            this.IDWrite.writeDevice(state1);
            final byte[] state2 = this.IDRead.readDevice();
            this.IDRead.setLatchState(0, false, false, state2);
            this.IDRead.setLatchState(1, false, false, state2);
            this.IDRead.writeDevice(state2);
        }
        catch (Exception ex) {}
    }
    
    private void PrepPIOS2Read() {
        final boolean rslt = false;
        try {
            final byte[] state2 = this.IDRead.readDevice();
            this.IDRead.setLatchState(0, false, false, state2);
            this.IDRead.setLatchState(1, false, false, state2);
            this.IDRead.writeDevice(state2);
            final byte[] state3 = this.IDWrite.readDevice();
            this.IDWrite.setLatchState(0, false, false, state3);
            this.IDWrite.setLatchState(1, false, false, state3);
            this.IDWrite.writeDevice(state3);
        }
        catch (Exception ex) {}
    }
    
    private boolean OpenPIOS(final int Pio) {
        boolean rslt = false;
        try {
            byte[] state1 = this.IDWrite.readDevice();
            byte[] state2 = this.IDRead.readDevice();
            this.IDWrite.setLatchState(Pio, false, false, state1);
            this.IDRead.setLatchState(Pio, false, false, state2);
            this.IDWrite.writeDevice(state1);
            this.IDWrite.writeDevice(state2);
            state1 = this.IDWrite.readDevice();
            state2 = this.IDRead.readDevice();
            rslt = (this.IDWrite.getLevel(Pio, state1) && this.IDRead.getLevel(Pio, state2));
        }
        catch (Exception ex) {}
        return rslt;
    }
    
    private boolean OpenPIOS_B() {
        final int Pio_B = 1;
        return this.OpenPIOS(Pio_B);
    }
    
    private boolean OpenPIOS_A() {
        final int Pio_A = 0;
        return this.OpenPIOS(Pio_A);
    }
    
    private boolean ConfigEscritura() {
        boolean rslt = false;
        try {
            this.PrepPIOS2Write();
            if (this.pto.isPresent(this.IDWrite.getAddress())) {
                this.pto.putByte(-11);
                this.pto.putByte(-116);
                this.pto.putByte(255);
                final int tmp = this.pto.getByte();
                rslt = true;
            }
        }
        catch (Exception ex) {}
        return rslt;
    }
    
    private boolean ConfigLectura() {
        boolean rslt = false;
        try {
            this.PrepPIOS2Read();
            if (this.pto.isPresent(this.IDRead.getAddress())) {
                this.pto.putByte(-11);
                this.pto.putByte(-20);
                this.pto.putByte(255);
                final int tmp = this.pto.getByte();
                rslt = true;
            }
        }
        catch (Exception ex) {}
        return rslt;
    }
    
    private boolean ConfigLecturaPulso() {
        try {
            this.PrepPIOS2Write();
            if (!this.pto.isPresent(this.IDRead.getAddress())) {
                return false;
            }
            this.pto.putByte(-11);
            this.pto.putByte(-56);
            this.pto.putByte(255);
            final int tmp = this.pto.getByte();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    private boolean WritePressureSensor(String Cmd) {
        boolean rslt = false;
        Cmd += "0";
        final int lng = Cmd.length();
        if (this.ConfigEscritura()) {
            for (int i = 0; i < lng; ++i) {
                if (Cmd.charAt(i) == '0') {
                    this.SendPressureBit(false);
                }
                else {
                    this.SendPressureBit(true);
                }
            }
            rslt = true;
        }
        return rslt;
    }
    
    private void SendPressureBit(final boolean i) {
        try {
            if (i) {
                this.pto.putBit(false);
                this.pto.putBit(true);
                this.pto.putBit(true);
                this.pto.putBit(true);
                this.pto.putBit(false);
                this.pto.putBit(false);
            }
            else {
                this.pto.putBit(false);
                this.pto.putBit(false);
                this.pto.putBit(true);
                this.pto.putBit(false);
                this.pto.putBit(false);
                this.pto.putBit(false);
            }
        }
        catch (Exception ex) {}
    }
    
    private boolean ReadPressureBit() {
        boolean rslt = false;
        try {
            boolean t = this.pto.getBit();
            t = this.pto.getBit();
            t = this.pto.getBit();
            t = this.pto.getBit();
            t = this.pto.getBit();
            t = this.pto.getBit();
            t = this.pto.getBit();
            rslt = this.pto.getBit();
            this.pto.putBit(false);
            this.pto.putBit(true);
            this.pto.putBit(false);
            this.pto.putBit(true);
            this.pto.putBit(true);
            this.pto.putBit(true);
            this.pto.putBit(true);
            this.pto.putBit(true);
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return rslt;
    }
    
    private String ReadPressureSensor(final byte CountBit) {
        final StringBuffer tmp = new StringBuffer();
        if (this.ConfigLectura()) {
            for (int i = 0; i < CountBit; ++i) {
                if (this.ReadPressureBit()) {
                    tmp.append('1');
                }
                else {
                    tmp.append('0');
                }
            }
        }
        return tmp.toString();
    }
    
    private boolean PressureReset() {
        boolean rsl = false;
        rsl = this.WritePressureSensor("101010101010101000000");
        return rsl;
    }
    
    private String ReadSensorValueStr(final String cmd) {
        String Lect = "0000000000000000";
        if (this.WritePressureSensor(cmd)) {
            final String tmp = this.ReadPressureSensor((byte)16);
            final boolean t = this.OpenPIOS_B();
            Lect = tmp;
        }
        return Lect;
    }
    
    private int ReadSensorValue(final String cmd) {
        if (!this.PressureReset()) {
            return 0;
        }
        if (!this.WritePressureSensor(cmd)) {
            return 0;
        }
        if (!this.CheckConversion()) {
            return 0;
        }
        if (!this.OpenPIOS_A()) {
            return 0;
        }
        final String Lect = this.ReadPressureSensor((byte)16);
        if (!this.OpenPIOS_B()) {
            return 0;
        }
        if (Lect.length() != 16) {
            return 0;
        }
        int Rslt = 0;
        for (int i = 0; i < 16; ++i) {
            Rslt <<= 1;
            if (Lect.charAt(i) == '1') {
                ++Rslt;
            }
        }
        return Rslt;
    }
    
    private boolean CheckConversion() {
        if (!this.ConfigLecturaPulso()) {
            return false;
        }
        try {
            int i;
            for (i = 0; i < 100 && this.pto.getBit(); ++i) {}
            return i < 100;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    private boolean ReadSensor_Calibration() {
        if (!this.PressureReset()) {
            return false;
        }
        final String Sw1 = this.ReadSensorValueStr("111010101000");
        if (Sw1.length() != 16) {
            return false;
        }
        final String Sw2 = this.ReadSensorValueStr("111010110000");
        if (Sw2.length() != 16) {
            return false;
        }
        final String Sw3 = this.ReadSensorValueStr("111011001000");
        if (Sw3.length() != 16) {
            return false;
        }
        final String Sw4 = this.ReadSensorValueStr("111011010000");
        if (Sw4.length() != 16) {
            return false;
        }
        final boolean c1 = false;
        this.C6 = (c1 ? 1 : 0);
        this.C5 = (c1 ? 1 : 0);
        this.C3 = (c1 ? 1 : 0);
        this.C2 = (c1 ? 1 : 0);
        this.C1 = (c1 ? 1 : 0);
        for (int i = 0; i < 15; ++i) {
            this.C1 <<= 1;
            if (Sw1.charAt(i) == '1') {
                ++this.C1;
            }
        }
        if (Sw1.charAt(15) == '1') {
            this.C5 = 1;
        }
        for (int i = 0; i < 10; ++i) {
            this.C5 <<= 1;
            if (Sw2.charAt(i) == '1') {
                ++this.C5;
            }
        }
        for (int i = 10; i < 16; ++i) {
            this.C6 <<= 1;
            if (Sw2.charAt(i) == '1') {
                ++this.C6;
            }
        }
        for (int i = 0; i < 10; ++i) {
            this.C4 <<= 1;
            if (Sw3.charAt(i) == '1') {
                ++this.C4;
            }
        }
        for (int i = 10; i < 16; ++i) {
            this.C2 <<= 1;
            if (Sw3.charAt(i) == '1') {
                ++this.C2;
            }
        }
        for (int i = 10; i < 16; ++i) {
            this.C2 <<= 1;
            if (Sw4.charAt(i) == '1') {
                ++this.C2;
            }
        }
        for (int i = 0; i < 10; ++i) {
            this.C3 <<= 1;
            if (Sw4.charAt(i) == '1') {
                ++this.C3;
            }
        }
        return true;
    }
    
    private boolean ReadSensor_Values() {
        this.D1 = this.ReadSensorValue("11110100000");
        this.D2 = this.ReadSensorValue("11110010000");
        final double DT = this.D2 - (8 * this.C5 + 20224.0);
        final double OFF = this.C2 * 4 + (this.C4 - 512) * DT / 4096.0;
        final double SENSE = 24576 + this.C1 + this.C3 * DT / 1024.0;
        final double X = SENSE * (this.D1 - 7168) / 16384.0 - OFF;
        this.Presion_mbar = 250.0 + X / 32.0;
        PressureSensorTAI8570.Temp_C = 20.0 + DT * (this.C6 + 50) / 10240.0;
        return true;
    }
    
    public void readDevice() throws Exception {
        if (!this.validCFG) {
            if (!this.ReadSensor_Calibration()) {
                throw new Exception("Error reading calibration constants");
            }
            this.validCFG = true;
        }
        if (!this.ReadSensor_Values()) {
            throw new Exception("Error reading Pressure and Temperature values");
        }
    }
    
    public double getPressure_mbar() throws Exception {
        return this.Presion_mbar;
    }
    
    public String getPressure_mbar_S() throws Exception {
        return Double.toString(this.getPressure_mbar());
    }
    
    public double getPressure_atm() throws Exception {
        return this.getPressure_mbar() * 9.872E-4;
    }
    
    public String getPressure_atm_S() throws Exception {
        return Double.toString(this.getPressure_atm());
    }
    
    public double getPressure_KgCm2() throws Exception {
        return this.getPressure_mbar() * 0.00102;
    }
    
    public String getPressure_KgCm2_S() throws Exception {
        return Double.toString(this.getPressure_KgCm2());
    }
    
    public double getPressure_inHg() throws Exception {
        return this.getPressure_mbar() * 0.02954;
    }
    
    public String getPressure_inHg_S() throws Exception {
        return Double.toString(this.getPressure_inHg());
    }
    
    public double getPressure_inH2O() throws Exception {
        return this.getPressure_mbar() * 0.4018;
    }
    
    public String getPressuree_inH2O_S() throws Exception {
        return Double.toString(this.getPressure_inH2O());
    }
    
    public double getPressure_Pa() throws Exception {
        return this.getPressure_mbar() * 100.0;
    }
    
    public String getPressure_Pa_S() throws Exception {
        return Double.toString(this.getPressure_Pa());
    }
    
    public double getPressure_psi() throws Exception {
        return this.getPressure_mbar() * 0.0145;
    }
    
    public String getPressure_psi_S() throws Exception {
        return Double.toString(this.getPressure_psi());
    }
    
    public static double getTemp_C() throws Exception {
        return PressureSensorTAI8570.Temp_C;
    }
    
    public double getTemp_F() throws Exception {
        return 1.8 * getTemp_C() + 32.0;
    }
    
    public static String getTemp_C_S() throws Exception {
        return Double.toString(getTemp_C());
    }
    
    public String getTemp_F_S() throws Exception {
        return Double.toString(this.getTemp_F());
    }
    
    void set_height_over_sea_level_meter(final float height) {
        this.height_meters = height;
    }
    
    void set_height_over_sea_level_feet(final float height) {
        this.height_meters = height * 0.3048;
    }
    
    static {
        PressureSensorTAI8570.Temp_C = 0.0;
    }
}
