const config = {
  KEYCLOAK_CLIENT_ID: "t ",
  KEYCLOAK_URL: "http://localhost:8081/auth",
  KEYCLOAK_REALM: "SignTrack",
  KEYCLOAK_REDIRECT_URI: "http://localhost:3000/keycloak",
  KEYCLOAK_REDIRECT_URI_LOGOUT: "http://localhost:3000/login",
  KEYCLOAK_REDIRECT_URI_LOGIN: "http://localhost:3000/register",
  EVENT_SOURCE:"http://localhost:8080/file/stream",
  BACKEND_URL: "http://localhost:8080",
  NEXU_URL: "https://localhost:9895/",
  FRONTEND_URL: "http://localhost:3000",
  CSC_CLINET_ID: " ",
  CSC_REDIRECT_URI: "http://localhost:3000/csc",
  CSC_PROVIDER_URL: "https://msign-test.transsped.ro/csc/v1",
  GRAFANA_URL: "http://localhost:4000/",
};

export default config;
