import apiClient from './api';
import type {
  SectionListRes,
  SectionRes,
  SectionCreateReq,
  SectionUpdateReq,
  SectionReorderReq,
} from '../features/divisions/types';

const BASE_PATH = '/sections';

export const sectionApi = {
  getSections: async (dojangId: string): Promise<SectionListRes> => {
    const response = await apiClient.get<SectionListRes>(BASE_PATH, {
      params: { dojangId },
    });
    return response.data;
  },

  createSection: async (dojangId: string, data: SectionCreateReq): Promise<SectionRes> => {
    const response = await apiClient.post<SectionRes>(BASE_PATH, data, {
      params: { dojangId },
    });
    return response.data;
  },

  updateSection: async (
    dojangId: string,
    sectionId: string,
    data: SectionUpdateReq
  ): Promise<SectionRes> => {
    const response = await apiClient.patch<SectionRes>(`${BASE_PATH}/${sectionId}`, data, {
      params: { dojangId },
    });
    return response.data;
  },

  deleteSection: async (dojangId: string, sectionId: string): Promise<void> => {
    await apiClient.delete(`${BASE_PATH}/${sectionId}`, {
      params: { dojangId },
    });
  },

  reorderSections: async (dojangId: string, data: SectionReorderReq): Promise<void> => {
    await apiClient.patch(`${BASE_PATH}/reorder`, data, {
      params: { dojangId },
    });
  },
};
