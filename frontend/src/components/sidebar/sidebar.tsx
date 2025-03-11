import { Link, useLocation } from "react-router-dom";
// import { useState } from "react";
// import { MdArrowBackIosNew } from "react-icons/md";
import { cn } from "../../common/utils";
import { NavItems, type NavItem } from "./items";

interface SidebarProps {
  className?: string;
}

export default function Sidebar({ className }: SidebarProps) {
  const { pathname } = useLocation();
  const isOpen = true;// const [isOpen, setIsOpen] = useState(true);

  // const toggleSidebar = () => setIsOpen((prev) => !prev);

  return (
    <nav
      className={cn(
        "relative hidden h-screen border-r pt-0 md:block transition-all duration-300 bg-primary-blue text-white",
        isOpen ? "w-72" : "w-[72px]",
        className,
      )}
    >
      {/* Expandable sidebar, just comment out the button if we're not using that part */}
      {/* <div
            className="absolute -right-5 top-20 z-10 flex h-7 w-5 cursor-pointer rounded-r-md border bg-white shadow"
            onClick={toggleSidebar}
        >
            <MdArrowBackIosNew
            size={15}
            className={cn(
                "text-neutral-600 self-center font-extrabold transition-transform",
                !isOpen && "rotate-180",
            )}
            />
        </div> */}
      <div className="pb-6 flex items-center px-5 pt-6">
        <div className="w-14 h-14 border-2 border-white rounded-lg"></div>
        <span className="h-auto ml-3 font-semibold text-2xl mb-1">RePose</span>
      </div>
      <div className="space-y-2 pb-2 h-full overflow-y-auto">
        {NavItems.map((item: NavItem) => (
          <Link
            key={item.title}
            to={item.href}
            className={cn(
              "flex items-center gap-3 p-4 transition hover:bg-primary-lightblue hover:text-black text-white",
              pathname === item.href && "bg-secondary-darkpink",
              pathname != item.href && "bg-primary-blue"
            )}
          >
            <item.icon className="h-6 w-6" />
            {isOpen && <span className="text-md font-semibold">{item.title}</span>}
          </Link>
        ))}
      </div>
    </nav>
  );
}