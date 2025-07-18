import axios from "axios";
import config from "./Config/config.tsx";

axios.defaults.baseURL = config.BACKEND_URL;
axios.defaults.headers.post["Content-Type"] = "application.json";

export const request = (method, url, data) => {
  return axios({
    method: method,
    url: url,
    data: data,
  });
};
