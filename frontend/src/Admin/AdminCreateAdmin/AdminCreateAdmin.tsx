import React, { useState, useRef, useMemo } from "react";
import { InputText } from "primereact/inputtext";
import { Password } from "primereact/password";
import { Button } from "primereact/button";
import CustomToast from "../../components/CustomToast/CustomToast.tsx";
import "./AdminCreateAdmin.css";
import apiClient from "../../Utils/ApiClient.tsx";
import { useNavigate } from "react-router-dom";

const AdminCreateAdmin = ({ onCreate }) => {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    firstName: "",
    lastName: "",
    password: "",
    confirmPassword: "",
  });

  const toastRef = useRef({});
  const isFormModified = useMemo(() => {
    return (
      formData.username.trim() !== "" &&
      formData.email.trim() !== "" &&
      formData.firstName.trim() !== "" &&
      formData.lastName.trim() !== "" &&
      formData.password.trim() !== "" &&
      formData.confirmPassword.trim() !== "" &&
      formData.password === formData.confirmPassword
    );
  }, [formData]);

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
      username: formData.username,
      email: formData.email,
      firstName: formData.firstName,
      lastName: formData.lastName,
      password: formData.password,
    };

    try {
      const response = await apiClient.post(`/admin/users/create`, requestBody);
      toastRef.current?.showSuccess("Administrator created successfully!");
      onCreate && onCreate(response.data);
      window.location.reload();
    } catch (err) {
      const defaultError = "Failed to create administrator!";
      const backendMessage =
        err?.response?.data?.errorMessage ||
        err?.response?.data ||
        defaultError;

      toastRef.current?.showError(backendMessage);
      console.error("Error creating admin:", err);
    }
  };

  return (
    <form className="admin-create-form" onSubmit={handleSubmit}>
      <div className="edit-section">
        <h3 className="section-title">
          <i className="pi pi-user-plus" /> Account
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
          <label>Password</label>
          <Password
            value={formData.password}
            onChange={(e) => handleChange(e, "password")}
            toggleMask
            className="input-field"
            feedback={false}
          />
        </div>

        <div className="form-group-password password-input-wrapper">
          <label>Confirm Password</label>
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
        label="Create Administrator"
        icon="pi pi-check"
        className="save-button p-button"
        type="submit"
        disabled={!isFormModified}
      />

      <CustomToast ref={toastRef} />
    </form>
  );
};

export default AdminCreateAdmin;
