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

    private Zone testZone = Zone.builder().city("test").localNameOfCity("????????????").province("????????????").build();

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
    @DisplayName("????????? ?????? ???")
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
    @DisplayName("????????? ???????????? ????????? ??????")
    void ?????????_????????????_?????????_??????() throws Exception {
        String bio = "?????? ????????? ???????????? ??????.";

        mockMvc.perform(post("/settings/profile")
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));

        Account findAccount = accountRepository.findByNickname("keesun");
        assertThat(findAccount.getBio()).isEqualTo("?????? ????????? ???????????? ??????.");
    }

    @Test
    @WithAccount("keesun")
    @DisplayName("????????? ???????????? ????????? ??????")
    void updateProfile_error() throws Exception {

        String bio = "?????? ????????? ?????? ?????? ?????? ????????? ?????? ?????? ?????? ????????? ?????? ?????? ?????? ????????? ?????? ?????? ?????? ????????? ?????? ?????? ?????? ????????? ?????? ?????? ";

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
    @DisplayName("???????????? ??????  - ????????? ??????")
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
    @DisplayName("???????????? ?????? ????????? ?????? ???????????? ?????????")
    void ????????????_??????_?????????_??????_????????????_?????????() throws Exception {
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
    @DisplayName("????????? ?????? ?????? ???")
    void updateTagForm() throws Exception {
        mockMvc.perform(get("/settings/tags"))
                .andExpect(view().name("settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @Test
    @WithAccount("keesun")
    @DisplayName("????????? ?????? ??????")
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
    @DisplayName("????????? ?????? ??????")
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
    @DisplayName("????????? ?????? ?????? ??????")
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
    @DisplayName("????????? ?????? ?????? ?????? ???")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get("/settings/zones"))
                .andExpect(view().name("settings/zones"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("keesun")
    @DisplayName("????????? ?????? ?????? ??????")
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