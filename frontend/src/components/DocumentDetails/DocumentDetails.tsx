import React from "react";
import "./DocumentDetails.css";

const DocumentDetails = ({ document }) => {
  if (!document) {
    return (
      <div className="document-details empty">
        Select a document to see details
      </div>
    );
  }

  return (
    <div className="document-details">
      <h3>{document.filename}</h3>
      <div className="document-info"></div>
      <div className="document-info">
        <span>Uploaded on:</span>
        <span>{new Date(...document.createdDate).toLocaleDateString()}</span>
      </div>
      <div className="document-info">
        <span>Last modified:</span>
        <span>
          {new Date(...document.lastModifiedDate).toLocaleDateString()}
        </span>
      </div>
      <div className="document-info"></div>
    </div>
  );
};

export default DocumentDetails;
