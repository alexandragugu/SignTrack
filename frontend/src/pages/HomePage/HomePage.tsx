import React, { useState } from "react";
import "./HomePage.css";
import "primereact/resources/themes/saga-purple/theme.css";
import TabFiles from "../../components/TabFiles/TabFiles.tsx";
import UserWeekPanel from "../../components/UserWeekPanel/UserWeekPanel.tsx";
import HomeChart from "../../components/HomeChart/HomeChart.tsx";

const HomePage = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [selectedDocument, setSelectedDocument] = useState(null);

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  return (
    <div>
      <div>
        <HomeChart />
      </div>
    </div>
  );
};

export default HomePage;
