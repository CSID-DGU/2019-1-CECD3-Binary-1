package huins.ex.proto.mavlink;

import android.os.Parcel;
import android.os.Parcelable;

import huins.ex.com.MAVLink.Messages.MAVLinkMessage;

/**
 * Wrapper class for a mavlink message, allowing it to be transmitted over android IPC mechanism.
 */
public class MavlinkMessageWrapper implements Parcelable {

    private MAVLinkMessage mavLinkMessage;

    public MavlinkMessageWrapper(MAVLinkMessage mavlinkMsg) {
        this.mavLinkMessage = mavlinkMsg;
    }

    public MAVLinkMessage getMavLinkMessage() {
        return mavLinkMessage;
    }

    public void setMavLinkMessage(MAVLinkMessage mavLinkMessage) {
        this.mavLinkMessage = mavLinkMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.mavLinkMessage);
    }

    private MavlinkMessageWrapper(Parcel in) {
        this.mavLinkMessage = (MAVLinkMessage) in.readSerializable();
    }

    public static final Creator<MavlinkMessageWrapper> CREATOR = new Creator<MavlinkMessageWrapper>() {
        public MavlinkMessageWrapper createFromParcel(Parcel source) {
            return new MavlinkMessageWrapper(source);
        }

        public MavlinkMessageWrapper[] newArray(int size) {
            return new MavlinkMessageWrapper[size];
        }
    };
}
