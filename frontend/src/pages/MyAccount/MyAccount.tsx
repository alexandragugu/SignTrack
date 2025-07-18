import React, { useEffect, useState, useRef } from "react";
import { useDispatch } from "react-redux";
import { TabView, TabPanel } from "primereact/tabview";
import { Button } from "primereact/button";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import "./MyAccount.css";
import apiClient from "./../../Utils/ApiClient.tsx";
import { Divider } from "primereact/divider";
import UserEditForm from "../../components/UserEditForm/UserEditForm.tsx";
import config from "../../Config/config.tsx";
import { logout } from "../../Config/authSlice.js";
import { motion } from "framer-motion";
import UserActivity from "../../Admin/UsersComponets/UserActivity/UserActivity.tsx";
import UserSignatures from "../../Admin/UsersComponets/UserSignatures/UserSignatures.tsx";

const MyAccount = () => {
  const [user, setUser] = useState(null);
  const dispatch = useDispatch();
  const toastRef = useRef({});

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

  const handleDeleteAccount = async () => {
    console.log("Deleting account...");
    try {
      const response = await apiClient.delete("/users/account");
      const id_token = response.data.id_token;
      if (response.status === 200) {
        console.log("Account deleted successfully");
        handleLogoutDelete(id_token);
      }
    } catch (error) {
      console.error("Failed to delete account", error);
    }
  };

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

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const response = await apiClient.get("/users/details");
        console.log(response.data);
        setUser(response.data);
      } catch (error) {
        console.error("Failed to fetch user data", error);
      }
    };
    fetchUserData();
  }, []);

  if (!user) {
    return <div className="user-profile-container">Loading...</div>;
  }

  const formatDate = (arr) => {
    if (!arr || arr.length < 3) return "-";

    const [year, month, day, hour = 0, minute = 0, second = 0, millis = 0] =
      arr;

    const date = new Date(year, month - 1, day, hour, minute, second, millis);

    return date.toLocaleString("ro-RO", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formatTimestamp = (timestamp) => {
    if (!timestamp || !Array.isArray(timestamp) || timestamp.length < 3)
      return "-";

    const [year, month, day, hour = 0, minute = 0, second = 0] = timestamp;

    const date = new Date(year, month - 1, day, hour, minute, second);
    if (isNaN(date.getTime())) return "-";

    return date.toLocaleString("ro-RO", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const sidebarVariants = {
    hidden: { opacity: 0, x: -40 },
    visible: {
      opacity: 1,
      x: 0,
      transition: {
        staggerChildren: 0.1,
        delayChildren: 0.2,
        type: "spring",
        duration: 0.6,
      },
    },
  };

  const cardVariants = {
    hidden: { opacity: 0, y: 30 },
    visible: {
      opacity: 1,
      y: 0,
      transition: { type: "spring", stiffness: 60, damping: 15 },
    },
  };

  return (
    <div className="user-profile-container">
      <motion.div
        className="sidebar-account"
        variants={sidebarVariants}
        initial="hidden"
        animate="visible"
      >
        <div className="avatar">
          <i className="pi pi-user" />
        </div>
        <span className="info-username">{user.username}</span>
        <Divider className="divider" align="center">
          <i className="pi pi-id-card" />
        </Divider>

        <div className="user-info-grid">
          {[
            {
              icon: "pi-user",
              label: "Username",
              value: user.username,
            },
            {
              icon: "pi-id-card",
              label: "Full Name",
              value: `${user.firstName} ${user.lastName}`,
            },
            {
              icon: "pi-envelope",
              label: "Email",
              value: user.email,
              title: user.email,
              className: "email-ellipsis",
            },
            {
              icon: "pi-calendar",
              label: "Registration date",
              value: formatDate(user.registrationDate),
            },
            {
              icon: "pi-clock",
              label: "Last login",
              value: formatTimestamp(user.lastLogin),
            },
          ].map((item, i) => (
            <motion.div
              key={i}
              className="user-info-card"
              variants={cardVariants}
            >
              <i className={`pi ${item.icon}`}></i>
              <div className="info-text">
                <span className="info-label">{item.label}</span>
                <span
                  className={`info-value ${item.className || ""}`}
                  title={item.title || ""}
                >
                  {item.value}
                </span>
              </div>
            </motion.div>
          ))}
        </div>

        <Divider className="divider" align="center">
          <i className="pi pi-cog" />
        </Divider>

        <Button
          onClick={handleDeleteAccount}
          className="delete-button-account p-button"
          icon="pi pi-trash"
        />
        <Button
          onClick={handleLogout}
          className="logout-button p-button"
          icon="pi pi-sign-out"
        />
      </motion.div>

      <div className="main-content">
        <TabView>
          <TabPanel header="Activity">
            <motion.div
              key="activity"
              initial={{ opacity: 0, y: 40 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -40 }}
            >
              <UserActivity userId={user.id} />
            </motion.div>
          </TabPanel>
          <TabPanel header="Signatures">
            <motion.div
              key="signatures"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              transition={{ duration: 0.6 }}
            >
              <UserSignatures userId={user.id} />
            </motion.div>
          </TabPanel>
          <TabPanel header="Account Settings">
            <motion.div
              key="settings"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              transition={{ duration: 0.6 }}
            >
              <p className="tab-text">
                <UserEditForm
                  userData={user}
                  onSave={(updatedData) =>
                    setUser((prev) => ({
                      ...prev,
                      ...updatedData,
                    }))
                  }
                />
              </p>
            </motion.div>
          </TabPanel>
        </TabView>
      </div>
    </div>
  );
};

export default MyAccount;
