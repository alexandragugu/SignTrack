import React, { useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import { useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import { login } from "../Config/authSlice.js";
import Cookies from "js-cookie";
import config from "../Config/config.tsx";
import CustomSpinner from "../components/CustomSpinner/CustomSpinner.tsx";
import apiClient from "../Utils/ApiClient.tsx";

const Register = () => {
  const [errorMessage, setErrorMessage] = useState<string>("");

  const navigate = useNavigate();
  const dispatch = useDispatch();

  const handleRegistration = async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const authorizationCode = urlParams.get("code");

    if (!authorizationCode) {
      console.error("Auth code is missing");
      return;
    }

    try {
      await apiClient.post("/auth/register", {
        code: authorizationCode,
        redirectUri: config.KEYCLOAK_REDIRECT_URI_LOGIN,
      });

      console.log("Cookie setat cu succes!");

      const response = await apiClient.get("/auth/me");
      const user = response.data;
      console.log("Utilizator autentificat:", user);
      navigate("/success");
    } catch (error: any) {
      console.error("Eroare la procesarea autentificarii:", error);
      setErrorMessage("Eroare la procesarea autentificarii");
    }
  };

  useEffect(() => {
    handleRegistration();
  }, []);

  return (
    <div>
      {" "}
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
    </div>
  );
};

export default Register;
