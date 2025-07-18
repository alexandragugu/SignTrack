import React, { useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import {
  redirect,
  UNSAFE_ErrorResponseImpl,
  useNavigate,
} from "react-router-dom";
import { useDispatch } from "react-redux";
import { login } from "../Config/authSlice.js";
import CustomSpinner from "../components/CustomSpinner/CustomSpinner.tsx";
import Cookies from "js-cookie";
import config from "../Config/config.tsx";
import apiClient from "../Utils/ApiClient.tsx";
import { persistor } from "../Config/store.js";

const Keycloak2 = () => {
  const [errorMessage, setErrorMessage] = useState<string>("");
  const [tokenFlag, setTokenFlag] = useState(false);

  const navigate = useNavigate();
  const dispatch = useDispatch();

  const excahngeCodeForTokens = async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const authorizationCode = urlParams.get("code");
    console.log("Authorization Code:", authorizationCode);

    if (!authorizationCode) {
      console.error("Auth code is missing");
      return;
    }

    try {
      await apiClient.post("/auth/token", {
        code: authorizationCode,
        redirectUri: config.KEYCLOAK_REDIRECT_URI,
      });

      console.log("Token received successfully");

      const { data: user } = await apiClient.get("/auth/me");
      console.log("User data received successfully");

      //localStorage.setI

      debugger;
      console.log("User data:", user);
      debugger;

      dispatch(
        login({
          username: user.username,
          roles: user.roles,
          lastLoginAt: Date.now(),
        })
      );

      persistor.flush();

      if (user?.roles?.includes("Admin")) {
        navigate("/admin");
      } else {
        navigate("/");
      }
    } catch (error: any) {
      window.location.href = "/login?error=auth_failed";
      console.error("Eroare la procesarea autentificarii:", error);
      setErrorMessage("Eroare la procesarea autentificarii");
    }
  };

  useEffect(() => {
    excahngeCodeForTokens();
  }, []);

  return (
    <div
      className="sign-req-page"
      style={{
        backgroundImage: `url(${process.env.PUBLIC_URL + "bg2.jpg"})`,
      }}
    >
      {" "}
      <div className="spinner-container" style={{}}>
        <CustomSpinner />
      </div>
    </div>
  );
};

export default Keycloak2;
