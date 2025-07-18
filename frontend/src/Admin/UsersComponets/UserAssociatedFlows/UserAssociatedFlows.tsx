import React, { useState, useEffect, useRef } from "react";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { FilterMatchMode } from "primereact/api";
import { Button } from "primereact/button";
import "primereact/resources/themes/saga-purple/theme.css";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import "./UserAssociatedFlows.css";
import { Skeleton } from "primereact/skeleton";
import apiClient from "../../../Utils/ApiClient.tsx";
import { useNavigate } from "react-router-dom";
import useToast from "../../../context/useToast.tsx";
import { MultiSelect } from "primereact/multiselect";
import { useConfirm } from "../../../components/GlobalConfirmDialog/GlobalConfirmDialog.tsx";
import CustomToast from "../../../components/CustomToast/CustomToast.tsx";
import FileInfoDialog from "../../FileInfoDialog/FileInfoDialog.tsx";

const AssociatedFlows = ({ userId }) => {
  const [data, setData] = useState([]);
  const [selectedRows, setSelectedRows] = useState([]);
  const [filters, setFilters] = useState({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS },
    filename: { value: null, matchMode: FilterMatchMode.CONTAINS },
    fileStatus: { value: null, matchMode: FilterMatchMode.EQUALS },
    ownerRole: { value: null, matchMode: FilterMatchMode.IN },
    date: { value: null, matchMode: FilterMatchMode.CONTAINS },
  });
  const navigate = useNavigate();
  const toast = useToast();
  const confirm = useConfirm();
  const toastRef = useRef(null);

  const [loading, setLoading] = useState(true);

  const [selectedRoles, setSelectedRoles] = useState([]);
  const [selectedStatuses, setSelectedStatuses] = useState([]);
  const [fileDialogVisible, setFileDialogVisible] = useState(false);
  const [selectedFileData, setSelectedFileData] = useState(null);

  const roleOptions = [
    { label: "Signer", value: "Signer" },
    { label: "Viewer", value: "Viewer" },
    { label: "Approver", value: "Approver" },
  ];

  const statusOptions = [
    { label: "Pending", value: "Pending" },
    { label: "Finished", value: "Finished" },
  ];

  const fetchData = async () => {
    try {
      const response = await apiClient.get(
        `/admin/users/personal-files/${userId}`
      );
      setData(response.data);
      console.log(response.data);
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

  const roleFilterTemplate = (options) => (
    <MultiSelect
      value={selectedRoles}
      options={roleOptions}
      onChange={(e) => {
        setSelectedRoles(e.value);
        options.filterApplyCallback(e.value);
      }}
      placeholder="Select roles"
      display="chip"
      className="p-column-filter"
      style={{ minWidth: "12rem" }}
      showSelectAll={true}
    />
  );

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

  const openFileDialog = (rowData) => {
    setSelectedFileData({
      fileUrl: rowData.fileUrl,
      filename: rowData.filename,
      receiverActions: rowData.receiverActions,
    });
    setFileDialogVisible(true);
  };

  const detailsTemplate = (rowData) => {
    return (
      <Button
        className="p-button-rounded p-button-text p-button-plain"
        onClick={() => openFileDialog(rowData)}
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

  const handleDelete = (rowData) => {
    confirm({
      message: `Are you sure you want to delete "${rowData.filename}"?`,
      onAccept: async () => {
        console.log("Delete confirmed for:", rowData.fileId);
        try {
          const response = await apiClient.delete(`/admin/delete-file`, {
            data: {
              fileId: rowData.fileId,
              filename: rowData.filename,
            },
          });

          if (response.status === 200) {
            toast.showSuccess("File removed successfully!");

            setData((prevData) =>
              prevData.filter((item) => item.fileId !== rowData.fileId)
            );
          }
        } catch (error) {
          console.error(
            "Error deleting file:",
            error.response?.data || error.message
          );
          toast.showError("Failed to delete file. Please try again.");
        }
      },
    });
  };

  const deleteTemplate = (rowData) => {
    return (
      <Button
        icon="pi pi-trash"
        className="p-button-rounded p-button-text"
        severity="danger"
        aria-label="Delete"
        onClick={() => handleDelete(rowData)}
        tooltip="Delete file"
        style={{
          fontSize: "1.2rem",
          color: "#ff5f5f",
        }}
      />
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

  const statusFilterTemplate = (options) => (
    <MultiSelect
      value={selectedStatuses || []}
      options={statusOptions}
      onChange={(e) => {
        const value = e.value.length ? e.value : null;
        setSelectedStatuses(value);
        options.filterApplyCallback(value);
      }}
      placeholder="Select status"
      display="chip"
      className="p-column-filter"
      style={{ minWidth: "12rem" }}
      showSelectAll={true}
    />
  );

  const handleBulkDelete = (rows) => {
    const fileIds = rows.map((row) => row.id);

    confirm({
      message: `Are you sure you want to delete ${fileIds.length} file(s)?`,
      header: "Confirm Bulk Delete",
      icon: "pi pi-exclamation-triangle",
      acceptClassName: "p-button-danger",
      onAccept: async () => {
        try {
          const response = await apiClient.post(
            "/admin/files/delete-multiple-files",
            selectedRows.map((f) => f.fileId)
          );

          toastRef.current?.showSuccess(
            response.data.message || "Files deleted successfully."
          );

          setSelectedRows([]);
          setTimeout(() => window.location.reload(), 1000);
        } catch (err) {
          const errorMsg =
            err?.response?.data?.errorMessage || "Failed to delete files.";
          toastRef.current?.showError(errorMsg);
        }
      },
    });
  };

  return (
    <div className="associated-flows-table-container">
      <div className="custom-table-toolbar">
        {selectedRows.length > 0 && (
          <button
            className="custom-table-bulk-delete-button p-button p-button-rounded p-button-danger"
            style={{ margin: "1rem 0.5rem 0 1rem" }}
            onClick={() => handleBulkDelete(selectedRows)}
          >
            <i className="pi pi-trash" />
          </button>
        )}
      </div>

      <DataTable
        value={data}
        loading={loading}
        paginator
        rows={5}
        rowsPerPageOptions={[3, 10, 20]}
        filters={filters}
        filterDisplay="menu"
        globalFilterFields={["filename", "fileStatus", "ownerRole", "date"]}
        emptyMessage="No records found."
        className="admin-associated-flows-table"
        sortField="date"
        sortOrder={-1}
        rowClassName={() => "admin-associated-flows-card-row"}
        selection={selectedRows}
        onSelectionChange={(e) => setSelectedRows(e.value)}
        dataKey="fileId"
      >
        <Column
          selectionMode="multiple"
          headerStyle={{ width: "3em" }}
          header={() => null}
        />
        <Column
          field="filename"
          header="Filename"
          sortable
          filter
          body={(rowData) =>
            loading ? (
              <Skeleton width="100%" height="1.5rem" className="mr-2" />
            ) : (
              filenameTemplate(rowData)
            )
          }
        />
        <Column
          field="fileStatus"
          header="Status"
          sortable
          filter
          filterMatchMode="in"
          showApplyButton={false}
          showClearButton={false}
          showFilterMatchModes={false}
          filterElement={statusFilterTemplate}
          body={(rowData) =>
            loading ? (
              <Skeleton width="70%" height="1.5rem" />
            ) : (
              statusTemplate(rowData)
            )
          }
          filterPlaceholder="Search by status"
          style={{ maxWidth: "7rem" }}
        />
        <Column
          field="ownerRole"
          header="Role"
          sortable
          filter
          filterMatchMode="in"
          showApplyButton={false}
          showClearButton={false}
          showFilterMatchModes={false}
          filterElement={roleFilterTemplate}
          body={(rowData) =>
            loading ? (
              <Skeleton width="60%" height="1.5rem" />
            ) : (
              myRoleTemplate(rowData)
            )
          }
          style={{ maxWidth: "10rem" }}
        />

        <Column
          field="date"
          header="Date"
          sortable
          filter
          body={(rowData) =>
            loading ? (
              <Skeleton width="60%" height="1.5rem" />
            ) : (
              formattedDateTemplate(rowData)
            )
          }
          filterPlaceholder="Search by date"
          style={{ maxWidth: "7rem" }}
        />
        <Column
          body={(rowData) =>
            loading ? (
              <Skeleton shape="circle" size="2rem" />
            ) : (
              deleteTemplate(rowData)
            )
          }
          header=""
          style={{ width: "4rem", textAlign: "center" }}
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
      </DataTable>

      <CustomToast ref={toastRef} />

      {selectedFileData && (
        <FileInfoDialog
          visible={fileDialogVisible}
          onHide={() => setFileDialogVisible(false)}
          fileUrl={selectedFileData.fileUrl}
          filename={selectedFileData.filename}
          receiverActions={selectedFileData.receiverActions}
        />
      )}
    </div>
  );
};

export default AssociatedFlows;
