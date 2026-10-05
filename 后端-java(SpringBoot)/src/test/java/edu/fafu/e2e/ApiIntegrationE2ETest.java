package edu.fafu.e2e;

import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.user.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API 集成 E2E 测试（纯 HTTP，不依赖浏览器）
 * 验证核心接口的连通性和返回结构正确性
 * 注意：部分接口需要验证码/登录态，本测试仅验证接口可访问和返回格式
 */
@DisplayName("API 集成 E2E 测试")
class ApiIntegrationE2ETest {

    private static final String BASE_URL = "http://localhost:8081";
    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
    private static final RestTemplate REST = new RestTemplate();

    @Test
    @DisplayName("登录接口可访问且返回正确结构")
    void loginApiReturnsCorrectStructure() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setAccount("KaiXin123!");
        request.setPassword("123456");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = REST.exchange(
                BASE_URL + "/user/login",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Result<?> result = MAPPER.readValue(response.getBody(), Result.class);
        assertNotNull(result);
        assertNotNull(result.getCode());
        assertTrue(result.getCode() == 200 || result.getCode() == 400,
                "登录接口应返回 200 或 400，实际: " + result.getCode());
    }

    @Test
    @DisplayName("登录接口缺少验证码应返回 400")
    void loginApiWithoutCaptchaReturns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setAccount("KaiXin123!");
        request.setPassword("123456");
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
        assertEquals(400, result.getCode(), "未提供验证码应返回 400");
    }

    @Test
    @DisplayName("未登录访问受保护接口应返回 403")
    void unauthorizedAccessReturns403() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            REST.exchange(
                    BASE_URL + "/user/info",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            fail("未认证访问应抛出 403 异常");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode(),
                    "未认证访问应返回 403");
        }
    }

    @Test
    @DisplayName("VIP 配置查询接口无需登录可访问")
    void vipConfigApiAccessibleWithoutAuth() throws Exception {
        ResponseEntity<String> response = REST.exchange(
                BASE_URL + "/vip/config",
                HttpMethod.GET,
                null,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Result<?> result = MAPPER.readValue(response.getBody(), Result.class);
        assertNotNull(result);
        assertEquals(200, result.getCode(), "VIP 配置接口应返回 200");
    }

    @Test
    @DisplayName("验证码生成接口可正常访问")
    void captchaApiAccessible() throws Exception {
        ResponseEntity<String> response = REST.exchange(
                BASE_URL + "/tool/captcha",
                HttpMethod.GET,
                null,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Result<?> result = MAPPER.readValue(response.getBody(), Result.class);
        assertNotNull(result);
        assertEquals(200, result.getCode(), "验证码接口应返回 200");
    }
}