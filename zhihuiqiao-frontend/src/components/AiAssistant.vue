<template>
  <div class="ai-assistant-wrapper">
    <!-- 悬浮触发按钮：点击展开/收起 AI 面板 -->
    <div class="ai-assistant-trigger" @click="togglePanel">
      <el-icon :size="24"><ChatDotRound /></el-icon>
      <span class="trigger-text">AI 助手</span>
    </div>

    <!-- 面板打开时的背景遮罩，点击可关闭面板 -->
    <transition name="fade">
      <div v-if="visible" class="ai-assistant-overlay" @click="togglePanel"></div>
    </transition>

    <!-- AI 对话侧边面板 -->
    <transition name="slide">
      <div v-if="visible" class="ai-assistant-panel">
        <!-- 面板头部：标题 + 操作按钮 -->
        <div class="panel-header">
          <div class="header-title">
            <div class="header-avatar">
              <el-icon :size="18"><Cpu /></el-icon>
            </div>
            <span>智汇桥 AI 助手</span>
          </div>
          <div class="header-actions">
            <el-tooltip content="新对话" placement="top">
              <el-icon class="header-action-icon" @click="clearConversation"><Delete /></el-icon>
            </el-tooltip>
            <el-icon class="close-icon" @click="togglePanel"><Close /></el-icon>
          </div>
        </div>

        <!-- 消息列表区域 -->
        <div ref="messageListRef" class="message-list">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-item', msg.role]"
          >
            <!-- AI / 用户头像 -->
            <div class="avatar">
              <el-icon v-if="msg.role === 'ai'" :size="18"><Cpu /></el-icon>
              <el-icon v-else :size="18"><User /></el-icon>
            </div>
            <!-- 消息内容：AI 使用 Markdown 渲染，用户保持纯文本 -->
            <div class="message-content">
              <div
                class="message-text"
                v-html="msg.role === 'ai' ? renderMarkdown(msg.content) : formatPlainText(msg.content)"
              ></div>
              <!-- AI 消息操作栏：复制 -->
              <div v-if="msg.role === 'ai'" class="message-actions">
                <el-tooltip content="复制内容" placement="bottom">
                  <el-icon class="action-icon" @click="copyMessage(msg.content)"><DocumentCopy /></el-icon>
                </el-tooltip>
              </div>
            </div>
          </div>

          <!-- AI 正在输入的占位状态 -->
          <div v-if="loading" class="message-item ai">
            <div class="avatar">
              <el-icon :size="18"><Cpu /></el-icon>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 面板底部：快捷问题 + 输入区 -->
        <div class="panel-footer">
          <!-- 快捷问题：水平滚动，点击即可发送 -->
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

          <!-- 输入区：多行文本框，支持 Shift+Enter 换行，Enter 发送 -->
          <div class="input-area">
            <el-input
              ref="inputRef"
              v-model="inputValue"
              type="textarea"
              :rows="1"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="输入问题，智汇桥 AI 为您解答"
              :disabled="loading"
              resize="none"
              @keydown="handleInputKeydown"
            />
            <el-button
              class="send-btn"
              type="primary"
              :loading="loading"
              :disabled="!inputValue.trim()"
              @click="sendMessage"
            >
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Cpu,
  User,
  Close,
  Promotion,
  DocumentCopy,
  Delete
} from '@element-plus/icons-vue'
import { chatWithAi, getRecommendedProjects } from '@/api/ai'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

/**
 * 消息对象定义
 */
interface Message {
  role: 'user' | 'ai'
  content: string
}

// 面板显隐状态
const visible = ref(false)
// 当前输入内容
const inputValue = ref('')
// AI 请求加载状态
const loading = ref(false)
// 消息列表
const messages = ref<Message[]>([
  {
    role: 'ai',
    content:
      '你好，我是智汇桥 AI 助手。我可以帮你了解平台功能、科研项目、学习资源等问题，请问有什么可以帮你的？'
  }
])

// DOM 引用：消息列表用于自动滚动，输入框用于聚焦
const messageListRef = ref<HTMLElement | null>(null)
const inputRef = ref<any>(null)

// 快捷问题列表
const quickQuestions = [
  '推荐适合我的科研项目',
  '如何发布科研项目？',
  '怎么申请加入项目？',
  '平台有哪些功能？'
]

/**
 * 切换面板显示状态
 * 打开时自动聚焦输入框，并监听 Esc 键；关闭时移除监听
 */
function togglePanel() {
  visible.value = !visible.value
  if (visible.value) {
    nextTick(() => {
      inputRef.value?.focus?.()
      scrollToBottom()
    })
    document.addEventListener('keydown', handleEscKey)
  } else {
    document.removeEventListener('keydown', handleEscKey)
  }
}

