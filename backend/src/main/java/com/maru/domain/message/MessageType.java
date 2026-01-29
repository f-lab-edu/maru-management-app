package com.maru.domain.message;

import lombok.Getter;

@Getter
public enum MessageType {
    ATTENDANCE_CHECKIN(10),
    ATTENDANCE_CHECKOUT(10),
    PAYMENT(20),
    ANNOUNCEMENT(30);

    private final int priority;

    MessageType(int priority){
        this.priority = priority;
    }

}
