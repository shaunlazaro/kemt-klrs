// import { useState } from "react";

import { GoogleLoginButton } from "../../components/googleLoginButton/googleLoginButton";

// TODO: This page
const SigninFail: React.FC = () => {
  return (
    <div className="grid grid-cols-2">
      <div className="h-full bg-primary-darkblue">
        <div className="flex justify-center flex-col items-center h-full pt-16">
          <div className="flex text-white text-6xl font-semibold gap-4 pb-12"> <img className="w-auto h-auto" src="logo-lg.png" /> RePose </div>
          <div className="text-white text-xl font-normal w-2/5 text-center"> Transforming physiotherapy care with real-time patient feedback </div>
          <div className="w-2/3 h-2/3"> <img src="/login-art.png"></img></div>
        </div>
      </div>
      <div className="h-full bg-white">
        <div className="flex-col flex justify-center w-full h-full items-center">
          <div className="flex flex-col items-start text-left w-full px-20">
            <div className="text-black text-xl font-semibold pb-6">Sign in failed.</div>
            <GoogleLoginButton />
          </div>
        </div>
      </div>
    </div>
  );
};

export default SigninFail;
