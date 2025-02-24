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
                {(cell.getValue() as string[]).map((condition) => 
                    (<div className="rounded-2xl border text-white bg-primary px-4 py-1">{condition}</div>)
                )}
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
                {`${(cell.getValue() as string[]).map((exercise) => exercise)}`}
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
        //   const patient = row.original
    
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
                <DropdownMenuItem>Edit Patient Info</DropdownMenuItem>
                <DropdownMenuItem>Delete Patient</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          )
        },
      },
    
]