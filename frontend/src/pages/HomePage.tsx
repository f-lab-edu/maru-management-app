import { useQuery } from "@tanstack/react-query";
import { api } from "../lib/axios";

const HomePage = () => {
  const { data, isLoading, error } = useQuery({
    queryKey: ["test-message"],
    queryFn: async () => {
      const res = await api.get<string>("/test", { responseType: "text" });
      return res.data;
    },
  });

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
      <div className="text-center space-y-8 p-8">
        <div className="space-y-4">
          <h1 className="text-4xl font-bold text-gray-800 mb-2">
            마루 태권도장 관리 시스템
          </h1>
          {isLoading && <p className="text-gray-600">불러오는 중...</p>}
          {error && <p className="text-red-600">불러오기에 실패했습니다.</p>}
          {data && <p className="text-gray-800">{data}</p>}
        </div>
      </div>
    </div>
  );
};

export default HomePage;
