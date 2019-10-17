// MESSAGE GIMBAL_PERFORM_FACTORY_TESTS PACKING
package huins.ex.com.MAVLink.ardupilotmega;
import huins.ex.com.MAVLink.MAVLinkPacket;
import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.Messages.MAVLinkPayload;
        
/**
* 
            Command the gimbal to perform a series of factory tests.  Should not be needed by end users
        
*/
public class msg_gimbal_perform_factory_tests extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_GIMBAL_PERFORM_FACTORY_TESTS = 209;
    public static final int MAVLINK_MSG_LENGTH = 2;
    private static final long serialVersionUID = MAVLINK_MSG_ID_GIMBAL_PERFORM_FACTORY_TESTS;


     	
    /**
    * System ID
    */
    public byte target_system;
     	
    /**
    * Component ID
    */
    public byte target_component;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_GIMBAL_PERFORM_FACTORY_TESTS;
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        
        return packet;
    }

    /**
    * Decode a gimbal_perform_factory_tests message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    
        this.target_system = payload.getByte();
        	    
        this.target_component = payload.getByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_gimbal_perform_factory_tests(){
        msgid = MAVLINK_MSG_ID_GIMBAL_PERFORM_FACTORY_TESTS;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_gimbal_perform_factory_tests(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GIMBAL_PERFORM_FACTORY_TESTS;
        unpack(mavLinkPacket.payload);        
    }

        
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_GIMBAL_PERFORM_FACTORY_TESTS -"+" target_system:"+target_system+" target_component:"+target_component+"";
    }
}
        