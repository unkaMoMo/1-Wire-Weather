// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import SensorEngine.WeatherStation;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Properties;
import java.text.SimpleDateFormat;

public class genXML
{
    StringBuffer xmldata;
    private String format;
    private String minmaxtime;
    SimpleDateFormat time;
    SimpleDateFormat formatter;
    public String msg;
    private String newMsg;
    private double dptc;
    private double humidity;
    boolean useBMP180;
    
    public genXML(final String[] sensorReadings, final Properties props, final CopyOnWriteArrayList minMaxList) {
        this.xmldata = new StringBuffer();
        this.format = "MM/dd/yy h:mm a";
        this.minmaxtime = "h:mm a";
        this.time = new SimpleDateFormat(this.minmaxtime);
        this.formatter = new SimpleDateFormat(this.format);
        final boolean metric = props.getProperty("webPageUnits").equalsIgnoreCase("metric");
        this.useBMP180 = props.getProperty("useBMP180").equalsIgnoreCase("true");
        MaxMin mm = new MaxMin(props);
        final LightningProcessor lp = new LightningProcessor();
        try {
            this.xmldata.append("<?xml version='1.0'?>\r\n<weather>\r\n");
            this.xmldata.append("<timestamp>");
            this.xmldata.append(this.formatter.format(System.currentTimeMillis()));
            this.xmldata.append("</timestamp>\r\n");
            this.xmldata.append("<uptime>");
            this.xmldata.append(WeatherStation.getUpTime());
            this.xmldata.append("</uptime>\r\n");
            this.xmldata.append("<windspeed>\r\n");
            String device = props.getProperty("windspeed");
            if (props.getProperty(device + ".type").equalsIgnoreCase("windspeed")) {
                this.xmldata.append("<description>");
                this.xmldata.append(props.getProperty(device + ".description"));
                this.xmldata.append("</description>\r\n");
                this.xmldata.append("<instant>");
                this.xmldata.append("<value>");
                if (metric) {
                    this.xmldata.append(this.convertWindToMetric(WindSpeedProcessor.getCurrentWindSpeed()));
                }
                else {
                    this.xmldata.append(WindSpeedProcessor.getCurrentWindSpeed());
                }
                this.xmldata.append("</value>\r\n");
                this.xmldata.append("<max>\r\n");
                this.msg = "instant:";
                this.xmldata.append("<value>");
                if (metric) {
                    this.xmldata.append(this.convertWindToMetric(Float.parseFloat(mm.getMaxValue(this.msg, minMaxList))));
                }
                else {
                    this.xmldata.append(mm.getMaxValue(this.msg, minMaxList));
                }
                this.xmldata.append("</value>\r\n");
                this.xmldata.append("<time>");
                this.xmldata.append(this.time.format(Long.parseLong(mm.getMaxTime(this.msg, minMaxList))));
                this.xmldata.append("</time>\r\n");
                this.xmldata.append("</max>\r\n");
                this.xmldata.append("</instant>\r\n");
                this.msg = "average:";
                this.xmldata.append("<average>\r\n");
                this.xmldata.append("<value>");
                if (metric) {
                    this.xmldata.append(this.convertWindToMetric(WindSpeedProcessor.getAverageWindSpeed()));
                }
                else {
                    this.xmldata.append(WindSpeedProcessor.getAverageWindSpeed());
                }
                this.xmldata.append("</value>\r\n");
                this.xmldata.append("<max>\r\n");
                this.xmldata.append("<value>");
                if (metric) {
                    this.xmldata.append(this.convertWindToMetric(Float.parseFloat(mm.getMaxValue(this.msg, minMaxList))));
                }
                else {
                    this.xmldata.append(mm.getMaxValue(this.msg, minMaxList));
                }
                this.xmldata.append("</value>\r\n");
                this.xmldata.append("<time>");
                this.xmldata.append(this.time.format(Long.parseLong(mm.getMaxTime(this.msg, minMaxList))));
                this.xmldata.append("</time>\r\n");
                this.xmldata.append("</max>\r\n");
                this.xmldata.append("</average>\r\n");
                this.msg = "gust:";
                this.xmldata.append("<gust>\r\n");
                this.xmldata.append("<value>");
                if (metric) {
                    this.xmldata.append(this.convertWindToMetric(WindSpeedProcessor.getWindGust()));
                }
                else {
                    this.xmldata.append(WindSpeedProcessor.getWindGust());
                }
                this.xmldata.append("</value>\r\n");
                this.xmldata.append("<max>\r\n");
                this.xmldata.append("<value>");
                if (metric) {
                    this.xmldata.append(this.convertWindToMetric(Float.parseFloat(mm.getMaxValue(this.msg, minMaxList))));
                }
                else {
                    this.xmldata.append(mm.getMaxValue(this.msg, minMaxList));
                }
                this.xmldata.append("</value>\r\n");
                this.xmldata.append("<time>");
                this.xmldata.append(this.time.format(Long.parseLong(mm.getMaxTime(this.msg, minMaxList))));
                this.xmldata.append("</time>\r\n");
                this.xmldata.append("</max>\r\n");
                this.xmldata.append("</gust>\r\n");
            }
            this.xmldata.append("</windspeed>\r\n");
            this.xmldata.append("<winddirection>\r\n");
            for (int i = 0; i < sensorReadings.length; ++i) {
                if (sensorReadings[i].split(":")[0].endsWith("20")) {
                    this.xmldata.append("<description>");
                    this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0].trim() + ".description"));
                    this.xmldata.append("</description>\r\n");
                    this.xmldata.append("<instant>");
                    this.xmldata.append(Float.parseFloat(sensorReadings[i].split(":")[2]) * 22.5f);
                    this.xmldata.append("</instant>\r\n");
                    this.xmldata.append("<average>");
                    this.xmldata.append(WeatherStation.getAverageDirection());
                    this.xmldata.append("</average>\r\n");
                }
            }
            this.xmldata.append("</winddirection>\r\n");
            this.xmldata.append("<temperatures>\r\n");
            for (int i = 0; i < sensorReadings.length; ++i) {
                if (sensorReadings[i].split(":")[0].trim().endsWith("10")) {
                    this.xmldata.append("<temperature>\r\n<description>");
                    this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0] + ".description"));
                    this.xmldata.append("</description>\r\n");
                    this.xmldata.append("<publish>");
                    this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0] + ".publish"));
                    this.xmldata.append("</publish>\r\n");
                    this.xmldata.append("<current>");
                    if (metric) {
                        this.xmldata.append(sensorReadings[i].split(":")[2]);
                    }
                    else {
                        this.xmldata.append(this.convertToF(sensorReadings[i].split(":")[2]));
                    }
                    this.xmldata.append("</current>\r\n");
                    this.xmldata.append("<max>\r\n");
                    this.msg = sensorReadings[i];
                    this.newMsg = this.buildMsg(this.msg);
                    this.xmldata.append("<value>");
                    if (metric) {
                        this.xmldata.append(mm.getMaxValue(this.newMsg, minMaxList));
                    }
                    else {
                        this.xmldata.append(this.convertToF(mm.getMaxValue(this.newMsg, minMaxList)));
                    }
                    this.xmldata.append("</value>\r\n");
                    this.xmldata.append("<time>");
                    this.xmldata.append(this.time.format(Long.parseLong(mm.getMaxTime(this.newMsg, minMaxList))));
                    this.xmldata.append("</time>\r\n");
                    this.xmldata.append("</max>\r\n");
                    this.xmldata.append("<min>\r\n");
                    this.xmldata.append("<value>");
                    if (metric) {
                        this.xmldata.append(mm.getMinValue(this.newMsg, minMaxList));
                    }
                    else {
                        this.xmldata.append(this.convertToF(mm.getMinValue(this.newMsg, minMaxList)));
                    }
                    this.xmldata.append("</value>\r\n");
                    this.xmldata.append("<time>");
                    this.xmldata.append(this.time.format(Long.parseLong(mm.getMinTime(this.newMsg, minMaxList))));
                    this.xmldata.append("</time>\r\n");
                    this.xmldata.append("</min>\r\n");
                    if (props.getProperty(sensorReadings[i].split(":")[0] + ".publish").contains("wunderground") || props.getProperty(sensorReadings[i].split(":")[0] + ".publish").contains("cwop") || props.getProperty(sensorReadings[i].split(":")[0] + ".publish").contains("w4u") || props.getProperty(sensorReadings[i].split(":")[0] + ".publish").contains("mainwebpage")) {
                        this.dptc = Double.parseDouble(sensorReadings[i].split(":")[2]);
                    }
                    this.newMsg = null;
                    this.xmldata.append("</temperature>\r\n");
                }
            }
            this.xmldata.append("</temperatures>\r\n");
            this.xmldata.append("<ds2438temps>\r\n");
            for (int i = 0; i < sensorReadings.length; ++i) {
                if (sensorReadings[i].split(":")[0].contains("_temp")) {
                    this.xmldata.append("<ds2438temp>\r\n");
                    this.xmldata.append("<description>");
                    this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0] + ".description") + " Temperature");
                    this.xmldata.append("</description>\r\n");
                    this.xmldata.append("<publish>");
                    this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0] + ".publish"));
                    this.xmldata.append("</publish>\r\n");
                    this.xmldata.append("<current>");
                    if (metric) {
                        this.xmldata.append(sensorReadings[i].split(":")[2]);
                    }
                    else {
                        this.xmldata.append(this.convertToF(sensorReadings[i].split(":")[2]));
                    }
                    this.xmldata.append("</current>\r\n");
                    this.xmldata.append("<max>\r\n");
                    this.msg = sensorReadings[i];
                    this.newMsg = this.buildMsg(this.msg);
                    this.xmldata.append("<value>");
                    if (metric) {
                        this.xmldata.append(mm.getMaxValue(this.newMsg, minMaxList));
                    }
                    else {
                        this.xmldata.append(this.convertToF(mm.getMaxValue(this.newMsg, minMaxList)));
                    }
                    this.xmldata.append("</value>\r\n");
                    this.xmldata.append("<time>");
                    this.xmldata.append(this.time.format(Long.parseLong(mm.getMaxTime(this.newMsg, minMaxList))));
                    this.xmldata.append("</time>\r\n");
                    this.xmldata.append("</max>\r\n");
                    this.xmldata.append("<min>\r\n");
                    this.xmldata.append("<value>");
                    if (metric) {
                        this.xmldata.append(mm.getMinValue(this.newMsg, minMaxList));
                    }
                    else {
                        this.xmldata.append(this.convertToF(mm.getMinValue(this.newMsg, minMaxList)));
                    }
                    this.xmldata.append("</value>\r\n");
                    this.xmldata.append("<time>");
                    this.xmldata.append(this.time.format(Long.parseLong(mm.getMinTime(this.newMsg, minMaxList))));
                    this.xmldata.append("</time>\r\n");
                    this.xmldata.append("</min>\r\n");
                    this.newMsg = null;
                    this.xmldata.append("</ds2438temp>\r\n");
                }
            }
            this.xmldata.append("</ds2438temps>\r\n");
            this.xmldata.append("<humidities>\r\n");
            for (int i = 0; i < sensorReadings.length; ++i) {
                if (!sensorReadings[i].split(":")[0].toString().contains("_temp") && sensorReadings[i].split(":")[0].trim().length() == 16 && props.getProperty(sensorReadings[i].split(":")[0].trim() + ".type").equalsIgnoreCase("humidity")) {
                    this.xmldata.append("<humidity>\r\n");
                    this.xmldata.append("<description>");
                    this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0].toString().trim() + ".description"));
                    this.xmldata.append("</description>\r\n");
                    this.xmldata.append("<publish>");
                    this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0].toString().trim() + ".publish"));
                    this.xmldata.append("</publish>\r\n");
                    this.xmldata.append("<current>");
                    this.xmldata.append(sensorReadings[i].split(":")[2]);
                    this.xmldata.append("</current>\r\n");
                    this.xmldata.append("<max>\r\n");
                    this.msg = sensorReadings[i];
                    this.newMsg = this.buildMsg(this.msg);
                    this.xmldata.append("<value>");
                    this.xmldata.append(mm.getMaxValue(this.newMsg, minMaxList));
                    this.xmldata.append("</value>\r\n");
                    this.xmldata.append("<time>");
                    this.xmldata.append(this.time.format(Long.parseLong(mm.getMaxTime(this.newMsg, minMaxList))));
                    this.xmldata.append("</time>\r\n");
                    this.xmldata.append("</max>\r\n");
                    this.xmldata.append("<min>\r\n");
                    this.xmldata.append("<value>");
                    this.xmldata.append(mm.getMinValue(this.newMsg, minMaxList));
                    this.xmldata.append("</value>\r\n");
                    this.xmldata.append("<time>");
                    this.xmldata.append(this.time.format(Long.parseLong(mm.getMinTime(this.newMsg, minMaxList))));
                    this.xmldata.append("</time>\r\n");
                    this.xmldata.append("</min>\r\n");
                    if (props.getProperty(sensorReadings[i].split(":")[0].toString().trim() + ".publish").contains("wunderground") || props.getProperty(sensorReadings[i].split(":")[0] + ".publish").contains("cwop") || props.getProperty(sensorReadings[i].split(":")[0].toString().trim() + ".publish").contains("w4u") || props.getProperty(sensorReadings[i].split(":")[0].toString().trim() + ".publish").contains("mainwebpage")) {
                        this.humidity = Double.parseDouble(sensorReadings[i].split(":")[2]);
                    }
                    this.xmldata.append("</humidity>\r\n");
                    this.newMsg = null;
                }
            }
            this.xmldata.append("</humidities>\r\n");
            this.xmldata.append("<dewpoint>");
            if (metric) {
                this.xmldata.append(Utilities.getDewpointC(this.dptc, this.humidity));
            }
            else {
                this.xmldata.append(Utilities.getDewpointF(this.dptc, this.humidity));
            }
            this.xmldata.append("</dewpoint>\r\n");
            this.xmldata.append("<barometer>\r\n");
            if (this.useBMP180) {
                for (int i = 0; i < sensorReadings.length; ++i) {
                    if (!sensorReadings[i].split(":")[0].toString().contains("_temp") && props.getProperty(sensorReadings[i].split(":")[0].toString().trim() + ".type").toLowerCase().contains("barometer")) {
                        this.xmldata.append("<description>");
                        this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0].toString().trim() + ".description"));
                        this.xmldata.append("</description>\r\n");
                        this.xmldata.append("<publish>");
                        this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0].toString().trim() + ".publish"));
                        this.xmldata.append("</publish>\r\n");
                        this.xmldata.append("<current>");
                        if (metric) {
                            this.xmldata.append(Dispatcher.getBarometer());
                        }
                        else {
                            final String bumpercar = String.valueOf(Dispatcher.getBarometer());
                            this.xmldata.append(this.convertPressureToHg(bumpercar));
                        }
                        this.xmldata.append("</current>\r\n");
                        this.xmldata.append("<max>\r\n");
                        this.msg = sensorReadings[i];
                        this.newMsg = this.buildMsg(this.msg);
                        this.xmldata.append("<value>");
                        if (metric) {
                            this.xmldata.append(mm.getMaxValue(this.newMsg, minMaxList));
                        }
                        else {
                            this.xmldata.append(this.convertPressureToHg(mm.getMaxValue(this.newMsg, minMaxList)));
                        }
                        this.xmldata.append("</value>\r\n");
                        this.xmldata.append("<time>");
                        this.xmldata.append(this.time.format(Long.parseLong(mm.getMaxTime(this.newMsg, minMaxList))));
                        this.xmldata.append("</time>\r\n");
                        this.xmldata.append("</max>\r\n");
                        this.xmldata.append("<min>\r\n");
                        this.xmldata.append("<value>");
                        if (metric) {
                            this.xmldata.append(mm.getMinValue(this.newMsg, minMaxList));
                        }
                        else {
                            this.xmldata.append(this.convertPressureToHg(mm.getMinValue(this.newMsg, minMaxList)));
                        }
                        this.xmldata.append("</value>\r\n");
                        this.xmldata.append("<time>");
                        this.xmldata.append(this.time.format(Long.parseLong(mm.getMinTime(this.newMsg, minMaxList))));
                        this.xmldata.append("</time>\r\n");
                        this.xmldata.append("</min>\r\n");
                        this.xmldata.append("<trend>\r\n");
                        this.xmldata.append("<value>");
                        this.xmldata.append(BaroTrend.getTrend());
                        this.xmldata.append("</value>\r\n");
                        this.xmldata.append("</trend>\r\n");
                        this.newMsg = null;
                    }
                }
                this.xmldata.append("</barometer>\r\n");
            }
            else {
                for (int i = 0; i < sensorReadings.length; ++i) {
                    if (!sensorReadings[i].split(":")[0].toString().contains("_temp") && sensorReadings[i].split(":")[0].trim().length() == 16) {
                        if (props.getProperty(sensorReadings[i].split(":")[0].toString().trim() + ".type").contains("barometer")) {
                            this.xmldata.append("<description>");
                            this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0].toString().trim() + ".description"));
                            this.xmldata.append("</description>\r\n");
                            this.xmldata.append("<publish>");
                            this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0].toString().trim() + ".publish"));
                            this.xmldata.append("</publish>\r\n");
                            this.xmldata.append("<current>");
                            if (metric) {
                                this.xmldata.append(sensorReadings[i].split(":")[2]);
                            }
                            else {
                                this.xmldata.append(this.convertPressureToHg(sensorReadings[i].split(":")[2]));
                            }
                            this.xmldata.append("</current>\r\n");
                            this.xmldata.append("<max>\r\n");
                            this.msg = sensorReadings[i];
                            this.newMsg = this.buildMsg(this.msg);
                            this.xmldata.append("<value>");
                            if (metric) {
                                this.xmldata.append(mm.getMaxValue(this.newMsg, minMaxList));
                            }
                            else {
                                this.xmldata.append(this.convertPressureToHg(mm.getMaxValue(this.newMsg, minMaxList)));
                            }
                            this.xmldata.append("</value>\r\n");
                            this.xmldata.append("<time>");
                            this.xmldata.append(this.time.format(Long.parseLong(mm.getMaxTime(this.newMsg, minMaxList))));
                            this.xmldata.append("</time>\r\n");
                            this.xmldata.append("</max>\r\n");
                            this.xmldata.append("<min>\r\n");
                            this.xmldata.append("<value>");
                            if (metric) {
                                this.xmldata.append(mm.getMinValue(this.newMsg, minMaxList));
                            }
                            else {
                                this.xmldata.append(this.convertPressureToHg(mm.getMinValue(this.newMsg, minMaxList)));
                            }
                            this.xmldata.append("</value>\r\n");
                            this.xmldata.append("<time>");
                            this.xmldata.append(this.time.format(Long.parseLong(mm.getMinTime(this.newMsg, minMaxList))));
                            this.xmldata.append("</time>\r\n");
                            this.xmldata.append("</min>\r\n");
                            this.xmldata.append("<trend>\r\n");
                            this.xmldata.append("<value>");
                            this.xmldata.append(BaroTrend.getTrend());
                            this.xmldata.append("</value>\r\n");
                            this.xmldata.append("</trend>\r\n");
                            this.newMsg = null;
                        }
                        this.xmldata.append("</barometer>\r\n");
                    }
                }
            }
            this.xmldata.append("<rain>\r\n");
            device = props.getProperty("rain");
            if (props.getProperty(device + ".type").equalsIgnoreCase("rain")) {
                this.xmldata.append("<description>");
                this.xmldata.append(props.getProperty(device + ".description"));
                this.xmldata.append("</description>\r\n");
                this.xmldata.append("<publish>");
                this.xmldata.append(props.getProperty(device + ".publish"));
                this.xmldata.append("</publish>\r\n");
                this.xmldata.append("<today>");
                if (metric) {
                    this.xmldata.append(this.convertToMilliMeter(RainProcessor.getRainSinceMidnight()));
                }
                else {
                    this.xmldata.append(RainProcessor.getRainSinceMidnight());
                }
                this.xmldata.append("</today>\r\n");
                this.xmldata.append("<rollingday>");
                final float rain24 = RainProcessor.getRain24Hours();
                if (metric) {
                    this.xmldata.append(this.convertToMilliMeter(rain24));
                }
                else {
                    this.xmldata.append(rain24);
                }
                this.xmldata.append("</rollingday>\r\n");
                this.xmldata.append("<currenthour>");
                if (metric) {
                    this.xmldata.append(this.convertToMilliMeter(RainProcessor.getRainHour()));
                }
                else {
                    this.xmldata.append(RainProcessor.getRainHour());
                }
                this.xmldata.append("</currenthour>\r\n");
                this.xmldata.append("<currentweek>");
                if (metric) {
                    this.xmldata.append(this.convertToMilliMeter(RainProcessor.getRainWeek()));
                }
                else {
                    this.xmldata.append(RainProcessor.getRainWeek());
                }
                this.xmldata.append("</currentweek>\r\n");
                this.xmldata.append("<currentmonth>");
                if (metric) {
                    this.xmldata.append(this.convertToMilliMeter(RainProcessor.getRainMonth()));
                }
                else {
                    this.xmldata.append(RainProcessor.getRainMonth());
                }
                this.xmldata.append("</currentmonth>\r\n");
                this.xmldata.append("<currentyear>");
                if (metric) {
                    this.xmldata.append(this.convertToMilliMeter(RainProcessor.getRainYear()));
                }
                else {
                    this.xmldata.append(RainProcessor.getRainYear());
                }
                this.xmldata.append("</currentyear>\r\n");
            }
            this.xmldata.append("</rain>\r\n");
            this.xmldata.append("<lightning>\r\n");
            for (int i = 0; i < sensorReadings.length; ++i) {
                if (sensorReadings[i].split(":")[0].trim().length() == 16 && props.getProperty(sensorReadings[i].split(":")[0].trim() + ".type").equalsIgnoreCase("lightning")) {
                    this.xmldata.append("<description>");
                    this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0].trim() + ".description"));
                    this.xmldata.append("</description>\r\n");
                    this.xmldata.append("<publish>");
                    this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0] + ".publish"));
                    this.xmldata.append("</publish>\r\n");
                    this.xmldata.append("<strikeshour>");
                    this.xmldata.append(lp.getLightningHour());
                    this.xmldata.append("</strikeshour>\r\n");
                    this.xmldata.append("<today>");
                    this.xmldata.append(lp.getLightningToday());
                    this.xmldata.append("</today>\r\n");
                    this.xmldata.append("<year>");
                    this.xmldata.append(lp.getLightningYear());
                    this.xmldata.append("</year>\r\n");
                }
            }
            this.xmldata.append("</lightning>\r\n");
            this.xmldata.append("<geigercounter>\r\n");
            for (int i = 0; i < sensorReadings.length; ++i) {
                if (sensorReadings[i].split(":")[0].trim().length() == 16 && props.getProperty(sensorReadings[i].split(":")[0].trim() + ".type").equalsIgnoreCase("geigercounter")) {
                    this.xmldata.append("<description>");
                    this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0].trim() + ".description"));
                    this.xmldata.append("</description>\r\n");
                    this.xmldata.append("<publish>");
                    this.xmldata.append(props.getProperty(sensorReadings[i].split(":")[0] + ".publish"));
                    this.xmldata.append("</publish>\r\n");
                    this.xmldata.append("<nanoSieverts>");
                    this.xmldata.append(gammaProcessor.getNanoSieverts());
                    this.xmldata.append("</nanoSieverts>\r\n");
                    this.xmldata.append("<CWOPnumber>");
                    this.xmldata.append(gammaProcessor.getCWOPNumber());
                    this.xmldata.append("</CWOPnumber>\r\n");
                }
            }
            this.xmldata.append("</geigercounter>\r\n");
            this.xmldata.append("</weather>\r\n");
        }
        catch (NullPointerException npe) {
            System.err.println("genXML null pointer ERROR!");
            npe.printStackTrace();
        }
        catch (Exception ex) {
            System.err.println("genXML ERROR!");
            ex.printStackTrace();
        }
        final String xmlFileName = props.getProperty("xmlFileName");
        Utilities.fileWriter(this.xmldata.toString(), xmlFileName, false);
        this.xmldata = null;
        mm = null;
        System.gc();
    }
    
    private String buildMsg(final String msg) {
        return this.newMsg = msg.split(":")[0] + ":" + msg.split(":")[1] + ":" + msg.split(":")[2];
    }
    
    private float convertWindToMetric(final float msg) {
        final float wind = msg * 1.609344f;
        return Utilities.roundValue2(wind);
    }
    
    private float convertPressureToHg(final String msg) {
        float pressure = Float.parseFloat(msg) * 0.029535f;
        pressure = Utilities.roundValue2(pressure);
        return pressure;
    }
    
    private float convertToF(final String msg) {
        final float f = Float.parseFloat(msg) * 1.8f + 32.0f;
        return Utilities.roundValue1(f);
    }
    
    private float convertToMilliMeter(final float msg) {
        final float milli = msg / 0.0394f;
        return Utilities.roundValue1(milli);
    }
}
