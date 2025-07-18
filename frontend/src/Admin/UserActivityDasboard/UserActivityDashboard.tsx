import React from "react";

const panelStyle = {
  border: "none",
  borderRadius: "8px",
  margin: "0.5rem",
  boxShadow: "0 0 12px rgba(0,0,0,0.3)",
};

const UserActivityDashboard = () => {
  return (
    <div
      style={{
        padding: "1rem",
        backgroundColor: "rgba(0, 0, 0, 0.25)",
        color: "white",
      }}
    >
      <h2 style={{ marginBottom: "1rem", color: "white" }}>
        {" "}
        User Activity Dashboard
      </h2>

      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <iframe
          src="http://localhost:4000/d-solo/fejrnd4mam0w0d/statistics?orgId=1&from=1742845408817&to=1745433808817&timezone=browser&panelId=8&__feature.dashboardSceneSolo"
          width="33%"
          height="100"
          style={panelStyle}
        ></iframe>
        <iframe
          src="http://localhost:4000/d-solo/fejrnd4mam0w0d/statistics?orgId=1&from=1742845408817&to=1745433808817&timezone=browser&panelId=10&__feature.dashboardSceneSolo"
          width="33%"
          height="100"
          style={panelStyle}
        ></iframe>
        <iframe
          src="http://localhost:4000/d-solo/fejrnd4mam0w0d/statistics?orgId=1&from=1742845408817&to=1745433808817&timezone=browser&panelId=11&__feature.dashboardSceneSolo"
          width="33%"
          height="100"
          style={panelStyle}
        ></iframe>
      </div>

      <div style={{ display: "flex", marginTop: "1rem" }}>
        <iframe
          src="http://localhost:4000/d-solo/fejrnd4mam0w0d/statistics?orgId=1&from=1742845408817&to=1745433808817&timezone=browser&panelId=5&__feature.dashboardSceneSolo"
          width="50%"
          height="300"
          style={panelStyle}
        ></iframe>
        <iframe
          src="http://localhost:4000/d-solo/fejrnd4mam0w0d/statistics?orgId=1&from=1742845408817&to=1745433808817&timezone=browser&panelId=4&__feature.dashboardSceneSolo"
          width="50%"
          height="300"
          style={panelStyle}
        ></iframe>
      </div>

      <div style={{ display: "flex", marginTop: "1rem" }}>
        <iframe
          src="http://localhost:4000/d-solo/fejrnd4mam0w0d/statistics?orgId=1&from=1742845408817&to=1745433808817&timezone=browser&panelId=3&__feature.dashboardSceneSolo"
          width="60%"
          height="350"
          style={panelStyle}
        ></iframe>
        <iframe
          src="http://localhost:4000/d-solo/fejrnd4mam0w0d/statistics?orgId=1&from=1742845408817&to=1745433808817&timezone=browser&panelId=6&__feature.dashboardSceneSolo"
          width="40%"
          height="350"
          style={panelStyle}
        ></iframe>
      </div>
    </div>
  );
};

export default UserActivityDashboard;
