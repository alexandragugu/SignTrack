import React, { useState, useRef, useEffect } from "react";
import config from "../../Config/config.tsx";

const panelStyle = {
  border: "none",
  borderRadius: "10px",
  margin: "0.5rem",
  boxShadow: "0 0 12px rgba(177, 8, 228, 0.25)",
  backgroundColor: "transparent",
};

const DeclineDashboard = () => {
  const [loadedCount, setLoadedCount] = useState(0);
  const [allLoaded, setAllLoaded] = useState(false);
  const iframeRefs = useRef([]);

  const totalIframes = 5;

  useEffect(() => {
    if (loadedCount === totalIframes) {
      setAllLoaded(true);
    }
  }, [loadedCount]);

  const handleIframeLoad = (index) => {
    setLoadedCount((prev) => prev + 1);
  };

  iframeRefs.current = Array(totalIframes)
    .fill()
    .map((_, i) => iframeRefs.current[i] || React.createRef());

  return (
    <div style={{ padding: "2rem", color: "white" }}>
      {!allLoaded && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100vh",
            fontSize: "1.5rem",
          }}
        >
          Loading dashboards...
        </div>
      )}

      <div style={{ display: allLoaded ? "block" : "none" }}>
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            flexWrap: "wrap",
          }}
        >
          <iframe
            ref={iframeRefs.current[0]}
            src={`${config.GRAFANA_URL}d-solo/fek6fgbumqyo0b/declinedashboard?orgId=1&from=1743175437757&to=1745763837757&timezone=browser&showCategory=Bar%20chart&panelId=6&__feature.dashboardSceneSolo`}
            width="40%"
            height="300"
            style={panelStyle}
            title="Total To Approve"
            onLoad={() => handleIframeLoad(0)}
          ></iframe>

          <iframe
            ref={iframeRefs.current[1]}
            src={`${config.GRAFANA_URL}d-solo/fek6fgbumqyo0b/declinedashboard?orgId=1&from=1743175437757&to=1745763837757&timezone=browser&showCategory=Bar%20chart&panelId=7&__feature.dashboardSceneSolo`}
            width="55%"
            height="300"
            style={panelStyle}
            title="Total Approved"
            onLoad={() => handleIframeLoad(1)}
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
            ref={iframeRefs.current[2]}
            src={`${config.GRAFANA_URL}d-solo/fek6fgbumqyo0b/declinedashboard?orgId=1&from=1743175437757&to=1745763837757&timezone=browser&showCategory=Bar%20chart&panelId=5&__feature.dashboardSceneSolo`}
            width="40%"
            height="300"
            style={panelStyle}
            title="Pending Approvals"
            onLoad={() => handleIframeLoad(2)}
          ></iframe>

          <iframe
            ref={iframeRefs.current[3]}
            src={`${config.GRAFANA_URL}d-solo/fek6fgbumqyo0b/declinedashboard?orgId=1&from=now-30d&to=now&timezone=browser&showCategory=Bar%20chart&panelId=4&__feature.dashboardSceneSolo`}
            width="55%"
            height="300"
            style={panelStyle}
            title="Top Approvers"
            onLoad={() => handleIframeLoad(3)}
          ></iframe>
        </div>

        <div style={{ marginTop: "2rem" }}>
          <iframe
            ref={iframeRefs.current[4]}
            src={`${config.GRAFANA_URL}d-solo/fek6fgbumqyo0b/declinedashboard?orgId=1&from=1743175437757&to=1745763837757&timezone=browser&showCategory=Bar%20chart&panelId=2&__feature.dashboardSceneSolo`}
            width="100%"
            height="350"
            style={panelStyle}
            title="Approval Trend Last 30 Days"
            onLoad={() => handleIframeLoad(4)}
          ></iframe>
        </div>
      </div>
    </div>
  );
};

export default DeclineDashboard;
