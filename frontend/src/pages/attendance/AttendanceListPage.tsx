import { useState, useMemo } from 'react';
import { Download, UserPlus, ChevronDown, LogOut } from 'lucide-react';
import { RowSelectionState } from '@tanstack/react-table';
import { useAuthStore } from '@/stores/authStore';
import { Button } from '@/shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { ToggleGroup, ToggleGroupItem } from '@/shared/components/ui/toggle-group';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Label } from '@/shared/components/ui/label';
import { SectionDivisionFilter } from '@/features/divisions';
import type { DateRange } from 'react-day-picker';
import type { ViewMode, StudentAttendanceRow, CheckMethod, AttendanceStatus } from '@/features/attendance/types';
import {
  getDateRangeForView,
  formatToISO,
  generateDatesInRange,
} from '@/features/attendance/utils/dateUtils';
import { calculateAttendanceSummary } from '@/features/attendance/utils/statsUtils';
import {
  useAttendanceRange,
  useAttendanceFilter,
  useBulkCheckIn,
  useBulkCheckout,
  useAttendanceStatusUpdate,
  useAttendanceCheckout,
  useCancelCheckout,
  useBulkStatusChange,
  useAttendanceCheck,
  useAttendanceTimeChange,
} from '@/features/attendance/hooks';
import {
  AttendanceChartCard,
  AttendanceDataTable,
  AttendanceDetailSheet,
  createAttendanceColumns,
} from '@/features/attendance/components';
import { DateRangePicker } from '@/shared/components/ui/date-range-picker';
import { Users, UserCheck, UserX, TrendingUp, HelpCircle } from 'lucide-react';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/shared/components/ui/tooltip';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import { useAlert, useConfirm } from '@/hooks';
import { ATTENDANCE_STATUS_LABELS } from '@/features/attendance/constants';

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
  const [showOnlyNotCheckedIn, setShowOnlyNotCheckedIn] = useState(false);

  // 수련부/수련반 필터 상태
  const [sectionId, setSectionId] = useState<string | null>(null);
  const [divisionId, setDivisionId] = useState<string | null>(null);

  // 선택 상태
  const [rowSelection, setRowSelection] = useState<RowSelectionState>({});

  // Sheet 상태 - ID만 저장하고 데이터는 파생 상태로 관리
  const [selectedStudentId, setSelectedStudentId] = useState<string | null>(null);

  // 뷰 모드 변경 핸들러
  const handleViewModeChange = (mode: string) => {
    if (mode === 'weekly' || mode === 'monthly') {
      setViewMode(mode);
      const { startDate, endDate } = getDateRangeForView(mode, new Date());
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
    sectionId,
    divisionId,
  });

  // 날짜 배열 생성 (dateRange에서 직접 생성 - API 응답 기다리지 않음)
  const dates = useMemo(() => {
    if (!dateRange?.from || !dateRange?.to) return [];
    return generateDatesInRange(dateRange.from, dateRange.to);
  }, [dateRange]);

  // 오늘 날짜 (ISO 형식)
  const todayISO = formatToISO(new Date());

  // 필터링 (검색어 + 미출석 필터)
  const baseFilteredStudents = useAttendanceFilter({
    students: rangeData?.students ?? [],
    searchQuery,
    statusFilter: 'ALL',
  });

  // 미출석 필터 적용
  const filteredStudents = useMemo(() => {
    if (!showOnlyNotCheckedIn) return baseFilteredStudents;

    return baseFilteredStudents.filter((student) => {
      const todayAttendance = student.attendances[todayISO];
      return !todayAttendance || todayAttendance.status !== 'PRESENT';
    });
  }, [baseFilteredStudents, showOnlyNotCheckedIn, todayISO]);

  // 선택된 학생 (파생 상태 - 데이터 갱신 시 자동 반영)
  const selectedStudent = useMemo(() => {
    if (!selectedStudentId || !rangeData?.students) return null;
    return rangeData.students.find((s) => s.id === selectedStudentId) ?? null;
  }, [selectedStudentId, rangeData?.students]);

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
  const handleRowClick = (student: StudentAttendanceRow) => {
    setSelectedStudentId(student.id);
  };

  // Sheet 닫기
  const handleCloseSheet = () => {
    setSelectedStudentId(null);
  };

  // 기간 레이블
  const periodLabel = viewMode === 'weekly' ? '주간' : '월간';

  // 다운로드 핸들러
  const handleDownload = () => {
    console.log('엑셀 다운로드');
  };

  const totalAbsent = summary.absent + summary.sick + summary.excused;
  const total = summary.present + totalAbsent;
  const attendanceRate = total > 0 ? Math.round((summary.present / total) * 100) : 0;

  // Alert
  const { showSuccess, showError, showWarning, showAlert } = useAlert();
  const { confirm } = useConfirm();

  // Mutations
  const { mutate: bulkCheckIn, isPending: isBulkCheckingIn } = useBulkCheckIn(dojangId);
  const { mutate: bulkCheckout, isPending: isBulkCheckingOut } = useBulkCheckout(dojangId);
  const { mutateAsync: changeStatus } = useAttendanceStatusUpdate(dojangId);
  const { mutateAsync: checkout } = useAttendanceCheckout(dojangId);
  const { mutate: bulkStatusChange, isPending: isBulkStatusChanging } = useBulkStatusChange(dojangId);
  const { mutateAsync: createAttendance } = useAttendanceCheck(dojangId);
  const { mutateAsync: changeTime } = useAttendanceTimeChange(dojangId);
  const { mutateAsync: cancelCheckout } = useCancelCheckout(dojangId);

  // 선택된 학생 정보 목록
  const selectedStudents = useMemo(() => {
    return filteredStudents.filter((s) => rowSelection[String(s.id)]);
  }, [filteredStudents, rowSelection]);

  // 선택된 학생들의 오늘 출석 기록 ID 목록
  const selectedTodayAttendanceIds = useMemo(() => {
    return selectedStudents
      .map((s) => s.attendances[todayISO]?.id)
      .filter((id): id is string => id !== undefined && id !== null);
  }, [selectedStudents, todayISO]);

  // 선택된 학생들 중 하원 가능한 출석 기록 ID 목록 (오늘 출석 + 아직 퇴관 안함)
  const selectedCheckoutableIds = useMemo(() => {
    return selectedStudents
      .filter((s) => {
        const todayAtt = s.attendances[todayISO];
        return todayAtt?.status === 'PRESENT' && !todayAtt?.checkoutAt;
      })
      .map((s) => s.attendances[todayISO]!.id);
  }, [selectedStudents, todayISO]);

  // 오늘 날짜 표시 형식
  const todayDisplay = new Date().toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  // 단체 출석 핸들러
  const handleBulkCheckIn = async () => {
    if (selectedStudents.length === 0) {
      showWarning('출석 처리할 원생을 선택해주세요');
      return;
    }

    const firstName = selectedStudents[0].name;
    const othersCount = selectedStudents.length - 1;
    const confirmTitle = othersCount > 0
      ? `${firstName} 외 ${othersCount}명`
      : firstName;

    const { isConfirmed } = await confirm({
      title: `${confirmTitle}`,
      text: `${todayDisplay} 출석 처리하시겠습니까?`,
      confirmText: '출석 처리',
      type: 'info',
    });

    if (!isConfirmed) return;

    const studentIds = selectedStudents.map((s) => s.id);

    bulkCheckIn(
      {
        studentIds,
        method: 'MANUAL' as CheckMethod,
      },
      {
        onSuccess: (result) => {
          if (result.failureCount === 0) {
            showSuccess(`${result.successCount}명 출석 처리 완료`);
          } else {
            const failureDetails = result.failureList
              .map((f) => {
                const student = selectedStudents.find((s) => s.id === f.studentId);
                return `• ${student?.name ?? f.studentId}: ${f.errorMessage}`;
              })
              .join('\n');

            showAlert({
              title: '출석 처리 결과',
              text: `성공: ${result.successCount}명\n실패: ${result.failureCount}명\n\n${failureDetails}`,
              type: result.successCount > 0 ? 'warning' : 'error',
            });
          }
          setRowSelection({});
        },
        onError: () => {
          showError('출석 처리 중 오류가 발생했습니다');
        },
      }
    );
  };

  // 단체 하원 핸들러
  const handleBulkCheckout = async () => {
    if (selectedCheckoutableIds.length === 0) {
      showWarning('하원 처리할 원생을 선택해주세요');
      return;
    }

    const checkoutableStudents = selectedStudents.filter((s) => {
      const todayAtt = s.attendances[todayISO];
      return todayAtt?.status === 'PRESENT' && !todayAtt?.checkoutAt;
    });
    const firstName = checkoutableStudents[0]?.name ?? '';
    const othersCount = checkoutableStudents.length - 1;
    const confirmTitle = othersCount > 0
      ? `${firstName} 외 ${othersCount}명`
      : firstName;

    const { isConfirmed } = await confirm({
      title: confirmTitle,
      text: `${todayDisplay} 하원 처리하시겠습니까?`,
      confirmText: '하원 처리',
      type: 'info',
    });

    if (!isConfirmed) return;

    bulkCheckout(
      selectedCheckoutableIds,
      {
        onSuccess: (result) => {
          if (result.failureCount === 0) {
            showSuccess(`${result.successCount}명 하원 처리 완료`);
          } else {
            showAlert({
              title: '하원 처리 결과',
              text: `성공: ${result.successCount}명\n실패: ${result.failureCount}명`,
              type: result.successCount > 0 ? 'warning' : 'error',
            });
          }
          setRowSelection({});
        },
        onError: () => {
          showError('하원 처리 중 오류가 발생했습니다');
        },
      }
    );
  };

  // 개별 상태 변경 핸들러
  const handleStatusChange = async (attendanceId: string, newStatus: AttendanceStatus) => {
    try {
      await changeStatus({ attendanceId, request: { status: newStatus } });
      showSuccess('상태가 변경되었습니다');
    } catch {
      showError('상태 변경에 실패했습니다');
    }
  };

  // 개별 퇴관 핸들러
  const handleCheckout = async (attendanceId: string) => {
    try {
      await checkout(attendanceId);
      showSuccess('퇴관 처리되었습니다');
    } catch {
      showError('퇴관 처리에 실패했습니다');
    }
  };

  // 퇴관 취소 핸들러
  const handleCancelCheckout = async (attendanceId: string) => {
    try {
      await cancelCheckout(attendanceId);
      showSuccess('퇴관이 취소되었습니다');
    } catch {
      showError('퇴관 취소에 실패했습니다');
    }
  };

  // 출석 기록 생성 핸들러 (결석/병결/공결)
  const handleCreateRecord = async (studentId: string, date: string, status: AttendanceStatus) => {
    try {
      await createAttendance({
        studentId,
        method: 'MANUAL' as CheckMethod,
        status,
        date,
      });
      showSuccess('출석 기록이 생성되었습니다');
    } catch {
      showError('출석 기록 생성에 실패했습니다');
    }
  };

  // 시간 변경 핸들러
  const handleTimeChange = async (attendanceId: string, checkinAt?: string, checkoutAt?: string) => {
    try {
      await changeTime({
        attendanceId,
        request: { checkinAt, checkoutAt },
      });
      showSuccess('시간이 변경되었습니다');
    } catch {
      showError('시간 변경에 실패했습니다');
    }
  };

  // 단체 상태 변경 핸들러
  const handleBulkStatusChange = async (newStatus: AttendanceStatus) => {
    if (selectedTodayAttendanceIds.length === 0) {
      showWarning('오늘 출석 기록이 있는 원생을 선택해주세요');
      return;
    }

    const statusLabel = ATTENDANCE_STATUS_LABELS[newStatus];
    const firstName = selectedStudents[0].name;
    const othersCount = selectedStudents.length - 1;
    const confirmTitle = othersCount > 0
      ? `${firstName} 외 ${othersCount}명`
      : firstName;

    const { isConfirmed } = await confirm({
      title: `${confirmTitle}`,
      text: `${todayDisplay} ${statusLabel} 처리하시겠습니까?`,
      confirmText: `${statusLabel} 처리`,
      type: newStatus === 'ABSENT' ? 'warning' : 'info',
    });

    if (!isConfirmed) return;

    bulkStatusChange(
      {
        attendanceIds: selectedTodayAttendanceIds,
        status: newStatus,
      },
      {
        onSuccess: (result) => {
          if (result.failureCount === 0) {
            showSuccess(`${result.successCount}명 ${statusLabel} 처리 완료`);
          } else {
            showAlert({
              title: '상태 변경 결과',
              text: `성공: ${result.successCount}명\n실패: ${result.failureCount}명`,
              type: result.successCount > 0 ? 'warning' : 'error',
            });
          }
          setRowSelection({});
        },
        onError: () => {
          showError('상태 변경 중 오류가 발생했습니다');
        },
      }
    );
  };

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
      <div className="grid gap-4 lg:grid-cols-2 shrink-0 lg:h-[220px]">
        {/* 왼쪽: Stats 카드 2x2 그리드 */}
        <div className="grid gap-3 grid-cols-2 h-[200px] lg:h-auto">
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
              <TooltipProvider>
                <div className="flex items-center gap-1">
                  <span className="text-2xl font-bold">{attendanceRate}%</span>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <button type="button" className="text-muted-foreground hover:text-foreground">
                        <HelpCircle className="h-3.5 w-3.5" />
                      </button>
                    </TooltipTrigger>
                    <TooltipContent side="top" className="max-w-[240px] text-xs">
                      <p className="font-medium mb-1">출석률 계산식</p>
                      <p className="text-muted-foreground">
                        출석 / (출석 + 결석 + 병결 + 공결) × 100
                      </p>
                    </TooltipContent>
                  </Tooltip>
                </div>
              </TooltipProvider>
              <p className="text-[10px] text-muted-foreground mt-0.5">{periodLabel} 평균</p>
            </CardContent>
          </Card>
        </div>

        {/* 오른쪽: Pie Chart */}
        <div className="h-[200px] lg:h-auto">
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
      </div>

      {/* 툴바 */}
      <div className="flex items-center justify-between gap-4 shrink-0">
        <div className="flex items-center gap-4">
          <Input
            placeholder="원생 이름 검색..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-[250px]"
          />
          <div className="flex items-center space-x-2">
            <Checkbox
              id="notCheckedIn"
              checked={showOnlyNotCheckedIn}
              onCheckedChange={(checked) => setShowOnlyNotCheckedIn(checked === true)}
            />
            <Label htmlFor="notCheckedIn" className="text-sm cursor-pointer">
              오늘 미출석만
            </Label>
          </div>
          {dojangId && (
            <SectionDivisionFilter
              dojangId={dojangId}
              selectedSectionId={sectionId}
              selectedDivisionId={divisionId}
              onSectionChange={setSectionId}
              onDivisionChange={setDivisionId}
              compact
            />
          )}
        </div>
        <div className="flex items-center gap-3">
          {selectedStudents.length > 0 && (
            <>
              <Button onClick={handleBulkCheckIn} disabled={isBulkCheckingIn}>
                <UserPlus className="mr-2 h-4 w-4" />
                {isBulkCheckingIn ? '처리 중...' : `${selectedStudents.length}명 단체 출석`}
              </Button>

              {/* 단체 하원 버튼 */}
              {selectedCheckoutableIds.length > 0 && (
                <Button variant="outline" onClick={handleBulkCheckout} disabled={isBulkCheckingOut}>
                  <LogOut className="mr-2 h-4 w-4" />
                  {isBulkCheckingOut ? '처리 중...' : `${selectedCheckoutableIds.length}명 단체 하원`}
                </Button>
              )}

              {/* 단체 상태 변경 드롭다운 */}
              {selectedTodayAttendanceIds.length > 0 && (
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="outline" disabled={isBulkStatusChanging}>
                      <UserX className="mr-2 h-4 w-4" />
                      {isBulkStatusChanging ? '처리 중...' : '상태 변경'}
                      <ChevronDown className="ml-2 h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem onClick={() => handleBulkStatusChange('ABSENT')}>
                      결석 처리
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => handleBulkStatusChange('SICK')}>
                      병결 처리
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => handleBulkStatusChange('EXCUSED')}>
                      공결 처리
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              )}
            </>
          )}
          {/* 월간뷰 레전드 */}
          {viewMode === 'monthly' && (
            <div className="flex items-center gap-3 text-xs text-muted-foreground border-r pr-3">
              <div className="flex items-center gap-1">
                <div className="w-2.5 h-2.5 rounded-full bg-emerald-600" />
                <span>출석</span>
              </div>
              <div className="flex items-center gap-1">
                <div className="w-2.5 h-2.5 rounded-full bg-red-500" />
                <span>결석</span>
              </div>
              <div className="flex items-center gap-1">
                <div className="w-2.5 h-2.5 rounded-full bg-amber-500" />
                <span>병결</span>
              </div>
              <div className="flex items-center gap-1">
                <div className="w-2.5 h-2.5 rounded-full bg-blue-500" />
                <span>공결</span>
              </div>
            </div>
          )}
          <DateRangePicker dateRange={dateRange} onDateRangeChange={handleDateRangeChange} />
        </div>
      </div>

      {/* Data Table - 남은 공간 모두 차지 */}
      <div className="flex-1 min-h-0">
        <AttendanceDataTable
          columns={columns}
          data={filteredStudents}
          isLoading={isLoading}
          onRowClick={handleRowClick}
          globalFilter={searchQuery}
          rowSelection={rowSelection}
          onRowSelectionChange={setRowSelection}
        />
      </div>

      {/* 상세 Sheet (오른쪽 사이드바) */}
      <AttendanceDetailSheet
        student={selectedStudent}
        dojangId={dojangId}
        startDate={startDate}
        endDate={endDate}
        isOpen={!!selectedStudent}
        onClose={handleCloseSheet}
        onStatusChange={handleStatusChange}
        onCheckout={handleCheckout}
        onCancelCheckout={handleCancelCheckout}
        onCreateRecord={handleCreateRecord}
        onTimeChange={handleTimeChange}
      />
    </div>
  );
}
