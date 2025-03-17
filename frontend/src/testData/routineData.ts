import { RoutineData } from "../interfaces/routineData.interface";
import { useState, useEffect } from "react";

export const useTestRoutineData = () => {
    const [routineData, setRoutineData] = useState<RoutineData | null>(null);

    useEffect(() => {
        fetch("/routine_data.json") // Automatically resolves from `public/`
            .then((res) => res.json())
            .then(setRoutineData)
            .catch((error) => console.error("Error loading JSON:", error));
    }, []);

    return routineData;
};