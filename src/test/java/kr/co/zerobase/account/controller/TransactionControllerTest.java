package kr.co.zerobase.account.controller;

import static kr.co.zerobase.account.type.ErrorCode.INVALID_REQUEST;
import static kr.co.zerobase.account.type.TransactionResultType.S;
import static kr.co.zerobase.account.type.TransactionType.USE;
import static kr.co.zerobase.account.type.ValidationMessage.ACCOUNT_NUMBER_NOT_NULL;
import static kr.co.zerobase.account.type.ValidationMessage.ACCOUNT_NUMBER_SIZE_10;
import static kr.co.zerobase.account.type.ValidationMessage.CANCEL_BALANCE_AMOUNT_MAX_1_000_000_000;
import static kr.co.zerobase.account.type.ValidationMessage.CANCEL_BALANCE_AMOUNT_MIN_10;
import static kr.co.zerobase.account.type.ValidationMessage.CANCEL_BALANCE_AMOUNT_NOT_NULL;
import static kr.co.zerobase.account.type.ValidationMessage.USER_ID_MIN_1;
import static kr.co.zerobase.account.type.ValidationMessage.USER_ID_NOT_NULL;
import static kr.co.zerobase.account.type.ValidationMessage.USE_BALANCE_AMOUNT_MAX_1_000_000_000;
import static kr.co.zerobase.account.type.ValidationMessage.USE_BALANCE_AMOUNT_MIN_10;
import static kr.co.zerobase.account.type.ValidationMessage.USE_BALANCE_AMOUNT_NOT_NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import kr.co.zerobase.account.dto.CancelBalance;
import kr.co.zerobase.account.dto.TransactionDto;
import kr.co.zerobase.account.dto.UseBalance;
import kr.co.zerobase.account.exception.AccountException;
import kr.co.zerobase.account.service.TransactionService;
import kr.co.zerobase.account.type.ErrorCode;
import kr.co.zerobase.account.type.TransactionType;
import kr.co.zerobase.account.util.MockMvcUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    private final static String USE_TRANSACTION_URL = "/transactions/use";
    private final static String CANCEL_TRANSACTION_URL = "/transactions/transactionIdForCancel/cancel";
    private final static String GET_TRANSACTION_URL = "/transactions/12345";

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("잔액 사용 성공")
    void successUseBalance() throws Exception {
        // given
        given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
            .willReturn(TransactionDto.builder()
                .accountNumber("1000000000")
                .transactedAt(LocalDateTime.now())
                .amount(1000L)
                .transactionId("transactionId")
                .transactionResultType(S)
                .build());

        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(1L)
            .accountNumber("1000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            USE_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountNumber").value("1000000000"))
            .andExpect(jsonPath("$.transactionResult").value("S"))
            .andExpect(jsonPath("$.transactionId").value("transactionId"))
            .andExpect(jsonPath("$.amount").value(1000L));
    }

    @Test
    @DisplayName("잔액 사용 실패 - 실패 거래 기록")
    void failUseBalance_saveFailedCancelTransaction() throws Exception {
        // given
        given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
            .willThrow(new AccountException(INVALID_REQUEST));

        assertThrows(AccountException.class,
            () -> transactionService.useBalance(1L, "1234567890", 1000L));

        // when
        ArgumentCaptor<TransactionType> transactionTypeCaptor = ArgumentCaptor.forClass(
            TransactionType.class);
        ArgumentCaptor<String> accountNumberCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> amountCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<ErrorCode> errorCodeCaptor = ArgumentCaptor.forClass(ErrorCode.class);

        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(1L)
            .accountNumber("1000000000")
            .amount(1000L)
            .build();

        MockMvcUtil.performPost(mockMvc, USE_TRANSACTION_URL, request);

        // then
        verify(transactionService, times(1)).saveFailedUseTransaction(
            transactionTypeCaptor.capture(),
            accountNumberCaptor.capture(),
            amountCaptor.capture(),
            errorCodeCaptor.capture());
        assertEquals("1000000000", accountNumberCaptor.getValue());
        assertEquals(1000L, amountCaptor.getValue());
        assertEquals(INVALID_REQUEST, errorCodeCaptor.getValue());
    }

    @Test
    @DisplayName("잔액 사용 실패 - 유효성 검사(userId @NotNull)")
    void failUseBalance_userId_NotNull() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .accountNumber("1000000000")
            .amount(100L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            USE_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(USER_ID_NOT_NULL));
    }

    @Test
    @DisplayName("잔액 사용 실패 - 유효성 검사(userId @Min(1))")
    void failUseBalance_userId_Min_1() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(0L)
            .accountNumber("1000000000")
            .amount(100L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            USE_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(USER_ID_MIN_1));
    }

    @Test
    @DisplayName("잔액 사용 실패 - 유효성 검사(accountNumber @NotNull)")
    void failUseBalance_accountNumber_NotNull() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(1L)
            .amount(100L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            USE_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(ACCOUNT_NUMBER_NOT_NULL));
    }

    @Test
    @DisplayName("잔액 사용 실패 - 유효성 검사(accountNumber @Size(10))")
    void failUseBalance_accountNumber_Size_10() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(1L)
            .accountNumber("123")
            .amount(100L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            USE_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(ACCOUNT_NUMBER_SIZE_10));
    }

    @Test
    @DisplayName("잔액 사용 실패 - 유효성 검사(amount @NotNull)")
    void failUseBalance_amount_NotNull() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(1L)
            .accountNumber("1234567890")
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            USE_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(USE_BALANCE_AMOUNT_NOT_NULL));
    }

    @Test
    @DisplayName("잔액 사용 실패 - 유효성 검사(amount @Min(10))")
    void failUseBalance_amount_Min_10() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(1L)
            .accountNumber("1234567890")
            .amount(9L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            USE_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(USE_BALANCE_AMOUNT_MIN_10));
    }

    @Test
    @DisplayName("잔액 사용 실패 - 유효성 검사(amount @Max(1_000_000_000))")
    void failUseBalance_amount_Max_1_000_000_000() throws Exception {
        // given
        // when
        UseBalance.RequestDto request = UseBalance.RequestDto.builder()
            .userId(1L)
            .accountNumber("1234567890")
            .amount(1_000_000_000L + 1L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            USE_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(USE_BALANCE_AMOUNT_MAX_1_000_000_000));
    }

    @Test
    @DisplayName("거래 취소 성공")
    void successCancelBalance() throws Exception {
        // given
        given(transactionService.cancelBalance(anyString(), anyString(), anyLong()))
            .willReturn(TransactionDto.builder()
                .accountNumber("1000000000")
                .transactedAt(LocalDateTime.now())
                .amount(1000L)
                .transactionId("canceledTransactionId")
                .transactionResultType(S)
                .build());

        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("1000000000")
            .amount(1000L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            CANCEL_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountNumber").value("1000000000"))
            .andExpect(jsonPath("$.transactionResult").value("S"))
            .andExpect(jsonPath("$.transactionId").value("canceledTransactionId"))
            .andExpect(jsonPath("$.amount").value(1000L));
    }

    @Test
    @DisplayName("거래 취소 실패 - 유효성 검사(accountNumber @NotNull)")
    void failCancelBalance_accountNumber_NotNull() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .amount(100L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            CANCEL_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(ACCOUNT_NUMBER_NOT_NULL));
    }

    @Test
    @DisplayName("거래 취소 실패 - 유효성 검사(accountNumber @Size(10))")
    void failCancelBalance_accountNumber_Size_10() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("123456789")
            .amount(100L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            CANCEL_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(ACCOUNT_NUMBER_SIZE_10));
    }

    @Test
    @DisplayName("거래 취소 실패 - 유효성 검사(amount @NotNull)")
    void failCancelBalance_amount_NotNull() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("1234567890")
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            CANCEL_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(CANCEL_BALANCE_AMOUNT_NOT_NULL));
    }

    @Test
    @DisplayName("거래 취소 실패 - 유효성 검사(amount @Min(10))")
    void failCancelBalance_amount_Min_10() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("1234567890")
            .amount(9L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            CANCEL_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(CANCEL_BALANCE_AMOUNT_MIN_10));
    }

    @Test
    @DisplayName("거래 취소 실패 - 유효성 검사(amount @Max(1_000_000_000))")
    void failCancelBalance_amount_Max_1_000_000_000() throws Exception {
        // given
        // when
        CancelBalance.RequestDto request = CancelBalance.RequestDto.builder()
            .accountNumber("1234567890")
            .amount(1_000_000_000L + 1L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
            CANCEL_TRANSACTION_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(CANCEL_BALANCE_AMOUNT_MAX_1_000_000_000));
    }

    @Test
    @DisplayName("거래 조회 성공")
    void successGetTransaction() throws Exception {
        // given
        given(transactionService.queryTransaction(anyString()))
            .willReturn(TransactionDto.builder()
                .accountNumber("100000000")
                .transactionType(USE)
                .transactedAt(LocalDateTime.now())
                .amount(54321L)
                .transactionId("transactionId")
                .transactionResultType(S)
                .build());

        // when
        ResultActions resultActions = MockMvcUtil.performGet(mockMvc, GET_TRANSACTION_URL);

        // then
        resultActions
            .andExpect(jsonPath("$.accountNumber").value("100000000"))
            .andExpect(jsonPath("$.transactionType").value("USE"))
            .andExpect(jsonPath("$.transactionResult").value("S"))
            .andExpect(jsonPath("$.amount").value(54321))
            .andExpect(jsonPath("$.transactionId").value("transactionId"));
    }
}