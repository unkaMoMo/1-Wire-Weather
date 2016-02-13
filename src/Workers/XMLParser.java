// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;

public class XMLParser
{
    private static String temp;
    private static String hum;
    private static String baro;
    private static String rainThisYear;
    private static String rainSinceMidnight;
    private static String rain24;
    private static String avgspeed;
    private static String gust;
    private static String avgdirection;
    private static String rainHour;
    
    private void setGust(final String gust) {
        XMLParser.gust = gust;
    }
    
    public static String getGust() {
        return XMLParser.gust;
    }
    
    private void setSpeed(final String avgspeed) {
        XMLParser.avgspeed = avgspeed;
    }
    
    public static String getSpeed() {
        return XMLParser.avgspeed;
    }
    
    private void setDirection(final String avgdirection) {
        XMLParser.avgdirection = avgdirection;
    }
    
    public static String getDirection() {
        return XMLParser.avgdirection;
    }
    
    private void setBaro(final String baro) {
        XMLParser.baro = baro;
    }
    
    public static String getBaro() {
        return XMLParser.baro;
    }
    
    private void setRain24(final String rain24) {
        XMLParser.rain24 = rain24;
    }
    
    public static String getRain24() {
        return XMLParser.rain24;
    }
    
    private void setRainSinceMidnight(final String rainSinceMidnight) {
        XMLParser.rainSinceMidnight = rainSinceMidnight;
    }
    
    public static String getRainSinceMidnight() {
        return XMLParser.rainSinceMidnight;
    }
    
    private void setRainThisYear(final String rainThisYear) {
        XMLParser.rainThisYear = rainThisYear;
    }
    
    public static String getRainThisYear() {
        return XMLParser.rainThisYear;
    }
    
    private void setRainHour(final String rainHour) {
        XMLParser.rainHour = rainHour;
    }
    
    public static String getRainHour() {
        return XMLParser.rainHour;
    }
    
    private void setTemp(final String temp) {
        XMLParser.temp = temp;
    }
    
    public static String getTemp() {
        return XMLParser.temp;
    }
    
    private void setHumidity(final String hum) {
        XMLParser.hum = hum;
    }
    
    public static String getHumidity() {
        return XMLParser.hum;
    }
    
