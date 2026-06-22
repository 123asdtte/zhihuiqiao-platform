<template>
  <div class="auth-layout">
    <div class="auth-container">
      <div class="auth-header">
        <span class="auth-logo">🌉</span>
        <h1 class="auth-title">智汇桥</h1>
        <p class="auth-subtitle">AI驱动的产学研用一体化智慧协同系统</p>
      </div>
      <div class="auth-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-container {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;

  .auth-logo {
    font-size: 48px;
    display: block;
    margin-bottom: 12px;
  }

  .auth-title {
    font-size: 28px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0 0 8px 0;
    letter-spacing: 4px;
  }

  .auth-subtitle {
    font-size: 13px;
    color: #909399;
    margin: 0;
  }
}

.auth-content {
  width: 100%;
}
</style>
