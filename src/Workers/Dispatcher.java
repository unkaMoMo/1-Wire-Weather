// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import SensorEngine.PressureSensorBMP180;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.net.Socket;

public class Dispatcher extends Thread
{
    protected int dataport;
    protected Socket socket;
    protected static final long CWOP_TEMPERATURE_AVERAGE_INTERVAL = 300000L;
    protected static final long CWOP_HUMIDITY_AVERAGE_INTERVAL = 60000L;
    protected static final long BAROTREND_UPDATE_INTERVAL = 300000L;
    String[] sensorReadings;
    boolean genXML;
    boolean log;
    long webUploadInterval;
    long cwopUploadInterval;
    long wunderUploadInterval;
    long w4uUploadInterval;
    long wowUploadInterval;
    protected static final long LOG_INTERVAL = 900000L;
    protected String weatherServerHost;
    private Properties props;
    private int numberofDevices;
    Calendar cal;
    int oldDay;
    private long saveInterval;
    private long saveintervalStartTime;
    public BaroTrend bt;
    public RainProcessor rp;
    public static long rainCount;
    private int newHour;
    private int oldHour;
    private boolean hoursUp;
    public LightningProcessor lp;
    public static float cwopTemp;
    public static float cwopHum;
    private float cwopTemp_;
    private float cwopHum_;
    private int cwopListCounter;
    public CopyOnWriteArrayList minMaxList;
    public ArrayList cwopServerList;
    private ArrayList cwopTempList;
    private ArrayList cwopHumList;
    private ArrayList barometerList;
    private boolean useBMP180;
    private boolean genHTML;
    private boolean generateSecondaryPage;
    private long newRainCount;
    private boolean webPost;
    private boolean cwopPost;
    private boolean w4uPost;
    private boolean wunderPost;
    private boolean wowPost;
    private boolean baroTrendUpdateRecords;
    private int j;
    private boolean cwopUpload;
    private long cwopStartupTime;
    private long webStartupTime;
    private long w4uStartupTime;
    private long wowStartupTime;
    private long wunderStartupTime;
    private long baroTrendStartupTime;
    private long logStartupTime;
    private boolean cwopAutoRotate;
    private float temp;
    private float hum;
    long oldRainCount;
    private int array;
    private boolean arrayFilled;
    private boolean cwopuploadfirstrun;
    private static double barometer;
    float bmp180slope;
    float bmp180offset;
    public Dispatcher(final Properties props, final long startupTime, final int numberofDevices) {
        this.socket = null;
        this.genXML = false;
        this.log = false;
        this.weatherServerHost = "localhost";
        this.cal = Calendar.getInstance();
        this.oldDay = this.cal.get(6);
        this.saveInterval = 120000L;
        this.saveintervalStartTime = System.currentTimeMillis();
        this.oldHour = this.cal.get(11);
        this.lp = new LightningProcessor();
        this.cwopListCounter = 0;
        this.minMaxList = new CopyOnWriteArrayList();
        this.cwopServerList = new ArrayList();
        this.cwopTempList = new ArrayList();
        this.cwopHumList = new ArrayList();
        this.barometerList = new ArrayList();
        this.useBMP180 = false;
        this.webPost = false;
        this.cwopPost = false;
        this.w4uPost = false;
        this.wunderPost = false;
        this.wowPost = false;
        this.baroTrendUpdateRecords = false;
        this.cwopUpload = false;
        this.cwopStartupTime = System.currentTimeMillis();
        this.webStartupTime = System.currentTimeMillis();
        this.w4uStartupTime = System.currentTimeMillis();
        this.wowStartupTime = System.currentTimeMillis();
        this.wunderStartupTime = System.currentTimeMillis();
        this.baroTrendStartupTime = System.currentTimeMillis();
        this.logStartupTime = System.currentTimeMillis();
        this.oldRainCount = 0L;
        this.array = 0;
        this.arrayFilled = false;
        this.cwopuploadfirstrun = true;
        this.props = props;
        bmp180slope= Float.parseFloat(props.getProperty("bmp180slope"));
        bmp180offset = Float.parseFloat(props.getProperty("bmp180offset"));
        this.dataport = Integer.parseInt(props.getProperty("dataport"));
        this.genXML = props.getProperty("generateXML").equalsIgnoreCase("true");
        this.cwopUploadInterval = Long.parseLong(props.getProperty("cwopUploadInterval"));
        this.wunderUploadInterval = Long.parseLong(props.getProperty("wunderUploadInterval"));
        this.webUploadInterval = Long.parseLong(props.getProperty("webUploadInterval"));
        this.w4uUploadInterval = Long.parseLong(props.getProperty("w4uUploadInterval"));
        this.wowUploadInterval = Long.parseLong(props.getProperty("wowUploadInterval"));
        this.sensorReadings = new String[numberofDevices];
        this.cwopUpload = props.getProperty("cwopUpload").equalsIgnoreCase("true");
        this.cwopAutoRotate = props.getProperty("cwopAutoRotate").equalsIgnoreCase("true");
        this.weatherServerHost = "localhost";
        this.numberofDevices = numberofDevices;
        final int i = Integer.parseInt(props.getProperty("numCWOPServers"));
        this.useBMP180 = props.getProperty("useBMP180").equalsIgnoreCase("true");
        this.useBMP180 = this.useBMP180;
        this.j = 0;
        final File file = new File("minMaxList.dat");
        if (file.exists()) {
            this.minMaxList = Utilities.restoreCopyOnWriteArrayListObject("minMaxList.dat");
        }
    }
    
