import { useState, useEffect, RefObject } from 'react';

export interface PageSizeConfig {
  rowHeight: number;
  tableHeaderHeight: number;
  paginationHeight: number;
  bottomPadding: number;
  minPageSize: number;
  maxPageSize: number;
}

/**
 * 컨테이너 위치 기반으로 동적 pageSize를 계산하는 훅
 * 화면 크기에 따라 테이블에 표시할 행 수를 자동으로 조절
 *
 * @param containerRef 테이블 컨테이너의 ref
 * @param config 테이블별 설정값 (행 높이, 헤더 높이 등)
 * @returns 계산된 pageSize
 */
export function useResponsivePageSize(
  containerRef: RefObject<HTMLElement | null>,
  config: PageSizeConfig
): number {
  const {
    rowHeight,
    tableHeaderHeight,
    paginationHeight,
    bottomPadding,
    minPageSize,
    maxPageSize,
  } = config;

  const [pageSize, setPageSize] = useState(minPageSize);

  useEffect(() => {
    const calculatePageSize = () => {
      if (!containerRef.current) return minPageSize;

      const containerTop = containerRef.current.getBoundingClientRect().top;
      const availableHeight =
        window.innerHeight - containerTop - tableHeaderHeight - paginationHeight - bottomPadding;
      const calculated = Math.floor(availableHeight / rowHeight);

      return Math.max(minPageSize, Math.min(maxPageSize, calculated));
    };

    const updatePageSize = () => {
      const newPageSize = calculatePageSize();
      setPageSize((prev) => (prev === newPageSize ? prev : newPageSize));
    };

    updatePageSize();
    window.addEventListener('resize', updatePageSize);
    return () => window.removeEventListener('resize', updatePageSize);
  }, [
    containerRef,
    rowHeight,
    tableHeaderHeight,
    paginationHeight,
    bottomPadding,
    minPageSize,
    maxPageSize,
  ]);

  return pageSize;
}
