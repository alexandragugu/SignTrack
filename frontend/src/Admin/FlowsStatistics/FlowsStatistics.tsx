import React from "react";

const panelStyle = {
  border: "none",
  borderRadius: "8px",
  margin: "0.5rem",
  boxShadow: "0 0 12px rgba(0, 0, 0, 0.28)",
};

const FlowsStatistics = () => {
  return (
    <div
      style={{
        border: "none",
        borderRadius: "10px",
        margin: "0.5rem",
        boxShadow: "0 0 12px rgba(177, 8, 228, 0.25)",
        backgroundColor: "none",
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <iframe
          src="http://localhost:4000/d-solo/dejt3qk344ni8f/files-dashboard?orgId=1&from=1745415148815&to=1745436748815&timezone=browser&panelId=1&__feature.dashboardSceneSolo"
          width="33%"
          height="100"
          style={panelStyle}
          title="Total Files"
        ></iframe>
        <iframe
          src="http://localhost:4000/d-solo/dejt3qk344ni8f/files-dashboard?orgId=1&from=1745415148815&to=1745436748815&timezone=browser&panelId=2&__feature.dashboardSceneSolo"
          width="33%"
          height="100"
          style={panelStyle}
          title="Total Actions"
        ></iframe>

        <iframe
          src="http://localhost:4000/d-solo/dejt3qk344ni8f/files-dashboard?orgId=1&from=1745415148815&to=1745436748815&timezone=browser&panelId=3&__feature.dashboardSceneSolo"
          width="33%"
          height="100"
          style={panelStyle}
          title="Active Users"
        ></iframe>
      </div>
    </div>
  );
};

export default FlowsStatistics;
