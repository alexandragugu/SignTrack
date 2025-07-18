import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./AdminUserDetails.css";
import apiClient from "../../../Utils/ApiClient.tsx";
import { useLocation } from "react-router-dom";
import { TabPanel, TabView } from "primereact/tabview";
import { motion } from "framer-motion";

import AdminEditUser from "../../AdminEditUser/AdminEditUser.tsx";
import UserActivity from "../../UsersComponets/UserActivity/UserActivity.tsx";
import UserFlows from "../../UsersComponets/UserFlows/UserFlows.tsx";
import UserSignatures from "../../UsersComponets/UserSignatures/UserSignatures.tsx";
import { useConfirm } from "../../../components/GlobalConfirmDialog/GlobalConfirmDialog.tsx";

const AdminUserDetails = () => {
  const location = useLocation();
  const { userId } = location.state || {};
  const navigate = useNavigate();

  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const confirm = useConfirm();

  useEffect(() => {
    fetchUserDetails();
  }, [userId]);

  const fetchUserDetails = async () => {
    try {
      const response = await apiClient.get(`/admin/users/${userId}`);

      console.log(response.data);
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
      setUser(response.data);
      setLoading(false);
    } catch (err) {
      setError("Failed to fetch user details");
      setLoading(false);
    }
  };

  const handleDelete = () => {
    confirm({
      message: "Are you sure you want to delete this user?",
      onAccept: async () => {
        try {
          await apiClient.delete(`/admin/users/delete/${userId}`);
          navigate("/admin/users");
        } catch (err) {
          alert("Failed to delete user");
        }
      },
    });
  };

  if (loading)
    return <p className="loading-message">Loading user details...</p>;
  if (error) return <p className="error-message">{error}</p>;
  if (!user) return <p className="error-message">User not found</p>;

  return (
    <div className="admin-user-details">
      <div className="sidebar-account">
        <div className="avatar">
          <i className="pi pi-user" />
        </div>
        <span className="info-username">{user.username}</span>
        <div className="user-info-grid">
          <div className="user-info-card">
            <i className="pi pi-envelope"></i>
            <div className="info-text">
              <span className="info-label">Email</span>
              <span className="info-value">{user.email}</span>
            </div>
          </div>
          <div className="user-info-card">
            <i className="pi pi-calendar"></i>
            <div className="info-text">
              <span className="info-label">Registration</span>
              <span className="info-value">
                {new Date(user.registrationDate).toLocaleDateString()}
              </span>
            </div>
          </div>

          {user.role.some(
            (r) => r.toLowerCase() === "admin" || r.toLowerCase() === "user"
          ) && (
            <div className="user-info-card">
              <i className="pi pi-shield"></i>
              <div className="info-text">
                <span className="info-label">Role</span>
                <span className="info-value">
                  {user.role.includes("Admin") ? "Admin" : "User"}
                </span>
              </div>
            </div>
          )}

          <div className="user-info-card">
            <i className="pi pi-info-circle"></i>
            <div className="info-text">
              <span className="info-label">Status</span>
              <span
                className={`info-value status-badge ${
                  user.status ? "active" : "inactive"
                }`}
              >
                {user.status ? "Active" : "Inactive"}
              </span>
            </div>
          </div>
        </div>
        <button className="delete-user-btn" onClick={handleDelete}>
          Delete User
        </button>
      </div>

      <div className="main-content">
         <div
        style={{
          display: "flex",
          justifyContent: "flex-end",
          marginBottom: "1rem",
        }}
      >
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
      </div>
        <TabView scrollable className="custom-tabview">
          <TabPanel header="Activity">
            <motion.div
              key="activity"
              initial={{ opacity: 0, y: 40 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -40 }}
            >
              <UserActivity userId={userId} />
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
              <UserSignatures userId={userId} />
            </motion.div>
          </TabPanel>

          <TabPanel header="Associated Flows">
            <motion.div
              key="flows"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              transition={{ duration: 0.6 }}
            >
              <UserFlows userId={userId} />
            </motion.div>
          </TabPanel>

          <TabPanel header="Edit User">
            <motion.div
              key="edit"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              transition={{ duration: 0.6 }}
            >
              <AdminEditUser
                userData={user}
                userId={userId}
                onSave={() => fetchUserDetails()}
              />
            </motion.div>
          </TabPanel>
        </TabView>
      </div>
    </div>
  );
};

export default AdminUserDetails;