    public void ParseXML(final String xmlFileName) {
        try {
            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            final Document doc = docBuilder.parse(new File(xmlFileName));
            doc.getDocumentElement().normalize();
            final Element rootElement = doc.getDocumentElement();
            final NodeList nodes = rootElement.getChildNodes();
            for (int i = 0; i < nodes.getLength(); ++i) {
                if (nodes.item(i) instanceof Element) {
                    final Element element = (Element)nodes.item(i);
                    if (element.getTagName().equals("temperatures")) {
                        final NodeList temperatureNodes = element.getChildNodes();
                        for (int j = 0; j < temperatureNodes.getLength(); ++j) {
                            if (temperatureNodes.item(j) instanceof Element) {
                                final Element temperatureElement = (Element)temperatureNodes.item(j);
                                final NodeList tempNodes = temperatureElement.getChildNodes();
                                for (int k = 0; k < tempNodes.getLength(); ++k) {
                                    if (tempNodes.item(k) instanceof Element) {
                                        Element tempElement = (Element)tempNodes.item(k);
                                        if (tempElement.getTagName().equals("publish") && tempElement.getTextContent().contains("cwop")) {
                                            for (int m = k; m < tempNodes.getLength() - k; ++m) {
                                                if (tempNodes.item(m) instanceof Element) {
                                                    tempElement = (Element)tempNodes.item(m);
                                                    if (tempElement.getTagName().equals("current")) {
                                                        this.setTemp(XMLParser.temp = tempElement.getTextContent());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (nodes.item(i) instanceof Element) {
                    final Element element = (Element)nodes.item(i);
                    if (element.getTagName().equals("humidities")) {
                        final NodeList humidityNodes = element.getChildNodes();
                        for (int j = 0; j < humidityNodes.getLength(); ++j) {
                            if (humidityNodes.item(j) instanceof Element) {
                                final Element humidityElement = (Element)humidityNodes.item(j);
                                final NodeList humNodes = humidityElement.getChildNodes();
                                for (int k = 0; k < humNodes.getLength(); ++k) {
                                    if (humNodes.item(k) instanceof Element) {
                                        Element humElement = (Element)humNodes.item(k);
                                        if (humElement.getTagName().equals("publish") && humElement.getTextContent().contains("cwop")) {
                                            for (int m = k; m < humNodes.getLength() - k; ++m) {
                                                if (humNodes.item(m) instanceof Element) {
                                                    humElement = (Element)humNodes.item(m);
                                                    if (humElement.getTagName().equals("current")) {
                                                        this.setHumidity(XMLParser.hum = humElement.getTextContent());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (nodes.item(i) instanceof Element) {
                    final Element element = (Element)nodes.item(i);
                    if (element.getTagName().equals("barometer")) {
                        final NodeList barometerNodes = element.getChildNodes();
                        for (int j = 0; j < barometerNodes.getLength(); ++j) {
                            if (barometerNodes.item(j) instanceof Element) {
                                final Element barometerElement = (Element)barometerNodes.item(j);
                                if (barometerElement.getTagName().equals("current")) {
                                    this.setBaro(XMLParser.baro = barometerElement.getTextContent());
                                }
                            }
                        }
                    }
                }
                if (nodes.item(i) instanceof Element) {
                    final Element element = (Element)nodes.item(i);
                    if (element.getTagName().equals("rain")) {
                        final NodeList rainNodes = element.getChildNodes();
                        for (int j = 0; j < rainNodes.getLength(); ++j) {
                            if (rainNodes.item(j) instanceof Element) {
                                final Element rainElement = (Element)rainNodes.item(j);
                                if (rainElement.getTagName().equals("currentyear")) {
                                    this.setRainThisYear(XMLParser.rainThisYear = rainElement.getTextContent());
                                }
                                else if (rainElement.getTagName().equals("today")) {
                                    this.setRainSinceMidnight(XMLParser.rainSinceMidnight = rainElement.getTextContent());
                                }
                                else if (rainElement.getTagName().equals("rollingday")) {
                                    this.setRain24(XMLParser.rain24 = rainElement.getTextContent());
                                }
                                else if (rainElement.getTagName().equals("currenthour")) {
                                    this.setRainHour(XMLParser.rainHour = rainElement.getTextContent());
                                }
                            }
                        }
                    }
                }
                if (nodes.item(i) instanceof Element) {
                    final Element element = (Element)nodes.item(i);
                    if (element.getTagName().equals("windspeed")) {
                        final NodeList windspeedNodes = element.getChildNodes();
                        for (int j = 0; j < windspeedNodes.getLength(); ++j) {
                            if (windspeedNodes.item(j) instanceof Element) {
                                final Element windspeedElement = (Element)windspeedNodes.item(j);
                                if (windspeedElement.getTagName().equals("average")) {
                                    final NodeList windNodes = windspeedElement.getChildNodes();
                                    for (int k = 0; k < windNodes.getLength(); ++k) {
                                        if (windNodes.item(k) instanceof Element) {
                                            final Element windElement = (Element)windNodes.item(k);
                                            if (windElement.getTagName().equals("value")) {
                                                this.setSpeed(XMLParser.avgspeed = windElement.getTextContent());
                                            }
                                        }
                                    }
                                }
                                else if (windspeedElement.getTagName().equals("gust")) {
                                    final NodeList windNodes = windspeedElement.getChildNodes();
                                    for (int k = 0; k < windNodes.getLength(); ++k) {
                                        if (windNodes.item(k) instanceof Element) {
                                            final Element windElement = (Element)windNodes.item(k);
                                            if (windElement.getTagName().equals("value")) {
                                                this.setGust(XMLParser.gust = windElement.getTextContent());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (nodes.item(i) instanceof Element) {
                    final Element element = (Element)nodes.item(i);
                    if (element.getTagName().equals("winddirection")) {
                        final NodeList directionNodes = element.getChildNodes();
                        for (int j = 0; j < directionNodes.getLength(); ++j) {
                            if (directionNodes.item(j) instanceof Element) {
                                final Element directionElement = (Element)directionNodes.item(j);
                                if (directionElement.getTagName().equals("average")) {
                                    this.setDirection(XMLParser.avgdirection = directionElement.getTextContent());
                                }
                            }
                        }
                    }
                }
            }
            if (XMLParser.temp != null) {}
        }
        catch (SAXException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex2) {
            ex2.printStackTrace();
        }
        catch (ParserConfigurationException ex3) {
            ex3.printStackTrace();
        }
    }
}
