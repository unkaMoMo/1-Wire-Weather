/*
 * Hub.java
 *
 * Created on May 24, 2003, 9:07 PM
 */

package SensorEngine;

import com.dalsemi.onewire.container.OneWireContainer;


/** Create a new Hub management class
 * @author mulveyr
 */
public class Hub {
    
    private com.dalsemi.onewire.adapter.DSPortAdapter adapter;
    /** A list of all the 1-wire switches on this network
     */    
    protected java.util.ArrayList hubChannels = new java.util.ArrayList();
    /** A mapping of devices to hub switches
     */    
    protected java.util.Hashtable deviceMap = new java.util.Hashtable();
    
    /**
     * The Data server
     */
    protected SocketServer socketServerData;
    
    
    /** Creates a new instance of Hub
     * @param adapter The 1-wire device adapter to use
     */
    public Hub(com.dalsemi.onewire.adapter.DSPortAdapter adapter,
            SocketServer socketServerData ) {
        this.adapter = adapter;
        this.socketServerData = socketServerData;
        
        this.socketServerData.sendMessage( "Initializing hub");
        
    }
    
    
    /**
     * Query the lan for all instances of a 0x1f Family code - the microlan
     * switches.
     */
    public void queryHub() {
        OneWireContainer owd;
        
        try {
            //socketServerDebug.sendMessage( "Starting Hub Query");
            
            //socketServerDebug.sendMessage( "Checking adapter " + adapter.getPortName() );
            adapter.setSearchAllDevices();
            
            adapter.targetAllFamilies();
            adapter.setSpeed(adapter.SPEED_FLEX);
            adapter.targetFamily( 0x1f );
            
            // enumerate through all the 1-Wire devices found
            for (java.util.Enumeration owd_enum = adapter.getAllDeviceContainers();
            owd_enum.hasMoreElements(); ) {
                owd = ( OneWireContainer ) owd_enum.nextElement();
                hubChannels.add( owd );
                
                
                //socketServerDebug.sendMessage( "Found the hub switch " + owd.getAddressAsString());
            }
            
            closeAllSwitches();
            
            for( int i = 0; i < hubChannels.size(); i++ ) {
                collectDevices( adapter, ( OneWireContainer ) hubChannels.get(i) );
            }
            
            
        } catch ( Exception e ) {
            System.out.println( "An error occurred while attempting hub processing" + e.getMessage() );
        }
        
        
    }
    
    /** Collect all the devices on a given 1-wire switch branch
     * @param adapter The 1-wire adapter to use
     * @param channel The hub switch to examine
     * @throws OneWireException Any 1-wire exception
     */    
    public void collectDevices(com.dalsemi.onewire.adapter.DSPortAdapter adapter, OneWireContainer channel) throws com.dalsemi.onewire.OneWireException {
        OneWireContainer owd;
        HubChannelMap channelRecord = null;
        com.dalsemi.onewire.container.OneWireContainer1F switchcontainer = ( com.dalsemi.onewire.container.OneWireContainer1F ) channel;
        byte[] state = switchcontainer.readDevice();
        int number_of_switches = switchcontainer.getNumberChannels(state);
       //socketServerDebug.sendMessage("This device has " + number_of_switches + " switches");
        for (int i=0; i < number_of_switches; i++) {
            channelRecord = new HubChannelMap( switchcontainer, i );
            state = switchcontainer.readDevice();
            
            boolean switch_state = switchcontainer.getLatchState(i, state);
            
            //socketServerDebug.sendMessage( "Enabling channel " + i + ":" + switch_state );
            
            switchcontainer.setLatchState(i,true,true,state);
            
            switchcontainer.writeDevice(state);
            
            state = switchcontainer.readDevice();
            switch_state = switchcontainer.getLatchState(i, state);
           //socketServerDebug.sendMessage( "Channel " + i + " is now " + switch_state );
            
            adapter.setSearchAllDevices();
            adapter.targetAllFamilies();
            adapter.setSpeed(adapter.SPEED_FLEX);
            
            // enumerate through all the 1-Wire devices found
            for (java.util.Enumeration owd_enum = adapter.getAllDeviceContainers();
            owd_enum.hasMoreElements(); ) {
                owd = ( OneWireContainer ) owd_enum.nextElement();
                
                // Filter out the port adapter and the microlan switches
                if( (!owd.getAddressAsString().endsWith("1F")) &&
                (!owd.getAddressAsString().endsWith("09"))) {
                    deviceMap.put( owd.getAddressAsString(), channelRecord );
                    
                   // socketServerDebug.sendMessage( "Found the device " + owd.getAddressAsString());
                }
            }
            
            
            // Turn the channel off
            switchcontainer.setLatchState( i, false, true, state );
            switchcontainer.writeDevice(state);
            
        }
        
        
        
    }
    
    /** Close all the channel for all microlan switch devices, to ensure
     * that we start in a known state
     * @throws OneWireException Any 1-wire exception
     */
    public void closeAllSwitches() throws com.dalsemi.onewire.OneWireException {
        
        byte[] state;
        int number_of_switches;
        boolean switch_state;
        
        //socketServerDebug.sendMessage( "Closing all switches" );
        for( int i = 0; i < hubChannels.size(); i++ ) {
            com.dalsemi.onewire.container.OneWireContainer1F switchcontainer = ( com.dalsemi.onewire.container.OneWireContainer1F ) hubChannels.get(i);
            state = switchcontainer.readDevice();
            number_of_switches = switchcontainer.getNumberChannels(state);
            for (int j=0; j < number_of_switches; j++) {
                state = switchcontainer.readDevice();
                
                switchcontainer.setLatchState(j,false,true,state);
                
                switchcontainer.writeDevice(state);
            }
            
        }
        
    }
    
    
    /** Get the hash that maps device addresses to the hub channel it resides on
     * @return The address map
     */
    protected java.util.Hashtable getDeviceMap() {
        return deviceMap;
    }
    
}
