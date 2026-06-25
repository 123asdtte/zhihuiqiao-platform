<template>
  <div class="auth-layout">
    <!-- 左侧品牌区 -->
    <aside class="auth-brand">
      <div class="brand-texture"></div>
      <div class="brand-content">
        <div class="brand-mark">
          <div class="mark-icon">
            <el-icon :size="32"><Reading /></el-icon>
          </div>
          <span class="mark-label">智汇桥</span>
        </div>

        <h1 class="brand-title">
          连接知识、<br />
          <span class="brand-accent">资源与机遇</span>
        </h1>
        <p class="brand-desc">
          AI 驱动的产学研用一体化智慧协同系统，<br />
          让科研合作、资源流转与教学辅助更加高效。
        </p>

        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>科研撮合</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>资源流转</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>教学辅助</span>
          </div>
        </div>
      </div>

      <div class="brand-decoration">
        <div class="deco-circle"></div>
        <div class="deco-line"></div>
        <div class="deco-square"></div>
      </div>
    </aside>

    <!-- 右侧表单区 -->
    <main class="auth-form-area">
      <div class="form-scroll">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { Reading } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.auth-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr minmax(420px, 480px);
  background: var(--zh-bg);
}

// ==================== 左侧品牌区 ====================
.auth-brand {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: var(--zh-space-12) var(--zh-space-10);
  background: linear-gradient(160deg, var(--zh-primary) 0%, #0f2748 100%);
  color: #fff;
  overflow: hidden;

  .brand-texture {
    position: absolute;
    inset: 0;
    opacity: 0.08;
    background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23ffffff' fill-opacity='0.6' fill-rule='evenodd'/%3E%3C/svg%3E");
  }

  .brand-content {
    position: relative;
    z-index: 1;
    max-width: 560px;
  }

  .brand-mark {
    display: inline-flex;
    align-items: center;
    gap: var(--zh-space-3);
    margin-bottom: var(--zh-space-8);

    .mark-icon {
      width: 52px;
      height: 52px;
      border-radius: var(--zh-radius);
      background: var(--zh-accent);
      color: var(--zh-primary);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
    }

    .mark-label {
      font-family: var(--zh-font-display);
      font-size: 24px;
      font-weight: 700;
      letter-spacing: 0.1em;
    }
  }

  .brand-title {
    font-family: var(--zh-font-display);
    font-size: clamp(40px, 5vw, 64px);
    font-weight: 700;
    line-height: 1.15;
    margin: 0 0 var(--zh-space-6) 0;
    letter-spacing: -0.02em;

    .brand-accent {
      color: var(--zh-accent);
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: 8px;
        left: 0;
        width: 100%;
        height: 8px;
        background: rgba(201, 162, 39, 0.3);
        border-radius: 4px;
      }
    }
  }

  .brand-desc {
    font-size: 16px;
    line-height: 1.8;
    opacity: 0.8;
    margin: 0 0 var(--zh-space-10) 0;
  }

  .brand-features {
    display: flex;
    gap: var(--zh-space-8);

    .feature-item {
      display: flex;
      align-items: center;
      gap: var(--zh-space-3);
      font-size: 15px;
      font-weight: 500;

      .feature-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: var(--zh-accent);
        box-shadow: 0 0 0 4px rgba(201, 162, 39, 0.25);
      }
    }
  }
}

// ==================== 装饰元素 ====================
.brand-decoration {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 320px;
  height: 320px;
  opacity: 0.25;
  pointer-events: none;

  .deco-circle {
    position: absolute;
    top: 60px;
    right: 80px;
    width: 120px;
    height: 120px;
    border-radius: 50%;
    border: 2px solid var(--zh-accent);
  }

  .deco-line {
    position: absolute;
    top: 120px;
    right: 160px;
    width: 100px;
    height: 2px;
    background: rgba(255, 255, 255, 0.5);
    transform: rotate(-30deg);
  }

  .deco-square {
    position: absolute;
    bottom: 60px;
    right: 60px;
    width: 80px;
    height: 80px;
    border: 2px solid rgba(255, 255, 255, 0.4);
    transform: rotate(15deg);
  }
}

// ==================== 右侧表单区 ====================
.auth-form-area {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: var(--zh-bg-elevated);
  padding: var(--zh-space-8);
  overflow-y: auto;

  .form-scroll {
    width: 100%;
    max-width: 420px;
    margin: 0 auto;
  }
}

// ==================== 响应式 ====================
@media (max-width: 992px) {
  .auth-layout {
    grid-template-columns: 1fr;
  }

  .auth-brand {
    display: none;
  }

  .auth-form-area {
    min-height: 100vh;
    padding: var(--zh-space-6);
  }
}
</style>
