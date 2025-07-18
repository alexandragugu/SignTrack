import React, { createContext, useState, useContext, ReactNode } from "react";

interface AuthContextType {
  authCode: string | null;
  setAuthCode: (code: string | null) => void;
  accessToken: string | null;
  setAccessToken: (token: string | null) => void;
  credentialID: string | null;
  setCredentialID: (id: string | null) => void;
  hash: string | null;
  setHash: (hash: string | null) => void;
  signAlgo: string | null;
  setSignAlgo: (algo: string | null) => void;
  isRedirected: boolean;
  setIsRedirected: (redirected: boolean) => void;
  isUploading: boolean;
  setIsUploading: (uploading: boolean) => void;
  isTokenFetched: boolean;
  setIsTokenFetched: (fetched: boolean) => void;
  isSigningPrepared: boolean;
  setIsSigningPrepared: (prepared: boolean) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [authCode, setAuthCode] = useState<string | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [credentialID, setCredentialID] = useState<string | null>(null);
  const [hash, setHash] = useState<string | null>(null);
  const [signAlgo, setSignAlgo] = useState<string | null>(null);
  const [isRedirected, setIsRedirected] = useState<boolean>(false);
  const [isUploading, setIsUploading] = useState(false);
  const [isTokenFetched, setIsTokenFetched] = useState(false);
  const [isSigningPrepared, setIsSigningPrepared] = useState(false);

  return (
    <AuthContext.Provider
      value={{
        authCode,
        setAuthCode,
        accessToken,
        setAccessToken,
        credentialID,
        setCredentialID,
        hash,
        setHash,
        signAlgo,
        setSignAlgo,
        isRedirected,
        setIsRedirected,
        isUploading,
        setIsUploading,
        isTokenFetched,
        setIsTokenFetched,
        isSigningPrepared,
        setIsSigningPrepared,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
