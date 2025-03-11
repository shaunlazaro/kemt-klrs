import { twMerge } from "tailwind-merge";
import { type ClassValue, clsx } from "clsx";
import { ExercisePlanListMock } from "../testData/exercisePlans";
import { RoutineConfig } from "../interfaces/exercisePlan.interface";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// Utility function, should probably be done by server, or create a "injury" table.
const getUniqueInjuryValues = (routineConfigs: RoutineConfig[]): string[] => {
  return [...new Set(routineConfigs.map((routineConfig) => routineConfig.injury))]
}
// Hard coded!
export const filterList = getUniqueInjuryValues(ExercisePlanListMock);
