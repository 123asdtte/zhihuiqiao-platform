<template>
  <div class="learning-resource-publish-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>发布学习资源</h2>
      <p>分享优质的课程、视频、论文、图书或工具，助力学习与科研</p>
    </div>

    <!-- 发布表单 -->
    <el-card shadow="never" class="publish-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 800px"
      >
        <!-- 资源名称 -->
        <el-form-item label="资源名称" prop="resourceName">
          <el-input v-model="form.resourceName" placeholder="请输入资源名称" clearable />
        </el-form-item>

        <!-- 资源类型 -->
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="form.resourceType" placeholder="请选择资源类型" style="width: 100%">
            <el-option label="在线课程" value="course" />
            <el-option label="教学视频" value="video" />
            <el-option label="学术论文" value="paper" />
            <el-option label="电子图书" value="book" />
            <el-option label="工具软件" value="tool" />
          </el-select>
        </el-form-item>

        <!-- 学科领域 -->
        <el-form-item label="学科领域" prop="subject">
          <el-select v-model="form.subject" placeholder="请选择学科领域" style="width: 100%">
            <el-option label="计算机科学" value="计算机科学" />
            <el-option label="人工智能" value="人工智能" />
            <el-option label="电子工程" value="电子工程" />
            <el-option label="数学" value="数学" />
            <el-option label="物理学" value="物理学" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <!-- 难度等级 -->
        <el-form-item label="难度等级" prop="difficultyLevel">
          <el-select v-model="form.difficultyLevel" placeholder="请选择难度等级" style="width: 100%">
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
          </el-select>
        </el-form-item>

        <!-- 封面图片 -->
        <el-form-item label="封面图片" prop="coverUrl">
          <ImageUpload v-model="form.coverUrl" />
          <div class="form-tip">建议上传 16:9 比例封面图，不上传则使用默认封面</div>
        </el-form-item>

        <!-- 内容附件 -->
        <el-form-item label="内容附件" prop="contentUrl">
          <FileUpload v-model="form.contentUrl" />
        </el-form-item>

        <!-- 资源描述 -->
        <el-form-item label="资源描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请描述资源的主要内容、适用人群、使用说明等"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            立即发布
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { publishLearningResource } from '@/api/learning'
import ImageUpload from '@/components/ImageUpload.vue'
import FileUpload from '@/components/FileUpload.vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const formRef = ref<FormInstance>()

// 提交中状态
const submitting = ref(false)

// 表单数据
const form = reactive({
  resourceName: '',
  resourceType: '',
  subject: '',
  difficultyLevel: '',
  coverUrl: '',
  contentUrl: '',
  description: ''
})

// 表单校验规则
const rules: FormRules = {
  resourceName: [
    { required: true, message: '请输入资源名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  resourceType: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ],
  subject: [
    { required: true, message: '请选择学科领域', trigger: 'change' }
  ],
  difficultyLevel: [
    { required: true, message: '请选择难度等级', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入资源描述', trigger: 'blur' }
  ]
}

/**
 * 提交发布表单
 */
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    if (!userStore.userInfo?.id) {
      ElMessage.warning('请先登录')
      return
    }

    submitting.value = true
    try {
      const submitData = {
        ...form,
        publisherId: userStore.userInfo.id
      }
      const res: any = await publishLearningResource(submitData)
      if (res.code === 200) {
        ElMessage.success('学习资源发布成功')
        router.push('/app/learning/resources')
      } else {
        ElMessage.error(res.message || '发布失败')
      }
    } catch (error) {
      ElMessage.error('发布失败，请稍后重试')
      console.error(error)
    } finally {
      submitting.value = false
    }
  })
}

/**
 * 重置表单
 */
function handleReset() {
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.learning-resource-publish-page {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    h2 {
      margin: 0 0 8px 0;
      color: #303133;
    }

    p {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .publish-card {
    padding: 20px;
  }
}
</style>
