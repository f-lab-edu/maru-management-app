/**
 * 출석 관리 날짜 유틸리티 함수
 */

import type { ViewMode } from '../types';
import { WEEKDAY_LABELS } from '../constants';

/**
 * 날짜 문자열을 "MM/DD (요일)" 형식으로 포맷
 *
 * @param dateStr - ISO 형식 날짜 문자열 (예: "2024-12-09")
 * @returns 포맷된 날짜 문자열 (예: "12/9 (월)")
 */
export const formatDateHeader = (dateStr: string): string => {
  const date = new Date(dateStr);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const weekday = WEEKDAY_LABELS[date.getDay()];

  return `${month}/${day} (${weekday})`;
};

/**
 * 뷰 모드에 따른 날짜 범위 계산
 *
 * @param viewMode - 뷰 모드 ('weekly' | 'monthly')
 * @param baseDate - 기준 날짜
 * @returns 시작일과 종료일 { startDate: string, endDate: string }
 */
export const getDateRangeForView = (
  viewMode: ViewMode,
  baseDate: Date = new Date()
): { startDate: string; endDate: string } => {
  const result = { startDate: '', endDate: '' };

  if (viewMode === 'weekly') {
    // 주간: 기준 날짜가 포함된 주의 월요일 ~ 일요일
    const currentDay = baseDate.getDay();
    const mondayOffset = currentDay === 0 ? -6 : 1 - currentDay;

    const monday = new Date(baseDate);
    monday.setDate(baseDate.getDate() + mondayOffset);

    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);

    result.startDate = formatToISO(monday);
    result.endDate = formatToISO(sunday);
  } else if (viewMode === 'monthly') {
    // 월간: 기준 날짜가 포함된 월의 1일 ~ 말일
    const firstDay = new Date(baseDate.getFullYear(), baseDate.getMonth(), 1);
    const lastDay = new Date(baseDate.getFullYear(), baseDate.getMonth() + 1, 0);

    result.startDate = formatToISO(firstDay);
    result.endDate = formatToISO(lastDay);
  }

  return result;
};

/**
 * Date 객체를 ISO 형식 문자열(YYYY-MM-DD)로 변환
 *
 * @param date - 변환할 Date 객체
 * @returns ISO 형식 날짜 문자열
 */
export const formatToISO = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
};

/**
 * 시작일과 종료일 사이의 모든 날짜 배열 생성
 *
 * @param startDate - 시작일 (ISO 문자열 또는 Date)
 * @param endDate - 종료일 (ISO 문자열 또는 Date)
 * @returns ISO 형식 날짜 문자열 배열
 */
export const generateDatesInRange = (
  startDate: string | Date,
  endDate: string | Date
): string[] => {
  const start = typeof startDate === 'string' ? new Date(startDate) : startDate;
  const end = typeof endDate === 'string' ? new Date(endDate) : endDate;
  const dates: string[] = [];

  const current = new Date(start);
  while (current <= end) {
    dates.push(formatToISO(current));
    current.setDate(current.getDate() + 1);
  }

  return dates;
};

/**
 * 오늘 날짜인지 확인
 *
 * @param dateStr - ISO 형식 날짜 문자열 (예: "2024-12-12")
 * @returns 오늘이면 true
 */
export const isToday = (dateStr: string): boolean => {
  return dateStr === formatToISO(new Date());
};

/**
 * 오늘 날짜를 ISO 형식으로 반환
 */
export const getTodayISO = (): string => {
  return formatToISO(new Date());
};
