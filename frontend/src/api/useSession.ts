export const useSession = () => {
    const token = localStorage.getItem("authToken");
    return !!token;
};