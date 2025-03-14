import { twMerge } from "tailwind-merge";
import { type ClassValue, clsx } from "clsx";
import { ExercisePlanListMock } from "../testData/exercisePlans";
import { RoutineConfig } from "../interfaces/exercisePlan.interface";
import { Patient } from "../interfaces/patient.interface";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// Utility function, should probably be done by server, or create a "injury" table.
const getUniqueInjuryValues = (routineConfigs: RoutineConfig[]): string[] => {
  return [...new Set(routineConfigs.map((routineConfig) => routineConfig.injury))]
}

// Hard coded!
export const injuryValueList = getUniqueInjuryValues(ExercisePlanListMock);

export const getPatientAge = (patient: Patient) => {
  const today = new Date()
  const timeDistance = Math.abs(today.getTime() - patient.dateOfBirth.getTime());
  const age = Math.floor((timeDistance / (1000 * 60 * 60 * 24) / 365))
  return age
}

export const getPatientName = (patient: Patient) => {
  return `${patient.firstName} ${patient.lastName}`
}