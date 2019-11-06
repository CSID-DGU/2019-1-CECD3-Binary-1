export interface IUserGps {
  id: string,
  lat: number,
  lon: number,
  timestamp: string,
}

export const UserGps: IUserGps = {
  id: '',
  lat: 0,
  lon: 0,
  timestamp: '',
}

export interface IDroneGps {
  id: string,
  lat: number,
  lon: number,
  timestamp: string,
}

export const DroneGps: IDroneGps = {
  id: '',
  lat: 0,
  lon: 0,
  timestamp: '',
}