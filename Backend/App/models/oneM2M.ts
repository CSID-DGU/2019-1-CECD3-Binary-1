export interface IoneM2M {
  Rosemary: { id: string, url: string },
  GW_APP: { id: string, url: string },
  Drone_Grp: { id: string, url: string }[],
}

export const oneM2M: IoneM2M = {
  Rosemary: { id: 'Rosemary', url: '' },
  GW_APP: { id: 'GW_APP', url: '' },
  Drone_Grp: [],
}