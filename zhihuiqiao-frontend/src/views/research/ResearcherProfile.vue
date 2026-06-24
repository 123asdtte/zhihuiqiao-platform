<template>
  <div class="researcher-profile-page">
    <div class="page-header">
      <h2>科研画像</h2>
      <p>完善您的科研背景、技能和兴趣，便于系统为您匹配合适的项目</p>
    </div>

    <el-card shadow="never" class="profile-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
      >
        <el-form-item label="研究方向" prop="researchDirections">
          <el-input
            v-model="form.researchDirections"
            type="textarea"
            :rows="3"
            placeholder="请填写您的研究方向，如：深度学习、自然语言处理、计算机视觉等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="专业技能" prop="skills">
          <el-input
            v-model="form.skills"
            type="textarea"
            :rows="3"
            placeholder="请填写您掌握的专业技能，如：Python、PyTorch、Java、数据分析等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="科研成果" prop="publications">
          <el-input
            v-model="form.publications"
            type="textarea"
            :rows="3"
            placeholder="请填写您的论文、专利、获奖等科研成果"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="项目经历" prop="projectExperience">
          <el-input
            v-model="form.projectExperience"
            type="textarea"
            :rows="3"
            placeholder="请填写您参与过的科研项目或工程实践经历"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="研究兴趣" prop="researchInterests">
          <el-input
            v-model="form.researchInterests"
            type="textarea"
            :rows="3"
            placeholder="请填写您感兴趣的研究课题或合作方向"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="可投入时间" prop="availability">
              <el-select v-model="form.availability" placeholder="请选择可投入时间" style="width: 100%">
                <el-option label="每周 1-5 小时" value="每周 1-5 小时" />
                <el-option label="每周 5-10 小时" value="每周 5-10 小时" />
                <el-option label="每周 10-20 小时" value="每周 10-20 小时" />
                <el-option label="每周 20 小时以上" value="每周 20 小时以上" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合作意向" prop="cooperationIntention">
              <el-select v-model="form.cooperationIntention" placeholder="请选择合作意向" style="width: 100%">
                <el-option label="仅校内合作" value="仅校内合作" />
                <el-option label="校企联合" value="校企联合" />
                <el-option label="开放合作" value="开放合作" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存画像</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getResearcherProfile, saveResearcherProfile } from '@/api/research'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref<any>(null)
const submitting = ref(false)

const form = reactive({
  userId: 0,
  researchDirections: '',
  skills: '',
  publications: '',
  projectExperience: '',
  researchInterests: '',
  availability: '',
  cooperationIntention: ''
})

const rules = {
  researchDirections: [{ required: true, message: '请填写研究方向', trigger: 'blur' }],
  skills: [{ required: true, message: '请填写专业技能', trigger: 'blur' }]
}

/**
 * 加载科研画像
 */
async function loadProfile() {
  const userId = userStore.userInfo?.id
  if (!userId) return

  try {
    const res: any = await getResearcherProfile(userId)
    if (res.data) {
      Object.assign(form, res.data)
    } else {
      form.userId = userId
    }
  } catch (error) {
    ElMessage.error('加载科研画像失败')
    console.error(error)
  }
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    submitting.value = true
    try {
      const submitData = {
        ...form,
        userId: userStore.userInfo?.id || 0
      }
      const res: any = await saveResearcherProfile(submitData)
      if (res.code === 200) {
        ElMessage.success('科研画像保存成功')
      } else {
        ElMessage.error(res.message || '保存失败')
      }
    } catch (error) {
      ElMessage.error('保存失败，请稍后重试')
      console.error(error)
    } finally {
      submitting.value = false
    }
  })
}

function handleReset() {
  loadProfile()
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped lang="scss">
.researcher-profile-page {
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

  .profile-card {
    max-width: 900px;
  }
}
</style>
