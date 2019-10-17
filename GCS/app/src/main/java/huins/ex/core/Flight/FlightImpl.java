package huins.ex.core.Flight;

import huins.ex.com.MAVLink.common.msg_heartbeat;
import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightInterfaces.OnFlightListener;
import huins.ex.core.Flight.camera.GoProImpl;
import huins.ex.core.Flight.profiles.Parameters;
import huins.ex.core.Flight.profiles.VehicleProfile;
import huins.ex.core.Flight.variables.Altitude;
import huins.ex.core.Flight.variables.Battery;
import huins.ex.core.Flight.variables.Camera;
import huins.ex.core.Flight.variables.GPS;
import huins.ex.core.Flight.variables.GuidedPoint;
import huins.ex.core.Flight.variables.HeartBeat;
import huins.ex.core.Flight.variables.Home;
import huins.ex.core.Flight.variables.Magnetometer;
import huins.ex.core.Flight.variables.MissionStats;
import huins.ex.core.Flight.variables.Navigation;
import huins.ex.core.Flight.variables.Orientation;
import huins.ex.core.Flight.variables.RC;
import huins.ex.core.Flight.variables.Radio;
import huins.ex.core.Flight.variables.Speed;
import huins.ex.core.Flight.variables.State;
import huins.ex.core.Flight.variables.StreamRates;
import huins.ex.core.Flight.variables.Type;
import huins.ex.core.Flight.variables.calibration.AccelCalibration;
import huins.ex.core.Flight.variables.calibration.MagnetometerCalibrationImpl;
import huins.ex.core.MAVLink.MAVLinkStreams;
import huins.ex.core.MAVLink.WaypointManager;
import huins.ex.core.firmware.FirmwareType;
import huins.ex.core.mission.Mission;
import huins.ex.core.model.AutopilotWarningParser;
import huins.ex.core.model.Flight;


public class FlightImpl implements Flight {

	private final FlightEvents events;
	private final Type type;
	private VehicleProfile profile;
	private final huins.ex.core.Flight.variables.GPS GPS;

	private final huins.ex.core.Flight.variables.RC RC;
	private final Speed speed;
	private final Battery battery;
	private final Radio radio;
	private final Home home;
	private final Mission mission;
	private final MissionStats missionStats;
	private final StreamRates streamRates;
	private final Altitude altitude;
	private final Orientation orientation;
	private final Navigation navigation;
	private final GuidedPoint guidedPoint;
	private final AccelCalibration accelCalibrationSetup;
	private final WaypointManager waypointManager;
	private final Magnetometer mag;
	private final Camera footprints;
	private final State state;
	private final HeartBeat heartbeat;
	private final Parameters parameters;
    private final GoProImpl goProImpl;

	private final MAVLinkStreams.MAVLinkOutputStream MavClient;
	private final Preferences preferences;

    private final LogMessageListener logListener;
	private final MagnetometerCalibrationImpl magCalibration;

	public FlightImpl(MAVLinkStreams.MAVLinkOutputStream mavClient, FlightInterfaces.Clock clock,
					  FlightInterfaces.Handler handler, Preferences pref, AutopilotWarningParser warningParser,
					  LogMessageListener logListener) {
		this.MavClient = mavClient;
		this.preferences = pref;
        this.logListener = logListener;

        events = new FlightEvents(this, handler);
		state = new State(this, clock, handler, warningParser);
		heartbeat = new HeartBeat(this, handler);
		parameters = new Parameters(this, handler);
        this.waypointManager = new WaypointManager(this, handler);

        RC = new RC(this);
        GPS = new GPS(this);
        this.type = new Type(this);
        this.speed = new Speed(this);
        this.battery = new Battery(this);
        this.radio = new Radio(this);
        this.home = new Home(this);
        this.mission = new Mission(this);
        this.missionStats = new MissionStats(this);
        this.streamRates = new StreamRates(this);
        this.altitude = new Altitude(this);
        this.orientation = new Orientation(this);
        this.navigation = new Navigation(this);
        this.guidedPoint =  new GuidedPoint(this);
        this.accelCalibrationSetup = new AccelCalibration(this);
		this.magCalibration = new MagnetometerCalibrationImpl(this);
        this.mag = new Magnetometer(this);
        this.footprints = new Camera(this);
        this.goProImpl = new GoProImpl(this, handler);

        loadVehicleProfile();
	}

