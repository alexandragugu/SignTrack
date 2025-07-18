import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import apiClient from "../../Utils/ApiClient.tsx";
import "./HomeChart.css";

const cardVariants = {
  hidden: { opacity: 0, y: 40 },
  visible: (i: number) => ({
    opacity: 1,
    y: 0,
    transition: {
      delay: i * 0.15,
      duration: 0.5,
      type: "spring",
      stiffness: 60,
    },
  }),
  hover: {
    scale: 1.05,
    boxShadow: "0px 12px 25px rgba(160, 64, 255, 0.5)",
  },
};

const HomeChart = () => {
  const navigate = useNavigate();
  const [personalFlows, setPersonalFlows] = useState(0);
  const [assignedFlows, setAssignedFlows] = useState(0);
  const [pendingFlows, setPendingFlows] = useState(0);

  useEffect(() => {
    const fetchFlowData = async () => {
      try {
        const response = await apiClient.get("/files/dashboard", {
          withCredentials: true,
        });
        const { personal, assigned, pending } = response.data;
        setPersonalFlows(personal);
        setAssignedFlows(assigned);
        setPendingFlows(pending);
      } catch (error) {
        console.error("Failed to fetch flow data:", error);
      }
    };

    fetchFlowData();
  }, []);

  const cards = [
    {
      title: "Personal Flows",
      subtitle: "Documents you created",
      value: personalFlows,
      style: "personal",
    },
    {
      title: "Assigned Flows",
      subtitle: "Documents awaiting your action",
      value: assignedFlows,
      style: "assigned",
    },
    {
      title: "Pending Flows",
      subtitle: "In progress or awaiting others",
      value: pendingFlows,
      style: "pending",
    },
  ];

  return (
    <div>
      <div className="flow-card-container">
        {cards.map((card, index) => (
          <motion.div
            className={`flow-card ${card.style}`}
            key={card.title}
            custom={index}
            variants={cardVariants}
            initial="hidden"
            animate="visible"
            whileHover="hover"
          >
            <h4>{card.title}</h4>
            <p>{card.subtitle}</p>
            <div className="flow-footer">
              <span>{card.value}</span>
            </div>
          </motion.div>
        ))}
      </div>

      <motion.div
        className="quick-actions"
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 0.6, type: "spring", stiffness: 70 }}
      >
        <motion.div
          className="action-card"
          whileHover={{ scale: 1.1, rotate: 1 }}
          whileTap={{ scale: 0.98 }}
          transition={{ type: "spring", stiffness: 300 }}
          onClick={() => navigate("/upload")}
        >
          <i className="pi pi-plus-circle" />
          <span>Create Flow</span>
        </motion.div>
      </motion.div>
    </div>
  );
};

export default HomeChart;
