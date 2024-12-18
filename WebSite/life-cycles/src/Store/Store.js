// src/app/store.js
import { configureStore } from '@reduxjs/toolkit';
import userReducer from './Slices/UserSlice.js'

const store = configureStore({
  reducer: {
    user: userReducer,
  },
});

export default store;