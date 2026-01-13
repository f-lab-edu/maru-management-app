import { Checkbox } from '../../../shared/components/ui/checkbox';
import { Label } from '../../../shared/components/ui/label';
import { Badge } from '../../../shared/components/ui/badge';
import { PermissionGroup } from '../constants/permissions';

interface PermissionGroupCardProps {
  group: PermissionGroup;
  selectedPermissions: Set<string>;
  onPermissionChange: (permissionKey: string, checked: boolean) => void;
  onGroupToggle: (groupKey: string, checked: boolean) => void;
}

export function PermissionGroupCard({
  group,
  selectedPermissions,
  onPermissionChange,
  onGroupToggle,
}: PermissionGroupCardProps) {
  const allSelected = group.permissions.every((p) => selectedPermissions.has(p.key));
  const someSelected = group.permissions.some((p) => selectedPermissions.has(p.key));
  const indeterminate = someSelected && !allSelected;

  return (
    <div className="rounded-lg border border-slate-200 bg-white">
      <div className="flex items-center justify-between px-4 py-3 border-b border-slate-100">
        <div className="flex items-center gap-3">
          <Checkbox
            id={`group-${group.key}`}
            checked={allSelected}
            ref={(el) => {
              if (el) {
                (el as unknown as HTMLInputElement).indeterminate = indeterminate;
              }
            }}
            onCheckedChange={(checked) => onGroupToggle(group.key, checked === true)}
          />
          <Label htmlFor={`group-${group.key}`} className="font-medium text-slate-900 cursor-pointer">
            {group.label}
          </Label>
        </div>
        <Badge variant="secondary" className="text-xs">
          {group.permissions.filter((p) => selectedPermissions.has(p.key)).length} / {group.permissions.length}
        </Badge>
      </div>
      <div className="p-4 space-y-3">
        {group.permissions.map((permission) => (
          <div key={permission.key} className="flex items-start gap-3">
            <Checkbox
              id={permission.key}
              checked={selectedPermissions.has(permission.key)}
              onCheckedChange={(checked) => onPermissionChange(permission.key, checked === true)}
              className="mt-0.5"
            />
            <div className="flex-1">
              <div className="flex items-center gap-2">
                <Label htmlFor={permission.key} className="text-sm text-slate-700 cursor-pointer">
                  {permission.label}
                </Label>
                {permission.defaultGranted && (
                  <Badge variant="outline" className="text-xs text-slate-400 border-slate-200">
                    기본
                  </Badge>
                )}
              </div>
              <p className="text-xs text-slate-400 mt-0.5">{permission.description}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
