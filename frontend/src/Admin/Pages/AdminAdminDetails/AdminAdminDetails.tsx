import React, { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import "./AdminAdminDetails.css";
import apiClient from "../../../Utils/ApiClient.tsx";
import { TabPanel, TabView } from "primereact/tabview";
import { motion } from "framer-motion";
import AdminEditUser from "../../AdminEditUser/AdminEditUser.tsx";
import { useConfirm } from "../../../components/GlobalConfirmDialog/GlobalConfirmDialog.tsx";
import CreatedAdmins from "../../CreatedAdmins/CreatedAdmins.tsx";

const AdminAdminDetails = () => {
  const location = useLocation();
  const { userId } = location.state || {};
  const navigate = useNavigate();
  const confirm = useConfirm();

  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchUserDetails();
  }, [userId]);

  const fetchUserDetails = async () => {
    try {
      const response = await apiClient.get(`/admin/users/admin/${userId}`);
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
    return <p className="admin-admin-loading">Loading user details...</p>;
  if (error) return <p className="admin-admin-error">{error}</p>;
  if (!user) return <p className="admin-admin-error">User not found</p>;

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
              <CreatedAdmins userId={userId} />
            </motion.div>
          </TabPanel>

          {user.creator && (
            <TabPanel header="Edit User">
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
          )}
        </TabView>
      </div>
    </motion.div>
  );
};

export default AdminAdminDetails;
