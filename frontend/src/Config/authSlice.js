import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  username: null,
  roles: [],
  isAuthenticated: false,
  loading: true,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    login: (state, action) => {
      const { username, roles } = action.payload;
      state.username = username;
      state.roles = roles;
      state.isAuthenticated = true;
      state.loading = false;
    },
    logout: (state) => {
      state.username = null;
      state.roles = [];
      state.isAuthenticated = false;
      state.loading = false;
    },
    setAuthLoading: (state, action) => {
      state.loading = action.payload;
    },
  },
});

export const { login, logout, setAuthLoading } = authSlice.actions;
export default authSlice.reducer;
