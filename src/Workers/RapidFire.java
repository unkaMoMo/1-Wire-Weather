// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.SocketTimeoutException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.rmi.UnknownHostException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;

public class RapidFire extends Thread
{
    Socket rapidFireSocket;
    PrintStream out;
    BufferedReader in;
    String host;
    int port;
    String msg;
    String line;
    StringBuffer sb;
    long sendTime;
    double winddir;
    double windspeedmph;
    double windgustmph;
    double humidity;
    double tempf;
    double rainin;
    double dailyrainin;
    double baromin;
    double dewptf;
    boolean okToBuild;
    private String wunderid;
    private String wunderpass;
    private Properties props;
    
    public RapidFire(final Properties props) {
        this.rapidFireSocket = null;
        this.out = null;
        this.in = null;
        this.host = "localhost";
        this.port = 0;
        this.msg = "";
        this.line = "";
        this.sb = new StringBuffer();
        this.sendTime = System.currentTimeMillis();
        this.winddir = -999.0;
        this.windspeedmph = -999.0;
        this.windgustmph = -999.0;
        this.humidity = -999.0;
        this.tempf = -999.0;
        this.rainin = -999.0;
        this.dailyrainin = -999.0;
        this.baromin = -999.0;
        this.dewptf = -999.0;
        this.okToBuild = false;
        this.props = props;
        this.port = Integer.parseInt(props.getProperty("dataport"));
        this.wunderid = props.getProperty("wunderid");
        this.wunderpass = props.getProperty("wunderpass");
    }
    
    @Override
    public void run() {
        System.out.println("Rapidfire thread is starting");
        this.connect();
        while (true) {
            this.getServerMessage();
        }
    }
    
    private void getServerMessage() {
        try {
            final String line = this.in.readLine();
            this.msg = line;
            if (!this.msg.contains("Connect") && !this.msg.contains("_temp")) {
                this.upDateVariables(this.msg);
            }
        }
        catch (IOException ex) {}
    }
    
    public void upDateVariables(final String msg) throws IOException {
        if (this.props.getProperty(msg.split(":")[0].substring(0, 16) + ".type").contains("barometer")) {
            this.baromin = Double.parseDouble(msg.split(":")[2]) * 0.029535;
            this.baromin = Utilities.roundValue2(this.baromin);
        }
        else if (this.props.getProperty(msg.split(":")[0].substring(0, 16) + ".type").equalsIgnoreCase("temperature") && this.props.getProperty(msg.split(":")[0].substring(0, 16) + ".publish").contains("wunderground")) {
            this.tempf = Double.parseDouble(msg.split(":")[2]);
            this.tempf = this.tempf * 1.8 + 32.0;
        }
        else if (this.props.getProperty(msg.split(":")[0].substring(0, 16) + ".type").equalsIgnoreCase("humidity") && this.props.getProperty(msg.split(":")[0].substring(0, 16) + ".publish").contains("wunderground")) {
            this.humidity = Double.parseDouble(msg.split(":")[2]);
            this.dewptf = Utilities.getDewpointF((this.tempf - 32.0) / 1.8, this.humidity);
        }
        else if (this.props.getProperty(msg.split(":")[0].substring(0, 16) + ".type").equalsIgnoreCase("direction") && this.props.getProperty(msg.split(":")[0].substring(0, 16) + ".publish").contains("wunderground")) {
            this.winddir = Double.parseDouble(msg.split(":")[2]) * 22.5;
        }
        this.windspeedmph = WindSpeedProcessor.getCurrentWindSpeed();
        this.windgustmph = WindSpeedProcessor.getWindGust();
        this.dailyrainin = RainProcessor.getRainSinceMidnight();
        this.rainin = RainProcessor.getRainHour();
        if (System.currentTimeMillis() >= this.sendTime + 5000L) {
            if (!this.okToBuild) {
                this.checkokToBuild();
                System.out.print("                                           Rapidfire: gathering data\r");
            }
            else {
                this.buildIt();
            }
            this.sendTime = System.currentTimeMillis();
        }
    }
    
