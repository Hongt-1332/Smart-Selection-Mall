package edu.fafu.service.service.toolservice;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import edu.fafu.config.SystemConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WechatService {

    private static final Logger log = LoggerFactory.getLogger(WechatService.class);

    private final SystemConfig systemConfig;
    private final WxMaService wxMaService;
    private boolean mockMode = false;

    public WechatService(SystemConfig systemConfig, WxMaService wxMaService) {
        this.systemConfig = systemConfig;
        this.wxMaService = wxMaService;
    }

    @PostConstruct
    public void init() {
        String appSecret = systemConfig.getWechatAppSecret();
        if (appSecret == null || appSecret.isBlank()) {
            mockMode = true;
            log.warn("未配置 WECHAT_APP_SECRET，启用微信登录模拟模式（code 直接作为 openid）");
        }

        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(systemConfig.getWechatAppId());
        config.setSecret(appSecret != null ? appSecret : "");
        wxMaService.setWxMaConfig(config);
    }

    /**
     * 微信小程序 code 换取 openid 和 session_key
     * 模拟模式下：code 直接作为 openid，方便本地测试
     */
    public WxMaJscode2SessionResult code2Session(String code) throws Exception {
        if (mockMode) {
            log.info("【模拟模式】微信登录, code={} → openid=test_{}", code, code);
            WxMaJscode2SessionResult result = new WxMaJscode2SessionResult();
            result.setOpenid("test_" + code);
            result.setSessionKey("mock_session_key");
            return result;
        }

        WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
        log.info("微信登录成功, openid={}", session.getOpenid());
        return session;
    }

    /**
     * 解密微信小程序用户手机号
     */
    public WxMaPhoneNumberInfo getPhoneNumber(String sessionKey, String encryptedData, String ivStr) {
        if (mockMode) {
            log.warn("【模拟模式】无法解密手机号，需配置 WECHAT_APP_SECRET");
            return null;
        }
        return wxMaService.getUserService().getPhoneNoInfo(sessionKey, encryptedData, ivStr);
    }
}