/**
 * Esc 键关闭面板
 */
function handleEscKey(event: KeyboardEvent) {
  if (event.key === 'Escape' && visible.value) {
    togglePanel()
  }
}

/**
 * 渲染 Markdown 内容，并使用 DOMPurify 做 XSS 过滤
 */
function renderMarkdown(text: string): string {
  const html = marked.parse(text, { async: false }) as string
  return DOMPurify.sanitize(html)
}

/**
 * 纯文本消息简单格式化：仅把换行转成 <br>
 */
function formatPlainText(text: string): string {
  return text.replace(/\n/g, '<br>')
}

/**
 * 复制 AI 消息内容到剪贴板
 */
async function copyMessage(content: string) {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

/**
 * 清空对话，恢复默认欢迎语
 */
function clearConversation() {
  messages.value = [
    {
      role: 'ai',
      content:
        '你好，我是智汇桥 AI 助手。我可以帮你了解平台功能、科研项目、学习资源等问题，请问有什么可以帮你的？'
    }
  ]
  nextTick(() => scrollToBottom())
}

/**
 * 发送消息
 */
async function sendMessage() {
  const question = inputValue.value.trim()
  if (!question || loading.value) {
    return
  }

  messages.value.push({ role: 'user', content: question })
  inputValue.value = ''
  loading.value = true

  try {
    // 科研项目推荐类问题，调用 DeepSeek 智能推荐接口
    if (isRecommendQuestion(question)) {
      const res: any = await getRecommendedProjects()
      if (res.code === 200 && res.data && res.data.length > 0) {
        const reply = formatRecommendations(res.data)
        messages.value.push({ role: 'ai', content: reply })
      } else {
        messages.value.push({
          role: 'ai',
          content:
            '当前没有合适的推荐项目，建议你完善个人资料和科研画像后再来获取个性化推荐。'
        })
      }
    } else {
      const res: any = await chatWithAi(question)
      if (res.code === 200 && res.data) {
        messages.value.push({ role: 'ai', content: res.data })
      } else {
        messages.value.push({ role: 'ai', content: '抱歉，我暂时无法回答这个问题。' })
      }
    }
  } catch (error) {
    ElMessage.error('AI 助手请求失败')
    messages.value.push({ role: 'ai', content: '抱歉，AI 助手暂时无法连接，请稍后再试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

/**
 * 判断是否为科研项目推荐意图
 */
function isRecommendQuestion(question: string): boolean {
  const lower = question.toLowerCase()
  const keywords = ['推荐', '科研项目', '推荐项目', '适合我的项目', '项目推荐']
  return keywords.some((keyword) => lower.includes(keyword))
}

/**
 * 格式化推荐结果：展示推荐理由，而不仅是固定文案
 */
function formatRecommendations(list: any[]): string {
  let sb = '根据你的个人信息和科研画像，为你推荐以下科研项目：\n\n'
  list.forEach((item, index) => {
    const project = item.project || item
    const reason = item.reason || '与你匹配度较高'
    sb += `${index + 1}. ${project.projectName || '未命名项目'}\n`
    sb += `   推荐理由：${reason}\n`
    sb += `   简介：${project.projectDescription || '暂无简介'}\n`
    sb += `   类型：${project.projectType || '未分类'} | 领域：${project.researchFields || '未填写'}\n\n`
  })
  sb += '你可以点击卡片查看详情，或进入“科研项目”模块浏览全部项目。'
  return sb
}

/**
 * 发送快捷问题
 */
function sendQuickQuestion(question: string) {
  inputValue.value = question
  sendMessage()
}

/**
 * 输入框键盘事件：Enter 发送，Shift+Enter 换行
 * @param event 键盘或通用事件对象
 */
function handleInputKeydown(event: KeyboardEvent | Event) {
  const e = event as KeyboardEvent
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

/**
 * 滚动消息列表到底部
 */
function scrollToBottom() {
  nextTick(() => {
    const el = messageListRef.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

/**
 * 组件卸载时移除全局事件监听，避免内存泄漏
 */
onUnmounted(() => {
  document.removeEventListener('keydown', handleEscKey)
})

/**
 * 页面加载时预配置 marked：允许换行符渲染为 <br>
 */
onMounted(() => {
  marked.setOptions({
    breaks: true,
    gfm: true
  })
})
</script>

<style scoped lang="scss">
// 悬浮按钮与面板共用容器
.ai-assistant-wrapper {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
}

// 悬浮触发按钮
.ai-assistant-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
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

// 背景遮罩：聚焦面板，点击关闭
.ai-assistant-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.25);
  z-index: 9998;
}

// AI 对话面板
.ai-assistant-panel {
  position: fixed;
  right: 24px;
  bottom: 90px;
  width: 460px;
  height: 620px;
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius-lg);
  box-shadow: var(--zh-shadow-xl);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 9999;
}

// 面板头部
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: linear-gradient(135deg, var(--zh-primary) 0%, #2c4a6e 100%);
  color: #fff;

  .header-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 15px;
    font-weight: 600;

    .header-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 14px;

    .header-action-icon,
    .close-icon {
      cursor: pointer;
      font-size: 18px;
      transition: opacity 0.2s;

      &:hover {
        opacity: 0.75;
      }
    }
  }
}

// 消息列表
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 18px;
  background: var(--zh-bg-secondary);
}

// 单条消息
.message-item {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;

  // 用户消息靠右
  &.user {
    flex-direction: row-reverse;

    .message-content {
      background: var(--zh-primary);
      color: #fff;
      border-bottom-right-radius: 4px;
    }
  }

  // AI 消息靠左
  &.ai {
    .message-content {
      background: var(--zh-bg-elevated);
      color: var(--zh-text-primary);
      border-bottom-left-radius: 4px;
    }
  }

  .avatar {
    width: 34px;
    height: 34px;
    border-radius: 50%;
    background: rgba(26, 54, 93, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    color: var(--zh-primary);
  }

  .message-content {
    max-width: 85%;
    padding: 12px 14px;
    border-radius: 14px;
    font-size: 14px;
    line-height: 1.7;
    box-shadow: var(--zh-shadow-sm);
    position: relative;
  }

  .message-text {
    word-break: break-word;
  }

  // 消息操作栏：复制等
  .message-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 8px;
    padding-top: 6px;
    border-top: 1px solid rgba(0, 0, 0, 0.05);

    .action-icon {
      font-size: 14px;
      color: var(--zh-text-tertiary);
      cursor: pointer;

      &:hover {
        color: var(--zh-primary);
      }
    }
  }
}

// 输入中动画：三颗跳动圆点
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 2px;

  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--zh-text-tertiary);
    animation: typingBounce 1.4s infinite ease-in-out both;

    &:nth-child(1) {
      animation-delay: -0.32s;
    }

    &:nth-child(2) {
      animation-delay: -0.16s;
    }
  }
}

@keyframes typingBounce {
  0%,
  80%,
  100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

// 面板底部：快捷问题 + 输入区
.panel-footer {
  padding: 12px 16px;
  background: var(--zh-bg-elevated);
  border-top: 1px solid var(--zh-border-light);

  // 快捷问题横向滚动
  .quick-questions {
    display: flex;
    flex-wrap: nowrap;
    gap: 8px;
    margin-bottom: 10px;
    overflow-x: auto;
    padding-bottom: 4px;

    // 隐藏滚动条但保留滚动能力
    &::-webkit-scrollbar {
      height: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--zh-border);
      border-radius: 2px;
    }

    .quick-tag {
      flex-shrink: 0;
      padding: 5px 12px;
      border-radius: 100px;
      background: rgba(26, 54, 93, 0.06);
      color: var(--zh-primary);
      font-size: 12px;
      cursor: pointer;
      transition: all var(--zh-transition-fast);
      white-space: nowrap;

      &:hover {
        background: rgba(26, 54, 93, 0.12);
      }
    }
  }

  // 输入区：文本域 + 发送按钮
  .input-area {
    display: flex;
    align-items: flex-end;
    gap: 10px;

    :deep(.el-textarea__inner) {
      min-height: 44px !important;
      max-height: 120px;
      border-radius: 12px;
      padding: 10px 14px;
      resize: none;
      box-shadow: 0 0 0 1px var(--zh-border-light) inset;
    }

    .send-btn {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      padding: 0;
      flex-shrink: 0;
    }
  }
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.96);
}

// Markdown 内容基础样式：列表、代码块、引用等
.message-text {
  :deep(p) {
    margin: 0 0 10px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(ul),
  :deep(ol) {
    margin: 0 0 10px 18px;
    padding: 0;
  }

  :deep(li) {
    margin-bottom: 4px;
  }

  :deep(pre) {
    background: #1e1e1e;
    color: #d4d4d4;
    padding: 12px;
    border-radius: 8px;
    overflow-x: auto;
    font-size: 13px;
    margin: 10px 0;
  }

  :deep(code) {
    font-family: 'Fira Code', 'Consolas', monospace;
  }

  :deep(:not(pre) > code) {
    background: rgba(26, 54, 93, 0.08);
    color: var(--zh-primary);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 13px;
  }

  :deep(blockquote) {
    margin: 10px 0;
    padding-left: 12px;
    border-left: 3px solid var(--zh-border);
    color: var(--zh-text-secondary);
  }

  :deep(a) {
    color: var(--zh-primary);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
