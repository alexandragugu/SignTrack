import React, {
  useRef,
  useEffect,
  useState,
  forwardRef,
  useImperativeHandle,
} from "react";
import DocumentSigner, { DocumentSignerRef } from "../DocumentSigner.tsx";
import "./NexU.css";
import config from "../Config/config.tsx";

const NexuClient = forwardRef((_, ref) => {
  const [nexuStatus, setNexuStatus] = useState<string>("loading");
  const nexuVersion = "1.";
  const submitButtonRef = useRef<HTMLButtonElement>(null);
  const documentSignerRef = useRef<DocumentSignerRef>(null);
  const [msh, setMsh] = useState<string>("");

  const isReady = () => nexuStatus === "Compatible";

  useImperativeHandle(ref, () => ({
    isReady,
  }));

  const loadingScript = (onSuccess: () => void) => {
    fetch(`${config.NEXU_URL}nexu.js`)
      .then((response) => {
        if (!response.ok) {
          console.error(
            "Unable to load Nexu",
            response.status,
            response.statusText
          );
          return;
        }
        return response.text();
      })
      .then((scriptContent) => {
        if (scriptContent) {
          const script = document.createElement("script");
          script.type = "text/javascript";
          script.text = scriptContent;
          document.body.appendChild(script);
          onSuccess();
        }
      })
      .catch((error) => console.error("Error loading NexU script:", error));
  };

  useEffect(() => {
    fetch(`${config.NEXU_URL}nexu-info`)
      .then((response) => response.json())
      .then((data) => {
        if (data.version && data.version.startsWith(nexuVersion)) {
          setNexuStatus("Compatible");
          loadingScript(() => {
            if (submitButtonRef.current) {
              submitButtonRef.current.disabled = false;
            }
          });
        } else {
          setNexuStatus("updateNeeded");
        }
      })
      .catch(() => {
        setNexuStatus("notInstalled");
      });
  }, []);

  const handleInstall = () => {
    window.location.href =
      "https://github.com/nowina-solutions/nexu/releases/download/nexu-1.22/nexu-bundle-1.22.zip";
  };

  return (
    // <div className="nexu-client-wrapper">
    //   <h1 className="header">NexU Status</h1>

    //   {nexuStatus === "loading" && <p>Checking NexU status...</p>}

    //   {nexuStatus === "Compatible" && (
    //     <div>
    //       <p id="nexu_ready_alert">NexU is ready!</p>
    //     </div>
    //   )}

    //   {nexuStatus === "updateNeeded" && (
    //     <div>
    //       <p>NexU needs an update</p>
    //       <button
    //         onClick={() => console.log("Update NexU")}
    //         className="action-button"
    //       >
    //         Update NexU
    //       </button>
    //     </div>
    //   )}

    //   {nexuStatus === "notInstalled" && (
    //     <div>
    //       <p id="warning-text">NexU not detected or not started!</p>
    //       <button onClick={handleInstall} className="action-button">
    //         Install NexU
    //       </button>
    //     </div>
    //   )}
    // </div>
    <div></div>
  );
});

export default NexuClient;
