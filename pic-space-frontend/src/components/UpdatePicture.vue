<script setup lang="ts">
import { computed, onMounted, ref, watch, reactive } from 'vue'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { editPictureUsingPost, listPictureTagCategoryUsingGet } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import {
  Combobox,
  ComboboxAnchor,
  ComboboxEmpty,
  ComboboxGroup,
  ComboboxInput,
  ComboboxItem,
  ComboboxList,
} from '@/components/ui/combobox'
import {
  TagsInput,
  TagsInputInput,
  TagsInputItem,
  TagsInputItemDelete,
  TagsInputItemText,
} from '@/components/ui/tags-input'
import { useFilter } from 'reka-ui'
import router from '@/router'

const props = defineProps<{
  pictureFormData?: API.PictureUpdateRequest | null
  successCallback?: () => void
}>()

// 使用 reactive 创建响应式表单对象
const pictureForm = reactive<API.PictureUpdateRequest>(
  props.pictureFormData ? props.pictureFormData : {},
)

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

const open = ref(false)
const searchTerm = ref('')

// 使用计算属性与 pictureForm.tags 双向绑定
const modelValue = computed({
  get: () => pictureForm.tags || [],
  set: (value) => {
    pictureForm.tags = value
  },
})

const { contains } = useFilter({ sensitivity: 'base' })
const filteredTagOptions = computed(() => {
  const options = tagOptions.value.filter((i) => !modelValue.value.includes(i.label))
  return searchTerm.value
    ? options.filter((option) => contains(option.label, searchTerm.value))
    : options
})

watch(pictureForm, () => {
  console.log(pictureForm)
})

const handleSubmit = async () => {
  console.log(pictureForm)
  const res = await editPictureUsingPost({
    ...pictureForm,
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('创建成功')
    // 跳转到图片详情页
    await router.push({
      path: `/picture/${pictureForm.id}`,
    })
  } else {
    message.error('创建失败，' + res.data.message)
  }
}
</script>

<template>
  <form
    name="pictureForm"
    class="flex flex-col justify-center w-2/3 space-y-6"
    @submit.prevent="handleSubmit()"
  >
    <FormField name="name">
      <FormItem>
        <FormLabel>名称</FormLabel>
        <FormControl>
          <Input v-model="pictureForm.name" type="text" placeholder="输入图片名称" />
        </FormControl>
        <!--        <FormDescription> This is your public display name. </FormDescription>-->
        <FormMessage />
      </FormItem>
    </FormField>
    <FormField name="introduction">
      <FormItem>
        <FormLabel>简介</FormLabel>
        <FormControl>
          <Textarea v-model="pictureForm.introduction" placeholder="输入简介" />
        </FormControl>
        <!--        <FormDescription> This is your public display name. </FormDescription>-->
        <FormMessage />
      </FormItem>
    </FormField>
    <FormField name="category">
      <FormItem>
        <FormLabel>分类</FormLabel>
        <FormControl>
          <Select v-model="pictureForm.category">
            <SelectTrigger class="w-full">
              <SelectValue placeholder="请选择分类" />
            </SelectTrigger>
            <SelectContent>
              <SelectGroup>
                <SelectLabel v-if="categoryOptions.length == 0">暂无分类</SelectLabel>
                <SelectItem v-for="item in categoryOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </SelectItem>
              </SelectGroup>
            </SelectContent>
          </Select>
        </FormControl>
        <!--        <FormDescription> This is your public display name. </FormDescription>-->
        <FormMessage />
      </FormItem>
    </FormField>

    <FormField name="tags">
      <FormItem>
        <FormLabel>标签</FormLabel>
        <FormControl>
          <Combobox v-model="modelValue" v-model:open="open" :ignore-filter="true">
            <ComboboxAnchor as-child>
              <TagsInput v-model="modelValue" class="px-2 gap-2 w-full">
                <div class="flex gap-2 flex-wrap items-center">
                  <TagsInputItem v-for="item in modelValue" :key="item" :value="item">
                    <TagsInputItemText />
                    <TagsInputItemDelete />
                  </TagsInputItem>
                </div>

                <ComboboxInput v-model="searchTerm" as-child>
                  <TagsInputInput
                    placeholder="搜索或输入标签..."
                    class="min-w-[200px] w-full p-0 border-none focus-visible:ring-0 h-auto"
                    @keydown.enter.prevent="
                      () => {
                        if (searchTerm.trim()) {
                          const currentTags = [...(modelValue || [])]
                          if (!currentTags.includes(searchTerm.trim())) {
                            modelValue = [...currentTags, searchTerm.trim()]
                          }
                          searchTerm = ''
                          open = false
                        }
                      }
                    "
                  />
                </ComboboxInput>
              </TagsInput>

              <ComboboxList class="w-full">
                <ComboboxEmpty>
                  <div v-if="searchTerm" class="p-2 text-sm text-gray-500">
                    按回车添加 "{{ searchTerm }}"
                  </div>
                  <div v-else class="p-2 text-sm text-gray-500">暂无标签选项</div>
                </ComboboxEmpty>
                <ComboboxGroup>
                  <!-- 显示匹配的标签选项 -->
                  <ComboboxItem
                    v-for="tag in filteredTagOptions"
                    :key="tag.value"
                    :value="tag.label"
                    @select.prevent="
                      (ev) => {
                        if (typeof ev.detail.value === 'string') {
                          searchTerm = ''
                          const currentTags = [...(modelValue || [])]
                          if (!currentTags.includes(ev.detail.value)) {
                            modelValue = [...currentTags, ev.detail.value]
                          }
                        }

                        if (filteredTagOptions.length === 0) {
                          open = false
                        }
                      }
                    "
                  >
                    {{ tag.label }}
                  </ComboboxItem>

                  <!-- 如果搜索词不在现有选项中，显示添加新标签的选项 -->
                  <ComboboxItem
                    v-if="
                      searchTerm &&
                      !filteredTagOptions.some(
                        (tag) => tag.label.toLowerCase() === searchTerm.toLowerCase(),
                      )
                    "
                    :value="searchTerm"
                    @select.prevent="
                      (ev) => {
                        if (typeof ev.detail.value === 'string') {
                          const currentTags = [...(modelValue || [])]
                          if (!currentTags.includes(ev.detail.value)) {
                            modelValue = [...currentTags, ev.detail.value]
                          }
                          searchTerm = ''
                          open = false
                        }
                      }
                    "
                  >
                    添加 "{{ searchTerm }}"
                  </ComboboxItem>
                </ComboboxGroup>
              </ComboboxList>
            </ComboboxAnchor>
          </Combobox>
        </FormControl>
        <!--        <FormDescription> This is your public display name. </FormDescription>-->
        <FormMessage />
      </FormItem>
    </FormField>

    <Button type="submit" :variant="'outline'"> Update </Button>
  </form>
</template>

<style scoped></style>
