import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Card } from "primereact/card";
import { Tag } from "primereact/tag";
import { Avatar } from "primereact/avatar";
import { FaUserCircle } from "react-icons/fa";
import { Paginator } from "primereact/paginator";
import "./FileStatusCard.css";
import { Button } from "primereact/button";

interface ReceiverAction {
  username: string;
  action: string;
  currentDate: string;
  email: string | null;
  userId: string;
}

interface FileStatusCardListProps {
  receiverActions: ReceiverAction[];
  itemsPerPage?: number;
}

const FileStatusCardList: React.FC<FileStatusCardListProps> = ({
  receiverActions,
  itemsPerPage = 2,
}) => {
  const [first, setFirst] = useState(0);
  const navigate = useNavigate();

  console.log("Receiver Actions:", receiverActions);

  const onPageChange = (event: any) => {
    setFirst(event.first);
  };

  const getStatusLabel = (action: string) => {
    return action.startsWith("TO_") ? "Pending" : "Finished";
  };

  const getStatusSeverity = (status: string) => {
    return status === "Pending" ? "warning" : "success";
  };

  const getRoleFromAction = (action: string): string => {
    let role = "Unknown";

    if (action.includes("APPROVE")) role = "Approver";
    else if (action.includes("SIGN")) role = "Signer";
    else if (action.includes("VIEW")) role = "Viewer";

    if (action.startsWith("DECLINED")) {
      role += " (request declined)";
    }

    return role;
  };

  return (
    <div className="file-status-list-container">
      <div className="top-bar-close"> </div>
      <div className="file-status-list">
        {receiverActions.length > 0 ? (
          receiverActions
            .slice(first, first + itemsPerPage)
            .map((status, index) => {
              const formattedDate = new Date(status.currentDate).toLocaleString(
                "ro-RO"
              );
              const statusLabel = getStatusLabel(status.action);
              const statusSeverity = getStatusSeverity(statusLabel);
              const role = getRoleFromAction(status.action);

              return (
                <Card key={index} className="file-status-card">
                  <div className="card-header">
                    <Avatar
                      icon={<FaUserCircle size={20} />}
                      className="user-icon"
                      shape="circle"
                    />
                    <span className="status-title">{status.username}</span>
                  </div>
                  <div className="card-body">
                    <div className="status-item">
                      <span className="status-label">Current Status:</span>
                      <Tag value={statusLabel} severity={statusSeverity} />
                    </div>
                    <div className="status-item">
                      <span className="status-label">Role:</span>
                      <Tag value={role} />
                    </div>
                    <div className="status-item">
                      <span className="status-label">Date:</span>
                      <Tag value={formattedDate} />
                    </div>
                  </div>
                </Card>
              );
            })
        ) : (
          <p className="no-status-message">No requests sent</p>
        )}
      </div>

      {receiverActions.length > itemsPerPage ? (
        <Paginator
          first={first}
          rows={itemsPerPage}
          totalRecords={receiverActions.length}
          onPageChange={onPageChange}
          className="file-status-paginator"
          template={{ layout: "PrevPageLink CurrentPageReport NextPageLink" }}
        />
      ) : null}
    </div>
  );
};

export default FileStatusCardList;
