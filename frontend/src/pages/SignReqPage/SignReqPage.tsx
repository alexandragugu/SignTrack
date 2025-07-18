import React, { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import axios from "axios";
import { send } from "process";
import "./SignReqPage.css";
import Navbar from "../../components/Navbar/Navbar.tsx";
import PDFViewer from "../../components/PdfViewer/PdfViewer.tsx";
import ApproveButtons from "../../components/ApproveButtons/ApproveButtons.tsx";
import ViewButtons from "../../components/ViewButtons/ViewButtons.tsx";
import apiClient from "../../Utils/ApiClient.tsx";
import CutomSpinner from "../../components/CustomSpinner/CustomSpinner.tsx";
import { motion } from "framer-motion";
import RecipientForm from "../../components/RecipientForm/RecipientForm.tsx";
import FileStatus from "../FileStatus/FileStatus.tsx";
import FileStatusCardList from "../../components/FileStatusCard/FileStatusCard.tsx";
import { Divider } from "primereact/divider";
import { Button } from "primereact/button";
import { FaPen, FaTimes } from "react-icons/fa";

const username = localStorage.getItem("username");

async function declineRequest(filename: string, senderUsername: string) {
  try {
    const response = await apiClient.post("/files/declineRequest", {
      filename: filename,
      senderUsername: senderUsername,
    });

    const data = await response.data;
    console.log(data);
  } catch (error) {
    console.error("Eroare:", error.message);
  }
}

const SignReqPage = () => {
  const [message, setMessage] = useState<string>("");

  const [isValidToken, setIsValidToken] = useState<boolean>(false);
  const [filename, setFilename] = useState<string | null>(null);
  const [senderUsername, setSenderUsername] = useState<string | null>(null);
  const [fileUrl, setFileUrl] = useState<string | null>(null);
  const [fileId, setFileId] = useState<File | null>(null);
  const [actionType, setActionType] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [recipientActions, setRecipientActions] = useState<any[]>([]);

  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const token = searchParams.get("token");

    if (token) {
      validateToken(token);
    } else {
      setMessage("Invalid or non-existent token.");
      setIsLoading(false);
    }
  }, [location.search]);

  const validateToken = async (token: string) => {
    console.log("token", token);
    try {
      const response = await apiClient.get(`/validateToken?token=${token}`);

      const data = await response.data;

      console.log("data", data);

      setIsValidToken(data.success);
      setFilename(data.filename);
      setSenderUsername(data.senderUsername);
      setFileUrl(data.fileUrl);
      setActionType(data.actionType);
      setFileId(data.fileId);

      if (Array.isArray(data.receiverActions)) {
        const mappedActions = data.receiverActions.map((item: any) => ({
          username: item.receiverUsername,
          action: item.action,
          currentDate: new Date().toISOString(),
          email: null,
          userId: item.userId || "",
        }));

        setRecipientActions(mappedActions);
      }

      if (data.success) {
        setMessage("Hello " + username + "!");
      } else {
        setMessage(data.message);
      }
    } catch (error) {
      console.error("Eroare la validarea tokenului:", error);
      setMessage(
        error.response?.data?.message ||
          "Error occurred while validating the token."
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleSign = () => {
    if (!fileId || !fileUrl || !filename) {
      console.warn("Incomplete file data for signing.");
      return;
    }

    navigate("/bulk", {
      state: {
        files: [
          {
            fileId: fileId,
            fileUrl: fileUrl,
            filename: filename,
          },
        ],
        action: "TO_SIGN",
        redirect: "home",
      },
    });
  };
  const handleDecline = () => {
    if (filename && senderUsername) {
      declineRequest(filename, senderUsername)
        .then(() => {
          alert("The request has been successfully declined!");
          navigate(`/`);
        })
        .catch((error) => {
          console.error("Eroare la respingerea cererii:", error);
        });
    }
  };

  const assignedRole = (action: string) => {
    if (action === "TO_SIGN") return "Signer";
    if (action === "TO_APPROVE") return "Approver";
    if (action === "TO_VIEW") return "Viewer";
    return "Unknown";
  };

  if (isLoading)
    return (
      <div className="sign-req-page">
        {" "}
        <CutomSpinner />
      </div>
    );

  return (
    <div>

      {!isValidToken ? (
        <div className="invalid-token-container">
          <p className="error-message">{message || "File action not found"}</p>
          <Button
            icon="pi pi-arrow-left"
            onClick={() => navigate("/")}
            className="p-button-rounded p-button-text  home-btn"
            tooltip="Go to Home"
          />
        </div>
      ) : (
        <div className="file-req-container">
          <div className="pdf-viewer-section">
            <PDFViewer file={fileUrl as string} />
          </div>

          <div className="side-panel">
            <div className="request-info-card">
              <text>Document Request</text>
              <p></p>
              User <strong>{senderUsername}</strong> assigned you the role of{" "}
              <strong>{assignedRole(actionType as string)}</strong> for{" "}
              <strong>{filename}</strong>
            </div>
            <Divider
              layout="horizontal"
              type="solid"
              align="center"
              style={{ background: "none!important", margin: "12px" }}
            >
              <span
                style={{
                  color: "rgb(255, 255, 255)",
                  display: "flex",
                  alignItems: "center",
                  fontWeight: "bold",
                  gap: "0.5rem",
                  backgroundColor: "none",
                }}
              >
                Status Overview
              </span>
            </Divider>

            <FileStatusCardList receiverActions={recipientActions} />

            <Divider content="none"></Divider>

            <div className="action-buttons">
              {actionType === "TO_SIGN" && (
                <>
                  <button
                    onClick={handleSign}
                    className="circle-button custom-table-sign-button"
                    title="Sign"
                  >
                    <FaPen />
                  </button>
                  <button
                    onClick={handleDecline}
                    className="circle-button custom-table-sign-button"
                    title="Decline"
                  >
                    <FaTimes />
                  </button>
                </>
              )}
              {actionType === "TO_APPROVE" && fileId && senderUsername && (
                <ApproveButtons
                  fileId={fileId ? fileId.toString() : ""}
                  senderUsername={senderUsername}
                />
              )}
              {actionType === "TO_VIEW" && fileId && senderUsername && (
                <ViewButtons
                  fileId={fileId ? fileId.toString() : ""}
                  senderUsername={senderUsername}
                />
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SignReqPage;
