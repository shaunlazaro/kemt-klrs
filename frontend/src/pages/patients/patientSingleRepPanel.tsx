import { getRepScore } from "../../common/utils";
import { RepData, RoutineComponentData } from "../../interfaces/routineData.interface";
import PoseVideo from "./poseVideo";

interface PatientSingleRepPanelProps {
    repData: RepData,
    componentData: RoutineComponentData,
}

const PatientSingleRepPanel: React.FC<PatientSingleRepPanelProps> = ({ repData, componentData }: PatientSingleRepPanelProps) => {
    return (<div>
        <div className="px-8 pt-2 pb-6">
            <div className="w-full flex justify-center py-8">
                <PoseVideo posesRaw={repData.poses} />
            </div>
            <div className="font-semibold text-xl text-primary-darkblue pb-4">
                {componentData.exercise_detail.display_name} - Rep {repData.rep_number}
            </div>
            <div className="px-6">
                <div className="justify-between grid grid-cols-2 pb-4">
                    <div className="flex gap-x-6">
                        <div className="font-bold">ROM</div>
                        <div>{repData.max_flexion.toFixed(0)}°/{repData.max_extension.toFixed(0)}°</div>
                    </div>
                    <div className="flex gap-x-6">
                        <div className="font-bold">Score</div>
                        <div>{getRepScore(repData, componentData.exercise_detail) * 100}</div>
                    </div>
                </div>
                <div className="font-bold pb-1">Errors</div>
                <ul className="list-disc pl-5">
                    {repData.alerts.map((alert) => (
                        <li key={alert} className="flex flex-col">
                            <span className="list-item">{alert}</span>
                        </li>
                    ))}
                    {repData.alerts.length == 0 && <li key={"noneKey"}>None</li>}
                </ul>
            </div>
        </div>
    </div>);
}

export default PatientSingleRepPanel