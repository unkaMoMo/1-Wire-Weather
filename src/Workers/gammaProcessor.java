/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Workers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jim
 */
public class gammaProcessor {

    //private static FileWriter writer;//writes the html file
   // private FileReader reader;//reads the template file
   // private static BufferedReader br;//reads the template file line by line
   // static ArrayList readings = new ArrayList();
    static String CWOPnumber;
    static long radiationCount;
    static long oldCount;
    static long oldReadingTime;
    static long firstReadingTime;
    static long countPeriod;
    static Integer hour = 0;
    static double nanoSieverts;
    static boolean firstRun;
    static float countsPerMinute;
    static long DAY_INTERVAL = 0x5265C00;
    static final double FACTOR = 0.95238095238095238095238095238095;
    static NumberFormat formatter = new DecimalFormat("#");
    //GraphingStuff graph = new GraphingStuff();
    //Calendar cal = new GregorianCalendar();
    //static StringBuffer tmp = new StringBuffer();
    //File file= new File("graph.dat");
    public gammaProcessor() throws NumberFormatException {
        firstRun = true;

//graph.updateChart(xValue, countsPerMinute);

    }

    public static void main(String[] args) {
        gammaProcessor gp = new gammaProcessor();
        int count = 0;
        while (count < 12000) {
            try {
                String message = "C12343BFF432DE23" + ":" + System.currentTimeMillis() + ":" + (98);

                process(message, 6);
                Thread.sleep(1000);
                count++;
            } catch (InterruptedException ex) {
                Logger.getLogger(gammaProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void process(String message, long gammaCount) {
        long readTime = Long.parseLong(message.split(":")[1]);
        try {
            if (firstRun) {
                oldCount = gammaCount;
                oldReadingTime = Long.parseLong(message.split(":")[1]);
                firstReadingTime = oldReadingTime;
                System.out.println("gammaProcessor initiated. Storing oldcount. Loading ArrayList " + oldCount);
                           
           
                firstRun = false;
            //oldCount = 0;//testing number
            }
            if (readTime - oldReadingTime >= 120000) {
                countPeriod = Long.parseLong(message.split(":")[1]) - oldReadingTime;
                radiationCount = gammaCount - oldCount;
                oldReadingTime = Long.parseLong(message.split(":")[1]);
                oldCount = gammaCount;
                //System.out.println("Got two minute radiation count: " + radiationCount);
                //System.out.println("Radiation measurement period= " + (countPeriod / 1000) + " seconds.");
                //Calculate CWOP reading(nanoSievert)
                countsPerMinute = radiationCount / (countPeriod / 60000);
                //System.out.println("Counts per minute = " + countsPerMinute);
                //System.out.println("FACTOR is " + FACTOR);
                nanoSieverts = FACTOR * countsPerMinute;//microRoentgens
                //System.out.println("microRoentgens = " + nanoSieverts);
                nanoSieverts *= 8.77;//Conversion factor from web to get to actual nanoSieverts
               
                System.out.println("NanoSieverts = " + Utilities.roundValue2(nanoSieverts));
                //formatter.format(nanoSieverts);
                double plutoSieverts = round(nanoSieverts, 2);
                //System.out.println("Formatted nanoSieverts = " + formatter.format(nanoSieverts));
                processNumber(formatter.format(plutoSieverts));
            //processNumber(plutoSieverts);
            }

        } catch (NumberFormatException nfe) {
            //System.err.println("Integer too large error..skipping");
            nfe.printStackTrace();
        //return;
        }

    }

    public static double round(double num, int n) {
        if (num == 0) {
            return 0;
        }

        final double d = Math.ceil(Math.log10(num < 0 ? -num : num));
        final int power = n - (int) d;

        final double magnitude = Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }

    public static String processNumber(String nanoSieverts) {
        try {
            //System.out.println("String is " + nanoSieverts.length());
            int size = nanoSieverts.length();
            int tempNumber = Integer.parseInt(nanoSieverts);
            CWOPnumber = formatter.format(tempNumber /= Math.pow(10, size - 2));
            CWOPnumber = CWOPnumber + (size - 2);
            if (size == 2) {
                CWOPnumber = nanoSieverts + "0";
            } else if (size < 2) {
                CWOPnumber = "0" + nanoSieverts + "0";
            }
            //System.out.println("CWOP posting number is " + CWOPnumber);


        } catch (NumberFormatException numberFormatException) {
            System.err.println("Number format error. Setting CWOPnumber to 'no data'");
            CWOPnumber = "X---";
        }
        return CWOPnumber;
    }

    

   




    public static String getCWOPNumber() {
        return CWOPnumber;
    }

    public static double getNanoSieverts() {
        return nanoSieverts;
    }
}
