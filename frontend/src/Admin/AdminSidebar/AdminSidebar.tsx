import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Sidebar } from "primereact/sidebar";
import { Button } from "primereact/button";
import { FaUsers, FaChartBar, FaBars, FaRegFilePdf } from "react-icons/fa";

import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import "./AdminSidebar.css";

const AdminSidebar = () => {
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
    { label: "Users", icon: <FaUsers />, path: "/admin/users",  navigationPath:"/admin/users" },
    {
      label: "Metrics",
      icon: <FaChartBar />,
      path: "/admin/metrics/flows/statistics/signatures",
      navigationPath:"/admin/metrics",
    },
        {
      label: "System Files",
      icon: <FaRegFilePdf />,
      path: "/admin/system/files/all",
      navigationPath:"/admin/system",
    },
  ];

  return (
    <>
      {isSmallScreen && (
        <Button
          icon={<FaBars />}
          className="sidebar-toggle-btn"
          onClick={() => setVisible(true)}
        />
      )}

      <Sidebar
        visible={visible}
        onHide={() => isSmallScreen && setVisible(false)}
        className={`custom-sidebar ${isSmallScreen ? "hidden" : ""}`}
        modal={false}
        dismissable={false}
        position="left"
      >
        <nav className="sidebar-nav">
          {menuItems.map((item) => (
            <Button
              key={item.path}
              label={item.label}
              icon={item.icon}
              className={`sidebar-btn ${
                 location.pathname.startsWith(item.navigationPath) ? "p-highlight" : ""
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

export default AdminSidebar;
