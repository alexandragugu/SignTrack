import React from "react";
import { Routes, Route, useLocation } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";
import HomePage from "./pages/HomePage/HomePage.tsx";
import SignPage from "./pages/SignPage/SignPage.tsx";
import FileUpload from "./components/FileUpload/FileUpload.tsx";
import RecipientForm from "./components/RecipientForm/RecipientForm.tsx";
import FileInfoPage from "./pages/FileInfoPage/FileInfoPage.tsx";
import MyAccount from "./pages/MyAccount/MyAccount.tsx";

const pageVariants = {
  initial: { opacity: 0, y: -100 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.3 } },
  exit: { opacity: 0, y: 100, transition: { duration: 0.3 } },
};

const pageVariantsUpload = {
  initial: { opacity: 0, y: -100 },
  animate: { opacity: 1, x: 0, transition: { duration: 0.3 } },
  exit: { opacity: 0, x: -100, transition: { duration: 0.3 } },
};

const pageVariantsReceipients = {
  initial: { opacity: 0, x: 100 },
  animate: { opacity: 1, x: 0, transition: { duration: 0.3 } },
  exit: { opacity: 0, x: -100, transition: { duration: 0.3 } },
};

const AnimatedRoutes = () => {
  const location = useLocation();
  const direction = location.pathname === "/recipients" ? 1 : -1;

  return (
    <AnimatePresence mode="wait">
      <Routes location={location} key={location.pathname}>
        <Route
          path="/"
          element={
            <motion.div
              variants={pageVariants}
              initial="initial"
              animate="animate"
              exit="exit"
            >
              <HomePage />
            </motion.div>
          }
        />
        <Route
          path="/upload"
          element={
            <motion.div
              variants={pageVariantsUpload}
              initial="initial"
              animate="animate"
              exit="exit"
            >
              <FileUpload />
            </motion.div>
          }
        />
        <Route
          path="/recipients"
          element={
            <motion.div
              variants={pageVariantsReceipients}
              initial="initial"
              animate="animate"
              exit="exit"
              custom={direction}
            >
              <RecipientForm />
            </motion.div>
          }
        />


        <Route
          path="/myAccount"
          element={
            <motion.div
              variants={pageVariants}
              initial="initial"
              animate="animate"
              exit="exit"
            >
              <MyAccount />
            </motion.div>
          }
        />
      </Routes>
    </AnimatePresence>
  );
};

export default AnimatedRoutes;
