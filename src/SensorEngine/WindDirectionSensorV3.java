package SensorEngine;

import java.util.ArrayList;

import com.dalsemi.onewire.container.OneWireContainer20;

/** <p>Title: WindDirectionSensor</p>
 * <p>Description: Create a new WindDirectionSensor</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Rich Mulvey
 * @version 1.0
 */

public class WindDirectionSensorV3 extends WindDirectionSensor {
    
    static final float lookupTable[][] = {
        {4.5F, 4.5F, 2.5F, 4.5F}, // N          0
        {4.5F, 2.5F, 2.5F, 4.5F}, // NNE                1
        {4.5F, 2.5F, 4.5F, 4.5F}, // NE         2
        {2.5F, 2.5F, 4.5F, 4.5F}, // ENE                3
        {2.5F, 4.5F, 4.5F, 4.5F}, // E          4
        {2.5F, 4.5F, 4.5F, 0.0F}, // ESE                5
        {4.5F, 4.5F, 4.5F, 0.0F}, // SE         6
        {4.5F, 4.5F, 0.0F, 0.0F}, // SSE                7
        {4.5F, 4.5F, 0.0F, 4.5F}, // S          8
        {4.5F, 0.0F, 0.0F, 4.5F}, // SSW                9
        {4.5F, 0.0F, 4.5F, 4.5F}, // SW         10
        {0.0F, 0.0F, 4.5F, 4.5F}, // WSW                11
        {0.0F, 4.5F, 4.5F, 4.5F}, // W          12
        {0.0F, 4.5F, 4.5F, 2.5F}, // WNW                13
        {4.5F, 4.5F, 4.5F, 2.5F}, // NW                 14
        {4.5F, 4.5F, 2.5F, 2.5F}, // NNW                15
    };
    
    
    /**
     * Create a new wind direction sensor
     * @param hubChannel The hubChannel associated with the sensor
     * @param adapter The device adapter to use
     * @param deviceAddress The device address to use
     * @param localName An alternate name for the sensor
     * @param DataServer The SocketServer handling the Data messages
     * @throws OneWireException Any 1-wire exception
     */
    public WindDirectionSensorV3(com.dalsemi.onewire.adapter.DSPortAdapter adapter, String deviceAddress, String localName,int northOffset, SocketServer DataServer, HubChannelMap hubChannel) throws com.dalsemi.onewire.OneWireException {
        
        super( adapter, deviceAddress, localName, DataServer, hubChannel );
        setNorthOffset(northOffset);
        if (sensor == null) {
            throw new com.dalsemi.onewire.OneWireException("No wind direction sensor found with deviceAddress " + deviceAddress );
        }
       
    }
    
    
    /** Read the wind direction
     * @throws OneWireException Any 1-wire exception
     * @return The wind direction
     */
    public String hardwareRead() throws com.dalsemi.onewire.OneWireException {
        
        int tryCount = 0;
        double [] voltages;
        boolean[] channelsToConvert = { true, true, true, true };
        byte[] state = ( ( OneWireContainer20 ) sensor ).readDevice();
        
   
        //debug( "Starting conversion\r\n");
        ( ( OneWireContainer20 ) sensor ).doADConvert( channelsToConvert, state );
        
        //debug( "Getting voltages\r\n");
        voltages =  ( ( OneWireContainer20 ) sensor ).getADVoltage( state );
        
        
       // System.out.println("Wind Dir AtoD Ch A, B, C, D :" + Math.round( voltages[OneWireContainer20.CHANNELA ]) +
        //":" + Math.round(voltages[OneWireContainer20.CHANNELB ]) +
       // ":" + Math.round(voltages[OneWireContainer20.CHANNELC ]) +
        //":" + Math.round(voltages[OneWireContainer20.CHANNELD ])+'\r'+'\n' );
        
        
        // Get the direction and update the bin count
        windDirection = getDirectionAsNumber( (float) voltages[OneWireContainer20.CHANNELA ],
        (float) voltages[OneWireContainer20.CHANNELB ],
        (float) voltages[OneWireContainer20.CHANNELC ],
        (float) voltages[OneWireContainer20.CHANNELD ]);
        
        directionBins[windDirection]++;
        
       // debug( "Wind direction :" + windDirection+'\r'+'\n' );
        
        return getDirectionAsString( windDirection );
    }
    
    /* convert wind direction A to D results to direction */
    private int getDirectionAsNumber(float a, float b, float c, float d) {
        
        int i;
        int direction = 16;
        int north_index = northOffset;        
        for(i=0; i<16; i++) {
           
            if(((a <= lookupTable[i][0] +1.0) && (a >= lookupTable[i][0] -1.0)) &&
            ((b <= lookupTable[i][1] +1.0) && (b >= lookupTable[i][1] -1.0)) &&
            ((c <= lookupTable[i][2] +1.0) && (c >= lookupTable[i][2] -1.0)) &&
            ((d <= lookupTable[i][3] +1.0) && (d >= lookupTable[i][3] -1.0)) ) {
                direction = i+ north_index;
                 if(direction > 16){
            direction = direction - 16;
            }
                
                break;
            }
        }
        return direction;
    }
    
    
    // Initialize the configuration
    protected void initialize()
    throws com.dalsemi.onewire.OneWireException {
        byte[] state = ( ( OneWireContainer20 ) sensor ).readDevice();
         
        //debug( "Initializing the wind direction sensor\r\n");
        ( ( OneWireContainer20 ) sensor ).setADResolution(OneWireContainer20.CHANNELA, 8, state);
        ( ( OneWireContainer20 ) sensor ).setADResolution(OneWireContainer20.CHANNELB, 8, state);
        ( ( OneWireContainer20 ) sensor ).setADResolution(OneWireContainer20.CHANNELC, 8, state);
        ( ( OneWireContainer20 ) sensor ).setADResolution(OneWireContainer20.CHANNELD, 8, state);
        
        ( ( OneWireContainer20 ) sensor ).setADRange(OneWireContainer20.CHANNELA, 5.12, state);
        ( ( OneWireContainer20 ) sensor ).setADRange(OneWireContainer20.CHANNELB, 5.12, state);
        ( ( OneWireContainer20 ) sensor ).setADRange(OneWireContainer20.CHANNELC, 5.12, state);
        ( ( OneWireContainer20 ) sensor ).setADRange(OneWireContainer20.CHANNELD, 5.12, state);
        ( ( OneWireContainer20 ) sensor ).writeDevice(state);
        
    }
    
    
}
