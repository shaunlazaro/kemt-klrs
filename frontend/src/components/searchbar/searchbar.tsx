import React, { ComponentProps } from "react";
import { MdSearch } from "react-icons/md";

type InputProps = ComponentProps<"input">;

const Searchbar = React.forwardRef<HTMLInputElement, InputProps>(
  (props, ref) => (
    <div className="relative">
      <div className="pointer-events-none absolute inset-y-0 start-0 flex items-center ps-1.5">
        <MdSearch size="1.5em" color="#242626" />
      </div>
      <input
        ref={ref}
        type="search"
        className="block h-9 w-[328px] rounded-md border-2 border-[#E2E2E2] px-4 py-1.5 ps-9 text-sm"
        {...props}
      />
    </div>
  ),
);

export default Searchbar;
