// import { useState } from "react";

import { Loader } from "lucide-react";
import { useGetDashboardData } from "../../api/hooks";
import { useEffect, useState } from "react";
import { LuUsers } from "react-icons/lu";
import { IoMdAddCircleOutline } from "react-icons/io";
import CircularProgress from "../../components/circularProgress/circularProgress";
import Button from "../../components/button";
import { useNavigate } from "react-router-dom";
import { ADDEDIT_EXERCISES_PATH_NEW, ADDEDIT_PATIENTS_PATH_NEW, PATIENTS_PATH } from "../../routes";
import { FaChevronRight } from "react-icons/fa";
import { MdOutlinePersonalInjury } from "react-icons/md";

const ADHERENCE_THRESHOLD = 70
const SCORE_THRESHOLD = 0.7

const Home: React.FC = () => {

  const navigate = useNavigate();

  const { data: dashboardData, isLoading: dashboardDataLoading } = useGetDashboardData()
  const [topInjuries, setTopInjuries] = useState<{ [injury: string]: number }[]>([])

  const [averageScore, setAverageScore] = useState(0);
  const [averageCompletion, setAverageCompletion] = useState(0);
  const [averageRating, setAverageRating] = useState(0);

  useEffect(
    () => {
      console.log(dashboardData)
      if (!dashboardData || dashboardData.length === 0) {
        setAverageScore(0);
        setAverageCompletion(0);
        setAverageRating(0);
        return;
      }

      const totalPatients = dashboardData.length;

      const totalScore = dashboardData.reduce((sum, patient) => sum + (patient.average_score ?? 0), 0);
      const totalCompletion = dashboardData.reduce((sum, patient) => sum + (patient.completion_percent ?? 0), 0);
      const totalRating = dashboardData.reduce((sum, patient) => sum + (patient.average_rating ?? 0), 0);

      setAverageScore(totalScore / totalPatients);
      setAverageCompletion(totalCompletion / totalPatients);
      setAverageRating(totalRating / totalPatients);

      const injuryCounts: Record<string, number> = {}

      dashboardData.forEach((p) => {
        if (p.injury) {
          injuryCounts[p.injury] = (injuryCounts[p.injury] || 0) + 1
        }
      })

      const sortedInjuries = Object.entries(injuryCounts)
        .sort((a, b) => b[1] - a[1]) // sort by count descending
        .slice(0, 5) // top 5
        .map(([injury, count]) => ({ [injury]: count }))

      setTopInjuries(sortedInjuries)
    },
    [dashboardData]
  )

  const onClickAddPatient = () => {
    navigate(ADDEDIT_PATIENTS_PATH_NEW);
  }

  const onClickAddExercisePlan = () => {
    navigate(ADDEDIT_EXERCISES_PATH_NEW);
  }

  const onClickPatient = () => {
    navigate(PATIENTS_PATH);
  }

  return (
    <>
      {(dashboardDataLoading) ?
        <Loader />
        :
        <div className="h-auto bg-white pb-20 px-8">
          <div className="text-2xl font-semibold text-primary-darkblue pb-6">
            Overview
          </div>
          <div className="grid grid-cols-3 gap-4 pb-8">
            <div className="rounded-lg shadow-[0px_4px_4px_rgba(0,0,0,0.25)] col-span-2 flex p-5 gap-4 flex-col">
              <div className="flex items-center space-x-2 text-primary-darkblue font-semibold">
                <LuUsers className="w-6.5 h-6.5 font-bold" />
                <span className="text-xl">At A Glance</span>
              </div>
              <div className="grid grid-cols-3 justify-between p-4">
                <div className="text-center flex flex-col items-center">
                  <CircularProgress fill={averageCompletion} text={`${averageCompletion.toFixed(0)}%`} />
                  <span>Patient Adherence</span>
                </div>
                <div className="text-center flex flex-col items-center">
                  <CircularProgress fill={averageRating / 5 * 100} text={`${averageRating.toFixed(1)}/5`} />
                  <span>Patient Satisfaction</span>
                </div>
                <div className="text-center flex flex-col items-center">
                  <CircularProgress fill={averageScore * 100} text={`${(averageScore * 100).toFixed(0)}`} />
                  <span>Average Score</span>
                </div>
              </div>
            </div>
            <div className="rounded-lg col-span-1 flex p-5 gap-4">
              <div className="flex flex-col gap-y-4 items-start space-x-2 text-primary-darkblue font-semibold">
                <div className="text-xl">Quick Actions</div>
                <Button
                  variant='primary'
                  className="gap-2"
                  onClick={() => { onClickAddPatient(); }}
                >
                  <IoMdAddCircleOutline /> Add Patient
                </Button>
                <Button
                  variant='primary'
                  className="gap-2"
                  onClick={() => { onClickAddExercisePlan(); }}
                >
                  <IoMdAddCircleOutline /> Add Exercise Plan
                </Button>
              </div>
            </div>
          </div>
          <div className="grid grid-cols-3 gap-4 pb-4 items-start">
            <div className="rounded-lg shadow-[0px_4px_4px_rgba(0,0,0,0.25)] col-span-2 flex p-5 gap-0 flex-col">
              <div className="flex items-center space-x-2 text-primary-darkblue font-semibold pb-1">
                <LuUsers className="w-6.5 h-6.5 font-bold" />
                <span className="text-xl">Patients of Concern</span>
              </div>
              <div className="italic">Patients with low score or adherence</div>
              <div className="grid grid-cols-10 p-4 gap-y-4 text-base">
                {/* Header Row */}
                <div className="col-span-3 text-left font-semibold"></div>
                <div className="col-span-2 text-center font-semibold">Injury</div>
                <div className="col-span-2 text-center font-semibold">Score</div>
                <div className="col-span-2 text-center font-semibold">Adherence</div>
                <div className="text-center font-semibold"></div>

                {/* Data Rows */}
                {dashboardData
                  ?.filter(p => p.average_score < SCORE_THRESHOLD || p.completion_percent < ADHERENCE_THRESHOLD) // Customize thresholds
                  .map((patient) => (
                    <>
                      <div className="col-span-3 text-left">{patient.patient}</div>
                      <div className="col-span-2 flex justify-center items-center">
                        <span className="bg-secondary-darkpink text-white text-xs font-medium px-3 py-1 rounded-full">
                          {patient.injury}
                        </span>
                      </div>
                      <div className={`col-span-2 text-center text-sm ${patient.average_score < SCORE_THRESHOLD ? "text-red font-semibold" : ""}`}>{(patient.average_score * 100).toFixed(0)}</div>
                      <div className={`col-span-2 text-center text-sm ${patient.completion_percent < ADHERENCE_THRESHOLD ? "text-red font-semibold" : ""}`}>{(patient.completion_percent).toFixed(0)}%</div>
                      <div className="text-center cursor-pointer items-center flex justify-center"><FaChevronRight onClick={() => onClickPatient()} /></div>
                    </>
                  ))}
              </div>
            </div>
            <div className="rounded-lg col-span-1 flex flex-col px-5 pb-4 py-0 gap-4">
              <div className="rounded-lg shadow-[0px_4px_4px_rgba(0,0,0,0.25)] col-span-3 flex p-5 gap-4 w-full h-auto">
                <div className="w-16 h-16 bg-secondary-darkpink rounded-full flex items-center justify-center overflow-hidden">
                  <img src="/icon-injury.png" className="object-cover" />
                </div>
                <div className="flex flex-col justify-between">
                  <div>Active Patients</div><div className="text-xl font-semibold">{dashboardData?.length ?? 0}</div>
                </div>
              </div>
              <div className="rounded-lg shadow-[0px_4px_4px_rgba(0,0,0,0.25)] col-span-3 flex flex-col p-5 gap-4 w-full h-auto">
                <div className="flex items-center space-x-2 text-primary-darkblue font-semibold pb-1">
                  <MdOutlinePersonalInjury className="w-6.5 h-6.5 font-bold" />
                  <span className="text-xl">Top Injuries</span>
                </div>
                <div className="flex flex-col justify-between space-y-2">
                  {topInjuries.map((injuryObj, idx) => {
                    const injury = Object.keys(injuryObj)[0]
                    const count = injuryObj[injury]

                    return (
                      <div key={idx} className="flex justify-between text-sm text-primary-darkblue">
                        <span>{injury}</span>
                        <span>{count}</span>
                      </div>
                    )
                  })}
                </div>
              </div>
            </div>
          </div>
        </div>
      }
    </>
  )
};

export default Home;
