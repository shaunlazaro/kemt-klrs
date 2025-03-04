import { VariantProps, cva } from "class-variance-authority";
import { forwardRef, ComponentProps } from "react";
import { cn } from "../../common/utils";

type ButtonProps = ComponentProps<"button"> &
  VariantProps<typeof buttonStyles> & {
    full?: boolean;
    disabled?: boolean;
  };

export const buttonStyles = cva(
  "group relative inline-flex justify-center items-center overflow-hidden z-10 px-5 gap-2.5 text-sm tracking-wide rounded-md hover:cursor-pointer",
  {
    variants: {
      variant: {
        primary:
          "bg-primary text-white hover:bg-primary-600 active:bg-primary-700",
        "primary-outline":
          "bg-transparent text-primary hover:bg-primary focus:bg-primary hover:bg-opacity-10 focus:bg-opacity-20 border border-primary",
        "primary-text":
          "bg-transparent text-primary hover:bg-primary focus:bg-primary hover:bg-opacity-10 focus:bg-opacity-20 border-none",
        grey: "bg-neutral-200 text-neutral-600 hover:bg-neutral-300 focus:bg-neutral-350",
        "grey-outline":
          "bg-transparent text-neutral-600 hover:bg-neutral-200 focus:bg-neutral-200 hover:bg-opacity-20 focus:bg-opacity-50 border border-neutral-300",
        "grey-text":
          "bg-transparent text-neutral-600 hover:bg-neutral-200 focus:bg-neutral-200 hover:bg-opacity-20 focus:bg-opacity-50 border-none",
        error: "bg-red text-white hover:bg-red-600 focus:bg-red-700",
        "error-outline":
          "bg-transparent text-red hover:bg-red focus:bg-red hover:bg-opacity-10 focus:bg-opacity-20 border border-red",
        "error-text":
          "bg-transparent text-red hover:bg-red focus:bg-red hover:bg-opacity-10 focus:bg-opacity-20 border-none",
        reverse:
          "bg-white text-neutral-600 hover:bg-neutral-50 focus:bg-neutral-100",
        "reverse-outline":
          "bg-transparent text-white hover:bg-white focus:bg-white hover:bg-opacity-10 focus:bg-opacity-20 border border-neutral-50",
        "reverse-text":
          "bg-transparent text-white hover:bg-white focus:bg-white hover:bg-opacity-20 focus:bg-opacity-50 border-none",
        outline:
          "border border-neutral-200 bg-white hover:bg-neutral-100 hover:text-neutral-900 dark:border-neutral-800 dark:bg-neutral-950 dark:hover:bg-neutral-800 dark:hover:text-neutral-50",
        ghost:
          "hover:bg-neutral-100 hover:text-neutral-900 dark:hover:bg-neutral-800 dark:hover:text-neutral-50",
      },
      size: {
        small: ["py-1.5", "font-regular"],
        medium: ["py-2", "font-semibold"],
      },
    },
    defaultVariants: {
      variant: "primary",
      size: "medium",
    },
  },
);

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      full = false,
      size,
      variant,
      disabled = false,
      className,
      children,
      ...rest
    },
    ref,
  ) => {
    return (
      <button
        ref={ref}
        className={cn(
          buttonStyles({ variant, size, className }),
          disabled && "opacity-25 pointer-events-none",
          full && "w-full",
        )}
        disabled={disabled}
        {...rest}
      >
        {children}
      </button>
    );
  },
);

export default Button;
