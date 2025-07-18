import React, { useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { Button } from "primereact/button";
import "./FileUpload.css";
import FileDropzone from "./../FileDropZone/FileDropZone.tsx";
import PDFViewer from "./../PdfViewer/PdfViewer.tsx";
import RecipientForm from "./../RecipientForm/RecipientForm.tsx";
import { Dropdown } from "primereact/dropdown";

const FileUpload = () => {
  const navigate = useNavigate();
  const [uploadedFiles, setUploadedFiles] = useState<File[]>([]);

  const signatureData = {
    files: uploadedFiles,
  };

  const [selectedFileIndex, setSelectedFileIndex] = useState(0);

  const fileInputRef = useRef(null);

  const handleAddFiles = (event) => {
    const newFiles = Array.from(event.target.files);
    setUploadedFiles((prev) => [...prev, ...newFiles]);
  };

  return (
    <motion.div
      initial={{ x: "-100vw", opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      exit={{ x: "100vw", opacity: 0 }}
      transition={{ type: "tween", duration: 0.8 }}
    >
      <div className="sign-page-container-custom">
        <AnimatePresence mode="wait">
          {uploadedFiles.length === 0 ? (
            <motion.div
              key="dropzone"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0.1, scale: 0.9 }}
            >
              <FileDropzone onFileSelected={setUploadedFiles} />
            </motion.div>
          ) : (
            <motion.div
              className="pdf-upload-container"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.9 }}
            >
              <div className="pdf-viewer-section">
                <PDFViewer
                  file={URL.createObjectURL(uploadedFiles[selectedFileIndex])}
                />
              </div>

              <div className="signature-options-section">
                <Dropdown
                  value={selectedFileIndex}
                  options={uploadedFiles.map((file, i) => ({
                    label: file.name,
                    value: i,
                  }))}
                  onChange={(e) => setSelectedFileIndex(e.value)}
                  className="file-dropdown"
                  placeholder="Select a file"
                  style={{ marginBottom: "1rem", width: "100%" }}
                />
                <div className="file-toolbar">
                  <Button
                    icon="pi pi-trash"
                    className="change-file-btn-glow"
                    onClick={() => {
                      setUploadedFiles((prev) => {
                        const newFiles = prev.filter(
                          (_, i) => i !== selectedFileIndex
                        );
                        if (newFiles.length > 0) {
                          setSelectedFileIndex((currentIndex) =>
                            currentIndex >= newFiles.length
                              ? newFiles.length - 1
                              : currentIndex
                          );
                        }
                        return newFiles;
                      });
                    }}
                    tooltip="Remove file"
                  />

                  <Button
                    icon="pi pi-plus"
                    className="change-file-btn-glow"
                    onClick={() => fileInputRef.current?.click()}
                    tooltip="Add file"
                  />

                  <input
                    type="file"
                    multiple
                    accept="application/pdf"
                    ref={fileInputRef}
                    style={{ display: "none" }}
                    onChange={handleAddFiles}
                  />
                </div>

                <text className="file-name">
                  <i
                    className="pi pi-file"
                    style={{
                      marginRight: "8px",
                      fontSize: "1.8rem",
                      color: "#ffffff",
                    }}
                  />
                  <span style={{ fontSize: "1.3rem", fontWeight: "bold" }}>
                    {uploadedFiles[selectedFileIndex].name}
                  </span>
                </text>

                <RecipientForm signatureData={{ files: uploadedFiles }} />
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </motion.div>
  );
};

export default FileUpload;
