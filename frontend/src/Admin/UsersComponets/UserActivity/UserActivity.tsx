import React, { useEffect, useState } from "react";
import apiClient from "../../../Utils/ApiClient.tsx";
import { Skeleton } from "primereact/skeleton";
import { motion } from "framer-motion";
import "./UserActivity.css";

const UserActivity = ({ userId }) => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const res = await apiClient.get(`/users/activity/${userId}`);
        setStats(res.data);
      } catch (err) {
        console.error("Error fetching user stats", err);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, [userId]);

  const cardVariants = {
    hidden: { opacity: 0, y: 30 },
    visible: (i) => ({
      opacity: 1,
      y: 0,
      transition: {
        delay: i * 0.1,
        duration: 0.5,
        ease: "easeOut",
      },
    }),
    hover: {
      scale: 1.03,
      transition: { duration: 0.3 },
    },
  };

  return (
    <motion.div
      className="user-stats-container"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.6 }}
    >
      {loading ? (
        <Skeleton width="100%" height="300px" />
      ) : (
        <div className="user-stats-grid">
          <div className="user-stats-row">
            {[0, 1, 2].map((i) => (
              <motion.div
                key={i}
                className="user-stats-card"
                variants={cardVariants}
                initial="hidden"
                animate="visible"
                whileHover="hover"
                custom={i}
              >
                <h4>
                  {
                    [
                      "Viewed Documents",
                      "Signed Documents",
                      "Approved Documents",
                    ][i]
                  }
                </h4>
                <p>
                  {
                    [
                      stats?.viewedDocs || 0,
                      stats?.signedDocs || 0,
                      stats?.approvedDocs || 0,
                    ][i]
                  }
                </p>
              </motion.div>
            ))}
          </div>

          <div className="user-stats-row">
            {[0, 1].map((i) => (
              <motion.div
                key={i + 3}
                className="user-stats-card"
                variants={cardVariants}
                initial="hidden"
                animate="visible"
                whileHover="hover"
                custom={i + 3}
              >
                <h4>{["Declined Documents", "Last Activity"][i]}</h4>
                <p>
                  {i === 0
                    ? stats?.declinedDocs || 0
                    : stats?.lastActivity
                    ? new Date(stats.lastActivity).toLocaleString()
                    : "No activity"}
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      )}
    </motion.div>
  );
};

export default UserActivity;
