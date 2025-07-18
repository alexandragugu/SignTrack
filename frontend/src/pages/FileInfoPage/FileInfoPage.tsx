import React, { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import "./FileInfoPage.css";
import PDFViewer from "./../../components/PdfViewer/PdfViewer.tsx";
import FileStatusCardList from "./../../components/FileStatusCard/FileStatusCard.tsx";
import { Button } from "primereact/button";

const FileInfoPage = () => {
  const { state } = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  if (!state) {
    return (
      <div style={{ padding: "2rem", color: "#fff" }}>
        No file data found.{" "}
        <button onClick={() => navigate(-1)} className="p-button p-button-sm">
          Go Back
        </button>
      </div>
    );
  }

  console.log("FileInfoPage state:", state);

  const { receiverActions, fileUrl, filename, redirect, index } = state;

  return (
    <div className="file-info-page">
      <div className="file-info-container">
        <div className="pdf-viewer-section">
          <PDFViewer file={fileUrl} />
        </div>

        <div className="right-panel">
          <div className="top-bar">
            {" "}
            <Button
              icon="pi pi-times"
              className="p-button-rounded p-button-text add-btn"
              onClick={() => {
                console.log("Redirecting to:", redirect);
                if (redirect === "home") {
                  navigate("/");
                } else if (redirect == "admin") {
                  debugger;
                  navigate(`/admin/metrics/flows/${state.subtab}`);
                } else {
                  navigate("/myFiles", { state: { index: index } });
                }
              }}
              tooltip="Close tab"
              style={{ margin: "1rem" }}
            />
          </div>

          <h1
            style={{
              alignItems: "center",
              justifyContent: "center",
              textAlign: "center",
              marginBottom: "0",
              color: "white",
            }}
          >
            {filename}
          </h1>
          <FileStatusCardList
            receiverActions={receiverActions}
            itemsPerPage={2}
          />
        </div>
      </div>
    </div>
  );
};

export default FileInfoPage;
