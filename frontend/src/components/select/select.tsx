import React, { ComponentProps } from "react";

type InputProps = ComponentProps<"select"> & {
  items: object[] | any[];
  label?: string;
  valueKey?: string;
  placeholderString?: string;
};

const Select = React.forwardRef<HTMLSelectElement, InputProps>((props, ref) => (
  <select
    ref={ref}
    key={props.id}
    className="rounded-lg border-2 border-[#E2E2E2] px-2 py-1.5 text-xs font-semibold text-neutral-600"
    {...props}
  >
    {!props.items && <option key="empty">N/A</option>}
    {props.placeholderString && (
      <option key="placeholder" value="">
        {props.placeholderString}
      </option>
    )}
    {props.items &&
      (typeof props.items[0] === "string" ||
        typeof props.items[0] === "number") &&
      props.items.map((item, i) => (
        <option
          key={i.toString() + item}
          value={item.toString().toUpperCase().startsWith("ALL") ? "" : item}
        >
          {item}
        </option>
      ))}
    {props.items &&
      props.items.length > 0 &&
      typeof props.items[0] !== "string" &&
      typeof props.items[0] !== "number" &&
      props.items.map((item, i) => (
        <option
          key={i.toString() + item[props.valueKey as string]}
          value={
            item.toString().toUpperCase().startsWith("ALL")
              ? ""
              : item[props.valueKey as string]
          }
        >
          {item[props.label as string]}
        </option>
      ))}
  </select>
));

export const WideSelect = React.forwardRef<HTMLSelectElement, InputProps>((props, ref) => (
  <select
    ref={ref}
    key={props.id}
    // className="w-full rounded-lg border-2 border-[#E2E2E2] px-2 py-1.5 text-xs font-semibold text-neutral-600"
    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-base ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 md:text-sm"
    {...props}
  >
    {!props.items && <option key="empty">N/A</option>}
    {props.placeholderString && (
      <option key="placeholder" value="">
        {props.placeholderString}
      </option>
    )}
    {props.items &&
      (typeof props.items[0] === "string" ||
        typeof props.items[0] === "number") &&
      props.items.map((item, i) => (
        <option
          key={i.toString() + item}
          value={item.toString().toUpperCase().startsWith("ALL") ? "" : item}
        >
          {item}
        </option>
      ))}
    {props.items &&
      props.items.length > 0 &&
      typeof props.items[0] !== "string" &&
      typeof props.items[0] !== "number" &&
      props.items.map((item, i) => (
        <option
          key={i.toString() + item[props.valueKey as string]}
          value={
            item.toString().toUpperCase().startsWith("ALL")
              ? ""
              : item[props.valueKey as string]
          }
        >
          {item[props.label as string]}
        </option>
      ))}
  </select>
));

export default Select;
