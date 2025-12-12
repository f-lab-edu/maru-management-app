import { useState } from 'react';
import { cn } from '../../../shared/utils';
import { SettingsTab } from '../types';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../../shared/components/ui/select';

interface SettingsLayoutProps {
  tabs: SettingsTab[];
}

export function SettingsLayout({ tabs }: SettingsLayoutProps) {
  const [activeTabId, setActiveTabId] = useState(tabs[0]?.id);
  const activeTab = tabs.find(tab => tab.id === activeTabId);

  return (
    <div className="flex flex-col lg:flex-row h-full bg-slate-50/50">
      {/* Mobile Dropdown - Hidden on lg+ */}
      <div className="lg:hidden bg-white border-b border-slate-200 p-4">
        <Select value={activeTabId} onValueChange={setActiveTabId}>
          <SelectTrigger className="w-full">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {tabs.map((tab) => (
              <SelectItem key={tab.id} value={tab.id}>
                <div className="flex items-center gap-2">
                  <tab.icon className="h-4 w-4" />
                  {tab.label}
                </div>
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {/* Desktop Sidebar - Hidden on mobile */}
      <div className="hidden lg:block w-64 shrink-0 border-r border-slate-200 bg-white p-6">
        <h2 className="text-lg font-bold text-slate-900 mb-6 px-2">설정</h2>
        <nav className="space-y-1">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTabId(tab.id)}
              className={cn(
                "w-full flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-lg transition-colors",
                activeTabId === tab.id
                  ? "bg-primary/10 text-primary"
                  : "text-slate-600 hover:bg-slate-50 hover:text-slate-900"
              )}
            >
              <tab.icon className="h-4 w-4" />
              {tab.label}
            </button>
          ))}
        </nav>
      </div>

      {/* Content Area */}
      <div className="flex-1 min-w-0 overflow-auto">
        <div className="max-w-4xl mx-auto p-4 lg:p-8">
          <div className="mb-6 lg:mb-8">
            <h1 className="text-xl lg:text-2xl font-bold text-slate-900">{activeTab?.label}</h1>
            {activeTab?.description && (
              <p className="text-sm lg:text-base text-slate-500 mt-1">{activeTab.description}</p>
            )}
          </div>

          <div className="animate-in fade-in slide-in-from-bottom-4 duration-300">
            {activeTab?.component}
          </div>
        </div>
      </div>
    </div>
  );
}
