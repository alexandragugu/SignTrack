import React, { useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import config from "../Config/config.tsx";

const CSCRedirect = () => {
  const [searchParams] = useSearchParams();

  useEffect(() => {
    console.log("CSCRedirect component mounted");
    const code = searchParams.get("code");
    const state = searchParams.get("state");

    console.log("URL:", window.location.href);

    if (!code || !state) {
      console.warn("Nu am parametrii");
      console.log("code:", code);
      console.log("state:", state);
    }

    console.log("A primit: " + state);

    if (state === "auth" || state === "refresh_step") {
      window.opener.postMessage(
        { type: "auth", code },
        `${config.FRONTEND_URL}`
      );
      window.close();
    }

    if (state === "sad") {
      window.opener?.postMessage(
        { type: "sad", code },
        `${config.FRONTEND_URL}`
      );
      window.close();
    }
  }, [searchParams]);

  return <div style={{ background: "none" }}>Procesare autentificare...</div>;
};

export default CSCRedirect;
