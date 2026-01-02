/**
 * YYYY-MM 형식의 billingYearMonth를 "YYYY년 M월" 형식으로 변환
 * @param billingYearMonth - "YYYY-MM" 형식의 청구 연월
 * @returns "YYYY년 M월" 형식의 문자열
 */
export function formatBillingYearMonth(billingYearMonth: string): string {
  const [year, month] = billingYearMonth.split('-');
  return `${year}년 ${parseInt(month, 10)}월`;
}

/**
 * YYYY-MM 형식의 billingYearMonth에서 연도 추출
 * @param billingYearMonth - "YYYY-MM" 형식의 청구 연월
 * @returns 연도 숫자
 */
export function extractYear(billingYearMonth: string): number {
  return parseInt(billingYearMonth.split('-')[0], 10);
}

/**
 * YYYY-MM 형식의 billingYearMonth에서 월 추출
 * @param billingYearMonth - "YYYY-MM" 형식의 청구 연월
 * @returns 월 숫자 (1-12)
 */
export function extractMonth(billingYearMonth: string): number {
  return parseInt(billingYearMonth.split('-')[1], 10);
}
