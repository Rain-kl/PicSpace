// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** addSpace POST /api/space/add */
export async function addSpaceUsingPost(
  body: API.SpaceAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong_>('/api/space/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** deleteSpace POST /api/space/delete */
export async function deleteSpaceUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/space/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** editSpace POST /api/space/edit */
export async function editSpaceUsingPost(
  body: API.SpaceEditRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/space/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getSpaceVOById GET /api/space/get/vo */
export async function getSpaceVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceVOByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseSpaceVO_>('/api/space/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** listSpace POST /api/space/list */
export async function listSpaceUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListSpaceVO_>('/api/space/list', {
    method: 'POST',
    ...(options || {}),
  })
}

/** listSpaceLevel GET /api/space/list/level */
export async function listSpaceLevelUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListSpaceLevel_>('/api/space/list/level', {
    method: 'GET',
    ...(options || {}),
  })
}
