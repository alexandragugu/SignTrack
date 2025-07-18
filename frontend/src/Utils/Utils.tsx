import { NavigateFunction, useNavigate } from "react-router-dom";
import React from "react";

export function withNavigate(Component: any) {
  return function WrappedComponent(props: any) {
    const navigate = useNavigate();
    return <Component {...props} navigate={navigate} />;
  };
}
