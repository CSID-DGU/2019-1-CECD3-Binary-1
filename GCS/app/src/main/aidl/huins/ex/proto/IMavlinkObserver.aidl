// IMavlinkObserver.aidl
package huins.ex.proto;

import huins.ex.proto.mavlink.MavlinkMessageWrapper;

/**
* Asynchronous notification on receipt of new mavlink message.
*/
oneway interface IMavlinkObserver {

    /**
    * Notify observer that a mavlink message was received.
    * @param messageWrapper Wrapper for the received mavlink message.
    */
    void onMavlinkMessageReceived(in MavlinkMessageWrapper messageWrapper);
}