	@Override
	public void setAltitudeGroundAndAirSpeeds(double altitude, double groundSpeed, double airSpeed,	double climb) {
		this.altitude.setAltitude(altitude);
		speed.setGroundAndAirSpeeds(groundSpeed, airSpeed, climb);
	}

	@Override
	public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error, double aspd_error) {
		missionStats.setDistanceToWp(disttowp);
		altitude.setAltitudeError(alt_error);
		speed.setSpeedError(aspd_error);
		notifyFlightEvent(FlightEventsType.ORIENTATION);
	}

    @Override
    public boolean isConnected(){
        return MavClient.isConnected() && heartbeat.hasHeartbeat();
    }

	@Override
	public boolean isConnectionAlive() {
		return heartbeat.isConnectionAlive();
	}

	@Override
	public void addFlightListener(OnFlightListener listener) {
		events.addFlightListener(listener);
	}

	@Override
	public void removeFlightListener(OnFlightListener listener) {
		events.removeFlightListener(listener);
	}

	@Override
	public void notifyFlightEvent(final FlightEventsType event) {
        events.notifyFlightEvent(event);
	}

	@Override
	public GPS getGps() {
		return GPS;
	}

	@Override
	public byte getSysid() {
		return heartbeat.getSysid();
	}

	@Override
	public byte getCompid() {
		return heartbeat.getCompid();
	}

	@Override
	public int getMavlinkVersion() {
		return heartbeat.getMavlinkVersion();
	}

	@Override
		public void onHeartbeat(msg_heartbeat msg) {
		heartbeat.onHeartbeat(msg);
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public Parameters getParameters() {
		return parameters;
	}

	@Override
	public void setType(int type) {
		this.type.setType(type);
	}

	@Override
	public int getType() {
		return type.getType();
	}

	@Override
	public FirmwareType getFirmwareType() {
		return type.getFirmwareType();
	}

	@Override
	public void loadVehicleProfile() {
		profile = preferences.loadVehicleProfile(getFirmwareType());
	}

	@Override
	public VehicleProfile getVehicleProfile() {
		return profile;
	}

	@Override
	public MAVLinkStreams.MAVLinkOutputStream getMavClient() {
		return MavClient;
	}

	@Override
	public Preferences getPreferences() {
		return preferences;
	}

	@Override
	public WaypointManager getWaypointManager() {
		return waypointManager;
	}

	@Override
	public RC getRC() {
		return RC;
	}

	@Override
	public Speed getSpeed() {
		return speed;
	}

	@Override
	public Battery getBattery() {
		return battery;
	}

	@Override
	public Radio getRadio() {
		return radio;
	}

	@Override
	public Home getHome() {
		return home;
	}

	@Override
	public Mission getMission() {
		return mission;
	}

	@Override
	public MissionStats getMissionStats() {
		return missionStats;
	}

	@Override
	public StreamRates getStreamRates() {
		return streamRates;
	}

	@Override
	public Altitude getAltitude() {
		return altitude;
	}

	@Override
	public Orientation getOrientation() {
		return orientation;
	}

	@Override
	public Navigation getNavigation() {
		return navigation;
	}

	@Override
	public GuidedPoint getGuidedPoint() {
		return guidedPoint;
	}

	@Override
	public AccelCalibration getCalibrationSetup() {
		return accelCalibrationSetup;
	}

	@Override
	public MagnetometerCalibrationImpl getMagnetometerCalibration() {
		return magCalibration;
	}

	@Override
	public String getFirmwareVersion() {
		return type.getFirmwareVersion();
	}

	@Override
	public void setFirmwareVersion(String message) {
		type.setFirmwareVersion(message);
	}

	@Override
	public Magnetometer getMagnetometer() {
		return mag;
	}
	
	public Camera getCamera() {
		return footprints;
	}

    @Override
    public void logMessage(int mavSeverity, String message) {
        if(logListener != null)
            logListener.onMessageLogged(mavSeverity, message);
    }

    @Override
    public GoProImpl getGoProImpl() {
        return this.goProImpl;
    }
}
