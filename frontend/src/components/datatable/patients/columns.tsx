import { Patient } from "../../../interfaces/patient.interface";
import { ColumnDef } from "@tanstack/react-table"
import { MoreHorizontal } from "lucide-react"
import Button from "../../button"
import {
    DropdownMenu,
    // DropdownMenuCheckboxItem,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    // DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "../../dropdown/dropdown"
import { ADDEDIT_PATIENTS_PATH, PATIENTS_PATH } from "../../../routes";
import { useNavigate } from "react-router-dom";
import { RoutineConfig } from "../../../interfaces/exercisePlan.interface";
import { getPatientAge, getPatientName } from "../../../common/utils";
import { useDeletePatient } from "../../../api/hooks";


export const PatientTableColumnDef: ColumnDef<Patient>[] = [
    {
        id: "name",
        accessorFn: (patient) => getPatientName(patient),
        header: () => (
            <div className="w-full cursor-default">Name</div>
        ),
        cell: ({ cell }) => (
            <div
                className={`rounded-md py-1`}
            >
                {`${cell.getValue()}`}
            </div>
        ),
    },
    {
        id: "age",
        accessorFn: (patient) => getPatientAge(patient),
        header: () => (
            <div className="w-full cursor-default">Age</div>
        ),
        cell: ({ cell }) => (
            <div
                className={`rounded-md py-1`}
            >
                {`${cell.getValue()}`}
            </div>
        ),
    },
    {
        id: "sex",
        accessorFn: (patient) => patient.sex,
        header: () => (
            <div className="w-full cursor-default">Sex</div>
        ),
        cell: ({ cell }) => (
            <div
                className={`rounded-md py-1`}
            >
                {`${cell.getValue()}`}
            </div>
        ),
    },
    {
        id: "condition",
        accessorFn: (patient) => patient.condition,
        header: () => (
            <div className="w-full cursor-default">Condition</div>
        ),
        cell: ({ cell }) => (
            <div
                className={`rounded-md py-1 font-semibold text-sm flex`}
            >
                <div className="rounded-2xl border text-white bg-secondary-darkpink px-4 py-1">{`${cell.getValue()}`}</div>
            </div>
        ),
    },
    {
        id: "exercises",
        accessorFn: (patient) => patient.exercises,
        header: () => (
            <div className="w-full cursor-default">Exercises</div>
        ),
        cell: ({ cell }) => (
            <div
                className={`rounded-md py-1 font-semibold text-sm flex`}
            >
                <div className="rounded-2xl border text-white bg-secondary-darkpink px-4 py-1">{`${(cell.getValue() as RoutineConfig)?.name}`}</div>
            </div>
        ),
    },
    {
        id: "weeklyProgress",
        accessorFn: (_) => 0,
        header: () => (
            <div className="w-full cursor-default">Weekly Progress</div>
        ),
        cell: ({ cell }) => (
            <div
                className={`rounded-md py-1`}
            >
                {`${cell.getValue()}`}/7
            </div>
        ),
    },
    {
        id: "actions",
        enableHiding: false,
        header: () => (
            <div className="w-full cursor-default">Action</div>
        ),
        cell: ({ row }) => {
            const patient = row.original
            const navigate = useNavigate();
            const deletePatient = useDeletePatient();

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
                        <DropdownMenuItem onClick={() => navigate(ADDEDIT_PATIENTS_PATH.replace(":id", patient.id))}>Edit Patient Info</DropdownMenuItem>
                        <DropdownMenuItem onClick={() => {
                            if (!confirm("Delete this patient?"))
                                return;
                            deletePatient.mutate(patient.id, {
                                onSuccess: () => navigate(PATIENTS_PATH),
                            });
                        }}>Delete Patient</DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu >
            )
        },
    },

]