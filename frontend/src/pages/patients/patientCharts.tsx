import { Bar, BarChart, CartesianGrid, Line, LineChart, Tooltip, XAxis, YAxis, ResponsiveContainer, Legend } from 'recharts';
import { getAverageScore, getPercentCompleted } from '../../common/utils';
import { RoutineData } from '../../interfaces/routineData.interface';

function processWeeklyData(routines: RoutineData[]) {
    const daysOfWeek = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const dataMap: Record<string, { totalScore: number, totalCompletion: number, count: number }> = {};

    daysOfWeek.forEach(day => {
        dataMap[day] = { totalScore: 0, totalCompletion: 0, count: 0 };
    });

    const today = new Date();
    const currentSunday = new Date(today);
    currentSunday.setDate(today.getDate() - today.getDay()); // Get most recent Sunday
    currentSunday.setHours(0, 0, 0, 0);

    routines.forEach(routine => {
        const routineDate = new Date(routine.created_at);
        if (routineDate < currentSunday) return; // Skip records before this week

        const day = daysOfWeek[routineDate.getDay()];
        dataMap[day].totalScore += getAverageScore(routine);
        dataMap[day].totalCompletion += getPercentCompleted(routine);
        dataMap[day].count += 1;
    });

    return daysOfWeek.map(day => ({
        day: day.charAt(0), // Abbreviate to first letter
        score: dataMap[day].count > 0 ? dataMap[day].totalScore / dataMap[day].count : 0,
        completion: dataMap[day].count > 0 ? dataMap[day].totalCompletion / dataMap[day].count : null, // Keep null for missing data
    }));
}

export function WeeklyCharts({ routines }: { routines: RoutineData[] }) {
    const weeklyData = processWeeklyData(routines);

    return (
        <div className="grid grid-cols-1 gap-4 w-full">
            <div className="font-bold text-center">This Week</div>
            <div className="px-4 bg-white w-full">
                <div className="text-center mb-2">Score</div>
                <ResponsiveContainer width="100%" height={200}>
                    <BarChart data={weeklyData} margin={{ left: 10, right: 10, top: 10 }}>
                        <CartesianGrid strokeDasharray="0" horizontal={true} vertical={false} />
                        <XAxis dataKey="day" interval={0} padding={{ left: 10, right: 10 }} />
                        <YAxis
                            axisLine={false}
                            tickLine={false}
                            width={30} // Keeps it compact
                            textAnchor="start" // Aligns numbers to the right
                            orientation='right'
                        />
                        <Tooltip />
                        <Bar dataKey="score" fill="var(--color-secondary-darkpink)" name="Average Score" />
                    </BarChart>
                </ResponsiveContainer>
            </div>
            <div className="px-4 bg-white w-full">
                <div className="text-center mb-2">Completion Percentage</div>
                <ResponsiveContainer width="100%" height={200}>
                    <LineChart data={weeklyData} margin={{ left: 10, right: 10, top: 10 }}>
                        <CartesianGrid strokeDasharray="0" horizontal={true} vertical={false} />
                        <XAxis dataKey="day" interval={0} padding={{ left: 10, right: 10 }} />
                        <YAxis
                            axisLine={false}
                            tickLine={false}
                            width={30} // Keeps it compact
                            textAnchor="start" // Aligns numbers to the right
                            orientation='right'
                        />
                        <Tooltip />
                        <Line type="monotone" dataKey="completion" stroke="var(--color-secondary-darkpink)" name="Completion %" connectNulls={false} />
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
}

function processMonthlyData(routines: RoutineData[]) {
    const today = new Date();
    const past30Days = new Array(30).fill(null).map((_, i) => {
        const date = new Date();
        date.setDate(today.getDate() - (29 - i));
        return date.toISOString().split('T')[0]; // Format YYYY-MM-DD
    });

    const dataMap: Record<string, { totalScore: number, totalCompletion: number, count: number }> = {};
    past30Days.forEach(date => {
        dataMap[date] = { totalScore: 0, totalCompletion: 0, count: 0 };
    });

    routines.forEach(routine => {
        const date = routine.created_at.split('T')[0];
        if (dataMap[date]) {
            dataMap[date].totalScore += getAverageScore(routine);
            dataMap[date].totalCompletion += getPercentCompleted(routine);
            dataMap[date].count += 1;
        }
    });

    return past30Days.map(date => ({
        day: new Date(date).toLocaleDateString(undefined, { month: 'numeric', day: 'numeric' }),
        score: dataMap[date].count > 0 ? dataMap[date].totalScore / dataMap[date].count : null,
        completion: dataMap[date].count > 0 ? dataMap[date].totalCompletion / dataMap[date].count : null,
    }));
}

export function MonthlyCharts({ routines }: { routines: RoutineData[] }) {
    const monthlyData = processMonthlyData(routines);

    return (
        <div className="grid grid-cols-1 gap-4 w-full">
            <div className="font-bold text-center">Last 30 Days</div>
            <div className="px-4 bg-white w-full">
                <div className="text-center mb-2">Score</div>
                <ResponsiveContainer width="100%" height={200}>
                    <BarChart data={monthlyData} margin={{ left: 10, right: 10, top: 10 }}>
                        <CartesianGrid strokeDasharray="0" horizontal={true} vertical={false} />
                        <XAxis dataKey="day" interval={3} padding={{ left: 10, right: 10 }} />
                        <YAxis axisLine={false} tickLine={false} width={30} textAnchor="start" orientation='right' />
                        <Tooltip />
                        <Bar dataKey="score" fill="var(--color-secondary-darkpink)" name="Average Score" />
                    </BarChart>
                </ResponsiveContainer>
            </div>
            <div className="px-4 bg-white w-full">
                <div className="text-center mb-2">Completion Percentage</div>
                <ResponsiveContainer width="100%" height={200}>
                    <LineChart data={monthlyData} margin={{ left: 10, right: 10, top: 10 }}>
                        <CartesianGrid strokeDasharray="0" horizontal={true} vertical={false} />
                        <XAxis dataKey="day" interval={3} padding={{ left: 10, right: 10 }} />
                        <YAxis axisLine={false} tickLine={false} width={30} textAnchor="start" orientation='right' />
                        <Tooltip />
                        <Line type="monotone" dataKey="completion" stroke="var(--color-secondary-darkpink)" name="Completion %" connectNulls={false} />
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
}