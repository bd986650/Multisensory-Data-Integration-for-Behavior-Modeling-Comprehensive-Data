import { createSlice } from '@reduxjs/toolkit'

const initialState = {
    coordinate: null, 
}

const coordinateSlice = createSlice({
  name: 'coordinate',
  initialState,
  reducers: {
    addCoordinateInStore(state, action) {
      state.coordinate = action.payload;
    },
    
  }
});

export const { addCoordinateInStore } = coordinateSlice.actions;

export default coordinateSlice.reducer;