import React from "react";
import { Outlet } from "react-router-dom";
import SidebarMenu from "../Sidebar/SidebarMenu.tsx";
import Navbar from "../Navbar/Navbar.tsx";
import "./MainLayout.css";

const MainLayout = ({ children }) => {
  return (
    <div
      className="homepage-layout scroll-container"
      style={{
        backgroundImage: `url(${process.env.PUBLIC_URL + "bg2.jpg"})`,
      }}
    >
      <div className="homepage-sidebar">
        <SidebarMenu />
      </div>

      <div className="homepage-main">
        <div className="homepage-navbar">
          <Navbar />
        </div>
        <div className="homepage-content">
          <Outlet />
        </div>
      </div>
    </div>
  );
};

export default MainLayout;
