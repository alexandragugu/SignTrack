import React from "react";
import "./DocumentPreview.css";

const handleOpenFile = (fileUrl: string) => {
  if (fileUrl) {
    const fileExtension = fileUrl.split(".").pop().toLowerCase();
    if (fileExtension === "pdf") {
      window.open(
        `https://docs.google.com/gview?url=${fileUrl}&embedded=true`,
        "_blank"
      );
    } else {
      window.open(fileUrl, "_blank");
    }
  } else {
    console.error("File URL is missing!");
  }
};

const DocumentPreview = ({ document }) => {
  if (!document) {
    return (
      <div className="document-preview-custom empty">
        Select a document to preview
      </div>
    );
  }

  return (
    <div className="document-preview-custom">
      <div className="preview-container-custom">
        <img
          src="/document.jpg"
          alt={document.name}
          className="preview-image-custom"
        />
      </div>
      <div className="preview-actions-custom">
        <button
          onClick={() => handleOpenFile(document.fileUrl)}
          className="action-button-custom"
        >
          Open
        </button>
      </div>
    </div>
  );
};

export default DocumentPreview;
