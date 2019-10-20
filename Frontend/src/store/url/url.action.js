import { makeAsyncActionTypes, makeAsyncActionCreator } from '../../utils/ActionHelper';

export const UrlActionTypes = {
  GET_ROSEMARY_URL: makeAsyncActionTypes('GET_ROSEMARY_URL'),
  GET_GW_APP_URL: makeAsyncActionTypes('GET_GW_APP_URL'),
  GET_DRONE_GRP_URL: makeAsyncActionTypes('GET_DRONE_GRP_URL'),
};

export const UrlAction = {
  getRosemaryUrl: makeAsyncActionCreator(UrlActionTypes.GET_ROSEMARY_URL),
  getGwAppUrl: makeAsyncActionCreator(UrlActionTypes.GET_GW_APP_URL),
  getDroneGrpUrl: makeAsyncActionCreator(UrlActionTypes.GET_DRONE_GRP_URL),
};