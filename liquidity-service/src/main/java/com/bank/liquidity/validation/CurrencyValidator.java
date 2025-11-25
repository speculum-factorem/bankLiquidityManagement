package com.bank.liquidity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    private static final Set<String> VALID_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD", 
        "CNY", "INR", "BRL", "RUB", "KRW", "MXN", "SGD", "HKD"
    );

    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        // Инициализация не требуется
    }

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        if (currency == null) {
            return true; // Валидация null обрабатывается через @NotNull
        }
        
        String upperCurrency = currency.toUpperCase();
        boolean isValid = VALID_CURRENCIES.contains(upperCurrency);
        
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("Currency '%s' is not supported. Supported currencies: %s", 
                    currency, VALID_CURRENCIES))
                .addConstraintViolation();
        }
        
        return isValid;
    }
}

