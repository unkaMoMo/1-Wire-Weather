// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import java.net.URLConnection;
import java.util.Calendar;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.PrintStream;

public class WunderGround_W4U
{
    PrintStream out;
    BufferedReader in;
    String host;
    int port;
    StringBuffer sb;
    float winddir;
    float windspeedmph;
    float windgustmph;
    float humidity;
    double tempf;
    float rainin;
    float dailyrainin;
    float baromin;
    double dewptf;
    private String wunderid;
    private String wunderpass;
    private Properties props;
    private String msg;
    private boolean w4uUpload;
    private boolean wunderUpload;
    private boolean WOWupload;
    
    public WunderGround_W4U(final Properties props) throws IOException {
        this.out = null;
        this.in = null;
        this.host = "localhost";
        this.port = 0;
        this.sb = new StringBuffer();
        this.props = props;
        this.w4uUpload = props.getProperty("w4uUpload").equalsIgnoreCase("true");
        this.wunderUpload = props.getProperty("wunderUpload").equalsIgnoreCase("true");
        this.WOWupload = props.getProperty("wowUpload").equalsIgnoreCase("true");
        final String xmlFileName = props.getProperty("xmlFileName");
        final XMLParser xmlp = new XMLParser();
        xmlp.ParseXML(xmlFileName);
        try {
            this.winddir = Float.parseFloat(XMLParser.getDirection());
            this.windspeedmph = Float.parseFloat(XMLParser.getSpeed());
            this.windgustmph = Float.parseFloat(XMLParser.getGust());
            this.dailyrainin = Float.parseFloat(XMLParser.getRainSinceMidnight());
            this.rainin = Float.parseFloat(XMLParser.getRainHour());
            this.baromin = Float.parseFloat(XMLParser.getBaro());
            this.humidity = Float.parseFloat(XMLParser.getHumidity());
            this.tempf = Double.parseDouble(XMLParser.getTemp());
            this.dewptf = Utilities.getDewpointF((this.tempf - 32.0) / 1.8, this.humidity);
            this.port = Integer.parseInt(props.getProperty("dataport"));
            this.wunderid = props.getProperty("wunderid");
            this.wunderpass = props.getProperty("wunderpass");
            this.windspeedmph = WindSpeedProcessor.getAverageWindSpeed();
            this.windgustmph = WindSpeedProcessor.getWindGust();
            if (this.windgustmph >= 120.0f) {
                this.windgustmph = 0.0f;
            }
        }
        catch (Exception ex) {
            System.err.println("Problem in Wunderground_W4u method ");
            ex.printStackTrace();
        }
        final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        this.sb = new StringBuffer();
        final int day = cal.get(5);
        final int hour = cal.get(11);
        final int minutes = cal.get(12);
        final int seconds = cal.get(13);
        final int year = cal.get(1);
        int month = cal.get(2);
        ++month;
        if (this.wunderUpload) {
            this.sb.append("ID=" + this.wunderid + "&PASSWORD=" + this.wunderpass);
            this.sb.append("&dateutc=" + year + "-");
            if (month < 10) {
                this.sb.append("0" + month + "-");
            }
            else {
                this.sb.append(month + "-");
            }
            if (day < 10) {
                this.sb.append("0" + day + " ");
            }
            else {
                this.sb.append(day + " ");
            }
            if (hour < 10) {
                this.sb.append("0" + hour + ":");
            }
            else {
                this.sb.append(hour + ":");
            }
            if (minutes < 10) {
                this.sb.append("0" + minutes + ":");
            }
            else {
                this.sb.append(minutes + ":");
            }
            if (seconds < 10) {
                this.sb.append("0" + seconds + ":" + "&");
            }
            else {
                this.sb.append(seconds + ":" + "&");
            }
            this.sb.append("winddir=" + this.winddir + "&windspeedmph=" + this.windspeedmph + "&windgustmph=" + this.windgustmph);
            this.sb.append("&humidity=" + this.humidity + "&tempf=" + this.tempf + "&rainin=" + this.rainin + "&dailyrainin=" + this.dailyrainin);
            this.sb.append("&baromin=" + this.baromin + "&dewptf=" + this.dewptf + "&weather=" + "&clouds=" + "&softwaretype=JavaOWW" + "&realtime=1&rtfreq=5.0" + "&action=updateraw");
            this.msg = this.sb.toString();
            final int timeout = 10000;
            URL url = null;
            try {
                url = new URL("http://weatherstation.wunderground.com/weatherstation/updateweatherstation.php?");
            }
            catch (MalformedURLException ex2) {
                ex2.printStackTrace();
            }
            URLConnection conn = null;
            try {
                conn = url.openConnection();
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                OutputStreamWriter wr = null;
                try {
                    wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(this.msg.toString());
                    wr.flush();
                    BufferedReader rd = null;
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        line.trim();
                        if (!line.startsWith("success")) {
                            System.out.println("Wunderground server problems..");
                            wr.close();
                            rd.close();
                            return;
                        }
                        System.out.println("Wunderground  data sent.Response: " + line);
                    }
                    wr.close();
                    rd.close();
                }
                catch (IOException ex3) {
                    ex3.printStackTrace();
                }
            }
            catch (SocketTimeoutException exception) {
                System.out.println("Connection to Wunderground server failed due to connection timeout. Reason: " + exception);
                return;
            }
            url = null;
            try {
                Thread.sleep(2000L);
            }
            catch (InterruptedException ex4) {
                ex4.printStackTrace();
            }
        }
        if (this.w4uUpload) {
            final int timeout = 10000;
            final StringBuffer w4u = new StringBuffer();
            final String w4uid = props.getProperty("w4uid");
            final String w4upass = props.getProperty("w4upass");
            w4u.append("ID=" + w4uid + "&" + "PASSWORD=" + w4upass + "&");
            w4u.append("dateutc=" + year + "-");
            if (month < 10) {
                w4u.append("0" + month + "-");
            }
            else {
                w4u.append(month + "-");
            }
            if (day < 10) {
                w4u.append("0" + day + " ");
            }
            else {
                w4u.append(day + " ");
            }
            if (hour < 10) {
                w4u.append("0" + hour + ":");
            }
            else {
                w4u.append(hour + ":");
            }
            if (minutes < 10) {
                w4u.append("0" + minutes + ":" + "00" + "&");
            }
            else {
                w4u.append(minutes + ":" + "00" + "&");
            }
            w4u.append("winddir=" + this.winddir + "&windspeedmph=" + this.windspeedmph + "&windgustmph=" + this.windgustmph);
            w4u.append("&humidity=" + this.humidity + "&tempf=" + this.tempf + "&rainin=" + this.rainin + "&dailyrainin=" + this.dailyrainin);
            w4u.append("&baromin=" + this.baromin + "&dewptf=" + this.dewptf + "&weather=" + "&clouds=" + "&softwaretype=JavaOWW" + "&action=updateraw");
            try {
                final URL url2 = new URL("http://www.pwsweather.com/pwsupdate/pwsupdate.php?");
                final URLConnection conn2 = url2.openConnection();
                conn2.setConnectTimeout(timeout);
                conn2.setReadTimeout(timeout);
                try {
                    conn2.setDoOutput(true);
                    conn2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    final OutputStreamWriter wr2 = new OutputStreamWriter(conn2.getOutputStream());
                    wr2.write(w4u.toString());
                    wr2.flush();
                    final BufferedReader rd2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                    String line2;
                    for (int linecount = 0; (line2 = rd2.readLine()) != null && linecount < 10; ++linecount) {
                        line2.trim();
                        if (line2.contains("Data")) {
                            System.out.println("Hamweather data sent: Response: " + line2);
                            break;
                        }
                        if (line2.contains("ERROR")) {
                            System.out.println("Hamweather problem: Response: " + line2);
                        }
                    }
                    wr2.close();
                    rd2.close();
                }
                catch (SocketTimeoutException exception2) {
                    System.out.println("Connection to Hamweather server failed due to connection timeout. Reason: " + exception2);
                }
            }
            catch (Exception e) {
                System.out.println("Hamweather exception caught: " + e);
            }
        }
        if (this.WOWupload) {
            final int timeout = 10000;
            final StringBuffer wow = new StringBuffer();
            final String WOWid = props.getProperty("wowid");
            final String WOWkey = props.getProperty("wowkey");
            wow.append("siteid=" + WOWid + "&" + "siteAuthenticationKey=" + WOWkey + "&");
            wow.append("dateutc=" + year + "-");
            if (month < 10) {
                wow.append("0" + month + "-");
            }
            else {
                wow.append(month + "-");
            }
            if (day < 10) {
                wow.append("0" + day + "+");
            }
            else {
                wow.append(day + "+");
            }
            if (hour < 10) {
                wow.append("0" + hour + "%3A");
            }
            else {
                wow.append(hour + "%3A");
            }
            if (minutes < 10) {
                wow.append("0" + minutes + "%3A" + "00" + "&");
            }
            else {
                wow.append(minutes + "%3A" + "00" + "&");
            }
            wow.append("winddir=" + Math.round((int)this.winddir) + "&windspeedmph=" + this.windspeedmph + "&windgustmph=" + this.windgustmph);
            wow.append("&humidity=" + this.humidity + "&dewptf=" + this.dewptf + "&tempf=" + this.tempf + "&rainin=" + this.rainin + "&dailyrainin=" + this.dailyrainin);
            wow.append("&baromin=" + this.baromin + "&softwaretype=javaOWW-1.3");
            try {
                final URL url3 = new URL("http://wow.metoffice.gov.uk/automaticreading?");
                final URLConnection conn3 = url3.openConnection();
                conn3.setConnectTimeout(timeout);
                conn3.setReadTimeout(timeout);
                try {
                    conn3.setDoOutput(true);
                    conn3.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    final OutputStreamWriter wr3 = new OutputStreamWriter(conn3.getOutputStream());
                    wr3.write(wow.toString());
                    wr3.flush();
                    System.out.println(" WOW data sent");
                    final BufferedReader rd3 = new BufferedReader(new InputStreamReader(conn3.getInputStream()));
                    String line2;
                    for (int linecount = 0; (line2 = rd3.readLine()) != null && linecount < 10; ++linecount) {
                        line2.trim();
                        if (line2.contains("")) {
                            System.out.println("WOW data sent: Response: " + line2);
                            break;
                        }
                        if (line2.contains("error")) {
                            System.out.println("WOW problem: Response: " + line2);
                        }
                    }
                    wr3.close();
                    rd3.close();
                }
                catch (SocketTimeoutException exception2) {
                    System.out.println("Connection to WOW server failed due to connection timeout. Reason: " + exception2);
                }
            }
            catch (Exception e) {
                System.out.println("WOW exception caught: " + e);
            }
        }
    }
}
