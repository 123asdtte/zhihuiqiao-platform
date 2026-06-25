<template>
  <div class="project-publish-page">
    <div class="page-header">
      <h2>发布科研项目</h2>
      <p>填写项目信息，招募合适的团队成员</p>
    </div>

    <el-card shadow="never" class="publish-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="form.projectName" placeholder="请输入项目名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目编号" prop="projectCode">
              <el-input v-model="form.projectCode" placeholder="例如：PRJ-2024-001" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="项目类型" prop="projectType">
              <el-select v-model="form.projectType" placeholder="请选择项目类型" style="width: 100%">
                <el-option label="基础研究" value="基础研究" />
                <el-option label="应用研究" value="应用研究" />
                <el-option label="技术开发" value="技术开发" />
                <el-option label="创新创业" value="创新创业" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="研究领域" prop="researchFields">
              <el-input v-model="form.researchFields" placeholder="例如：人工智能、计算机视觉" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                placeholder="选择开始日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                placeholder="选择结束日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="最大成员数" prop="maxMembers">
              <el-input-number v-model="form.maxMembers" :min="1" :max="50" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目状态">
              <el-tag type="warning">提交后待管理员审核</el-tag>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="项目简介" prop="projectDescription">
          <el-input
            v-model="form.projectDescription"
            type="textarea"
            :rows="4"
            placeholder="请简要描述项目背景、目标和内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="成员要求" prop="requirements">
          <el-input
            v-model="form.requirements"
            type="textarea"
            :rows="3"
            placeholder="请描述对申请成员的技能、专业、时间等要求"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="预期成果" prop="expectedOutcomes">
          <el-input
            v-model="form.expectedOutcomes"
            type="textarea"
            :rows="3"
            placeholder="请描述项目预期取得的成果，如论文、专利、产品原型等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">发布项目</el-button>
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
import { publishProject } from '@/api/research'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<any>(null)
const submitting = ref(false)

const form = reactive({
  projectName: '',
  projectCode: '',
  projectType: '',
  researchFields: '',
  projectDescription: '',
  requirements: '',
  expectedOutcomes: '',
  startDate: '',
  endDate: '',
  maxMembers: 5,
  status: 'pending_audit'
})

const rules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
  researchFields: [{ required: true, message: '请输入研究领域', trigger: 'blur' }],
  projectDescription: [{ required: true, message: '请输入项目简介', trigger: 'blur' }],
  requirements: [{ required: true, message: '请输入成员要求', trigger: 'blur' }],
  maxMembers: [{ required: true, message: '请输入最大成员数', trigger: 'change' }],
  status: [{ required: true, message: '请选择项目状态', trigger: 'change' }]
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    submitting.value = true
    try {
      const submitData = {
        ...form,
        creatorId: userStore.userInfo?.id || 0,
        currentMembers: 0,
        views: 0
      }
      const res: any = await publishProject(submitData)
      if (res.code === 200) {
        ElMessage.success('项目已提交，等待管理员审核通过后将自动上架')
        router.push('/app/research/projects')
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

function handleReset() {
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.project-publish-page {
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
    max-width: 900px;
  }
}
</style>
