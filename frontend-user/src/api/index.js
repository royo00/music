// src/api/index.js
/**
 * API 统一导出
 */

import * as authApi from './auth'
import * as userApi from './user'
import * as musicApi from './music'

export { authApi, userApi, musicApi }

export default {
  auth: authApi,
  user: userApi,
  music: musicApi
}
