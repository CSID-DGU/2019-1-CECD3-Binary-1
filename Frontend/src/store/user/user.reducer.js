import { UserActionTypes } from './user.action';

export const initialState = {
  auth: null,
  id: '',
  name: '',
};

const userReducer = (state = initialState, action) => {
  switch (action.type) {
    case UserActionTypes.LOGIN.SUCCESS:
      return {
        ...state,
        auth: action.payload.auth,
        id: action.payload.id,
        name: action.payload.name,
      };
    case UserActionTypes.LOGOUT.SUCCESS:
      return initialState;
    case UserActionTypes.SIGN_UP.SUCCESS:
      alert('회원가입이 완료되었습니다.');
      window.location.href = '/';
      return {
        auth: action.payload.auth,
        id: action.payload.id,
        name: action.payload.name,
      };
    case UserActionTypes.SIGN_UP_ADMIN.SUCCESS:
      alert('회원가입이 완료되었습니다.');
      window.location.href = '/';
      return {
        auth: action.payload.auth,
        id: action.payload.id,
        name: action.payload.name,
      };
    default:
      return state;
  }
};

export default userReducer;