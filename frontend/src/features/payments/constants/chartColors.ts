export const PAYMENT_COLORS = {
  paid: {
    main: '#10b981',
    light: '#d1fae5',
    dark: '#059669',
  },
  partial: {
    main: '#f59e0b',
    light: '#fef3c7',
    dark: '#d97706',
  },
  unpaid: {
    main: '#f43f5e',
    light: '#ffe4e6',
    dark: '#e11d48',
  },
  draft: {
    main: '#6b7280',
    light: '#f3f4f6',
    dark: '#4b5563',
  },
  void: {
    main: '#64748b',
    light: '#f1f5f9',
    dark: '#475569',
  },
} as const;

export const CHART_COLORS = {
  amount: '#3b82f6',
  paid: '#10b981',
  grid: '#e5e7eb',
  axis: '#9ca3af',
} as const;

export const PIE_CHART_COLORS = [
  PAYMENT_COLORS.paid.main,
  PAYMENT_COLORS.partial.main,
  PAYMENT_COLORS.unpaid.main,
] as const;
