import React, { useEffect, useState } from "react";
import "./DocumentList.css";
import apiClient from "../../Utils/ApiClient.tsx";

interface DocumentModel {
  fileId: number;
  filename: string;
  status: string;
  lastModified: Date;
  createdDate: Date;
  fileUrl: string;
}
const DocumentList = ({ onSelect }) => {
  const [documents, setDocuments] = useState<DocumentModel[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDocuments = async () => {
      try {
        const response = await apiClient.get("/files/lastFiles");
        const data = response.data;

        console.log(data);
        setDocuments(data);
      } catch (err) {
        setError("Error fetching documents");
      } finally {
        setLoading(false);
      }
    };

    fetchDocuments();
  }, []);

  return (
    <div className="document-list">
      <h2 className="title">Recent documents</h2>
      <ul>
        {documents.length === 0 ? (
          <li className="document-item">No documents uploaded</li>
        ) : (
          documents.map((doc) => (
            <li
              key={doc.fileId}
              className="document-item"
              onClick={() => onSelect(doc)}
            >
              <span className="document-name">{doc.filename}</span>
            </li>
          ))
        )}
      </ul>
    </div>
  );
};

export default DocumentList;
