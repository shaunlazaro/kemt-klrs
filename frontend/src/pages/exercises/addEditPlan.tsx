import { useEffect, useState } from "react";
import Button from "../../components/button"
import { WideSelect } from "../../components/select/select"
import { Input } from "../../components/input/input";
import { ExercisePlanListMock, TestRoutineConfig2 } from "../../testData/exercisePlans";
import { RoutineComponent, RoutineConfig } from "../../interfaces/exercisePlan.interface";
import { useNavigate, useParams } from "react-router-dom";
import { CiCircleInfo } from "react-icons/ci";
import { IoIosCloseCircleOutline } from "react-icons/io";
import { MdDragHandle } from "react-icons/md";
import { ExerciseList } from "../../testData/exerciseDetail";
import { IoMdAddCircleOutline } from "react-icons/io";
import { ExerciseDetail } from "../../interfaces/exerciseDetail.interface";
import { EXERCISES_PATH } from "../../routes";
import { injuryValueList } from "../../common/utils";

const NEW_PLAN_ID = "new"
const TEST_PLAN = TestRoutineConfig2;

const BLANK_PLAN: RoutineConfig = {
    name: "",
    injury: "Knee",
    exercises: []
}
const BLANK_EXERCISE: RoutineComponent = {
    reps: 0,
    exercise: ExerciseList[0]
}


