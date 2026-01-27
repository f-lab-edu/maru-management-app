package com.maru.service.notification.adapter;

import com.maru.common.exception.BusinessException;
import com.maru.domain.message.MessageChannel;
import com.maru.domain.message.exception.MessageDispatchErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageChannelAdapterFactory {

    private final List<MessageChannelAdapter> adapters;

    /**
     * 채널에 맞는 어댑터 조회
     *
     * @param channel 메시지 채널
     * @return 채널 어댑터
     * @throws BusinessException 지원하지 않는 채널인 경우
     */
    public MessageChannelAdapter getAdapter(MessageChannel channel) {
        return adapters.stream()
                .filter(adapter -> adapter.getChannel() == channel)
                .findFirst()
                .orElseThrow(() -> new BusinessException(MessageDispatchErrorCode.UNSUPPORTED_CHANNEL));
    }
}
