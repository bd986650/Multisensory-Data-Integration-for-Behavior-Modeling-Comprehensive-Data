import { createSlice } from '@reduxjs/toolkit'

const initialState = {
  user: '',
  tokenAuthorization: '',
  data: '',
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
  }
});

export const { loginSuccessAddToken, loginSuccessAddUsername, addData } = userSlice.actions;

export default userSlice.reducer;