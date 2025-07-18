import React from "react";
import config from "../../Config/config.tsx";

const panelStyle = {
  border: "none",
  borderRadius: "10px",
  margin: "0.5rem",
  boxShadow: "0 0 12px rgba(177, 8, 228, 0.25)",
  backgroundColor: "transparent",
};

const ViewDashboard = () => {
  return (
    <div style={{ padding: "2rem", color: "white" }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          flexWrap: "wrap",
        }}
      >
        <iframe
          src={`${config.GRAFANA_URL}d-solo/eek635wnlrqioc/viewsdashboard?orgId=1&from=1743168394149&to=1745756794149&timezone=browser&panelId=1&__feature.dashboardSceneSolo`}
          width="30%"
          height="200"
          style={panelStyle}
          title="Total To Approve"
        ></iframe>

        <iframe
          src={`${config.GRAFANA_URL}d-solo/eek635wnlrqioc/viewsdashboard?orgId=1&from=1743168394149&to=1745756794149&timezone=browser&panelId=2&__feature.dashboardSceneSolo`}
          width="30%"
          height="200"
          style={panelStyle}
          title="Total Approved"
        ></iframe>

        <iframe
          src={`${config.GRAFANA_URL}d-solo/eek635wnlrqioc/viewsdashboard?orgId=1&from=1743168394149&to=1745756794149&timezone=browser&panelId=3&__feature.dashboardSceneSolo`}
          width="30%"
          height="200"
          style={panelStyle}
          title="Approval Rate"
        ></iframe>
      </div>

      <div
        style={{
          display: "flex",
          justifyContent: "space-evenly",
          marginTop: "2rem",
          flexWrap: "wrap",
        }}
      >
        <iframe
          src={`${config.GRAFANA_URL}d-solo/eek635wnlrqioc/viewsdashboard?orgId=1&from=1743168394149&to=1745756794149&timezone=browser&panelId=4&__feature.dashboardSceneSolo`}
          width="40%"
          height="300"
          style={panelStyle}
          title="Pending Approvals"
        ></iframe>

        <iframe
          src={`${config.GRAFANA_URL}d-solo/eek635wnlrqioc/viewsdashboard?orgId=1&from=now-30d&to=now&timezone=browser&panelId=5&__feature.dashboardSceneSolo`}
          width="55%"
          height="300"
          style={panelStyle}
          title="Top Approvers"
        ></iframe>
      </div>

      <div style={{ marginTop: "2rem" }}>
        <iframe
          src={`${config.GRAFANA_URL}d-solo/eek635wnlrqioc/viewsdashboard?orgId=1&from=1743168394149&to=1745756794149&timezone=browser&panelId=6&__feature.dashboardSceneSolo`}
          width="100%"
          height="350"
          style={panelStyle}
          title="Approval Trend Last 30 Days"
        ></iframe>
      </div>
    </div>
  );
};

export default ViewDashboard;
