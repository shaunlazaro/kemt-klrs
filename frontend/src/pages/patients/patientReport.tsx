import { useEffect, useState } from "react";
import { useGetPatientById, useGetRoutineData } from "../../api/hooks";
import Loader from "../../components/loader/loader";
import { BLANK_PATIENT, Patient } from "../../interfaces/patient.interface";
import { getAverageScore, getPatientAge, getPatientName, getPercentCompleted, getTotalAlerts, getUniqueExercisesWithAlerts } from "../../common/utils";
import { useNavigate, useParams } from "react-router-dom";
import { RoutineData } from "../../interfaces/routineData.interface";
import { GiProgression } from "react-icons/gi";
import { TbAlertCircle } from "react-icons/tb";
import { FaHistory } from "react-icons/fa";
import { FaChevronRight } from "react-icons/fa";
import { MonthlyCharts, WeeklyCharts } from "./patientCharts";
import Button from "../../components/button";
import { PATIENT_REPORT_SINGLE_PATH } from "../../routes";

const PatientReport: React.FC = () => {

    const { id: patientId } = useParams();
    const navigate = useNavigate();

    // const routineDataReturn = useTestRoutineData();

    const { data: routineDataReturn } = useGetRoutineData();
    const { data: patientData, isLoading: patientLoading } = useGetPatientById(patientId ?? "");

    const [patient, setPatient] = useState<Patient>(BLANK_PATIENT);
    const [workoutHistory, setWorkoutHistory] = useState<RoutineData[]>([]);
    const [chartView, setChartView] = useState<'weekly' | 'monthly'>('weekly');

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
                setWorkoutHistory(routineDataReturn)
            }
            console.log(routineDataReturn)
        },
        [routineDataReturn]
    )

    const onClickReport = (workoutReport: RoutineData) => {
        const path = PATIENT_REPORT_SINGLE_PATH.replace(":id", patientId ?? "-1").replace(":reportId", workoutReport.id);
        navigate(path);
    }

    return (
        <>
            {patientLoading && <Loader />}
            <div className="h-auto bg-white pb-20 px-8">
                <div className="text-2xl font-semibold text-primary-darkblue pb-6">
                    {getPatientName(patient)}
                </div>
                <div className="grid grid-cols-10 gap-4 pb-4">
                    <div className="rounded-lg shadow-[0px_4px_4px_rgba(0,0,0,0.25)] col-span-3 flex p-5 gap-4">
                        <div className="w-16 h-16 bg-secondary-darkpink rounded-full flex items-center justify-center overflow-hidden">
                            <img src="/icon-injury.png" className="object-cover" />
                        </div>
                        <div className="h-full flex flex-col justify-center">
                            <span className="font-normal text-base">Injury</span>
                            <span className="font-semibold text-xl">{patient.condition}</span>
                        </div>
                    </div>
                    <div className="rounded-lg shadow-[0px_4px_4px_rgba(0,0,0,0.25)] col-span-4 flex p-5 gap-4">
                        <div className="w-16 h-16 bg-secondary-darkpink rounded-full flex items-center justify-center overflow-hidden">
                            <img src="/icon-book-open.png" className="object-cover" />
                        </div>
                        <div className="h-full flex flex-col justify-center">
                            <span className="font-normal text-base">Current Exercise Plan</span>
                            <span className="font-semibold text-xl">{patient.exercises?.name ?? "No plan assigned"}</span>
                        </div>
                    </div>
                    <div className="rounded-lg shadow-[0px_4px_4px_rgba(0,0,0,0.25)] col-span-3 flex p-5 gap-4">
                        <div className="w-16 h-16 bg-secondary-darkpink rounded-full flex items-center justify-center overflow-hidden">
                            <img src="/icon-injury.png" className="object-cover" />
                        </div>
                        <div className="h-full flex flex-col justify-center">
                            <span className="font-normal text-base">Age</span>
                            <span className="font-semibold text-xl">{getPatientAge(patient)}</span>
                        </div>
                    </div>
                </div>
                <div className="grid grid-cols-7 gap-4">
                    <div className="rounded-lg shadow-[0px_4px_4px_rgba(0,0,0,0.25)] col-span-3 flex flex-col p-5 gap-4 items-start">
                        <div className="flex items-center space-x-2 text-primary-darkblue font-semibold">
                            <GiProgression className="w-6 h-6" />
                            <span className="text-xl">Progress</span>
                        </div>

                        <div className="flex w-full gap-x-4">
                            <Button
                                variant={chartView === 'weekly' ? 'primary' : 'primary-outline'}
                                onClick={() => setChartView('weekly')}
                                className="flex-1"
                            >
                                Weekly
                            </Button>
                            <Button
                                variant={chartView === 'monthly' ? 'primary' : 'primary-outline'}
                                onClick={() => setChartView('monthly')}
                                className="flex-1"
                            >
                                Monthly
                            </Button>
                        </div>

                        {chartView === 'weekly' ? <WeeklyCharts routines={workoutHistory} /> : <MonthlyCharts routines={workoutHistory} />}
                    </div>
                    <div className="col-span-4 flex flex-col gap-4">
                        <div className="rounded-lg shadow-[0px_4px_4px_rgba(0,0,0,0.25)] col-span-4 flex flex-col p-5 gap-4 items-start">
                            <div className="flex items-center space-x-2 text-primary-darkblue font-semibold">
                                <TbAlertCircle className="w-6.5 h-6.5 font-bold" />
                                <span className="text-xl">Common Errors</span>
                            </div>
                            <div>
                                {getUniqueExercisesWithAlerts(workoutHistory).map((dataset) => (
                                    <div className="flex flex-col">
                                        <span className="font-bold">{dataset.exercise.display_name}</span>
                                        <ul className="list-disc pl-5">
                                            {dataset.alerts.map((kvp) => (
                                                <li key={kvp.message} className="flex flex-col">
                                                    <span className="list-item">{kvp.message}</span>
                                                    {kvp.count > 1 && <span className="italic pl-4">({kvp.count} times this week)</span>}
                                                </li>
                                            ))}
                                            {dataset.alerts.length == 0 && <li key={"noneKey"}>None</li>}
                                        </ul>
                                        <div className="pt-2" />
                                    </div>
                                ))}

                            </div>
                        </div>
                        <div className="rounded-lg shadow-[0px_4px_4px_rgba(0,0,0,0.25)] col-span-4 flex flex-col p-5 gap-4 items-start">
                            <div className="w-full flex justify-between">
                                <div className="flex items-center space-x-2 text-primary-darkblue font-semibold">
                                    <FaHistory className="w-6 h-6" />
                                    <span className="text-xl">Workout History</span>
                                </div>
                                <div>
                                    {/*TODO Make this direct somewhere  */}
                                    See All
                                </div>
                            </div>
                            <div className="w-full grid grid-cols-11 text-base gap-y-1 text-center items-center">
                                <div className="col-start-4 col-end-6 font-bold"># Errors</div>
                                <div className="col-start-6 col-end-9 font-bold">% Completed</div>
                                <div className="col-start-9 col-end-11 font-bold">Score</div>
                                {workoutHistory.map((routineData) => (<>
                                    <div className="col-start-1 col-end-4 text-left">
                                        {new Date(routineData.created_at).toLocaleDateString("en-US", { weekday: "short", month: "short", day: "numeric" })}
                                    </div>
                                    <div className="col-start-4 col-end-6">{getTotalAlerts(routineData)}</div>
                                    <div className="col-start-6 col-end-9">{getPercentCompleted(routineData)}</div>
                                    <div className="col-start-9 col-end-11">{getAverageScore(routineData).toFixed(0)}</div>
                                    <div className="col-start-12 cursor-pointer h-auto align-middle" onClick={() => onClickReport(routineData)}> <FaChevronRight /></div>
                                </>))}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default PatientReport;