    private void connect() {
        if (this.rapidFireSocket == null) {
            System.out.println("Rapidfire client is connecting to " + this.host + " At port " + this.port);
            try {
                this.rapidFireSocket = new Socket(this.host, this.port);
                this.in = new BufferedReader(new InputStreamReader(this.rapidFireSocket.getInputStream()));
            }
            catch (UnknownHostException e) {
                System.err.println("Host: " + this.host + " not found.");
                return;
            }
            catch (IOException e2) {
                System.err.println("Couldn't get I/O for the connection to: " + this.host);
                return;
            }
        }
        if (this.rapidFireSocket.isConnected()) {
            try {
                this.out = new PrintStream(this.rapidFireSocket.getOutputStream());
            }
            catch (Exception ex) {}
        }
    }
    
    private boolean checkokToBuild() {
        if (this.winddir == -999.0 || this.windgustmph == -999.0 || this.windspeedmph == -999.0 || this.tempf == -999.0 || this.dewptf == -999.0 || this.baromin == -999.0 || this.dailyrainin == -999.0 || this.rainin == -999.0 || this.humidity == -999.0) {
            this.okToBuild = false;
        }
        else {
            this.okToBuild = true;
        }
        return this.okToBuild;
    }
    
    private void sendIt(final String msg) throws MalformedURLException, IOException {
        final int timeout = 12000;
        URL url = new URL("http://rtupdate.wunderground.com/weatherstation/updateweatherstation.php?");
        final URLConnection conn = url.openConnection();
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        try {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            final OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(msg.toString());
            wr.flush();
            final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                line.trim();
                if (!line.startsWith("success")) {
                    System.out.println("Wunderground server problems..");
                    wr.close();
                    rd.close();
                    return;
                }
                System.out.print("                                           Wunderground rapidfire data sent\r");
            }
            wr.close();
            rd.close();
        }
        catch (SocketTimeoutException exception) {
            System.out.println("Connection to Wunderground server failed due to connection timeout. Reason: " + exception);
            return;
        }
        url = null;
    }
    
    private void buildIt() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        StringBuffer sb = new StringBuffer();
        final int day = cal.get(5);
        final int hour = cal.get(11);
        final int minutes = cal.get(12);
        final int seconds = cal.get(13);
        final int year = cal.get(1);
        int month = cal.get(2);
        ++month;
        sb.append("ID=" + this.wunderid + "&PASSWORD=" + this.wunderpass);
        sb.append("&dateutc=" + year + "-");
        if (month < 10) {
            sb.append("0" + month + "-");
        }
        else {
            sb.append(month + "-");
        }
        if (day < 10) {
            sb.append("0" + day + " ");
        }
        else {
            sb.append(day + " ");
        }
        if (hour < 10) {
            sb.append("0" + hour + ":");
        }
        else {
            sb.append(hour + ":");
        }
        if (minutes < 10) {
            sb.append("0" + minutes + ":");
        }
        else {
            sb.append(minutes + ":");
        }
        if (seconds < 10) {
            sb.append("0" + seconds + ":" + "&");
        }
        else {
            sb.append(seconds + ":" + "&");
        }
        sb.append("winddir=" + this.winddir + "&windspeedmph=" + this.windspeedmph + "&windgustmph=" + this.windgustmph);
        sb.append("&humidity=" + this.humidity + "&tempf=" + this.tempf + "&rainin=" + this.rainin + "&dailyrainin=" + this.dailyrainin);
        sb.append("&baromin=" + this.baromin + "&dewptf=" + this.dewptf + "&weather=" + "&clouds=" + "&softwaretype=JavaOWW" + "&realtime=1&rtfreq=5.0" + "&action=updateraw");
        this.msg = sb.toString();
        try {
            this.sendIt(this.msg);
        }
        catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex2) {
            ex2.printStackTrace();
        }
        sb = null;
        cal = null;
    }
}
