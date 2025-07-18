import React, { useState } from "react";
import { Dropdown } from "primereact/dropdown";

const SignatureDropdown = ({
  onSelect,
}: {
  onSelect: (option: string) => void;
}) => {
  const options = [
    { label: "Token Signature", value: "token-signature" },
    { label: "Cloud Signature", value: "cloud-signature" },
  ];

  const [selectedOption, setSelectedOption] = useState(options[0]);

  const handleChange = (e: any) => {
    setSelectedOption(e.value);
    onSelect(e.value);
  };

  return (
    <div className="p-field">
      <Dropdown
        value={selectedOption}
        options={options}
        onChange={handleChange}
        placeholder="Select Signature"
      />
    </div>
  );
};

export default SignatureDropdown;
