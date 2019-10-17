// MESSAGE GOPRO_SET_RESPONSE PACKING
package huins.ex.com.MAVLink.ardupilotmega;
import huins.ex.com.MAVLink.MAVLinkPacket;
import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Response from a GOPRO_COMMAND set request
*/
public class msg_gopro_set_response extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_GOPRO_SET_RESPONSE = 219;
    public static final int MAVLINK_MSG_LENGTH = 2;
    private static final long serialVersionUID = MAVLINK_MSG_ID_GOPRO_SET_RESPONSE;


     	
    /**
    * Command ID
    */
    public byte cmd_id;
     	
    /**
    * Result
    */
    public byte result;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_GOPRO_SET_RESPONSE;
        		packet.payload.putByte(cmd_id);
        		packet.payload.putByte(result);
        
        return packet;
    }

    /**
    * Decode a gopro_set_response message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    
        this.cmd_id = payload.getByte();
        	    
        this.result = payload.getByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_gopro_set_response(){
        msgid = MAVLINK_MSG_ID_GOPRO_SET_RESPONSE;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_gopro_set_response(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GOPRO_SET_RESPONSE;
        unpack(mavLinkPacket.payload);        
    }

        
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_GOPRO_SET_RESPONSE -"+" cmd_id:"+cmd_id+" result:"+result+"";
    }
}
        