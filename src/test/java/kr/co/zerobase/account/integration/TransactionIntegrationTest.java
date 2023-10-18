package kr.co.zerobase.account.integration;

import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_ALREADY_UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_NOT_FOUND;
import static kr.co.zerobase.account.type.ErrorCode.AMOUNT_EXCEED_BALANCE;
import static kr.co.zerobase.account.type.ErrorCode.CANCEL_BALANCE_MUST_SUCCESS_TRANSACTION;
import static kr.co.zerobase.account.type.ErrorCode.CANCEL_BALANCE_MUST_USE_TRANSACTION;
import static kr.co.zerobase.account.type.ErrorCode.CANCEL_MUST_FULLY;
import static kr.co.zerobase.account.type.ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH;
import static kr.co.zerobase.account.type.ErrorCode.TRANSACTION_ALREADY_CANCELED;
import static kr.co.zerobase.account.type.ErrorCode.TRANSACTION_NOT_FOUND;
import static kr.co.zerobase.account.type.ErrorCode.USER_ACCOUNT_UN_MATCH;
import static kr.co.zerobase.account.type.ErrorCode.USER_NOT_FOUND;
import static kr.co.zerobase.account.type.TransactionResultType.S;
import static kr.co.zerobase.account.type.TransactionType.USE;
import static org.hamcrest.Matchers.hasLength;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.co.zerobase.account.dto.CancelBalance;
import kr.co.zerobase.account.dto.UseBalance;
import kr.co.zerobase.account.util.MockMvcUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

public class TransactionIntegrationTest extends BaseIntegrationTest {

    private static final String USE_BALANCE_URL = "/transactions/use";

    @Test
    @DisplayName("잔액 사용 성공")
    void successUseBalance() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(1L)
            .accountNumber("1000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, USE_BALANCE_URL, request);

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountNumber").value("1000000000"))
            .andExpect(jsonPath("$.transactionResult").value(S.toString()))
            .andExpect(jsonPath("$.transactionId").value(hasLength(32)))
            .andExpect(jsonPath("$.amount").value(1000))
            .andExpect(jsonPath("$.transactedAt").isNotEmpty());
    }

    @Test
    @DisplayName("잔액 사용 실패 - 사용자 없음")
    void failUseBalance_UserNotFound() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(99L)
            .accountNumber("1000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, USE_BALANCE_URL, request);

        // then
        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value(USER_NOT_FOUND.toString()));
    }

    @Test
    @DisplayName("잔액 사용 실패 - 계좌 없음")
    void failUseBalance_AccountNotFound() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(1L)
            .accountNumber("9000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, USE_BALANCE_URL, request);

        // then
        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value(ACCOUNT_NOT_FOUND.toString()));
    }

    @Test
    @DisplayName("잔액 사용 실패 - 계좌 소유주 다름")
    void failUseBalance_UserAccountUnMatch() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(1L)
            .accountNumber("2000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, USE_BALANCE_URL, request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(USER_ACCOUNT_UN_MATCH.toString()));
    }

    @Test
    @DisplayName("잔액 사용 실패 - 이미 해지된 계좌")
    void failUseBalance_AccountAlreadyUnregistered() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(1L)
            .accountNumber("1000000002")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, USE_BALANCE_URL, request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(ACCOUNT_ALREADY_UNREGISTERED.toString()));
    }

    @Test
    @DisplayName("잔액 사용 실패 - 거래 금액이 잔액보다 큼")
    void failUseBalance_AmountExceedBalance() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(1L)
            .accountNumber("1000000000")
            .amount(999999L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, USE_BALANCE_URL, request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(AMOUNT_EXCEED_BALANCE.toString()));
    }

    @Test
    @DisplayName("거래 취소 성공")
    void successCancelBalance() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("1000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            String.format("/transactions/%s/cancel", "TRAN1"),
            request);

        // then
        resultActions
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("거래 취소 실패 - 계좌 없음")
    void failCancelBalance_AccountNotFound() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("9000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            String.format("/transactions/%s/cancel", "TRAN1"),
            request);

        // then
        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value(ACCOUNT_NOT_FOUND.toString()));
    }

    @Test
    @DisplayName("거래 취소 실패 - 거래 없음")
    void failCancelBalance_TransactionNotFound() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("1000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            String.format("/transactions/%s/cancel", "TRAN10"),
            request);

        // then
        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value(TRANSACTION_NOT_FOUND.toString()));
    }

    @Test
    @DisplayName("거래 취소 실패 - 계좌 불일치")
    void failCancelBalance_TransactionAccountUnMatch() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("1000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            String.format("/transactions/%s/cancel", "TRAN5"),
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(TRANSACTION_ACCOUNT_UN_MATCH.toString()));
    }

    @Test
    @DisplayName("거래 취소 실패 - 부분 취소 불가")
    void failCancelBalance_CancelMustFully() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("1000000000")
            .amount(100L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            String.format("/transactions/%s/cancel", "TRAN1"),
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(CANCEL_MUST_FULLY.toString()));
    }

    @Test
    @DisplayName("거래 취소 실패 - 이미 취소된 거래")
    void failCancelBalance_TransactionAlreadyCanceled() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("1000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            String.format("/transactions/%s/cancel", "TRAN4"),
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(TRANSACTION_ALREADY_CANCELED.toString()));
    }

    @Test
    @DisplayName("거래 취소 실패 - 취소된 거래 취소")
    void failCancelBalance_CancelBalanceMustUseTransaction() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("1000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            String.format("/transactions/%s/cancel", "TRAN3"),
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.errorCode").value(CANCEL_BALANCE_MUST_USE_TRANSACTION.toString()));
    }

    @Test
    @DisplayName("거래 취소 실패 - 실패한 거래 취소")
    void failCancelBalance_CancelBalanceMustSuccessTransaction() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("1000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            String.format("/transactions/%s/cancel", "TRAN2"),
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.errorCode").value(CANCEL_BALANCE_MUST_SUCCESS_TRANSACTION.toString()));
    }

    @Test
    @DisplayName("거래 조회 성공")
    void successQueryTransaction() throws Exception {
        // given
        // when
        ResultActions resultActions = MockMvcUtil.performGet(mockMvc, "/transactions/TRAN1");

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountNumber").value("1000000000"))
            .andExpect(jsonPath("$.transactionType").value(USE.toString()))
            .andExpect(jsonPath("$.transactionResult").value(S.toString()))
            .andExpect(jsonPath("$.transactionId").value("TRAN1"))
            .andExpect(jsonPath("$.amount").value(1000))
            .andExpect(jsonPath("$.transactedAt").isNotEmpty());
    }

    @Test
    @DisplayName("거래 조회 실패 - 거래 없음")
    void failQueryTransaction_TransactionNotFound() throws Exception {
        // given
        // when
        ResultActions resultActions = MockMvcUtil.performGet(mockMvc,
            "/transactions/NOT_EXIST_TRANSACTION");

        // then
        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value(TRANSACTION_NOT_FOUND.toString()));
    }
}
