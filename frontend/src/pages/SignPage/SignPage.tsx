import React, { useState, useEffect, useRef } from "react";
import { useParams, useLocation } from "react-router-dom";
import axios from "axios";
import NexuClient from "../../services/NexU.tsx";
import DocumentSigner, {
  DocumentSignerRef,
  CertificateData,
} from "../../DocumentSigner.tsx";
import "./SignPage.css";
import PDFViewer from "../../components/PdfViewer/PdfViewer.tsx";
import Navbar from "../../components/Navbar/Navbar.tsx";
import FilePage from "../FilePage/FilePage.tsx";
import apiClient from "../../Utils/ApiClient.tsx";
import PopupSuccess from "../../components/PopupSuccess/PopupSuccess.tsx";
import config from "../../Config/config.tsx";

interface SignatureOptionsProps {
  files: {
    fileId: string;
    fileUrl: string;
    filename: string;
  }[];
  selectedProfile: string;
  isVisibleSignature: boolean;
  selectedPage: string;
  selectedPosition: string;
  type: string;
}
const SignPage: React.FC<SignatureOptionsProps> = ({
  files,
  selectedProfile,
  isVisibleSignature,
  selectedPage,
  selectedPosition,
  type,
}) => {
  const [msh, setMsh] = useState<Uint8Array | null>(null);
  const [message, setMessage] = useState<string>("");
  const [messageType, setMessageType] = useState<"success" | "error" | "">("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [nexuMessage, setNexuMessage] = useState<string>("");

  const [isSigningPrepared, setIsSigningPrepared] = useState(false);
  const documentSignerRef = useRef<DocumentSignerRef>(null);
  const nexuClientRef = useRef<any | null>(null);

  const signingQueueRef = useRef([]);
  const isProcessingRef = useRef(false);
  const certificatesData = useRef<CertificateData | null>(null);
  const successfullySignedFiles = useRef(0);
  const [showPopup, setShowPopup] = useState(false);

  const signingStartTimeRef = useRef<number | null>(null);
  const hashReceivedTimeRef = useRef<number | null>(null);

  var receivedHashes = 0;

  const requestId = crypto.randomUUID();

  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    const eventSource = new EventSource(
      `${config.EVENT_SOURCE}?requestId=${requestId}`,

      {
        withCredentials: true,
      }
    );
    eventSourceRef.current = eventSource;

    eventSource.addEventListener("hash-ready", (event) => {
      const payload = JSON.parse(event.data);
      console.log("Hash primit:", payload);
      signingQueueRef.current.push(payload);
      receivedHashes++;

      if (receivedHashes === 1) {
        const endTime = performance.now();
        const durationSeconds =
          (endTime - (hashReceivedTimeRef.current || 0)) / 1000;
        console.log(`Am primit primul hash in ${durationSeconds} secunde`);
        processQueue();
      }

      if (receivedHashes >= files.length) {
        console.log("Toate hash-urile primite. Închid conexiunea SSE.");
        eventSource.close();
        eventSourceRef.current = null;
      }
    });

    eventSource.addEventListener("ping", (event) => {
      console.log("Ping primit:", event.data);
    });

    return () => {
      eventSource.close();
      eventSourceRef.current = null;
    };
  }, []);

  useEffect(() => {
    handlePrepareSigning();
  }, []);

  const formatHash = (hash: string) => {
    const binaryString = atob(hash);
    const uint8Array = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
      uint8Array[i] = binaryString.charCodeAt(i);
    }
    setMsh(uint8Array);
    return uint8Array;
  };

  const processQueue = async () => {
    if (isProcessingRef.current) return;
    if (signingQueueRef.current.length === 0) return;

    isProcessingRef.current = true;

    const item = signingQueueRef.current.shift();

    const { fileId, hashBase64 } = item;

    const cleanHash = new Uint8Array(formatHash(hashBase64));

    try {
      const result = await sign(cleanHash, fileId);
      if (result?.error) {
        console.error("Signing failed:", result.error);
        return;
      }

      successfullySignedFiles.current++;
    } catch (err) {
      console.error("Eroare la semnare:", err);
    } finally {
      isProcessingRef.current = false;

      if (signingQueueRef.current.length > 0) {
        processQueue();
      } else {
        console.log(
          "semnate cu succes",
          successfullySignedFiles.current + " din ",
          files.length
        );
        const endTime = performance.now();
        const durationSeconds =
          (endTime - (signingStartTimeRef.current || 0)) / 1000;
        console.log(
          `Toate fisierele au fost semnate in ${durationSeconds} secunde`
        );
        if (successfullySignedFiles.current === files.length) {
          const endTime = performance.now();
          const durationSeconds =
            (endTime - (signingStartTimeRef.current || 0)) / 1000;
          console.log(
            `Toate fisierele au fost semnate in ${durationSeconds} secunde`
          );
          setShowPopup(true);
        }
      }
    }
  };

  useEffect(() => {
    const checkNexUReady = async () => {
      if (nexuClientRef.current) {
        const isReady = await nexuClientRef.current.isReady();

        if (isReady) {
          setNexuMessage("NexU is ready for signing.");
          setMessageType("success");
        } else {
          setNexuMessage("NexU is NOT ready. Please check connection.");
          setMessageType("error");
        }
      } else {
        setNexuMessage("NexU client is not initialized.");
        setMessageType("error");
      }
    };
    checkNexUReady();
    const interval = setInterval(checkNexUReady, 100);

    return () => clearInterval(interval);
  }, []);

  const sendFilesToBackend = async (dataToSend) => {
    try {
      await apiClient.post("/file/upload/csc/inMemBulk/stream", dataToSend, {
        headers: { "Content-Type": "application/json" },
      });
      signingStartTimeRef.current = performance.now();
      hashReceivedTimeRef.current = performance.now();
      console.log("A trimis cererea catre backend pentru upload!");
    } catch (error) {
      console.error("File upload failed:", error);
      return;
    }
  };

  const handlePrepareSigning = async () => {
    if (!documentSignerRef.current) {
      setNexuMessage("DocumentSigner ref is not initialized.");
      setMessageType("error");
      return;
    }

    if (documentSignerRef.current) {
      try {
        certificatesData.current =
          await documentSignerRef.current.prepareSigning();

        if (certificatesData.current) {
          console.log("Certificates data:", certificatesData.current);

          const onlyCert = {
            certificate: certificatesData.current.certificate,
            certificateChain: certificatesData.current.certificateChain,
            encryptionAlgorithm: certificatesData.current.encryptionAlgorithm,
          };

          const encodedCertificates = encodeURIComponent(
            JSON.stringify(onlyCert)
          );

          const data = {
            requestId: requestId,
            certificatesData: encodedCertificates,
            files: files.map((file) => ({
              fileId: file.fileId,
              filename: file.filename,
              fileUrl: file.fileUrl,
            })),
            profile: selectedProfile,
            visibleSignature: isVisibleSignature,
            page: selectedPage,
            position: selectedPosition,
            type: type,
          };

          await sendFilesToBackend(data);
        }
      } catch (error) {
        setMessage("Error during signing preparation.");
        console.error("Error during signing preparation:", error);
      }
    }
  };

  const sendSignatureToBackend = async (data) => {
    try {
      await apiClient.post("/file/receiveSignature/inMemFile", data);
    } catch (error) {
      console.error("Eroare la trimiterea semnaturii catre backend:", error);
    }
  };

  const sign = async (hash: Uint8Array, fileId: string) => {
    if (certificatesData.current && hash) {
      const onlySign = {
        tokenId: certificatesData.current.tokenId,
        keyId: certificatesData.current.keyId,
        digestAlgorithm: "SHA256",
      };

      console.log("Inainte de apel getDataToSign");
      console.log("Ce trimit la semnat:", onlySign, hash);
      const signedHash = await documentSignerRef.current?.getSignNexU(
        onlySign,
        hash
      );

      if (!signedHash || signedHash.error) {
        console.error(
          "Signing failed or was cancelled:",
          signedHash?.error || "Unknown error"
        );

        return;
      }

      const data = {
        fileId: fileId,
        hashValue: btoa(String.fromCharCode.apply(null, hash)),
        signatureValue: signedHash,
      };

      console.log("ce trimit in backend:", data);

      sendSignatureToBackend(data);

      console.log("Dupa apel getDataToSign");

      setIsSigningPrepared(true);
    }
  };

  return (
    <div>
      <DocumentSigner ref={documentSignerRef} />
      <NexuClient ref={nexuClientRef} />

      <div>
        <PopupSuccess
          visible={showPopup}
          onHide={() => setShowPopup(false)}
          fileName={files.map((file) => file.filename).join(", ")}
          message="Files signed successfuly!"
        />
      </div>
    </div>
  );
};

export default SignPage;
