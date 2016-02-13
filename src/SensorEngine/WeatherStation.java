// 
// Decompiled by Procyon v0.5.30
// 
package SensorEngine;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.security.Security;
import java.util.Enumeration;
import java.util.StringTokenizer;
import Workers.Utilities;
import java.util.logging.Level;
import java.util.logging.Logger;
import Workers.RainProcessor;
import Workers.gammaProcessor;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.OneWireAccessProvider;
import Workers.Dispatcher;
import Workers.WindSpeedProcessor;
import java.util.Hashtable;
import java.util.Properties;
import java.util.ArrayList;
import com.dalsemi.onewire.adapter.DSPortAdapter;

public class WeatherStation {

    private DSPortAdapter adapter;
    protected ArrayList windDirectionSensors;
    protected ArrayList humiditySensors;
    protected ArrayList counterSensors;
    protected ArrayList solarSensors;
    protected ArrayList pressureSensors;
    protected ArrayList temperatureSensors;
    protected ArrayList batteryMonitors;
    public static Properties props;
    protected static SocketServer socketServerData;
    protected int socketDataPort;
    protected static int oneWireWaitPeriod;
    protected String portAdapter;
    protected String portName;
    protected Hub hub;
    protected Hashtable hubDeviceMap;
    public int priority;
    int tempCounter;
    int humCounter;
    int battCounter;
    int counterCounter;
    int loop;
    static int errorcount;
    static long startupTime;
    static int numberofDevices;
    public static boolean sendMessageNow;
    public static boolean useBMP180;
    public static double averageDirection;
    public static String upTime;
    WindSpeedProcessor wsp;
    private static Dispatcher ds;
    public static double solarTemp;
    public static double barometerTemp;
    public static double humidityTemp;
    public static float bmp180offset;
    public static float bmp180slope;

    public WeatherStation(final Properties props) throws OneWireException {
        this.priority = 1;
        this.tempCounter = 0;
        this.humCounter = 0;
        this.battCounter = 0;
        this.counterCounter = 0;
        this.loop = 0;
        this.wsp = new WindSpeedProcessor();
        this.humiditySensors = new ArrayList();
        this.counterSensors = new ArrayList();
        this.solarSensors = new ArrayList();
        this.pressureSensors = new ArrayList();
        this.temperatureSensors = new ArrayList();
        this.windDirectionSensors = new ArrayList();
        this.batteryMonitors = new ArrayList();
        this.socketDataPort = 0;
        this.hub = null;
        this.hubDeviceMap = null;
        this.initialize(props);
        (WeatherStation.socketServerData = new SocketServer(this.socketDataPort)).start();
        System.out.println("Opening data socket server on " + this.socketDataPort);
        this.adapter = OneWireAccessProvider.getAdapter(this.portAdapter, this.portName);
        if (this.portAdapter == null || this.portName == null) {
            System.out.println("Missing portAdapter or portName in properties file.  Trying to use the default.");
        }
        if (this.adapter != null) {
            System.out.println("Found Adapter: " + this.adapter.getAdapterName());
            this.resetBus();
            this.adapter.setSpeed(1);
            (this.hub = new Hub(this.adapter, WeatherStation.socketServerData)).closeAllSwitches();
            this.hub.queryHub();
            this.hubDeviceMap = this.hub.getDeviceMap();
            return;
        }
        System.out.println("Error: Unable to find adapter!");
        throw new OneWireException("1-Wire Adapter Not Found");
    }

