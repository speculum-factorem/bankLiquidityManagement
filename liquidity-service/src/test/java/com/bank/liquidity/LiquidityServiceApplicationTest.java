package com.bank.liquidity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LiquidityServiceApplicationTest {

    @Test
    void contextLoads() {
        assertTrue(true, "Context should load successfully");
    }
}