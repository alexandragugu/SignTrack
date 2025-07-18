import React, { useRef } from "react";
import { ConfirmDialog, confirmDialog } from "primereact/confirmdialog";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import "./GlobalConfirmDialog.css";

export const useConfirm = () => {
  const confirm = ({ message, onAccept }) => {
    confirmDialog({
      message:
        message ||
        "Are you sure you want to delete this item? This action is permanent and irreversible.",
      header: "Confirm Deletion",
      icon: "pi pi-exclamation-triangle",
      acceptLabel: "Yes, Delete",
      rejectLabel: "Cancel",
      acceptClassName: "p-button-danger",
      rejectClassName: "p-button-secondary",
      accept: onAccept,
      reject: () => {},
    });
  };

  return confirm;
};

export const GlobalConfirmDialog = () => {
  return <ConfirmDialog className="custom-confirm-dialog" />;
};
