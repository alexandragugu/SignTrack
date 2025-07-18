import React, { createContext, useRef, ReactNode } from "react";
import CustomToast from "../components/CustomToast/CustomToast.tsx";

type ToastMethods = {
  showSuccess: (msg: string) => void;
  showError: (msg: string) => void;
};

export const ToastContext = createContext<ToastMethods>({
  showSuccess: () => {},
  showError: () => {},
});

type ToastProviderProps = {
  children: ReactNode;
};

export const ToastProvider = ({ children }: ToastProviderProps) => {
  const toastRef = useRef<ToastMethods>(null);

  const showSuccess = (message: string) =>
    toastRef.current?.showSuccess(message);
  const showError = (message: string) => toastRef.current?.showError(message);

  return (
    <ToastContext.Provider value={{ showSuccess, showError }}>
      <CustomToast ref={toastRef} />
      {children}
    </ToastContext.Provider>
  );
};
