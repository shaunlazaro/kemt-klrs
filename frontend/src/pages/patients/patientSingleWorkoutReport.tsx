import { useEffect, useState } from "react";
import { useGetPatientById, useGetRoutineDataById } from "../../api/hooks";
import Loader from "../../components/loader/loader";
import { BLANK_PATIENT, Patient } from "../../interfaces/patient.interface";
import { getAverageExtension, getAverageFlexion, getAverageScoreOfComponentData, getPatientName, getRepScore, getRoutineConfigRepsByComponentData } from "../../common/utils";
import { useParams } from "react-router-dom";
import { RepData, RoutineComponentData, RoutineData } from "../../interfaces/routineData.interface";
import { FaSearch } from "react-icons/fa";
import PatientSingleRepPanel from "./patientSingleRepPanel";

const PatientSingleWorkoutReport: React.FC = () => {

    const { id: patientId, reportId: reportId } = useParams();
    // const navigate = useNavigate();

    // const routineDataReturn = useTestRoutineData(); // TODO: Fetch report with id = reportID
    // const uploadRoutineData = useAddRoutineData();

    const { data: routineDataReturn } = useGetRoutineDataById(reportId ?? "");
    const { data: patientData, isLoading: patientLoading } = useGetPatientById(patientId ?? "");

    const [patient, setPatient] = useState<Patient>(BLANK_PATIENT);
    const [routineData, setRoutineData] = useState<RoutineData | undefined>();

    const [selectedRep, setSelectedRep] = useState<RepData | undefined>();
    const [selectedRepComponent, setSelectedRepComponent] = useState<RoutineComponentData | undefined>();

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
                setRoutineData(routineDataReturn);
            }
        },
        [routineDataReturn]
    )

    // Used to upload a routineData.  Kept as an example.
    // const testFunction = () => {
    //     if (!routineDataReturn)
    //         return;

    //     uploadRoutineData.mutate(routineDataReturn, {
    //         onSuccess: () => navigate(HOME_PATH),
    //     });
    // }

    const onClickRep = (repData: RepData, repComponent: RoutineComponentData) => {
        setSelectedRep(repData);
        setSelectedRepComponent(repComponent);
    }

    return (
        <>
            {patientLoading && <Loader />}
            <div className="h-auto bg-white pb-20 px-10">
                <div className="text-xl font-base text-black pb-2">
                    {getPatientName(patient)}
                </div>
                <div className="text-2xl font-semibold text-primary-darkblue pb-6">
                    Workout on {(new Date(routineData?.created_at ?? "")).toLocaleDateString("en-US", { weekday: "short", month: "short", day: "numeric", year: "numeric" })}
                </div>
                <div className="text-black font-base flex flex-row w-full gap-x-2 pb-6">
                    <div className="rounded-full bg-secondary-darkpink p-1 w-6 h-6">
                        <img src="/icon-book-open.png" />
                    </div>
                    <div>
                        <span className="font-bold">Exercise Plan: </span> {routineData?.routine_config?.name ?? "Not Found"}
                    </div>
                </div>
                <div className="grid grid-cols-2 gap-x-4">
                    <div>
                        {routineData?.routine_component_data?.map((componentData) => (
                            <div className="pb-4">
                                <div className="font-semibold text-primary-darkblue text-xl pb-2 pt-2"> {componentData?.exercise_detail?.display_name} </div>
                                <div className="grid grid-cols-3 gap-x-2 text-center pb-4">
                                    <div className="shadow-[0px_4px_4px_rgba(0,0,0,0.25)] p-4 rounded-lg">
                                        <div className="font-bold">
                                            {componentData?.rep_data?.length ?? 0} / {getRoutineConfigRepsByComponentData(componentData, routineData)}</div>
                                        <div className="font-base">reps</div>
                                    </div>
                                    <div className="shadow-[0px_4px_4px_rgba(0,0,0,0.25)] p-4 rounded-lg">
                                        <div className="font-bold">{getAverageScoreOfComponentData(componentData, routineData).toFixed(0)}</div>
                                        <div className="font-base">avg. score</div>
                                    </div>
                                    <div className="shadow-[0px_4px_4px_rgba(0,0,0,0.25)] p-4 rounded-lg">
                                        <div className="font-bold">{getAverageFlexion(componentData)?.toFixed(0)}° / {getAverageExtension(componentData)?.toFixed(0)}°</div>
                                        <div className="font-base">flexion / extension</div>
                                    </div>
                                </div>
                                <div className="font-base text-primary-darkblue pb-2 text-lg"> Individual Reps </div>
                                <div className="flex flex-row gap-x-4">
                                    {componentData?.rep_data.map((repData) => (
                                        <div
                                            className={`rounded-full p-2 font-semibold text-white text-base cursor-pointer w-10 h-10 text-center ${repData == selectedRep ? "bg-primary-darkblue" : "bg-secondary-darkpink"}`}
                                            onClick={() => { onClickRep(repData, componentData) }}
                                        >
                                            {getRepScore(repData, componentData.exercise_detail) * 100}
                                        </div>
                                    ))}
                                </div>
                            </div>
                        ))}
                    </div>
                    <div className="p-4 rounded-lg flex flex-col">
                        <div className="shadow-[0px_4px_4px_rgba(0,0,0,0.25)] rounded-lg m-4">
                            {selectedRep == undefined || selectedRepComponent == undefined ?
                                <div className="h-auto w-full text-center flex flex-col justify-center items-center px-4 py-32 gap-y-4 text-lg">
                                    <div><FaSearch className="h-10 w-auto" /> </div>
                                    <div> Click on an individual rep to view details </div>
                                </div>
                                : <PatientSingleRepPanel repData={selectedRep} componentData={selectedRepComponent} />}
                        </div>
                    </div>
                </div>
                {/* <button onClick={testFunction}>test</button> */}
            </div>
        </>
    );
};

export default PatientSingleWorkoutReport;
