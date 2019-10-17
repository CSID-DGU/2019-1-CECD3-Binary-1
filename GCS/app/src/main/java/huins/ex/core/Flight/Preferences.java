package huins.ex.core.Flight;

import huins.ex.core.Flight.profiles.VehicleProfile;
import huins.ex.core.Flight.variables.StreamRates;
import huins.ex.core.firmware.FirmwareType;

public interface Preferences {

	public abstract FirmwareType getVehicleType();

	public abstract VehicleProfile loadVehicleProfile(FirmwareType firmwareType);

    public StreamRates.Rates getRates();
}
