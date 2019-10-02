import { UserActionTypes } from './user.action';

export const initialState = {
  isLoggedIn: null,
  id: '',
  name: '',
};

const userReducer = (state = initialState, action) => {
  switch (action.type) {
    case UserActionTypes.LOGIN.SUCCESS:
      window.location.href = '/';
      return {
        ...state,
        isLoggedIn: action.payload.isLoggedIn,
        id: action.payload.id,
        name: action.payload.name,
      };
    case UserActionTypes.LOGOUT.SUCCESS:
      window.location.href = '/';
      return initialState;
    case UserActionTypes.SIGN_UP.SUCCESS:
      window.location.href = '/';
      alert('회원가입이 완료되었습니다.');
      return {
        ...state,
        isLoggedIn: action.payload.isLoggedIn,
        id: action.payload.id,
        name: action.payload.name,
      };
    default:
      return state;
  }
};

export default userReducer;