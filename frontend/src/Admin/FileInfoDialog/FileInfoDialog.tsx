import React from "react";
import { Dialog } from "primereact/dialog";
import { Button } from "primereact/button";
import PDFViewer from "../../components/PdfViewer/PdfViewer.tsx";
import FileStatusCardList from "../../components/FileStatusCard/FileStatusCard.tsx";
import "./FileInfoDialog.css";

const FileInfoDialog = ({
  visible,
  onHide,
  fileUrl,
  filename,
  receiverActions,
}) => {
  return (
    <Dialog
      header="File Information"
      visible={visible}
      onHide={onHide}
      className="file-info-dialog"
      style={{ width: "90vw", height: "100vh", maxWidth: "1400px" }}
      modal
      dismissableMask
      contentStyle={{ height: "100%" }}
    >
      <div className="file-info-dialog__container">
        <div className="file-info-dialog__pdf-viewer">
          <PDFViewer file={fileUrl} />
        </div>

        <div className="file-info-dialog__right-panel">
          <h1 className="file-info-dialog__filename">{filename}</h1>

          <FileStatusCardList
            receiverActions={receiverActions}
            itemsPerPage={2}
          />
        </div>
      </div>
    </Dialog>
  );
};

export default FileInfoDialog;
