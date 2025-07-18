import React, { useEffect, useState } from "react";
import { TabView, TabPanel } from "primereact/tabview";
import { motion } from "framer-motion";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import "./AdminMetricsPage.css";

const AdminMetricsPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [activeIndex, setActiveIndex] = useState(0);

  useEffect(() => {
    if (location.state?.tab === "flows") {
      setActiveIndex(1);
    } else {
      setActiveIndex(0);
    }
  }, [location.state]);

  const handleTabChange = (e) => {
    setActiveIndex(e.index);

    if (e.index === 0) {
      navigate("/admin/metrics/flows/statistics/signatures");
    } else if (e.index === 1) {
      navigate("/admin/metrics/user-activity");
    }
  };

  return (
    <div className="admin-metrics-page">
      <TabView
        className="metrics-tabview"
        activeIndex={activeIndex}
        onTabChange={handleTabChange}
        style={{ width: "100%", height: "100%" }}
      >
        <TabPanel
          header={
            <span>
              <i className="pi pi-sitemap" style={{ marginRight: "8px" }} />
              Flows
            </span>
          }
        >
          <motion.div
            className="metrics-tab-content"
            initial={{ opacity: 0, x: -100 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: 100 }}
            transition={{ duration: 0.4, ease: "easeInOut" }}
          >
            <Outlet />
          </motion.div>
        </TabPanel>
        <TabPanel
          header={
            <span>
              <i className="pi pi-chart-line" style={{ marginRight: "8px" }} />
              User Activity
            </span>
          }
        >
          <motion.div
            className="metrics-tab-content"
            initial={{ opacity: 0, x: -100 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: 100 }}
            transition={{ duration: 0.4, ease: "easeInOut" }}
          >
            <Outlet />
          </motion.div>
        </TabPanel>
      </TabView>
    </div>
  );
};

export default AdminMetricsPage;
