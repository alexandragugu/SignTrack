import React from "react";
import { Outlet } from "react-router-dom";
import "./AdminLayout.css";
import AdminSidebar from "../AdminSidebar/AdminSidebar.tsx";
import AdminNavbar from "../AdminNavbar/AdminNavbar.tsx";

const AdminLayout = ({ children }) => {
  return (
    <div
      className="admin-homepage-layout "
      style={{
        backgroundImage: `url(${process.env.PUBLIC_URL + "bg2.jpg"})`,
      }}
    >
      <div className="admin-homepage-sidebar">
        <AdminSidebar />
      </div>
      <div className="admin-homepage-main">
        <div className="homepage-navbar">
          <AdminNavbar />
        </div>
        <div className="admin-content">
          <Outlet />
        </div>
      </div>
    </div>
  );
};

export default AdminLayout;
