import React from "react";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import "./System.css";

const System = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const systemFilesItems = [
    { key: "all", label: "All flows", icon: "pi pi-folder-open" },
    { key: "finished", label: "Finished", icon: "pi pi-check-circle" },
    { key: "pending", label: "Pending", icon: "pi pi-clock" },
  ];

  const handleNavigation = (section, key) => {
    if (section === "system") {
      navigate(`/admin/system/files/${key}`);
    }
  };

  return (
    <div className="flow-dashboard-wrapper">
      <div className="flow-sidebar-static">
        <div className="sidebar-section">
          <ul className="sidebar-list">
            {systemFilesItems.map((item) => (
              <li
                key={item.key}
                className={location.pathname.includes(item.key) ? "active" : ""}
                onClick={() => handleNavigation("system", item.key)}
              >
                <i className={item.icon} style={{ marginRight: "0.6rem" }}></i>
                {item.label}
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="flow-content">
        <Outlet />
      </div>
    </div>
  );
};

export default System;
