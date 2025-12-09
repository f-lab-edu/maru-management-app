import Swal from 'sweetalert2';
import withReactContent from 'sweetalert2-react-content';

const MySwal = withReactContent(Swal);

type ConfirmType = 'warning' | 'danger' | 'info';

interface ConfirmOptions {
  title: string;
  text?: string;
  confirmText?: string;
  cancelText?: string;
  type?: ConfirmType;
}

interface ConfirmResult {
  isConfirmed: boolean;
}

const CONFIRM_BUTTON_COLORS: Record<ConfirmType, string> = {
  warning: '#f59e0b',
  danger: '#ef4444',
  info: '#3b82f6',
};

export function useConfirm() {
  const confirm = async ({
    title,
    text,
    confirmText = '확인',
    cancelText = '취소',
    type = 'warning',
  }: ConfirmOptions): Promise<ConfirmResult> => {
    const result = await MySwal.fire({
      title,
      text,
      icon: type === 'danger' ? 'warning' : type,
      showCancelButton: true,
      confirmButtonText: confirmText,
      cancelButtonText: cancelText,
      confirmButtonColor: CONFIRM_BUTTON_COLORS[type],
      cancelButtonColor: '#6b7280',
      reverseButtons: true,
      returnFocus: false,
    });

    return { isConfirmed: result.isConfirmed };
  };

  const confirmDelete = (message: ConfirmOptions) =>
    confirm({ ...message, type: 'danger' });

  const confirmWarning = (message: ConfirmOptions) =>
    confirm({ ...message, type: 'warning' });

  const confirmInfo = (message: ConfirmOptions) =>
    confirm({ ...message, type: 'info' });

  return {
    confirm,
    confirmDelete,
    confirmWarning,
    confirmInfo,
  };
}
