import React, { useState, useRef, useMemo, useEffect } from "react";
import { InputText } from "primereact/inputtext";
import { Password } from "primereact/password";
import { Dropdown } from "primereact/dropdown";
import { Button } from "primereact/button";
import CustomToast from "../../components/CustomToast/CustomToast.tsx";
import "./AdminEditUser.css";
import apiClient from "../../Utils/ApiClient.tsx";

const AdminEditUser = ({ userData, userId, onSave }) => {
  const [formData, setFormData] = useState({
    username: userData.username || "",
    email: userData.email || "",
    firstName: userData.firstName || "",
    lastName: userData.lastName || "",
    password: "",
    confirmPassword: "",
    role: userData.role.includes("Admin") ? "Admin" : "User",
  });

  const toastRef = useRef({});

  const isFormModified = useMemo(() => {
    return (
      formData.username !== userData.username ||
      formData.email !== userData.email ||
      formData.firstName !== userData.firstName ||
      formData.lastName !== userData.lastName ||
      formData.password.trim() !== "" ||
      formData.confirmPassword.trim() !== "" ||
      formData.role !== (userData.role.includes("Admin") ? "Admin" : "User")
    );
  }, [formData, userData]);

  const handleChange = (e, field) => {
    setFormData((prev) => ({
      ...prev,
      [field]: e.target.value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (formData.password !== formData.confirmPassword) {
      toastRef.current?.showError("Passwords do not match!");
      return;
    }

    const requestBody = {
      userId: userId,
      username: formData.username,
      email: formData.email,
      firstName: formData.firstName,
      lastName: formData.lastName,
      role: formData.role,
      ...(formData.password.trim() && { password: formData.password }),
    };

    try {
      const response = await apiClient.put(`/admin/users/update`, requestBody);
      toastRef.current?.showSuccess("User updated successfully!");
      onSave && onSave(response.data);
    } catch (err) {
      toastRef.current?.showError("Failed to update user!");
      console.error("Error updating user:", err);
    }
  };

  const roleOptions = [
    { label: "Admin", value: "Admin" },
    { label: "User", value: "User" },
  ];

  return (
    <form className="user-edit-form" onSubmit={handleSubmit}>
      <div className="edit-section">
        <h3 className="section-title">
          <i className="pi pi-user-edit" /> Account
        </h3>

        <div className="form-group">
          <label>Username</label>
          <InputText
            value={formData.username}
            onChange={(e) => handleChange(e, "username")}
            className="input-field"
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>First Name</label>
            <InputText
              value={formData.firstName}
              onChange={(e) => handleChange(e, "firstName")}
              className="input-field"
            />
          </div>
          <div className="form-group">
            <label>Last Name</label>
            <InputText
              value={formData.lastName}
              onChange={(e) => handleChange(e, "lastName")}
              className="input-field"
            />
          </div>
        </div>

        <div className="form-group">
          <label>Email</label>
          <InputText
            type="email"
            value={formData.email}
            onChange={(e) => handleChange(e, "email")}
            className="input-field"
          />
        </div>
      </div>

      <div className="edit-section">
        <h3 className="section-title">
          <i className="pi pi-shield" /> Security
        </h3>

        <div className="form-group-password password-input-wrapper">
          <label>New Password</label>
          <Password
            value={formData.password}
            onChange={(e) => handleChange(e, "password")}
            toggleMask
            className="input-field"
            feedback={false}
          />
        </div>

        <div className="form-group-password password-input-wrapper">
          <label>Confirm New Password</label>
          <Password
            value={formData.confirmPassword}
            onChange={(e) => handleChange(e, "confirmPassword")}
            toggleMask
            className="input-field"
            feedback={false}
          />
        </div>
      </div>

      <Button
        label="Save Changes"
        icon="pi pi-check"
        className="save-button p-button"
        type="submit"
        disabled={!isFormModified}
      />

      <CustomToast ref={toastRef} />
    </form>
  );
};

export default AdminEditUser;
