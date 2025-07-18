import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "./FilesList.css";
import apiClient from "../../Utils/ApiClient.tsx";

interface FileDetailsModel {
  filename: string;
  fileUrl: string;
  receiverUsername: string;
  receiverStatus: string;
  senderUsername: string;
  fileId: string;
}

const FilesList: React.FC = () => {
  const [files, setFiles] = useState<FileDetailsModel[] | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [showPopup, setShowPopup] = useState<boolean>(false);
  const [fileDetails, setFileDetails] = useState<FileDetailsModel[]>([]);
  const [popupMessage, setPopupMessage] = useState<string>("");
  const navigate = useNavigate();

  const token = localStorage.getItem("token");

  useEffect(() => {
    const fetchFiles = async () => {
      try {
        const response = await apiClient.get("/files/allFiles");

        const data = await response.data;
        console.log("Fetched Data:", data);

        if (data && Array.isArray(data)) {
          setFiles(data);
        } else {
          console.error("Unexpected data format:", data);
          setFiles([]);
        }
      } catch (err) {
        console.error("Error fetching files:", err);
        setError("Failed to fetch files");
      } finally {
        setLoading(false);
      }
    };

    fetchFiles();
  }, []);

  const handleDeleteFile = async (fileId, filename) => {
    try {
      const response = await apiClient.delete(`/files/delete`, {
        data: { fileId, filename },
      });
      if (response.status === 200) {
        alert("File deleted successfully!");

        window.location.reload();
      }
    } catch (error) {
      console.error(
        "Error deleting file:",
        error.response?.data || error.message
      );
      alert("Failed to delete file. Please try again.");
    }
  };

  return (
    <div className="user-files-container">
      <h2 className="section-title">My Files</h2>
      {loading && <p>Loading files...</p>}
      {error && <p className="error">{error}</p>}
      <table className="files-table">
        <thead>
          <tr>
            <th>Filename</th>
            <th>Download</th>
            <th>Status</th>
            <th>Delete</th>
          </tr>
        </thead>
        <tbody>
          {files && files.length > 0 ? (
            files.map((file, index) => (
              <tr key={index}>
                <td>{file.filename}</td>
                <td>
                  <a
                    href={file.fileUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <button className="download-btn">Download</button>
                  </a>
                </td>
                <td>
                  <button
                    className="status-btn"
                    onClick={() =>
                      navigate(`/view-file`, {
                        state: {
                          filename: file.filename,
                          fileUrl: file.fileUrl,
                          fileId: file.fileId,
                        },
                      })
                    }
                  >
                    Check Status
                  </button>
                </td>

                <td>
                  <button
                    className="status-btn"
                    onClick={() => handleDeleteFile(file.fileId, file.filename)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={3} style={{ textAlign: "center", padding: "10px" }}>
                No files found.
              </td>
            </tr>
          )}
        </tbody>
      </table>

      {showPopup && (
        <div className="popup-overlay">
          <div className="popup-content">
            <h3>File Status</h3>
            {popupMessage ? (
              <p>{popupMessage}</p>
            ) : (
              <table className="status-table">
                <thead>
                  <tr>
                    <th>Send to</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {fileDetails.map((detail, index) => (
                    <tr key={index}>
                      <td>{detail.receiverUsername}</td>
                      <td>{detail.receiverStatus}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
            <button className="close-popup" onClick={() => setShowPopup(false)}>
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default FilesList;
