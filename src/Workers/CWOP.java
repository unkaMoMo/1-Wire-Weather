/*
 * CWOP.java
 * Edit for timestamp renewal
 * Created on August 24, 2007, 3:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Workers;

import java.net.Socket;
import java.text.DecimalFormat;
import java.util.*;
//no edit
//import java.util.Timer;
//import javax.swing.Timer;
//import sun.misc.Timer;
/**
 *
 * @author jim
 */
public class CWOP {

    private int j;
    //Socket socket;
    int numCWOPServers;
    String outgoing;
    //BufferedOutputStream outputStream;
    String cwopid;
    String Long;
    String Lat;
    String server;
    String xmlFileName;
    //int port;
    StringBuffer bf;
    ArrayList cwopServerList = new ArrayList();
    //private boolean cwopuploadfirstrun;
    //Thread t = new Thread(CWOPUpload());

   

    /**
     * Creates a new instance of CWOP
     */
    public CWOP(Properties props, boolean cwopuploadfirstrun) {// throws ServerBusyException {
        //System.out.println("In CWOP() method");
        cwopid = props.getProperty("cwopid");
        Long = props.getProperty("Long");
        Lat = props.getProperty("Lat");
        numCWOPServers = Integer.parseInt(props.getProperty("numCWOPServers"));
        xmlFileName = props.getProperty("xmlFileName");
        for (int i = 0; i < numCWOPServers; i++) {
            String u = props.getProperty("cwopServer" + i);
            cwopServerList.add(u);
        }
        XMLParser xmlp = new XMLParser();
        xmlp.ParseXML(xmlFileName);
        bf = new StringBuffer("");
        //Get CWOP Variables from XML and
        float CWOPDir = Float.parseFloat(XMLParser.getDirection());
        float CWOPSpeed = Float.parseFloat(XMLParser.getSpeed());
        float CWOPGust = Float.parseFloat(XMLParser.getGust());
        float CWOPRain24 = Float.parseFloat(XMLParser.getRain24()) * 100;
        float CWOPRainSinceMidnight = Float.parseFloat(XMLParser.getRainSinceMidnight()) * 100;
        float CWOPRainHour = Float.parseFloat(XMLParser.getRainHour()) * 100;
        float CWOPBaro = Float.parseFloat(XMLParser.getBaro());
        float CWOPHumidity = Dispatcher.getCwopHum();
        float CWOPTemperature = Utilities.roundValue1((Dispatcher.getCwopTemp() * 1.8F) + 32);
        String CWOPRadiation = gammaProcessor.CWOPnumber;
        // Get the current time in gmt
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        int day=cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);         // 0..11
        int minutes = cal.get(Calendar.MINUTE);      // 0..59
        int year= cal.get(Calendar.YEAR);
        int month=cal.get(Calendar.MONTH);
        DecimalFormat myFormatter2 = new DecimalFormat("00");//two digits for humidity
        DecimalFormat myFormatter3 = new DecimalFormat("000"); //three digits with leading zero for direction, rain      
        DecimalFormat myFormatter5 = new DecimalFormat("00000");//five digits for barometer
        //Build the CWOP String 
        bf.append(cwopid).append(">APRS,TCPIP*:");
        if(day < 10){
         bf.append("@"+ "0" + day );
         } else { bf.append("@" + day);
         }
        if(hour < 10) {
         bf.append("0" + hour);
        } else{ bf.append(hour);
         }
        if(minutes < 10){
         bf.append("0" + minutes);
        } else { bf.append(minutes);
         }
        bf.append( "z" + Lat + "/" + Long);
        //Experimental removal of timestamp.Uncomment above lines to restore
        //and remove line under this
        //bf.append("!").append(Lat).append("/").append(Long);
        bf.append("_").append(myFormatter3.format(CWOPDir)).append("/");
        bf.append(myFormatter3.format(CWOPSpeed)).append("g").append(myFormatter3.format(CWOPGust));
        bf.append("t").append(myFormatter3.format(CWOPTemperature));
        bf.append("r").append(myFormatter3.format(CWOPRainHour));
        bf.append("p").append(myFormatter3.format(CWOPRain24));
        bf.append("P").append(myFormatter3.format(CWOPRainSinceMidnight));
        if (myFormatter2.format(CWOPHumidity).toString().equals("100")) {
            //CWOPHumidity = 0.0F;think this is errored(1-3-2014)
        bf.append("h00");
        }else{
        bf.append("h").append(myFormatter2.format(CWOPHumidity));
        }
        CWOPBaro = (CWOPBaro / .029535F) * 10;
        bf.append("b").append(myFormatter5.format(CWOPBaro));
        bf.append("X").append(CWOPRadiation);
        bf.append("j1W\r\n");
        cwopid = "user " + cwopid + " pass -1 vers JavaOWW 1.73\r\n";
        outgoing = bf.toString();
        //for (int i = 0; i < numCWOPServers - 1; i++) {
        //System.out.println("Trying Server: " + server + " On Port " + port);
        //Socket socket = new Socket();
        try {
            //System.out.println("Time to run CWOPUpload1");
            new Thread(new CWOPUpload1(props, cwopid, outgoing, cwopServerList)).start();
            //Runnable r = new CWOPUpload1(socket,props, cwopid, outgoing, cwopServerList);
            //Thread thread = new Thread(r);
            //r.run();
//new CWOPUpload1(props, cwopid, outgoing, cwopServerList).run();

            //cwopuploadfirstrun = false;
            //break
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
            }



            bf = null;
            System.gc();
        } catch (Exception ex) {
        }

        //}
    }
}
