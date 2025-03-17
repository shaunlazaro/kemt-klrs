import { twMerge } from "tailwind-merge";
import { type ClassValue, clsx } from "clsx";
import { ExercisePlanListMock } from "../testData/exercisePlans";
import { RoutineConfig } from "../interfaces/exercisePlan.interface";
import { Patient } from "../interfaces/patient.interface";
import { RoutineData } from "../interfaces/routineData.interface";
import { ExerciseDetail } from "../interfaces/exerciseDetail.interface";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// Utility function, should probably be done by server, or create a "injury" table.
export const getUniqueInjuryValues = (routineConfigs: RoutineConfig[]): string[] => {
  const uniqueInjuries = [...new Set(routineConfigs.map((routineConfig) => routineConfig.injury))];

  if (!uniqueInjuries.includes("Ankle")) {
    uniqueInjuries.push("Ankle");
  }
  if (!uniqueInjuries.includes("Knee")) {
    uniqueInjuries.push("Knee");
  }

  return uniqueInjuries;
}

// Hard coded!
export const defaultInjuryValueList = getUniqueInjuryValues(ExercisePlanListMock);

export const getPatientAge = (patient: Patient) => {
  const today = new Date()
  const timeDistance = Math.abs(today.getTime() - new Date(patient.date_of_birth).getTime());
  const age = Math.floor((timeDistance / (1000 * 60 * 60 * 24) / 365))
  return age
}

export const getPatientName = (patient: Patient) => {
  return `${patient.first_name} ${patient.last_name}`
}

export function getUniqueExercisesWithAlerts(routineDataList: RoutineData[]): {
  exercise: ExerciseDetail;
  alerts: { message: string; count: number }[];
}[] {
  // Map <"display_name",  {exerciseObject, Map<"alertString", frequency>}>
  const exerciseMap = new Map<string, { exercise: ExerciseDetail; alerts: Map<string, number> }>();

  for (const routineData of routineDataList) {
    for (const routineComponentData of routineData.routine_component_data) {
      const exercise = routineComponentData.exercise_detail;
      if (!exercise) continue;

      if (!exerciseMap.has(exercise.display_name)) {
        exerciseMap.set(exercise.display_name, { exercise, alerts: new Map() });
      }
      const exerciseEntry = exerciseMap.get(exercise.display_name)!;

      for (const repData of routineComponentData.rep_data) {
        for (const alert of repData.alerts) {
          exerciseEntry.alerts.set(alert, (exerciseEntry.alerts.get(alert) || 0) + 1);
        }
      }
    }
  }

  return Array.from(exerciseMap.values())
    .map(({ exercise, alerts }) => ({
      exercise,
      alerts: Array.from(alerts.entries())
        .map(([message, count]) => ({ message, count }))
        .sort((a, b) => b.count - a.count) // Sort alerts by count in descending order
    }))
    .sort((a, b) => {
      const totalAlertsA = a.alerts.reduce((sum, alert) => sum + alert.count, 0);
      const totalAlertsB = b.alerts.reduce((sum, alert) => sum + alert.count, 0);
      return totalAlertsB - totalAlertsA; // Sort exercises by total alert count in descending order
    });
}

export function getTotalAlerts(routineData: RoutineData): number {
  return routineData.routine_component_data.reduce((componentAlerts, componentData) => {
    return componentAlerts + componentData.rep_data.reduce((repAlerts, rep) => {
      return repAlerts + rep.alerts.length;
    }, 0);
  }, 0);
}

export function getAverageScore(routineData: RoutineData): number {
  const componentScores = routineData.routine_component_data.map(componentData => {
    const totalScore = componentData.rep_data.reduce((sum, rep) => sum + rep.max_score, 0);
    return componentData.rep_data.length > 0 ? totalScore / componentData.rep_data.length : 0;
  });

  const totalComponentScore = componentScores.reduce((sum, score) => sum + score, 0);
  const averageScore = componentScores.length > 0 ? totalComponentScore / componentScores.length : 0;

  return averageScore * 100; // Convert to percentage
}

export function getPercentCompleted(routineData: RoutineData): number {
  const componentCompletion = routineData.routine_component_data.map(componentData => {
    const totalReps = routineData.routine_config.exercises.find((exercise) => exercise.exercise.display_name == componentData.exercise_detail.display_name)?.reps ?? 0;
    return totalReps > 0 ? componentData.rep_data.length / totalReps : 0;
  });

  const totalCompletion = componentCompletion.reduce((sum, completion) => sum + completion, 0);
  const averageCompletion = componentCompletion.length > 0 ? totalCompletion / componentCompletion.length : 0;

  return averageCompletion * 100; // Convert to percentage
}
