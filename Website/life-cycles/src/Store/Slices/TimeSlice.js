import { createSlice } from '@reduxjs/toolkit'

const initialState = {
    time: null, 
}

const timeSlice = createSlice({
  name: 'time',
  initialState,
  reducers: {
    addTimeInStore(state, action) {
      state.time = action.payload;
    },
    
  }
});

export const { addTimeInStore } = timeSlice.actions;

export default timeSlice.reducer;