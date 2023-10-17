package kr.co.zerobase.account.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import kr.co.zerobase.account.dto.AccountDto;
import kr.co.zerobase.account.dto.CreateAccount;
import kr.co.zerobase.account.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @MockBean
    private AccountService accountService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        // then
        mockMvc.perform(
                post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        CreateAccount.Request.builder()
                            .userId(1L)
                            .initialBalance(100L)
                            .build())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.accountNumber").value("1234567890"))
            .andDo(print());
    }

    @Test
    @DisplayName("계좌 생성 실패 - 유효성 검사(userId @NotNull)")
    void failCreateAccount_userId_NotNull() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(
                post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        CreateAccount.Request.builder()
                            .initialBalance(100L)
                            .build())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
            .andExpect(jsonPath("$.errorMessage").value("사용자 아이디는 빈 값일 수 없습니다."))
            .andDo(print());
    }

    @Test
    @DisplayName("계좌 생성 실패 - 유효성 검사(userId @Min(1))")
    void failCreateAccount_userId_Min_1() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(
                post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        CreateAccount.Request.builder()
                            .userId(0L)
                            .initialBalance(100L)
                            .build())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
            .andExpect(jsonPath("$.errorMessage").value("사용자 아이디는 1 이상이어야 합니다."))
            .andDo(print());
    }

    @Test
    @DisplayName("계좌 생성 실패 - 유효성 검사(initialBalance @NotNull)")
    void failCreateAccount_initialBalance_NotNull() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(
                post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        CreateAccount.Request.builder()
                            .userId(1L)
                            .build())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
            .andExpect(jsonPath("$.errorMessage").value("초기 잔액은 빈 값일 수 없습니다."))
            .andDo(print());
    }

    @Test
    @DisplayName("계좌 생성 실패 - 유효성 검사(initialBalance @Min(1))")
    void failCreateAccount_initialBalance_Min_1() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(
                post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        CreateAccount.Request.builder()
                            .userId(1L)
                            .initialBalance(-1L)
                            .build())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
            .andExpect(jsonPath("$.errorMessage").value("초기 잔액은 0 이상이어야 합니다."))
            .andDo(print());
    }

    @Test
    @DisplayName("계좌 생성 실패 - 사용자 없음")
    void failCreateAccount_UserNotFound() throws Exception {
        // TODO: 해당 케이스를 controller 테스트에서 작성하는게 맞을까?
        // given
        // when
        // then
    }

    @Test
    @DisplayName("계좌 생성 실패 - 사용자당 생성 개수 초과")
    void failCreateAccount_MaxAccountPerUser() {
        // TODO: 해당 케이스를 controller 테스트에서 작성하는게 맞을까?
        // given
        // when
        // then
    }
}