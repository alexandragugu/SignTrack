import React, { useState, useRef } from "react";
import { useEffect, useMemo } from "react";
import { InputText } from "primereact/inputtext";
import { Password } from "primereact/password";
import { Button } from "primereact/button";
import "./UserEditForm.css";
import apiClient from "../../Utils/ApiClient.tsx";
import CustomToast from "../CustomToast/CustomToast.tsx";

const UserEditForm = ({ userData, onSave }) => {
  const [formData, setFormData] = useState({
    username: userData.username || "",
    email: userData.email || "",
    firstName: userData.firstName || "",
    lastName: userData.lastName || "",
    password: "",
    confirmPassword: "",
  });

  const requestBody = useRef({});
  const toastRef = useRef({});

  const isFormModified = useMemo(() => {
    return (
      formData.username !== userData.username ||
      formData.email !== userData.email ||
      formData.firstName !== userData.firstName ||
      formData.lastName !== userData.lastName ||
      formData.password.trim() !== "" ||
      formData.confirmPassword.trim() !== ""
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

    const passwordPattern =
      /^(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).+$/;
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const usernamePattern = /^[a-zA-Z][a-zA-Z0-9-_@]*$/;
    const lettersOnlyPattern = /^[a-zA-Z]+$/;
    const startsWithCapitalPattern = /^[A-Z]/;

    if (!lettersOnlyPattern.test(formData.firstName)) {
      toastRef.current?.showError("First name must contain only letters.");
      return;
    }

    if (!startsWithCapitalPattern.test(formData.firstName)) {
      toastRef.current?.showError(
        "First name must start with a capital letter."
      );
      return;
    }

    if (!lettersOnlyPattern.test(formData.lastName)) {
      toastRef.current?.showError("Last name must contain only letters.");
      return;
    }

    if (!startsWithCapitalPattern.test(formData.lastName)) {
      toastRef.current?.showError(
        "Last name must start with a capital letter."
      );
      return;
    }

    if (!usernamePattern.test(formData.username)) {
      toastRef.current?.showError(
        "Username must start with a letter and can contain only letters, numbers, '-', '_' or '@'."
      );
      return;
    }

    if (!emailPattern.test(formData.email)) {
      toastRef.current?.showError("Please enter a valid email address.");
      return;
    }

    if (formData.password && !passwordPattern.test(formData.password)) {
      toastRef.current?.showError(
        "Password must contain at least one special character and one digit."
      );
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      toastRef.current?.showError("Passwords do not match.");
      return;
    }

    const requestBody = {
      username: formData.username,
      email: formData.email,
      firstName: formData.firstName,
      lastName: formData.lastName,
      ...(formData.password.trim() && { password: formData.password }),
    };

    console.log("req body", requestBody);

    try {
      const response = await apiClient.put("users/update", requestBody);

      const data = response.data;
      console.log("User updated successfully:", data);
      toastRef.current?.showSuccess("Profile updated successfully!");
    } catch (err) {
      toastRef.current?.showError("Failed to update profile!");
      console.error("Error updating user:", err);
    }
    console.log("Updated user:", formData);
    onSave && onSave(formData);
  };

  return (
    <form className="user-edit-form" onSubmit={handleSubmit}>
      <div className="edit-section">
        <h3 className="section-title">
          <i className="pi pi-user-edit" /> Profil
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
          <i className="pi pi-shield" /> Securiy
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

export default UserEditForm;
