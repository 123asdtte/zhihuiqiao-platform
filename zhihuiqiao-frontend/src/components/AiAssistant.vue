<template>
  <div class="ai-assistant-wrapper">
    <!-- 悬浮按钮 -->
    <div class="ai-assistant-trigger" @click="togglePanel">
      <el-icon :size="24"><ChatDotRound /></el-icon>
      <span class="trigger-text">AI 助手</span>
    </div>

    <!-- 侧边对话面板 -->
    <transition name="slide">
      <div v-if="visible" class="ai-assistant-panel">
        <div class="panel-header">
          <div class="header-title">
            <el-icon :size="20"><Cpu /></el-icon>
            <span>智汇桥 AI 助手</span>
          </div>
          <el-icon class="close-icon" @click="togglePanel"><Close /></el-icon>
        </div>

        <div ref="messageListRef" class="message-list">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-item', msg.role]"
          >
            <div class="avatar">
              <el-icon v-if="msg.role === 'ai'" :size="18"><Cpu /></el-icon>
              <el-icon v-else :size="18"><User /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
            </div>
          </div>

          <div v-if="loading" class="message-item ai">
            <div class="avatar">
              <el-icon :size="18"><Cpu /></el-icon>
            </div>
            <div class="message-content">
              <el-icon class="typing"><Loading /></el-icon>
            </div>
          </div>
        </div>

        <div class="panel-footer">
          <div class="quick-questions">
            <span
              v-for="q in quickQuestions"
              :key="q"
              class="quick-tag"
              @click="sendQuickQuestion(q)"
            >
              {{ q }}
            </span>
          </div>
          <div class="input-area">
            <el-input
              v-model="inputValue"
              placeholder="输入问题，智汇桥 AI 为您解答"
              :disabled="loading"
              @keyup.enter="sendMessage"
            >
              <template #append>
                <el-button :loading="loading" @click="sendMessage">
                  <el-icon><Promotion /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Cpu, User, Close, Loading, Promotion } from '@element-plus/icons-vue'
import { chatWithAi } from '@/api/ai'

interface Message {
  role: 'user' | 'ai'
  content: string
}

const visible = ref(false)
const inputValue = ref('')
const loading = ref(false)
const messages = ref<Message[]>([
  {
    role: 'ai',
    content: '你好，我是智汇桥 AI 助手。我可以帮你了解平台功能、科研项目、学习资源等问题，请问有什么可以帮你的？'
  }
])
const messageListRef = ref<HTMLElement | null>(null)

const quickQuestions = [
  '推荐适合我的科研项目',
  '如何发布科研项目？',
  '怎么申请加入项目？',
  '平台有哪些功能？'
]

function togglePanel() {
  visible.value = !visible.value
}

function formatMessage(text: string) {
  // 简单处理换行
  return text.replace(/\n/g, '<br>')
}

async function sendMessage() {
  const question = inputValue.value.trim()
  if (!question || loading.value) {
    return
  }

  messages.value.push({ role: 'user', content: question })
  inputValue.value = ''
  loading.value = true

  try {
    const res: any = await chatWithAi(question)
    if (res.code === 200 && res.data) {
      messages.value.push({ role: 'ai', content: res.data })
    } else {
      messages.value.push({ role: 'ai', content: '抱歉，我暂时无法回答这个问题。' })
    }
  } catch (error) {
    ElMessage.error('AI 助手请求失败')
    messages.value.push({ role: 'ai', content: '抱歉，AI 助手暂时无法连接，请稍后再试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function sendQuickQuestion(question: string) {
  inputValue.value = question
  sendMessage()
}

function scrollToBottom() {
  nextTick(() => {
    const el = messageListRef.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}
</script>

<style scoped lang="scss">
.ai-assistant-wrapper {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
}

.ai-assistant-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 100px;
  background: linear-gradient(135deg, var(--zh-primary) 0%, #2c4a6e 100%);
  color: #fff;
  box-shadow: var(--zh-shadow-md);
  cursor: pointer;
  transition: all var(--zh-transition-fast);
  user-select: none;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--zh-shadow-lg);
  }

  .trigger-text {
    font-size: 14px;
    font-weight: 600;
  }
}

.ai-assistant-panel {
  position: fixed;
  right: 24px;
  bottom: 90px;
  width: 380px;
  height: 520px;
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  box-shadow: var(--zh-shadow-lg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: linear-gradient(135deg, var(--zh-primary) 0%, #2c4a6e 100%);
  color: #fff;

  .header-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
  }

  .close-icon {
    cursor: pointer;
    font-size: 18px;

    &:hover {
      opacity: 0.8;
    }
  }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: var(--zh-bg-secondary);
}

.message-item {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;

  &.user {
    flex-direction: row-reverse;

    .message-content {
      background: var(--zh-primary);
      color: #fff;
      border-bottom-right-radius: 4px;
    }
  }

  &.ai {
    .message-content {
      background: var(--zh-bg-elevated);
      color: var(--zh-text-primary);
      border-bottom-left-radius: 4px;
    }
  }

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(26, 54, 93, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    color: var(--zh-primary);
  }

  .message-content {
    max-width: 260px;
    padding: 10px 12px;
    border-radius: 12px;
    font-size: 13px;
    line-height: 1.6;
    box-shadow: var(--zh-shadow-sm);
  }

  .typing {
    color: var(--zh-text-tertiary);
  }
}

.panel-footer {
  padding: 12px 16px;
  background: var(--zh-bg-elevated);
  border-top: 1px solid var(--zh-border-light);

  .quick-questions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 10px;

    .quick-tag {
      padding: 4px 10px;
      border-radius: 100px;
      background: rgba(26, 54, 93, 0.06);
      color: var(--zh-primary);
      font-size: 12px;
      cursor: pointer;
      transition: all var(--zh-transition-fast);

      &:hover {
        background: rgba(26, 54, 93, 0.12);
      }
    }
  }

  .input-area {
    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px var(--zh-border-light) inset;
    }
  }
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
