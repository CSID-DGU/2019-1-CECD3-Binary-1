// MESSAGE TIMESYNC PACKING
package huins.ex.com.MAVLink.common;
import huins.ex.com.MAVLink.MAVLinkPacket;
import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Time synchronization message.
*/
public class msg_timesync extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_TIMESYNC = 111;
    public static final int MAVLINK_MSG_LENGTH = 16;
    private static final long serialVersionUID = MAVLINK_MSG_ID_TIMESYNC;


     	
    /**
    * Time sync timestamp 1
    */
    public long tc1;
     	
    /**
    * Time sync timestamp 2
    */
    public long ts1;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_TIMESYNC;
        		packet.payload.putLong(tc1);
        		packet.payload.putLong(ts1);
        
        return packet;
    }

    /**
    * Decode a timesync message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    
        this.tc1 = payload.getLong();
        	    
        this.ts1 = payload.getLong();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_timesync(){
        msgid = MAVLINK_MSG_ID_TIMESYNC;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_timesync(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_TIMESYNC;
        unpack(mavLinkPacket.payload);        
    }

        
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_TIMESYNC -"+" tc1:"+tc1+" ts1:"+ts1+"";
    }
}
        