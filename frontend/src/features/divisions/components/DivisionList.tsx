import { useState } from 'react';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragEndEvent,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Plus, Edit2, Trash2, Users, Loader2, Clock, GripVertical } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';
import { useConfirm, usePermissions } from '@/hooks';
import { useDivisions, useDeleteDivision, useReorderDivisions } from '../hooks';
import { DivisionCreateDialog } from './DivisionCreateDialog';
import { DivisionEditDialog } from './DivisionEditDialog';
import { DivisionEnrollmentSheet } from './DivisionEnrollmentSheet';
import { DayBadges } from './DayBadges';
import { formatTimeRange } from '../utils';
import type { DivisionRes } from '../types';

interface DivisionListProps {
  dojangId: string;
  sectionId: string;
  sectionName: string;
}

interface SortableDivisionItemProps {
  division: DivisionRes;
  onEdit: () => void;
  onDelete: () => void;
  onManageStudents: () => void;
  isDeleting: boolean;
  canManage?: boolean;
}

function SortableDivisionItem({
  division,
  onEdit,
  onDelete,
  onManageStudents,
  isDeleting,
  canManage = true,
}: SortableDivisionItemProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: division.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={cn(
        "p-4 rounded-lg border border-slate-100 bg-white hover:border-primary/20 transition-colors",
        isDragging && "opacity-50 bg-slate-100 shadow-lg"
      )}
    >
      <div className="flex items-start justify-between">
        <div className="flex items-start gap-3">
          <button
            type="button"
            className="cursor-grab active:cursor-grabbing text-slate-400 hover:text-slate-600 touch-none mt-1"
            {...attributes}
            {...listeners}
          >
            <GripVertical className="h-4 w-4" />
          </button>
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <span className="font-medium text-slate-900">{division.name}</span>
              <Badge variant="secondary" className="text-xs">
                <Users className="h-3 w-3 mr-1" />
                {division.studentCount}명
              </Badge>
            </div>
            <div className="flex flex-wrap items-center gap-3 text-sm text-slate-500">
              {division.scheduleDays.length > 0 && (
                <DayBadges selectedDays={division.scheduleDays} />
              )}
              {(division.startTime || division.endTime) && (
                <span className="flex items-center gap-1">
                  <Clock className="h-3.5 w-3.5" />
                  {formatTimeRange(division.startTime, division.endTime)}
                </span>
              )}
              {division.scheduleDays.length === 0 && !division.startTime && !division.endTime && (
                <span className="text-slate-400">시간표 미설정</span>
              )}
            </div>
          </div>
        </div>
        <div className="flex items-center gap-1">
          <Button
            size="sm"
            variant="outline"
            onClick={onManageStudents}
          >
            <Users className="h-4 w-4 mr-1" />
            원생
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="h-8 w-8"
            onClick={onEdit}
            disabled={!canManage}
          >
            <Edit2 className="h-4 w-4" />
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="h-8 w-8 text-red-500 hover:text-red-600 hover:bg-red-50"
            onClick={onDelete}
            disabled={!canManage || isDeleting}
          >
            {isDeleting ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Trash2 className="h-4 w-4" />
            )}
          </Button>
        </div>
      </div>
    </div>
  );
}

export function DivisionList({ dojangId, sectionId, sectionName }: DivisionListProps) {
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingDivision, setEditingDivision] = useState<DivisionRes | null>(null);
  const [enrollmentDivision, setEnrollmentDivision] = useState<DivisionRes | null>(null);

  const { data: divisionsData, isLoading } = useDivisions(dojangId, sectionId);
  const deleteDivision = useDeleteDivision(dojangId);
  const reorderDivisions = useReorderDivisions(dojangId);
  const { confirmDelete } = useConfirm();
  const { hasPermission } = usePermissions();
  const canManage = hasPermission('DOJANG_MANAGE_CLASS');

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const divisions = divisionsData?.divisions ?? [];

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (over && active.id !== over.id) {
      const oldIndex = divisions.findIndex((d) => d.id === active.id);
      const newIndex = divisions.findIndex((d) => d.id === over.id);
      const newOrder = arrayMove(divisions, oldIndex, newIndex);
      const divisionIds = newOrder.map((d) => d.id);

      reorderDivisions.mutate({ sectionId, divisionIds });
    }
  };

  const handleDelete = async (division: DivisionRes) => {
    const { isConfirmed } = await confirmDelete({
      title: '수련반 삭제',
      text: `"${division.name}" 수련반을 삭제하시겠습니까?`,
      confirmText: '삭제',
      cancelText: '취소',
    });

    if (!isConfirmed) return;

    try {
      await deleteDivision.mutateAsync(division.id);
    } catch (error) {
      console.error('수련반 삭제 실패:', error);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <>
      <Card className="border-none shadow-sm h-full">
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-base">
              수련반 ({sectionName})
            </CardTitle>
            <Button size="sm" onClick={() => setIsCreateOpen(true)} disabled={!canManage}>
              <Plus className="h-4 w-4 mr-1" />
              수련반 추가
            </Button>
          </div>
        </CardHeader>
        <CardContent className="pt-0">
          <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragEnd={handleDragEnd}
          >
            <SortableContext
              items={divisions.map((d) => d.id)}
              strategy={verticalListSortingStrategy}
            >
              <div className="space-y-3">
                {divisions.map((division) => (
                  <SortableDivisionItem
                    key={division.id}
                    division={division}
                    onEdit={() => setEditingDivision(division)}
                    onDelete={() => handleDelete(division)}
                    onManageStudents={() => setEnrollmentDivision(division)}
                    isDeleting={deleteDivision.isPending}
                    canManage={canManage}
                  />
                ))}
                {divisions.length === 0 && (
                  <div className="text-center py-12 text-slate-500">
                    등록된 수련반이 없습니다.
                    <br />
                    <span className="text-sm">수련반을 추가하여 원생을 배정하세요.</span>
                  </div>
                )}
              </div>
            </SortableContext>
          </DndContext>
        </CardContent>
      </Card>

      <DivisionCreateDialog
        dojangId={dojangId}
        sectionId={sectionId}
        open={isCreateOpen}
        onOpenChange={setIsCreateOpen}
      />

      <DivisionEditDialog
        dojangId={dojangId}
        division={editingDivision}
        open={!!editingDivision}
        onOpenChange={(open) => !open && setEditingDivision(null)}
      />

      <DivisionEnrollmentSheet
        dojangId={dojangId}
        division={enrollmentDivision}
        open={!!enrollmentDivision}
        onOpenChange={(open) => !open && setEnrollmentDivision(null)}
      />
    </>
  );
}
