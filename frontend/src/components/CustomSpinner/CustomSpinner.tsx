import React from "react";
import { useSpring, animated } from "@react-spring/web";
import "./CustomSpinner.css";

const CustomSpinner = () => {
  const spin = useSpring({
    loop: true,
    from: { transform: "rotate(0deg)" },
    to: { transform: "rotate(360deg)" },
    config: { duration: 1000 },
  });

  return (
    <div className="spinner-overlay">
      <animated.div style={spin} className="spinner-circle" />
    </div>
  );
};

export default CustomSpinner;
