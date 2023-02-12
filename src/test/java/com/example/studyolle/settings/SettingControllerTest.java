package com.example.studyolle.settings;

import com.example.studyolle.WithAccount;
import com.example.studyolle.account.AccountRepository;
import com.example.studyolle.account.AccountService;
import com.example.studyolle.domain.Account;
import com.example.studyolle.domain.Tag;
import com.example.studyolle.domain.Zone;
import com.example.studyolle.tag.TagRepository;
import com.example.studyolle.zone.ZoneRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
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

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
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

    @Test
    @WithAccount("keesun")
    @DisplayName("계정의 태그 수정 폼")
    void updateTagForm() throws Exception {
        mockMvc.perform(get("/settings/tags"))
                .andExpect(view().name("settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @Test
    @WithAccount("keesun")
    @DisplayName("계정에 태그 추가")
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        String tag = "newTag";
        tagForm.setTagTitle(tag);

        mockMvc.perform(post("/settings/tags/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle(tagForm.getTagTitle());
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("keesun").getTags().contains(newTag));
    }


    @Test
    @WithAccount("keesun")
    @DisplayName("계정에 태그 추가")
    void removeTag() throws Exception {
        Account keesun = accountRepository.findByNickname("keesun");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(keesun, newTag);

        assertTrue(keesun.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/settings/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(keesun.getTags().contains(newTag));
    }


    @WithAccount("keesun")
    @DisplayName("계정의 지역 정보 추가")
    @Test
    void removeZone() throws Exception {
        Account keesun = accountRepository.findByNickname("keesun");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(keesun, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post("/settings/zones/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(keesun.getZones().contains(zone));
    }

    @WithAccount("keesun")
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get("/settings/zones"))
                .andExpect(view().name("settings/zones"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("keesun")
    @DisplayName("계정의 지역 정보 추가")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post("/settings/zones/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Account keesun = accountRepository.findByNickname("keesun");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(keesun.getZones().contains(zone));
    }}