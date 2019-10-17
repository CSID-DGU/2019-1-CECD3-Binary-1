// MESSAGE AIRSPEED_AUTOCAL PACKING
package huins.ex.com.MAVLink.ardupilotmega;
import huins.ex.com.MAVLink.MAVLinkPacket;
import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.Messages.MAVLinkPayload;

/**
* Airspeed auto-calibration
*/
public class msg_airspeed_autocal extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_AIRSPEED_AUTOCAL = 174;
    public static final int MAVLINK_MSG_LENGTH = 48;
    private static final long serialVersionUID = MAVLINK_MSG_ID_AIRSPEED_AUTOCAL;


     	
    /**
    * GPS velocity north m/s
    */
    public float vx;
     	
    /**
    * GPS velocity east m/s
    */
    public float vy;
     	
    /**
    * GPS velocity down m/s
    */
    public float vz;
     	
    /**
    * Differential pressure pascals
    */
    public float diff_pressure;
     	
    /**
    * Estimated to true airspeed ratio
    */
    public float EAS2TAS;
     	
    /**
    * Airspeed ratio
    */
    public float ratio;
     	
    /**
    * EKF state x
    */
    public float state_x;
     	
    /**
    * EKF state y
    */
    public float state_y;
     	
    /**
    * EKF state z
    */
    public float state_z;
     	
    /**
    * EKF Pax
    */
    public float Pax;
     	
    /**
    * EKF Pby
    */
    public float Pby;
     	
    /**
    * EKF Pcz
    */
    public float Pcz;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_AIRSPEED_AUTOCAL;
        		packet.payload.putFloat(vx);
        		packet.payload.putFloat(vy);
        		packet.payload.putFloat(vz);
        		packet.payload.putFloat(diff_pressure);
        		packet.payload.putFloat(EAS2TAS);
        		packet.payload.putFloat(ratio);
        		packet.payload.putFloat(state_x);
        		packet.payload.putFloat(state_y);
        		packet.payload.putFloat(state_z);
        		packet.payload.putFloat(Pax);
        		packet.payload.putFloat(Pby);
        		packet.payload.putFloat(Pcz);
        
        return packet;
    }

    /**
    * Decode a airspeed_autocal message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    
        this.vx = payload.getFloat();
        	    
        this.vy = payload.getFloat();
        	    
        this.vz = payload.getFloat();
        	    
        this.diff_pressure = payload.getFloat();
        	    
        this.EAS2TAS = payload.getFloat();
        	    
        this.ratio = payload.getFloat();
        	    
        this.state_x = payload.getFloat();
        	    
        this.state_y = payload.getFloat();
        	    
        this.state_z = payload.getFloat();
        	    
        this.Pax = payload.getFloat();
        	    
        this.Pby = payload.getFloat();
        	    
        this.Pcz = payload.getFloat();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_airspeed_autocal(){
        msgid = MAVLINK_MSG_ID_AIRSPEED_AUTOCAL;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_airspeed_autocal(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_AIRSPEED_AUTOCAL;
        unpack(mavLinkPacket.payload);        
    }

                            
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_AIRSPEED_AUTOCAL -"+" vx:"+vx+" vy:"+vy+" vz:"+vz+" diff_pressure:"+diff_pressure+" EAS2TAS:"+EAS2TAS+" ratio:"+ratio+" state_x:"+state_x+" state_y:"+state_y+" state_z:"+state_z+" Pax:"+Pax+" Pby:"+Pby+" Pcz:"+Pcz+"";
    }
}
        