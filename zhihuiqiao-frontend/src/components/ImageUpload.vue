<template>
  <div class="image-upload">
    <!-- 单图模式 -->
    <template v-if="!multiple">
      <div v-if="modelValue" class="image-preview single-preview">
        <el-image :src="modelValue as string" fit="cover" class="preview-img" />
        <div class="image-actions">
          <el-icon class="action-icon" @click="handlePreview(modelValue as string)"><View /></el-icon>
          <el-icon class="action-icon" @click="handleRemove"><Delete /></el-icon>
        </div>
      </div>
      <el-upload
        v-else
        class="uploader"
        action="#"
        :auto-upload="true"
        :http-request="customUpload"
        :show-file-list="false"
        :before-upload="beforeUpload"
        accept="image/*"
      >
        <div class="uploader-trigger">
          <el-icon class="uploader-icon"><Plus /></el-icon>
          <span class="uploader-text">点击上传图片</span>
        </div>
      </el-upload>
    </template>

    <!-- 多图模式 -->
    <template v-else>
      <div class="image-list">
        <div v-for="(url, index) in imageList" :key="index" class="image-preview multi-preview">
          <el-image :src="url" fit="cover" class="preview-img" />
          <div class="image-actions">
            <el-icon class="action-icon" @click="handlePreview(url)"><View /></el-icon>
            <el-icon class="action-icon" @click="handleRemove(index)"><Delete /></el-icon>
          </div>
        </div>
        <el-upload
          v-if="imageList.length < maxCount"
          class="uploader multi-uploader"
          action="#"
          :auto-upload="true"
          :http-request="customUpload"
          :show-file-list="false"
          :before-upload="beforeUpload"
          accept="image/*"
        >
          <div class="uploader-trigger">
            <el-icon class="uploader-icon"><Plus /></el-icon>
            <span class="uploader-text">上传图片</span>
          </div>
        </el-upload>
      </div>
    </template>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="previewList"
      :initial-index="previewIndex"
      @close="previewVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, View, Delete } from '@element-plus/icons-vue'
import { uploadFile } from '@/api/common'

/**
 * 图片上传组件 Props
 */
interface Props {
  modelValue: string | string[]
  multiple?: boolean
  maxCount?: number
  maxSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  multiple: false,
  maxCount: 9,
  maxSize: 10
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | string[]): void
}>()

// 多图模式下本地维护的图片列表
const imageList = computed({
  get: () => (Array.isArray(props.modelValue) ? props.modelValue : props.modelValue ? [props.modelValue] : []),
  set: (val: string[]) => {
    if (props.multiple) {
      emit('update:modelValue', val)
    } else {
      emit('update:modelValue', val[0] || '')
    }
  }
})

// 图片预览相关
const previewVisible = ref(false)
const previewIndex = ref(0)
const previewList = computed(() => (props.multiple ? imageList.value : [props.modelValue as string]))

/**
 * 监听 modelValue 变化，确保多图模式数据同步
 */
watch(
  () => props.modelValue,
  (val) => {
    if (props.multiple && !Array.isArray(val)) {
      emit('update:modelValue', val ? [val] : [])
    }
  },
  { immediate: true }
)

/**
 * 上传前的校验
 */
function beforeUpload(file: File) {
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/bmp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('仅支持 jpg、png、gif、webp、bmp 格式的图片')
    return false
  }
  if (file.size > props.maxSize * 1024 * 1024) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

/**
 * 自定义上传逻辑
 */
async function customUpload(options: any) {
  try {
    const res: any = await uploadFile(options.file, 'image')
    if (res.code === 200 && res.data?.url) {
      if (props.multiple) {
        imageList.value = [...imageList.value, res.data.url]
      } else {
        emit('update:modelValue', res.data.url)
      }
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('图片上传失败')
    console.error(error)
  }
}

/**
 * 删除图片
 */
function handleRemove(index?: number) {
  if (props.multiple && typeof index === 'number') {
    const list = [...imageList.value]
    list.splice(index, 1)
    imageList.value = list
  } else {
    emit('update:modelValue', props.multiple ? [] : '')
  }
}

/**
 * 预览图片
 */
function handlePreview(url: string) {
  previewIndex.value = imageList.value.indexOf(url)
  previewVisible.value = true
}
</script>

<style scoped lang="scss">
.image-upload {
  .uploader {
    display: inline-block;

    :deep(.el-upload) {
      border: 1px dashed var(--zh-border-color);
      border-radius: var(--zh-radius);
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--zh-transition-fast);

      &:hover {
        border-color: var(--zh-primary);
      }
    }
  }

  .uploader-trigger {
    width: 148px;
    height: 148px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--zh-text-tertiary);
    background: var(--zh-bg-warm);

    .uploader-icon {
      font-size: 28px;
      margin-bottom: 8px;
    }

    .uploader-text {
      font-size: 12px;
    }
  }

  .image-preview {
    position: relative;
    width: 148px;
    height: 148px;
    border-radius: var(--zh-radius);
    overflow: hidden;
    border: 1px solid var(--zh-border-light);

    .preview-img {
      width: 100%;
      height: 100%;
      display: block;
    }

    .image-actions {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16px;
      opacity: 0;
      transition: opacity var(--zh-transition-fast);

      .action-icon {
        color: #fff;
        font-size: 20px;
        cursor: pointer;

        &:hover {
          color: var(--zh-accent-light);
        }
      }
    }

    &:hover .image-actions {
      opacity: 1;
    }
  }

  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: var(--zh-space-3);

    .multi-preview,
    .multi-uploader {
      width: 120px;
      height: 120px;

      .uploader-trigger {
        width: 120px;
        height: 120px;
      }
    }
  }
}
</style>
