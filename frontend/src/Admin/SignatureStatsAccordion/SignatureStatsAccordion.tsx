import React from "react";
import { Accordion, AccordionTab } from "primereact/accordion";
import { Button } from "primereact/button";
import { useNavigate } from "react-router-dom";
import "./SignatureStatsAccordion.css";
import SignatureActivityPreview from "../SignatureActivityPreview/SignatureActivityPreview.tsx";

const SignatureStatsAccordion = () => {
  const navigate = useNavigate();

  return (
    <div className="signature-accordion-container">
      <Accordion multiple activeIndex={[0]}>
        <AccordionTab
          header={
            <div className="accordion-header">
              <i
                className="pi pi-user-edit"
                style={{ marginRight: "0.5rem" }}
              />
              Signers Activity
              <Button
                label="Details"
                icon="pi pi-arrow-right"
                className="p-button-text p-button-sm p-button-secondary"
                onClick={() =>
                  navigate("/admin/metrics/flows/statistics/signatures-page", {
                    state: { tab: "flows", subTab: "signatures" },
                  })
                }
                style={{ marginLeft: "auto", color: "white" }}
              />
            </div>
          }
        >
          <SignatureActivityPreview />
        </AccordionTab>
      </Accordion>
    </div>
  );
};

export default SignatureStatsAccordion;
