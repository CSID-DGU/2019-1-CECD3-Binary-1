import React from 'react';
import Map from 'pigeon-maps'

const Marker = ({ left, top, style, children }) => (
  <div style={{
    position: 'absolute',
    left: left,
    top: top,
    ...style,
  }}>{children}</div>
)

const CityMap = ({ drones }) => {
  const userMarkers = [];
  const droneMarkers = [];

  drones.forEach(drone => droneMarkers.push(
    <Marker
      anchor={[drone.lat, drone.lon]}
      payload={drone.id}
      key={drone.id}
      style={{
        width: 20,
        height: 20,
        color: 'white',
        textAlign: 'center',
        background: '#8CC1D8',
        borderTopLeftRadius: '50%',
        borderTopRightRadius: '50%',
        borderBottomLeftRadius: '50%',
        borderBottomRightRadius: '50%',
      }}>
      {drone.id.substring(5, drone.id.length)}
    </Marker>
  ));

  return (
    <Map center={[37.550462, 126.994100]} zoom={13} defaultWidth={'100%'} height={500}>
      {droneMarkers}
    </Map>
  );
}

export default CityMap;