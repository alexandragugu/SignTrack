import React, { useEffect, useState } from "react";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Skeleton } from "primereact/skeleton";
import apiClient from "../../Utils/ApiClient.tsx";
import "./SignaturesActivityTable.css";

const SignaturesActivityTable = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchActivity = async () => {
      try {
        const res = await apiClient.get("/admin/signature-activity");
        console.log("Signature activity data:", res.data);
        setData(res.data);
        setLoading(false);
      } catch (error) {
        console.error("Error fetching signature activity", error);
        setLoading(false);
      }
    };

    fetchActivity();
  }, []);

  const formatDate = (value) => {
    if (!value || !Array.isArray(value)) return "";

    const [year, month, day, hour, minute, second] = value;

    const date = new Date(year, month - 1, day, hour, minute, second);

    if (isNaN(date)) {
      console.error("Invalid parsed date:", value);
      return "-";
    }

    return date.toLocaleString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formattedDateTemplate = (rowData) => {
    return rowData.signatureDate ? (
      <span>{formatDate(rowData.signatureDate)}</span>
    ) : (
      "-"
    );
  };

  const visibilityTemplate = (rowData) => {
    return (
      <span style={{ color: rowData.visibility ? "#4ade80" : "#f87171" }}>
        {rowData.visibility ? "Visible" : "Invisible"}
      </span>
    );
  };

  const formatPosition = (position) => {
    if (!position) return "N/A";

    return position
      .toLowerCase()
      .split("_")
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(" ");
  };

  const visibilityIcon = (visible) => {
    if (visible === true) {
      return (
        <i
          className="pi pi-eye"
          style={{ color: "#4ade80", fontSize: "1.2rem" }}
          title="Visible"
        ></i>
      );
    } else {
      return (
        <i
          className="pi pi-eye-slash"
          style={{ color: "#f87171", fontSize: "1.2rem" }}
          title="Invisible"
        ></i>
      );
    }
  };

  return (
    <div className="signature-activity-container">
      <div className="signature-activity-card">
        <DataTable
          value={loading ? Array(8).fill({}) : data}
          loading={loading}
          paginator
          rows={10}
          rowsPerPageOptions={[5, 10, 20]}
          className="signature-activity-table"
          emptyMessage="No signature activity found."
        >
          <Column
            field="signer"
            header="Signer"
            body={(rowData) =>
              loading ? (
                <Skeleton width="70%" height="1.5rem" />
              ) : (
                rowData.signer || "-"
              )
            }
            sortable
            style={{ textAlign: "center" }}
          />
          <Column
            field="signatureDate"
            header="Signature Date"
            body={(rowData) =>
              loading ? (
                <Skeleton width="70%" height="1.5rem" />
              ) : (
                formattedDateTemplate(rowData)
              )
            }
            sortable
            style={{ textAlign: "center" }}
          />
          <Column
            field="signatureType"
            header="Signature Type"
            body={(rowData) =>
              loading ? <Skeleton width="60%" /> : rowData.signatureType || "-"
            }
            style={{ textAlign: "center" }}
          />
          <Column
            field="visibility"
            header="Visibility"
            body={(rowData) =>
              loading ? (
                <Skeleton width="60%" />
              ) : (
                visibilityIcon(rowData.visibility)
              )
            }
            style={{ textAlign: "center" }}
          />
          <Column
            field="position"
            header="Position"
            body={(rowData) =>
              loading ? (
                <Skeleton width="60%" />
              ) : (
                formatPosition(rowData.position)
              )
            }
            style={{ textAlign: "center" }}
          />
          <Column
            field="profile"
            header="Profile"
            body={(rowData) =>
              loading ? <Skeleton width="60%" /> : rowData.profile || "-"
            }
            style={{ textAlign: "center" }}
          />
        </DataTable>
      </div>
    </div>
  );
};

export default SignaturesActivityTable;
