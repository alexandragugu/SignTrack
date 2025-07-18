import React from "react";
import "./SignaturePositionSelector.css";

interface SignaturePositionSelectorProps {
  selectedPosition: string;
  setSelectedPosition: (position: string) => void;
}

const SignaturePositionSelector: React.FC<SignaturePositionSelectorProps> = ({
  selectedPosition,
  setSelectedPosition,
}) => {
  const positions = [
    {
      value: "top-left",
      style: { top: "10px", left: "10px" },
    },
    {
      value: "top-right",
      style: { top: "10px", right: "10px" },
    },
    {
      value: "bottom-left",
      style: { bottom: "10px", left: "10px" },
    },
    {
      value: "bottom-right",
      style: { bottom: "10px", right: "10px" },
    },
  ];

  return (
    <div className="signature-position-container">
      {positions.map((pos) => (
        <label
          key={pos.value}
          className={`signature-position-label ${
            selectedPosition === pos.value ? "selected" : ""
          }`}
          style={pos.style}
          onClick={() => setSelectedPosition(pos.value)}
        ></label>
      ))}
    </div>
  );
};

export default SignaturePositionSelector;
