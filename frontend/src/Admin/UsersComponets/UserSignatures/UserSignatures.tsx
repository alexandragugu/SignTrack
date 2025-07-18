import React from "react";
import config from "../../../Config/config.tsx";

const panelStyle = {
  border: "none",
  borderRadius: "10px",
  margin: "0.5rem",
  boxShadow: "0 0 12px rgba(72, 16, 87, 0.68)",
};

const rowStyle = {
  display: "flex",
  flexWrap: "wrap",
  gap: "1rem",
  width: "100%",
  alignItems: "center",
  justifyContent: "space-between",
};
const iframeStyle = {
  flex: "1",
  minWidth: "0",
  height: "300px",
  ...panelStyle,
};

const UserSignatures = ({ userId }) => {
  return (
    <div
      style={{
        padding: "0",
        color: "white",
        backgroundColor: "rgba(2, 2, 3, 0.61)",
      }}
    >
      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          gap: "1rem",
          width: "100%",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <iframe
          src={`${config.GRAFANA_URL}d-solo/eek9ntu5s11xce/usersignaturedashboard?orgId=1&from=1745805282752&to=1745848482752&timezone=browser&var-query0=&var-userId=${userId}&panelId=1&__feature.dashboardSceneSolo`}
          style={{
            ...iframeStyle,
            width: "35%",
            flex: "0 0 auto",
          }}
          title="Signature Types"
        ></iframe>

        <iframe
          src={`${config.GRAFANA_URL}d-solo/eek9ntu5s11xce/usersignaturedashboard?orgId=1&from=1745805282752&to=1745848482752&timezone=browser&var-query0=&var-userId=${userId}&panelId=2&__feature.dashboardSceneSolo`}
          style={{
            ...iframeStyle,
            width: "70%",
          }}
          title="Signature Positions"
        ></iframe>
      </div>

      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          gap: "1rem",
          width: "100%",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <iframe
          src={`${config.GRAFANA_URL}d-solo/eek9ntu5s11xce/usersignaturedashboard?orgId=1&from=1745805282752&to=1745848482752&timezone=browser&var-query0=&var-userId=${userId}&panelId=4&__feature.dashboardSceneSolo`}
          style={{
            ...iframeStyle,
            width: "70%",
          }}
          title="Signature Visibility"
        ></iframe>
        <iframe
          src={`${config.GRAFANA_URL}d-solo/eek9ntu5s11xce/usersignaturedashboard?orgId=1&from=1745805282752&to=1745848482752&timezone=browser&var-query0=&var-userId=${userId}&panelId=3&__feature.dashboardSceneSolo`}
          style={{
            ...iframeStyle,
            width: "35%",
            flex: "0 0 auto",
          }}
          title="Signature Profiles"
        ></iframe>
      </div>
    </div>
  );
};

export default UserSignatures;
