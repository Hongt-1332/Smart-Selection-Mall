<template>
  <div class="about">
    <div class="about-container">
      <!-- 品牌介绍 -->
      <section class="about-section hero-brand">
        <div class="brand-icon">✦</div>
        <h2 class="brand-title">智选商城</h2>
        <p class="brand-slogan">品质生活 · 一站直达</p>
        <p class="brand-desc">
          智选商城成立于 2024 年，致力于为消费者提供高品质、高性价比的商品与卓越的购物体验。
          我们与全球数千家优质品牌深度合作，覆盖数码电子、服饰美妆、家居生活、食品生鲜等全品类商品。
        </p>
      </section>

      <!-- 核心数据 -->
      <section class="about-section">
        <h3 class="section-title">平台数据</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <span class="stat-num">5000+</span>
            <span class="stat-label">合作品牌</span>
          </div>
          <div class="stat-card">
            <span class="stat-num">200万+</span>
            <span class="stat-label">注册用户</span>
          </div>
          <div class="stat-card">
            <span class="stat-num">50万+</span>
            <span class="stat-label">在售商品</span>
          </div>
          <div class="stat-card">
            <span class="stat-num">99.6%</span>
            <span class="stat-label">好评率</span>
          </div>
        </div>
      </section>

      <!-- 技术栈 -->
      <section class="about-section">
        <h3 class="section-title">技术架构</h3>
        <div class="tech-grid">
          <div class="tech-item">
            <div class="tech-icon">🖥</div>
            <span class="tech-name">Vue 3</span>
            <span class="tech-ver">v3.4+</span>
          </div>
          <div class="tech-item">
            <div class="tech-icon">📘</div>
            <span class="tech-name">TypeScript</span>
            <span class="tech-ver">v5.0+</span>
          </div>
          <div class="tech-item">
            <div class="tech-icon">🎨</div>
            <span class="tech-name">Element Plus</span>
            <span class="tech-ver">v2.6+</span>
          </div>
          <div class="tech-item">
            <div class="tech-icon">🍃</div>
            <span class="tech-name">Spring Boot</span>
            <span class="tech-ver">v3.2+</span>
          </div>
          <div class="tech-item">
            <div class="tech-icon">🐬</div>
            <span class="tech-name">MySQL</span>
            <span class="tech-ver">v8.0+</span>
          </div>
          <div class="tech-item">
            <div class="tech-icon">📦</div>
            <span class="tech-name">Redis</span>
            <span class="tech-ver">v7.0+</span>
          </div>
        </div>
      </section>

      <!-- 核心功能 -->
      <section class="about-section">
        <h3 class="section-title">核心功能</h3>
        <div class="feature-list">
          <div class="feature-item">
            <span class="feature-icon">🛒</span>
            <div class="feature-content">
              <strong>智能购物</strong>
              <p>AI 智能推荐引擎，根据您的浏览和购买记录，精准推荐最适合的商品</p>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon">👤</span>
            <div class="feature-content">
              <strong>用户中心</strong>
              <p>完整的订单管理、收藏夹、收货地址管理，一站式个人中心</p>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon">🏪</span>
            <div class="feature-content">
              <strong>商户中心</strong>
              <p>支持商家入驻、商品上架、库存管理、销售数据分析</p>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon">⚙</span>
            <div class="feature-content">
              <strong>系统管理</strong>
              <p>用户权限管理、订单审核、数据统计、系统配置等多维度后台管理</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 联系方式 -->
      <section class="about-section">
        <h3 class="section-title">联系我们</h3>
        <div class="contact-grid">
          <div class="contact-item">
            <span class="contact-label">客服邮箱</span>
            <span class="contact-value">service@youxuan.com</span>
          </div>
          <div class="contact-item">
            <span class="contact-label">客服热线</span>
            <span class="contact-value">400-888-8888</span>
          </div>
          <div class="contact-item">
            <span class="contact-label">工作时间</span>
            <span class="contact-value">周一至周日 9:00 - 21:00</span>
          </div>
          <div class="contact-item">
            <span class="contact-label">公司地址</span>
            <span class="contact-value">北京市朝阳区科技园区优选大厦</span>
          </div>
        </div>
      </section>

      <!-- 底部版权 -->
      <footer class="about-footer">
        <p>&copy; 2024 - 2026 智选商城 | Version 1.0.0 | All Rights Reserved</p>
        <p class="footer-sub">京ICP备2024000001号 | 京公网安备 11010000000001号</p>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.about {
  min-height: calc(100vh - 60px);
  padding: 20px;
  position: relative;
}

