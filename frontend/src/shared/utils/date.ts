import { format, differenceInYears, parseISO } from 'date-fns';
import { ko } from 'date-fns/locale';

/**
 * 생년월일로부터 만 나이를 계산합니다.
 * @param birthDate ISO 형식의 생년월일 문자열 (예: "2015-03-15")
 * @returns 만 나이
 */
export function calculateAge(birthDate: string): number {
  const birth = parseISO(birthDate);
  return differenceInYears(new Date(), birth);
}

/**
 * 날짜를 한국어 형식으로 포맷팅합니다.
 * @param dateString ISO 형식의 날짜 문자열
 * @param formatString 포맷 문자열 (기본값: 'yyyy.MM.dd')
 * @returns 포맷팅된 날짜 문자열
 */
export function formatDate(dateString: string, formatString: string = 'yyyy.MM.dd'): string {
  return format(parseISO(dateString), formatString, { locale: ko });
}

/**
 * 날짜를 상대적인 형식으로 표시합니다.
 * @param dateString ISO 형식의 날짜 문자열
 * @returns "오늘", "어제", 또는 날짜 문자열
 */
export function formatRelativeDate(dateString: string): string {
  const date = parseISO(dateString);
  const today = new Date();
  const diffDays = Math.floor((today.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return '오늘';
  if (diffDays === 1) return '어제';
  if (diffDays <= 7) return `${diffDays}일 전`;

  return formatDate(dateString);
}
