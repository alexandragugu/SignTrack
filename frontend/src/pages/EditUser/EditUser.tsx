import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import apiClient from "../../Utils/ApiClient.tsx";

const EditUser = () => {
  const { userId } = useParams();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    role: "",
  });

  useEffect(() => {
    apiClient
      .get(`/admin/users/${userId}`)
      .then((res) => setFormData(res.data));
  }, [userId]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    await apiClient.put(`/admin/users/${userId}`, formData);
    navigate(`/admin/users/${userId}`);
  };

  return (
    <form onSubmit={handleSubmit}>
      {Object.keys(formData).map((key) => (
        <div key={key}>
          <label>{key}</label>
          <input name={key} value={formData[key]} onChange={handleChange} />
        </div>
      ))}
      <button type="submit">Save</button>
    </form>
  );
};

export default EditUser;
