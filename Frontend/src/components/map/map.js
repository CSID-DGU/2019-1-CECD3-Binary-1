import React from 'react';
import Map from 'pigeon-maps'

const drones = [
  { id: 'drone1', lat: 37.550462, lng: 126.994100 },
  { id: 'drone2', lat: 37.551562, lng: 126.997000 },
  { id: 'drone3', lat: 37.552662, lng: 126.999200 },
  { id: 'drone4', lat: 37.551762, lng: 126.990400 },
  { id: 'drone5', lat: 37.554862, lng: 126.988300 },
];

const DroneMarker = ({ left, top, style, children }) => (
  <div style={{
    position: 'absolute',
    left: left,
    top: top,
    ...style,
  }}>{children}</div>
)

const CityMap = () => {
  return (
    <Map center={[37.550462, 126.994100]} zoom={13} defaultWidth={'100%'} height={500}>
      {drones.map((drone, index) => (
        <DroneMarker
          anchor={[drone.lat, drone.lng]}
          payload={index}
          key={index}
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
        </DroneMarker>
      ))}
    </Map>
  );
}

export default CityMap;