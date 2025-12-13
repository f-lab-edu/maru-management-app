import { useState, useMemo } from 'react';
import { Download } from 'lucide-react';
import { useAuthStore } from '@/stores/authStore';
import { Button } from '@/shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { ToggleGroup, ToggleGroupItem } from '@/shared/components/ui/toggle-group';
import type { DateRange } from 'react-day-picker';
import type { ViewMode, RangeStudentRow } from '@/features/attendance/types';
import {
  getDateRangeForView,
  formatToISO,
  generateDatesInRange,
} from '@/features/attendance/utils/dateUtils';
import { calculateAttendanceSummary } from '@/features/attendance/utils/statsUtils';
import {
  useAttendanceRange,
  useAttendanceFilter,
} from '@/features/attendance/hooks';
import {
  AttendanceChartCard,
  AttendanceDataTable,
  AttendanceDetailSheet,
  createAttendanceColumns,
} from '@/features/attendance/components';
import { DateRangePicker } from '@/shared/components/ui/date-range-picker';
import { Users, UserCheck, UserX, TrendingUp } from 'lucide-react';

export default function AttendanceListPage() {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;

  // 뷰 모드 및 날짜 범위
  const [viewMode, setViewMode] = useState<ViewMode>('weekly');
  const [dateRange, setDateRange] = useState<DateRange | undefined>(() => {
    const { startDate, endDate } = getDateRangeForView('weekly', new Date());
    return { from: new Date(startDate), to: new Date(endDate) };
  });

  // 필터 상태
  const [searchQuery, setSearchQuery] = useState('');

  // Sheet 상태
  const [selectedStudent, setSelectedStudent] = useState<RangeStudentRow | null>(null);

  // 뷰 모드 변경 핸들러
  const handleViewModeChange = (mode: string) => {
    if (mode === 'weekly' || mode === 'monthly') {
      setViewMode(mode);
      const baseDate = dateRange?.from ?? new Date();
      const { startDate, endDate } = getDateRangeForView(mode, baseDate);
      setDateRange({ from: new Date(startDate), to: new Date(endDate) });
    }
  };

  // 날짜 범위 변경 핸들러
  const handleDateRangeChange = (range: DateRange | undefined) => {
    setDateRange(range);
  };

  // 기간별 출석 데이터 조회
  const startDate = dateRange?.from ? formatToISO(dateRange.from) : '';
  const endDate = dateRange?.to ? formatToISO(dateRange.to) : '';
  const { data: rangeData, isLoading } = useAttendanceRange({
    dojangId,
    startDate,
    endDate,
  });

  // 날짜 배열 생성 (dateRange에서 직접 생성 - API 응답 기다리지 않음)
  const dates = useMemo(() => {
    if (!dateRange?.from || !dateRange?.to) return [];
    return generateDatesInRange(dateRange.from, dateRange.to);
  }, [dateRange]);

  // 필터링
  const filteredStudents = useAttendanceFilter({
    students: rangeData?.students ?? [],
    searchQuery,
    statusFilter: 'ALL',
  });

  // 통계 계산
  const summary = useMemo(() => {
    if (!rangeData?.students || dates.length === 0) {
      return { totalStudents: 0, present: 0, absent: 0, sick: 0, excused: 0 };
    }
    return calculateAttendanceSummary(rangeData.students, dates);
  }, [rangeData?.students, dates]);

  // 테이블 컬럼
  const columns = useMemo(() => createAttendanceColumns({ dates, viewMode }), [dates, viewMode]);

  // Row 클릭 핸들러
  const handleRowClick = (student: RangeStudentRow) => {
    setSelectedStudent(student);
  };

  // Sheet 닫기
  const handleCloseSheet = () => {
    setSelectedStudent(null);
  };

  // 기간 레이블
  const periodLabel = viewMode === 'weekly' ? '주간' : '월간';

  // 다운로드 핸들러
  const handleDownload = () => {
    console.log('엑셀 다운로드');
  };

  const totalAbsent = summary.absent + summary.sick + summary.excused;
  const attendanceRate =
    summary.totalStudents > 0 ? Math.round((summary.present / (summary.present + totalAbsent)) * 100) : 0;

  return (
    <div className="h-full flex flex-col gap-4 p-4 lg:gap-6 lg:p-8">
      {/* 헤더 */}
      <header className="flex items-center justify-between shrink-0">
        <div>
          <h1 className="text-2xl font-bold">출석 관리</h1>
          <p className="text-sm text-muted-foreground">
            {selectedDojang?.dojangName}의 출석 현황
          </p>
        </div>
        <div className="flex items-center gap-3">
          {/* 주간/월간 토글 - 큰 버튼 */}
          <ToggleGroup type="single" value={viewMode} onValueChange={handleViewModeChange}>
            <ToggleGroupItem value="weekly" className="px-6 py-2">
              주간
            </ToggleGroupItem>
            <ToggleGroupItem value="monthly" className="px-6 py-2">
              월간
            </ToggleGroupItem>
          </ToggleGroup>

          <Button variant="outline" onClick={handleDownload}>
            <Download className="mr-2 h-4 w-4" />
            내보내기
          </Button>
        </div>
      </header>

      {/* 상단 영역: Stats 카드 + 차트 (50:50 양분) */}
      <div className="grid gap-4 lg:grid-cols-2 shrink-0">
        {/* 왼쪽: Stats 카드 2x2 그리드 */}
        <div className="grid gap-3 grid-cols-2">
          <Card className="border shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
              <CardTitle className="text-xs font-medium text-muted-foreground">전체 원생</CardTitle>
              <Users className="h-3.5 w-3.5 text-muted-foreground" />
            </CardHeader>
            <CardContent className="px-4 pb-3 pt-0">
              <div className="text-2xl font-bold">{summary.totalStudents}</div>
              <p className="text-[10px] text-muted-foreground mt-0.5">{periodLabel} 기준</p>
            </CardContent>
          </Card>

          <Card className="border shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
              <CardTitle className="text-xs font-medium text-muted-foreground">출석</CardTitle>
              <UserCheck className="h-3.5 w-3.5 text-muted-foreground" />
            </CardHeader>
            <CardContent className="px-4 pb-3 pt-0">
              <div className="text-2xl font-bold">{summary.present}</div>
              <p className="text-[10px] text-muted-foreground mt-0.5">전체 기간 합계</p>
            </CardContent>
          </Card>

          <Card className="border shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
              <CardTitle className="text-xs font-medium text-muted-foreground">미출석</CardTitle>
              <UserX className="h-3.5 w-3.5 text-muted-foreground" />
            </CardHeader>
            <CardContent className="px-4 pb-3 pt-0">
              <div className="text-2xl font-bold">{totalAbsent}</div>
              <p className="text-[10px] text-muted-foreground mt-0.5">
                결석 {summary.absent} / 병결 {summary.sick} / 공결 {summary.excused}
              </p>
            </CardContent>
          </Card>

          <Card className="border shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 px-4 pt-3">
              <CardTitle className="text-xs font-medium text-muted-foreground">출석률</CardTitle>
              <TrendingUp className="h-3.5 w-3.5 text-muted-foreground" />
            </CardHeader>
            <CardContent className="px-4 pb-3 pt-0">
              <div className="text-2xl font-bold">{attendanceRate}%</div>
              <p className="text-[10px] text-muted-foreground mt-0.5">{periodLabel} 평균</p>
            </CardContent>
          </Card>
        </div>

        {/* 오른쪽: Pie Chart */}
        <AttendanceChartCard
          data={{
            present: summary.present,
            absent: summary.absent,
            sick: summary.sick,
            excused: summary.excused,
          }}
          periodLabel={periodLabel}
        />
      </div>

      {/* 툴바 */}
      <div className="flex items-center justify-between gap-4 shrink-0">
        <div className="flex items-center gap-3">
          <Input
            placeholder="원생 이름 검색..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-[250px]"
          />
        </div>
        <DateRangePicker dateRange={dateRange} onDateRangeChange={handleDateRangeChange} />
      </div>

      {/* Data Table - 남은 공간 모두 차지 */}
      <div className="flex-1 min-h-0">
        <AttendanceDataTable
          columns={columns}
          data={filteredStudents}
          isLoading={isLoading}
          onRowClick={handleRowClick}
          globalFilter={searchQuery}
        />
      </div>

      {/* 상세 Sheet (오른쪽 사이드바) */}
      <AttendanceDetailSheet
        student={selectedStudent}
        startDate={startDate}
        endDate={endDate}
        isOpen={!!selectedStudent}
        onClose={handleCloseSheet}
      />
    </div>
  );
}
