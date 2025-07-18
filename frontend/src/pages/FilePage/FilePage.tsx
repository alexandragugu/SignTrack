import React, { FC, useState, useEffect, useRef } from "react";
import axios from "axios";
import NexuClient from "../../services/NexU.tsx";
import apiClient from "../../Utils/ApiClient.tsx";

import DocumentSigner, {
  DocumentSignerRef,
  CertificateData,
} from "../../DocumentSigner.tsx";

import "./FilePage.css";

interface NexuClientMethods {
  isReady: () => boolean;
}

interface FilePageProps {
  file: File | null | string;
}

const FilePage: FC<FilePageProps> = ({ file }) => {
  const [message, setMessage] = useState<string>("");
  const [msh, setMsh] = useState<Uint8Array | null>(null);
  const [nexuReady, setNexuReady] = useState<boolean>(false);
  const [certificatesData, setCertificatesData] =
    useState<CertificateData | null>(null);

  const [isSigningPrepared, setIsSigningPrepared] = useState(false);

  const documentSignerRef = useRef<DocumentSignerRef>(null);
  const nexuClientRef = useRef<NexuClientMethods | null>(null);

  useEffect(() => {
    const checkNexuStatus = setInterval(() => {
      if (nexuClientRef.current?.isReady()) {
        setNexuReady(true);
      } else {
        setNexuReady(false);
      }
    }, 1000);

    return () => clearInterval(checkNexuStatus);
  }, []);

  const handlePrepareSigning = async () => {
    if (!file) {
      setMessage("Please upload a file first.");
      return;
    }
    if (nexuClientRef.current?.isReady()) {
      console.log("Nexu ready");
    }

    if (documentSignerRef.current) {
      console.log("Incepe semnarea");

      try {
        const certificatesData =
          await documentSignerRef.current.prepareSigning();
        if (certificatesData) {
          setCertificatesData(certificatesData);
          const onlyCert = {
            certificate: certificatesData.certificate,
            certificateChain: certificatesData.certificateChain,
            encryptionAlgorithm: certificatesData.encryptionAlgorithm,
          };
          if (file) {
            const formData = new FormData();
            formData.append("file", file);
            if (onlyCert) {
              formData.append("certificatesData", JSON.stringify(onlyCert));

              try {
                const token = localStorage.getItem("token");
                console.log("token:" + token);
                if (!token) {
                  console.error("Nu gaseste token in localstorage");
                  return;
                }
                console.log("token inaite de upload:" + token);
                const response = await apiClient.post(
                  "/file/upload",
                  formData,
                  {
                    headers: {
                      "Content-Type": "multipart/form-data",
                    },
                    responseType: "text",
                  }
                );

                console.log("MSH IN BASE 64 CAND PRIMESC 1:" + response.data);

                const binaryString = atob(response.data);

                const uint8Array = new Uint8Array(binaryString.length);
                for (let i = 0; i < binaryString.length; i++) {
                  uint8Array[i] = binaryString.charCodeAt(i);
                }

                console.log("AICI TE UITI:", uint8Array);

                const mshUint8Array = new Uint8Array(uint8Array);

                setMsh(mshUint8Array);
              } catch (error) {
                console.error("Error uploading file:", error);
                setMessage("Failed to upload file");
              }
            }
          } else {
            console.log("Nu s-au primit datele certificatului");
          }
        }
      } catch (error) {
        setMessage("NexU is not ready for signing.");
      }
    }
  };

  useEffect(() => {
    if (certificatesData && msh) {
      const onlySign = {
        tokenId: certificatesData.tokenId,
        keyId: certificatesData.keyId,
        digestAlgorithm: "SHA256",
      };
      console.log("Apeleaza getDataToSign cu msh:" + msh);
      documentSignerRef.current?.getDataToSign(onlySign, msh);
      setIsSigningPrepared(true);
    }
  }, [certificatesData, msh, isSigningPrepared]);
  const handleButtonClick = () => {
    if (file) {
      handlePrepareSigning();
    } else {
      setMessage("Please upload a file first.");
    }
  };

  return (
    <div className="nexu-sign-info">
      {file && (
        <button
          onClick={handlePrepareSigning}
          disabled={!file || !nexuReady}
          className="upload-button"
        >
          Upload & Sign
        </button>
      )}

      <div className="nexu-status">
        <p
          className={`nexu-message ${
            nexuReady ? "nexu-ready" : "nexu-not-ready"
          }`}
        >
          {nexuReady ? "NexU is ready!" : "NexU not connected"}
        </p>
      </div>

      {message && (
        <p
          className={`message ${
            message.includes("successfully") ? "success" : "error"
          }`}
        >
          {message}
        </p>
      )}

      <NexuClient ref={nexuClientRef} />

      <DocumentSigner ref={documentSignerRef} />
    </div>
  );
};

export default FilePage;
