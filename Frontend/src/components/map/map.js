import React from 'react';
import Map from 'pigeon-maps'

const Marker = ({ left, top, style, children }) => (
  <div style={{
    position: 'absolute',
    left: left,
    top: top,
    width: 20,
    height: 20,
    color: 'white',
    textAlign: 'center',
    borderTopLeftRadius: '50%',
    borderTopRightRadius: '50%',
    borderBottomLeftRadius: '50%',
    borderBottomRightRadius: '50%',
    ...style,
  }}>{children}</div>
)

const CityMap = ({ users, drones }) => {
  const userMarkers = [];
  const droneMarkers = [];

  users.forEach(user => userMarkers.push(
    <Marker
      anchor={[user.lat, user.lon]}
      payload={user.id}
      key={user.id}
      style={{ background: 'black' }}>
      {user.id.substring(0, 1)}
    </Marker>
  ));

  drones.forEach(drone => droneMarkers.push(
    <Marker
      anchor={[drone.lat, drone.lon]}
      payload={drone.id}
      key={drone.id}
      style={{ background: '#8CC1D8' }}>
      {drone.id.substring(6, drone.id.length)}
    </Marker>
  ));

  return (
    <Map center={[37.550462, 126.994100]} zoom={13} defaultWidth={'100%'} height={500}>
      {userMarkers}
      {droneMarkers}
    </Map>
  );
}

export default CityMap;