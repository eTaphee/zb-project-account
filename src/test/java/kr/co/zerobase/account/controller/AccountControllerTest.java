package kr.co.zerobase.account.controller;

import static kr.co.zerobase.account.type.ErrorCode.INVALID_REQUEST;
import static kr.co.zerobase.account.type.ValidationMessage.INITIAL_BALANCE_MIN_0;
import static kr.co.zerobase.account.type.ValidationMessage.INITIAL_BALANCE_NOT_NULL;
import static kr.co.zerobase.account.type.ValidationMessage.USER_ID_MIN_1;
import static kr.co.zerobase.account.type.ValidationMessage.USER_ID_NOT_NULL;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import kr.co.zerobase.account.dto.AccountDto;
import kr.co.zerobase.account.dto.CreateAccount;
import kr.co.zerobase.account.dto.DeleteAccount;
import kr.co.zerobase.account.dto.DeleteAccount.RequestDto;
import kr.co.zerobase.account.service.AccountService;
import kr.co.zerobase.account.util.MockMvcUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    private static final String CREATE_ACCOUNT_URL = "/accounts";
    private static final String DELETE_ACCOUNT_URL = "/accounts/1234567890";
    private static final String GET_ACCOUNT_URL = "/accounts?user_id=1";

    @MockBean
    private AccountService accountService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("계좌 생성 성공")
    void successCreateAccount() throws Exception {
        // given
        given(accountService.createAccount(anyLong(), anyLong()))
            .willReturn(AccountDto.builder()
                .userId(1L)
                .accountNumber("1234567890")
                .registeredAt(LocalDateTime.now())
                .unregisteredAt(LocalDateTime.now())
                .build());

        // when
        CreateAccount.RequestDto request = CreateAccount.RequestDto.builder()
            .userId(1L)
            .initialBalance(100L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, CREATE_ACCOUNT_URL, request);

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.accountNumber").value("1234567890"));
    }

    @Test
    @DisplayName("계좌 생성 실패 - 유효성 검사(userId @NotNull)")
    void failCreateAccount_userId_NotNull() throws Exception {
        // given
        // when
        CreateAccount.RequestDto request = CreateAccount.RequestDto.builder()
            .initialBalance(100L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, CREATE_ACCOUNT_URL, request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(USER_ID_NOT_NULL));
    }

    @Test
    @DisplayName("계좌 생성 실패 - 유효성 검사(userId @Min(1))")
    void failCreateAccount_userId_Min_1() throws Exception {
        // given
        // when
        CreateAccount.RequestDto request = CreateAccount.RequestDto.builder()
            .userId(0L)
            .initialBalance(100L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, CREATE_ACCOUNT_URL, request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(USER_ID_MIN_1));
    }

    @Test
    @DisplayName("계좌 생성 실패 - 유효성 검사(initialBalance @NotNull)")
    void failCreateAccount_initialBalance_NotNull() throws Exception {
        // given
        // when
        CreateAccount.RequestDto request = CreateAccount.RequestDto.builder().userId(1L).build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, CREATE_ACCOUNT_URL, request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(INITIAL_BALANCE_NOT_NULL));
    }

    @Test
    @DisplayName("계좌 생성 실패 - 유효성 검사(initialBalance @Min(1))")
    void failCreateAccount_initialBalance_Min_1() throws Exception {
        // given
        // when
        CreateAccount.RequestDto request = CreateAccount.RequestDto.builder()
            .userId(1L)
            .initialBalance(-1L)
            .build();

        ResultActions resultActions = MockMvcUtil.performPost(mockMvc, CREATE_ACCOUNT_URL, request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(INITIAL_BALANCE_MIN_0));
    }

    @Disabled
    @Test
    @DisplayName("계좌 생성 실패 - 사용자 없음")
    void failCreateAccount_UserNotFound() throws Exception {
        // TODO: 해당 케이스를 controller 테스트에서 작성하는게 맞을까?
        // given
        // when
        // then
    }

    @Disabled
    @Test
    @DisplayName("계좌 생성 실패 - 사용자당 생성 개수 초과")
    void failCreateAccount_MaxAccountPerUser() {
        // TODO: 해당 케이스를 controller 테스트에서 작성하는게 맞을까?
        // given
        // when
        // then
    }

    @Test
    @DisplayName("계좌 해지 성공")
    void successDeleteAccount() throws Exception {
        // given
        given(accountService.deleteAccount(anyLong(), anyString()))
            .willReturn(
                AccountDto.builder()
                    .userId(1L)
                    .accountNumber("1234567890")
                    .registeredAt(LocalDateTime.now())
                    .unregisteredAt(LocalDateTime.now())
                    .build());

        // when
        RequestDto request = RequestDto.builder().userId(1L).build();

        ResultActions resultActions = MockMvcUtil.performDelete(mockMvc,
            DELETE_ACCOUNT_URL,
            request);

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.accountNumber").value("1234567890"))
            .andExpect(jsonPath("$.unregisteredAt").isNotEmpty());
    }

    @Test
    @DisplayName("계좌 해지 실패 - 유효성 검사(userId @NotNull)")
    void failDeleteAccount_userId_NotNull() throws Exception {
        // given
        // when
        ResultActions resultActions = MockMvcUtil.performDelete(mockMvc,
            DELETE_ACCOUNT_URL,
            DeleteAccount.RequestDto.builder().build());

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(USER_ID_NOT_NULL));
    }

    @Test
    @DisplayName("계좌 해지 실패 - 유효성 검사(userId @Min(1))")
    void failDeleteAccount_userId_Min_1() throws Exception {
        // given
        // when
        RequestDto request = RequestDto.builder().userId(0L).build();

        ResultActions resultActions = MockMvcUtil.performDelete(mockMvc,
            DELETE_ACCOUNT_URL,
            request);

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value(INVALID_REQUEST.toString()))
            .andExpect(jsonPath("$.errorMessage").value(USER_ID_MIN_1));
    }

    @Test
    @DisplayName("계좌 확인 성공")
    void successGetAccounts() throws Exception {
        // given
        List<AccountDto> accounts = Arrays.asList(
            AccountDto.builder().accountNumber("1000000000").balance(0L).build(),
            AccountDto.builder().accountNumber("2000000000").balance(100L).build(),
            AccountDto.builder().accountNumber("3000000000").balance(2000L).build()
        );

        given(accountService.getAccountsByUserId(anyLong()))
            .willReturn(accounts);

        // when
        ResultActions resultActions = MockMvcUtil.performGet(mockMvc, GET_ACCOUNT_URL);

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].accountNumber").value("1000000000"))
            .andExpect(jsonPath("$[0].balance").value(0))
            .andExpect(jsonPath("$[1].accountNumber").value("2000000000"))
            .andExpect(jsonPath("$[1].balance").value(100))
            .andExpect(jsonPath("$[2].accountNumber").value("3000000000"))
            .andExpect(jsonPath("$[2].balance").value(2000));
    }
}