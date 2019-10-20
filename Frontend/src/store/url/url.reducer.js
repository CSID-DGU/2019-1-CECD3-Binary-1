import { UrlActionTypes } from './url.action';

export const initialState = {
  rosemary: { id: 'Rosemary', url: '' },
  gwApp: { id: 'GW_APP', url: '' },
  droneGrp: [],
};

const urlReducer = (state = initialState, action) => {
  switch (action.type) {
    case UrlActionTypes.GET_ROSEMARY_URL.SUCCESS:
      return {
        ...state,
        rosemary: action.payload,
      };
    case UrlActionTypes.GET_GW_APP_URL.SUCCESS:
      return {
        ...state,
        gwApp: action.payload,
      };
    case UrlActionTypes.GET_DRONE_GRP_URL.SUCCESS:
      return {
        ...state,
        droneGrp: action.payload,
      };
    default:
      return state;
  }
};

export default urlReducer;