import React from "react";
import config from "../../Config/config.tsx";
import SignatureStatsAccordion from "../SignatureStatsAccordion/SignatureStatsAccordion.tsx";

const panelStyle = {
  border: "none",
  borderRadius: "10px",
  margin: "0.5rem",
  boxShadow: "0 0 12px rgba(177, 8, 228, 0.25)",
  flex: "1 1 32%",
  minWidth: "300px",
};

const SignaturesStatistics = () => {
  return (
    <div className="signature-statistics">
      <div
        style={{
          padding: "1rem",
          backgroundColor: "rgba(0, 0, 0, 0.1)",
          color: "white",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            gap: "1rem",
            flexWrap: "wrap",
          }}
        >
          <iframe
            src={`${config.GRAFANA_URL}d-solo/cejyznoruatxcf/signatures?orgId=1&from=1745558566821&to=1745580166821&timezone=browser&panelId=3&__feature.dashboardSceneSolo`}
            width="32%"
            height="300"
            style={panelStyle}
            title="Signature Type Distribution"
          ></iframe>

          <iframe
            src={`${config.GRAFANA_URL}d-solo/cejyznoruatxcf/signatures?orgId=1&from=1745558566821&to=1745580166821&timezone=browser&panelId=1&__feature.dashboardSceneSolo`}
            width="32%"
            height="300"
            style={panelStyle}
            title="Visible vs Invisible Signatures"
          ></iframe>

          <iframe
            src={`${config.GRAFANA_URL}d-solo/cejyznoruatxcf/signatures?orgId=1&from=1745558566821&to=1745580166821&timezone=browser&panelId=2&__feature.dashboardSceneSolo`}
            width="32%"
            height="300"
            style={panelStyle}
            title="Signature Profile Distribution"
          ></iframe>
        </div>
      </div>
      <SignatureStatsAccordion />
    </div>
  );
};

export default SignaturesStatistics;
