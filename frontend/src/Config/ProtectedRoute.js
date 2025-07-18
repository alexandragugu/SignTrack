import { useSelector } from "react-redux";
import { Navigate, Outlet } from "react-router-dom";

const ProtectedRoute = () => {
  const { isAuthenticated, roles, loading } = useSelector(
    (state) => state.auth
  );

  if (loading) return <div>Se verifica autentificarea...</div>;

  const isUser = roles.includes("User");

  return isAuthenticated && isUser ? (
    <Outlet />
  ) : (
    <Navigate to="/login" replace />
  );
};

export default ProtectedRoute;
