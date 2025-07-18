import React, { useEffect, useState, useRef } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Dropdown } from "primereact/dropdown";
import { Button } from "primereact/button";
import { Card } from "primereact/card";
import { motion } from "framer-motion";
import { FaTrash, FaPaperPlane } from "react-icons/fa";
import { Dialog } from "primereact/dialog";
import "./RecipientForm.css";
import apiClient from "../../Utils/ApiClient.tsx";
import useToast from "../../context/useToast.tsx";

interface User {
  id: string;
  username: string;
  email: string;
}

interface Recipient {
  id: number;
  userId: string | null;
  email: string | null;
  action: string;
  actionLabel: string;
  username: string | null;
}

const signerRoles = [
  { label: "Signer", value: "to-sign" },
  { label: "Viewer", value: "to-view" },
  { label: "Approver", value: "to-approve" },
];

interface SignatureData {
  files: File[];
}

interface RecipientFormProps {
  signatureData: SignatureData;
}

interface proceedSigData {
  fileId: string;
  filename: string;
}

interface FileDetailsModel {
  fileId: string;
  fileUrl: string;
  filename: string;
}

const RecipientForm: React.FC<RecipientFormProps> = ({ signatureData }) => {
  const location = useLocation();
  const username = localStorage.getItem("username");
  console.log("Username from cookies:", username);
  const [filename, setFilename] = useState("");
  const [fileUrl, setFileUrl] = useState("");
  const [fileId, setFileId] = useState("");
  const [responseData, setResponseData] = useState<FileDetailsModel[]>([]);
  const [localSignatureData, setLocalSignatureData] =
    useState<SignatureData | null>(null);
  const [dialogVisibleSignature, setDialogVisible] = useState(false);
  const [dialogMessage, setDialogMessage] = useState("");
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const navigate = useNavigate();
  const [newRecipient, setNewRecipient] = useState<Recipient>({
    id: 0,
    userId: null,
    email: null,
    action: "",
    actionLabel: "",
    username: null,
  });

  const toast = useToast();

  useEffect(() => {
    if (signatureData) {
      setLocalSignatureData(signatureData);
      console.log("Primite din FileUpload:", signatureData);
    }
  }, [signatureData]);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response = await apiClient.get("/users/allUsers");
        const data: User[] = await response.data;
        setUsers(data);
      } catch (err) {
        console.error("Error fetching users:", err);
      }
    };

    fetchUsers();
  }, []);

  const handleAddRecipient = () => {
    if (!newRecipient.userId || !newRecipient.action) {
      toast.showError("Please select a user and a role.");
      return;
    }

    const selectedUser = users.find((u) => u.id === newRecipient.userId);

    if (!selectedUser) {
      toast.showError("User does not exist.");
      return;
    }

    const alreadyExists = recipients.some(
      (recipient) => recipient.userId === newRecipient.userId
    );

    if (alreadyExists) {
      toast.showError("This user is already in the recipient list.");
      return;
    }

    const newRecipientWithUsername: Recipient = {
      ...newRecipient,
      id: recipients.length + 1,
      username: selectedUser.username,
      email: selectedUser.email,
    };

    setRecipients([...recipients, newRecipientWithUsername]);

    setNewRecipient({
      id: 0,
      userId: null,
      username: null,
      email: null,
      action: "",
      actionLabel: "",
    });
  };

  const handleRemoveRecipient = (id: number) => {
    setRecipients(recipients.filter((r) => r.id !== id));
  };

  const getCurrentUserAction = (recipients: Recipient[]): string | null => {
    const currentUsername = localStorage.getItem("username");
    if (!currentUsername) return null;

    const matched = recipients.find((r) => r.username === currentUsername);
    console.log("Matched recipient:", matched);
    debugger;
    return matched?.action || null;
  };

  const handleProceed = async () => {
    setDialogVisible(false);

    const currentUsername = localStorage.getItem("username");
    console.log("Username:", currentUsername);
    if (!currentUsername) {
      console.warn("No username found in localStorage.");
      return;
    }

    const matchedRecipient = recipients.find(
      (recipient) =>
        users.find((user) => user.id === recipient.userId)?.username ===
        currentUsername
    );

    if (!matchedRecipient) {
      console.warn("User not found in recipient list.");
      return;
    }

    switch (matchedRecipient.action) {
      case "to-sign":
        if (!Array.isArray(localSignatureData?.files)) {
          console.warn("No localSignatureData.files available.");
          return;
        }
        if (!Array.isArray(responseData)) {
          console.error("No responseData found for bulk sign.");
          return;
        }

        navigate("/bulk", {
          state: {
            files: responseData.map((file: any) => ({
              fileId: file.fileId,
              fileUrl: file.fileUrl,
              filename: file.filename,
            })),
          },
        });
        break;

      case "to-view":
        try {
          if (!Array.isArray(responseData)) {
            console.error("responseData missing or invalid.");
            return;
          }

          const payload = responseData.map((file) => ({
            fileId: file.fileId,
          }));

          await apiClient.post("/files/answerRequest/bulk", payload);

          toast.showSuccess(
            "All files marked as viewed. The owner has been notified."
          );
          navigate("/");
        } catch (error) {
          console.error("Error during TO_VIEW:", error.message);
          toast.showError("Failed to mark one or more files as viewed.");
        }
        break;

      case "to-approve":
        try {
          if (!Array.isArray(responseData)) {
            console.error("responseData missing or invalid.");
            return;
          }

          const payload = responseData.map((file) => ({
            fileId: file.fileId,
          }));

          await apiClient.post("/files/answerRequest/bulk", payload);

          toast.showSuccess("All files approved. The owner has been notified.");
          navigate("/");
        } catch (error) {
          console.error("Error during TO_APPROVE:", error.message);
          toast.showError("Failed to approve one or more files.");
        }
        break;

      default:
        console.log("Unknown action:", matchedRecipient.action);
    }
  };

  const handleSubmit = async () => {
    if (recipients.length === 0) {
      toast.showError("Please add at least one recipient.");
      return;
    }

    if (
      !signatureData ||
      !signatureData.files ||
      signatureData.files.length === 0
    ) {
      toast.showError("Missing files.");
      return;
    }

    const formData = new FormData();

    signatureData.files.forEach((file, index) => {
      formData.append("files", file);
    });

    formData.append(
      "receivers",
      JSON.stringify(
        recipients.map((r) => ({
          userId: r.userId,
          action: r.action,
          email: r.email,
        }))
      )
    );

    console.log(formData);

    try {
      const response = await apiClient.post("/email/sendMail-bulk", formData);

      toast.showSuccess("Document sent successfully!");
      console.log("Response:", response.data);
      setResponseData(response.data);
      const userAction = getCurrentUserAction(recipients);

      if (userAction) {
        setDialogVisible(true);
        console.log("User action", userAction);

        setFilename(response.data.filename);
        setFileUrl(response.data.fileUrl);
        setFileId(response.data.fileId);

        if (userAction == "to-sign") {
          console.log("trebuie sa semneze");
          setDialogMessage(
            "You have been identified as a recipient \n Would you like to sign now?"
          );
        } else if (userAction == "to-approve") {
          setDialogMessage(
            "You have been identified as a recipient \n Would you like to approve now?"
          );
        } else if (userAction == "to-view") {
          setDialogMessage(
            "You have been identified as a recipient \n Would you like to mark it now?"
          );
        }
      } else {
        console.log("User not in recipients.");
        navigate("/");
      }
    } catch (error) {
      console.error("Error sending email:", error);
      alert("Failed to send email. Please try again.");
    }
  };

  return (
    <div className="recipient-form-container">
      <Card className="recipient-card-container">
        <div className="input-container">
          <div className="recipient-field">
            <label>Username </label>
            <Dropdown
              value={newRecipient.userId}
              options={users.map((user) => ({
                label: user.username,
                value: user.id,
              }))}
              onChange={(e) =>
                setNewRecipient({ ...newRecipient, userId: e.value })
              }
              placeholder="Select user"
              filter
              className="p-dropdown-users"
            />
          </div>

          <div className="recipient-field">
            <label> Role</label>
            <Dropdown
              value={newRecipient.action}
              options={signerRoles}
              onChange={(e) =>
                setNewRecipient({
                  ...newRecipient,
                  action: e.value,
                  actionLabel:
                    signerRoles.find((role) => role.value === e.value)?.label ||
                    "",
                })
              }
              placeholder="Select role"
              className="p-dropdown-roles"
            />
          </div>

          <Button
            icon="pi pi-plus"
            className="add-btn"
            onClick={handleAddRecipient}
            tooltip="Add recipient"
          />
        </div>

        <div className="recipient-list">
          {recipients.map((recipient) => (
            <motion.div
              key={recipient.id}
              className="recipient-card"
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              transition={{ duration: 0.3 }}
            >
              <div className="recipient-info">
                <span>
                  {users.find((user) => user.id === recipient.userId)?.username}
                </span>
                <span className="recipient-role">{recipient.actionLabel}</span>
              </div>
              <Button
                className="delete-btn"
                icon={<FaTrash />}
                onClick={() => handleRemoveRecipient(recipient.id)}
                tooltip="Remove"
              />
            </motion.div>
          ))}
        </div>

        <div className="submit-container">
          <Button
            icon={<FaPaperPlane />}
            className="submit-icon-btn"
            onClick={handleSubmit}
            tooltip="Send Request"
          />
        </div>
      </Card>
      <Dialog
        visible={dialogVisibleSignature}
        onHide={() => {}}
        closable={false}
        modal
        className="custom-dialog"
        style={{ width: "400px" }}
        footer={
          <div className="p-dialog-footer">
            <Button
              label="Not now"
              icon="pi pi-times"
              className="custom-dialog-btn cancel"
              onClick={() => {
                setDialogVisible(false);
                navigate("/");
              }}
            />
            <Button
              label="Proceed"
              icon="pi pi-check"
              className="custom-dialog-btn proceed"
              onClick={handleProceed}
            />
          </div>
        }
      >
        <p>{dialogMessage}</p>
      </Dialog>
    </div>
  );
};

export default RecipientForm;
