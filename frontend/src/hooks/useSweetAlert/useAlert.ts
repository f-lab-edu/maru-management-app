import Swal from 'sweetalert2';
import withReactContent from 'sweetalert2-react-content';
import { getErrorMessage } from '@/constants/errorMessages';

const MySwal = withReactContent(Swal);

type AlertType = 'success' | 'error' | 'warning' | 'info';

interface AlertOptions {
  title?: string;
  text?: string;
  type?: AlertType;
  timer?: number;
}

export function useAlert() {
  const showAlert = async ({
    title,
    text,
    type = 'info',
    timer,
  }: AlertOptions) => {
    await MySwal.fire({
      title,
      text,
      icon: type,
      confirmButtonText: '확인',
      confirmButtonColor: '#3b82f6',
      timer,
      timerProgressBar: !!timer,
      returnFocus: false,
    });
  };

  const showSuccess = (message: string, title = '완료') =>
    showAlert({ title, text: message, type: 'success', timer: 2000 });

  const showError = (codeOrMessage: string, title = '오류') => {
    const isErrorCode = /^[A-Z]+_\d+$/.test(codeOrMessage);
    const message = isErrorCode ? getErrorMessage(codeOrMessage) : codeOrMessage;
    return showAlert({ title, text: message, type: 'error' });
  };

  const showWarning = (message: string, title = '주의') =>
    showAlert({ title, text: message, type: 'warning' });

  const showInfo = (message: string, title = '안내') =>
    showAlert({ title, text: message, type: 'info' });

  return {
    showAlert,
    showSuccess,
    showError,
    showWarning,
    showInfo,
  };
}
