import React, { createContext, useContext, useState } from "react";

interface ApiCacheContextType {
    fetchData: <T>(key: string, endpoint: string, options?: RequestInit) => Promise<T>;
}

const ApiCacheContext = createContext<ApiCacheContextType | null>(null);

export const ApiCacheProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [cache, setCache] = useState<Record<string, unknown>>({});

    const fetchData = async <T extends unknown>(key: string, endpoint: string, options: RequestInit = {}): Promise<T> => {
        if (cache[key]) {
            return cache[key] as T;
        }
        try {
            const response = await fetch(endpoint, options);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data: T = await response.json();
            setCache((prevCache) => ({ ...prevCache, [key]: data }));
            return data;
        } catch (error) {
            console.error("API Fetch Error:", error);
            throw error;
        }
    };

    return (
        <ApiCacheContext.Provider value={{ fetchData }}>
            {children}
        </ApiCacheContext.Provider>
    );
};

export const useApiCache = (): ApiCacheContextType => {
    const context = useContext(ApiCacheContext);
    if (!context) {
        throw new Error("useApiCache must be used within an ApiCacheProvider");
    }
    return context;
};

/*
example for my own reference:
  const { fetchData } = useApiCache();
    useEffect(() => {
    const loadPosts = async () => {
      try {
        const data = await fetchData<Post[]>("posts", "https://jsonplaceholder.typicode.com/posts");
        setPosts(data);
      } catch (err) {
        setError("Failed to fetch posts");
      } finally {
        setLoading(false);
      }
    };

    loadPosts();
  }, [fetchData]);
*/