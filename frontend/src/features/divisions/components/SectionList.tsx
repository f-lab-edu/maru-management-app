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
import { Plus, Edit2, Trash2, Loader2, GripVertical } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';
import { useConfirm, usePermissions } from '@/hooks';
import { useDeleteSection, useReorderSections } from '../hooks';
import { SectionCreateDialog } from './SectionCreateDialog';
import { SectionEditDialog } from './SectionEditDialog';
import type { SectionRes } from '../types';

interface SectionListProps {
  dojangId: string;
  sections: SectionRes[];
  selectedSectionId: string | null;
  onSelectSection: (sectionId: string | null) => void;
}

interface SortableSectionItemProps {
  section: SectionRes;
  isSelected: boolean;
  onSelect: () => void;
  onEdit: () => void;
  onDelete: () => void;
  isDeleting: boolean;
  canManage?: boolean;
}

function SortableSectionItem({
  section,
  isSelected,
  onSelect,
  onEdit,
  onDelete,
  isDeleting,
  canManage = true,
}: SortableSectionItemProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: section.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={cn(
        "group flex items-center justify-between p-3 rounded-lg cursor-pointer transition-colors",
        isSelected ? "bg-primary/10 text-primary" : "hover:bg-slate-50",
        isDragging && "opacity-50 bg-slate-100"
      )}
      onClick={onSelect}
    >
      <div className="flex items-center gap-2 min-w-0">
        <button
          type="button"
          className="cursor-grab active:cursor-grabbing text-slate-400 hover:text-slate-600 touch-none"
          {...attributes}
          {...listeners}
          onClick={(e) => e.stopPropagation()}
        >
          <GripVertical className="h-4 w-4" />
        </button>
        <span className="font-medium truncate">{section.name}</span>
        <Badge variant="secondary" className="text-xs shrink-0">
          {section.divisionCount}반
        </Badge>
      </div>
      <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
        <Button
          size="icon"
          variant="ghost"
          className="h-7 w-7"
          onClick={(e) => {
            e.stopPropagation();
            onEdit();
          }}
          disabled={!canManage}
        >
          <Edit2 className="h-3.5 w-3.5" />
        </Button>
        <Button
          size="icon"
          variant="ghost"
          className="h-7 w-7 text-red-500 hover:text-red-600 hover:bg-red-50"
          onClick={(e) => {
            e.stopPropagation();
            onDelete();
          }}
          disabled={!canManage || isDeleting}
        >
          {isDeleting ? (
            <Loader2 className="h-3.5 w-3.5 animate-spin" />
          ) : (
            <Trash2 className="h-3.5 w-3.5" />
          )}
        </Button>
      </div>
    </div>
  );
}

export function SectionList({
  dojangId,
  sections,
  selectedSectionId,
  onSelectSection,
}: SectionListProps) {
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingSection, setEditingSection] = useState<SectionRes | null>(null);
  const deleteSection = useDeleteSection(dojangId);
  const reorderSections = useReorderSections(dojangId);
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

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (over && active.id !== over.id) {
      const oldIndex = sections.findIndex((s) => s.id === active.id);
      const newIndex = sections.findIndex((s) => s.id === over.id);
      const newOrder = arrayMove(sections, oldIndex, newIndex);
      const sectionIds = newOrder.map((s) => s.id);

      reorderSections.mutate({ sectionIds });
    }
  };

  const handleDelete = async (section: SectionRes) => {
    const { isConfirmed } = await confirmDelete({
      title: '수련부 삭제',
      text: `"${section.name}" 수련부를 삭제하시겠습니까? 소속된 수련반이 있으면 삭제할 수 없습니다.`,
      confirmText: '삭제',
      cancelText: '취소',
    });

    if (!isConfirmed) return;

    try {
      await deleteSection.mutateAsync(section.id);
      if (selectedSectionId === section.id) {
        onSelectSection(null);
      }
    } catch (error) {
      console.error('수련부 삭제 실패:', error);
    }
  };

  return (
    <>
      <Card className="border-none shadow-sm h-full">
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-base">수련부</CardTitle>
            <Button size="sm" variant="outline" onClick={() => setIsCreateOpen(true)} disabled={!canManage}>
              <Plus className="h-4 w-4 mr-1" />
              추가
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
              items={sections.map((s) => s.id)}
              strategy={verticalListSortingStrategy}
            >
              <div className="space-y-1">
                {sections.map((section) => (
                  <SortableSectionItem
                    key={section.id}
                    section={section}
                    isSelected={selectedSectionId === section.id}
                    onSelect={() => onSelectSection(section.id)}
                    onEdit={() => setEditingSection(section)}
                    onDelete={() => handleDelete(section)}
                    isDeleting={deleteSection.isPending}
                    canManage={canManage}
                  />
                ))}
                {sections.length === 0 && (
                  <div className="text-center py-8 text-slate-500 text-sm">
                    등록된 수련부가 없습니다.
                  </div>
                )}
              </div>
            </SortableContext>
          </DndContext>
        </CardContent>
      </Card>

      <SectionCreateDialog
        dojangId={dojangId}
        open={isCreateOpen}
        onOpenChange={setIsCreateOpen}
      />

      <SectionEditDialog
        dojangId={dojangId}
        section={editingSection}
        open={!!editingSection}
        onOpenChange={(open) => !open && setEditingSection(null)}
      />
    </>
  );
}
