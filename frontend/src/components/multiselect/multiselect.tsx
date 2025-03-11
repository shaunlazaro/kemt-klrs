//https://gist.github.com/tushar-rupani/b59d30d82dfa248814739cb07291f94a
import { ComponentProps } from "react";
import {
    DropdownMenu,
    DropdownMenuCheckboxItem,
    DropdownMenuContent,
    DropdownMenuTrigger,
} from "../dropdown/dropdown";
import { FiChevronDown } from "react-icons/fi";
import { MdCancel } from "react-icons/md";
import classNames from "classnames";

type ISelectProps = ComponentProps<"button"> & {
    items: any[];
    label: string;
    valueKey: string;
    selectedValues: any[];
    placeholder?: string;
    dropDownClassName?: string;
    onChangeHandler: (checked: string) => void;
};

const MultiSelect = ({
    items,
    label,
    valueKey,
    selectedValues,
    placeholder,
    onChangeHandler,
    className,
    dropDownClassName,
}: ISelectProps) => {
    const isOptionSelected = (value: string): boolean =>
        selectedValues?.includes(value);
    const getDisplayedName = (value: string) => {
        const item = items?.find((item) => item[valueKey] === value);
        return item ? item[label] : "";
    };

    const sortedItems = items.sort((a, b) =>
        isOptionSelected(a[valueKey]) ? -1 : isOptionSelected(b[valueKey]) ? 1 : 0,
    );

    return (
        <>
            <DropdownMenu>
                <DropdownMenuTrigger asChild>
                    <button
                        className={classNames(
                            "flex h-9 w-fit items-center justify-between rounded-md border border-[#E2E2E2] px-3 text-left",
                            className,
                        )}
                    >
                        <div className="flex gap-1 overflow-hidden text-sm text-neutral-400">
                            {selectedValues?.length > 0
                                ? selectedValues.map((value) => (
                                    <div
                                        key={value}
                                        className="flex gap-0.5 rounded-md border border-[#E2E2E2] p-1 text-xs font-semibold"
                                    >
                                        {getDisplayedName(value)}
                                        <MdCancel size="16px" color="#545757" className="ml-1" />
                                    </div>
                                ))
                                : placeholder}
                        </div>
                        <FiChevronDown className="h-5 w-5 text-gray-600" />
                    </button>
                </DropdownMenuTrigger>
                <DropdownMenuContent
                    className={classNames(
                        "w-[342px] max-h-64 overflow-y-auto",
                        dropDownClassName,
                    )}
                    onCloseAutoFocus={(e) => e.preventDefault()}
                >
                    {sortedItems &&
                        sortedItems.length > 0 &&
                        sortedItems.map((value: any, index: number) => {
                            return (
                                <DropdownMenuCheckboxItem
                                    onSelect={(e) => e.preventDefault()}
                                    key={index}
                                    checked={isOptionSelected(value[valueKey])}
                                    onCheckedChange={() => onChangeHandler(value[valueKey])}
                                    className={
                                        isOptionSelected(value[valueKey]) ? "bg-accent-100" : ""
                                    }
                                >
                                    {value[label]}
                                </DropdownMenuCheckboxItem>
                            );
                        })}
                </DropdownMenuContent>
            </DropdownMenu>
        </>
    );
};

export default MultiSelect;
