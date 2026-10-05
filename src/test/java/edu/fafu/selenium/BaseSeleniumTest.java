package edu.fafu.selenium;

import tools.jackson.databind.ObjectMapper;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.user.LoginRequest;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Selenium E2E 测试基类
 * 提供 WebDriver 管理、登录、等待等通用能力
 * 若环境无 Chrome 浏览器则自动跳过所有测试
 */
public abstract class BaseSeleniumTest {

    protected static final String BASE_URL = "http://localhost:8081";
    protected static final ObjectMapper MAPPER = new ObjectMapper();
    protected static final RestTemplate REST = new RestTemplate();

    protected static WebDriver driver;
    protected static WebDriverWait wait;
    protected static boolean chromeAvailable = false;

    @BeforeAll
    static void setUpClass() {
        chromeAvailable = detectChrome();
        Assumptions.assumeTrue(chromeAvailable, "Chrome 浏览器未安装，跳过 Selenium 测试");

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * 检测系统是否安装了 Chrome 浏览器
     */
    private static boolean detectChrome() {
        String os = System.getProperty("os.name").toLowerCase();
        Path[] possiblePaths;
        if (os.contains("win")) {
            possiblePaths = new Path[]{
                    Paths.get("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"),
                    Paths.get("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"),
                    Paths.get(System.getenv("LOCALAPPDATA"), "Google\\Chrome\\Application\\chrome.exe")
            };
        } else if (os.contains("mac")) {
            possiblePaths = new Path[]{
                    Paths.get("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
            };
        } else {
            possiblePaths = new Path[]{
                    Paths.get("/usr/bin/google-chrome"),
                    Paths.get("/usr/bin/chromium-browser"),
                    Paths.get("/usr/bin/chromium")
            };
        }
        for (Path path : possiblePaths) {
            if (path != null && Files.exists(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过 API 登录并返回 Token
     */
    protected String apiLogin(String account, String password) {
        try {
            LoginRequest request = new LoginRequest();
            request.setAccount(account);
            request.setPassword(password);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = REST.exchange(
                    BASE_URL + "/user/login",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            Result<?> result = MAPPER.readValue(response.getBody(), Result.class);
            if (result.getCode() == 200 && result.getData() != null) {
                java.util.Map<?, ?> data = MAPPER.convertValue(result.getData(), java.util.Map.class);
                return (String) data.get("token");
            }
            throw new RuntimeException("API 登录失败: " + result.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("API 登录异常", e);
        }
    }

    /**
     * 设置认证 Cookie，绕过前端登录流程
     */
    protected void setAuthCookie(String token) {
        driver.get(BASE_URL + "/login.html");
        driver.manage().addCookie(new Cookie("token", token));
    }

    /**
     * 等待元素可见
     */
    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * 等待元素可点击
     */
    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * 安全点击（滚动到元素后点击）
     */
    protected void safeClick(By locator) {
        WebElement element = waitClickable(locator);
        org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
        actions.moveToElement(element).click().perform();
    }

    /**
     * 清空并输入文本
     */
    protected void clearAndSendKeys(By locator, String text) {
        WebElement element = waitVisible(locator);
        element.clear();
        element.sendKeys(text);
    }
}