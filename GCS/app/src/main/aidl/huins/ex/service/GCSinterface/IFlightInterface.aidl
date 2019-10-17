// IFlightInterface.aidl
package huins.ex.service.GCSinterface;

// Declare any non-default types here with import statements
import huins.ex.proto.IObserver;
import huins.ex.proto.action.Action;
import huins.ex.proto.IMavlinkObserver;

interface IFlightInterface {

    Bundle getAttribute(String attributeType);

    void performAction(inout Action action);

    oneway void performAsyncAction(in Action action);

    oneway void addAttributesObserver(IObserver observer);

    oneway void removeAttributesObserver(IObserver observer);

    oneway void addMavlinkObserver(IMavlinkObserver observer);

    oneway void removeMavlinkObserver(IMavlinkObserver observer);
}
