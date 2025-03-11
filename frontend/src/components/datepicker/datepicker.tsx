"use client"

import { format } from "date-fns"
import * as React from "react"

import { Button } from "../button/button"
import { Calendar } from "../calendar/calendar"
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "../popover/popover"
import { cn } from "../../common/utils"
import { CalendarIcon } from "lucide-react"

type DatePickerProps = React.HTMLAttributes<HTMLDivElement> & {
    date: Date;
    onDateChange: (date: Date) => void;
};

// export default function DatePicker() {
export const DatePicker = ({
    date,
    onDateChange,
}: DatePickerProps) => {
    // const [date, setDate] = React.useState<Date>()

    return (
        <Popover>
            <PopoverTrigger asChild>
                <Button
                    variant={"primary-outline"}
                    className={cn(
                        "w-[240px] justify-start text-left font-normal",
                        !date && "text-muted-foreground"
                    )}
                >
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {date ? format(date, "PPP") : <span>Pick a date</span>}
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0 bg-white" align="start">
                <Calendar mode="single" selected={date} onSelect={(date) => onDateChange(date ?? new Date())} />
            </PopoverContent>
        </Popover>
    )
}

export default DatePicker;