    public void fireDevices(final boolean sendMessageNow) throws Exception {
        try {
            int count = 0;
            WeatherStation.useBMP180 = WeatherStation.props.getProperty("useBMP180").equalsIgnoreCase("true");
            if (counterSensors.size() > 0) {
                while (count < counterSensors.size()) {
                    try {
                        CounterSensor counterSensor = (CounterSensor) counterSensors.get(count);
                        String message = counterSensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + counterSensor.readSensor() + "\r\n";
                        WeatherStation.socketServerData.sendMessage(message + "\r\n");
                        if (message == null) {
                            System.out.println("Counter read was null");
                        } else {
                            WeatherStation.ds.updateSensorReadings(message);
                        }
                        if (WeatherStation.props.getProperty(message.split(":")[0] + ".type").equalsIgnoreCase("windspeed")) {
                            final long windcount = Long.parseLong(counterSensor.readSensor());
                            this.wsp.processWind(windcount, sendMessageNow);
                        }
                        if (WeatherStation.props.getProperty(message.split(":")[0] + ".type").equalsIgnoreCase("geigercounter")) {
                            final long gammaCount = Long.parseLong(counterSensor.readSensor());
                            gammaProcessor.process(message, gammaCount);
                        }
                        message = null;
                        ++this.loop;
                        System.out.print("Sensor Read number " + this.loop + "\r");
                    } catch (OneWireException ex) {
                        System.err.println("Caught 1-wire exception " + ex);
                        ex.printStackTrace();
                    }
                    ++count;
                }
                WeatherStation.socketServerData.sendMessage("dailyRain:" + RainProcessor.getRainSinceMidnight() + "\r\n");
                WeatherStation.socketServerData.sendMessage("currentWind:" + WindSpeedProcessor.getCurrentWindSpeed() + "\r\n");
            }
            for (count = 0; count < this.windDirectionSensors.size(); ++count) {
                try {
                    final WindDirectionSensor windDirectionSensor = (WindDirectionSensor) this.windDirectionSensors.get(count);
                    windDirectionSensor.readSensor();
                    WeatherStation.socketServerData.sendMessage(windDirectionSensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + windDirectionSensor.windDirection + "\r\n");
                    String message = windDirectionSensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + windDirectionSensor.windDirection + "\r\n";
                    WeatherStation.ds.updateSensorReadings(message);
                    message = null;
                    WeatherStation.socketServerData.sendMessage("windDirection:" + windDirectionSensor.getAverageDirection() + "\r\n");
                    if (sendMessageNow) {
                        WeatherStation.averageDirection = windDirectionSensor.getAverageDirection();
                        windDirectionSensor.clearDirectionBins();
                    }
                    ++this.loop;
                    System.out.print("Sensor Read number " + this.loop + "\r");
                } catch (OneWireException ex) {
                    System.err.println("Caught 1-wire exception " + ex);
                    ex.printStackTrace();
                }
            }
            final TemperatureSensor temperatureSensor = (TemperatureSensor) this.temperatureSensors.get(this.tempCounter);
            final String temperature = temperatureSensor.readSensor();
            if (temperature.equals("-999")) {
                System.out.println("Temperature sensor " + temperatureSensor.getDeviceAddress() + " had errored reading. Skipping!");
            } else {
                WeatherStation.socketServerData.sendMessage(temperatureSensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + temperatureSensor.getCelsius() + "\r\n");
                String message = temperatureSensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + temperatureSensor.getCelsius() + "\r\n";
                WeatherStation.ds.updateSensorReadings(message);
                message = null;
                ++this.loop;
                System.out.print("Sensor Read number " + this.loop + "\r");
            }
            ++this.tempCounter;
            if (this.tempCounter >= this.temperatureSensors.size()) {
                this.tempCounter = 0;
            }
            if (this.priority == 3) {
                if (this.humiditySensors.size() > 0) {
                    final HumiditySensor humiditySensor = (HumiditySensor) this.humiditySensors.get(this.humCounter);
                    final String value = humiditySensor.readSensor();
                    final double temp = humiditySensor.getHumidityTemp();
                    if (value.equals("-999")) {
                        System.out.println("Humidity sensor " + humiditySensor.getDeviceAddress() + " had errored reading. Skipping");
                    } else {
                        WeatherStation.socketServerData.sendMessage(humiditySensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + value + "\r\n");
                        String message = humiditySensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + value + "\r\n";
                        WeatherStation.ds.updateSensorReadings(message);
                        message = null;
                        WeatherStation.socketServerData.sendMessage(humiditySensor.getDeviceAddress() + "_temp" + ":" + System.currentTimeMillis() + ":" + temp + "\r\n");
                        message = humiditySensor.getDeviceAddress() + "_temp" + ":" + System.currentTimeMillis() + ":" + temp + "\r\n";
                        WeatherStation.ds.updateSensorReadings(message);
                        message = null;
                        ++this.loop;
                        System.out.print("Sensor Read number " + this.loop + "\r");
                    }
                    ++this.humCounter;
                    if (this.humCounter >= this.humiditySensors.size()) {
                        this.humCounter = 0;
                    }
                }
                if (this.batteryMonitors.size() > 0) {
                    final BatteryMonitor batteryMonitor = (BatteryMonitor) this.batteryMonitors.get(this.battCounter);
                    final String value = batteryMonitor.readSensor();
                    final double temp = BatteryMonitor.getBatteryTemp();
                    if (value.equals("-999")) {
                        System.out.println("Battery monitor " + batteryMonitor.getDeviceAddress() + " had errored reading. Skipping");
                    } else {
                        WeatherStation.socketServerData.sendMessage(batteryMonitor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + value + "\r\n");
                        WeatherStation.socketServerData.sendMessage(batteryMonitor.getDeviceAddress() + "_temp" + ":" + System.currentTimeMillis() + ":" + temp + "\r\n");
                        ++this.loop;
                        System.out.print("Sensor Read number " + this.loop + "\r");
                    }
                    ++this.battCounter;
                    if (this.battCounter >= this.batteryMonitors.size()) {
                        this.battCounter = 0;
                    }
                }
            }
            count = 0;
            if (this.priority == 4 && this.solarSensors.size() > 0) {
                while (count < this.solarSensors.size()) {
                    final SolarSensor solarSensor = (SolarSensor) this.solarSensors.get(count);
                    WeatherStation.socketServerData.sendMessage(solarSensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + solarSensor.readSensor() + "\r\n");
                    String message = solarSensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + solarSensor.readSensor() + "\r\n";
                    WeatherStation.ds.updateSensorReadings(message);
                    message = null;
                    WeatherStation.socketServerData.sendMessage(solarSensor.getDeviceAddress() + "_temp" + ":" + System.currentTimeMillis() + ":" + SolarSensor.getSolarTemp() + "\r\n");
                    message = solarSensor.getDeviceAddress() + "_temp" + ":" + System.currentTimeMillis() + ":" + SolarSensor.getSolarTemp() + "\r\n";
                    WeatherStation.ds.updateSensorReadings(message);
                    message = null;
                    ++this.loop;
                    System.out.print("Sensor Read number " + this.loop + "\r");
                    ++count;
                }
            }
            if (this.priority == 5) {
                if (WeatherStation.useBMP180) {
                    final String pressuremB = Double.toString(PressureSensorBMP180.readSealevelPressure(184.4, bmp180offset, bmp180slope));
                    WeatherStation.socketServerData.sendMessage("BMP180:" + System.currentTimeMillis() + ":" + pressuremB + "\r\n");
                    String message = "BMP180:" + System.currentTimeMillis() + ":" + pressuremB + "\r\n";
                    WeatherStation.ds.updateSensorReadings(message);
                    message = null;
                    final String temp2 = Double.toString(PressureSensorBMP180.readTemperature());
                    WeatherStation.socketServerData.sendMessage("BMP180_temp:" + System.currentTimeMillis() + ":" + temp2 + "\r\n");
                    message = "BMP180_temp:" + System.currentTimeMillis() + ":" + temp2 + "\r\n";
                    WeatherStation.ds.updateSensorReadings(message);
                    message = null;
                    ++this.loop;
                    System.out.print(this.loop + "\r");
                } else if (this.pressureSensors.size() > 0) {
                    while (count < this.pressureSensors.size()) {
                        final PressureSensor pressureSensor = (PressureSensor) this.pressureSensors.get(count);
                        final String pressuremB2 = pressureSensor.readSensor();
                        if (pressuremB2.equals("-999")) {
                            System.out.println("Barometer had errored reading. Skipping!");
                        } else if (WeatherStation.props.getProperty(pressureSensor.getDeviceAddress() + ".type").contains("jennings")) {
                            WeatherStation.socketServerData.sendMessage(pressureSensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + pressuremB2 + "\r\n");
                            String message = pressureSensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + pressuremB2 + "\r\n";
                            WeatherStation.ds.updateSensorReadings(message);
                            message = null;
                            WeatherStation.socketServerData.sendMessage(pressureSensor.getDeviceAddress() + "_temp" + ":" + System.currentTimeMillis() + ":" + PressureSensorJennings.getBaroTemp() + "\r\n");
                            message = pressureSensor.getDeviceAddress() + "_temp" + ":" + System.currentTimeMillis() + ":" + PressureSensorJennings.getBaroTemp() + "\r\n";
                            WeatherStation.ds.updateSensorReadings(message);
                            message = null;
                        } else {
                            try {
                                WeatherStation.socketServerData.sendMessage(pressureSensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + pressureSensor.getPressure() + "\r\n");
                                String message = pressureSensor.getDeviceAddress() + ":" + System.currentTimeMillis() + ":" + pressureSensor.getPressure() + "\r\n";
                                WeatherStation.ds.updateSensorReadings(message);
                                message = null;
                                WeatherStation.socketServerData.sendMessage(pressureSensor.getDeviceAddress() + "_temp" + ":" + System.currentTimeMillis() + ":" + PressureSensorTAI8570.getTemp_C_S() + "\r\n");
                                message = pressureSensor.getDeviceAddress() + "_temp" + ":" + System.currentTimeMillis() + ":" + PressureSensorTAI8570.getTemp_C_S() + "\r\n";
                                WeatherStation.ds.updateSensorReadings(message);
                                message = null;
                            } catch (Exception ex2) {
                                System.err.println("Caught 1-wire exception " + ex2);
                                ex2.printStackTrace();
                            }
                        }
                        ++this.loop;
                        System.out.print(this.loop + "\r");
                        ++count;
                    }
                }
            }
            if (sendMessageNow) {
            }
            if (this.priority >= 5) {
                this.priority = 1;
            }
            ++this.priority;
            count = 0;
        } catch (OneWireException ex3) {
            Logger.getLogger(WeatherStation.class.getName()).log(Level.SEVERE, null, (Throwable) ex3);
        }
    }

