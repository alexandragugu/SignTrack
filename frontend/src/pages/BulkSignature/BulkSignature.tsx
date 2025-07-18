import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { SelectButton } from "primereact/selectbutton";
import { Checkbox } from "primereact/checkbox";
import { RadioButton } from "primereact/radiobutton";
import { Button } from "primereact/button";
import PDFViewer from "../../components/PdfViewer/PdfViewer.tsx";
import SignaturePositionSelector from "../../components/SignaturePositionSelector/SignaturePositionSelector.tsx";
import CloudBulk from "../../components/CloudBulk/CloudBulk.tsx";
import { Dropdown } from "primereact/dropdown";
import "./BulkSignature.css";
import SignPage from "../SignPage/SignPage.tsx";

const BulkSignaturePage: React.FC = () => {
  const location = useLocation();
  const [files, setFiles] = useState([]);
  const [selectedFileIndex, setSelectedFileIndex] = useState(0);
  const [selectedFileNodeKey, setSelectedFileNodeKey] = useState(null);

  const [selectedProfile, setSelectedProfile] = useState("Baseline B");
  const [isVisibleSignature, setIsVisibleSignature] = useState(false);
  const [selectedPage, setSelectedPage] = useState("first");
  const [selectedPosition, setSelectedPosition] = useState("top-left");
  const [showSignPage, setShowSignPage] = useState(false);
  const [signPageKey, setSignPageKey] = useState(0);

  const profileOptions = ["Baseline B", "Baseline B-T", "Baseline LTA"];

  useEffect(() => {
    const state = location.state as { files: any[] };

    if (!state || !state.files || state.files.length === 0) {
      console.warn("No files received in state");
      return;
    }

    setFiles(state.files);
    setSelectedFileNodeKey(state.files[0]?.fileId || null);
    setSelectedFileIndex(0);
  }, [location.state]);

  const handleSubmit = (type: "local" | "cloud") => {
    console.log("A apasat pe local");
    const file = files[selectedFileIndex];
    const signatureData = {
      fileName: file.filename,
      profile: selectedProfile,
      visibleSignature: isVisibleSignature,
      page: selectedPage,
      position: selectedPosition,
      type,
    };

    if (type === "local") {
      setShowSignPage(true);
      setSignPageKey((prev) => prev + 1);
    }
  };

  if (!files.length) {
    return (
      <div style={{ padding: "2rem", color: "white" }}>No files to sign.</div>
    );
  }

  const currentFile = files[selectedFileIndex];

  return (
    <div className="sign-options-container-custom">
      <AnimatePresence mode="wait">
        <motion.div
          key={currentFile.fileId}
          className="pdf-upload-container"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={{ opacity: 0, scale: 0.9 }}
        >
          <div className="pdf-viewer-section">
            <PDFViewer file={currentFile.fileUrl} />
          </div>

          <div className="signature-options-section">
            <h3>Select Document</h3>
            <Dropdown
              value={selectedFileNodeKey}
              options={files.map((file) => ({
                label: file.filename,
                value: file.fileId,
              }))}
              onChange={(e) => {
                const index = files.findIndex((f) => f.fileId === e.value);
                if (index !== -1) {
                  setSelectedFileIndex(index);
                  setSelectedFileNodeKey(e.value);
                  setShowSignPage(false);
                }
              }}
              placeholder="Select a file"
              className="custom-dropdown-theme"
              style={{ marginBottom: "1rem", width: "100%" }}
            />
            <div className="file-name">
              <i
                className="pi pi-file"
                style={{
                  marginRight: "8px",
                  fontSize: "1.8rem",
                  color: "#ffffff",
                }}
              ></i>
              <span style={{ fontSize: "1.3rem", fontWeight: "bold" }}>
                {currentFile.filename}
              </span>
            </div>

            <h3>Signature Profile</h3>
            <SelectButton
              value={selectedProfile}
              options={profileOptions}
              onChange={(e) => setSelectedProfile(e.value)}
              className="profile-select-button"
            />

            <div className="checkbox-group">
              <Checkbox
                inputId="visibleSignature"
                checked={isVisibleSignature}
                onChange={(e) => setIsVisibleSignature(e.checked!)}
              />
              <label htmlFor="visibleSignature">Visible Signature</label>
            </div>

            {isVisibleSignature && (
              <>
                <h3>Select Page</h3>
                <div className="page-select">
                  <div className="radio-item">
                    <RadioButton
                      inputId="firstPage"
                      name="page"
                      value="first"
                      checked={selectedPage === "first"}
                      onChange={(e) => setSelectedPage(e.value)}
                    />
                    <label htmlFor="firstPage">First Page</label>
                  </div>
                  <div className="radio-item">
                    <RadioButton
                      inputId="lastPage"
                      name="page"
                      value="last"
                      checked={selectedPage === "last"}
                      onChange={(e) => setSelectedPage(e.value)}
                    />
                    <label htmlFor="lastPage">Last Page</label>
                  </div>
                </div>

                <h3>Select Position</h3>
                <SignaturePositionSelector
                  selectedPosition={selectedPosition}
                  setSelectedPosition={setSelectedPosition}
                />
              </>
            )}

            <div className="signature-buttons">
              <Button
                label="Local Signature"
                className="p-button signature-btn"
                onClick={() => handleSubmit("local")}
              />

              <CloudBulk
                files={files}
                selectedProfile={selectedProfile}
                isVisibleSignature={isVisibleSignature}
                selectedPage={selectedPage}
                selectedPosition={selectedPosition}
                type="cloud"
              />
            </div>
          </div>
          {showSignPage && (
            <div className="sign-page-wrapper">
              {
                <SignPage
                  key={signPageKey}
                  files={files}
                  selectedProfile={selectedProfile}
                  isVisibleSignature={isVisibleSignature}
                  selectedPage={selectedPage}
                  selectedPosition={selectedPosition}
                  type="local"
                />
              }
            </div>
          )}
        </motion.div>
      </AnimatePresence>
    </div>
  );
};

export default BulkSignaturePage;
