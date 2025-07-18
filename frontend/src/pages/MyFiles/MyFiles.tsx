import React from "react";
import { useLocation } from "react-router-dom";
import "./MyFiles.css";
import TabFiles from "../../components/TabFiles/TabFiles.tsx";
import { motion } from "framer-motion";

const MyFiles = () => {
  const location = useLocation();
  const initialIndex = location.state?.index || 0;

  return (
    <motion.div
      initial={{ x: "-80vw", opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      exit={{ x: "-40vw", opacity: 0 }}
      transition={{ type: "tween", duration: 0.6 }}
    >
      <TabFiles initialIndex={initialIndex} />
    </motion.div>
  );
};
export default MyFiles;
