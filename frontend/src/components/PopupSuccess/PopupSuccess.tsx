import React from "react";
import { Dialog } from "primereact/dialog";
import { Button } from "primereact/button";
import { useNavigate } from "react-router-dom";
import "./PopupSuccess.css";

const PopupSuccess = ({ visible, onHide, fileName, message }) => {
  const navigate = useNavigate();

  return (
    <Dialog
      visible={visible}
      style={{ width: "30vw", maxWidth: "90vw" }}
      onHide={onHide}
      className="custom-popup"
      closable={false}
      draggable={false}
      dismissableMask={false}
    >
      <div>
        File(s): {fileName}
        <h2 className="popup-title">
          {message || "Has been signed successfully!"}
        </h2>
        <p className="popup-subtitle">Owner has been notified!</p>
        <Button
          label="Homepage"
          icon="pi pi-home"
          className="homepage-button p-button-rounded p-button-lg"
          onClick={() => navigate("/")}
        />
      </div>
    </Dialog>
  );
};

export default PopupSuccess;
