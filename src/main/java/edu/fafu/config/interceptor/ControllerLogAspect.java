package edu.fafu.config.interceptor;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Aspect
@Component
public class ControllerLogAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerLogAspect.class);
    private final ObjectMapper mapper;

    /** 需要脱敏的字段名（不区分大小写） */
    private static final Set<String> SENSITIVE_KEYS = new HashSet<>();

    static {
        SENSITIVE_KEYS.add("password");
        SENSITIVE_KEYS.add("confirmpassword");
        SENSITIVE_KEYS.add("oldpassword");
        SENSITIVE_KEYS.add("captchacode");
        SENSITIVE_KEYS.add("verifycode");
        SENSITIVE_KEYS.add("code");
        SENSITIVE_KEYS.add("token");
        SENSITIVE_KEYS.add("secret");
        SENSITIVE_KEYS.add("appsecret");
        SENSITIVE_KEYS.add("accesskeysecret");
        SENSITIVE_KEYS.add("wechatappsecret");
    }

    /** 递归将敏感字段值替换为掩码，避免明文密码/验证码/密钥写入日志 */
    private JsonNode mask(JsonNode node) {
        if (node == null) return null;
        if (node instanceof ObjectNode obj) {
            ObjectNode copy = obj.deepCopy();
            Iterator<String> names = copy.fieldNames();
            String name;
            while (names.hasNext()) {
                name = names.next();
                if (SENSITIVE_KEYS.contains(name.toLowerCase())) {
                    copy.put(name, "***");
                } else {
                    JsonNode val = copy.get(name);
                    if (val.isObject() || val.isArray()) {
                        copy.set(name, mask(val));
                    }
                }
            }
            return copy;
        }
        if (node instanceof ArrayNode arr) {
            ArrayNode copy = arr.deepCopy();
            for (int i = 0; i < copy.size(); i++) {
                JsonNode val = copy.get(i);
                if (val.isObject() || val.isArray()) {
                    copy.set(i, mask(val));
                }
            }
            return copy;
        }
        return node;
    }

    /** 序列化并脱敏请求参数，返回可安全落盘的内容 */
    private String redact(String json) {
        if (json == null || json.isBlank()) return json;
        try {
            JsonNode tree = mapper.readTree(json);
            JsonNode masked = mask(tree);
            return masked == null ? json : mapper.writeValueAsString(masked);
        } catch (Exception e) {
            return json;
        }
    }

    public ControllerLogAspect(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Around("execution(* edu.fafu.controller..*.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        String cls = pjp.getSignature().getDeclaringTypeName();
        cls = cls.substring(cls.lastIndexOf('.') + 1);
        String method = pjp.getSignature().getName();
        long start = System.currentTimeMillis();

        String token = "";
        String httpMethod = "";
        String requestUri = "";
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            // 不再将 Authorization 头内容（token）记入日志，只记录是否携带
            String t = attrs.getRequest().getHeader("Authorization");
            if (t != null && !t.isBlank()) {
                token = "[present]";
            }
            httpMethod = attrs.getRequest().getMethod();
            requestUri = attrs.getRequest().getRequestURI();
        }

        String argsStr = "";
        Object[] args = pjp.getArgs();
        if (args != null && args.length > 0) {
            try {
                Object[] filteredArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof MultipartFile mf) {
                        filteredArgs[i] = "[file:" + mf.getOriginalFilename() + ",size:" + mf.getSize() + "]";
                    } else {
                        filteredArgs[i] = args[i];
                    }
                }
                String raw = mapper.writeValueAsString(filteredArgs);
                if (raw.length() > 500) raw = raw.substring(0, 500) + "...";
                argsStr = redact(raw);
            } catch (Exception e) {
                argsStr = "[serialize failed]";
            }
        }

        log.info(">>> {} {} {}.{}() token=[{}] args={}", httpMethod, requestUri, cls, method, token, argsStr);

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            String resultStr = "";
            try {
                resultStr = mapper.writeValueAsString(result);
                if (resultStr.contains("`")) {
                    System.err.println("[ControllerLogAspect] BACKTICK DETECTED in serialized result! raw length=" + resultStr.length());
                    int idx = resultStr.indexOf('`');
                    System.err.println("[ControllerLogAspect] context: ..." + resultStr.substring(Math.max(0, idx - 30), Math.min(resultStr.length(), idx + 50)) + "...");
                }
                if (resultStr.length() > 500) resultStr = resultStr.substring(0, 500) + "...";
            } catch (Exception e) {
                resultStr = "[serialize failed]";
            }
            log.info("<<< {} {} {}.{}() {}ms result={}", httpMethod, requestUri, cls, method, duration, resultStr);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.warn("<<< {}.{}() {}ms exception={}", cls, method, duration, ex.getMessage());
            throw ex;
        }
    }
}