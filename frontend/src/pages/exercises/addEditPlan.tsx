import { useEffect, useState } from "react";
import Button from "../../components/button"
import Searchbar from "../../components/searchbar"
import Select, { WideSelect } from "../../components/select/select"
import { Input } from "../../components/input/input";
import { ExercisePlanListMock } from "../../testData/exercisePlans";
import MultiSelect from "../../components/multiselect/multiselect";
import { RoutineConfig } from "../../interfaces/exercisePlan.interface";
import { useParams } from "react-router-dom";
import { CiCircleInfo } from "react-icons/ci";
import { IoIosCloseCircleOutline } from "react-icons/io";
import { MdDragHandle } from "react-icons/md";
import { ExerciseList } from "../../testData/exerciseDetail";
import { IoMdAddCircleOutline } from "react-icons/io";


const NEW_PLAN_ID = "new"

// Utility function, should probably be done by server, or create a "injury" table.
const getUniqueInjuryValues = (routineConfigs: RoutineConfig[]): string[] => {
    return [...new Set(routineConfigs.map((routineConfig) => routineConfig.injury))]
}
const filterList = getUniqueInjuryValues(ExercisePlanListMock);


const AddEditPlan: React.FC = () => {

    const { id: planId } = useParams();

    const [planName, setPlanName] = useState<string>("");
    const [planInjury, setPlanInjury] = useState<string>(filterList[0]);

    useEffect(
        () => {
            const newPlan: RoutineConfig = {
                name: planName,
                injury: planInjury,
                exercises: [],
            }
            setPlan(newPlan);
        },
        [planName, planInjury]
    )

    const [plan, setPlan] = useState<RoutineConfig>();

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
                        <WideSelect items={filterList} value={plan?.injury ?? filterList[0]} onChange={(e) => setPlanInjury(e.target.value)} />
                    </div>
                    <div className="w-full h-[1px] bg-neutral-800 my-8" />

                    <span className="font-semibold text-xl text-black">Add individual exercises</span>

                    <div className="grid grid-cols-11 h-auto w-full pt-4 text-center gap-y-2 items-center pb-8">
                        <div className="col-start-2 col-span-1 w-full text-left font-semibold text-primary-blue text-sm">Count</div>
                        <div className="col-start-4 col-span-1 w-full text-left font-semibold text-primary-blue text-sm">Exercise</div>
                        {/* Row */}
                        <div className="col-start-1 w-full text-center flex justify-center"> <MdDragHandle className="w-7 h-auto" /> </div>
                        <div className="col-start-2 w-full h-full"> <Input /> </div>
                        <div className="col-start-3 w-full text-sm pr-2 font-semibold"> reps </div>
                        <div className="col-start-4 col-span-6 w-full"> <WideSelect items={ExerciseList} valueKey="display_name" label="display_name" /> </div>
                        <div className="col-start-10 flex justify-center ml-3"> <CiCircleInfo className="w-7 h-auto" /> </div>
                        <div className="col-start-11 flex justify-center mr-3"> <IoIosCloseCircleOutline className="w-7 h-auto" /> </div>
                    </div>

                    <div className="flex rounded-4xl bg-primary-blue pt-1.5 pb-2 px-3 w-fit text-white text-sm font-semibold gap-x-1"> <IoMdAddCircleOutline className="w-5 h-auto" /> Add exercise </div>

                    <div className="flex pt-12 gap-x-4 text-sm font-semibold">
                        <Button variant="primary-outline" className="border-2 font-bold w-[150px]"> Cancel </Button>
                        <Button variant="primary" className="w-[150px]"> Save</Button>
                    </div>
                </div>
                <div className="col-span-2 rounded-lg bg-primary-lightblue h-auto w-full">
                </div>
            </div>
        </div>
    );
}

export default AddEditPlan