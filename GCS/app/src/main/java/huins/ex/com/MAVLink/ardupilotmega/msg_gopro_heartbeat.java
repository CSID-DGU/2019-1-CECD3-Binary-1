// MESSAGE GOPRO_HEARTBEAT PACKING
package huins.ex.com.MAVLink.ardupilotmega;
import huins.ex.com.MAVLink.MAVLinkPacket;
import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Heartbeat from a HeroBus attached GoPro
*/
public class msg_gopro_heartbeat extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_GOPRO_HEARTBEAT = 215;
    public static final int MAVLINK_MSG_LENGTH = 1;
    private static final long serialVersionUID = MAVLINK_MSG_ID_GOPRO_HEARTBEAT;


     	
    /**
    * Status
    */
    public byte status;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_GOPRO_HEARTBEAT;
        		packet.payload.putByte(status);
        
        return packet;
    }

    /**
    * Decode a gopro_heartbeat message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    
        this.status = payload.getByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_gopro_heartbeat(){
        msgid = MAVLINK_MSG_ID_GOPRO_HEARTBEAT;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_gopro_heartbeat(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GOPRO_HEARTBEAT;
        unpack(mavLinkPacket.payload);        
    }

      
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_GOPRO_HEARTBEAT -"+" status:"+status+"";
    }
}
        