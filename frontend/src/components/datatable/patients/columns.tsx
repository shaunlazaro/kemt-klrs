import { Patient } from "../../../interfaces/patient.interface";
import { ColumnDef } from "@tanstack/react-table"
import { ArrowUpDown, ChevronDown, MoreHorizontal } from "lucide-react"
import Button from "../../button"
import {
    DropdownMenu,
    DropdownMenuCheckboxItem,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "../../dropdown/dropdown"
import { ADDEDIT_PATIENTS_PATH } from "../../../routes";
import { useNavigate } from "react-router-dom";
import { RoutineConfig } from "../../../interfaces/exercisePlan.interface";


export const PatientTableColumnDef: ColumnDef<Patient>[] = [
    {
        id: "name",
        accessorFn: (patient) => patient.name,
        header: () => (
            <div className="w-full cursor-default text-center">Name</div>
        ),
        cell: ({ cell }) => (
            <div
                className={`rounded-md py-1 text-center font-semibold text-sm`}
            >
                {`${cell.getValue()}`}
            </div>
        ),
    },
    {
        id: "age",
        accessorFn: (patient) => patient.age,
        header: () => (
            <div className="w-full cursor-default text-center">Age</div>
        ),
        cell: ({ cell }) => (
            <div
                className={`rounded-md py-1 text-center font-semibold text-sm`}
            >
                {`${cell.getValue()}`}
            </div>
        ),
    },
    {
        id: "sex",
        accessorFn: (patient) => patient.sex,
        header: () => (
            <div className="w-full cursor-default text-center">Sex</div>
        ),
        cell: ({ cell }) => (
            <div
                className={`rounded-md py-1 text-center font-semibold text-sm`}
            >
                {`${cell.getValue()}`}
            </div>
        ),
    },
    {
        id: "condition",
        accessorFn: (patient) => patient.condition,
        header: () => (
            <div className="w-full cursor-default text-center">Condition</div>
        ),
        cell: ({ cell }) => (
            <div
                className={`rounded-md py-1 text-center font-semibold text-sm flex justify-center`}
            >
                <div className="rounded-2xl border text-white bg-primary px-4 py-1">{`${cell.getValue()}`}</div>
            </div>
        ),
    },
    {
        id: "exercises",
        accessorFn: (patient) => patient.exercises,
        header: () => (
            <div className="w-full cursor-default text-center">Exercises</div>
        ),
        cell: ({ cell }) => (
            <div
                className={`rounded-md py-1 text-center font-semibold text-sm`}
            >
                {(cell.getValue() as RoutineConfig)?.name &&
                    <div className="rounded-2xl border text-white bg-primary px-4 py-1">
                        {(cell.getValue() as RoutineConfig)?.name}
                    </div>
                }
            </div>
        ),
    },
    {
        id: "weeklyProgress",
        accessorFn: (patient) => patient.weeklyProgress,
        header: () => (
            <div className="w-full cursor-default text-center">Weekly Progress</div>
        ),
        cell: ({ cell }) => (
            <div
                className={`rounded-md py-1 text-center font-semibold text-sm`}
            >
                {`${cell.getValue()}`}/7
            </div>
        ),
    },
    {
        id: "actions",
        enableHiding: false,
        cell: ({ row }) => {
            const patient = row.original
            const navigate = useNavigate();

            return (
                <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                        <Button variant="ghost" className="h-8 w-8 p-0">
                            <span className="sr-only">Open menu</span>
                            <MoreHorizontal />
                        </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" className="bg-neutral-300">
                        <DropdownMenuLabel>Actions</DropdownMenuLabel>
                        {/* <DropdownMenuItem
                    onClick={() => navigator.clipboard.writeText(payment.id)}
                    >
                    Copy payment ID
                    </DropdownMenuItem>
                    <DropdownMenuSeparator /> */}
                        <DropdownMenuItem onClick={() => navigate(ADDEDIT_PATIENTS_PATH.replace(":id", patient.userId))}>Edit Patient Info</DropdownMenuItem>
                        <DropdownMenuItem onClick={() => alert("Unimplemented!\nTODO: Delete patient functionality")}>Delete Patient</DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu >
            )
        },
    },

]