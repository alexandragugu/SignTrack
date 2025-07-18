import React, { useEffect, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { useDispatch } from "react-redux";
import { TabView, TabPanel } from "primereact/tabview";
import { Button } from "primereact/button";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import "./AdminAccount.css";
import apiClient from "./../../../Utils/ApiClient.tsx";
import config from "../../../Config/config.tsx";
import { logout } from "../../../Config/authSlice.js";
import { motion } from "framer-motion";
import CreatedAdmins from "../../CreatedAdmins/CreatedAdmins.tsx";
import AdminEditUser from "../../AdminEditUser/AdminEditUser.tsx";
import { useConfirm } from "../../../components/GlobalConfirmDialog/GlobalConfirmDialog.tsx";

const AdminAccount = () => {
  const location = useLocation();
  const { userId } = location.state || {};
  const navigate = useNavigate();
  const confirm = useConfirm();
  const dispatch = useDispatch();

  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchUserDetails();
  }, [userId]);

  const fetchUserDetails = async () => {
    try {
      const response = await apiClient.get(`/admin/account`);
      const userData = response.data;
      if (userData.lastLogin && Array.isArray(userData.lastLogin)) {
        userData.lastLogin = new Date(...userData.lastLogin);
      }
      if (
        userData.registrationDate &&
        Array.isArray(userData.registrationDate)
      ) {
        userData.registrationDate = new Date(...userData.registrationDate);
      }
      setUser(userData);
      setLoading(false);
      console.log("User data:", userData);
    } catch (err) {
      setError("Failed to fetch user details");
      setLoading(false);
    }
  };

  const handleLogoutDelete = (id_token) => {
    console.log("Logging out...");
    const logoutUrl =
      `${config.KEYCLOAK_URL}/realms/${config.KEYCLOAK_REALM}/protocol/openid-connect/logout` +
      `?post_logout_redirect_uri=${encodeURIComponent(
        config.KEYCLOAK_REDIRECT_URI_LOGOUT
      )}` +
      `&id_token_hint=${id_token}`;

    dispatch(logout());
    window.location.href = logoutUrl;
  };

  const handleDelete = async () => {
    console.log("Deleting account...");
    try {
      const response = await apiClient.delete("/admin/account");
      const id_token = response.data.id_token;
      if (response.status === 200) {
        console.log("Account deleted successfully");
        handleLogoutDelete(id_token);
      }
    } catch (error) {
      console.error("Failed to delete account", error);
    }
  };

  if (loading)
    return <p className="admin-admin-loading">Loading user details...</p>;
  if (error) return <p className="admin-admin-error">{error}</p>;
  if (!user) return <p className="admin-admin-error">User not found</p>;

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

  return (
    <motion.div
      className="admin-admin-details"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      transition={{
        type: "spring",
        stiffness: 60,
        damping: 30,
        duration: 0.6,
      }}
    >
      <div className="admin-admin-sidebar">
        <div className="admin-admin-avatar">
          <i className="pi pi-user" />
        </div>
        <span className="admin-admin-username">{user.username}</span>
        <div className="admin-admin-info-grid">
          <div className="admin-admin-info-card">
            <i className="pi pi-envelope"></i>
            <div className="admin-admin-info-text">
              <span className="admin-admin-label">Email</span>
              <span className="admin-admin-value">{user.email}</span>
            </div>
          </div>
          <div className="admin-admin-info-card">
            <i className="pi pi-calendar"></i>
            <div className="admin-admin-info-text">
              <span className="admin-admin-label">Registration</span>
              <span className="admin-admin-value">
                {new Date(user.registrationDate).toLocaleDateString()}
              </span>
            </div>
          </div>
          {user.role.some(
            (r) => r.toLowerCase() === "admin" || r.toLowerCase() === "user"
          ) && (
            <div className="admin-admin-info-card">
              <i className="pi pi-shield"></i>
              <div className="admin-admin-info-text">
                <span className="admin-admin-label">Role</span>
                <span className="admin-admin-value">
                  {user.role.includes("Admin") ? "Admin" : "User"}
                </span>
              </div>
            </div>
          )}
          <div className="admin-admin-info-card">
            <i className="pi pi-info-circle"></i>
            <div className="admin-admin-info-text">
              <span className="admin-admin-label">Status</span>
              <span
                className={`admin-admin-value status-badge ${
                  user.status ? "active" : "inactive"
                }`}
              >
                {user.status ? "Active" : "Inactive"}
              </span>
            </div>
          </div>
        </div>
        {user.creator && (
          <button className="admin-admin-delete-btn" onClick={handleDelete}>
            Delete User
          </button>
        )}
        <Button
          onClick={handleLogout}
          className="logout-button p-button"
          icon="pi pi-sign-out"
        />
      </div>

      <div className="admin-admin-main-content">
        <button
          onClick={() => navigate(-1)}
          style={{
            backgroundColor: "rgba(160, 16, 193, 0.42)",
            color: "white",
            width: "50px",
            height: "50px",
            border: "2px solid rgba(194, 12, 170, 0.66)",
            borderRadius: "50%",
            fontWeight: 600,
            cursor: "pointer",
            boxShadow: "0 4px 8px rgba(0, 0, 0, 0.3)",
            transition: "background-color 0.2s ease",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            fontSize: "1.2rem",
            marginTop: "0",
          }}
          onMouseOver={(e) =>
            (e.currentTarget.style.backgroundColor = "rgba(161, 27, 143, 0.42)")
          }
          onMouseOut={(e) =>
            (e.currentTarget.style.backgroundColor = "rgba(160, 16, 193, 0.42)")
          }
        >
          <i className="pi pi-arrow-left"></i>
        </button>
        <TabView scrollable className="admin-admin-tabview">
          <TabPanel header="Created Accounts">
            <motion.div
              key="activity"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
            >
              <CreatedAdmins userId={user.id} />
            </motion.div>
          </TabPanel>

          <TabPanel header="Edit Account">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, ease: "easeOut" }}
              style={{ width: "100%" }}
            >
              <div style={{ width: "100%" }}>
                <AdminEditUser
                  userData={user}
                  onSave={() => fetchUserDetails()}
                />
              </div>
            </motion.div>
          </TabPanel>
        </TabView>
      </div>
    </motion.div>
  );
};

export default AdminAccount;
