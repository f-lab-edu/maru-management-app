import { useMemo, useState } from 'react';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui/sheet';
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { Input } from '@/shared/components/ui/input';
import { LogOut, ChevronRight, Clock, Plus, Undo2 } from 'lucide-react';
import { AttendanceStatusBadge } from './AttendanceStatusBadge';
import { formatDateHeader, generateDatesInRange, isToday, formatToISO } from '../utils/dateUtils';
import { ATTENDANCE_STATUS_LABELS, ATTENDANCE_STATUS_COLORS } from '../constants';
import type { StudentAttendanceRow, AttendanceStatus, AttendanceInfo } from '../types';
import { cn } from '@/shared/utils';
import { useStudentAttendance } from '../hooks/useStudentAttendance';

interface AttendanceDetailSheetProps {
  student: StudentAttendanceRow | null;
  dojangId: string | null;
  startDate: string;
  endDate: string;
  isOpen: boolean;
  onClose: () => void;
  onStatusChange?: (attendanceId: string, newStatus: AttendanceStatus) => Promise<void>;
  onCheckout?: (attendanceId: string) => Promise<void>;
  onCancelCheckout?: (attendanceId: string) => Promise<void>;
  onCreateRecord?: (studentId: string, date: string, status: AttendanceStatus) => Promise<void>;
  onTimeChange?: (attendanceId: string, checkinAt?: string, checkoutAt?: string) => Promise<void>;
  canUpdate?: boolean;
}

interface AttendanceHistoryItem {
  date: string;
  attendanceId: string | null;
  status: AttendanceStatus | null;
  checkinAt: string | null;
  checkoutAt: string | null;
  isToday: boolean;
}

const STATUS_OPTIONS: { value: AttendanceStatus; label: string }[] = [
  { value: 'PRESENT', label: '출석' },
  { value: 'ABSENT', label: '결석' },
  { value: 'SICK', label: '병결' },
  { value: 'EXCUSED', label: '공결' },
];

const CREATE_STATUS_OPTIONS: { value: AttendanceStatus; label: string }[] = [
  { value: 'PRESENT', label: '출석' },
  { value: 'ABSENT', label: '결석' },
  { value: 'SICK', label: '병결' },
  { value: 'EXCUSED', label: '공결' },
];

function isPastOrToday(dateStr: string): boolean {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const date = new Date(dateStr);
  date.setHours(0, 0, 0, 0);
  return date <= today;
}

function SheetSkeleton() {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Skeleton className="h-16 w-16 rounded-full" />
        <div className="space-y-2">
          <Skeleton className="h-6 w-32" />
          <Skeleton className="h-4 w-24" />
        </div>
      </div>
      <div className="space-y-4">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-32 w-full" />
      </div>
    </div>
  );
}

function getThisMonthRange(): { startDate: string; endDate: string; label: string } {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth();

  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);

  return {
    startDate: formatToISO(firstDay),
    endDate: formatToISO(lastDay),
    label: `${year}년 ${month + 1}월`,
  };
}

