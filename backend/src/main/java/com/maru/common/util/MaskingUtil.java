package com.maru.common.util;

public final class MaskingUtil {

    private MaskingUtil() {
    }

    /**
     * 전화번호 마스킹 (중간 4자리 숨김) 예: 01012345678 → 010****5678
     *
     * @param phone 전화번호
     * @return 마스킹된 전화번호
     */
    public static String phone(String phone) {
        if (phone == null || phone.length() < 8) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
