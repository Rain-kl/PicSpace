// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** getDataOverview GET /api/dataAnalysis/overview */
export async function getDataOverviewUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseDataOverview_>('/api/dataAnalysis/overview', {
    method: 'GET',
    ...(options || {}),
  })
}
