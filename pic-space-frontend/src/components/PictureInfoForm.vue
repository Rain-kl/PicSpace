<template>
  <div class="space-y-6">
    <!-- 标题输入 -->
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-2">标题</label>
      <input
        v-model="pictureForm.name"
        :disabled="disabled"
        type="text"
        placeholder="添加标题"
        class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100 disabled:text-gray-500 disabled:cursor-not-allowed"
      />
    </div>

    <!-- 描述输入 -->
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-2">描述</label>
      <textarea
        v-model="pictureForm.introduction"
        :disabled="disabled"
        placeholder="添加详细描述"
        rows="4"
        class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none disabled:bg-gray-100 disabled:text-gray-500 disabled:cursor-not-allowed"
      ></textarea>
    </div>

    <!-- 分类选择 -->
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-2">分类</label>
      <select
        v-model="pictureForm.category"
        :disabled="disabled"
        class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100 disabled:text-gray-500 disabled:cursor-not-allowed"
      >
        <option value="" disabled>选择一块图版</option>
        <option v-for="category in categoryOptions" :key="category.value" :value="category.value">
          {{ category.label }}
        </option>
      </select>
    </div>

    <!-- Tag -->
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-2"
        >Tag ({{ modelValue.length }})</label
      >
      <div class="relative">
        <input
          v-model="tagInput"
          :disabled="disabled"
          type="text"
          placeholder="搜索标签"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100 disabled:text-gray-500 disabled:cursor-not-allowed"
          @keydown.enter.prevent="addTag"
          @focus="showTagSuggestions = true"
          @blur="hideTagSuggestions"
        />

        <!-- 标签建议列表 -->
        <div
          v-if="showTagSuggestions && !disabled && filteredTagOptions.length > 0"
          class="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-40 overflow-y-auto"
        >
          <div
            v-for="tag in filteredTagOptions"
            :key="tag.value"
            class="px-3 py-2 hover:bg-gray-100 cursor-pointer text-sm"
            @mousedown.prevent="selectTag(tag.label)"
          >
            {{ tag.label }}
          </div>
        </div>
      </div>

      <!-- 已选标签显示 -->
      <div v-if="modelValue.length > 0" class="flex flex-wrap gap-2 mt-3">
        <span
          v-for="tag in modelValue"
          :key="tag"
          class="inline-flex items-center px-3 py-1 bg-gray-100 text-gray-700 text-sm rounded-full"
        >
          {{ tag }}
          <button
            v-if="!disabled"
            @click="removeTag(tag)"
            class="ml-2 text-gray-500 hover:text-gray-700"
          >
            ×
          </button>
        </span>
      </div>
    </div>

    <!-- 保存按钮 -->
    <button
      :disabled="disabled || !pictureForm.name?.trim()"
      @click="handleSubmit"
      class="w-full bg-white-600 hover:bg-gray-200 border-1 disabled:bg-gray-300 disabled:cursor-not-allowed text-white font-medium py-3 px-4 rounded-lg transition-colors"
    >
      {{ disabled ? '请先上传图片' : '保存' }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { editPictureUsingPost, listPictureTagCategoryUsingGet } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

interface Props {
  pictureFormData?: API.PictureUpdateRequest | null
  disabled?: boolean
  successCallback?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
})

// 使用 reactive 创建响应式表单对象
const pictureForm = reactive<API.PictureUpdateRequest>(
  props.pictureFormData ? props.pictureFormData : {},
)

// 额外的字段
const tagInput = ref('')
const showTagSuggestions = ref(false)

const categoryOptions = ref<{ value: string; label: string }[]>([])
const tagOptions = ref<{ value: string; label: string }[]>([])

const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

// 使用计算属性与 pictureForm.tags 双向绑定
const modelValue = computed({
  get: () => pictureForm.tags || [],
  set: (value) => {
    pictureForm.tags = value
  },
})

// 过滤标签选项
const filteredTagOptions = computed(() => {
  const options = tagOptions.value.filter((i) => !modelValue.value.includes(i.label))
  return tagInput.value
    ? options.filter((option) => option.label.toLowerCase().includes(tagInput.value.toLowerCase()))
    : options.slice(0, 10) // 限制显示数量
})

// 添加标签
const addTag = () => {
  const tag = tagInput.value.trim()
  if (tag && !modelValue.value.includes(tag)) {
    modelValue.value = [...modelValue.value, tag]
    tagInput.value = ''
    showTagSuggestions.value = false
  }
}

// 选择标签
const selectTag = (tag: string) => {
  if (!modelValue.value.includes(tag)) {
    modelValue.value = [...modelValue.value, tag]
  }
  tagInput.value = ''
  showTagSuggestions.value = false
}

// 删除标签
const removeTag = (tag: string) => {
  modelValue.value = modelValue.value.filter((t) => t !== tag)
}

// 隐藏标签建议（延迟以处理点击事件）
const hideTagSuggestions = () => {
  setTimeout(() => {
    showTagSuggestions.value = false
  }, 200)
}

const handleSubmit = async () => {
  if (!pictureForm.name?.trim()) {
    message.error('请输入图片标题')
    return
  }

  const res = await editPictureUsingPost({
    ...pictureForm,
  })

  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('保存成功')
    props.successCallback?.()
  } else {
    message.error('保存失败，' + res.data.message)
  }
}
</script>

<style scoped>
/* 自定义样式 */
</style>
