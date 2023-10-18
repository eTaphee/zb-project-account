package kr.co.zerobase.account.integration;

import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_ALREADY_UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_NOT_FOUND;
import static kr.co.zerobase.account.type.ErrorCode.BALANCE_NOT_EMPTY;
import static kr.co.zerobase.account.type.ErrorCode.MAX_ACCOUNT_PER_USER;
import static kr.co.zerobase.account.type.ErrorCode.USER_ACCOUNT_UN_MATCH;
import static kr.co.zerobase.account.type.ErrorCode.USER_NOT_FOUND;
import static org.hamcrest.Matchers.hasLength;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.co.zerobase.account.dto.CreateAccount;
import kr.co.zerobase.account.dto.DeleteAccount;
import kr.co.zerobase.account.util.MockMvcUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

public class AccountIntegrationTest extends BaseIntegrationTest {

    private static final String CREATE_ACCOUNT_URL = "/accounts";

    @Test
    @DisplayName("계좌 생성 성공")
    void successCreateAccount() throws Exception {
        // given
        // when
        CreateAccount.RequestDto request = CreateAccount.RequestDto.builder()
            .userId(1L)
            .initialBalance(100L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, CREATE_ACCOUNT_URL, request);

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.accountNumber").value(hasLength(10)))
            .andExpect(jsonPath("$.registeredAt").isNotEmpty());
    }

    @Test
    @DisplayName("계좌 생성 실패 - 사용자 없음")
    void failCreateAccount_UserNotFound() throws Exception {
        // given
        // when
        CreateAccount.RequestDto request = CreateAccount.RequestDto.builder()
            .userId(10L)
            .initialBalance(0L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, CREATE_ACCOUNT_URL, request);

        // then
        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value(USER_NOT_FOUND.toString()));
    }

    @Test
    @DisplayName("계좌 생성 실패 - 사용자당 생성 개수 초과")
    void failCreateAccount_MaxAccountPerUser() throws Exception {
        // given
        // when
        CreateAccount.RequestDto request = CreateAccount.RequestDto.builder()
            .userId(3L)
            .initialBalance(0L)
            .build();

        for (int i = 0; i < 10; i++) {
            MockMvcUtil.performPost(mockMvc, CREATE_ACCOUNT_URL, request);
        }

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, CREATE_ACCOUNT_URL, request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(MAX_ACCOUNT_PER_USER.toString()));
    }

    @Test
    @DisplayName("계좌 해지 성공")
    void successDeleteAccount() throws Exception {
        // given
        // when
        DeleteAccount.RequestDto request = DeleteAccount.RequestDto.builder()
            .userId(1L)
            .build();

        ResultActions resultActions = MockMvcUtil.performDelete(mockMvc,
            "/accounts/1000000001"
            , request);

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.accountNumber").value("1000000001"))
            .andExpect(jsonPath("$.unregisteredAt").isNotEmpty());
    }

    @Test
    @DisplayName("계좌 해지 실패 - 사용자 없음")
    void failDeleteAccount_UserNotFound() throws Exception {
        // given
        // when
        DeleteAccount.RequestDto request = DeleteAccount.RequestDto.builder().userId(100L).build();

        ResultActions resultActions = MockMvcUtil.performDelete(mockMvc,
            "/accounts/1000000000",
            request);

        // then
        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value(USER_NOT_FOUND.toString()));
    }

    @Test
    @DisplayName("계좌 해지 실패 - 계좌 없음")
    void failDeleteAccount_AccountNotFound() throws Exception {
        // given
        // when
        DeleteAccount.RequestDto request = DeleteAccount.RequestDto.builder()
            .userId(1L)
            .build();

        ResultActions resultActions = MockMvcUtil.performDelete(mockMvc,
            "/accounts/9000000000",
            request);

        // then
        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value(ACCOUNT_NOT_FOUND.toString()));
    }

    @Test
    @DisplayName("계좌 해지 실패 - 계좌 소유주 다름")
    void failDeleteAccount_UserAccountUnMatch() throws Exception {
        // given
        // when
        DeleteAccount.RequestDto request = DeleteAccount.RequestDto.builder()
            .userId(1L)
            .build();

        ResultActions resultActions = MockMvcUtil.performDelete(mockMvc,
            "/accounts/2000000000",
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(USER_ACCOUNT_UN_MATCH.toString()));
    }

    @Test
    @DisplayName("계좌 해지 실패 - 잔액 있음")
    void failDeleteAccount_BalanceNotEmpty() throws Exception {
        // given
        // when
        DeleteAccount.RequestDto request = DeleteAccount.RequestDto.builder()
            .userId(1L)
            .build();

        ResultActions resultActions = MockMvcUtil.performDelete(mockMvc,
            "/accounts/1000000000",
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(BALANCE_NOT_EMPTY.toString()));
    }

    @Test
    @DisplayName("계좌 해지 실패 - 이미 해지됨")
    void failDeleteAccount_AccountAlreadyUnregistered() throws Exception {
        // given
        // when
        DeleteAccount.RequestDto request = DeleteAccount.RequestDto.builder()
            .userId(1L)
            .build();

        ResultActions resultActions = MockMvcUtil.performDelete(mockMvc,
            "/accounts/1000000002",
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(ACCOUNT_ALREADY_UNREGISTERED.toString()));
    }

    @Test
    @DisplayName("계좌 확인 성공")
    void successGetAccountsByUserId() throws Exception {
        // given
        // when
        ResultActions resultActions = MockMvcUtil.performGet(mockMvc,
            "/accounts?user_id=1");

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(3));
    }

    @Test
    @DisplayName("계좌 확인 실패 - 사용자 없음")
    void failGetAccountsByUserId_UserNotFound() throws Exception {
        // given
        // when
        ResultActions resultActions = MockMvcUtil.performGet(mockMvc, "/accounts?user_id=100");

        // then
        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value(USER_NOT_FOUND.toString()));
    }
}