    public static String getUpTime() {
        double runTime = 0.0;
        runTime = System.currentTimeMillis() - WeatherStation.startupTime;
        final int days = (int) (runTime / 8.64E7);
        final int hours = (int) ((runTime / 8.64E7 - days) * 8.64E7 / 3600000.0);
        final int minutes = (int) (60.0 * ((runTime / 8.64E7 - days) * 8.64E7 / 3600000.0 - hours));
        return WeatherStation.upTime = days + " Days, " + hours + " Hours, " + minutes + " Minutes";
    }

    public static double getAverageDirection() {
        return Utilities.roundValue2(WeatherStation.averageDirection);
    }

    public void loadDevices() throws OneWireException, ConfigurationException {
        final Enumeration keys = WeatherStation.props.keys();
        HubChannelMap hubChannel = null;
        System.out.println("Loading Devices");
        try {
            while (keys.hasMoreElements()) {
                hubChannel = null;
                final String nextKey = (String) keys.nextElement();
                String value = WeatherStation.props.getProperty(nextKey).trim();
                final StringTokenizer tokenizer = new StringTokenizer(value, ",");
                if (nextKey.startsWith("sensor.t")) {
                    hubChannel = this.getHubChannel(value);
                    final Sensor sensor = new TemperatureSensor(this.adapter, value, nextKey, WeatherStation.socketServerData, hubChannel);
                    this.temperatureSensors.add(sensor);
                    System.out.println("Adding sensor " + value.substring(0, 16) + " That Measures " + WeatherStation.props.getProperty(value.substring(0, 16) + ".description"));
                    ++WeatherStation.numberofDevices;
                } else if (nextKey.startsWith("sensor.h")) {
                    if (tokenizer.countTokens() != 3) {
                        throw new ConfigurationException("The humidity sensor property file entry for " + nextKey + " does not contain the address,slope, or intercept values" + '\r' + '\n');
                    }
                    final String device = tokenizer.nextToken();
                    final String slope = tokenizer.nextToken();
                    final String intercept = tokenizer.nextToken();
                    hubChannel = this.getHubChannel(device);
                    final Sensor sensor2 = new HumiditySensor(this.adapter, device, new Double(slope), new Double(intercept), nextKey, WeatherStation.socketServerData, hubChannel);
                    this.humiditySensors.add(sensor2);
                    System.out.println("Adding sensor " + value.substring(0, 16) + " That Measures " + WeatherStation.props.getProperty(value.substring(0, 16) + ".description"));
                    ++WeatherStation.numberofDevices;
                    ++WeatherStation.numberofDevices;
                } else if (nextKey.startsWith("sensor.p")) {
                    Sensor sensor;
                    if (tokenizer.countTokens() == 2) {
                        final String device2 = tokenizer.nextToken();
                        final String intercept = tokenizer.nextToken();
                        hubChannel = this.getHubChannel(device2);
                        sensor = new PressureSensorJennings(this.adapter, device2, new Double(intercept), nextKey, WeatherStation.socketServerData, hubChannel);
                        System.out.println("Adding sensor " + value.substring(0, 16) + " That is a " + WeatherStation.props.getProperty(value.substring(0, 16) + ".description"));
                        ++WeatherStation.numberofDevices;
                        ++WeatherStation.numberofDevices;
                    } else {
                        final String address1 = tokenizer.nextToken();
                        final String address2 = tokenizer.nextToken();
                        final String intercept2 = tokenizer.nextToken();
                        hubChannel = this.getHubChannel(address1);
                        sensor = new PressureSensorTAI8570(this.adapter, address1, address2, new Double(intercept2), nextKey, WeatherStation.socketServerData, hubChannel);
                        System.out.println("Adding sensor " + value.substring(0, 16) + " That is a " + WeatherStation.props.getProperty(value.substring(0, 16) + ".description"));
                        ++WeatherStation.numberofDevices;
                        ++WeatherStation.numberofDevices;
                    }
                    this.pressureSensors.add(sensor);
                } else if (nextKey.startsWith("sensor.c")) {
                    hubChannel = this.getHubChannel(value);
                    final Sensor sensor = new CounterSensor(this.adapter, value, nextKey, WeatherStation.socketServerData, hubChannel);
                    this.counterSensors.add(sensor);
                    System.out.println("Adding sensor " + value.substring(0, 16) + " That is for " + WeatherStation.props.getProperty(value.substring(0, 16) + ".description"));
                    ++WeatherStation.numberofDevices;
                } else if (nextKey.startsWith("sensor.b")) {
                    hubChannel = this.getHubChannel(value);
                    final Sensor sensor = new BatteryMonitor(this.adapter, value, nextKey, WeatherStation.socketServerData, hubChannel);
                    this.batteryMonitors.add(sensor);
                    System.out.println("Adding sensor " + value.substring(0, 16) + " That is for " + WeatherStation.props.getProperty(value.substring(0, 16) + ".description"));
                } else if (nextKey.startsWith("sensor.s")) {
                    if (tokenizer.countTokens() != 2) {
                        throw new ConfigurationException("The solar sensor property file entry for " + nextKey + " does not contain the address or sense resistor values" + '\r' + '\n');
                    }
                    final String device = tokenizer.nextToken();
                    final String senseResistor = tokenizer.nextToken();
                    hubChannel = this.getHubChannel(device);
                    final Sensor sensor3 = new SolarSensor(this.adapter, device, new Double(senseResistor), nextKey, WeatherStation.socketServerData, hubChannel);
                    this.solarSensors.add(sensor3);
                    System.out.println("Adding sensor " + value.substring(0, 16) + " That Measures " + WeatherStation.props.getProperty(value.substring(0, 16) + ".description"));
                    ++WeatherStation.numberofDevices;
                    ++WeatherStation.numberofDevices;
                } else {
                    if (!nextKey.trim().startsWith("sensor.v")) {
                        continue;
                    }
                    Sensor sensor;
                    if (tokenizer.countTokens() == 10) {
                        final ArrayList vanes = new ArrayList();
                        final String device3 = tokenizer.nextToken();
                        for (int i = 0; i < 8; ++i) {
                            vanes.add(tokenizer.nextToken());
                        }
                        final String offsetToken = tokenizer.nextToken();
                        final int offset = new Integer(offsetToken);
                        hubChannel = this.getHubChannel(device3);
                        sensor = new WindDirectionSensorV2(this.adapter, device3, nextKey, vanes, offset, WeatherStation.socketServerData, hubChannel);
                        System.out.println("Adding sensor " + value.substring(0, 16) + " That Measures " + WeatherStation.props.getProperty(value.substring(0, 16) + ".description"));
                    } else {
                        value = tokenizer.nextToken();
                        final String north_Offset = tokenizer.nextToken();
                        final int northOffset = new Integer(north_Offset);
                        hubChannel = this.getHubChannel(value);
                        sensor = new WindDirectionSensorV3(this.adapter, value, nextKey, northOffset, WeatherStation.socketServerData, hubChannel);
                    }
                    this.windDirectionSensors.add(sensor);
                    System.out.println("Adding sensor " + value.substring(0, 16) + " That Measures " + WeatherStation.props.getProperty(value.substring(0, 16) + ".description"));
                    ++WeatherStation.numberofDevices;
                }
            }
        } catch (Exception e) {
            System.out.println("Error while loading sensors.\r\n Check that propsfile is correct.");
            e.printStackTrace();
        }
    }

