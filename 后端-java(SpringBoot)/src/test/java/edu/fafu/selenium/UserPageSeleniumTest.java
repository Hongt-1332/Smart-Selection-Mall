package edu.fafu.selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用户个人中心页面 Selenium 测试
 * 需要预先登录
 */
@DisplayName("用户个人中心 Selenium 测试")
class UserPageSeleniumTest extends BaseSeleniumTest {

    @BeforeEach
    void setUp() {
        String token = apiLogin("KaiXin123!", "123456");
        setAuthCookie(token);
    }

    @Test
    @DisplayName("用户信息页面可正常加载")
    void userInfoPageLoads() {
        driver.get(BASE_URL + "/user/info.html");

        wait.until(d -> d.getPageSource().contains("用户信息") || d.getPageSource().contains("个人中心"));
        assertTrue(driver.getPageSource().contains("用户信息") || driver.getPageSource().contains("个人中心"));
    }

    @Test
    @DisplayName("用户背景页面可正常加载")
    void userBackgroundPageLoads() {
        driver.get(BASE_URL + "/user/background.html");

        wait.until(d -> !d.findElements(By.tagName("body")).isEmpty());
        assertTrue(driver.getPageSource().contains("背景") || driver.getPageSource().contains("商品"));
    }

    @Test
    @DisplayName("购物车页面可正常加载")
    void cartPageLoads() {
        driver.get(BASE_URL + "/user/cart.html");

        wait.until(d -> !d.findElements(By.tagName("body")).isEmpty());
        assertTrue(driver.getPageSource().contains("购物车") || driver.getPageSource().contains("cart"));
    }

    @Test
    @DisplayName("订单页面可正常加载")
    void tradePageLoads() {
        driver.get(BASE_URL + "/user/trade.html");

        wait.until(d -> !d.findElements(By.tagName("body")).isEmpty());
        assertTrue(driver.getPageSource().contains("订单") || driver.getPageSource().contains("trade"));
    }
}