    @Override
    public void run() {
        System.out.println(" Thread: Dispatcher started");
        try {
            this.rp = new RainProcessor();
            this.bt = new BaroTrend();
            if (this.props.getProperty("enableRapidFire").equalsIgnoreCase("true")) {
                final RapidFire rf = new RapidFire(this.props);
                new Thread(rf).start();
                System.out.println("Starting rapidfire thread");
                this.wunderPost = false;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.rp.createArrayList();
        this.rp.populateArrayList();
        RainProcessor.readRainData();
        this.lp.readLightningData();
        this.j = 0;
        while (this.j < this.numberofDevices) {
            this.sensorReadings[this.j] = "aaaaaaaaaaaaaaaa:bbbbbbbbbbbbb:345672";
            ++this.j;
        }
        System.out.println("SensorReadings array has " + this.numberofDevices + " elements");
    }
    
    public void updateSensorReadings(final String message) {
        try {
            if (message.length() > 0) {
                this.j = 0;
                while (this.j < this.sensorReadings.length) {
                    if (this.sensorReadings[this.j] != null && message.split(":")[0].trim().equals(this.sensorReadings[this.j].split(":")[0].trim())) {
                        this.sensorReadings[this.j] = message;
                    }
                    ++this.j;
                }
                if ((message.split(":")[0].trim().length() == 16 || this.useBMP180) && !message.split(":")[0].contains("_temp")) {
                    if (this.props.getProperty(message.split(":")[0].trim() + ".type").equalsIgnoreCase("humidity") && this.props.getProperty(message.split(":")[0].trim() + ".publish").trim().contains("cwop")) {
                        this.cwopHum_ = Float.parseFloat(message.split(":")[2].trim());
                        this.cwopHumList.add(this.cwopHum_ + "," + System.currentTimeMillis());
                        this.processCwopHum(this.cwopHumList);
                        System.out.println("Processing CWOP Hum...");
                    }
                    else if (this.props.getProperty(message.split(":")[0].trim() + ".type").equalsIgnoreCase("temperature") && this.props.getProperty(message.split(":")[0].trim() + ".publish").trim().contains("cwop")) {
                        this.cwopTemp_ = Float.parseFloat(message.split(":")[2]);
                        this.cwopTempList.add(this.cwopTemp_ + "," + System.currentTimeMillis());
                        this.processCwopTemp(this.cwopTempList);
                        System.out.println("Processing CWOP Temperature...");
                    }
                    else if (this.props.getProperty(message.split(":")[0].trim() + ".description").trim().contains("Barometer") && this.baroTrendUpdateRecords) {
                        System.out.println("Processing Barotrend");
                        this.bt.updateRecords(message);
                        this.baroTrendUpdateRecords = false;
                    }
                    else if (this.props.getProperty(message.split(":")[0].trim() + ".description").trim().contains("Barometer")) {
                        final double baro_ = Double.parseDouble(message.split(":")[2]);
                        this.barometerList.add(baro_ + "," + System.currentTimeMillis());
                        System.out.println("Adding record to barometerList " + baro_);
                        this.processBarometer(this.barometerList);
                    }
                }
            }
        }
        catch (Exception ws) {
            System.out.println("Well shitfar and dadburnnit " + ws);
            ws.printStackTrace();
        }
        try {
            if (!this.arrayFilled) {
                boolean found = false;
                this.j = 0;
                while (this.j < this.sensorReadings.length) {
                    if (message.split(":")[0].equals(this.sensorReadings[this.j].split(":")[0])) {
                        found = true;
                        break;
                    }
                    ++this.j;
                }
                if (!found) {
                    this.sensorReadings[this.array] = message;
                    System.out.println("Adding sensor reading element @ " + this.array + " which is " + this.props.getProperty(message.split(":")[0].trim() + ".description"));
                    ++this.array;
                    if (this.array == this.sensorReadings.length) {
                        this.arrayFilled = true;
                    }
                }
            }
        }
        catch (Exception ex) {
            System.err.println("Had a boo boo " + ex);
        }
        this.dispatch(this.arrayFilled, this.minMaxList);
    }
    
    public void dispatch(final boolean arrayFilled, final CopyOnWriteArrayList minMaxList) {
        this.newHour = this.cal.get(11);
        if (this.oldHour != this.newHour) {
            this.hoursUp = true;
            this.oldHour = this.newHour;
        }
        if (this.webStartupTime + this.webUploadInterval <= System.currentTimeMillis()) {
            this.webPost = true;
            this.webStartupTime = System.currentTimeMillis();
        }
        if (this.cwopStartupTime + this.cwopUploadInterval <= System.currentTimeMillis()) {
            this.cwopPost = true;
            this.cwopStartupTime = System.currentTimeMillis();
        }
        if (this.wowStartupTime + this.wowUploadInterval <= System.currentTimeMillis()) {
            this.wowPost = true;
            this.wowStartupTime = System.currentTimeMillis();
        }
        if (this.w4uStartupTime + this.w4uUploadInterval <= System.currentTimeMillis()) {
            this.w4uPost = true;
            this.w4uStartupTime = System.currentTimeMillis();
        }
        if (this.wunderStartupTime + this.wunderUploadInterval <= System.currentTimeMillis()) {
            this.wunderPost = true;
            this.wunderStartupTime = System.currentTimeMillis();
        }
        if (this.baroTrendStartupTime + 300000L <= System.currentTimeMillis()) {
            this.baroTrendUpdateRecords = true;
            this.baroTrendStartupTime = System.currentTimeMillis();
        }
        if (this.logStartupTime + 900000L <= System.currentTimeMillis()) {
            this.log = true;
            this.logStartupTime = System.currentTimeMillis();
        }
        for (int counter = 0; counter < this.numberofDevices; ++counter) {
            if (arrayFilled && this.sensorReadings[counter].split(":")[0].length() == 16) {
                if (this.props.getProperty(this.sensorReadings[counter].split(":")[0].substring(0, 16) + ".type").trim().equalsIgnoreCase("rain")) {
                    String msg = this.sensorReadings[counter].split(":")[2].trim();
                    if (msg == null) {
                        System.out.println("Null rain reading!");
                    }
                    try {
                        this.newRainCount = Long.parseLong(msg);
                        if (this.newRainCount < this.oldRainCount) {
                            System.out.println("Bad raincount. New reading < old reading");
                        }
                        if (this.oldRainCount == 0L) {
                            this.rp.process(this.newRainCount);
                            this.oldRainCount = this.newRainCount;
                        }
                        else if (this.newRainCount != this.oldRainCount || this.hoursUp || this.log) {
                            System.out.println("Doing rain processing.Oldcount=" + this.oldRainCount + " Newcount=" + this.newRainCount + " HoursUp status=" + this.hoursUp + " Log interval=" + this.log);
                            setRainCount(this.newRainCount);
                            this.rp.process(this.newRainCount);
                            msg = null;
                            if (this.oldRainCount != this.newRainCount) {
                                this.oldRainCount = this.newRainCount;
                            }
                            if (this.hoursUp) {
                                this.hoursUp = false;
                            }
                            this.rp.saveRainReadingList();
                        }
                    }
                    catch (NumberFormatException ex) {
                        System.err.println("Got a number format exception " + ex);
                    }
                }
                if (this.props.getProperty(this.sensorReadings[counter].split(":")[0].substring(0, 16) + ".type").trim().equalsIgnoreCase("lightning")) {
                    final String count = this.sensorReadings[counter].split(":")[2].trim();
                    this.lp.processLightning(count);
                }
            }
        }
        if (this.saveintervalStartTime + this.saveInterval <= System.currentTimeMillis()) {
            final MaxMin maxmin = new MaxMin(this.props);
            maxmin.updateRecord(this.sensorReadings, this.props, minMaxList);
            maxmin.saveminMax(minMaxList);
            System.out.println("Firing XML generator");
            new genXML(this.sensorReadings, this.props, minMaxList);
            this.saveintervalStartTime = System.currentTimeMillis();
        }
        if (this.cwopPost && this.cwopUpload) {
            if (this.cwopAutoRotate) {
                final String server = this.cwopServerList.get(this.cwopListCounter).toString().split(",")[0];
                final int port = Integer.parseInt(this.cwopServerList.get(this.cwopListCounter).toString().split(",")[1]);
                new CWOP(this.props, this.cwopuploadfirstrun);
                ++this.cwopListCounter;
            }
            else {
                System.out.println("Time to upload CWOP");
                new CWOP(this.props, this.cwopuploadfirstrun);
                if (this.cwopuploadfirstrun) {
                    this.cwopuploadfirstrun = false;
                }
            }
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException ex2) {
                ex2.printStackTrace();
            }
            this.cwopPost = false;
        }
        Label_0990: {
            if (!this.wunderPost && !this.w4uPost) {
                if (!this.wowPost) {
                    break Label_0990;
                }
            }
            try {
                new WunderGround_W4U(this.props);
            }
            catch (IOException ex3) {
                ex3.printStackTrace();
            }
            if (this.wunderPost) {
                this.wunderPost = false;
            }
            if (this.w4uPost) {
                this.w4uPost = false;
            }
            if (this.wowPost) {
                this.wowPost = false;
            }
        }
        if (this.log) {
            new WeatherLogger(this.props);
            this.log = false;
        }
        if (this.webPost) {
            this.genHTML = this.props.getProperty("generateHTML").equalsIgnoreCase("true");
            this.generateSecondaryPage = this.props.getProperty("generateSecondaryPage").equalsIgnoreCase("true");
            final boolean ftpUpload = this.props.getProperty("FTPUpload").equalsIgnoreCase("true");
            if (this.genHTML) {
                final String inFile = this.props.getProperty("xmlFileName");
                final String outFile = this.props.getProperty("MainWebPageName");
                final String xslFile = this.props.getProperty("MainXslFileName");
                new Xfmr(inFile, outFile, xslFile);
            }
            if (this.generateSecondaryPage) {
                final String inFile = this.props.getProperty("xmlFileName");
                final String outFile = this.props.getProperty("SecondaryWebPageName");
                final String xslFile = this.props.getProperty("SecondaryXslFileName");
                new Xfmr(inFile, outFile, xslFile);
            }
            if (ftpUpload) {
                new Ftp(this.props).start();
            }
            this.webPost = false;
        }
    }
    
    private static void setRainCount(final long newRainCount) {
        Dispatcher.rainCount = newRainCount;
    }
    
    public static long getRainCount() {
        return Dispatcher.rainCount;
    }
    
    private void processCwopTemp(final ArrayList cwopTempList) {
        int i = 0;
        final long thisTime = System.currentTimeMillis();
        while (i < cwopTempList.size()) {
            long listTime = Long.parseLong(cwopTempList.get(0).toString().split(",")[1]);
            if (thisTime - listTime > 300000L) {
                cwopTempList.remove(0);
                listTime = 0L;
            }
            ++i;
        }
        i = 0;
        this.temp = 0.0f;
        while (i < cwopTempList.size()) {
            this.temp += Float.parseFloat(cwopTempList.get(i).toString().split(",")[0]);
            ++i;
        }
        this.setCwopTemp(Dispatcher.cwopTemp = this.temp / cwopTempList.size());
    }
    
    private void processBarometer(final ArrayList barometerList) {
        System.out.println("In barometer averaging method. List size = " + barometerList.size());
        int i = 0;
        final long thisTime = System.currentTimeMillis();
        while (i < barometerList.size()) {
            long listTime = Long.parseLong(barometerList.get(0).toString().split(",")[1]);
            if (thisTime - listTime > 300000L) {
                barometerList.remove(0);
                listTime = 0L;
                System.out.print("Removed old Barometer element");
            }
            ++i;
        }
        i = 0;
        Dispatcher.barometer = 0.0;
        while (i < barometerList.size()) {
            Dispatcher.barometer += Double.parseDouble(barometerList.get(i).toString().split(",")[0]);
            ++i;
        }
        if (barometerList.isEmpty()) {
            Dispatcher.barometer = PressureSensorBMP180.readSealevelPressure(184.4,bmp180offset,bmp180slope);
        }
        else {
            Dispatcher.barometer /= barometerList.size();
            System.out.println("Barometer average = " + Dispatcher.barometer);
        }
        this.setBarometer(Dispatcher.barometer);
    }
    
    private void processCwopHum(final ArrayList cwopHumList) {
        int i = 0;
        this.hum = 0.0f;
        final long thisTime = System.currentTimeMillis();
        while (i < cwopHumList.size()) {
            long listTime = Long.parseLong(cwopHumList.get(0).toString().split(",")[1]);
            if (thisTime - listTime > 300000L) {
                cwopHumList.remove(0);
                listTime = 0L;
            }
            ++i;
        }
        for (i = 0; i < cwopHumList.size(); ++i) {
            this.hum += Float.parseFloat(cwopHumList.get(i).toString().split(",")[0]);
        }
        this.setCwopHum(Dispatcher.cwopHum = this.hum / cwopHumList.size());
    }
    
    private void setCwopTemp(final float cwopTemp) {
        Dispatcher.cwopTemp = cwopTemp;
    }
    
    private void setBarometer(final double barometer) {
        Dispatcher.barometer = barometer;
    }
    
    private void setCwopHum(final float cwopHum) {
        Dispatcher.cwopHum = cwopHum;
    }
    
    public static float getCwopTemp() {
        return Dispatcher.cwopTemp;
    }
    
    public static double getBarometer() {
        return Dispatcher.barometer;
    }
    
    public static float getCwopHum() {
        return Dispatcher.cwopHum;
    }
    
    static {
        Dispatcher.rainCount = 0L;
    }
}
