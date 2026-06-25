<template>
  <div class="file-upload">
    <el-upload
      class="file-uploader"
      action="#"
      :auto-upload="true"
      :http-request="customUpload"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :disabled="uploading"
    >
      <el-button type="primary" :loading="uploading" :icon="Upload">
        {{ modelValue ? '重新上传' : '点击上传文件' }}
      </el-button>
    </el-upload>

    <div v-if="modelValue" class="file-info">
      <el-icon class="file-icon"><Document /></el-icon>
      <span class="file-name">{{ fileName || modelValue }}</span>
      <el-icon class="file-delete" @click="handleRemove"><CircleClose /></el-icon>
    </div>

    <div class="upload-tip">
      <slot name="tip">支持 pdf、doc、docx、xls、xlsx、ppt、txt、zip 等格式，最大 50MB</slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Document, CircleClose } from '@element-plus/icons-vue'
import { uploadFile } from '@/api/common'

/**
 * 通用文件上传组件 Props
 */
interface Props {
  modelValue: string
  fileName?: string
  maxSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  maxSize: 50
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'update:fileName', value: string): void
}>()

const uploading = ref(false)

/**
 * 上传前的校验
 */
function beforeUpload(file: File) {
  const validTypes = [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-powerpoint',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'text/plain',
    'application/zip',
    'application/x-rar-compressed',
    'application/x-7z-compressed'
  ]
  if (!validTypes.includes(file.type)) {
    ElMessage.error('不支持的文件格式')
    return false
  }
  if (file.size > props.maxSize * 1024 * 1024) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

/**
 * 自定义上传逻辑
 */
async function customUpload(options: any) {
  uploading.value = true
  try {
    const res: any = await uploadFile(options.file, 'file')
    if (res.code === 200 && res.data?.url) {
      emit('update:modelValue', res.data.url)
      if (res.data.filename) {
        emit('update:fileName', res.data.filename)
      }
      ElMessage.success('文件上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('文件上传失败')
    console.error(error)
  } finally {
    uploading.value = false
  }
}

/**
 * 删除文件
 */
function handleRemove() {
  emit('update:modelValue', '')
  emit('update:fileName', '')
}
</script>

<style scoped lang="scss">
.file-upload {
  .file-uploader {
    display: inline-block;
    margin-right: var(--zh-space-3);
  }

  .file-info {
    display: inline-flex;
    align-items: center;
    gap: var(--zh-space-2);
    margin-top: var(--zh-space-3);
    padding: var(--zh-space-2) var(--zh-space-3);
    background: var(--zh-bg-warm);
    border: 1px solid var(--zh-border-light);
    border-radius: var(--zh-radius-sm);

    .file-icon {
      color: var(--zh-primary);
      font-size: 18px;
    }

    .file-name {
      font-size: 13px;
      color: var(--zh-text-secondary);
      max-width: 300px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .file-delete {
      color: var(--zh-danger);
      cursor: pointer;
      font-size: 16px;

      &:hover {
        opacity: 0.8;
      }
    }
  }

  .upload-tip {
    margin-top: var(--zh-space-2);
    font-size: 12px;
    color: var(--zh-text-tertiary);
  }
}
</style>
