import React, { useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import apiClient from "../../Utils/ApiClient.tsx";
import "./AdminUsersList.css";
import { useConfirm } from "./../../components/GlobalConfirmDialog/GlobalConfirmDialog.tsx";
import CustomToast from "../../components/CustomToast/CustomToast.tsx";
const AdminUserList = ({ users: initialUsers, onDeleteUser }) => {
  const [localUsers, setLocalUsers] = useState(initialUsers);
  const navigate = useNavigate();
  const confirm = useConfirm();
  const [selectedUsers, setSelectedUsers] = useState([]);
  const toastRef = useRef({});

  console.log("AdminUserList users:", initialUsers);

  const handleDelete = async (userId, role) => {
    confirm({
      message: "Are you sure you want to delete this user?",
      onAccept: async () => {
        try {
          const endpoint =
            role === "Admin"
              ? `/admin/users/delete-admin/${userId}`
              : `/admin/users/delete/${userId}`;

          await apiClient.delete(endpoint);

          setLocalUsers((prevUsers) =>
            prevUsers.filter((user) => user.id !== userId)
          );
          if (onDeleteUser) onDeleteUser(userId);
        } catch (err) {
          alert("Failed to delete user");
        }
      },
    });
  };

  const handleDetails = (user) => {
    const targetPath =
      user.role === "User"
        ? "/admin/users/details"
        : "/admin/users/details-admin";

    navigate(targetPath, {
      state: { userId: user.id },
    });
  };
  const [filters, setFilters] = useState({
    global: { value: null, matchMode: "contains" },
    username: { value: null, matchMode: "contains" },
    email: { value: null, matchMode: "contains" },
    role: { value: null, matchMode: "contains" },
    firstName: { value: null, matchMode: "contains" },
    lastName: { value: null, matchMode: "contains" },
  });

  const handleBulkDelete = () => {
    if (!selectedUsers.length) return;

    const hasAdminUser = selectedUsers.some((user) => user.role === "Admin");

    confirm({
      message: `Are you sure you want to delete ${selectedUsers.length} user(s)?`,
      header: "Confirm Bulk Delete",
      icon: "pi pi-exclamation-triangle",
      acceptClassName: "p-button-danger",
      onAccept: async () => {
        try {
          const userIds = selectedUsers.map((user) => user.id);

          const endpoint = hasAdminUser
            ? "/admin/users/delete-multiple/admins"
            : "/admin/users/delete-multiple";

          const response = await apiClient.post(endpoint, userIds);

          setLocalUsers((prev) =>
            prev.filter((user) => !userIds.includes(user.id))
          );
          setSelectedUsers([]);

          toastRef.current?.showSuccess(
            response.data.message || "Users deleted successfully."
          );

          setTimeout(() => {
            window.location.reload();
          }, 1000);
        } catch (err) {
          const errorMessage =
            err?.response?.data?.errorMessage || "Failed to delete users.";
          toastRef.current?.showError(errorMessage);
        }
      },
    });
  };

  return (
    <div className="admin-user-list">
      {selectedUsers.length > 0 &&
        !selectedUsers.some(
          (user) => user.role === "Admin" && !user.isCreator
        ) && (
          <button
            className="bulk-delete-button"
            onClick={handleBulkDelete}
            style={{
              marginBottom: "1rem",
              backgroundColor: "#d32f2f",
              color: "white",
              padding: "0.5rem 1rem",
              border: "none",
              borderRadius: "4px",
              cursor: "pointer",
            }}
          >
            <i className="pi pi-trash" />
          </button>
        )}
      <DataTable
        value={localUsers}
        paginator
        rows={4}
        filters={filters}
        onFilter={(e) => setFilters(e.filters)}
        className="custom-table-users"
        emptyMessage="No users found."
        style={{ width: "100%" }}
        selection={selectedUsers}
        onSelectionChange={(e) => setSelectedUsers(e.value)}
        selectionMode="multiple"
        dataKey="id"
      >
        <Column selectionMode="multiple" headerStyle={{ width: "3em" }} />
        <Column
          field="username"
          header="Username"
          filter
          filterPlaceholder="Search by username"
          sortable
          style={{ minWidth: "10rem" }}
        />
        <Column
          field="email"
          header="Email"
          filter
          filterPlaceholder="Search by email"
          sortable
          body={(rowData) => (
            <span
              title={rowData.email}
              style={{
                display: "inline-block",
                width: "100%",
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
              }}
            >
              {rowData.email}
            </span>
          )}
        />
        <Column
          field="firstName"
          header="First Name"
          filter
          filterPlaceholder="Search by first name"
          style={{ minWidth: "8rem" }}
        />
        <Column
          field="lastName"
          header="Last Name"
          filter
          filterPlaceholder="Search by last name"
          style={{ minWidth: "8rem" }}
        />
        <Column
          header="Details"
          body={(rowData) => (
            <button
              className="details-button"
              onClick={() => handleDetails(rowData)}
              title="Details"
            >
              <i className="pi pi-arrow-right" />
            </button>
          )}
          style={{ width: "4rem", textAlign: "center" }}
        />
        <Column
          header="Delete"
          body={(rowData) => {
            if (rowData.role === "Admin" && !rowData.isCreator) return null;

            return (
              <div
                style={{
                  display: "flex",
                  justifyContent: "center",
                  alignItems: "center",
                  height: "100%",
                }}
              >
                <button
                  className="delete-button"
                  onClick={() => handleDelete(rowData.id, rowData.role)}
                  title="Delete"
                >
                  <i className="pi pi-trash" />
                </button>
              </div>
            );
          }}
          style={{ width: "4rem", textAlign: "center" }}
        />
      </DataTable>
      <CustomToast ref={toastRef} />
    </div>
  );
};

export default AdminUserList;
