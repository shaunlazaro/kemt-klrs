import { useEffect, useState } from "react";
import { BLANK_PATIENT, NEW_PATIENT_ID, Patient } from "../../interfaces/patient.interface";
import { PATIENTS_PATH } from "../../routes";
import { useNavigate, useParams } from "react-router-dom";
import { Input } from "../../components/input/input";
import Button from "../../components/button";
import { RoutineConfig } from "../../interfaces/exercisePlan.interface";
import DatePicker from "../../components/datepicker/datepicker";
import { WideSelect } from "../../components/select/select";
import { defaultInjuryValueList, getUniqueInjuryValues } from "../../common/utils";
import { RoutineConfigList } from "../../testData/exercisePlans";
import { useAddEditPatient, useGetPatientById, useGetRoutineConfigs } from "../../api/hooks";

const AddEditPatients: React.FC = () => {

    const { id: patientId } = useParams();
    const navigate = useNavigate();

    const { data: patientData } = useGetPatientById(patientId ?? "")
    const mutatePatient = useAddEditPatient();

    const { data: allExercisePlanData } = useGetRoutineConfigs(); // TODO: Dedicate an endpoint for these so we don't need to grab every routineConfig just to list injuries...

    const [injuryValueList, setInjuryValueList] = useState<string[]>(defaultInjuryValueList);
    const [routineConfigs, setRoutineConfigs] = useState<RoutineConfig[]>(RoutineConfigList);

    const [patient, setPatient] = useState<Patient>(BLANK_PATIENT);
    const [patientEmail, setPatientEmail] = useState<string>(patient.email);
    const [patientFirstName, setPatientFirstName] = useState<string>(patient.first_name);
    const [patientLastName, setPatientLastName] = useState<string>(patient.last_name);
    const [patientDOB, setPatientDOB] = useState<Date>(patient.date_of_birth);
    const [patientInjury, setPatientInjury] = useState<string>(patient.condition);
    const [patientExercise, setPatientExercise] = useState<RoutineConfig | undefined>(patient.exercises);
    const SEX_OPTIONS = [
        { value: "M", label: "Male" },
        { value: "F", label: "Female" },
        { value: "O", label: "Other / Prefer not to say" },
    ];
    const [patientSex, setPatientSex] = useState<string>(patient.sex);


    useEffect(
        () => {
            if (patientData) {
                setPatient(patientData);
                setPatientEmail(patientData.email);
                setPatientFirstName(patientData.first_name);
                setPatientLastName(patientData.last_name);
                setPatientDOB(new Date(patientData.date_of_birth))
                setPatientInjury(patientData.condition);
                setPatientExercise(patientData.exercises);
                setPatientSex(patientData.sex)
            }
        },
        [patientData]
    )

    useEffect(
        () => {
            const newPatient: Patient = {
                id: patientId ?? "NEW",
                // user_id: patient.user_id,
                // weeklyProgress: patient.weeklyProgress,
                // -------
                email: patientEmail,
                first_name: patientFirstName,
                last_name: patientLastName,
                date_of_birth: patientDOB,
                condition: patientInjury,
                exercises: patientExercise,
                sex: patientSex,
            }
            setPatient(newPatient);
        },
        [patientEmail, patientFirstName, patientLastName, patientDOB, patientInjury, patientExercise, patientSex]
    )

    useEffect(() => {
        if (allExercisePlanData && allExercisePlanData.length > 0) {
            setRoutineConfigs(allExercisePlanData);
            const uniqueVals = getUniqueInjuryValues(allExercisePlanData);
            setInjuryValueList(uniqueVals);
            if (!uniqueVals.includes(patientInjury)) {
                setPatientInjury(uniqueVals[0]);
            }
        }
    }, [allExercisePlanData]);

    const onClickCancel = () => navigate(PATIENTS_PATH);

    const onClickSubmit = () => {
        mutatePatient.mutate(patient, {
            onSuccess: () => navigate(PATIENTS_PATH),
        });
    };

    return (<>
        <div className="h-auto bg-white pb-20 px-8">
            <div className="text-3xl font-bold text-neutral-800 pb-4">
                {patientId == NEW_PATIENT_ID ? "Add New Patient" : "Edit Patient Info"}
            </div>
            <div className="grid grid-cols-2">
                <div className="col-span-1">
                    <div className="pb-4">
                        <div className="font-base text-sm text-primary-gray pb-1"> Email </div>
                        <Input className="border-2 border-primary-gray" placeholder="Email" value={patientEmail} onChange={(e) => setPatientEmail(e.target.value)} />
                    </div>
                    <div className="pb-4">
                        <div className="font-base text-sm text-primary-gray pb-1"> First Name </div>
                        <Input className="border-2 border-primary-gray" placeholder="First name" value={patientFirstName} onChange={(e) => setPatientFirstName(e.target.value)} />
                    </div>
                    <div className="pb-4">
                        <div className="font-base text-sm text-primary-gray pb-1"> Last Name </div>
                        <Input className="border-2 border-primary-gray" placeholder="Last name" value={patientLastName} onChange={(e) => setPatientLastName(e.target.value)} />
                    </div>
                    <div className="pb-4">
                        <div className="font-base text-sm text-primary-gray pb-1"> Date of Birth </div>
                        {/* <Input className="border-2 border-primary-gray" placeholder="DOB TODO" /> */}
                        <DatePicker date={patientDOB} onDateChange={(date: Date) => { setPatientDOB(date ?? new Date()); }} />
                    </div>
                    <div className="pb-4">
                        <div className="font-base text-sm text-primary-gray pb-1"> Sex </div>
                        <WideSelect
                            items={SEX_OPTIONS}
                            valueKey="value"
                            label="label"
                            value={patient.sex}
                            onChange={(e) => setPatientSex(e.target.value)}
                        />
                    </div>
                    <div className="pb-4">
                        <div className="font-base text-sm text-primary-gray pb-1"> Condition </div>
                        <WideSelect items={injuryValueList} value={patientInjury} onChange={(e) => setPatientInjury(e.target.value)} />
                        {/* <Input className="border-2 border-primary-gray" placeholder="CON TODO" /> */}
                    </div>
                    <div className="pb-4">
                        <div className="font-base text-sm text-primary-gray pb-1"> Exercise </div>
                        <WideSelect
                            // items={routineConfigs}
                            items={[{ name: "", label: "Select an exercise plan" }, ...routineConfigs]} // Add blank option at the start
                            valueKey="name"
                            label="name"
                            value={patient.exercises?.name ?? ""}
                            onChange={(e) => {
                                setPatientExercise(routineConfigs.find((ex) => ex.name == e.target.value) ?? undefined)
                            }}
                        />
                    </div>
                </div>
            </div>

            <div className="pb-4 pt-5">
                <span className="text-base text-black font-semibold"> Ensure the patient's email address is correct before saving. They will receieve an invite to join RePose. </span>
            </div>

            <div className="flex pt-12 gap-x-4 text-sm font-semibold">
                <Button variant="primary-outline" className="border-2 font-bold w-[150px]" onClick={() => onClickCancel()}> Cancel </Button>
                <Button variant="primary" className="w-[150px]" onClick={() => onClickSubmit()}> Save</Button>
            </div>
        </div>
    </>)
}

export default AddEditPatients