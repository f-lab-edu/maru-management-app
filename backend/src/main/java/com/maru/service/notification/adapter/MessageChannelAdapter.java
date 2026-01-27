package com.maru.service.notification.adapter;

import com.maru.domain.message.MessageChannel;
import com.maru.domain.message.MessageDispatch;
import com.maru.service.notification.result.BatchSendResult;

import java.util.List;

public interface MessageChannelAdapter {

    /**
     * 지원하는 채널
     *
     * @return 메시지 채널
     */
    MessageChannel getChannel();

    /**
     * 배치 발송
     *
     * @param messages 발송할 메시지 목록
     * @return 벤더 중립 발송 결과
     */
    BatchSendResult sendBatch(List<MessageDispatch> messages);
}
