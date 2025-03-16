import {
    QueryOptions,
    useMutation,
    useQuery,
    useQueryClient,
} from "@tanstack/react-query";
import { QueryKeys } from "./queryKeys.enum";
// import { Pagination } from "../api.type";

// Utils
import request from "./request";

import { Patient } from "../interfaces/patient.interface";
import { RoutineConfig } from "../interfaces/exercisePlan.interface";
import { ExerciseDetail } from "../interfaces/exerciseDetail.interface";


export const useGetRoutineConfigs = () => {
    return useQuery({
        queryKey: [QueryKeys.ROUTINE_CONFIGS],
        queryFn: () =>
            request.get(`/routine-configs`) as Promise<RoutineConfig[]>,
        staleTime: 10000,
        // enabled: !!reportId,
    });
};

export const useGetExerciseDetails = () => {
    return useQuery({
        queryKey: [QueryKeys.EXERCISE_DETAILS],
        queryFn: () =>
            request.get(`/exercise-details`) as Promise<ExerciseDetail[]>,
        staleTime: 10000,
        // enabled: !!reportId,
    });
};


export const useGetRoutineConfigById = (routineConfigId: string) => {
    return useQuery({
        queryKey: [QueryKeys.ROUTINE_CONFIG, routineConfigId],
        queryFn: () =>
            request.get(`/routine-configs/${routineConfigId}`) as Promise<RoutineConfig>,
        staleTime: 10000,
        enabled: !!routineConfigId && routineConfigId != "new",
    });
};

export const useAddEditRoutineConfig = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (routine: RoutineConfig) => {
            // Transform exercises to send only exercise IDs
            const { id, ...rest } = routine; // Remove `id`
            const transformedData = {
                ...rest,
                exercises: routine.exercises.map((exercise) => ({
                    reps: exercise.reps,
                    exercise_id: exercise.exercise.id, // Extract only the ID
                })),
            };
            if (id === "TEMP" || id === "new") {
                // Create new routine
                return request.post(`/routine-configs/`, transformedData);
            } else {
                // Update existing routine
                return request.put(`/routine-configs/${routine.id}/`, transformedData);
            }
        },
        onSuccess: (_, routine) => {
            queryClient.invalidateQueries({ queryKey: [QueryKeys.ROUTINE_CONFIGS] });

            if (routine.id !== "TEMP" && routine.id !== "new") {
                queryClient.invalidateQueries({ queryKey: [QueryKeys.ROUTINE_CONFIG, routine.id] });
            }
        },
    });
};

export const useDeleteRoutineConfig = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: string) => request.delete(`/routine-configs/${id}/`),
        onSuccess: (_, id) => {
            queryClient.invalidateQueries({ queryKey: [QueryKeys.ROUTINE_CONFIGS] });
            queryClient.removeQueries({ queryKey: [QueryKeys.ROUTINE_CONFIG, id] });
        },
    });
};

// TODO: Get patients into the server.
export const useGetPatients = (reportId: string) => {
    return useQuery({
        queryKey: [QueryKeys.PATIENTS, reportId],
        queryFn: () =>
            request.get(`/patients`) as Promise<Patient[]>,
        staleTime: 10000,
        enabled: !!reportId,
    });
};