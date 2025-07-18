import React, { use } from "react";
import { Button } from "primereact/button";
import apiClient from "../../Utils/ApiClient.tsx";
import { useNavigate } from "react-router-dom";
import { useEffect, useState, useRef } from "react";
import axios from "axios";
import PopupSuccess from "../PopupSuccess/PopupSuccess.tsx";
import config from "../../Config/config.tsx";
import "./CloudBulk.css";

interface CloudBulkProps {
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

const CloudBulk: React.FC<CloudBulkProps> = ({
  files,
  selectedProfile,
  isVisibleSignature,
  selectedPage,
  selectedPosition,
  type,
}) => {
  const [SAD, setSAD] = useState(null);
  const [showPopup, setShowPopup] = useState(false);
  const hashToSignRef = useRef<string | null>(null);
  const signingQueueRef = useRef([]);
  const csc_token = useRef<string | null>(null);
  const csc_expires_in = useRef<string | null>(null);
  const credeantialIdRef = useRef<string>(null);
  const keyAlgoRef = useRef<string>(null);
  const signAlgoRef = useRef<string>(null);
  const receivedHashesRef = useRef(0);
  const fileIdRef = useRef<string | null>(null);
  const signingStartTimeRef = useRef<number | null>(null);
  const hashGenerationStartTimeRef = useRef<number | null>(null);
  const firstHashRef = useRef<boolean | null>(null);

  let signingInProgress = false;
  let first = true;

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
      receivedHashesRef.current += 1;

      if (receivedHashesRef.current === 1) {
        firstHashRef.current = true;
        processQueue();
      }

      if (receivedHashesRef.current >= files.length) {
        console.log("Toate hash-urile primite. Inchid conexiunea SSE.");
        eventSource.close();
        eventSourceRef.current = null;
      }
    });

    eventSource.addEventListener("ping", (event) => {
      console.log("Ping primit:", event.data);
    });

    eventSource.addEventListener("done", () => {
      console.log("Toate hash-urile semnalate de server. inchid SSE.");
      eventSource.close();
      eventSourceRef.current = null;
    });

    return () => {
      eventSource.close();
      eventSourceRef.current = null;
    };
  }, [requestId, files]);

  async function processQueue() {
    if (signingInProgress || signingQueueRef.current.length === 0) return;

    signingInProgress = true;
    const { fileId, hashBase64 } = signingQueueRef.current.shift();
    fileIdRef.current = fileId;

    console.log("ce iau din lista si trimit la semnare:", hashBase64);

    try {
      const signature = await signWithCSC(hashBase64);
    } catch (err) {
      console.error("Semnare esuata:", err);
    }
  }

  async function signWithCSC(base64Hash) {
    const cleanHash = base64Hash.trim();
    hashToSignRef.current = cleanHash;

    if (first) {
      console.log("prima");

      await sendOtpRequest(cleanHash);
      first = false;
    }
    return null;
  }

  const handleWithCSC = async () => {
    try {
      const tokenValid = await getValidToken();
      const cert = await fetchCertificates(tokenValid);
      handleUploadFile(cert?.certificatesReturn);
    } catch (error) {
      console.error(
        "Error in fetching access token or sending request:",
        error
      );
    }
  };

  async function sendSignatureToBackend(data) {
    console.log("Ce trimit la backend:", data);
    const responseServer = await apiClient.post(
      "/file/receiveSignature/inMemFile",
      data
    );
  }

  const extendTransaction = async (
    credentialID: string,
    sad: string,
    token: string,
    hashArray: any
  ) => {
    console.log(
      "Ce trimit cand fac extendTransaction: credeantialId",
      credentialID,
      " SAD: ",
      sad,
      " hashArray: ",
      hashArray
    );
    const response = await axios.post(
      `${config.CSC_PROVIDER_URL}/credentials/extendTransaction`,
      {
        credentialID,
        SAD: sad,
        hash: hashArray,
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );

    return response.data.SAD;
  };

  async function signRemainingHashesSequentially(
    initialSAD: string,
    cscAccessToken: string,
    signatureReqData: any
  ) {
    let currentSAD = initialSAD;

    for (let i = 0; i < signingQueueRef.current.length; i++) {
      const { fileId, hashBase64 } = signingQueueRef.current[i];

      try {
        currentSAD = await extendTransaction(
          signatureReqData.credentialID,
          currentSAD,
          cscAccessToken,
          [hashBase64]
        );

        console.log("SAD extins:", currentSAD);

        signatureReqData.SAD = currentSAD;
        signatureReqData.hash = [hashBase64];

        const signResp = await axios.post(
          `${config.CSC_PROVIDER_URL}/signatures/signHash`,
          {
            hash: [hashBase64],
            credentialID: signatureReqData.credentialID,
            SAD: currentSAD,
            signAlgo: signatureReqData.signAlgo,
          },
          {
            headers: {
              Authorization: `Bearer ${cscAccessToken}`,
            },
          }
        );

        const signature = signResp.data.signatures[0];

        await apiClient.post("/file/receiveSignature/inMemFile", {
          fileId: fileId,
          hashValue: hashBase64,
          signatureValue: signature,
        });
      } catch (err) {
        console.error(`Eroare la semnarea hash-ului ${i + 1}:`, err);
        console.error(`Status: ${err.response.status}`);
        return;
      }
    }

    signingInProgress = false;
    receivedHashesRef.current = 0;
    first = true;
    fileIdRef.current = "";
    setShowPopup(true);
  }

  const isTokenExpired = (expires_in) => {
    if (!expires_in) return true;

    const now = Date.now();
    return now >= parseInt(expires_in) * 1000;
  };

  const getTokenFromStorage = async () => {
    try {
      const responseToken = await apiClient.get("/csc/extractToken", {
        withCredentials: true,
      });

      const tokenData = {
        token: responseToken.data.access_token,
        expires_in: responseToken.data.expires_in,
      };
      console.log("Token extras din cookie:", tokenData);
      return tokenData;
    } catch (error) {
      console.error("No csc token cookie");
      return null;
    }
  };

  const startTokenFlow = async () => {
    return new Promise((resolve, reject) => {
      const state = "refresh_step";
      const authUrl = `${
        config.CSC_PROVIDER_URL
      }/oauth2/authorize?response_type=code&client_id=${
        config.CSC_CLINET_ID
      }&redirect_uri=${encodeURIComponent(
        config.CSC_REDIRECT_URI
      )}&scope=service&lang=en-US&state=${state}`;

      console.log("Auth URL:", authUrl);

      const popup = window.open(
        authUrl,
        "transspedLogin",
        "width=600,height=700"
      );

      const listener = async (event) => {
        if (event.origin !== config.FRONTEND_URL) return;

        const { code, type } = event.data;

        if (type === "auth" || type === "refresh_step") {
          window.removeEventListener("message", listener);
          popup?.close();

          try {
            const tokenResponse = await apiClient.get("/csc/token", {
              params: { code },
            });

            console.log("Token primit din backend:", tokenResponse.data);

            const token = tokenResponse.data.access_token;
            const expiresIn = tokenResponse.data.expires_in;
            const expirationTime = Date.now() + expiresIn * 1000;
            csc_token.current = token;
            csc_expires_in.current = expirationTime.toString();
            resolve(token);
          } catch (err) {
            reject(err);
          }
        }
      };

      window.addEventListener("message", listener);
    });
  };

  const getValidToken = async () => {
    if (!csc_token.current && !csc_expires_in.current) {
      const tokenData = await getTokenFromStorage();
      if (tokenData === null) {
        await startTokenFlow();
        return csc_token.current;
      }

      csc_token.current = tokenData.token;
      csc_expires_in.current = tokenData.expires_in;
    }

    if (isTokenExpired(csc_expires_in.current)) {
      await startTokenFlow();
      return csc_token.current;
    } else {
      return csc_token.current;
    }
  };

  useEffect(() => {
    const listener = async (event) => {
      if (event.origin !== config.FRONTEND_URL) return;

      const { code, type } = event.data;
      if (!code || !type) return;

      if (type === "auth" || type === "refresh_step") {
        return;
      }

      if (type === "sad") {
        console.log("SAD step triggered");
        await fetchToken2(code);
      }
    };

    window.addEventListener("message", listener);

    return () => window.removeEventListener("message", listener);
  }, []);

  const fetchCertificates = async (token) => {
    try {
      const data = {
        maxResults: 10,
      };

      await getValidToken();

      const certResponse = await axios.post(
        `${config.CSC_PROVIDER_URL}/credentials/list`,
        data,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${csc_token.current}`,
          },
        }
      );

      const credentialID = certResponse.data.credentialIDs[0];
      console.log("Credential ID retrieved:", credentialID);
      credeantialIdRef.current = credentialID;

      if (credentialID) {
        const certInfoResponse = await axios.post(
          `${config.CSC_PROVIDER_URL}/credentials/info`,
          {
            credentialID: credentialID,
            certificates: "chain",
            certInfo: true,
            authInfo: true,
          },
          {
            headers: {
              Authorization: `Bearer ${token}`,
              "Content-Type": "application/json",
            },
          }
        );

        keyAlgoRef.current = certInfoResponse.data.key.algo[2];
        signAlgoRef.current = certInfoResponse.data.key.algo[2];
        sessionStorage.setItem("sign_algo", signAlgoRef.current);

        const certificatesReturn = certInfoResponse.data.cert.certificates;
        return { certificatesReturn, credentialID };
      }
    } catch (error) {
      console.error("Error in fetching certificates:", error);
      return;
    }
  };

  const sendFilesToBackend = async (dataToSend) => {
    try {
      await apiClient.post("/file/upload/csc/inMemBulk/stream", dataToSend, {
        headers: { "Content-Type": "application/json" },
      });

      signingStartTimeRef.current = performance.now();
      hashGenerationStartTimeRef.current = performance.now();
    } catch (error) {
      console.error("File upload failed:", error);
      return;
    }
  };

  const handleUploadFile = async (cert) => {
    if (!cert) {
      console.log("No certificates available");
      return;
    }

    const certificatesData = {
      certificate: cert[0],
      certificateChain: cert,
    };

    const encodedCertificatesData = encodeURIComponent(
      JSON.stringify(certificatesData)
    );

    const data = {
      requestId: requestId,
      certificatesData: encodedCertificatesData,
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
  };

  const sendOtpRequest = async (hash) => {
    const base64urlHashes = hash.replace(/\+/g, "-").replace(/\//g, "_");
    const hashesArray = Array.from(
      { length: files.length },
      () => base64urlHashes
    );
    const hashParam = hashesArray;

    const authUrlSign = `${
      config.CSC_PROVIDER_URL
    }/oauth2/authorize?response_type=code&client_id=${
      config.CSC_CLINET_ID
    }&redirect_uri=${encodeURIComponent(
      config.CSC_REDIRECT_URI
    )}&scope=credential&credentialID=${
      credeantialIdRef.current
    }&numSignatures=${hashParam.length}&hash=${hashParam}&state=sad`;

    const popup = window.open(
      authUrlSign,
      "transspedSignPopup",
      "width=600,height=700"
    );
  };

  const fetchToken2 = async (authorizationCode) => {
    try {
      console.log("A inceput fetchToken2");
      const tokenResponse = await apiClient.post(
        `/csc/sadToken?code=${authorizationCode}`,
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      const tokenSAD = tokenResponse.data.access_token;
      setSAD(tokenSAD);

      const rawHashed = hashToSignRef.current;
      const hashArray = [rawHashed];

      const access_token = await getValidToken();
      var tokenSADRenew;
      tokenSADRenew = await extendTransaction(
        credeantialIdRef.current,
        tokenSAD,
        access_token,
        hashArray
      );

      const data = {
        credentialID: credeantialIdRef.current,
        SAD: tokenSADRenew,
        hashes: hashArray,
        hashAlgo: "2.16.840.1.101.3.4.2.1",
        signAlgo: signAlgoRef.current,
      };

      const response = await axios.post(
        `${config.CSC_PROVIDER_URL}/signatures/signHash`,
        data,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${access_token}`,
          },
        }
      );

      const signedSignatures = response.data.signatures;

      const reqData = {
        fileId: fileIdRef.current,
        signatureValue: signedSignatures[0],
        hashValue: hashToSignRef.current,
      };

      await sendSignatureToBackend(reqData);

      await signRemainingHashesSequentially(tokenSADRenew, access_token, data);
    } catch (error) {
      if (error.response) {
        console.error("Error in fetching access token or sending request:");
        console.error("Status Code:", error.response.status);
      } else {
        console.error(
          "Error in fetching access token or sending request:",
          error.message
        );
      }
    }
  };

  return (
    <div className="container-cloud-signature">
      <Button
        label="Cloud Signature"
        className="p-button signature-btn"
        onClick={() => handleWithCSC()}
      />

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

export default CloudBulk;
