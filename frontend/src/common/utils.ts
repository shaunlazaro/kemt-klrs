import { twMerge } from "tailwind-merge";
import { type ClassValue, clsx } from "clsx";
import { ExercisePlanListMock } from "../testData/exercisePlans";
import { RoutineConfig } from "../interfaces/exercisePlan.interface";
import { Patient } from "../interfaces/patient.interface";
import { RepData, RoutineComponentData, RoutineData } from "../interfaces/routineData.interface";
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
  if (!uniqueInjuries.includes("Not Specified")) {
    uniqueInjuries.push("Not Specified");
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

export const REP_ALERT_PENALTY = 0.2

// export function getRepScore(rep: RepData): number {
//   return rep.max_score - REP_ALERT_PENALTY * rep.alerts.length
// }

// We deduct the REP_ALERT_PENALTY for each alert, except if the alert is the rep tracking alert.
export function getRepScore(rep: RepData, exercise: ExerciseDetail): number {
  if (rep.alerts.find((alert) => alert == exercise.rep_tracking?.alert_message))
    return rep.max_score - REP_ALERT_PENALTY * (rep.alerts.length - 1);
  return rep.max_score - REP_ALERT_PENALTY * rep.alerts.length
}

export function getAverageScore(routineData: RoutineData): number {
  const componentScores = routineData.routine_component_data.map(componentData => {
    const totalScore = componentData.rep_data.reduce((sum, rep) => sum + getRepScore(rep, componentData.exercise_detail), 0);
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

// Not straightforward to do this, since we need to match exerciseDetails.
export const getRoutineConfigRepsByComponentData = (componentData: RoutineComponentData, routineData: RoutineData) => {
  console.log(componentData?.exercise_detail?.display_name ?? "")
  console.log(routineData?.routine_config?.exercises?.find((component) => component?.exercise?.display_name ?? ""
    == componentData?.exercise_detail?.display_name ?? ""))
  return routineData?.routine_config?.exercises?.find((component) => component?.exercise?.display_name ?? ""
    == componentData?.exercise_detail?.display_name ?? "")?.reps ?? 0;
}

export const getAverageScoreOfComponentData = (componentData: RoutineComponentData, routineData: RoutineData) => {
  const simplifiedRoutineData = {
    ...routineData,
    routine_component_data: [componentData],
  }
  return getAverageScore(simplifiedRoutineData);
}

export const getAverageFlexion = (componentData: RoutineComponentData) => {
  if (!componentData || !componentData.rep_data || componentData.rep_data.length < 1)
    return;
  return componentData.rep_data.reduce((sum, rep) => sum + rep.max_flexion, 0) / componentData.rep_data.length;
}

export const getAverageExtension = (componentData: RoutineComponentData) => {
  if (!componentData || !componentData.rep_data || componentData.rep_data.length < 1)
    return;
  return componentData.rep_data.reduce((sum, rep) => sum + rep.max_extension, 0) / componentData.rep_data.length;
}