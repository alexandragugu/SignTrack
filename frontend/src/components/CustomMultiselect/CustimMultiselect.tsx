import React from "react";
import { Checkbox } from "primereact/checkbox";

const roleOptions = ["Signer", "Viewer", "Approver"];

const CustomMultiSelectFilter = ({ value, onChange }) => {
  const isAllSelected = value?.length === roleOptions.length;

  const toggleAll = (checked) => {
    if (checked) {
      onChange(roleOptions);
    } else {
      onChange([]);
    }
  };

  const toggleOption = (option) => {
    if (value?.includes(option)) {
      onChange(value.filter((val) => val !== option));
    } else {
      onChange([...(value || []), option]);
    }
  };

  return (
    <div
      className="custom-multiselect-filter"
      style={{ padding: "0.5rem 0.75rem" }}
    >
      <div className="flex align-items-center mb-2">
        <Checkbox
          inputId="selectAll"
          checked={isAllSelected}
          onChange={(e) => toggleAll(e.checked)}
        />
        <label
          htmlFor="selectAll"
          style={{ marginLeft: "0.5rem", color: "white" }}
        >
          Select all roles
        </label>
      </div>
      {roleOptions.map((option) => (
        <div key={option} className="flex align-items-center mb-2">
          <Checkbox
            inputId={option}
            checked={value?.includes(option)}
            onChange={() => toggleOption(option)}
          />
          <label
            htmlFor={option}
            style={{ marginLeft: "0.5rem", color: "white" }}
          >
            {option}
          </label>
        </div>
      ))}
    </div>
  );
};

export default CustomMultiSelectFilter;
