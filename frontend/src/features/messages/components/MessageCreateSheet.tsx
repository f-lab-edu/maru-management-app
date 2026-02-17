import { useState, useEffect, useMemo } from 'react';
import { Loader2, Check, Search } from 'lucide-react';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui/sheet';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Textarea } from '@/shared/components/ui/textarea';
import { Label } from '@/shared/components/ui/label';
import { ToggleGroup, ToggleGroupItem } from '@/shared/components/ui/toggle-group';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components/ui/alert-dialog';
import { cn } from '@/shared/utils';
import { useAuthStore } from '@/stores/authStore';
import { useSections } from '@/features/divisions/hooks/useSections';
import { useDivisions } from '@/features/divisions/hooks/useDivisions';
import { useStudents } from '@/features/students/hooks/useStudents';
import { useCreateBroadcast, usePreviewRecipients } from '../hooks/useBroadcasts';
import { TITLE_MAX_LENGTH } from '../constants';
import type { RecipientType, MessageChannel, RecipientPreviewRequest } from '@/types/message';
import { CHANNEL_LABEL, RECIPIENT_TYPE_LABEL } from '@/types/message';

const CHANNELS: MessageChannel[] = ['SMS', 'KAKAO', 'EXPO_PUSH'];
const RECIPIENT_TYPES: RecipientType[] = ['ALL', 'SECTION', 'DIVISION', 'INDIVIDUAL'];

const MAX_BODY_BYTES = 2000;

const getByteLength = (str: string): number => new TextEncoder().encode(str).length;

interface MessageCreateSheetProps {
  isOpen: boolean;
  onClose: () => void;
}

