import React from "react";
import { Accordion, AccordionTab } from "primereact/accordion";
import "./UserFlows.css";
import AssociatedFlows from "./../UserAssociatedFlows/UserAssociatedFlows.tsx";
import UserAssignedFlows from "../UserAssignedFlows/UserAssignedFlows.tsx";

const UserFlows = ({ userId }) => {
  return (
    <div className="user-flows-container">
      <Accordion multiple activeIndex={[0]}>
        <AccordionTab
          header={
            <span className="accordion-header">
              <i
                className="pi pi-folder-open"
                style={{ marginRight: "0.5rem" }}
              />
              Personal Flows
            </span>
          }
        >
          <div className="accordion-content">
            <AssociatedFlows userId={userId} />
          </div>
        </AccordionTab>

        <AccordionTab
          header={
            <span className="accordion-header">
              <i className="pi pi-users" style={{ marginRight: "0.5rem" }} />
              Assigned Flows
            </span>
          }
        >
          <div className="accordion-content">
            <UserAssignedFlows userId={userId} />
          </div>
        </AccordionTab>
      </Accordion>
    </div>
  );
};

export default UserFlows;