export function AttendanceDetailSheet({
  student,
  dojangId,
  isOpen,
  onClose,
  onStatusChange,
  onCheckout,
  onCancelCheckout,
  onCreateRecord,
  onTimeChange,
  canUpdate = true,
}: AttendanceDetailSheetProps) {
  const thisMonth = useMemo(() => getThisMonthRange(), []);
  const [loadingAction, setLoadingAction] = useState<string | null>(null);
  const [expandedDate, setExpandedDate] = useState<string | null>(null);
  const [editingTime, setEditingTime] = useState<{
    attendanceId: string;
    checkinAt: string;
    checkoutAt: string;
  } | null>(null);

  // 이번 달 전체 출석 데이터 조회 (부모의 조회 범위와 무관하게)
  const { data: monthlyHistory, isLoading: isLoadingHistory } = useStudentAttendance({
    dojangId,
    studentId: student?.id ?? null,
    startDate: thisMonth.startDate,
    endDate: thisMonth.endDate,
  });

  // monthlyHistory를 Map으로 변환 (date → AttendanceInfo)
  const monthlyAttendanceMap = useMemo((): Record<string, AttendanceInfo> => {
    if (!monthlyHistory) return {};
    return monthlyHistory.reduce((acc, record) => {
      // attendanceDate가 있으면 사용, 없으면 checkinAt에서 추출
      const dateStr = record.attendanceDate ?? record.checkinAt?.split('T')[0];
      if (!dateStr) return acc;
      acc[dateStr] = {
        id: record.id,
        status: record.status,
        checkinAt: record.checkinAt,
        checkoutAt: record.checkoutAt,
      };
      return acc;
    }, {} as Record<string, AttendanceInfo>);
  }, [monthlyHistory]);

  const attendanceHistory = useMemo((): AttendanceHistoryItem[] => {
    if (!student) return [];

    const dates = generateDatesInRange(thisMonth.startDate, thisMonth.endDate);
    return dates.map((date) => {
      // monthlyAttendanceMap 우선, 없으면 student.attendances 사용
      const attendance = monthlyAttendanceMap[date] ?? student.attendances[date];
      return {
        date,
        attendanceId: attendance?.id ?? null,
        status: attendance?.status ?? null,
        checkinAt: attendance?.checkinAt ?? null,
        checkoutAt: attendance?.checkoutAt ?? null,
        isToday: isToday(date),
      };
    });
  }, [student, thisMonth, monthlyAttendanceMap]);

  const stats = useMemo(() => {
    if (!attendanceHistory.length) {
      return { present: 0, absent: 0, sick: 0, excused: 0, total: 0 };
    }

    const counts = { present: 0, absent: 0, sick: 0, excused: 0, total: 0 };

    attendanceHistory.forEach((item) => {
      if (item.status) {
        counts.total++;
        switch (item.status) {
          case 'PRESENT':
            counts.present++;
            break;
          case 'ABSENT':
            counts.absent++;
            break;
          case 'SICK':
            counts.sick++;
            break;
          case 'EXCUSED':
            counts.excused++;
            break;
        }
      }
    });

    return counts;
  }, [attendanceHistory]);

  const attendanceRate = stats.total > 0 ? Math.round((stats.present / stats.total) * 100) : 0;

  const formatTime = (isoString: string | null): string => {
    if (!isoString) return '-';
    const date = new Date(isoString);
    return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
  };

  const handleStatusChange = async (attendanceId: string | null, newStatus: AttendanceStatus) => {
    if (!attendanceId || !onStatusChange) return;
    setLoadingAction(`status-${attendanceId}`);
    try {
      await onStatusChange(attendanceId, newStatus);
    } finally {
      setLoadingAction(null);
    }
  };

  const handleCheckout = async (attendanceId: string | null) => {
    if (!attendanceId || !onCheckout) return;
    setLoadingAction(`checkout-${attendanceId}`);
    try {
      await onCheckout(attendanceId);
    } finally {
      setLoadingAction(null);
    }
  };

  const handleCancelCheckout = async (attendanceId: string | null) => {
    if (!attendanceId || !onCancelCheckout) return;
    setLoadingAction(`cancel-checkout-${attendanceId}`);
    try {
      await onCancelCheckout(attendanceId);
    } finally {
      setLoadingAction(null);
    }
  };

  const handleCreateRecord = async (date: string, status: AttendanceStatus) => {
    if (!student || !onCreateRecord) return;
    setLoadingAction(`create-${date}`);
    try {
      await onCreateRecord(student.id, date, status);
    } finally {
      setLoadingAction(null);
    }
  };

  const startEditingTime = (attendanceId: string, checkinAt: string | null, checkoutAt: string | null) => {
    const formatForInput = (isoString: string | null): string => {
      if (!isoString) return '';
      const date = new Date(isoString);
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      return `${hours}:${minutes}`;
    };
    setEditingTime({
      attendanceId,
      checkinAt: formatForInput(checkinAt),
      checkoutAt: formatForInput(checkoutAt),
    });
  };

  const buildISOTime = (date: string, time: string): string | undefined => {
    if (!time) return undefined;
    return `${date}T${time}:00`;
  };

  const handleInteractOutside = (e: Event) => {
    // SweetAlert가 열려있으면 외부 클릭 무시
    const swalContainer = document.querySelector('.swal2-container');
    if (swalContainer) {
      e.preventDefault();
    }
  };

  return (
    <Sheet open={isOpen} onOpenChange={(open) => !open && onClose()} modal={false}>
      <SheetContent
        side="right"
        className="w-[90vw] sm:w-[580px] md:w-[640px] flex flex-col"
        onInteractOutside={handleInteractOutside}
        onPointerDownOutside={handleInteractOutside}
      >
        {!student ? (
          <SheetSkeleton />
        ) : (
          <>
            <SheetHeader className="space-y-1 pt-4 shrink-0">
              <div className="flex items-center gap-4">
                <Avatar className="h-14 w-14">
                  <AvatarImage src={student.photoUrl ?? undefined} alt={student.name} />
                  <AvatarFallback className="bg-primary/10 text-primary text-lg font-medium">
                    {student.name.slice(0, 2)}
                  </AvatarFallback>
                </Avatar>
                <div>
                  <SheetTitle className="text-xl">{student.name}</SheetTitle>
                  <p className="text-sm text-muted-foreground">{student.className}</p>
                </div>
              </div>
            </SheetHeader>

            <div className="mt-6 flex flex-col flex-1 min-h-0 gap-6">
              {/* 출석 통계 */}
              <div className="grid grid-cols-4 gap-2 shrink-0">
                <div className="text-center p-3 border rounded-lg bg-card">
                  <div className="text-xl font-bold">{stats.present}</div>
                  <div className="text-[10px] text-muted-foreground">{ATTENDANCE_STATUS_LABELS.PRESENT}</div>
                </div>
                <div className="text-center p-3 border rounded-lg bg-card">
                  <div className="text-xl font-bold">{stats.absent}</div>
                  <div className="text-[10px] text-muted-foreground">{ATTENDANCE_STATUS_LABELS.ABSENT}</div>
                </div>
                <div className="text-center p-3 border rounded-lg bg-card">
                  <div className="text-xl font-bold">{stats.sick}</div>
                  <div className="text-[10px] text-muted-foreground">{ATTENDANCE_STATUS_LABELS.SICK}</div>
                </div>
                <div className="text-center p-3 border rounded-lg bg-card">
                  <div className="text-xl font-bold">{attendanceRate}%</div>
                  <div className="text-[10px] text-muted-foreground">출석률</div>
                </div>
              </div>

              {/* 출석 이력 */}
              <div className="flex flex-col flex-1 min-h-0">
                <div className="flex items-center justify-between shrink-0 mb-3">
                  <h4 className="font-medium">출석 이력</h4>
                  <Badge variant="outline" className="text-xs">
                    {thisMonth.label}
                  </Badge>
                </div>

                <div className="flex-1 overflow-y-auto rounded-md border">
                  {isLoadingHistory ? (
                    <div className="p-4 space-y-3">
                      {Array.from({ length: 7 }).map((_, i) => (
                        <Skeleton key={i} className="h-10 w-full" />
                      ))}
                    </div>
                  ) : (
                  <div className="p-2 space-y-1">
                    {attendanceHistory.map((item) => {
                      const hasRecord = item.status !== null && item.attendanceId !== null;
                      const canExpand = hasRecord || (!hasRecord && isPastOrToday(item.date));
                      const canCheckout = item.isToday && item.status === 'PRESENT' && !item.checkoutAt && hasRecord;
                      const canCancelCheckout = item.isToday && item.status === 'PRESENT' && !!item.checkoutAt && hasRecord;
                      const isStatusLoading = loadingAction === `status-${item.attendanceId}`;
                      const isCheckoutLoading = loadingAction === `checkout-${item.attendanceId}`;
                      const isCancelCheckoutLoading = loadingAction === `cancel-checkout-${item.attendanceId}`;
                      const isCreateLoading = loadingAction === `create-${item.date}`;
                      const isTimeLoading = loadingAction === `time-${item.attendanceId}`;
                      const isExpanded = expandedDate === item.date && canExpand;
                      const isEditingThisTime = editingTime?.attendanceId === item.attendanceId;
                      const showTime = item.status === 'PRESENT';

                      return (
                        <div key={item.date} className="rounded-md overflow-hidden">
                          {/* 메인 행 */}
                          <div
                            className={cn(
                              'flex items-center justify-between py-3 px-3 transition-colors',
                              canExpand && 'cursor-pointer',
                              item.isToday
                                ? 'bg-primary/10 ring-1 ring-primary/30'
                                : canExpand ? 'hover:bg-muted/50' : '',
                              isExpanded && 'bg-muted/30'
                            )}
                            onClick={() => canExpand && setExpandedDate(isExpanded ? null : item.date)}
                          >
                            <div className="flex items-center gap-2 min-w-0">
                              {canExpand && (
                                <ChevronRight
                                  className={cn(
                                    'h-4 w-4 text-muted-foreground transition-transform shrink-0',
                                    isExpanded && 'rotate-90'
                                  )}
                                />
                              )}
                              {item.isToday && (
                                <span className="text-[10px] font-medium text-primary bg-primary/20 px-1.5 py-0.5 rounded shrink-0">
                                  오늘
                                </span>
                              )}
                              <span className={cn('text-sm font-medium shrink-0', item.isToday && 'text-primary')}>
                                {formatDateHeader(item.date)}
                              </span>
                            </div>
                            <div className="flex items-center gap-1.5 shrink-0">
                              {/* 출석 시간 - 출석(PRESENT)일 때만 표시 */}
                              {showTime && item.checkinAt && (
                                <span className="text-[11px] text-blue-600 font-medium">
                                  {formatTime(item.checkinAt)}
                                </span>
                              )}
                              {/* 구분선 */}
                              {showTime && item.checkinAt && item.checkoutAt && (
                                <span className="text-muted-foreground">→</span>
                              )}
                              {/* 퇴관 시간 */}
                              {showTime && item.checkoutAt && (
                                <span className="text-[11px] text-gray-500 font-medium">
                                  {formatTime(item.checkoutAt)}
                                </span>
                              )}
                              {/* 상태 뱃지 */}
                              <AttendanceStatusBadge status={item.status} checkoutAt={item.checkoutAt} date={item.date} size="sm" />
                            </div>
                          </div>

                          {/* 확장 영역 - 애니메이션 */}
                          <div
                            className={cn(
                              'grid transition-[grid-template-rows] duration-200 ease-out',
                              isExpanded ? 'grid-rows-[1fr]' : 'grid-rows-[0fr]'
                            )}
                          >
                            <div className="overflow-hidden">
                              <div className="px-3 py-3 bg-muted/20 border-t space-y-3">
                              {/* 기존 기록이 있는 경우: 상태 변경 */}
                              {hasRecord && onStatusChange && (
                                <div className="space-y-2">
                                  <p className="text-xs text-muted-foreground font-medium">상태 변경</p>
                                  <div className="flex gap-2">
                                    {STATUS_OPTIONS.map((option) => {
                                      const isCurrentStatus = item.status === option.value;
                                      const colorClass = ATTENDANCE_STATUS_COLORS[option.value];
                                      return (
                                        <Button
                                          key={option.value}
                                          variant={isCurrentStatus ? 'default' : 'outline'}
                                          size="sm"
                                          className={cn(
                                            'h-8 text-xs flex-1',
                                            isCurrentStatus && colorClass
                                          )}
                                          disabled={!canUpdate || isCurrentStatus || isStatusLoading}
                                          onClick={(e) => {
                                            e.stopPropagation();
                                            handleStatusChange(item.attendanceId, option.value);
                                          }}
                                        >
                                          {option.label}
                                          {isCurrentStatus && ' ✓'}
                                        </Button>
                                      );
                                    })}
                                  </div>
                                </div>
                              )}

                              {/* 기존 기록이 없는 경우: 기록 생성 */}
                              {!hasRecord && onCreateRecord && (
                                <div className="space-y-2">
                                  <p className="text-xs text-muted-foreground font-medium">출석 기록 생성</p>
                                  <div className="flex gap-2">
                                    {CREATE_STATUS_OPTIONS.map((option) => {
                                      const colorClass = ATTENDANCE_STATUS_COLORS[option.value];
                                      return (
                                        <Button
                                          key={option.value}
                                          variant="outline"
                                          size="sm"
                                          className={cn('h-8 text-xs flex-1', colorClass)}
                                          disabled={!canUpdate || isCreateLoading}
                                          onClick={(e) => {
                                            e.stopPropagation();
                                            handleCreateRecord(item.date, option.value);
                                          }}
                                        >
                                          <Plus className="h-3 w-3 mr-1" />
                                          {option.label}
                                        </Button>
                                      );
                                    })}
                                  </div>
                                </div>
                              )}

                              {/* 시간 변경 - 출석(PRESENT)이고 기록이 있을 때만 */}
                              {hasRecord && showTime && onTimeChange && (
                                <div className="pt-2 border-t space-y-2">
                                  <div className="flex items-center justify-between">
                                    <p className="text-xs text-muted-foreground font-medium">시간 변경</p>
                                    {!isEditingThisTime && (
                                      <Button
                                        variant="ghost"
                                        size="sm"
                                        className="h-6 text-xs"
                                        disabled={!canUpdate}
                                        onClick={(e) => {
                                          e.stopPropagation();
                                          startEditingTime(item.attendanceId!, item.checkinAt, item.checkoutAt);
                                        }}
                                      >
                                        <Clock className="h-3 w-3 mr-1" />
                                        수정
                                      </Button>
                                    )}
                                  </div>
                                  {isEditingThisTime && editingTime && (
                                    <div className="space-y-2">
                                      <div className="flex gap-2 items-center">
                                        <div className="flex-1">
                                          <label className="text-[10px] text-muted-foreground">출석 시간</label>
                                          <Input
                                            type="time"
                                            value={editingTime.checkinAt}
                                            onChange={(e) =>
                                              setEditingTime({ ...editingTime, checkinAt: e.target.value })
                                            }
                                            className="h-8 text-xs"
                                            onClick={(e) => e.stopPropagation()}
                                          />
                                        </div>
                                        <div className="flex-1">
                                          <label className="text-[10px] text-muted-foreground">퇴관 시간</label>
                                          <Input
                                            type="time"
                                            value={editingTime.checkoutAt}
                                            onChange={(e) =>
                                              setEditingTime({ ...editingTime, checkoutAt: e.target.value })
                                            }
                                            className="h-8 text-xs"
                                            onClick={(e) => e.stopPropagation()}
                                          />
                                        </div>
                                      </div>
                                      <div className="flex gap-2">
                                        <Button
                                          variant="outline"
                                          size="sm"
                                          className="h-7 text-xs flex-1"
                                          onClick={(e) => {
                                            e.stopPropagation();
                                            setEditingTime(null);
                                          }}
                                        >
                                          취소
                                        </Button>
                                        <Button
                                          size="sm"
                                          className="h-7 text-xs flex-1"
                                          disabled={!canUpdate || isTimeLoading}
                                          onClick={(e) => {
                                            e.stopPropagation();
                                            onTimeChange(
                                              item.attendanceId!,
                                              buildISOTime(item.date, editingTime.checkinAt),
                                              buildISOTime(item.date, editingTime.checkoutAt)
                                            ).then(() => setEditingTime(null));
                                          }}
                                        >
                                          {isTimeLoading ? '처리 중...' : '저장'}
                                        </Button>
                                      </div>
                                    </div>
                                  )}
                                </div>
                              )}

                              {/* 퇴관 버튼 */}
                              {canCheckout && onCheckout && (
                                <div className="pt-2 border-t">
                                  <Button
                                    variant="outline"
                                    size="sm"
                                    className="h-8 w-full"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleCheckout(item.attendanceId);
                                    }}
                                    disabled={!canUpdate || isCheckoutLoading}
                                  >
                                    <LogOut className="h-4 w-4 mr-2" />
                                    {isCheckoutLoading ? '처리 중...' : '퇴관 처리'}
                                  </Button>
                                </div>
                              )}

                              {/* 퇴관 취소 버튼 */}
                              {canCancelCheckout && onCancelCheckout && (
                                <div className="pt-2 border-t">
                                  <Button
                                    variant="ghost"
                                    size="sm"
                                    className="h-8 w-full text-muted-foreground"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleCancelCheckout(item.attendanceId);
                                    }}
                                    disabled={!canUpdate || isCancelCheckoutLoading}
                                  >
                                    <Undo2 className="h-4 w-4 mr-2" />
                                    {isCancelCheckoutLoading ? '처리 중...' : '퇴관 취소'}
                                  </Button>
                                </div>
                              )}
                              </div>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                  )}
                </div>
              </div>
            </div>
          </>
        )}
      </SheetContent>
    </Sheet>
  );
}
