import React from "react";

interface CircularProgressProps {
    fill: number; // 0 to 100
    text: string;
}

const CircularProgress: React.FC<CircularProgressProps> = ({ fill, text }) => {
    const radius = 45;
    const stroke = 10;
    const normalizedRadius = radius - stroke / 2;
    const circumference = normalizedRadius * 2 * Math.PI;
    const strokeDashoffset = circumference - (fill / 100) * circumference;

    return (
        <div className="relative w-28 h-28">
            <svg height="100%" width="100%" viewBox="0 0 100 100">
                {/* Background circle */}
                <circle
                    stroke="currentColor"
                    strokeWidth={stroke}
                    fill="none"
                    cx="50"
                    cy="50"
                    r={normalizedRadius}
                    className="text-primary-lightblue"
                />
                {/* Progress circle */}
                <circle
                    stroke="currentColor"
                    strokeWidth={stroke}
                    fill="none"
                    cx="50"
                    cy="50"
                    r={normalizedRadius}
                    strokeDasharray={circumference}
                    strokeDashoffset={strokeDashoffset}
                    strokeLinecap="round"
                    transform="rotate(-90 50 50)"
                    className="text-primary-blue transition-all duration-500"
                />
            </svg>

            {/* Center text */}
            <div className="absolute inset-0 flex items-center justify-center text-primary-darkblue text-lg font-semibold">
                {text}
            </div>
        </div>
    );
};

export default CircularProgress;