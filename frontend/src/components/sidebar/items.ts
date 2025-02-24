import { MdOutlineHome } from "react-icons/md";
import { HiOutlineUsers } from "react-icons/hi";
import { FiBookOpen } from "react-icons/fi";
import { type IconType } from "react-icons";
import { HOME_PATH, PATIENTS_PATH, EXERCISES_PATH } from "../../routes/name";

export interface NavItem {
  title: string;
  href: string;
  icon: IconType;
}

export const NavItems: NavItem[] = [
  {
    title: "Home",
    icon: MdOutlineHome,
    href: HOME_PATH,
  },
  {
    title: "Patients",
    icon: HiOutlineUsers,
    href: PATIENTS_PATH,
  },
  {
    title: "Exercises",
    icon: FiBookOpen,
    href: EXERCISES_PATH,
  },
];
