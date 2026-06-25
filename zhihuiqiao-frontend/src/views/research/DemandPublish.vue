<template>
  <div class="demand-publish-page">
    <div class="page-header">
      <h2>发布企业需求</h2>
      <p>发布技术攻关、成果转化、人才招聘等产学研合作需求</p>
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
            <el-form-item label="需求标题" prop="demandTitle">
              <el-input v-model="form.demandTitle" placeholder="请输入需求标题" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求类型" prop="demandType">
              <el-select v-model="form.demandType" placeholder="请选择需求类型" style="width: 100%">
                <el-option label="技术攻关" value="技术攻关" />
                <el-option label="成果转化" value="成果转化" />
                <el-option label="人才招聘" value="人才招聘" />
                <el-option label="联合研发" value="联合研发" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="行业领域" prop="industryField">
              <el-input v-model="form.industryField" placeholder="例如：智能制造、生物医药" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预算范围" prop="budgetRange">
              <el-input v-model="form.budgetRange" placeholder="例如：5万-10万" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="合作方式" prop="cooperationMode">
              <el-select v-model="form.cooperationMode" placeholder="请选择合作方式" style="width: 100%">
                <el-option label="委托开发" value="委托开发" />
                <el-option label="联合研发" value="联合研发" />
                <el-option label="技术转让" value="技术转让" />
                <el-option label="技术服务" value="技术服务" />
                <el-option label="人才引进" value="人才引进" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人姓名" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="需求描述" prop="demandDescription">
          <el-input
            v-model="form.demandDescription"
            type="textarea"
            :rows="5"
            placeholder="请详细描述企业需求背景、技术要求、合作期望等"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">发布需求</el-button>
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
import { publishDemand } from '@/api/research'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<any>(null)
const submitting = ref(false)

const form = reactive({
  demandTitle: '',
  demandType: '',
  industryField: '',
  demandDescription: '',
  budgetRange: '',
  cooperationMode: '',
  contactPerson: '',
  status: 'pending_audit'
})

const rules = {
  demandTitle: [{ required: true, message: '请输入需求标题', trigger: 'blur' }],
  demandType: [{ required: true, message: '请选择需求类型', trigger: 'change' }],
  industryField: [{ required: true, message: '请输入行业领域', trigger: 'blur' }],
  demandDescription: [{ required: true, message: '请输入需求描述', trigger: 'blur' }],
  cooperationMode: [{ required: true, message: '请选择合作方式', trigger: 'change' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }]
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    submitting.value = true
    try {
      const submitData = {
        ...form,
        publisherId: userStore.userInfo?.id || 0,
        enterpriseId: userStore.userInfo?.id || 0,
        views: 0
      }
      const res: any = await publishDemand(submitData)
      if (res.code === 200) {
        ElMessage.success('需求已提交，等待管理员审核通过后将自动上架')
        router.push('/app/research/demands')
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
.demand-publish-page {
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
