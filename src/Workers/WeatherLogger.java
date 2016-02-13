// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import java.util.Properties;
import java.util.Calendar;

public class WeatherLogger
{
    private String xmlFileName;
    private String msg;
    private String logFileName;
    Calendar cal;
    
    public WeatherLogger(final Properties props) {
        this.cal = Calendar.getInstance();
        try {
            this.xmlFileName = props.getProperty("xmlFileName");
            final XMLParser xmlp = new XMLParser();
            xmlp.ParseXML(this.xmlFileName);
            final long timeStamp = System.currentTimeMillis();
            final StringBuffer sb = new StringBuffer();
            sb.append(timeStamp + ":");
            sb.append("Temperature=" + XMLParser.getTemp() + ":");
            sb.append("Humidity=" + XMLParser.getHumidity() + ":");
            sb.append("Barometer=" + XMLParser.getBaro() + ":");
            sb.append("RainHour=" + XMLParser.getRainHour() + ":");
            sb.append("WindDirection=" + XMLParser.getDirection() + ":");
            sb.append("WindSpeed=" + XMLParser.getSpeed() + ":");
            sb.append("WindGust=" + XMLParser.getGust() + ":");
            sb.append("LightningToday=" + LightningProcessor.lightningToday);
            this.msg = sb.toString();
            this.logFileName = props.getProperty("logFileName");
            this.logFileName = this.cal.get(1) + this.logFileName;
            Utilities.fileWriter(this.msg, this.logFileName, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
