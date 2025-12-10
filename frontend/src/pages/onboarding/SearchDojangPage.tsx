import { useState, useEffect } from 'react';
import { useUser } from '../../hooks';
import { userService } from '../../services/userService';
import { CardHeader, CardTitle, CardDescription, CardContent } from '../../shared/components/ui/card';
import { Search, MapPin, User, Loader2, Send, CheckCircle2, ChevronLeft, ChevronRight, Clock, Building2 } from 'lucide-react';
import { OnboardingBackButton } from './components/OnboardingBackButton';
import { Button } from '../../shared/components/ui/button';
import { Input } from '../../shared/components/ui/input';
import { useDojangSearch, useRequestApproval, useMyRequests, useCancelRequest } from '../../features/employment';
import { DojangSearchResult } from '../../types/employment';

export default function SearchDojangPage() {
  const { refetch: refreshUser } = useUser();
  const [keyword, setKeyword] = useState('');
  const [debouncedKeyword, setDebouncedKeyword] = useState('');
  const [page, setPage] = useState(0);

  const { data: myRequests, isLoading: isLoadingRequests } = useMyRequests();
  const { data: searchResult, isLoading: isSearching } = useDojangSearch(debouncedKeyword, page);
  const requestApproval = useRequestApproval();
  const cancelRequest = useCancelRequest();

  const pendingRequest = myRequests?.find(r => r.status === 'PENDING');

  useEffect(() => {
    if (keyword.length > 0 && keyword.length < 2) return;
    const timer = setTimeout(() => {
      setDebouncedKeyword(keyword);
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [keyword]);

  const handlePrevious = async () => {
    try {
      await userService.rollbackOnboardingStep();
      await refreshUser();
    } catch (error) {
      console.error('이전 단계 이동 실패:', error);
    }
  };

  const handleRequest = async (dojang: DojangSearchResult) => {
    try {
      await requestApproval.mutateAsync(dojang.id);
    } catch (error) {
      console.error('승인 요청 실패:', error);
    }
  };

  const isAlreadyRequested = (dojangId: number) => {
    return myRequests?.some(r =>
      r.dojangId === dojangId &&
      (r.status === 'PENDING' || r.status === 'ACTIVE')
    ) ?? false;
  };

  const handleCancel = async () => {
    if (!pendingRequest) return;
    if (!confirm('승인 요청을 취소하시겠습니까?')) return;
    try {
      await cancelRequest.mutateAsync(pendingRequest.id);
    } catch (error) {
      console.error('요청 취소 실패:', error);
    }
  };

  if (isLoadingRequests) {
    return (
      <div className="flex items-center justify-center h-[400px]">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (pendingRequest) {
    return (
      <div className="animate-in fade-in slide-in-from-right-8 duration-500">
        <CardHeader className="pb-6 pt-8 text-center">
          <div className="w-20 h-20 mx-auto mb-4 rounded-full bg-amber-100 flex items-center justify-center">
            <Clock className="w-10 h-10 text-amber-600" />
          </div>
          <CardTitle className="text-2xl font-bold text-slate-900">승인 대기 중</CardTitle>
          <CardDescription className="text-base mt-2">
            관장님의 승인을 기다리고 있습니다
          </CardDescription>
        </CardHeader>

        <CardContent className="px-8 pb-10">
          <div className="bg-slate-50 rounded-xl p-6 text-center">
            <div className="w-12 h-12 mx-auto mb-3 rounded-full bg-white flex items-center justify-center shadow-sm">
              <Building2 className="w-6 h-6 text-slate-600" />
            </div>
            <h3 className="font-semibold text-lg text-slate-900">{pendingRequest.dojangName}</h3>
            <p className="text-sm text-slate-500 mt-1">
              {new Date(pendingRequest.requestedAt).toLocaleDateString('ko-KR')}에 요청함
            </p>
          </div>

          <Button
            variant="outline"
            className="w-full mt-8"
            onClick={handleCancel}
            disabled={cancelRequest.isPending}
          >
            요청 취소
          </Button>

          <p className="text-xs text-slate-400 text-center mt-6">
            승인이 완료되면 자동으로 다음 단계로 이동합니다
          </p>
        </CardContent>
      </div>
    );
  }

  return (
    <div className="animate-in fade-in slide-in-from-right-8 duration-500">
      <OnboardingBackButton onClick={handlePrevious} />
      <CardHeader className="pb-6 pt-2">
        <CardTitle className="text-3xl font-bold text-slate-900">도장 찾기</CardTitle>
        <CardDescription className="text-lg mt-2">
          소속된 도장을 검색하여 가입을 요청하세요.
        </CardDescription>
      </CardHeader>

      <CardContent className="px-4 sm:px-8 pb-6 sm:pb-10 space-y-4">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
          <Input
            type="text"
            placeholder="도장명, 주소, 관장님 성함으로 검색"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            className="pl-10 h-11 text-base"
          />
        </div>

        <div className="h-[445px] overflow-y-auto">
          {isSearching && (
            <div className="flex items-center justify-center h-full">
              <Loader2 className="w-5 h-5 animate-spin text-primary" />
              <span className="ml-2 text-slate-500 text-sm">검색 중...</span>
            </div>
          )}

          {!isSearching && searchResult && searchResult.content.length > 0 && (
            <div className="space-y-2">
              {searchResult.content.map((dojang) => (
                <div
                  key={dojang.id}
                  className="flex items-center justify-between gap-3 p-3 rounded-lg border border-slate-200 bg-white hover:border-primary/30 transition-colors"
                >
                  <div className="flex-1 min-w-0">
                    <h3 className="font-medium text-sm text-slate-900 truncate">{dojang.name}</h3>
                    <p className="text-xs text-slate-500 truncate mt-0.5">
                      <MapPin className="w-3 h-3 inline mr-1" />
                      {dojang.address}
                    </p>
                    <p className="text-xs text-slate-400 mt-0.5">
                      <User className="w-3 h-3 inline mr-1" />
                      {dojang.ownerName} 관장님
                    </p>
                  </div>
                  <Button
                    size="sm"
                    onClick={() => handleRequest(dojang)}
                    disabled={isAlreadyRequested(dojang.id) || requestApproval.isPending}
                    className={`shrink-0 h-8 px-3 text-xs ${isAlreadyRequested(dojang.id) ? 'bg-green-600 hover:bg-green-600' : ''}`}
                  >
                    {isAlreadyRequested(dojang.id) ? (
                      <>
                        <CheckCircle2 className="w-3.5 h-3.5 mr-1" />
                        완료
                      </>
                    ) : (
                      <>
                        <Send className="w-3.5 h-3.5 mr-1" />
                        요청
                      </>
                    )}
                  </Button>
                </div>
              ))}
            </div>
          )}

          {!isSearching && keyword.length === 1 && (
            <div className="flex flex-col items-center justify-center h-full text-center">
              <Search className="w-10 h-10 mb-2 text-slate-300" />
              <p className="text-sm text-slate-500">2글자 이상 입력해주세요</p>
            </div>
          )}

          {!isSearching && debouncedKeyword.length >= 2 && searchResult?.content.length === 0 && (
            <div className="flex flex-col items-center justify-center h-full text-center">
              <Search className="w-10 h-10 mb-2 text-slate-300" />
              <p className="text-sm text-slate-500">검색 결과가 없습니다</p>
              <p className="text-xs text-slate-400 mt-1">다른 검색어를 입력해보세요</p>
            </div>
          )}

        </div>

        {searchResult && searchResult.totalElements > 0 && (
          <div className="flex items-center justify-between pt-2 border-t border-slate-100">
            <p className="text-xs text-slate-500">
              총 {searchResult.totalElements}개 중 {page * searchResult.size + 1}-{Math.min((page + 1) * searchResult.size, searchResult.totalElements)}
            </p>
            <div className="flex items-center gap-1">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(p => p - 1)}
                disabled={page === 0 || isSearching}
                className="h-8 w-8 p-0"
              >
                <ChevronLeft className="w-4 h-4" />
              </Button>
              <span className="text-xs text-slate-600 px-2">
                {page + 1} / {searchResult.totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(p => p + 1)}
                disabled={page >= searchResult.totalPages - 1 || isSearching}
                className="h-8 w-8 p-0"
              >
                <ChevronRight className="w-4 h-4" />
              </Button>
            </div>
          </div>
        )}
      </CardContent>
    </div>
  );
}
