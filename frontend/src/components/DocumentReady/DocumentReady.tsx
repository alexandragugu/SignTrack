import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams, useLocation } from "react-router-dom";
import "./DocumentReady.css";
import Navbar from "../Navbar/Navbar.tsx";
import PDFViewer from "../PdfViewer/PdfViewer.tsx";
import RecipientForm from "../RecipientForm/RecipientForm.tsx";

const DocumentReady: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const location = useLocation();

  const [sendEmail, setSendEmail] = useState(false);

  const filename = searchParams.get("filename");
  const fileUrl = searchParams.get("fileUrl");
  const fileId = location.state?.fileId;

  useEffect(() => {
    if (filename && fileUrl) {
      console.log("FILENAME: " + filename);
      console.log("File URL: " + fileUrl);
      console.log("File Id: " + fileId);
    }
  }, [filename, fileUrl, fileId]);

  const handleSendToOthers = () => {
    setSendEmail(true);
  };

  const handleDownload = () => {
    if (fileUrl) {
      try {
        const link = document.createElement("a");
        link.href = fileUrl;
        link.download = filename || "downloaded_file";
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      } catch (error) {
        console.error("Download failed:", error);
        alert("Failed to download the file.");
      }
    } else {
      alert("File URL is not available.");
    }
  };

  return (
    <div className="document-ready-page">
      <Navbar />
      <div className="document-ready-container">
        <div className="document-ready-left">
          {fileUrl ? <PDFViewer file={fileUrl} /> : <p>Loading document...</p>}
        </div>

        <div className="document-ready-right">
          <p className="filename">{filename || "No file selected"}</p>

          <div className="document-button-group">
            <button
              className="document-button-send-button"
              onClick={handleSendToOthers}
            >
              Send to Others
            </button>
            <button
              className="document-button-download-button"
              onClick={handleDownload}
            >
              Download
            </button>
          </div>

          {sendEmail && (
            <RecipientForm fileId={fileId || ""} />
          )}
        </div>
      </div>
    </div>
  );
};

export default DocumentReady;
