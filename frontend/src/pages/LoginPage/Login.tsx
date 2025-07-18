import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Login.css";
import { jwtDecode } from "jwt-decode";
import config from "../../Config/config.tsx";

const LoginForm: React.FC = () => {
  const handleLogin = () => {
    const authUrl = `${config.KEYCLOAK_URL}/realms/${
      config.KEYCLOAK_REALM
    }/protocol/openid-connect/auth?response_type=code&client_id=${
      config.KEYCLOAK_CLIENT_ID
    }&redirect_uri=${encodeURIComponent(
      config.KEYCLOAK_REDIRECT_URI
    )}&scope=openid%20profile%20email`;

    window.location.href = authUrl;
  };

  const handleRegister = () => {
    sessionStorage.setItem("register", "true");
    const registerUrl = `${config.KEYCLOAK_URL}/realms/${config.KEYCLOAK_REALM}/protocol/openid-connect/registrations?client_id=${config.KEYCLOAK_CLIENT_ID}&response_type=code&scope=openid&redirect_uri=${config.KEYCLOAK_REDIRECT_URI_LOGIN}`;
    window.location.href = registerUrl;
  };

  return (
    <div>
      <div className="video-container">
        <video autoPlay loop muted className="background-video">
          <source src="/backgroungVideo.mp4" type="video/mp4" />
          Your browser does not support the video tag.
        </video>
        <div className="content">
          <h1>Welcome to SignTrack</h1>

          <div className="login-container">
            <div className="button-group">
              <button onClick={handleRegister}>Register</button>
              <button onClick={handleLogin}>Login</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginForm;
