import React, { useRef } from "react";
import "./ViewButtons.css";
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

    const data = await response.data;
    console.log(data);
  } catch (error) {
    console.error("Eroare:", error.message);
  }
}

async function consentRequest(fileId: string, senderUsername: string) {
  try {
    const response = await apiClient.post("/files/answerRequest", {
      fileId: fileId,
      senderUsername: senderUsername,
    });

    const data = await response.data;
    console.log(data);
  } catch (error) {
    console.error("Eroare:", error.message);
  }
}

interface ViewButtonsProps {
  fileId: string;
  senderUsername: string;
}

const ViewButtons: React.FC<ViewButtonsProps> = ({
  fileId,
  senderUsername,
}) => {
  const navigate = useNavigate();
  const toast = useRef(null);
  const handleView = async () => {
    console.log("View action for:", fileId);

    try {
      const response = await apiClient.post("/files/answerRequest", {
        fileId: fileId,
      });

      toast.showSuccess(
        "Status updated. The owner has been notified accordingly."
      );
      setTimeout(() => navigate("/"), 2000);
    } catch (error) {
      console.error("Eroare:", error.message);
      toast.showError("An error occurred while updating view status.");
    }
  };

  const handleDecline = () => {
    if (fileId && senderUsername) {
      declineRequest(fileId, senderUsername)
        .then(() => {
          toast.current?.showSuccess("Request declined successfully.");
          setTimeout(() => navigate("/"), 2000);
        })
        .catch((error) => {
          toast.current?.showError("Failed to decline the request.");
          console.error("Eroare la respingerea cererii:", error);
          setTimeout(() => navigate("/"), 2000);
        });
    }
    console.log("Declined action triggered");
  };

  return (
    <div className="approve-buttons-container">
      <Button
        icon="pi pi-eye"
        className="approve-btn p-button-rounded p-button-outlined"
        onClick={handleView}
        tooltip="View"
        tooltipOptions={{ position: "top" }}
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

export default ViewButtons;
