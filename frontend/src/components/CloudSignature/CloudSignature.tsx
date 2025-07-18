import React from "react";
import { Button } from "primereact/button";
import "./CloudSignature.css";
import apiClient from "../../Utils/ApiClient.tsx";
import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "axios";
import PopupSuccess from "../PopupSuccess/PopupSuccess.tsx";
import config from "../../Config/config.tsx";

interface CloudSignatureProps {
  fileId: string;
  fileUrl: string;
  filename: string;
  selectedProfile: string;
  isVisibleSignature: boolean;
  selectedPage: string;
  selectedPosition: string;
  type: string;
}

var sign_algo;
// const client_id = "mta-test";
// const redirectUri = "http://localhost:3000/csc";
const CloudSignature: React.FC<CloudSignatureProps> = ({
  fileId,
  fileUrl,
  filename,
  selectedProfile,
  isVisibleSignature,
  selectedPage,
  selectedPosition,
  type,
}) => {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [certificates, setCertificates] = useState(null);
  const [keyAlgo, setKeyAlgo] = useState(null);
  const [hash, setHash] = useState(null);
  const [credentialID, setCredentialID] = useState(null);
  const [SAD, setSAD] = useState(null);
  const [showPopup, setShowPopup] = useState(false);

  const isTokenExpired = () => {
    const expirationTime = sessionStorage.getItem("tokenExpiresAt");

    if (!expirationTime) return true;

    const now = Date.now();
    return now >= parseInt(expirationTime);
  };

  const getValidToken = async () => {
    if (isTokenExpired()) {
      console.log("Token expirat. Refac login...");

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

              const token = tokenResponse.data.access_token;
              const expiresIn = tokenResponse.data.expires_in;
              const expirationTime = Date.now() + expiresIn * 1000;

              sessionStorage.setItem(
                "tokenExpiresAt",
                expirationTime.toString()
              );
              localStorage.setItem("access_token", token);
              setAccessToken(token);
              resolve(token);
            } catch (err) {
              reject(err);
            }
          }
        };

        window.addEventListener("message", listener);
      });
    } else {
      return localStorage.getItem("access_token");
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

  const handleWithCSC = async () => {
    try {
      //iau certificat
      const tokenValid = await getValidToken();
      const cert = await fetchCertificates(tokenValid);
      const receivedHash = await handleUploadFile(cert?.certificatesReturn);
      await sendOtpRequest(receivedHash, cert?.credentialID);
    } catch (error) {
      console.error(
        "Error in fetching access token or sending request:",
        error
      );
    }
  };

  const fetchCertificates = async (token) => {
    try {
      const data = {
        maxResults: 10,
      };

      console.log("access token:", token);

      token = await getValidToken();

      const certResponse = await axios.post(
        `${config.CSC_PROVIDER_URL}/credentials/list`,
        data,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const credentialID = certResponse.data.credentialIDs[0];
      console.log("Credential ID retrieved:", credentialID);
      setCredentialID(credentialID);
      sessionStorage.setItem("credentialID", credentialID);

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
        console.log("Certificate Info:", certInfoResponse.data);

        setCertificates(certInfoResponse.data.cert.certificates);
        console.log("Certificates:", certInfoResponse.data.key.algo[2]);
        setKeyAlgo(certInfoResponse.data.key.algo[2]);
        sign_algo = certInfoResponse.data.key.algo[2];
        console.log("Key Algo:", sign_algo);
        sessionStorage.setItem("sign_algo", sign_algo);

        const certificatesReturn = certInfoResponse.data.cert.certificates;

        return { certificatesReturn, credentialID };
      }
    } catch (error) {
      console.error("Error in fetching certificates:", error);
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
      // encryptionAlgorithm: selectedAlgo,
    };

    console.log("Certificate chain:", cert);

    const encodedCertificatesData = encodeURIComponent(
      JSON.stringify(certificatesData)
    );

    const data = {
      certificatesData: encodedCertificatesData,
      filename: filename,
      id: fileId,
      profile: selectedProfile,
      visibleSignature: isVisibleSignature,
      page: selectedPage,
      position: selectedPosition,
      type: type,
    };

    console.log("date back:" + data);

    try {
      const response = await apiClient.post(
        "/file/upload/csc/inMemFile",
        data,
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      console.log(
        "File and certificates uploaded successfully:",
        response.data
      );

      const hashValue = response.data;
      const cleanHash = hashValue.trim();
      setHash(hashValue);
      sessionStorage.setItem("hash", cleanHash);
      console.log("Hash primit în frontend:", cleanHash);

      await new Promise((resolve) => setTimeout(resolve, 2000));
      console.log("Upload finalizat!");

      return cleanHash;
    } catch (error) {
      console.error("File upload failed:", error);
      return;
    }
  };

  const sendOtpRequest = async (hash, credentialIDReq) => {
    console.log("Hash primit in sendOtpRequest:", hash);
    const myHash = hash;
    console.log("Valoare hash storage:" + myHash);
    console.log("valoare hash simpla" + hash);

    const encodedHash = myHash.replace(/\+/g, "-").replace(/\//g, "_");

    const authUrlSign = `${
      config.CSC_PROVIDER_URL
    }/oauth2/authorize?response_type=code&client_id=${
      config.CSC_CLINET_ID
    }&redirect_uri=${encodeURIComponent(
      config.CSC_REDIRECT_URI
    )}&scope=credential&credentialID=${credentialIDReq}&numSignatures=1&hash=${encodedHash}&state=sad`;

    const popup = window.open(
      authUrlSign,
      "transspedSignPopup",
      "width=600,height=700"
    );
  };

  const fetchToken2 = async (authorizationCode) => {
    try {
      const tokenResponse = await apiClient.post(
        `/csc/sadToken?code=${authorizationCode}`,
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      const tokenSAD = tokenResponse.data.access_token;
      console.log("Access Token2:", tokenSAD);
      setSAD(tokenSAD);

      sessionStorage.setItem("stopRequests", "true");

      var credentialID = sessionStorage.getItem("credentialID");
      console.log("CredentialID:" + credentialID);

      const myHash = sessionStorage.getItem("hash");
      console.log("Hash:" + myHash);

      const access_token = await getValidToken();
      const signatureAlgo = sessionStorage.getItem("sign_algo");
      console.log("access_token: obtinut prima oara: " + access_token);
      const data = {
        credentialID: credentialID,
        SAD: tokenSAD,
        hash: [myHash],
        hashAlgo: "2.16.840.1.101.3.4.2.1",
        signAlgo: signatureAlgo || "TestValue",
      };

      console.log("DATA: " + JSON.stringify(data, null, 2));

      console.log("Se trimite hash catre csc");

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
      console.log("Server Response:", response.data);
      console.log("myHash2:" + myHash);
      const signatureValue = response.data.signatures[0];
      const requestData = {
        signatureValue: signatureValue,
        hashValue: myHash,
      };

      console.log("Request Data:", requestData);

      const responseServer = await apiClient.post(
        "/file/receiveSignature/inMemFile",
        requestData
      );
      const responseData = responseServer.data;
      if (!responseData) {
        throw new Error("Failed to send signature value to the server.");
      } else {
        console.log("Raspuns primit de la backend:", responseData);

        console.log("Am terminat semnarea");
        setShowPopup(true);
      }
    } catch (error) {
      if (error.response) {
        console.error("Error in fetching access token or sending request:");
        console.error("Status Code:", error.response.status);
        console.error("Error Message:", error.response.statusText);
        console.error("Error Data:", error.response.data);
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
      <div>
        <PopupSuccess
          visible={showPopup}
          onHide={() => setShowPopup(false)}
          fileName={filename}
          message="Has been signed successfully!"
        />
      </div>
    </div>
  );
};

export default CloudSignature;
