import React, { forwardRef, useImperativeHandle, useRef } from "react";
import { Toast } from "primereact/toast";
import "./CustomToast.css";

const CustomToast = forwardRef((props, ref) => {
  const toast = useRef(null);

  useImperativeHandle(ref, () => ({
    showSuccess: (message) => {
      toast.current?.show({
        severity: "success",
        summary: "Success",
        detail: message,
        life: 4000,
      });
    },
    showError: (message) => {
      toast.current?.show({
        severity: "error",
        summary: "Error",
        detail: message,
        life: 5000,
      });
    },
  }));

  return <Toast ref={toast} position="top-right" className="custom-toast" />;
});

export default CustomToast;
