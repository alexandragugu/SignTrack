import React from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import "./AdminHomePage.css";
import "primeicons/primeicons.css";

const MotionLink = motion(Link);

const cardVariants = {
  hidden: { opacity: 0, y: 30 },
  visible: (i: number) => ({
    opacity: 1,
    y: 0,
    transition: {
      delay: i * 0.2,
      duration: 0.6,
      type: "spring",
      stiffness: 80,
    },
  }),
  hover: {
    scale: 1.05,
    boxShadow: "0px 12px 25px rgba(100, 50, 255, 0.4)",
  },
};

const AdminHomePage = () => {
  const cards = [
    {
      to: "/admin/users",
      icon: "pi pi-users",
      title: "Users",
      description: "View and manage all users",
    },
    {
      to: "/admin/metrics/flows/statistics/signatures",
      icon: "pi pi-chart-bar",
      title: "Metrics",
      description: "Analyze system activity",
    },
    {
      to: "/admin/system/files/all",
      icon: "pi pi-file-pdf",
      title: "System",
      description: "Manage system files",
    },
  ];

  return (
    <motion.div
      className="admin-container"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.8 }}
    >
      <div className="admin-home">
        <motion.h1
          className="glow-text"
          initial={{ y: -40, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ type: "spring", duration: 1 }}
        >
          Welcome Admin!
        </motion.h1>

        <motion.p
          className="subheading"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.5 }}
        >
          Manage users, system flows and activity
        </motion.p>

        <div className="admin-cards">
          {cards.map((card, i) => (
            <MotionLink
              to={card.to}
              key={card.title}
              className="card dark-card"
              variants={cardVariants}
              initial="hidden"
              animate="visible"
              whileHover="hover"
              custom={i}
            >
              <i className={`${card.icon} icon`} />
              <h2>{card.title}</h2>
              <p>{card.description}</p>
            </MotionLink>
          ))}
        </div>
      </div>
    </motion.div>
  );
};

export default AdminHomePage;
