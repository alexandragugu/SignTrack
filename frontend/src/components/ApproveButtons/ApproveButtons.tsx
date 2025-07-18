import React, { useRef } from "react";
import "./ApproveButtons.css";
import { useNavigate } from "react-router-dom";
import apiClient from "../../Utils/ApiClient.tsx";
import { Button } from "primereact/button";

const tokenAuth = localStorage.getItem("token");
async function declineRequest(fileId: string, senderUsername: string) {
  try {
    const response = await apiClient.post("/files/declineRequest", {
      fileId: fileId,
      senderUsername: senderUsername,
    });
    const data = response.data;
    console.log(data);
  } catch (error) {
    console.error("Eroare:", error.message);
  }
}

async function approveRequest(fileId: string) {
  try {
    const response = await apiClient.post("/files/answerRequest", {
      fileId: fileId,
    });
    const data = response.data;
    console.log(data);
  } catch (error) {
    console.error("Eroare:", error.message);
  }
}

interface ApproveButtonsProps {
  fileId: string;
  senderUsername: string;
}

const ApproveButtons: React.FC<ApproveButtonsProps> = ({
  fileId,
  senderUsername,
}) => {
  const navigate = useNavigate();
  const toastRef = useRef(null);

  const handleApprove = () => {
    approveRequest(fileId)
      .then(() => {
        toastRef.current?.showSuccess(
          "File approved successfully. The owner has been notified."
        );
        setTimeout(() => navigate("/"), 2000);
      })
      .catch((error) => {
        toastRef.current?.showError(
          "An error occurred while approving the file."
        );
        console.error("Eroare la acceptarea cererii:", error);
      });
  };

  const handleDecline = () => {
    if (fileId) {
      declineRequest(fileId, senderUsername)
        .then(() => {
          toastRef.current?.showSuccess("Request declined successfully.");
          setTimeout(() => navigate("/"), 2000);
        })
        .catch((error) => {
          console.error("Eroare la respingerea cererii:", error);
          toastRef.current?.showError("Failed to decline the request.");
        });
    }
    console.log("Declined action triggered");
  };

  return (
    <div className="approve-buttons-container">
      <Button
        icon="pi pi-check"
        className="approve-btn"
        onClick={handleApprove}
        rounded
        text
      />
      <Button
        icon="pi pi-times"
        className="deny-button-req p-button-rounded p-button-outlined"
        onClick={handleDecline}
        tooltip="Decline"
        tooltipOptions={{ position: "top" }}
      />
    </div>
  );
};

export default ApproveButtons;
