// src/app/store.js
import { configureStore } from '@reduxjs/toolkit';
import userReducer from './Slices/UserSlice.js'
import coordinateSlice from './Slices/CoordinateSlice.js';
import timeSlice from './Slices/TimeSlice.js'
import changebleLifeDataSlice from './Slices/ChangebleLifeDataSlice.js'

const store = configureStore({
  reducer: {
    user: userReducer,
    coordinate: coordinateSlice,
    time: timeSlice,
    changebleLifeData: changebleLifeDataSlice
  },
});

export default store;