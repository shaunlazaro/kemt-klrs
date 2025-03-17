import { useEffect, useState } from "react";
import { useAddRoutineData, useGetPatientById } from "../../api/hooks";
import Loader from "../../components/loader/loader";
import { BLANK_PATIENT, Patient } from "../../interfaces/patient.interface";
import { getAverageScore, getPatientAge, getPatientName, getPercentCompleted, getTotalAlerts, getUniqueExercisesWithAlerts } from "../../common/utils";
import { useNavigate, useParams } from "react-router-dom";
import { useTestRoutineData } from "../../testData/routineData";
import { RoutineData } from "../../interfaces/routineData.interface";
import { GiProgression } from "react-icons/gi";
import { TbAlertCircle } from "react-icons/tb";
import { FaHistory } from "react-icons/fa";
import { FaChevronRight } from "react-icons/fa";
import { MonthlyCharts, WeeklyCharts } from "./patientCharts";
import Button from "../../components/button";
import { HOME_PATH, PATIENT_REPORT_PATH } from "../../routes";

const PatientSingleWorkoutReport: React.FC = () => {

    const { id: patientId, reportId: reportId } = useParams();
    const navigate = useNavigate();

    const routineDataReturn = useTestRoutineData(); //Gets the reportId
    const uploadRoutineData = useAddRoutineData();

    const { data: patientData, isLoading: patientLoading } = useGetPatientById(patientId ?? "");

    const [patient, setPatient] = useState<Patient>(BLANK_PATIENT);
    const [routineData, setRoutineData] = useState<RoutineData | undefined>();

    useEffect(
        () => {
            if (patientData)
                setPatient(patientData);
        },
        [patientData]
    )

    useEffect(
        () => {
            if (routineDataReturn) {
                // routineDataReturn.created_at = new Date().toISOString().split("T")[0]
                setRoutineData(routineDataReturn);
            }
            console.log(routineDataReturn)
        },
        [routineDataReturn]
    )

    const testFunction = () => {
        if (!routineDataReturn)
            return;

        uploadRoutineData.mutate(routineDataReturn, {
            onSuccess: () => navigate(HOME_PATH),
        });
    }

    return (
        <>
            {patientLoading && <Loader />}
            <div className="h-auto bg-white pb-20 px-8">
                <div className="text-2xl font-semibold text-primary-darkblue pb-6">
                    {getPatientName(patient)}
                </div>
                <Button variant='primary' onClick={() => { testFunction(); }} />
            </div>
        </>
    );
};

export default PatientSingleWorkoutReport;
