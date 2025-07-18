import React from "react";
import { useNavigate } from "react-router-dom";
import SignatureActivityPreview from "../../SignatureActivityPreview/SignatureActivityPreview.tsx";
import SignaturesActivityTable from "../../SignaturesActivityTable/SignaturesActivityTable.tsx";

const SignaturesActivityPage = () => {
  const navigate = useNavigate();
  return (
    <div style={{ padding: "2rem" }}>
      <div
        style={{
          display: "flex",
          justifyContent: "flex-end",
          marginBottom: "1rem",
        }}
      >
        <button
          onClick={() => navigate(-1)}
          style={{
            backgroundColor: "rgba(160, 16, 193, 0.42)",
            color: "white",
            width: "50px",
            height: "50px",
            border: "2px solid rgba(194, 12, 170, 0.66)",
            borderRadius: "50%",
            fontWeight: 600,
            cursor: "pointer",
            boxShadow: "0 4px 8px rgba(0, 0, 0, 0.3)",
            transition: "background-color 0.2s ease",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            fontSize: "1.2rem",
            marginTop: "0",
          }}
          onMouseOver={(e) =>
            (e.currentTarget.style.backgroundColor = "rgba(161, 27, 143, 0.42)")
          }
          onMouseOut={(e) =>
            (e.currentTarget.style.backgroundColor = "rgba(160, 16, 193, 0.42)")
          }
        >
          <i className="pi pi-arrow-left"></i>
        </button>
      </div>
      <SignatureActivityPreview />
      <SignaturesActivityTable />
    </div>
  );
};

export default SignaturesActivityPage;
