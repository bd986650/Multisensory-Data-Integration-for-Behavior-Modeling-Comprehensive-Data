import { createSlice } from '@reduxjs/toolkit'

const initialState = {
  user: '',
  tokenAuthorization: '',
  data: null,
}

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    loginSuccessAddToken(state, action) {
      state.tokenAuthorization = action.payload;
    },
    loginSuccessAddUsername(state, action) {
      state.user = action.payload;
    },
    addData(state, action) {
      state.data = action.payload
    },
    logout(state) {
      state.user = '';
      state.tokenAuthorization = '';
      state.data = null;
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    }
  }
});

export const { loginSuccessAddToken, loginSuccessAddUsername, addData, logout } = userSlice.actions;

export default userSlice.reducer;