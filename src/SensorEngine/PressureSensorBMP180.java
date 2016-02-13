// 
// Decompiled by Procyon v0.5.30
// 

package SensorEngine;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.NumberFormat;
import com.pi4j.system.SystemInfo;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CBus;

public class PressureSensorBMP180
{
    static double currentPressuremB;
    public static final int BMP180_ADDRESS = 119;
    public static final int BMP180_ULTRALOWPOWER = 0;
    public static final int BMP180_STANDARD = 1;
    public static final int BMP180_HIGHRES = 2;
    public static final int BMP180_ULTRAHIGHRES = 3;
    public static final int BMP180_CAL_AC1 = 170;
    public static final int BMP180_CAL_AC2 = 172;
    public static final int BMP180_CAL_AC3 = 174;
    public static final int BMP180_CAL_AC4 = 176;
    public static final int BMP180_CAL_AC5 = 178;
    public static final int BMP180_CAL_AC6 = 180;
    public static final int BMP180_CAL_B1 = 182;
    public static final int BMP180_CAL_B2 = 184;
    public static final int BMP180_CAL_MB = 186;
    public static final int BMP180_CAL_MC = 188;
    public static final int BMP180_CAL_MD = 190;
    public static final int BMP180_CONTROL = 244;
    public static final int BMP180_TEMPDATA = 246;
    public static final int BMP180_PRESSUREDATA = 246;
    public static final int BMP180_READTEMPCMD = 46;
    public static final int BMP180_READPRESSURECMD = 52;
    private static int cal_AC1;
    private static int cal_AC2;
    private static int cal_AC3;
    private static int cal_AC4;
    private static int cal_AC5;
    private static int cal_AC6;
    private static int cal_B1;
    private static int cal_B2;
    private static int cal_MB;
    private static int cal_MC;
    private static int cal_MD;
    private static boolean verbose;
    private static I2CBus bus;
    private static I2CDevice bmp180;
    private static int mode;
    static double temperatureC;
    private int standardSeaLevelPressure;
    
    public PressureSensorBMP180() {
        this(119);
    }
    
    public PressureSensorBMP180(final int BMP180_ADDRESS) {
        this.standardSeaLevelPressure = 101325;
    }
    
