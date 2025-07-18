import React, { useState, useEffect } from "react";
import { Document, Page, pdfjs } from "react-pdf";
import "react-pdf/dist/esm/Page/AnnotationLayer.css";
import "react-pdf/dist/esm/Page/TextLayer.css";
import "./PdfViewer.css";

import {
  FaArrowLeft,
  FaArrowRight,
  FaSearch,
  FaDownload,
} from "react-icons/fa";

pdfjs.GlobalWorkerOptions.workerSrc = `https://cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.min.js`;

interface PDFViewerProps {
  file: string;
  className?: string;
}

const PDFViewer: React.FC<PDFViewerProps> = ({ file, className }) => {
  const [numPages, setNumPages] = useState<number | null>(null);
  const [pageNumber, setPageNumber] = useState(1);
  const [inputPage, setInputPage] = useState("");

  function onDocumentLoadSuccess({ numPages }: { numPages: number }) {
    setNumPages(numPages);
  }

  useEffect(() => {
    setInputPage(pageNumber.toString());
  }, [pageNumber]);

  const handlePageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setInputPage(value);
  };

  const goToPage = () => {
    const pageNum = parseInt(inputPage, 10);
    if (numPages && pageNum >= 1 && pageNum <= numPages) {
      setPageNumber(pageNum);
    } else {
      setInputPage(pageNumber.toString());
    }
  };

  return (
    <div className={`pdf-viewer ${className || ""}`}>
      <div className="pdf-toolbar">
        <button
          className="pdf-toolbar-button"
          disabled={pageNumber <= 1}
          onClick={() => setPageNumber(pageNumber - 1)}
        >
          <FaArrowLeft />
        </button>
        <input
          type="number"
          value={inputPage}
          onChange={handlePageChange}
          onBlur={goToPage}
          onKeyDown={(e) => e.key === "Enter" && goToPage()}
        />
        <span> / {numPages || "..."}</span>

        <button
          className="pdf-toolbar-button"
          disabled={numPages !== null && pageNumber >= numPages}
          onClick={() => setPageNumber(pageNumber + 1)}
        >
          <FaArrowRight />
        </button>
      </div>

      <div className="pdf-content">
        <Document file={file} onLoadSuccess={onDocumentLoadSuccess}>
          <Page
            pageNumber={pageNumber}
            scale={0.8}
            renderMode="canvas"
            canvasBackground="white"
            devicePixelRatio={2}
            renderTextLayer={true}
            renderAnnotationLayer={false}
          />
        </Document>
      </div>
    </div>
  );
};

export default PDFViewer;
