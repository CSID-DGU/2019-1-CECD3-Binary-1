import { call, put, takeLatest } from 'redux-saga/effects';
import { UrlAction, UrlActionTypes } from './url.action';
import { GET } from '../../utils/Api';

export function* getRosemaryUrl() {
  try {
    yield put(UrlAction.getRosemaryUrl.request());
    const response = yield call(GET, '/oneM2M/rosemary');
    yield put(UrlAction.getRosemaryUrl.success(response.data));
  } catch (error) {
    yield put(UrlAction.getRosemaryUrl.failure(error));
  }
}

export function* getGwAppUrl() {
  try {
    yield put(UrlAction.getGwAppUrl.request());
    const response = yield call(GET, '/oneM2M/gwApp');
    console.log(response);
    yield put(UrlAction.getGwAppUrl.success(response.data));
  } catch (error) {
    yield put(UrlAction.getGwAppUrl.failure());
  }
}

export function* getDroneGrpUrl() {
  try {
    yield put(UrlAction.getDroneGrpUrl.request());
    const response = yield call(GET, '/oneM2M/droneGrp');
    console.log(response);
    yield put(UrlAction.getDroneGrpUrl.success(response.data));
  } catch (error) {
    yield put(UrlAction.getDroneGrpUrl.failure(error));
  }
}

export const UrlSagas = [
  takeLatest(UrlActionTypes.GET_ROSEMARY_URL.INDEX, getRosemaryUrl),
  takeLatest(UrlActionTypes.GET_GW_APP_URL.INDEX, getGwAppUrl),
  takeLatest(UrlActionTypes.GET_DRONE_GRP_URL.INDEX, getDroneGrpUrl),
];