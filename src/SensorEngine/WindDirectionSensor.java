package SensorEngine;

import java.util.ArrayList;

import com.dalsemi.onewire.container.OneWireContainer12;

/**
 * <p>Title: WindDirectionSensor</p>
 * <p>Description: Create a new WindDirectionSensor</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p/>
 * <p>Base class for wind direction sensors
 * @author Rich Mulvey
 * @version 1.0
 */

public class WindDirectionSensor extends Sensor {
    
    /** The offset to true north, in the direction array
     */
    protected int northOffset;// = 1;
    /** The current wind direction
     */
    protected int windDirection = 0;
    /** List of all the device ID's in the direction array
     */
    
    /** Bins to store the wind directions in */
    public int directionBins[] = new int[20];
    
    
    /**
     * Create a new wind direction sensor
     * @param hubChannel The hubChannel associated with the sensor
     * @param adapter The device adapter to use
     * @param deviceAddress The device address to use
     * @param localName An alternate name for the sensor
     * @param DataServer The SocketServer handling the Data messages
     * @throws OneWireException Any 1-wire exception
     */
    public WindDirectionSensor(com.dalsemi.onewire.adapter.DSPortAdapter adapter, String deviceAddress, String localName, SocketServer DataServer, HubChannelMap hubChannel) throws com.dalsemi.onewire.OneWireException {
        
        super( adapter, deviceAddress, localName, DataServer, hubChannel );
        
        if (sensor == null) {
            throw new com.dalsemi.onewire.OneWireException("No wind direction sensor found with deviceAddress " + deviceAddress );
        }
        
        this.northOffset = northOffset;
        //debug( "North offset is " + northOffset );
       // clearDirectionBins();
    }
    
    
    /** Read the wind direction
     * @throws OneWireException Any 1-wire exception
     * @return The wind direction
     */
    public String hardwareRead() throws com.dalsemi.onewire.OneWireException {
        return "Base Class Implementation";
    }
    
    
    /** Get the wind direction
     * @return The wind direction
     */
    public String getStringValue() {
        return getDirectionAsString( windDirection );
    }
    
    
    /* convert direction integer into compass direction string */
    /** Get the wind direction, as a string
     * @param input The direction, as an integer offset
     * @return The wind direction
     *
     */
    public String getDirectionAsString(int input) {
        String[]  direction = {" N ", "NNE", "NE ", "ENE",
        " E ", "ESE", "SE ", "SSE",
        " S ", "SSW", "SW ", "WSW",
        " W ", "WNW", "NW ", "NNW" };
        
         //debug("WindDirection " + input * 22.5+"\r\n") ;
        //" , cal :" + northOffset );
        
        input = (input + northOffset ) % 16;
        
        //debug("getWindDirectionString result :" + direction[input] );
        
        
        return direction[input];
    }
    
    /**
     * Get the wind direction as a number, from 1 to 16
     * @return The wind direction as a number, from 1 to 16
     */
    public int getDirectionAsNumber() {
        return -1;
    }
    
    
    /** Get the north offset
     * @return The north offset
     */
    public int getNorthOffset() {
        return northOffset;
    }
    /** Set the north offset
     * @param northOffset The north offset
     */
    public void setNorthOffset(int northOffset) {
        this.northOffset = northOffset;
    }
    /** Get the wind direction
     * @return The wind direction
     */
    public int getWindDirection() {
        return windDirection;
    }
    
    /** Reset the counts in all of the direction bins */
    public void clearDirectionBins() {
        for( int i = 0; i < 19; i++ ) {
            directionBins[i] = 0;
        }
    }
    
    
    /**
     * Get the direction bin with the most hits
     * @return The direction bin with the most hits
     */
    public int getProcessedDirection() {
        int maxIndex = 0;
        int maxValue = -1;
        
        for( int i = 0; i < 15; i++ ) {
            
            //debug( "getProcessedDirection: bin " + i + " = " + directionBins[i] );
            // Check to see if we have a new maximum
            if( directionBins[i] > maxValue ) {
                maxIndex = i;
                
                maxValue = directionBins[i];
                
            }
        }
        
        return maxIndex;
        
    }
    public  double getAverageDirection(){
        double averageDirection=1;
        double sum=0;
        int I=0;
       double S=1;
        int holdValue=0;
        double W=0;
        for(int i=16; i<19;i++){
            directionBins[i]=directionBins[i-16];//Append bins 0..3 to end for following calc.
        }
        for( int i = 0; i < 15; i++ ) {
            holdValue=directionBins[i] + directionBins[i+1] + directionBins[i+2]+ directionBins[i+3] + directionBins[i+4]  ;
            if(sum < holdValue){
                sum = holdValue;
                //debug( "getaverageDirection: bin " + i + " = " + directionBins[i] );                               
                I = i;                
                S = sum;//directionBins[i];
            }            
        }
        
        
            W = ( directionBins[I+1] + 2 * directionBins[I+2] + 3 * directionBins[I+3] + 4 * directionBins[I+4] ) * 45 / S;
        
        averageDirection= I * 45 + W ;
        //System.out.println("I= "+I);
        //System.out.println("S= "+S);
        //System.out.println("W= "+W);
        //System.out.println("Raw Average= "+averageDirection);
        if(averageDirection > 720){averageDirection -= 720;}
        averageDirection = (averageDirection / 2) + (northOffset * 22.5 );
        if(averageDirection > 360){averageDirection -= 360;}
        
        
        return Sensor.roundValue(averageDirection);
    }
    
    
}