.about-container {
  position: relative;
  z-index: 1;
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.about-section {
  background: rgba(18, 20, 28, 0.45);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  padding: 28px 32px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

/* ===== 品牌介绍 ===== */
.hero-brand {
  text-align: center;
  background: linear-gradient(135deg,
    rgba(99, 102, 241, 0.08) 0%,
    rgba(18, 20, 28, 0.75) 50%,
    rgba(168, 85, 247, 0.06) 100%
  );
}

.brand-icon {
  font-size: 42px;
  color: #a78bfa;
  margin-bottom: 12px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.brand-title {
  font-size: 30px;
  color: #f5f5f5;
  margin: 0 0 6px;
  font-weight: 700;
  letter-spacing: 2px;
}

.brand-slogan {
  font-size: 16px;
  color: #a78bfa;
  margin: 0 0 16px;
}

.brand-desc {
  font-size: 14px;
  color: #9ca3af;
  line-height: 1.8;
  max-width: 650px;
  margin: 0 auto;
}

/* ===== 通用标题 ===== */
.section-title {
  font-size: 18px;
  color: #e5e7eb;
  margin: 0 0 20px;
  font-weight: 600;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

/* ===== 平台数据 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  text-align: center;
  padding: 20px 12px;
  background: rgba(26, 28, 38, 0.35);
  backdrop-filter: blur(8px);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: transform 0.2s, background 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  background: rgba(30, 32, 44, 0.5);
}

.stat-num {
  display: block;
  font-size: 26px;
  font-weight: 700;
  color: #a78bfa;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

/* ===== 技术栈 ===== */
.tech-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
}

.tech-item {
  text-align: center;
  padding: 18px 10px;
  background: rgba(26, 28, 38, 0.3);
  backdrop-filter: blur(8px);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.04);
  transition: all 0.25s;
}

.tech-card:hover {
  background: rgba(30, 32, 44, 0.5);
  border-color: rgba(99, 102, 241, 0.3);
  transform: translateY(-2px);
}

.tech-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.tech-name {
  display: block;
  font-size: 13px;
  color: #d1d5db;
  font-weight: 500;
}

.tech-ver {
  display: block;
  font-size: 11px;
  color: #6b7280;
  margin-top: 4px;
}

/* ===== 核心功能 ===== */
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.feature-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: rgba(26, 28, 38, 0.3);
  backdrop-filter: blur(8px);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.04);
  transition: all 0.2s;
}

.feature-card:hover {
  background: rgba(30, 32, 44, 0.45);
  border-color: rgba(255, 255, 255, 0.08);
}

.feature-icon {
  font-size: 28px;
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 10px;
}

.feature-content strong {
  display: block;
  font-size: 15px;
  color: #e5e7eb;
  margin-bottom: 4px;
}

.feature-content p {
  margin: 0;
  font-size: 13px;
  color: #9ca3af;
  line-height: 1.5;
}

/* ===== 联系我们 ===== */
.contact-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.contact-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px;
  background: rgba(26, 28, 38, 0.3);
  backdrop-filter: blur(8px);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.contact-label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.contact-value {
  font-size: 14px;
  color: #d1d5db;
  font-weight: 500;
}

/* ===== 底部 ===== */
.about-footer {
  text-align: center;
  padding: 20px 0 8px;
}

.about-footer p {
  margin: 0 0 4px;
  font-size: 12px;
  color: #4b5563;
}

.footer-sub {
  font-size: 11px !important;
  color: #374151 !important;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .tech-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .contact-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .about {
    padding: 10px;
  }
  .about-container {
    gap: 12px;
  }
  .about-section {
    padding: 20px 16px;
  }
  .brand-title {
    font-size: 24px;
  }
  .brand-desc {
    font-size: 13px;
  }
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  .stat-num {
    font-size: 22px;
  }
  .tech-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
  }
  .tech-icon {
    font-size: 22px;
  }
  .contact-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  .feature-item {
    gap: 10px;
    padding: 12px;
  }
  .feature-icon {
    width: 38px;
    height: 38px;
    font-size: 22px;
  }
}
</style>