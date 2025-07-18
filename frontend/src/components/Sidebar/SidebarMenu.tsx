import React, { useState, useEffect } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Sidebar } from "primereact/sidebar";
import { Button } from "primereact/button";
import {
  FaFileAlt,
  FaBars,
  FaFileUpload,
} from "react-icons/fa";
import { ImHome } from "react-icons/im";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import "./SidebarMenu.css";

const SidebarMenu = () => {
  const [isSmallScreen, setIsSmallScreen] = useState(window.innerWidth <= 768);
  const [visible, setVisible] = useState(!isSmallScreen);

  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const handleResize = () => {
      const isSmall = window.innerWidth <= 768;
      setIsSmallScreen(isSmall);
      setVisible(!isSmall);
    };
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const menuItems = [
    { label: "Home", icon: <ImHome />, path: "/" },
    { label: "My Files", icon: <FaFileAlt />, path: "/myFiles" },
    { label: "Create flow", icon: <FaFileUpload />, path: "/upload" },
  ];

  return (
    <>
      {isSmallScreen && (
        <Button
          icon={<FaBars />}
          className="sidebar-toggle"
          onClick={() => setVisible(true)}
        />
      )}

      <Sidebar
        visible={visible}
        onHide={() => isSmallScreen && setVisible(false)}
        className={`custom-sidebar ${isSmallScreen ? "hidden" : ""}`}
        modal={false}
        dismissable={false}
      >
        <nav className="sidebar-nav">
          {menuItems.map((item) => (
            <Button
              key={item.path}
              label={item.label}
              icon={item.icon}
              className={`sidebar-btn ${
                location.pathname === item.path ? "p-highlight" : ""
              }`}
              onClick={() => {
                navigate(item.path);
                if (isSmallScreen) setVisible(false);
              }}
            />
          ))}
        </nav>
      </Sidebar>
    </>
  );
};

export default SidebarMenu;
