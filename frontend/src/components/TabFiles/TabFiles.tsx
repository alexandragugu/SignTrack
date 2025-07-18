import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { TabView, TabPanel } from "primereact/tabview";
import "primereact/resources/themes/saga-purple/theme.css";
import "./TabFiles.css";
import CustomTable from "../CustomTable/CustomTable.tsx";
import { AnimatePresence, motion } from "framer-motion";
import AssignedFlows from "../AssignedFlows/AssignedFlows.tsx";

const tabVariants = {
  initial: { opacity: 0, y: -10 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.5 } },
  exit: { opacity: 0, y: 10, transition: { duration: 0.5 } },
};

const TabFiles = ({ initialIndex = 0 }) => {
  const [activeIndex, setActiveIndex] = useState(initialIndex);
  const navigate = useNavigate();

  const handleNavigate = () => {
    navigate("/upload");
  };

  return (
    <div className="tab-files-container">
      <TabView
        activeIndex={activeIndex}
        onTabChange={(e) => setActiveIndex(e.index)}
        className="custom-tabview"
      >
        <TabPanel header="Personal Flows" contentClassName="custom-tabpanel">
          <AnimatePresence mode="wait">
            <motion.div
              key={activeIndex}
              variants={tabVariants}
              initial="initial"
              animate="animate"
              exit="exit"
            >
              <div className="tab-content">
                <CustomTable />
              </div>
            </motion.div>
          </AnimatePresence>
        </TabPanel>
        <TabPanel header="Assigned Flows" contentClassName="custom-tabpanel">
          <AnimatePresence mode="wait">
            <motion.div
              key={activeIndex}
              variants={tabVariants}
              initial="initial"
              animate="animate"
              exit="exit"
            >
              <AssignedFlows />
            </motion.div>
          </AnimatePresence>
        </TabPanel>
      </TabView>
    </div>
  );
};

export default TabFiles;