export const MessageCreateSheet = ({ isOpen, onClose }: MessageCreateSheetProps) => {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? '';

  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [channel, setChannel] = useState<MessageChannel>('SMS');
  const [recipientType, setRecipientType] = useState<RecipientType>('ALL');
  const [selectedSectionIds, setSelectedSectionIds] = useState<string[]>([]);
  const [selectedDivisionIds, setSelectedDivisionIds] = useState<string[]>([]);
  const [selectedStudentIds, setSelectedStudentIds] = useState<string[]>([]);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [previewSectionId, setPreviewSectionId] = useState<string | null>(null);
  const [studentSearchQuery, setStudentSearchQuery] = useState('');

  const { data: sectionsData } = useSections(dojangId);
  const { data: divisionsData } = useDivisions(dojangId, previewSectionId);
  const { data: studentsData } = useStudents(dojangId);

  const previewMutation = usePreviewRecipients(dojangId);
  const createMutation = useCreateBroadcast(dojangId);

  const sections = sectionsData?.sections ?? [];
  const divisions = divisionsData?.divisions ?? [];
  const students = studentsData?.students ?? [];

  const filteredStudents = useMemo(() => {
    if (!studentSearchQuery) return students;
    const query = studentSearchQuery.toLowerCase();
    return students.filter((s) => s.name.toLowerCase().includes(query));
  }, [students, studentSearchQuery]);

  const bodyBytes = getByteLength(body);
  const isOverLimit = bodyBytes > MAX_BODY_BYTES;

  const resetForm = () => {
    setTitle('');
    setBody('');
    setChannel('SMS');
    setRecipientType('ALL');
    setSelectedSectionIds([]);
    setSelectedDivisionIds([]);
    setSelectedStudentIds([]);
    setPreviewSectionId(null);
    setStudentSearchQuery('');
    previewMutation.reset();
    createMutation.reset();
  };

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      const swalContainer = document.querySelector('.swal2-container');
      if (swalContainer) return;
      resetForm();
      onClose();
    }
  };

  const handleInteractOutside = (e: Event) => {
    const target = e.target as HTMLElement;
    if (
      target.closest('tr') ||
      target.closest('button') ||
      target.closest('[role="dialog"]') ||
      target.closest('[data-radix-select-content]') ||
      target.closest('[data-radix-popper-content-wrapper]')
    ) {
      e.preventDefault();
    }
  };

  const buildPreviewRequest = (): RecipientPreviewRequest => {
    const req: RecipientPreviewRequest = { recipientType };
    if (recipientType === 'SECTION') req.sectionIds = selectedSectionIds;
    if (recipientType === 'DIVISION') req.divisionIds = selectedDivisionIds;
    if (recipientType === 'INDIVIDUAL') req.studentIds = selectedStudentIds;
    return req;
  };

  const hasRecipientSelection = () => {
    switch (recipientType) {
      case 'ALL': return true;
      case 'SECTION': return selectedSectionIds.length > 0;
      case 'DIVISION': return selectedDivisionIds.length > 0;
      case 'INDIVIDUAL': return selectedStudentIds.length > 0;
    }
  };

  const canSubmit = title.trim() && body.trim() && !isOverLimit && hasRecipientSelection() && !createMutation.isPending;

  const handleRecipientTypeChange = (type: RecipientType) => {
    setRecipientType(type);
    setSelectedSectionIds([]);
    setSelectedDivisionIds([]);
    setSelectedStudentIds([]);
  };

  const handleSubmit = () => {
    if (!canSubmit) return;
    setConfirmOpen(true);
  };

  const handleConfirmSend = () => {
    setConfirmOpen(false);
    createMutation.mutate(
      {
        title: title.trim(),
        body: body.trim(),
        channel,
        recipientType,
        ...(recipientType === 'SECTION' && { sectionIds: selectedSectionIds }),
        ...(recipientType === 'DIVISION' && { divisionIds: selectedDivisionIds }),
        ...(recipientType === 'INDIVIDUAL' && { studentIds: selectedStudentIds }),
      },
      {
        onSuccess: () => {
          resetForm();
          onClose();
        },
      },
    );
  };

  const toggleSectionId = (id: string) => {
    setSelectedSectionIds((prev) =>
      prev.includes(id) ? prev.filter((v) => v !== id) : [...prev, id],
    );
  };

  const toggleDivisionId = (id: string) => {
    setSelectedDivisionIds((prev) =>
      prev.includes(id) ? prev.filter((v) => v !== id) : [...prev, id],
    );
  };

  const toggleStudentId = (id: string) => {
    setSelectedStudentIds((prev) =>
      prev.includes(id) ? prev.filter((v) => v !== id) : [...prev, id],
    );
  };

  useEffect(() => {
    if (!hasRecipientSelection()) {
      previewMutation.reset();
      return;
    }
    const timer = setTimeout(() => {
      previewMutation.mutate(buildPreviewRequest());
    }, 300);
    return () => clearTimeout(timer);
  }, [recipientType, selectedSectionIds, selectedDivisionIds, selectedStudentIds]);

  return (
    <>
      <Sheet open={isOpen} onOpenChange={handleOpenChange} modal={false}>
        <SheetContent
          side="right"
          className="w-[90vw] overflow-y-auto sm:w-[480px] sm:max-w-[480px] z-50"
          hideOverlay
          onInteractOutside={handleInteractOutside}
          onPointerDownOutside={handleInteractOutside}
          aria-describedby={undefined}
        >
          <SheetHeader>
            <SheetTitle>새 문자 발송</SheetTitle>
          </SheetHeader>

          <div className="mt-6 space-y-6">
            {/* 제목 */}
            <div className="space-y-2">
              <Label>제목</Label>
              <Input
                placeholder="문자 제목을 입력하세요"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                maxLength={TITLE_MAX_LENGTH}
              />
            </div>

            {/* 발송 설정 */}
            <div className="border rounded-lg p-4 space-y-4">
              <div className="space-y-2">
                <Label className="text-xs text-muted-foreground">발송 채널</Label>
                <ToggleGroup
                  type="single"
                  value={channel}
                  onValueChange={(v) => v && setChannel(v as MessageChannel)}
                  className="justify-start"
                >
                  {CHANNELS.map((ch) => (
                    <ToggleGroupItem
                      key={ch}
                      value={ch}
                      className="text-xs px-3 py-1 h-7 data-[state=on]:bg-foreground data-[state=on]:text-background data-[state=on]:font-semibold"
                    >
                      {CHANNEL_LABEL[ch]}
                    </ToggleGroupItem>
                  ))}
                </ToggleGroup>
              </div>

              <div className="border-t pt-4 space-y-3">
                <Label className="text-xs text-muted-foreground">수신 대상</Label>
                <ToggleGroup
                  type="single"
                  value={recipientType}
                  onValueChange={(v) => v && handleRecipientTypeChange(v as RecipientType)}
                  className="justify-start"
                >
                  {RECIPIENT_TYPES.map((type) => (
                    <ToggleGroupItem
                      key={type}
                      value={type}
                      className="text-xs px-3 py-1 h-7 data-[state=on]:bg-foreground data-[state=on]:text-background data-[state=on]:font-semibold"
                    >
                      {RECIPIENT_TYPE_LABEL[type]}
                    </ToggleGroupItem>
                  ))}
                </ToggleGroup>

                {/* 수련부 선택 — 토글 칩 */}
                {recipientType === 'SECTION' && (
                  <div className="space-y-2">
                    {sections.length === 0 ? (
                      <p className="text-sm text-muted-foreground">등록된 수련부가 없습니다</p>
                    ) : (
                      <div className="flex flex-wrap gap-2">
                        {sections.map((section) => {
                          const selected = selectedSectionIds.includes(section.id);
                          return (
                            <button
                              key={section.id}
                              type="button"
                              onClick={() => toggleSectionId(section.id)}
                              className={cn(
                                'inline-flex items-center gap-1.5 rounded-md border px-3 py-1.5 text-sm transition-colors',
                                selected
                                  ? 'bg-foreground text-background border-foreground font-semibold'
                                  : 'bg-background hover:bg-accent',
                              )}
                            >
                              {selected && <Check className="h-3 w-3" />}
                              {section.name}
                            </button>
                          );
                        })}
                      </div>
                    )}
                  </div>
                )}

                {/* 수련반 선택 — 수련부 Select + 토글 칩 */}
                {recipientType === 'DIVISION' && (
                  <div className="space-y-3">
                    <Select
                      value={previewSectionId ?? ''}
                      onValueChange={(v) => setPreviewSectionId(v)}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="수련부를 먼저 선택해주세요" />
                      </SelectTrigger>
                      <SelectContent>
                        {sections.map((section) => (
                          <SelectItem key={section.id} value={section.id}>
                            {section.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {previewSectionId && (
                      <div className="space-y-2">
                        {divisions.length === 0 ? (
                          <p className="text-sm text-muted-foreground">등록된 수련반이 없습니다</p>
                        ) : (
                          <div className="flex flex-wrap gap-2">
                            {divisions.map((division) => {
                              const selected = selectedDivisionIds.includes(division.id);
                              return (
                                <button
                                  key={division.id}
                                  type="button"
                                  onClick={() => toggleDivisionId(division.id)}
                                  className={cn(
                                    'inline-flex items-center gap-1.5 rounded-md border px-3 py-1.5 text-sm transition-colors',
                                    selected
                                      ? 'bg-foreground text-background border-foreground font-semibold'
                                      : 'bg-background hover:bg-accent',
                                  )}
                                >
                                  {selected && <Check className="h-3 w-3" />}
                                  {division.name}
                                </button>
                              );
                            })}
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                )}

                {/* 개별 선택 — 인라인 검색 리스트 */}
                {recipientType === 'INDIVIDUAL' && (
                  <div className="space-y-2">
                    {students.length === 0 ? (
                      <p className="text-sm text-muted-foreground">등록된 원생이 없습니다</p>
                    ) : (
                      <>
                        <div className="relative">
                          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                          <Input
                            placeholder="이름으로 검색..."
                            value={studentSearchQuery}
                            onChange={(e) => setStudentSearchQuery(e.target.value)}
                            className="pl-9"
                          />
                        </div>
                        <div className="max-h-48 overflow-y-auto rounded-md border">
                          {filteredStudents.length === 0 ? (
                            <p className="py-4 text-center text-sm text-muted-foreground">
                              검색 결과가 없습니다
                            </p>
                          ) : (
                            filteredStudents.map((student) => {
                              const selected = selectedStudentIds.includes(student.id);
                              return (
                                <button
                                  key={student.id}
                                  type="button"
                                  onClick={() => toggleStudentId(student.id)}
                                  className={cn(
                                    'flex w-full items-center gap-2 px-3 py-2 text-sm transition-colors hover:bg-accent',
                                    selected && 'bg-accent/50',
                                  )}
                                >
                                  <Check
                                    className={cn(
                                      'h-4 w-4 shrink-0',
                                      selected ? 'opacity-100' : 'opacity-0',
                                    )}
                                  />
                                  {student.name}
                                </button>
                              );
                            })
                          )}
                        </div>
                        {selectedStudentIds.length > 0 && (
                          <p className="text-xs text-muted-foreground">{selectedStudentIds.length}명 선택됨</p>
                        )}
                      </>
                    )}
                  </div>
                )}

                {(previewMutation.isPending || previewMutation.data) && (
                  <div className="text-sm text-muted-foreground flex items-center gap-2 border-t pt-3">
                    {previewMutation.isPending && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
                    {previewMutation.data && (
                      <span>
                        수신 예상: {previewMutation.data.estimatedMessageCount}명
                        {previewMutation.data.skippedNoGuardian > 0 && (
                          <span className="text-yellow-600 ml-2">
                            (보호자 미등록 {previewMutation.data.skippedNoGuardian}명 제외)
                          </span>
                        )}
                        {previewMutation.data.skippedNoPhone > 0 && (
                          <span className="text-yellow-600 ml-2">
                            (연락처 없음 {previewMutation.data.skippedNoPhone}명 제외)
                          </span>
                        )}
                      </span>
                    )}
                  </div>
                )}
              </div>
            </div>

            {/* 본문 */}
            <div className="space-y-2">
              <Label>본문</Label>
              <Textarea
                placeholder="문자 내용을 입력하세요"
                value={body}
                onChange={(e) => setBody(e.target.value)}
                rows={6}
              />
              <p className={cn('text-xs', isOverLimit ? 'text-red-600' : 'text-muted-foreground')}>
                {bodyBytes} / {MAX_BODY_BYTES} byte
              </p>
            </div>

            {/* 발송 버튼 */}
            <div className="flex gap-2 pt-4">
              <Button type="button" variant="outline" onClick={onClose} className="flex-1">
                취소
              </Button>
              <Button onClick={handleSubmit} disabled={!canSubmit} className="flex-1">
                {createMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                발송하기
              </Button>
            </div>

            {createMutation.isError && (
              <p className="text-sm text-red-600">발송에 실패했습니다. 다시 시도해주세요.</p>
            )}
          </div>
        </SheetContent>
      </Sheet>

      {/* 발송 확인 모달 */}
      <AlertDialog open={confirmOpen} onOpenChange={setConfirmOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>문자 발송 확인</AlertDialogTitle>
            <AlertDialogDescription>
              {previewMutation.data
                ? `${previewMutation.data.estimatedMessageCount}명에게 발송합니다. 발송하시겠습니까?`
                : '선택한 수신자에게 발송합니다. 발송하시겠습니까?'}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>취소</AlertDialogCancel>
            <AlertDialogAction onClick={handleConfirmSend}>발송</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
};