    protected HubChannelMap getHubChannel(final String deviceAddress) {
        HubChannelMap hubChannel = null;
        if (this.hubDeviceMap != null && this.hubDeviceMap.containsKey(deviceAddress)) {
            hubChannel = (HubChannelMap) this.hubDeviceMap.get(deviceAddress);
        }
        return hubChannel;
    }

    public void resetBus() {
        try {
            final int result = this.adapter.reset();
            if (result == 0) {
                System.out.println("Warning: Reset indicates no Device Present\r\n");
            }
            if (result == 3) {
                System.out.println("Warning: Reset indicates 1-Wire bus is shorted\r\n");
            }
        } catch (OneWireException e) {
            System.out.println("Exception Resetting the bus: " + e + '\r' + '\n');
        }
    }

    public boolean initialize(final Properties props) {
        final boolean status = true;
        this.socketDataPort = Integer.parseInt(props.getProperty("dataport"));
        this.portAdapter = props.getProperty("portAdapter");
        this.portName = props.getProperty("portName");
        WeatherStation.oneWireWaitPeriod = Integer.parseInt(props.getProperty("onewirewaitperiod"));
        return status;
    }

    public static void main(final String[] args) throws FileNotFoundException {
        Security.setProperty("networkaddress.cache.ttl", "0");
        Security.setProperty("networkaddress.negative.cache.ttl", "0");
        long lastMessageTime = System.currentTimeMillis();
        WeatherStation.sendMessageNow = false;
        try {
            String propsFile;
            if (args.length == 0) {
                propsFile = "prefs.props";
            } else {
                propsFile = args[0];
            }
            (WeatherStation.props = new Properties()).load(new FileInputStream(propsFile));
            final WeatherStation ws = new WeatherStation(WeatherStation.props);
            WeatherStation.useBMP180 = WeatherStation.props.getProperty("useBMP180").equalsIgnoreCase("true");
            ws.loadDevices();
            if (WeatherStation.useBMP180) {
                bmp180offset = Float.parseFloat(props.getProperty("bmp180offset"));
                bmp180slope = Float.parseFloat(props.getProperty("bmp180slope"));
                WeatherStation.numberofDevices += 2;
                System.out.println("Adding two to device count for BMP180 Barometer");
                PressureSensorBMP180.init();
            }
            System.out.println("Number of Devices counted = " + WeatherStation.numberofDevices);
            try {
                WeatherStation.ds = new Dispatcher(WeatherStation.props, WeatherStation.startupTime, WeatherStation.numberofDevices);
                new Thread(WeatherStation.ds).start();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            while (true) {
                ws.fireDevices(WeatherStation.sendMessageNow);
                WeatherStation.sendMessageNow = false;
                if (System.currentTimeMillis() - lastMessageTime > 120000L) {
                    WeatherStation.sendMessageNow = true;
                    lastMessageTime = System.currentTimeMillis();
                    if (!WeatherStation.ds.isAlive()) {
                        continue;
                    }
                    if (WeatherStation.socketServerData.isAlive()) {
                        continue;
                    }
                    System.out.println("SocketServer thread has died");
                    System.exit(1);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("The Properties file wasn't found.");
            System.out.println("Check that the file:'prefs.props' is in the JavaOWW directory, ");
            System.out.println("or specify the name of your properties file as a command line argument if different.");
            System.out.println("System will now exit");
        } catch (Exception e) {
            System.out.println();
            System.out.println("^%$^^%$%!  Got an exception " + e.toString() + '\r' + '\n');
            e.printStackTrace();
            WeatherStation.socketServerData = null;
            System.out.println("Fatal Exception " + e);
            System.out.println("Weather Server is exiting\r\n");
            System.exit(1);
        }
    }

    static {
        WeatherStation.socketServerData = null;
        WeatherStation.errorcount = 0;
        WeatherStation.startupTime = System.currentTimeMillis();
        WeatherStation.numberofDevices = 0;
        WeatherStation.sendMessageNow = false;
        WeatherStation.useBMP180 = false;
        WeatherStation.averageDirection = 0.0;
        WeatherStation.upTime = "";
    }
}
