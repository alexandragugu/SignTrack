import React from "react";
import { Link, useNavigate } from "react-router-dom";
import "./AdminNavbar.css";
import config from "../../Config/config.tsx";
import { useDispatch } from "react-redux";
import { logout } from "../../Config/authSlice.js";
import apiClient from "../../Utils/ApiClient.tsx";
import { SpeedDial } from "primereact/speeddial";

const AdminNavbar = () => {
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
        navigate("/admin/account");
      },
    },

  ];

  return (
    <div className="custom-navbar-container">
      <div className="custom-navbar">
        <Link to="/admin" className="custom-navbar-logo">
          SIGNTRACK
        </Link>

        <div className="custom-navbar-right">
          <Link to="/admin" className="custom-home-button">
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

export default AdminNavbar;
