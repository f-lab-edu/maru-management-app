import apiClient from './api';
import { DojangSearchResult, Employment, PendingApprovalRequest } from '../types/employment';

// TODO: 백엔드 API 완성 후 Mock 데이터 제거
const MOCK_DOJANGS: DojangSearchResult[] = [
  { id: 1, name: '강남태권도', address: '서울 강남구 테헤란로 123', ownerName: '김관장', phone: '02-1234-5678' },
  { id: 2, name: '서초무도관', address: '서울 서초구 서초대로 456', ownerName: '이관장', phone: '02-2345-6789' },
  { id: 3, name: '송파태권도장', address: '서울 송파구 올림픽로 789', ownerName: '박관장', phone: '02-3456-7890' },
  { id: 4, name: '태권도명가', address: '서울 강동구 천호대로 321', ownerName: '최관장', phone: '02-4567-8901' },
  { id: 5, name: '정통태권도', address: '서울 마포구 월드컵로 654', ownerName: '정관장', phone: '02-5678-9012' },
  { id: 6, name: '용인대태권도', address: '경기 용인시 처인구 삼가동 123', ownerName: '한관장', phone: '031-123-4567' },
  { id: 7, name: '대한태권도', address: '서울 종로구 세종대로 100', ownerName: '장관장', phone: '02-6789-0123' },
  { id: 8, name: '무적태권도', address: '서울 영등포구 여의대로 200', ownerName: '조관장', phone: '02-7890-1234' },
  { id: 9, name: '강서태권도관', address: '서울 강서구 화곡로 300', ownerName: '윤관장', phone: '02-8901-2345' },
  { id: 10, name: '동작태권', address: '서울 동작구 상도로 400', ownerName: '임관장', phone: '02-9012-3456' },
];

export interface PagedResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

const MOCK_MY_REQUESTS: Employment[] = [
  { id: 1, userId: 1, dojangId: 1, dojangName: '강남태권도', status: 'PENDING', requestedAt: '2024-12-01T10:00:00' },
];

const MOCK_PENDING_REQUESTS: PendingApprovalRequest[] = [
  { id: 1, userId: 10, userName: '김철수', userEmail: 'kim@example.com', userPhone: '010-1234-5678', requestedAt: '2024-11-22', status: 'PENDING' },
  { id: 2, userId: 11, userName: '이영희', userEmail: 'lee@example.com', userPhone: '010-9876-5432', requestedAt: '2024-11-21', status: 'PENDING' },
];

const DEFAULT_PAGE_SIZE = 5;

export const employmentService = {
  searchDojangs: async (
    keyword: string,
    page: number = 0,
    size: number = DEFAULT_PAGE_SIZE,
    _strategy: string = 'MEMORY'
  ): Promise<PagedResult<DojangSearchResult>> => {
    // TODO: 백엔드 API 완성 후 주석 해제
    // const response = await apiClient.get<PagedResult<DojangSearchResult>>('/dojangs/search', {
    //   params: { keyword, page, size, strategy },
    // });
    // return response.data;

    await new Promise(resolve => setTimeout(resolve, 300));
    const lowerKeyword = keyword.toLowerCase();
    const filtered = MOCK_DOJANGS.filter(
      d => d.name.toLowerCase().includes(lowerKeyword) ||
           d.address.toLowerCase().includes(lowerKeyword) ||
           d.ownerName.toLowerCase().includes(lowerKeyword)
    );

    const totalElements = filtered.length;
    const totalPages = Math.ceil(totalElements / size);
    const start = page * size;
    const content = filtered.slice(start, start + size);

    return { content, totalElements, totalPages, page, size };
  },

  requestApproval: async (dojangId: number): Promise<Employment> => {
    // TODO: 백엔드 API 완성 후 주석 해제
    // const response = await apiClient.post<Employment>('/employments/request', null, {
    //   params: { dojangId },
    // });
    // return response.data;

    await new Promise(resolve => setTimeout(resolve, 300));
    const dojang = MOCK_DOJANGS.find(d => d.id === dojangId);
    return {
      id: Date.now(),
      userId: 1,
      dojangId,
      dojangName: dojang?.name ?? '알 수 없는 도장',
      status: 'PENDING',
      requestedAt: new Date().toISOString(),
    };
  },

  getMyRequests: async (): Promise<Employment[]> => {
    // TODO: 백엔드 API 완성 후 주석 해제
    // const response = await apiClient.get<Employment[]>('/employments/pending');
    // return response.data;

    await new Promise(resolve => setTimeout(resolve, 300));
    return MOCK_MY_REQUESTS;
  },

  getPendingRequests: async (): Promise<PendingApprovalRequest[]> => {
    // TODO: 백엔드 API 완성 후 주석 해제
    // const response = await apiClient.get<PendingApprovalRequest[]>('/employments/pending');
    // return response.data;

    await new Promise(resolve => setTimeout(resolve, 300));
    return MOCK_PENDING_REQUESTS;
  },

  approve: async (employmentId: number): Promise<Employment> => {
    // TODO: 백엔드 API 완성 후 주석 해제
    // const response = await apiClient.patch<Employment>(`/employments/${employmentId}/approve`);
    // return response.data;

    await new Promise(resolve => setTimeout(resolve, 300));
    return { id: employmentId, userId: 10, dojangId: 1, dojangName: '강남태권도', status: 'ACTIVE', requestedAt: '2024-11-22', approvedAt: new Date().toISOString() };
  },

  reject: async (employmentId: number): Promise<Employment> => {
    // TODO: 백엔드 API 완성 후 주석 해제
    // const response = await apiClient.patch<Employment>(`/employments/${employmentId}/reject`);
    // return response.data;

    await new Promise(resolve => setTimeout(resolve, 300));
    return { id: employmentId, userId: 10, dojangId: 1, dojangName: '강남태권도', status: 'REJECTED', requestedAt: '2024-11-22', rejectedAt: new Date().toISOString() };
  },

  cancel: async (employmentId: number): Promise<void> => {
    // TODO: 백엔드 API 완성 후 주석 해제
    // await apiClient.delete(`/employments/${employmentId}/cancel`);

    await new Promise(resolve => setTimeout(resolve, 300));
    console.log('취소 요청:', employmentId);
  },
};
