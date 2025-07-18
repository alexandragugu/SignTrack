import React, { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { animate, stagger } from "motion";
import { splitText } from "motion-plus";
import "./SuccessPage.css";
import config from "../../Config/config.tsx";

const SuccessPage = () => {
  const navigate = useNavigate();
  const titleRef = useRef<HTMLHeadingElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  const handleLogin = () => {
    const authUrl = `${config.KEYCLOAK_URL}/realms/${
      config.KEYCLOAK_REALM
    }/protocol/openid-connect/auth?response_type=code&client_id=${
      config.KEYCLOAK_CLIENT_ID
    }&redirect_uri=${encodeURIComponent(
      config.KEYCLOAK_REDIRECT_URI
    )}&scope=openid%20profile%20email`;

    window.location.href = authUrl;
  };

  useEffect(() => {
    if (!containerRef.current) return;

    document.fonts.ready.then(() => {
      containerRef.current!.style.visibility = "visible";

      animate(
        containerRef.current,
        { opacity: [0, 1], y: [-50, 0] },
        {
          duration: 0.8,
          easing: "ease-out",
        }
      );

      const title = containerRef.current.querySelector("h2");
      const message = containerRef.current.querySelector("p");
      const button = containerRef.current.querySelector("button");

      if (title) {
        const { words } = splitText(title, { by: "word" });
        animate(
          words,
          { opacity: [0, 1], y: [10, 0] },
          { type: "spring", duration: 2, delay: stagger(0.05), bounce: 0.2 }
        );
      }

      if (message) {
        animate(
          message,
          { opacity: [0, 1], y: [20, 0] },
          { delay: 0.8, duration: 1.2 }
        );
      }

      if (button) {
        animate(
          button,
          { opacity: [0, 1], scale: [0.8, 1] },
          { delay: 1.2, duration: 0.8, easing: "ease-out" }
        );
      }
    });
  }, []);

  return (
    <div
      className="success-container"
      style={{
        backgroundImage: `url(${process.env.PUBLIC_URL + "bg2.jpg"})`,
      }}
    >
      <div className="success-card" ref={containerRef}>
        <h2 className="success-title">Welcome to SignTrack!</h2>
        <p className="success-message">
          You can now start managing your documents.
        </p>
        <button className="success-button pulse" onClick={handleLogin}>
          <i className="pi pi-sign-in icon-spacing"></i>
          Start now
        </button>
      </div>
    </div>
  );
};

export default SuccessPage;
