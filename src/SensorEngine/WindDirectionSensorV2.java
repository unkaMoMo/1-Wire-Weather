package SensorEngine;

import java.util.ArrayList;

import com.dalsemi.onewire.container.OneWireContainer12;

/**
 * <p>Title: WindDirectionSensorV2</p>
 * <p>Description: Create a new WindDirectionSensorV2</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Create a Wind Direction Sensor for the original DalSemi weather Station, and the AAG Version 2 station<p>
 * @author Rich Mulvey
 * @version 1.0
 */

public class WindDirectionSensorV2 extends WindDirectionSensor {
    
    /**
     * A list of the addressess for each of the wind direction switches
     */
    protected ArrayList directionOrder = new ArrayList();
    /** List of directions found on the last read
     */
    protected ArrayList lastDirection = new ArrayList();
    
    /** Family code for the 1-Wire ID DS2401  */
    public static final int   DIRECTION_FAMILY     = 0x01;
    
    
    /**
     * Create a new wind direction sensor
     * @param northOffset The offset ( from 1 ) for North
     * @param hubChannel The hubChannel associated with the sensor
     * @param adapter The device adapter to use
     * @param deviceAddress The device address to use
     * @param localName An alternate name for the sensor
     * @param directionOrder A list of all device addresses in the direction sensor
     * @param DataServer The SocketServer handling the Data messages
     * @throws OneWireException Any 1-wire exception
     */
    public WindDirectionSensorV2(com.dalsemi.onewire.adapter.DSPortAdapter adapter, String deviceAddress, String localName, ArrayList directionOrder, int northOffset, SocketServer DataServer, HubChannelMap hubChannel) throws com.dalsemi.onewire.OneWireException {
        
        super( adapter, deviceAddress, localName, DataServer, hubChannel );
        
        this.directionOrder = directionOrder;
        
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
        
        // connect channel B of the DS2407
        byte[] state = ( ( OneWireContainer12 ) sensor ).readDevice();
        ( ( OneWireContainer12 ) sensor ).setLatchState( 1, true,
        ( ( OneWireContainer12 ) sensor ).hasSmartOn(), state );
        
        ( ( OneWireContainer12 ) sensor ).writeDevice( state );
        
        // delay a bit to let line settle
        try {
            Thread.sleep(10);
        } catch ( InterruptedException e ) {
        }
        
        do {
            
            if( tryCount > 0 ) {
               System.out.println( "Retrying the wind direction iteration " + tryCount  );
                
                try {
                    Thread.sleep(500);
                } catch ( Exception sleepException ) {
                }
            }
            
            // find all of the DS2401's currently on the line
            // clear any previous search restrictions
            adapter.setSearchAllDevices();
            adapter.targetAllFamilies();
            
            // get enumeration of the DS2401's on Channel B branch
            lastDirection.clear();
            adapter.targetFamily(DIRECTION_FAMILY);
            
            // copy the enumerated iButtons' ID's into a ArrayList
            for(java.util.Enumeration direction_enum = adapter.getAllDeviceContainers();
            direction_enum.hasMoreElements(); ) {
                // cast the enum as a OneWireContainer
                com.dalsemi.onewire.container.OneWireContainer temp_ibutton =
                (com.dalsemi.onewire.container.OneWireContainer)direction_enum.nextElement();
                
                // extract the ID and add it to the ArrayList
                if( lastDirection.indexOf( temp_ibutton.getAddressAsString() ) == -1 ) {
                    lastDirection.add(temp_ibutton.getAddressAsString());
                    
                    System.out.println( "Adding wind dir ID " + temp_ibutton.getAddressAsString() +
                    " to list of positions found" );
                }
                
            }
        } while ( ( tryCount++ < MAX_RETRY_COUNT ) && ( lastDirection.size() == 0 ) );
        
        if( tryCount >= MAX_RETRY_COUNT ) {
            throw new com.dalsemi.onewire.OneWireException( "Unable to get wind direction" );
        }
        
        // disconnect channel B of the DS2407
        // loop to make sure this was done
        boolean done;
        int cnt = 0;
        do {
            done = true;
            try {
                // attempt the disconnect
                state = ( ( OneWireContainer12 ) sensor ).readDevice();
                ( ( OneWireContainer12 ) sensor ).setLatchState( 1, false, ( ( OneWireContainer12 ) sensor ).hasSmartOn(), state );
                ( ( OneWireContainer12 ) sensor ).writeDevice( state );
            } catch(Exception e) {
                
                // delay a bit to let things settle
                try {
                    Thread.sleep(10);
                } catch ( InterruptedException e2 ) {
                }
                done = false;
            }
        } while ((cnt++ < 10) && (!done));
        
        // check for failure to close
        if (!done) {
            throw new com.dalsemi.onewire.adapter.OneWireIOException("Could not close wind direction sensor");
        }
        
        // Get the direction and update the bin count
        windDirection = getDirectionAsNumber();
        directionBins[windDirection]++;
        
        System.out.println( "Wind direction :" + windDirection );
        
        return getDirectionAsString( windDirection );
    }
    
    
    //-------------------------------------------------------------------------
    /** Get the last direction as a number from 1 to 16 where 1 is due
     *  North and 9 is due South.  Warning, the reference direction
     *  method 'setDirectionIDs' and 'setNorthReference' must be called
     *  successfully before calling this method.
     *
     *  @return number from 1 to 16 representing the true directions. If
     *          the necessary calibration have not first been called, then
     *          this will return 0.
     *  <ul>
     *  <li> (1) NORTH
     *  <li> (2) NORTH-BY-NORTH-EAST
     *  <li> (3) NORTH-EAST
     *  <li> (4) EAST-BY-NORTH-EAST
     *  <li> (5) EAST
     *  <li> (6) EAST-BY-SOUTH-EAST
     *  <li> (7) SOUTH-EAST
     *  <li> (8) SOUTH-BY-SOUTH-EAST
     *  <li> (9) SOUTH
     *  <li> (10) SOUTH-BY-SOUTH-WEST
     *  <li> (11) SOUTH-WEST
     *  <li> (12) WEST-BY-SOUTH-WEST
     *  <li> (13) WEST
     *  <li> (14) WEST-BY-NORTH-WEST
     *  <li> (15) NORTH-WEST
     *  <li> (16) NORTH-BY-NORTH-WEST
     *  </ul>
     */
    public int getDirectionAsNumber() {
        
        int retVal = 0;
        
        // check for 0 direction devices found on last reading
        // or incorrect refererence
        if ((lastDirection.size() == 0) || (directionOrder.size() != 8) ) {
            System.out.println( "Had a problem with the configuration: " + lastDirection.size() + ":" + directionOrder.size() );
            return 0;
        }
        
        
        // loop throught the 8 reference devices
        int next_ref;
        int north_index = northOffset * 2;
        
        for (int ref = 0; ref < 8; ref++) {
            // find next reference device (could be wrap around)
            next_ref = ref + 1;
            if (ref == 7)
                next_ref = 0;
            
            // look for single direction indicator
            if (lastDirection.size() == 1) {
                if (lastDirection.get(0).equals( directionOrder.get(ref))) {
                    int raw_location = ref * 2;
                    
                    if ((raw_location - north_index ) < 0)
                        return (raw_location - north_index + 16);
                    else
                        return (raw_location - north_index);
                }
            }        // look for dual direction indicator
            else {
                if (lastDirection.get(0).equals( directionOrder.get(ref)) &&
                lastDirection.get(1).equals( directionOrder.get(next_ref)) ||
                lastDirection.get(1).equals( directionOrder.get(ref)) &&
                lastDirection.get(0).equals( directionOrder.get(next_ref))) {
                    int raw_location = (ref * 2 + 1);
                    
                    if ((raw_location - north_index) < 0)
                        return (raw_location - north_index + 16);
                    else
                        return (raw_location - north_index);
                }
            }
        }
        
        // not found
        return 0;
    }
    
    
    /** Get the list of wind direction switches that were active
     * @return The list of wind direction switches that were active
     */
    public ArrayList getLastDirection() {
        return lastDirection;
    }
    /** Get the list of device addresses in this wind device
     * @return The list of device addresses in this wind device
     */
    public ArrayList getDirectionOrder() {
        return directionOrder;
    }
    
}