const AddEditPlan: React.FC = () => {

    const { id: planId } = useParams();
    const navigate = useNavigate();


    const getPlanData = (id: string): RoutineConfig => {
        return id == NEW_PLAN_ID ? BLANK_PLAN : TEST_PLAN; // TODO Back end integration
    }

    const [plan, setPlan] = useState<RoutineConfig>(getPlanData(planId ?? NEW_PLAN_ID));

    console.log(plan);

    const [planName, setPlanName] = useState<string>(plan.name);
    const [planInjury, setPlanInjury] = useState<string>(plan.injury);
    const [planComponents, setPlanComponents] = useState<RoutineComponent[]>(plan.exercises);

    useEffect(
        () => {
            const newPlan: RoutineConfig = {
                name: planName,
                injury: planInjury,
                exercises: planComponents,
            }
            setPlan(newPlan);
        },
        [planName, planInjury]
    )

    const onClickInfo = (exercise: ExerciseDetail) => {
        alert("Unimplemented: Show Display for " + exercise.display_name);
    }
    const onClickDelete = (index: number) => {
        if (confirm("Remove this exercise from the routine?")) {
            const newPlanComponents = [...planComponents]
            newPlanComponents.splice(index, 1)
            setPlanComponents(newPlanComponents)
        }
    }
    const onClickAddExercise = () => {
        const newPlanComponents = [...planComponents]
        newPlanComponents.push(BLANK_EXERCISE)
        setPlanComponents(newPlanComponents)
    }
    const onClickCancel = () => {
        navigate(EXERCISES_PATH);
    }
    const onClickSubmit = () => {
        alert("TODO: Save changes...");
        navigate(EXERCISES_PATH);
    }


    return (
        <div className="h-auto bg-white pb-20 px-8">
            <div className="text-3xl font-bold text-neutral-800 pb-4">
                {planId == NEW_PLAN_ID ? "Create New " : "Edit "}Exercise Plan
            </div>
            <div className="grid grid-cols-6 gap-x-16">
                <div className="col-span-4">
                    <div>
                        <span className="text-primary-darkblue text-sm font-semibold pb-1">Name</span>
                        <Input value={planName} onChange={(e) => setPlanName(e.target.value)} />
                    </div>
                    <div className="pt-4">
                        <span className="text-primary-darkblue text-sm font-semibold pb-1">Injury / Condition</span><br />
                        <WideSelect items={injuryValueList} value={plan?.injury ?? injuryValueList[0]} onChange={(e) => setPlanInjury(e.target.value)} />
                    </div>
                    <div className="w-full h-[1px] bg-neutral-800 my-8" />

                    <span className="font-semibold text-xl text-black">Add individual exercises</span>

                    <div className="grid grid-cols-11 h-auto w-full pt-4 text-center gap-y-2 items-center pb-8">
                        <div className="col-start-2 col-span-1 w-full text-left font-semibold text-primary-blue text-sm">Count</div>
                        <div className="col-start-4 col-span-1 w-full text-left font-semibold text-primary-blue text-sm">Exercise</div>
                        {/* Row */}
                        {planComponents.map((planComponent, index) => (
                            <>
                                <div className="col-start-1 w-full text-center flex justify-center"> <MdDragHandle className="w-7 h-auto" /> </div>
                                <div className="col-start-2 w-full h-full">
                                    <Input
                                        // type="number"
                                        type="text"
                                        pattern="\d*"
                                        inputMode="numeric"
                                        value={planComponent.reps}
                                        onChange={(e) => {
                                            const newPlanComponents = [...planComponents];
                                            newPlanComponents[index] = { ...newPlanComponents[index], reps: Number.isNaN(parseInt(e.target.value)) ? 0 : parseInt(e.target.value) }
                                            setPlanComponents(newPlanComponents);
                                        }}
                                    />
                                </div>
                                <div className="col-start-3 w-full text-sm pr-2 font-semibold"> reps </div>
                                <div className="col-start-4 col-span-6 w-full">
                                    <WideSelect
                                        items={ExerciseList}
                                        valueKey="display_name"
                                        label="display_name"
                                        value={planComponent.exercise.display_name}
                                        onChange={(e) => {
                                            const newPlanComponents = [...planComponents];
                                            const newExercise = ExerciseList.find((ex) => ex.display_name == e.target.value) ?? ExerciseList[0]
                                            newPlanComponents[index] = { ...newPlanComponents[index], exercise: newExercise }
                                            setPlanComponents(newPlanComponents);
                                        }}
                                    />
                                </div>
                                <div className="col-start-10 flex justify-center ml-3 hover:cursor-pointer"> <CiCircleInfo className="w-7 h-auto" onClick={() => onClickInfo(planComponent.exercise)} /> </div>
                                <div className="col-start-11 flex justify-center mr-3 hover:cursor-pointer"> <IoIosCloseCircleOutline className="w-7 h-auto" onClick={() => onClickDelete(index)} /> </div>
                            </>
                        ))}
                        {/* ROW TEMPLATE: <div className="col-start-1 w-full text-center flex justify-center"> <MdDragHandle className="w-7 h-auto" /> </div>
                        <div className="col-start-2 w-full h-full"> <Input /> </div>
                        <div className="col-start-3 w-full text-sm pr-2 font-semibold"> reps </div>
                        <div className="col-start-4 col-span-6 w-full"> <WideSelect items={ExerciseList} valueKey="display_name" label="display_name" /> </div>
                        <div className="col-start-10 flex justify-center ml-3"> <CiCircleInfo className="w-7 h-auto" /> </div>
                        <div className="col-start-11 flex justify-center mr-3"> <IoIosCloseCircleOutline className="w-7 h-auto" /> </div> */}
                    </div>

                    <div className="flex rounded-4xl bg-primary-blue pt-1.5 pb-2 px-3 w-fit text-white text-sm font-semibold gap-x-1 hover:cursor-pointer" onClick={() => onClickAddExercise()}>
                        <IoMdAddCircleOutline className="w-5 h-auto" /> Add exercise
                    </div>

                    <div className="flex pt-12 gap-x-4 text-sm font-semibold">
                        <Button variant="primary-outline" className="border-2 font-bold w-[150px]" onClick={() => onClickCancel()}> Cancel </Button>
                        <Button variant="primary" className="w-[150px]" onClick={() => onClickSubmit()}> Save</Button>
                    </div>
                </div>
                <div className="col-span-2 rounded-lg bg-primary-lightblue h-auto w-full">
                </div>
            </div>
        </div>
    );
}

export default AddEditPlan