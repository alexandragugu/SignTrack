import logo from "./logo.svg";
import "./App.css";
import React, { useEffect, useState } from "react";

import {
  BrowserRouter as Router,
  Route,
  Link,
  Routes,
  Navigate,
} from "react-router-dom";

import LoginForm from "./pages/LoginPage/Login.tsx";
import DocumentReady from "./components/DocumentReady/DocumentReady.tsx";
import Keycloak2 from "./services/Keycloak2.tsx";
import ProtectedRoute from "./Config/ProtectedRoute.js";
import Register from "./services/Register.tsx";
import FilePage from "./pages/FilePage/FilePage.tsx";
import SignReqPage from "./pages/SignReqPage/SignReqPage.tsx";
import AdminRoute from "./Config/AdminRoute.js";
import FileStatus from "./pages/FileStatus/FileStatus.tsx";
import MainLayout from "./components/MainLayout/MainLayout.tsx";
import AnimatedRoutes from "./AnimatedRoutes.tsx";
import SignatureOptions from "./pages/SignatureOptions/SignatureOptions.tsx";
import CSCRedirect from "./services/CSCRedirect.tsx";
import { ToastProvider } from "./context/ToastContext.tsx";
import EditUser from "./pages/EditUser/EditUser.tsx";

import { useDispatch } from "react-redux";
import { login, logout, setAuthLoading } from "./Config/authSlice.js";
import apiClient from "./Utils/ApiClient.tsx";
import { useLocation } from "react-router-dom";
import AdminHomePage from "./Admin/Pages/AdminHomePage/AdminHomePage.tsx";
import AdminUsersPage from "./Admin/Pages/AdminUsersPage/AdminUsersPage.tsx";
import AdminUserDetails from "./Admin/Pages/AdminUserDetails/AdminUserDetails.tsx";
import AdminLayout from "./Admin/AdminLayout/AdminLayout.tsx";
import AdminMetricsPage from "./Admin/Pages/AdminMetricsPage/AdminMetricsPage.tsx";
import FileInfoPage from "./pages/FileInfoPage/FileInfoPage.tsx";
import { GlobalConfirmDialog } from "./components/GlobalConfirmDialog/GlobalConfirmDialog.tsx";
import FlowOverviewDashboard from "./Admin/FlowOverviewDashboard/FlowOverviewDashboard.tsx";
import UserActivityDashboard from "./Admin/UserActivityDasboard/UserActivityDashboard.tsx";
import SignaturesStatistics from "./Admin/SignaturesStatistics/SignaturesStatistics.tsx";
import SystemFiles from "./Admin/SystemFiles/SystemFiles.tsx";
import FlowsStatistics from "./Admin/FlowsStatistics/FlowsStatistics.tsx";
import SignaturesActivityPage from "./Admin/Pages/SignaturesActivityPage/SignaturesActivityPage.tsx";
import ApproveDashboard from "./Admin/ApproveDashboard/ApproveDashboard.tsx";
import ViewDashboard from "./Admin/ViewDashboard/ViewDashboard.tsx";
import DeclineDashboard from "./Admin/DeclineDashboard/DeclineDashboard.tsx";
import BulkSignaturePage from "./pages/BulkSignature/BulkSignature.tsx";
import AdminAdminDetails from "./Admin/Pages/AdminAdminDetails/AdminAdminDetails.tsx";
import SuccessPage from "./pages/SuccessPage/SuccessPage.tsx";
import AdminAccount from "./Admin/Pages/AdminAccount/AdminAccount.tsx";
import System from "./Admin/Pages/System/System.tsx";
import SystemFilesLayout from "./Admin/Pages/SystemFilesLayout/SystemFilesLayout.tsx";
import MyFiles from "./pages/MyFiles/MyFiles.tsx";

function App() {
  const dispatch = useDispatch();
  const location = useLocation();

  useEffect(() => {
    const publicPaths = ["/login", "/register", "/keycloak"];
    const isPublicRoute = publicPaths.includes(location.pathname);

    if (isPublicRoute) {
      return;
    }

    const fetchUser = async () => {
      try {
        const res = await apiClient.get("/auth/me");
        localStorage.setItem("username", res.data.username);
        dispatch(
          login({
            username: res.data.username,
            roles: res.data.roles,
          })
        );
      } catch (err) {
        dispatch(logout());
      }
    };

    fetchUser();
  }, [location.pathname]);

  return (
    <ToastProvider>
      <GlobalConfirmDialog />
      <Routes>
        <Route path="/login" element={<LoginForm />} />
        <Route path="/keycloak" element={<Keycloak2 />} />
        <Route path="/register" element={<Register />} />
        <Route path="/success" element={<SuccessPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/csc" element={<CSCRedirect />} />
          <Route element={<MainLayout />}>
            <Route path="/*" element={<AnimatedRoutes />} />
            <Route path="/csc" element={<CSCRedirect />} />
            <Route path="/bulk" element={<BulkSignaturePage />} />
            <Route path="/documentReady" element={<DocumentReady />} />
            <Route path="/signRequest" element={<SignReqPage />} />
            <Route path="/fileStatus" element={<FileStatus />} />
            <Route path="/sign" element={<SignatureOptions />} />
            <Route path="/filePage" element={<FilePage />} />
            <Route path="/file-info" element={<FileInfoPage />} />
            <Route path="/myFiles" element={<MyFiles />} />
          </Route>
        </Route>
        <Route element={<AdminRoute />}>
          <Route element={<AdminLayout />}>
            <Route path="/admin" element={<AdminHomePage />} />
            <Route path="/admin/users" element={<AdminUsersPage />} />
            <Route path="/admin/users/details" element={<AdminUserDetails />} />
            <Route
              path="/admin/users/details-admin"
              element={<AdminAdminDetails />}
            />
              <Route
              path="/admin/account"
              element={<AdminAccount />}
            />
            <Route path="/admin/users/:userId/edit" element={<EditUser />} />
            <Route path="/admin/system" element={<SystemFilesLayout/>} >
             <Route
                path="/admin/system/files"
                element={<System />}
              >
                <Route path="all" element={<SystemFiles filter="all" />} />
                <Route
                  path="finished"
                  element={<SystemFiles filter="finished" />}
                />
                <Route
                  path="pending"
                  element={<SystemFiles filter="pending" />}
                />
              </Route>

            </Route>
            <Route path="/admin/metrics" element={<AdminMetricsPage />}>
              <Route path="user-activity" element={<UserActivityDashboard />} />

              <Route
                path="/admin/metrics/flows"
                element={<FlowOverviewDashboard />}
              >
                <Route
                  path="statistics/signatures"
                  element={<SignaturesStatistics />}
                />
                <Route
                  path="statistics/signatures-page"
                  element={<SignaturesActivityPage />}
                />
                <Route
                  path="statistics/approvals"
                  element={<ApproveDashboard />}
                />
                <Route path="statistics/views" element={<ViewDashboard />} />
                <Route
                  path="statistics/declines"
                  element={<DeclineDashboard />}
                />
                <Route path="statistics/ranks" element={<FlowsStatistics />} />
              </Route>
            </Route>
          </Route>
        </Route>
      </Routes>
    </ToastProvider>
  );
}

export default App;
