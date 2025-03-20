// import Header from "components/organisms/header/header";
import Sidebar from "./components/sidebar/sidebar";
import React from "react";

export const PageTemplate = ({ children }: { children: React.ReactNode }) => {
  return (
    <>
      {/* <Header /> */}
      <div className="flex h-screen border-collapse overflow-hidden">
        <Sidebar />
        <main className="flex-1 overflow-y-auto overflow-x-hidden mt-16 bg-secondary/10">
          {children}
        </main>
      </div>
    </>
  );
};