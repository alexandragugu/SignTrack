import React, { useEffect, useState } from "react";
import apiClient from "../../Utils/ApiClient.tsx";
import { Skeleton } from "primereact/skeleton";
import "./SignatureActivityPreview.css";

const SignatureActivityPreview = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchSignatureStats = async () => {
      try {
        const response = await apiClient.get("/admin/signature-preview");
        setStats(response.data);
        console.log("Signature Stats:", response.data);
        setLoading(false);
      } catch (error) {
        console.error("Error fetching signature activity:", error);
        setLoading(false);
      }
    };

    fetchSignatureStats();
  }, []);

  return (
    <div className="signature-activity-preview">
      <div className="preview-grid">
        <div className="preview-card">
          <h4 className="preview-title">Signed Files</h4>
          {loading ? (
            <Skeleton width="60%" height="2rem" />
          ) : (
            <p className="preview-value">{stats?.totalSignedFiles || 0}</p>
          )}
        </div>
        <div className="preview-card">
          <h4 className="preview-title">Most Active Signer</h4>
          {loading ? (
            <>
              <Skeleton width="80%" height="1.5rem" />
              <Skeleton width="40%" height="1rem" />
            </>
          ) : stats?.mostActiveSigner ? (
            <>
              <p className="preview-signer">
                {stats.mostActiveSigner.username}
              </p>
              <p className="preview-subvalue">
                {stats.mostActiveSigner.signedCount} files
              </p>
            </>
          ) : (
            <p>No signer data</p>
          )}
        </div>
        <div className="preview-card">
          <h4 className="preview-title">This Month Activity</h4>

          <p className="preview-value">{stats?.thisMonthSigned || 0}</p>
        </div>
      </div>
    </div>
  );
};

export default SignatureActivityPreview;
