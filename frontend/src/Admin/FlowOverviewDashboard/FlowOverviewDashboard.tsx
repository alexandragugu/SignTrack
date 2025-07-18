import React from "react";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import "./FlowOverviewDashboard.css";

const FlowOverviewDashboard = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const statisticsItems = [
    { key: "signatures", label: "Signatures", icon: "pi pi-pencil" },
    { key: "approvals", label: "Approvals", icon: "pi pi-check" },
    { key: "views", label: "Views", icon: "pi pi-eye" },
    { key: "declines", label: "Declines", icon: "pi pi-ban" },
  ];

  const handleNavigation = (section, key) => {
    if (section === "system") {
      navigate(`/admin/metrics/flows/${key}`);
    } else if (section === "stats") {
      navigate(`/admin/metrics/flows/statistics/${key}`);
    }
  };

  return (
    <div className="flow-dashboard-wrapper">
      <div className="flow-sidebar-static">
        <div className="sidebar-section">
          <h4 className="sidebar-title">
            <i
              className="pi pi-chart-bar"
              style={{ marginRight: "0.5rem" }}
            ></i>
            Statistics
          </h4>
          <ul className="sidebar-list">
            {statisticsItems.map((item) => (
              <li
                key={item.key}
                className={location.pathname.includes(item.key) ? "active" : ""}
                onClick={() => handleNavigation("stats", item.key)}
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

export default FlowOverviewDashboard;
