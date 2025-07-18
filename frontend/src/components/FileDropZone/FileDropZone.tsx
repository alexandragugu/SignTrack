import React, { useCallback } from "react";
import { useDropzone } from "react-dropzone";
import { FaCloudUploadAlt } from "react-icons/fa";
import "./FileDropZone.css";
import { PiXBold } from "react-icons/pi";
import { useNavigate } from "react-router-dom";
import { Button } from "primereact/button";

interface FileDropzoneProps {
  onFileSelected: (files: File[]) => void;
}

const FileDropzone: React.FC<FileDropzoneProps> = ({ onFileSelected }) => {
  const navigate = useNavigate();

  const onDrop = useCallback(
    (acceptedFiles: File[]) => {
      if (acceptedFiles.length > 0) {
        onFileSelected(acceptedFiles);
      }
    },
    [onFileSelected]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: true,
  });

  return (
    <div className="relative">
      <div className="top-bar">
        {" "}
        <Button
          icon="pi pi-times"
          className="p-button-rounded p-button-text add-btn"
          onClick={() => navigate("/")}
          tooltip="Close tab"
        />
      </div>

      <div
        {...getRootProps()}
        className={`file-dropzone ${isDragActive ? "active" : ""}`}
      >
        <input {...getInputProps()} />
        <FaCloudUploadAlt className="upload-icon" />
        {isDragActive ? (
          <p className="drop-text">Drop the file here...</p>
        ) : (
          <p className="drop-text">
            <strong>Choose a file</strong> or drag it here.
          </p>
        )}
      </div>
    </div>
  );
};

export default FileDropzone;
