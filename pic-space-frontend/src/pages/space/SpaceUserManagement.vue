<template>
  <Card class="border-none shadow-none">
    <CardHeader>
      <CardTitle class="text-3xl">成员管理</CardTitle>
    </CardHeader>
    <CardContent>
      <a-table :columns="columns" :dataSource="dataSource" />
    </CardContent>
    <!--        <CardFooter> Card Footer </CardFooter>-->
  </Card>
</template>

<script setup>
import { ref } from 'vue'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card/index.ts'

const dataSource = ref([
  {
    key: '1',
    name: '胡彦斌',
    age: 32,
    address: '西湖区湖底公园1号',
  },
  {
    key: '2',
    name: '胡彦祖',
    age: 42,
    address: '西湖区湖底公园1号',
  },
])

const columns = [
  {
    title: '姓名',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '年龄',
    dataIndex: 'age',
    key: 'age',
  },
  {
    title: '住址',
    dataIndex: 'address',
    key: 'address',
  },
]
</script>
