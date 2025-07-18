import React, { useEffect, useState } from "react";
import axios from "axios";

const CodeReceiver = () => {
  const [code, setCode] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [access_token, setToken] = useState<string | null>(null);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const authCode = params.get("code");
    const token = params.get("access_token");

    if (authCode) {
      setCode(authCode);
      console.log("Authorization Code:", authCode);
      initiateConnection(authCode);
    }

    if (token) {
      setToken(token);
      console.log(token);
    }
  }, []);

  const initiateConnection = async (authCode: string) => {
    setLoading(true);
    try {
      const response = await axios.post(
        "https://service.csctest.online/csc/v0/oauth2/token",
        {
          client_id: "bBdNs9Fa7kMx0qnFsPk66sklrDw",
          client_secret: "QOui8WD8wX07hGd73KjO6pF3xwKj09PlKzx2e6Z8iILg2fmA",
          grant_type: "authorization_code",
          code: authCode,
          redirect_uri: "http://localhost:8080/login.html", //
        },
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      console.log("Response from server:", response.data);
    } catch (err) {
      console.error("Error connecting to server:", err);
      setError("Failed to connect to server.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {loading && <h1>Loading...</h1>} {}
      {error && <h1 style={{ color: "red" }}>{error}</h1>} {}
      {code ? (
        <h1>Received Authorization Code: {code}</h1>
      ) : (
        <h1>No Authorization Code Received</h1>
      )}
    </div>
  );
};

export default CodeReceiver;
