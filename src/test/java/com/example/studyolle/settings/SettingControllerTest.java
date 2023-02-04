package com.example.studyolle.settings;

import com.example.studyolle.WithAccount;
import com.example.studyolle.account.AccountRepository;
import com.example.studyolle.account.AccountService;
import com.example.studyolle.domain.Account;
import lombok.With;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
    }

    @WithAccount("keesun")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(post("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(view().name("settings/profile"));
    }

    @Test
    @WithAccount("keesun")
    @DisplayName("프로필 수정하기 입력값 정상")
    void 프로필_수정하기_입력값_정상() throws Exception {
        String bio = "짧은 소개를 수정하는 경우.";

        mockMvc.perform(post("/settings/profile")
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));

        Account findAccount = accountRepository.findByNickname("keesun");
        assertThat(findAccount.getBio()).isEqualTo("짧은 소개를 수정하는 경우.");
    }

    @Test
    @WithAccount("keesun")
    @DisplayName("프로필 수정하기 입력값 에러")
    void updateProfile_error() throws Exception {

        String bio = "길게 소개를 하는 경우 길게 소개를 하는 경우 길게 소개를 하는 경우 길게 소개를 하는 경우 길게 소개를 하는 경우 길게 소개를 하는 경우 ";

        mockMvc.perform(post("/settings/profile")
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));

        Account keesun = accountRepository.findByNickname("keesun");
        assertThat(keesun.getBio()).isNull();
    }

    @WithAccount("keesun")
    @DisplayName("패스워드 수정  - 입력값 정상")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post("/settings/account")
                        .param("newPassword", "12345678")
                        .param("newPasswordConfirm", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/account"))
                .andExpect(flash().attributeExists("message"));

        Account findAccount = accountRepository.findByNickname("keesun");
        assertTrue(passwordEncoder.matches("12345678", findAccount.getPassword()));
    }

    @Test
    @WithAccount("keesun")
    @DisplayName("패스워드 수정 입력값 에러 패스워드 불일치")
    void 패스워드_수정_입력값_에러_패스워드_불일치() throws Exception {
        mockMvc.perform(post("/settings/account")
                        .param("newPassword", "12345678")
                        .param("newPasswordConfirm", "11111")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/account"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"));
    }

}