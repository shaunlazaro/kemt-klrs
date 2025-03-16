import { useEffect, useState } from "react";
import { MockPatients } from "../../testData/patient";
import { Patient } from "../../interfaces/patient.interface";
import { PATIENTS_PATH } from "../../routes";
import { useNavigate, useParams } from "react-router-dom";
import { Input } from "../../components/input/input";
import Button from "../../components/button";
import { RoutineConfig } from "../../interfaces/exercisePlan.interface";
import DatePicker from "../../components/datepicker/datepicker";
import { WideSelect } from "../../components/select/select";
import { defaultInjuryValueList } from "../../common/utils";
import { RoutineConfigList } from "../../testData/exercisePlans";

const NEW_PATIENT_ID = "new"
const patientData = MockPatients
const BLANK_PATIENT: Patient = {
    userId: "NA",
    firstName: "",
    lastName: "",
    email: "",
    dateOfBirth: new Date(),
    sex: "",
    condition: "",
    exercises: undefined,
    weeklyProgress: 0
}

const AddEditPatients: React.FC = () => {

    const { id: patientId } = useParams();
    const navigate = useNavigate();

    const noPatientFound = () => {
        alert("No patient found.");
        navigate(PATIENTS_PATH);
        return BLANK_PATIENT // TODO: this doesn't work...
    }
    const getPatientData = (id: string): Patient => {
        return id == NEW_PATIENT_ID ? BLANK_PATIENT : patientData.find((patient) => patient.userId == id) ?? noPatientFound()
    }

    const [patient, setPatient] = useState<Patient>(getPatientData(patientId ?? "N/A"));
    const [patientEmail, setPatientEmail] = useState<string>(patient.email);
    const [patientFirstName, setPatientFirstName] = useState<string>(patient.firstName);
    const [patientLastName, setPatientLastName] = useState<string>(patient.lastName);
    const [patientDOB, setPatientDOB] = useState<Date>(patient.dateOfBirth);
    const [patientInjury, setPatientInjury] = useState<string>(patient.condition);
    const [patientExercise, setPatientExercise] = useState<RoutineConfig | undefined>(patient.exercises);

    useEffect(
        () => {
            const newPatient: Patient = {
                userId: patient.userId,
                sex: patient.sex,
                weeklyProgress: patient.weeklyProgress,
                // -------
                email: patientEmail,
                firstName: patientFirstName,
                lastName: patientLastName,
                dateOfBirth: patientDOB,
                condition: patientInjury,
                exercises: patientExercise,
            }
            setPatient(newPatient);
        },
        [patientEmail, patientFirstName, patientLastName, patientDOB, patientInjury, patientExercise]
    )

    const onClickCancel = () => {

    }
    const onClickSubmit = () => {

    }

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
                        <div className="font-base text-sm text-primary-gray pb-1"> Condition </div>
                        <WideSelect items={defaultInjuryValueList} value={patientInjury ?? defaultInjuryValueList[0]} onChange={(e) => setPatientInjury(e.target.value)} />
                        {/* <Input className="border-2 border-primary-gray" placeholder="CON TODO" /> */}
                    </div>
                    <div className="pb-4">
                        <div className="font-base text-sm text-primary-gray pb-1"> Exercise </div>
                        <WideSelect
                            items={RoutineConfigList}
                            valueKey="name"
                            label="name"
                            value={patient.exercises?.name ?? ""}
                            onChange={(e) => {
                                setPatientExercise(RoutineConfigList.find((ex) => ex.name == e.target.value) ?? RoutineConfigList[0])
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