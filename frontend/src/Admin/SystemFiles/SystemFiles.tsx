import React, { useEffect, useState } from "react";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { FilterMatchMode } from "primereact/api";
import { Button } from "primereact/button";
import apiClient from "../../Utils/ApiClient.tsx";
import { useNavigate } from "react-router-dom";
import useToast from "../../context/useToast.tsx";
import { useConfirm } from "../../components/GlobalConfirmDialog/GlobalConfirmDialog.tsx";
import "./SystemFiles.css";
import FlowsStatistics from "../FlowsStatistics/FlowsStatistics.tsx";
import FileInfoDialog from "../FileInfoDialog/FileInfoDialog.tsx";

const SystemFiles = ({ filter = "all" }) => {
  const [allData, setAllData] = useState([]);
  const [filteredData, setFilteredData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [fileDialogVisible, setFileDialogVisible] = useState(false);
  const [selectedFileData, setSelectedFileData] = useState(null);
  const [filters, setFilters] = useState({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS },
    filename: { value: null, matchMode: FilterMatchMode.CONTAINS },
    receiverUsername: { value: null, matchMode: FilterMatchMode.CONTAINS },
    ownerUsernamer: { value: null, matchMode: FilterMatchMode.CONTAINS },
  });

  const toast = useToast();
  const confirm = useConfirm();

  const fetchFiles = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get("/admin/systemFiles");
      console.log("ALL DATA:", res.data);
      setAllData(res.data);
      setLoading(false);
    } catch (err) {
      console.error("Error fetching files:", err);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFiles();
  }, []);

  useEffect(() => {
    if (!allData.length) return;

    let filtered = allData;

    if (filter === "finished") {
      filtered = allData.filter((file) => file.fileStatus === "Finished");
    } else if (filter === "pending") {
      filtered = allData.filter((file) => file.fileStatus === "Pending");
    }
    setFilteredData(filtered);
  }, [allData, filter]);

  const fileTemplate = (rowData) => {
    return (
      <span
        title={rowData.filename}
        style={{
          display: "inline-block",
          maxWidth: "200px",
          whiteSpace: "nowrap",
          overflow: "hidden",
          textOverflow: "ellipsis",
        }}
      >
        {rowData.filename}
      </span>
    );
  };

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

  const downloadTemplate = (rowData) => {
    const handleDownload = () => {
      if (rowData.fileUrl) window.open(rowData.fileUrl, "_blank");
    };
    return (
      <Button
        icon="pi pi-download"
        className="p-button-rounded p-button-text"
        onClick={handleDownload}
        tooltip="Download file"
        style={{ fontSize: "1.2rem", color: "white" }}
      />
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

  const handleDelete = async (rowData) => {
    try {
      const response = await apiClient.delete(`/admin/delete-file`, {
        data: {
          fileId: rowData.fileId,
          filename: rowData.filename,
        },
      });

      if (response.status === 200) {
        toast.showSuccess("File removed successfully!");
        setAllData((prevData) =>
          prevData.filter((item) => item.fileId !== rowData.fileId)
        );
      }
    } catch (error) {
      console.error(
        "Error deleting file:",
        error.response?.data || error.message
      );
      toast.showError("An error occurred while deleting the file.");
    }
  };

  const deleteTemplate = (rowData) => {
    return (
      <Button
        icon="pi pi-trash"
        className="p-button-rounded p-button-text"
        severity="danger"
        aria-label="Delete"
        onClick={() =>
          confirm({
            message: `Are you sure you want to delete ${rowData.filename}? This action is irreversible.`,
            onAccept: () => handleDelete(rowData),
          })
        }
        tooltip="Delete file"
        style={{
          fontSize: "1.2rem",
          color: "#ff5f5f",
        }}
      />
    );
  };

  return (
    <div className="custom-system-files-table">
      <FlowsStatistics />
      <DataTable
        value={filteredData}
        loading={loading}
        paginator
        rows={10}
        rowsPerPageOptions={[5, 10, 20]}
        filters={filters}
        filterDisplay="menu"
        globalFilterFields={["filename", "receiverStatus", "receiverUsername"]}
        emptyMessage="No files found."
        className="custom-table-system-files"
        sortField="date"
        sortOrder={-1}
      >
        <Column
          field="filename"
          header="Filename"
          sortable
          filter
          body={(rowData) => fileTemplate(rowData)}
          filterPlaceholder="Search filename"
        />

        <Column
          field="ownerUsernamer"
          header="Owner"
          sortable
          filter
          filterPlaceholder="Search user"
          body={(rowData) => rowData.ownerUsernamer || "-"}
        />

        <Column
          field="receiverStatus"
          header="Status"
          body={(rowData) => statusTemplate(rowData)}
          filterPlaceholder="Search status"
        />
        <Column
          field="date"
          header="Date"
          sortable
          body={(rowData) => formattedDateTemplate(rowData)}
          style={{ minWidth: "auto" }}
        />

        <Column
          header=""
          body={(rowData) => downloadTemplate(rowData)}
          style={{ width: "4rem", textAlign: "center" }}
        />

        <Column
          body={(rowData) => deleteTemplate(rowData)}
          header=""
          style={{ width: "4rem", textAlign: "center" }}
        />

        <Column
          header=""
          body={(rowData) => detailsTemplate(rowData)}
          style={{ width: "auto", textAlign: "center", maxWidth: "3.5rem" }}
        />
      </DataTable>
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

export default SystemFiles;
