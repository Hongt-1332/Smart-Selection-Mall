package edu.fafu.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户登录 Selenium 测试
 */
@DisplayName("用户登录 Selenium 测试")
class UserLoginSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("登录页面可正常访问")
    void loginPageAccessible() {
        driver.get(BASE_URL + "/login.html");
        String title = driver.getTitle();
        assertNotNull(title);
        assertTrue(title.contains("登录") || title.contains("Login") || driver.getPageSource().contains("登录"));
    }

    @Test
    @DisplayName("登录表单存在且可交互")
    void loginFormElementsPresent() {
        driver.get(BASE_URL + "/login.html");

        assertFalse(driver.findElements(By.name("account")).isEmpty(), "账号输入框应存在");
        assertFalse(driver.findElements(By.name("password")).isEmpty(), "密码输入框应存在");
        assertFalse(driver.findElements(By.cssSelector("button[type='submit']")).isEmpty(), "提交按钮应存在");
    }

    @Test
    @DisplayName("空账号提交应提示错误")
    void loginWithEmptyAccountShowsError() {
        driver.get(BASE_URL + "/login.html");

        clearAndSendKeys(By.name("account"), "");
        clearAndSendKeys(By.name("password"), "123456");
        safeClick(By.cssSelector("button[type='submit']"));

        wait.until(d -> {
            String source = d.getPageSource();
            return source.contains("不能为空") || source.contains("请输入") || source.contains("错误");
        });
    }

    @Test
    @DisplayName("错误密码登录应提示失败")
    void loginWithWrongPasswordFails() {
        driver.get(BASE_URL + "/login.html");

        clearAndSendKeys(By.name("account"), "test_user_99999");
        clearAndSendKeys(By.name("password"), "wrong_password");
        safeClick(By.cssSelector("button[type='submit']"));

        wait.until(d -> {
            String source = d.getPageSource();
            return source.contains("不存在") || source.contains("错误") || source.contains("失败");
        });
    }
}