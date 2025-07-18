import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { SelectButton } from "primereact/selectbutton";
import { Checkbox } from "primereact/checkbox";
import { RadioButton } from "primereact/radiobutton";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import "./SignatureOptions.css";
import PDFViewer from "../../components/PdfViewer/PdfViewer.tsx";
import SignaturePositionSelector from "../../components/SignaturePositionSelector/SignaturePositionSelector.tsx";
import SignPage from "./../SignPage/SignPage.tsx";


interface LocationState {
  fileId: string;
  fileUrl: string;
  filename: string;
}

const SignatureOptions: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [fileId, setFileId] = useState<string>("");
  const [fileUrl, setFileUrl] = useState<string>("");
  const [filename, setFilename] = useState<string>("");
  const [selectedProfile, setSelectedProfile] = useState<string>("Baseline B");
  const [isVisibleSignature, setIsVisibleSignature] = useState<boolean>(false);
  const [selectedPage, setSelectedPage] = useState<string>("first");
  const [selectedPosition, setSelectedPosition] = useState<string>("top-left");
  const [selectSignatureType, setSelectSignatureType] = useState<string | null>(
    null
  );

  const [showSignPage, setShowSignPage] = useState(false);
  const [signPageKey, setSignPageKey] = useState(0);

  const profileOptions = ["Baseline B", "Baseline B-T", "Baseline LTA"];
  const positionOptions = [
    "top-left",
    "top-right",
    "bottom-left",
    "bottom-right",
  ];

  const handleShowSignPage = () => {
    setShowSignPage(true);
    setSignPageKey((prev) => prev + 1);
  };

  const handleSubmit = (type: "local" | "cloud") => {
    console.log("A apasat pe local")
    const signatureData = {
      fileName: filename,
      profile: selectedProfile,
      visibleSignature: isVisibleSignature,
      page: selectedPage,
      position: selectedPosition,
      type: type,
    };

    if (type === "local") {
       setSelectSignatureType("local");
      setShowSignPage(true);
      console.log("Local signature data:", signatureData);
    }
  };

  useEffect(() => {
    const state = location.state as LocationState;

    if (!state || !state.fileId || !state.fileUrl || !state.filename) {
      console.warn("Missing data in location state");
      // navigate("/");
    } else {
      setFileId(state.fileId);
      setFileUrl(state.fileUrl);
      setFilename(state.filename);

      console.log("fileId" + fileId);
      console.log("fileUrl" + fileUrl);
      console.log("filename" + filename);
    }
  }, [location.state]);

  return (
    <div className="sign-options-container-custom">
      <AnimatePresence mode="wait">
        <motion.div
          key="pdf-viewer"
          className="pdf-upload-container"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={{ opacity: 0, scale: 0.9 }}
        >
          <div className="pdf-viewer-section">
            <PDFViewer file={fileUrl} />
          </div>

          <div className="signature-options-section">
            <text className="file-name">
              <i
                className="pi pi-file"
                style={{
                  marginRight: "8px",
                  fontSize: "1.8rem",
                  color: "#ffffff",
                }}
              ></i>
              <span
                style={{
                  fontSize: "1.3rem",
                  fontWeight: "bold",
                  verticalAlign: "middle",
                }}
              >
                {filename}
              </span>
            </text>

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
              <div className="signature-visibility-options">
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
                <div className="signature-position-container">
                  <SignaturePositionSelector
                    selectedPosition={selectedPosition}
                    setSelectedPosition={setSelectedPosition}
                  />
                </div>
              </div>
            )}
            <div className="signature-buttons">
              <Button
                type="button"
                label="Local Signature"
                className="p-button signature-btn"
                onClick={() => {
                  console.log("Clicked Local Signature button");
                  handleSubmit("local");
                }}
              />

            </div>
          </div>
         {showSignPage && fileId && fileUrl && filename && (
         
            <div className="sign-page-wrapper">
              {
                <SignPage
                  key={signPageKey}
                  fileId={fileId}
                  fileUrl={fileUrl}
                  filename={filename}
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
export default SignatureOptions;
