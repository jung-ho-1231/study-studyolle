package com.example.studyolle.settings;

import com.example.studyolle.account.AccountService;
import com.example.studyolle.account.CurrentUser;
import com.example.studyolle.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SettingController {

    private final AccountService accountService;

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @GetMapping("/settings/profile")
    public String updateProfileForm(@CurrentUser Account account, Model model) {

        model.addAttribute(account);
        model.addAttribute(new Profile(account));
        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentUser Account account, @Validated Profile profile, BindingResult error, Model model, RedirectAttributes attributes) {
        if (error.hasErrors()) {
            model.addAttribute(account);
            return "settings/profile";
        }

        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필이 업데이트되었습니다.");
        return "redirect:/settings/profile";
    }

    @GetMapping("/settings/password")
    public String updateAccountForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return "settings/password";
    }

    @PostMapping("/settings/password")
    public String updateAccount(@CurrentUser Account account, @Validated PasswordForm passwordForm, BindingResult error, Model model, RedirectAttributes attributes) {
        if (error.hasErrors()) {
            model.addAttribute(account);
            return "settings/password";
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
        return "redirect:/settings/password";
    }
}
