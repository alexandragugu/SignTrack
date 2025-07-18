import React, {
  useImperativeHandle,
  forwardRef,
  useState,
  useEffect,
} from "react";
import axios from "axios";
import "./DocumentSigner.css";
import { useNavigate } from "react-router-dom";
import config from "./Config/config.tsx";

import PopupSuccess from "./components/PopupSuccess/PopupSuccess.tsx";
import apiClient from "./Utils/ApiClient.tsx";

export interface CertificateData {
  certificate: string;
  certificateChain: string[];
  encryptionAlgorithm: string;
  digestAlgorithm: string;
  tokenId: string;
  keyId: string;
}

interface OnlySign {
  tokenId: string;
  keyId: string;
  digestAlgorithm: "SHA256";
}

export interface DocumentSignerRef {
  prepareSigning: () => Promise<CertificateData | null>;
  getDataToSign: (
    certificateData: OnlySign,
    msh: Uint8Array | null
  ) => Promise<string | null>;

  getSignNexU: (
    certificateData: OnlySign,
    msh: Uint8Array | null
  ) => Promise<string | null>;
}

const DocumentSigner = forwardRef<DocumentSignerRef, {}>((props, ref) => {
  const [progress, setProgress] = useState<number>(0);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);
  const [msh, setMsh] = useState<Buffer | null>(null);
  const [signatureSent, setSignatureSent] = useState<boolean>(false);
  const [isSigning, setIsSigning] = useState(false);
  const navigate = useNavigate();
  const [filename, setFilename] = useState<string | null>(null);
  const [fileUrl, setFileUrl] = useState<string | null>(null);
  const [inMem, setInMem] = useState<boolean>(false);
  const [showPopup, setShowPopup] = useState(false);
  const [fileId, setFileId] = useState<string | null>(null);

  useImperativeHandle(ref, () => ({
    prepareSigning: async () => {
      try {
        const certificatesData = await getCertificates();
        return certificatesData ?? null;
      } catch (error) {
        console.error("Error preparing signing:", error);
        throw error;
      }
    },

    getDataToSign: async (
      certificateData: OnlySign,
      msh: Uint8Array | null
    ) => {
      try {
        if (!msh) {
          console.error(certificateData);
          return null;
        }
        return await handleGetDataToSign(certificateData, new Uint8Array(msh));
      } catch (error) {
        console.error("Error getting data to sign:", error);
        throw error;
      }
    },

    getSignNexU: async (certificateData: OnlySign, msh: Uint8Array | null) => {
      try {
        console.log("A intrat in functie");
        if (!msh) {
          console.error("msh este null și nu poate fi folosit.");
          console.error(certificateData);
          return null;
        }
        return await handleGetSignNexU(certificateData, new Uint8Array(msh));
      } catch (error) {
        console.error("Error getting data to sign:", error);
        throw error;
      }
    },
  }));

  useEffect(() => {
    console.log("DocumentSigner has mounted.");
    const script = document.createElement("script");
    //script.src = "https://localhost:9895/nexu.js";
    script.src = `${config.NEXU_URL}nexu.js`;
    script.async = true;
    document.body.appendChild(script);

    return () => {
      document.body.removeChild(script);
    };
  }, []);

  const getCertificates = async () => {
    updateProgress("Loading certificates...", 10);
    try {
      const response = await nexu_get_certificates();
      if (response) {
        updateProgress("Certificates loaded.", 25);
      }
      return response;
    } catch (err) {
      handleError(err);
    }
  };

  const handleGetDataToSign = async (
    certificateData: OnlySign,
    msh: Uint8Array
  ): Promise<string | null> => {
    if (!certificateData) {
      handleError(new Error("Error while reading the Smart Card"));
      return null;
    }

    const toSend = {
      tokenId: certificateData.tokenId,
      digestAlgorithm: certificateData.digestAlgorithm,
      keyId: certificateData.keyId,
      toBeSigned: {
        bytes: btoa(String.fromCharCode.apply(null, new Uint8Array(msh))),
      },
    };

    try {
      const response = await fetch(`${config.NEXU_URL}rest/sign`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(toSend),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error("Server responded with error:", errorText);
        throw new Error(
          `Failed to sign document. Server responded with: ${errorText}`
        );
      }

      const responseData = await response.json();
      console.log("Valoare imediat dupa semnare:", responseData);
      return responseData.response.signatureValue;
    } catch (error) {
      handleError(error);
      return null;
    }
  };

  const handleGetSignNexU = async (
    certificateData: OnlySign,
    msh: Uint8Array
  ): Promise<string | null> => {
    if (!certificateData) {
      handleError(new Error("Error while reading the Smart Card"));
      return null;
    }
    const toSend = {
      tokenId: certificateData.tokenId,
      digestAlgorithm: certificateData.digestAlgorithm,
      keyId: certificateData.keyId,
      toBeSigned: {
        bytes: btoa(String.fromCharCode.apply(null, new Uint8Array(msh))),
      },
      keepTokenAlive: true,
    };

    try {
      // const response = await fetch("https://localhost:9895/rest/sign", {
      const response = await fetch(`${config.NEXU_URL}rest/sign`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(toSend),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error("Server responded with error:", errorText);
        throw new Error(
          `Failed to sign document. Server responded with: ${errorText}`
        );
      }

      const responseData = await response.json();
      return responseData.response.signatureValue;
    } catch (error) {
      handleError(error);
      return null;
    }
  };

  const handleError = (error: Error) => {
    console.error(error);
    setError(error.message);
    setProgress(0);
    setSuccess(false);
  };

  const updateProgress = (action: string, percent: number) => {
    console.log(action);
    setProgress(percent);
  };

  const sendSignatureToServer = async (
    responseData: any,
    msh: Uint8Array | null
  ) => {
    const signatureValue = responseData.response.signatureValue;
    if (!signatureValue) {
      console.error("Signature value not found in response data.");
      return;
    }

    try {
      if (typeof signatureValue !== "string" || !msh) {
        console.error("Invalid signatureValue or msh.");
        return;
      }

      const requestData = {
        signatureValue: signatureValue,
        hashValue: btoa(String.fromCharCode.apply(null, msh)),
      };

      const response = await apiClient.post(
        "/file/receiveSignature",
        requestData
      );

      if (!response.data) {
        throw new Error("Failed to send signature value to the server.");
      }

      console.log("Signature sent successfully.");
      setSignatureSent(true);
      setSuccess(true);
      console.log("Raspuns primit de la backend:", responseData);

      setFilename(responseData.filename);
      setFileUrl(responseData.url);
      setFileId(responseData.fileId);

      console.log("NUMELE FISIERULUI BUCKET:" + responseData.filename);
      console.log("URL-ul fisierului:" + responseData.url);
    } catch (error) {
      setSignatureSent(false);
      console.error("Error sending signature value to server:", error);
    }
  };

  const sendSignInMemFile = async (
    responseData: any,
    msh: Uint8Array | null
  ) => {
    const signatureValue = responseData.response.signatureValue;
    if (!signatureValue) {
      console.error("Signature value not found in response data.");
      return;
    }

    try {
      if (typeof signatureValue !== "string" || !msh) {
        console.error("Invalid signatureValue or msh.");
        return;
      }

      const requestData = {
        signatureValue: signatureValue,
        hashValue: btoa(String.fromCharCode.apply(null, msh)),
      };

      try {
        const response = await apiClient.post(
          "/file/receiveSignature/inMemFile",
          requestData
        );
      } catch (error) {
        console.error("Error sending signature value:", error);
        throw new Error("Failed to send signature value to the server.");
      }

      console.log("Signature sent successfully.");
      setSignatureSent(true);
      setSuccess(true);
      const responseData = await response.json();
      console.log("Raspuns primit de la backend:", responseData);

      setFilename(responseData.filename);
      setFileUrl(responseData.url);

      setInMem(true);

      console.log("NUMELE FISIERULUI BUCKET:" + responseData.filename);
      console.log("URL-ul fisierului:" + responseData.url);
      // navigate("/");
    } catch (error) {
      setSignatureSent(false);
      console.error("Error sending signature value to server:", error);
    }
  };

  const nexu_get_certificates = async () => {
    try {
      const response = await axios.get(
        // "https://localhost:9895/rest/certificates",
        `${config.NEXU_URL}rest/certificates`,

        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      if (response.data && response.data.success) {
        const structuredData: CertificateData = {
          certificate: response.data.response.certificate,
          certificateChain: response.data.response.certificateChain,
          encryptionAlgorithm: response.data.response.encryptionAlgorithm,
          tokenId: response.data.response.tokenId,
          keyId: response.data.response.keyId,
          digestAlgorithm: response.data.response.digestAlgorithm,
        };

        console.log(
          "certificate chain:",
          response.data.response.certificateChain
        );
        return structuredData;
      }
    } catch (error) {
      console.error("Error fetching certificates:", error);
      throw error;
    }
  };

  useEffect(() => {
    if (progress === 100 && success) {
      // if (inMem) {
      setShowPopup(true);
      // } else {
      //   navigate(
      //     `/documentReady?filename=${encodeURIComponent(
      //       filename
      //     )}&fileUrl=${encodeURIComponent(fileUrl)}`,
      //     {
      //       state: { fileId },
      //     }
      //   );
      // }
    }
  }, [progress, success, navigate, filename, fileUrl, fileId]);

  return (
    <div>
      <PopupSuccess
        visible={showPopup}
        onHide={() => setShowPopup(false)}
        fileName={filename}
        message="Has been signed successfully!"
      />
    </div>
  );
});

export default DocumentSigner;
