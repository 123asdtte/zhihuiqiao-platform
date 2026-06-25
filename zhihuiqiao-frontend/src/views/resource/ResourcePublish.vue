<template>
  <div class="resource-publish-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>发布闲置资源</h2>
      <p>将您闲置的设备、图书、场地等资源发布出来，供校内师生预约借用</p>
    </div>

    <el-card shadow="never" class="publish-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 800px"
      >
        <el-form-item label="资源名称" prop="resourceName">
          <el-input v-model="form.resourceName" placeholder="请输入资源名称" clearable />
        </el-form-item>

        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="form.resourceType" placeholder="请选择资源类型" style="width: 100%">
            <el-option label="实验设备" value="实验设备" />
            <el-option label="图书资料" value="图书资料" />
            <el-option label="办公用品" value="办公用品" />
            <el-option label="电子数码" value="电子数码" />
            <el-option label="场地空间" value="场地空间" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="存放位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入资源存放位置，如：计算机楼 302 室" clearable />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原价" prop="originalPrice">
              <el-input-number
                v-model="form.originalPrice"
                :min="0"
                :precision="2"
                :step="10"
                style="width: 100%"
                placeholder="请输入原价"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="租赁价格" prop="rentalPrice">
              <el-input-number
                v-model="form.rentalPrice"
                :min="0"
                :precision="2"
                :step="1"
                style="width: 100%"
                placeholder="0 表示免费"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="资源图片" prop="images">
          <ImageUpload v-model="form.images" multiple :max-count="6" />
          <div class="form-tip">最多上传 6 张资源图片，首张将作为封面展示</div>
        </el-form-item>

        <el-form-item label="资源描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请描述资源的基本情况、新旧程度、适用范围等"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="借用规则" prop="borrowRules">
          <el-input
            v-model="form.borrowRules"
            type="textarea"
            :rows="3"
            placeholder="请说明借用规则，如：最长借用时长、损坏赔偿、预约须知等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

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
import { publishResource } from '@/api/resource'
import ImageUpload from '@/components/ImageUpload.vue'
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
  description: '',
  images: [] as string[],
  location: '',
  originalPrice: 0,
  rentalPrice: 0,
  borrowRules: '',
  ownerId: 0
})

// 表单校验规则
const rules: FormRules = {
  resourceName: [
    { required: true, message: '请输入资源名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  resourceType: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入存放位置', trigger: 'blur' }
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
        ownerId: userStore.userInfo.id,
        originalPrice: form.originalPrice || 0,
        rentalPrice: form.rentalPrice || 0,
        // 将图片数组转为逗号分隔字符串，适配数据库字段
        images: form.images && form.images.length > 0 ? form.images.join(',') : ''
      }
      const res: any = await publishResource(submitData)
      if (res.code === 200) {
        ElMessage.success('资源发布成功')
        router.push('/app/resource/list')
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
  form.originalPrice = 0
  form.rentalPrice = 0
}
</script>

<style scoped lang="scss">
.resource-publish-page {
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
