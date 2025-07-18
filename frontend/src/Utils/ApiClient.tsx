import axios from "axios";
import Cookies from "js-cookie";
import config from "../Config/config.tsx";
import { store } from "../Config/store.js";
import { logout, login } from "../Config/authSlice.js";

const apiClient = axios.create({
  baseURL: config.BACKEND_URL,
  withCredentials: true,
});

apiClient.interceptors.request.use(
  (config) => {
    const state = store.getState();
    // const accessToken = localStorage.getItem("token");
    //f (accessToken) {
    // config.headers["Authorization"] = `Bearer ${accessToken}`;
    // }
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (
      error.response &&
      error.response.status === 401 &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true;

      try {
        const response = await axios.post(
          `${config.BACKEND_URL}/auth/refreshToken`,
          {},
          { withCredentials: true }
        );

        const newToken = response.data.token;

        store.dispatch(
          login({
            username: response.data.username,
            roles: response.data.roles,
            token: newToken,
          })
        );

        //originalRequest.headers["Authorization"] = `Bearer ${newToken}`;

        return apiClient(originalRequest);
      } catch (refreshError) {
        store.dispatch(logout());
        window.location.href = "/login";
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
