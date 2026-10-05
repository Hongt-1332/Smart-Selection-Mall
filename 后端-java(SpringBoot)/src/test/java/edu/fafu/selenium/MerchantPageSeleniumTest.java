package edu.fafu.selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 商户页面 Selenium 测试
 */
@DisplayName("商户页面 Selenium 测试")
class MerchantPageSeleniumTest extends BaseSeleniumTest {

    @BeforeEach
    void setUp() {
        String token = apiLogin("merchant_account", "merchant_password");
        setAuthCookie(token);
    }

    @Test
    @DisplayName("商户商品管理页面可正常加载")
    void merchantGoodsPageLoads() {
        driver.get(BASE_URL + "/merchant/goods.html");

        wait.until(d -> !d.findElements(By.tagName("body")).isEmpty());
        assertTrue(driver.getPageSource().contains("商品") || driver.getPageSource().contains("goods"));
    }

    @Test
    @DisplayName("商户订单管理页面可正常加载")
    void merchantTradePageLoads() {
        driver.get(BASE_URL + "/merchant/trade.html");

        wait.until(d -> !d.findElements(By.tagName("body")).isEmpty());
        assertTrue(driver.getPageSource().contains("订单") || driver.getPageSource().contains("trade"));
    }
}