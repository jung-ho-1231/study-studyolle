package com.example.studyolle.settings;

import com.example.studyolle.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm)target;
        if (accountRepository.existsByNickname(nicknameForm.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{nicknameForm.getNickname()}, "이미 사용중인 닉네임입니다.");
        }
    }
}
