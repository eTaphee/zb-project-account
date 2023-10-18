package kr.co.zerobase.account.type;

public final class ValidationMessage {

    public static final String USER_ID_NOT_NULL = "사용자 아이디는 빈 값일 수 없습니다.";
    public static final String USER_ID_MIN_1 = "사용자 아이디는 1 이상이어야 합니다.";

    public static final String INITIAL_BALANCE_NOT_NULL = "초기 잔액은 빈 값일 수 없습니다.";
    public static final String INITIAL_BALANCE_MIN_0 = "초기 잔액은 0 이상이어야 합니다.";

    public static final String ACCOUNT_NUMBER_NOT_NULL = "계좌번호는 빈 값일 수 없습니다.";
    public static final String ACCOUNT_NUMBER_SIZE_10 = "계좌번호는 10자리입니다.";

    public static final String USE_BALANCE_AMOUNT_NOT_NULL = "잔액 사용 금액은 빈 값일 수 없습니다.";
    public static final String USE_BALANCE_AMOUNT_MIN_10 = "최소 잔액 사용 금액은 10원입니다.";
    public static final String USE_BALANCE_AMOUNT_MAX_1_000_000_000 = "최대 잔액 사용 금액은 1,000,000,000원입니다.";

    public static final String CANCEL_BALANCE_AMOUNT_NOT_NULL = "거래 취소 금액은 빈 값일 수 없습니다.";
    public static final String CANCEL_BALANCE_AMOUNT_MIN_10 = "최소 거래 취소 금액은 10원입니다.";
    public static final String CANCEL_BALANCE_AMOUNT_MAX_1_000_000_000 = "최대 거래 취소 금액은 1,000,000,000원입니다.";
}
