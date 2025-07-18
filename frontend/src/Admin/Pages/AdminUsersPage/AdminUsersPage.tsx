import React, { useEffect, useState } from "react";
import { TabView, TabPanel } from "primereact/tabview";
import { motion } from "framer-motion";
import apiClient from "../../../Utils/ApiClient.tsx";
import AdminUserList from "../../AdminUsersList/AdminUsersList.tsx";
import "./AdminUsersPage.css";
import AdminCreateAdmin from "../../AdminCreateAdmin/AdminCreateAdmin.tsx";

const AdminUsersPage = () => {
  const [allUsers, setAllUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response = await apiClient.get("/admin/users");
        console.log("Fetched users:", response.data);
        setAllUsers(response.data);
      } catch (error) {
        console.error("Failed to fetch users", error);
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, []);

  const regularUsers = allUsers.filter(
    (user) =>
      typeof user.role === "string" && user.role.toLowerCase() === "user"
  );

  const admins = allUsers.filter(
    (user) =>
      typeof user.role === "string" && user.role.toLowerCase() === "admin"
  );

  return (
    <div className="main-content-users">
      <TabView style={{ width: "100%" }}>
        <TabPanel
          header={
            <span>
              <i className="pi pi-users" style={{ marginRight: "8px" }} />
              Manage Users
            </span>
          }
        >
          <motion.div
            key="users"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.5 }}
          >
            {!loading ? (
              <AdminUserList
                users={regularUsers}
                onDeleteUser={(userId) =>
                  console.log("Delete user with ID:", userId)
                }
              />
            ) : (
              <p>Loading users...</p>
            )}
          </motion.div>
        </TabPanel>

        <TabPanel
          header={
            <span>
              <i className="pi pi-user" style={{ marginRight: "8px" }} />
              Manage Administrators
            </span>
          }
        >
          <motion.div
            key="admins"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.5 }}
          >
            {!loading ? (
              <AdminUserList
                users={admins}
                onDeleteUser={(userId) =>
                  console.log("Delete admin with ID:", userId)
                }
              />
            ) : (
              <p>Loading administrators...</p>
            )}
          </motion.div>
        </TabPanel>

        <TabPanel
          header={
            <span>
              <i className="pi pi-user-plus" style={{ marginRight: "8px" }} />
              Create Administrator
            </span>
          }
        >
          <motion.div
            key="create"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.5 }}
          >
            <AdminCreateAdmin
              onCreate={(newAdmin) => console.log("Created admin:", newAdmin)}
            />
          </motion.div>
        </TabPanel>
      </TabView>
    </div>
  );
};

export default AdminUsersPage;
