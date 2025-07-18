const config = {
  KEYCLOAK_CLIENT_ID: "test_client",
  //KEYCLOAK_URL: "http://localhost:8081/auth",
  KEYCLOAK_URL: "http://localhost:8081/auth",
  // KEYCLOAK_REALM: "my-realm",
  KEYCLOAK_REALM: "SignTrack",
  KEYCLOAK_REDIRECT_URI: "http://localhost:3000/keycloak",
  //KEYCLOAK_REDIRECT_URI: "https://localhost/keycloak",
  KEYCLOAK_REDIRECT_URI_LOGOUT: "http://localhost:3000/login",
  KEYCLOAK_REDIRECT_URI_LOGIN: "http://localhost:3000/register",
  //CLIENT_ID_CSC: "mta-test",
  //GRANT_TYPE_CSC: "authorization_code",
  //MAX_UPLOAD_SIZE: 10000000, // 10 MB

  //BACKEND_URL: "/api",
  EVENT_SOURCE:"http://localhost:8080/file/stream",
  //EVENT_SOURCE:"https://localhost/api/file/stream",
  BACKEND_URL: "http://localhost:8080",
  NEXU_URL: "https://localhost:9895/",
  FRONTEND_URL: "http://localhost:3000",
  CSC_CLINET_ID: "mta-test",
  CSC_REDIRECT_URI: "http://localhost:3000/csc",
  //CSC_REDIRECT_URI: "https://localhost/csc",
  CSC_PROVIDER_URL: "https://msign-test.transsped.ro/csc/v1",
  GRAFANA_URL: "http://localhost:4000/",
};

export default config;