    public static void init() {
        final int address = 119;
        try {
            PressureSensorBMP180.bus = I2CFactory.getInstance(1);
            if (PressureSensorBMP180.verbose) {
                System.out.println("Connected to bus. OK.");
            }
            PressureSensorBMP180.bmp180 = PressureSensorBMP180.bus.getDevice(address);
            if (PressureSensorBMP180.verbose) {
                System.out.println("Connected to device. OK.");
            }
            try {
                readCalibrationData();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    private int readU8(final int reg) throws Exception {
        int result = 0;
        try {
            result = PressureSensorBMP180.bmp180.read(reg);
            if (PressureSensorBMP180.verbose) {
                System.out.println("I2C: Device 119 returned " + result + " from reg " + reg);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
    
    private int readS8(final int reg) throws Exception {
        int result = 0;
        try {
            result = PressureSensorBMP180.bmp180.read(reg);
            if (result > 127) {
                result -= 256;
            }
            if (PressureSensorBMP180.verbose) {
                System.out.println("I2C: Device 119 returned " + result + " from reg " + reg);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
    
    private static int readU16(final int register) throws Exception {
        final int hi = PressureSensorBMP180.bmp180.read(register);
        return (hi << 8) + PressureSensorBMP180.bmp180.read(register + 1);
    }
    
    private static int readS16(final int register) throws Exception {
        int hi = 0;
        final int lo = 0;
        hi = PressureSensorBMP180.bmp180.read(register);
        if (hi > 127) {
            hi -= 256;
        }
        return hi * 256 + PressureSensorBMP180.bmp180.read(register + 1);
    }
    
    public static void readCalibrationData() throws Exception {
        PressureSensorBMP180.cal_AC1 = readS16(170);
        PressureSensorBMP180.cal_AC2 = readS16(172);
        PressureSensorBMP180.cal_AC3 = readS16(174);
        PressureSensorBMP180.cal_AC4 = readU16(176);
        PressureSensorBMP180.cal_AC5 = readU16(178);
        PressureSensorBMP180.cal_AC6 = readU16(180);
        PressureSensorBMP180.cal_B1 = readS16(182);
        PressureSensorBMP180.cal_B2 = readS16(184);
        PressureSensorBMP180.cal_MB = readS16(186);
        PressureSensorBMP180.cal_MC = readS16(188);
        PressureSensorBMP180.cal_MD = readS16(190);
        if (PressureSensorBMP180.verbose) {
            showCalibrationData();
        }
    }
    
    private static void showCalibrationData() {
        System.out.println("DBG: AC1 = " + PressureSensorBMP180.cal_AC1);
        System.out.println("DBG: AC2 = " + PressureSensorBMP180.cal_AC2);
        System.out.println("DBG: AC3 = " + PressureSensorBMP180.cal_AC3);
        System.out.println("DBG: AC4 = " + PressureSensorBMP180.cal_AC4);
        System.out.println("DBG: AC5 = " + PressureSensorBMP180.cal_AC5);
        System.out.println("DBG: AC6 = " + PressureSensorBMP180.cal_AC6);
        System.out.println("DBG: B1  = " + PressureSensorBMP180.cal_B1);
        System.out.println("DBG: B2  = " + PressureSensorBMP180.cal_B2);
        System.out.println("DBG: MB  = " + PressureSensorBMP180.cal_MB);
        System.out.println("DBG: MC  = " + PressureSensorBMP180.cal_MC);
        System.out.println("DBG: MD  = " + PressureSensorBMP180.cal_MD);
    }
    
    public static int readRawTemp() throws Exception {
        PressureSensorBMP180.bmp180.write(244, (byte)46);
        waitfor(5L);
        final int raw = readU16(246);
        if (PressureSensorBMP180.verbose) {
            System.out.println("DBG: Raw Temp: " + (raw & 0xFFFF) + ", " + raw);
        }
        return raw;
    }
    
    public static double readSealevelPressure(double altitude_m,float bmp180offset,float bmp180slope) {
        double pressure = 0.0;
        try {
            pressure = readPressure();
        }
        catch (Exception ex) {
            Logger.getLogger(PressureSensorBMP180.class.getName()).log(Level.SEVERE, null, ex);
        }
        final double p0 = (pressure / Math.pow(1.0 - altitude_m / 44330.0, 5.255) * bmp180slope) + (bmp180offset*100);
        setCurrentPressuremB(p0);
        return p0 / 100.0;
    }
    
    public static int readRawPressure() throws Exception {
        PressureSensorBMP180.bmp180.write(244, (byte)(52 + (PressureSensorBMP180.mode << 6)));
        if (PressureSensorBMP180.mode == 0) {
            waitfor(5L);
        }
        else if (PressureSensorBMP180.mode == 2) {
            waitfor(14L);
        }
        else if (PressureSensorBMP180.mode == 3) {
            waitfor(26L);
        }
        else {
            waitfor(8L);
        }
        final int msb = PressureSensorBMP180.bmp180.read(246);
        final int lsb = PressureSensorBMP180.bmp180.read(247);
        final int xlsb = PressureSensorBMP180.bmp180.read(248);
        final int raw = (msb << 16) + (lsb << 8) + xlsb >> 8 - PressureSensorBMP180.mode;
        if (PressureSensorBMP180.verbose) {
            System.out.println("DBG: Raw Pressure: " + (raw & 0xFFFF) + ", " + raw);
        }
        return raw;
    }
    
    public static float readTemperature() throws Exception {
        int UT = 0;
        int X1 = 0;
        int X2 = 0;
        int B5 = 0;
        float temp = 0.0f;
        UT = readRawTemp();
        X1 = (UT - PressureSensorBMP180.cal_AC6) * PressureSensorBMP180.cal_AC5 >> 15;
        X2 = (PressureSensorBMP180.cal_MC << 11) / (X1 + PressureSensorBMP180.cal_MD);
        B5 = X1 + X2;
        temp = (B5 + 8 >> 4) / 10.0f;
        if (PressureSensorBMP180.verbose) {
            System.out.println("DBG: Calibrated temperature = " + temp + " C");
        }
        setCurrentTemperatureC(temp);
        return temp;
    }
    
    public static float readPressure() throws Exception {
        int UT = 0;
        int UP = 0;
        int B3 = 0;
        int B4 = 0;
        int B5 = 0;
        int X1 = 0;
        int X2 = 0;
        int X3 = 0;
        int p = 0;
        int B6 = 0;
        int B7 = 0;
        UT = readRawTemp();
        UP = readRawPressure();
        final boolean dsValues = false;
        if (dsValues) {
            UT = 27898;
            UP = 23843;
            PressureSensorBMP180.cal_AC6 = 23153;
            PressureSensorBMP180.cal_AC5 = 32757;
            PressureSensorBMP180.cal_MB = -32768;
            PressureSensorBMP180.cal_MC = -8711;
            PressureSensorBMP180.cal_MD = 2868;
            PressureSensorBMP180.cal_B1 = 6190;
            PressureSensorBMP180.cal_B2 = 4;
            PressureSensorBMP180.cal_AC3 = -14383;
            PressureSensorBMP180.cal_AC2 = -72;
            PressureSensorBMP180.cal_AC1 = 408;
            PressureSensorBMP180.cal_AC4 = 32741;
            PressureSensorBMP180.mode = 0;
            if (PressureSensorBMP180.verbose) {
                showCalibrationData();
            }
        }
        X1 = (UT - PressureSensorBMP180.cal_AC6) * PressureSensorBMP180.cal_AC5 >> 15;
        X2 = (PressureSensorBMP180.cal_MC << 11) / (X1 + PressureSensorBMP180.cal_MD);
        B4 = X1 + X2;
        if (PressureSensorBMP180.verbose) {
            System.out.println("DBG: X1 = " + X1);
            System.out.println("DBG: X2 = " + X2);
            System.out.println("DBG: B5 = " + B4);
            System.out.println("DBG: True Temperature = " + (B4 + 8 >> 4) / 10.0 + " C");
        }
        B5 = B4 - 4000;
        X1 = PressureSensorBMP180.cal_B2 * (B5 * B5) >> 12 >> 11;
        X2 = PressureSensorBMP180.cal_AC2 * B5 >> 11;
        X3 = X1 + X2;
        B3 = ((PressureSensorBMP180.cal_AC1 * 4 + X3 << PressureSensorBMP180.mode) + 2) / 4;
        if (PressureSensorBMP180.verbose) {
            System.out.println("DBG: B6 = " + B5);
            System.out.println("DBG: X1 = " + X1);
            System.out.println("DBG: X2 = " + X2);
            System.out.println("DBG: X3 = " + X3);
            System.out.println("DBG: B3 = " + B3);
        }
        X1 = PressureSensorBMP180.cal_AC3 * B5 >> 13;
        X2 = PressureSensorBMP180.cal_B1 * (B5 * B5 >> 12) >> 16;
        X3 = X1 + X2 + 2 >> 2;
        B6 = PressureSensorBMP180.cal_AC4 * (X3 + 32768) >> 15;
        B7 = (UP - B3) * (50000 >> PressureSensorBMP180.mode);
        if (PressureSensorBMP180.verbose) {
            System.out.println("DBG: X1 = " + X1);
            System.out.println("DBG: X2 = " + X2);
            System.out.println("DBG: X3 = " + X3);
            System.out.println("DBG: B4 = " + B6);
            System.out.println("DBG: B7 = " + B7);
        }
        if (B7 < Integer.MIN_VALUE) {
            p = B7 * 2 / B6;
        }
        else {
            p = B7 / B6 * 2;
        }
        if (PressureSensorBMP180.verbose) {
            System.out.println("DBG: X1 = " + X1);
        }
        X1 = (p >> 8) * (p >> 8);
        X1 = X1 * 3038 >> 16;
        X2 = -7357 * p >> 16;
        if (PressureSensorBMP180.verbose) {
            System.out.println("DBG: p  = " + p);
            System.out.println("DBG: X1 = " + X1);
            System.out.println("DBG: X2 = " + X2);
        }
        p += X1 + X2 + 3791 >> 4;
        if (PressureSensorBMP180.verbose) {
            System.out.println("DBG: Pressure = " + p + " Pa");
        }
        return p;
    }
    
    public void setStandardSeaLevelPressure(int standardSeaLevelPressure) {
        standardSeaLevelPressure = standardSeaLevelPressure;
    }
    
    public double readAltitude() throws Exception {
        double altitude = 0.0;
        final float pressure = readPressure();
        altitude = 44330.0 * (1.0 - Math.pow(pressure / this.standardSeaLevelPressure, 0.1903));
        if (PressureSensorBMP180.verbose) {
            System.out.println("DBG: Altitude = " + altitude);
        }
        return altitude;
    }
    
    protected static void waitfor(final long howMuch) {
        try {
            Thread.sleep(howMuch);
        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
    
    public static void main(final String[] args) {
        final NumberFormat NF = new DecimalFormat("##00.00");
        final PressureSensorBMP180 sensor = new PressureSensorBMP180();
        float press = 0.0f;
        float temp = 0.0f;
        double alt = 0.0;
        double slp = 0.0;
        try {
            press = readPressure();
        }
        catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
        try {
            slp = readSealevelPressure(184.4,(float)-230,(float)1.0);
        }
        catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
        sensor.setStandardSeaLevelPressure((int)press);
        try {
            alt = sensor.readAltitude();
        }
        catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
        try {
            temp = readTemperature();
        }
        catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("Temperature: " + NF.format(temp) + " C");
        System.out.println("Pressure   : " + NF.format(press / 100.0f) + " hPa");
        System.out.println("Altitude   : " + NF.format(alt) + " m");
        System.out.println("Sea Level Pressure: " + NF.format(slp) + " hPa");
        try {
            System.out.println("CPU Temperature   :  " + SystemInfo.getCpuTemperature());
            System.out.println("CPU Core Voltage  :  " + SystemInfo.getCpuVoltage());
        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void readFile() {
        try {
            final BufferedReader br = new BufferedReader(new FileReader("bmp.txt"));
            String lineOfText = null;
            while ((lineOfText = br.readLine()) != null) {
                lineOfText.trim();
                PressureSensorBMP180.currentPressuremB = Double.parseDouble(lineOfText.split(" ")[0]);
                PressureSensorBMP180.temperatureC = Double.parseDouble(lineOfText.split(" ")[1]);
            }
        }
        catch (FileNotFoundException fnf) {
            System.out.println("Could not find the file bmp.txt " + fnf);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static double getCurrentPressuremB() {
        return PressureSensorBMP180.currentPressuremB;
    }
    
    public static double getCurrentTemperatureC() {
        return PressureSensorBMP180.temperatureC;
    }
    
    public static void setCurrentPressuremB(final double press) {
        PressureSensorBMP180.currentPressuremB = press / 100.0;
    }
    
    public static void setCurrentTemperatureC(final float temp) {
        PressureSensorBMP180.temperatureC = temp;
    }
    
    static {
        PressureSensorBMP180.currentPressuremB = 0.0;
        PressureSensorBMP180.cal_AC1 = 0;
        PressureSensorBMP180.cal_AC2 = 0;
        PressureSensorBMP180.cal_AC3 = 0;
        PressureSensorBMP180.cal_AC4 = 0;
        PressureSensorBMP180.cal_AC5 = 0;
        PressureSensorBMP180.cal_AC6 = 0;
        PressureSensorBMP180.cal_B1 = 0;
        PressureSensorBMP180.cal_B2 = 0;
        PressureSensorBMP180.cal_MB = 0;
        PressureSensorBMP180.cal_MC = 0;
        PressureSensorBMP180.cal_MD = 0;
        PressureSensorBMP180.verbose = false;
        PressureSensorBMP180.mode = 3;
        PressureSensorBMP180.temperatureC = 0.0;
    }
}
