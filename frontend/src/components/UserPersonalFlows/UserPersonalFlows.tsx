import React, { useState, useEffect } from "react";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { FilterMatchMode } from "primereact/api";
import { Button } from "primereact/button";
import "primereact/resources/themes/saga-purple/theme.css";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import apiClient from "../../Utils/ApiClient.tsx";
import { redirect, useNavigate } from "react-router-dom";
import "./UserPersonalFlows.css";
import useToast from "../../context/useToast.tsx";
import { Skeleton } from "primereact/skeleton";

const UserPersonalFlows = () => {
  const [data, setData] = useState([]);
  const [filters, setFilters] = useState({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS },
    filename: { value: null, matchMode: FilterMatchMode.CONTAINS },
    fileStatus: { value: null, matchMode: FilterMatchMode.EQUALS },
    ownerRole: { value: null, matchMode: FilterMatchMode.CONTAINS },
    ownerUsernamer: { value: null, matchMode: FilterMatchMode.CONTAINS },
    date: { value: null, matchMode: FilterMatchMode.CONTAINS },
  });
  const navigate = useNavigate();

  const toast = useToast();
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    try {
      const response = await apiClient.get("/files/assigned");
      console.log(response.data);
      setData(response.data);
      setLoading(false);
    } catch (error) {
      console.error("Error fetching data:", error);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const formatDate = (value) => {
    if (!value) return "";

    const date = new Date(value);
    return date.toLocaleString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formattedDateTemplate = (rowData) => {
    return <span>{formatDate(rowData.date)}</span>;
  };

  const myRoleTemplate = (rowData) => {
    const role = rowData.ownerRole ? rowData.ownerRole : "No role assigned";
    const isAssigned = !!rowData.ownerRole;

    return (
      <span
        style={{
          display: "inline-block",
          padding: "7px",
          borderRadius: "12px",
          border: isAssigned ? "2px solid rgba(177, 8, 228, 0)" : "none",
          background: isAssigned
            ? "rgba(219, 20, 241, 0.24)"
            : "rgba(255, 255, 255, 0.1)",
          color: isAssigned ? "azure" : "#bbb",
          fontStyle: isAssigned ? "normal" : "italic",
          fontWeight: 500,
          fontSize: isAssigned ? "1rem" : "0.85rem",
          textAlign: "center",
          minWidth: "100px",
        }}
      >
        {role}
      </span>
    );
  };

  const statusTemplate = (rowData) => {
    const status = rowData.fileStatus?.toLowerCase();
    let background = "";
    let color = "";
    let border = "";

    if (status === "finished") {
      background = "none";
      border = "3px solid rgba(6, 6, 134, 0.99)";
      color = "white";
    } else if (status === "pending") {
      background = "none";
      border = "3px solid rgba(228, 214, 13, 0.95)";
      color = "white";
    } else {
      background = "rgba(255, 255, 255, 0.1)";
      color = "#azure";
    }

    return (
      <span
        style={{
          display: "inline-block",
          padding: "10px",
          background: background,
          border: border,
          color: color,
          fontWeight: 500,
          fontSize: "1rem",
          textAlign: "center",
          minWidth: "100px",
          boxShadow: "0 0 10px rgba(6, 6, 134, 0.6)",
        }}
      >
        {rowData.fileStatus || "Unknown"}
      </span>
    );
  };

  const detailsTemplate = (rowData) => {
    const handleClick = () => {
      navigate("/file-info", {
        state: {
          fileUrl: rowData.fileUrl,
          ownerUsername: rowData.ownerUsername,
          createdAt: rowData.date,
          receiverActions: rowData.receiverActions,
        },
      });
    };

    return (
      <Button
        className="p-button-rounded p-button-text p-button-plain"
        onClick={handleClick}
        tooltip="Document Info"
        tooltipOptions={{ position: "top" }}
        style={{
          width: "3rem",
          height: "3rem",
          alignItems: "center",
          justifyContent: "center",
          padding: 0,
        }}
      >
        <i
          className="pi pi-info-circle"
          style={{
            fontSize: "1.2rem",
            color: "#b266ff",
          }}
        ></i>
      </Button>
    );
  };

  const actionTemplate = (rowData) => {
    const goToSignPage = () => {
      navigate("/sign", {
        state: {
          fileId: rowData.fileId,
          fileUrl: rowData.fileUrl,
          filename: rowData.filename,
        },
      });
    };

    const handleView = async () => {
      console.log("View action for:", rowData.fileId);

      try {
        const response = await apiClient.post("/files/answerRequest", {
          fileId: rowData.fileId,
        });

        toast.showSuccess(
          "Status updated. The owner has been notified accordingly."
        );
        fetchData();
      } catch (error) {
        console.error("Eroare:", error.message);
        toast.showError("An error occurred while updating view status.");
      }
    };

    const handleApprove = async () => {
      try {
        const response = await apiClient.post("/files/answerRequest", {
          fileId: rowData.fileId,
        });

        console.log(response.data);

        toast.showSuccess(
          "File approved successfully. The owner has been notified."
        );
        fetchData();
      } catch (error) {
        console.error("Eroare:", error.message);
        toast.showError("An error occurred while approving the file.");
      }
    };

    const handleDecline = async () => {
      try {
        const response = await apiClient.post("/files/declineRequest", {
          fileId: rowData.fileId,
          senderUsername: rowData.ownerUsernamer,
        });

        const data = response.data;
        console.log(data);

        toast.showSuccess("Request declined successfully.");
        fetchData();
      } catch (error) {
        console.error("Eroare:", error.message);
        toast.showError("Failed to decline the request.");
      }
    };

    switch (rowData.actionRequired) {
      case "TO_SIGN":
        return (
          <div style={{ display: "flex", gap: "2px", flexDirection: "row" }}>
            <Button
              icon="pi pi-pencil"
              tooltip="Sign"
              className="custom-decline-button"
              onClick={goToSignPage}
              style={{ backgroundColor: "none" }}
            />
            <Button
              icon="pi pi pi-ban"
              tooltip="Decline"
              className="custom-decline-button"
              onClick={handleDecline}
              style={{
                width: "2.5rem",
                height: "2.5rem",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                padding: 0,
              }}
            />
          </div>
        );

      case "TO_VIEW":
        return (
          <div style={{ display: "flex", gap: "2px", flexDirection: "row" }}>
            <Button
              icon="pi pi-eye"
              className="p-button-rounded p-button-text p-button-plain"
              onClick={handleView}
              tooltip="View"
              style={{ fontSize: "1.2rem", width: "3rem", height: "3rem" }}
            />
            <Button
              icon="pi pi pi-ban"
              tooltip="Decline"
              className="custom-decline-button"
              onClick={handleDecline}
              style={{
                width: "2.5rem",
                height: "2.5rem",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                padding: 0,
              }}
            />
          </div>
        );

      case "TO_APPROVE":
        return (
          <div style={{ display: "flex", gap: "2px", flexDirection: "row" }}>
            <Button
              icon="pi pi-check-circle"
              className="p-button-rounded p-button-warning p-button-text"
              onClick={handleApprove}
              tooltip="Approve"
              style={{
                width: "2.5rem",
                height: "2.5rem",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                padding: 0,
              }}
            />
            <Button
              icon="pi pi pi-ban"
              tooltip="Decline"
              className="custom-decline-button"
              onClick={handleDecline}
              style={{
                width: "2.5rem",
                height: "2.5rem",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                padding: 0,
              }}
            />
          </div>
        );

      default:
        return <div></div>;
    }
  };

  const filenameTemplate = (rowData) => {
    return (
      <span
        title={rowData.filename}
        style={{
          display: "inline-block",
          maxWidth: "140px",
          whiteSpace: "nowrap",
          overflow: "hidden",
          textOverflow: "ellipsis",
          verticalAlign: "middle",
        }}
      >
        {rowData.filename}
      </span>
    );
  };

  const downloadTemplate = (rowData) => {
    const handleDownload = () => {
      if (rowData.fileUrl) {
        window.open(rowData.fileUrl, "_blank");
      }
    };

    return (
      <Button
        icon="pi pi-download"
        className="p-button-rounded p-button-text"
        onClick={handleDownload}
        tooltip="Download file"
        style={{
          fontSize: "1.2rem",
          color: "#6b6bff",
        }}
      />
    );
  };

  return (
    <div className="custom-table-container">
      <DataTable
        value={loading ? Array(5).fill({}) : data}
        loading={loading}
        paginator
        rows={5}
        rowsPerPageOptions={[3, 10, 20]}
        filters={filters}
        filterDisplay="menu"
        globalFilterFields={["filename", "fileStatus", "ownerRole", "date"]}
        emptyMessage="No records found."
        className="custom-table"
        sortField="date"
        sortOrder={-1}
      >
        <Column
          field="filename"
          header="Filename"
          sortable
          filter
          body={(rowData) =>
            loading ? (
              <Skeleton width="90%" height="1.5rem" />
            ) : (
              filenameTemplate(rowData)
            )
          }
          filterPlaceholder="Search by filename"
          style={{ maxWidth: "8rem", alignItems: "left" }}
        />
        <Column
          field="fileStatus"
          header="Status"
          sortable
          filter
          body={(rowData) =>
            loading ? (
              <Skeleton width="70%" height="1.5rem" />
            ) : (
              statusTemplate(rowData)
            )
          }
          filterPlaceholder="Search by status"
          style={{ minWidth: "auto", alignItems: "center" }}
        />
        <Column
          field="ownerRole"
          header="My role"
          sortable
          filter
          body={(rowData) =>
            loading ? (
              <Skeleton width="60%" height="1.5rem" />
            ) : (
              myRoleTemplate(rowData)
            )
          }
          filterPlaceholder="Search by owner"
          style={{ minWidth: "auto", alignItems: "center" }}
        />

        <Column
          field="ownerUsernamer"
          header="Owner"
          sortable
          filter
          filterPlaceholder="Search by username"
          style={{ maxWidth: "7rem", alignItems: "center" }}
        />
        <Column
          field="date"
          header="Date"
          sortable
          filter
          body={(rowData) =>
            loading ? (
              <Skeleton width="70%" height="1.5rem" />
            ) : (
              formattedDateTemplate(rowData)
            )
          }
          filterPlaceholder="Search by date"
          style={{ minWidth: "auto" }}
        />
        <Column
          header=""
          body={(rowData) =>
            loading ? (
              <Skeleton shape="circle" size="2rem" />
            ) : (
              detailsTemplate(rowData)
            )
          }
          style={{ width: "auto", textAlign: "center", maxWidth: "3.5rem" }}
        />
        <Column
          header=""
          body={(rowData) =>
            loading ? (
              <Skeleton shape="circle" size="2rem" />
            ) : (
              downloadTemplate(rowData)
            )
          }
          style={{ width: "4rem", textAlign: "center" }}
        />
        <Column
          header=""
          body={(rowData) =>
            loading ? (
              <Skeleton shape="circle" size="2rem" />
            ) : (
              actionTemplate(rowData)
            )
          }
          style={{
            minWidth: "auto",
            textAlign: "center",
            maxWidth: "6.5rem",
          }}
        />
      </DataTable>
    </div>
  );
};

export default UserPersonalFlows;
