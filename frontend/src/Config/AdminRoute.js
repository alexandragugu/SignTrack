import { useSelector } from "react-redux";
import { Navigate, Outlet } from "react-router-dom";

const AdminRoute = () => {
  const { isAuthenticated, roles, loading } = useSelector(
    (state) => state.auth
  );
  const isAdmin = roles.includes("Admin");

  if (loading) return <div>Se verifică accesul...</div>;

  return isAuthenticated && isAdmin ? (
    <Outlet />
  ) : (
    <Navigate to="/unauthorized" />
  );
};

export default AdminRoute;
