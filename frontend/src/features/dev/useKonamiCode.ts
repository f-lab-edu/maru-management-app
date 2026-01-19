// TODO: 테스트용. 프로덕션에서는 반드시 삭제할 것.

import { useState, useEffect, useCallback } from 'react';

const KONAMI_CODE = [
  'ArrowUp', 'ArrowUp',
  'ArrowDown', 'ArrowDown',
  'ArrowLeft', 'ArrowRight',
  'ArrowLeft', 'ArrowRight',
  'KeyB', 'KeyA'
];

export function useKonamiCode(onActivate: () => void) {
  const [, setInputSequence] = useState<string[]>([]);

  const handleKeyDown = useCallback((event: KeyboardEvent) => {
    const key = event.code;

    setInputSequence(prev => {
      const newSequence = [...prev, key].slice(-KONAMI_CODE.length);

      if (newSequence.length === KONAMI_CODE.length &&
          newSequence.every((k, i) => k === KONAMI_CODE[i])) {
        setTimeout(() => {
          onActivate();
          setInputSequence([]);
        }, 0);
      }

      return newSequence;
    });
  }, [onActivate]);

  useEffect(() => {
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [handleKeyDown]);
}
