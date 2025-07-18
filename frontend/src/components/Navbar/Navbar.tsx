import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import { logout } from "../../Config/authSlice.js";
import { SpeedDial } from "primereact/speeddial";
import apiClient from "../../Utils/ApiClient.tsx";
import "primereact/resources/themes/saga-purple/theme.css";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import "./Navbar.css";

import config from "../../Config/config.tsx";

const Navbar = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleLogout = async () => {
    console.log("Logging out...");

    try {
      const response = await apiClient.get("/auth/id_token");
      if (!response.data) {
        console.log("Eroare la obtinerea id_token");
        return;
      }
      const idToken = response.data;
      console.log("ID Token:", idToken);

      const logoutUrl =
        `${config.KEYCLOAK_URL}/realms/${config.KEYCLOAK_REALM}/protocol/openid-connect/logout` +
        `?post_logout_redirect_uri=${encodeURIComponent(
          config.KEYCLOAK_REDIRECT_URI_LOGOUT
        )}` +
        `&id_token_hint=${idToken}`;

      const logoutResponse = await apiClient.get("/auth/logout");
      if (!response.data) {
        throw new Error("Logout failed");
      }

      dispatch(logout());
      window.location.href = logoutUrl;
    } catch (error) {
      console.log("Eroare la obtinere id_token", error);
      return;
    }
  };

  const items = [
    {
      label: "Logout",
      icon: "pi pi-power-off",
      command: handleLogout,
    },
    {
      label: "Profile",
      icon: "pi pi-user",
      command: () => {
        navigate("/myAccount");
      },
    },
  ];

  return (
    <div className="custom-navbar-container">
      <div className="custom-navbar">
        <Link to="/" className="custom-navbar-logo">
          SIGNTRACK
        </Link>

        <div className="custom-navbar-right">
          <Link to="/" className="custom-home-button">
            <i className="pi pi-home"></i>
          </Link>
          <SpeedDial
            model={items}
            direction="down"
            buttonClassName="custom-speed-dial-button"
            showIcon="pi pi-caret-down"
            className="custom-speed-dial"
          />
        </div>
      </div>
    </div>
  );
};

export default Navbar;
