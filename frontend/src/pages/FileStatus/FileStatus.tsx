import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import apiClient from "./../../Utils/ApiClient.tsx";
import CustomSpinner from "../../components/CustomSpinner/CustomSpinner.tsx";

const FileStatus = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [message, setMessage] = useState<string>("");
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const token = searchParams.get("token");

    if (token) {
      validateToken(token);
    } else {
      setMessage("Invalid or non-existent token.");
      setIsLoading(false);
    }
  }, [location.search]);

  const validateToken = async (token: string) => {
    console.log("token", token);
    try {
      const response = await apiClient.get(`/validateToken?token=${token}`);

      const data = await response.data;

      console.log("data", data);

      navigate("/file-info", {
        state: {
          fileUrl: data.fileUrl,
          filename: data.filename,
          ownerUsername: data.senderUsername,
          receiverActions: data.receiverActions,
        },
      });

      if (data.success) {
        // setMessage("Hello " + username + "!");
      } else {
        setMessage(data.message);
      }
    } catch (error) {
      console.error("Eroare la validarea tokenului:", error);
      setMessage(error.response.data.message);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading)
    return (
      <div className="sign-req-page">
        {" "}
        <CustomSpinner />
      </div>
    );

  return <div></div>;
};

export default FileStatus;
