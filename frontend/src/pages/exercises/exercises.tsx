import Button from "../../components/button";
import Searchbar from "../../components/searchbar";
import Select from "../../components/select/select";
import { MdAddCircleOutline } from "react-icons/md";
import { ExercisePlanListMock } from "../../testData/exercisePlans";
import { RoutineConfig } from "../../interfaces/exercisePlan.interface";
import { useEffect, useState } from "react";
import { LiaEditSolid } from "react-icons/lia";
import { injuryValueList } from "../../common/utils";
import { useNavigate } from "react-router-dom";
import { ADDEDIT_EXERCISES_PATH, ADDEDIT_EXERCISES_PATH_NEW } from "../../routes";

const Exercises: React.FC = () => {
  const navigate = useNavigate();
  const baseExercisePlanList = ExercisePlanListMock
  const [ExercisePlanList, setExercisePlanList] = useState<RoutineConfig[]>(baseExercisePlanList);
  const [searchString, setSearchString] = useState<string>("");
  const [selectedFilter, setSelectedFilter] = useState<string>("");

  const applyListFilters = () => {
    setExercisePlanList(baseExercisePlanList.filter((routineConfig) => routineConfig.name.includes(searchString)
      && (selectedFilter == "" || routineConfig.injury == selectedFilter)));
  }

  useEffect(
    () => { applyListFilters(); },
    [searchString, selectedFilter]
  )

  const onSearchChange = (searchVal: string) => {
    setSearchString(searchVal);
  }

  const onFilterChange = (filterVal: string) => {
    console.log("test: ", filterVal)
    setSelectedFilter(filterVal)
  }

  return (
    <div className="h-auto bg-white pb-20 px-8">
      <div className="text-3xl font-bold text-neutral-800 pb-4">
        Exercise Plans
      </div>
      <div className="w-full flex justify-between pb-4">
        <div className="flex space-x-4">
          <div>
            <span className="text-primary-darkblue text-sm font-semibold">Search</span>
            <Searchbar placeholder="Search by name" onChange={(e) => onSearchChange(e.target.value)} />
          </div>
          <div>
            <span className="text-primary-darkblue text-sm font-semibold">Filter by Injury</span><br />
            <Select items={injuryValueList} placeholderString="All" onChange={(e) => onFilterChange(e.target.value)} />
          </div>
        </div>
        <Button variant="primary" className="h-auto mt-4 mb-1" onClick={() => navigate(ADDEDIT_EXERCISES_PATH_NEW)}>
          <MdAddCircleOutline className="h-auto w-6" />
          <span className="font-semibold">New Exercise Plan</span>
        </Button>
      </div>
      <div>
        <span className="text-primary-darkblue text-sm font-semibold ml-1">{ExercisePlanList.length} results <br /></span>
        <div className="grid-cols-3 grid gap-x-8 pt-2 gap-y-8">
          {
            ExercisePlanList.map((routineConfig) => (<>
              <div className="border border-neutral-200 rounded-lg px-4 pt-3 pb-6 col-span-1">
                <div className="w-full h-auto flex justify-between">
                  <div className="space-y-0">
                    <span className="font-semibold text-black text-xl">{routineConfig.name}</span><br />
                    <span className="font-semibold text-primary-darkblue text-sm">{routineConfig.injury}</span>
                  </div>
                  <div
                    className="rounded-2xl bg-secondary-lightpink text-white h-8 w-8 flex justify-center pb-1.5 pl-1.5 pr-1 pt-1 hover:cursor-pointer"
                    onClick={() => navigate(ADDEDIT_EXERCISES_PATH.replace(":id", routineConfig.id))}
                  >
                    <LiaEditSolid className="w-6 h-auto" />
                  </div>
                </div>
                <div className="pt-1 leading-0 px-2">
                  {(routineConfig as RoutineConfig).exercises.map((exercise) => (<>
                    <span className="text-sm text-black font-normal">{exercise.reps} x {exercise.exercise.display_name}</span><br />
                  </>))}
                </div>
              </div>
            </>))
          }
        </div>
      </div>
    </div>
  );
};

export default Exercises;
