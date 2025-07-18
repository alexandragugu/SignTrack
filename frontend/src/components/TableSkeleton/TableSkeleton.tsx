import React from "react";
import { Skeleton } from "primereact/skeleton";
const TableSkeleton = () => {
  return (
    <div>
      <Skeleton shape="circle" size="2rem" className="mr-2"></Skeleton>
      <Skeleton shape="circle" size="3rem" className="mr-2"></Skeleton>
      <Skeleton shape="circle" size="4rem" className="mr-2"></Skeleton>
      <Skeleton shape="circle" size="5rem"></Skeleton>
    </div>
  );
};

export default TableSkeleton;
