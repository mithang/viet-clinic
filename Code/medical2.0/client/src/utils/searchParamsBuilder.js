/**
 * Builder tham số truy vấn
 * Cung cấp fluent API để xây dựng tham số truy vấn, nâng cao tính dễ đọc của code
 *
 * Ví dụ sử dụng: 
 * import { SearchParamsBuilder } from '@/utils/searchParamsBuilder'
 *
 * const params = SearchParamsBuilder.create()
 *   .eq('company_id', companyId)
 *   .like('name', name)
 *   .like('linkman', linkman)
 *   .eq('type', type)
 *   .build()
 */
export class SearchParamsBuilder {
  constructor() {
    this.params = []
  }

  /**
   * Tạo instance builder mới
   */
  static create() {
    return new SearchParamsBuilder()
  }

  /**
   * Tạo instance builder có lọc tenant
   */
  static createWithTenant(companyId) {
    return new SearchParamsBuilder().eq('company_id', companyId)
  }

  /**
   * Truy vấn bằng
   */
  eq(columnName, value) {
    if (value !== null && value !== undefined && value !== '') {
      this.params.push({ columnName, queryType: '=', value })
    }
    return this
  }

  /**
   * Truy vấn không bằng
   */
  ne(columnName, value) {
    if (value !== null && value !== undefined) {
      this.params.push({ columnName, queryType: '!=', value })
    }
    return this
  }

  /**
   * Truy vấn mờ
   */
  like(columnName, value) {
    if (value !== null && value !== undefined && value.toString().trim() !== '') {
      this.params.push({ columnName, queryType: 'like', value })
    }
    return this
  }

  /**
   * Truy vấn mờ trái
   */
  leftLike(columnName, value) {
    if (value !== null && value !== undefined && value.toString().trim() !== '') {
      this.params.push({ columnName, queryType: 'left_like', value })
    }
    return this
  }

  /**
   * Truy vấn mờ phải
   */
  rightLike(columnName, value) {
    if (value !== null && value !== undefined && value.toString().trim() !== '') {
      this.params.push({ columnName, queryType: 'right_like', value })
    }
    return this
  }

  /**
   * Truy vấn lớn hơn
   */
  gt(columnName, value) {
    if (value !== null && value !== undefined) {
      this.params.push({ columnName, queryType: '>', value })
    }
    return this
  }

  /**
   * Truy vấn lớn hơn hoặc bằng
   */
  ge(columnName, value) {
    if (value !== null && value !== undefined) {
      this.params.push({ columnName, queryType: '>=', value })
    }
    return this
  }

  /**
   * Truy vấn nhỏ hơn
   */
  lt(columnName, value) {
    if (value !== null && value !== undefined) {
      this.params.push({ columnName, queryType: '<', value })
    }
    return this
  }

  /**
   * Truy vấn nhỏ hơn hoặc bằng
   */
  le(columnName, value) {
    if (value !== null && value !== undefined) {
      this.params.push({ columnName, queryType: '<=', value })
    }
    return this
  }

  /**
   * INTruy vấn
   */
  in(columnName, values) {
    if (values && values.length > 0) {
      this.params.push({ columnName, queryType: 'in', value: values })
    }
    return this
  }

  /**
   * NOT INTruy vấn
   */
  notIn(columnName, values) {
    if (values && values.length > 0) {
      this.params.push({ columnName, queryType: 'not in', value: values })
    }
    return this
  }

  /**
   * BETWEENTruy vấn
   */
  between(columnName, start, end) {
    if (start !== null && start !== undefined && end !== null && end !== undefined) {
      this.params.push({ columnName, queryType: 'between', value: [start, end] })
    }
    return this
  }

  /**
   * IS NULLTruy vấn
   */
  isNull(columnName) {
    this.params.push({ columnName, queryType: 'is null', value: null })
    return this
  }

  /**
   * IS NOT NULLTruy vấn
   */
  isNotNull(columnName) {
    this.params.push({ columnName, queryType: 'is not null', value: null })
    return this
  }

  /**
   * Thêm tham số tùy chỉnh
   */
  add(param) {
    if (param) {
      this.params.push(param)
    }
    return this
  }

  /**
   * Thêm tham số logic OR
   */
  or(columnName, queryType, value) {
    if (value !== null && value !== undefined) {
      this.params.push({ columnName, queryType, value, logic: 'OR' })
    }
    return this
  }

  /**
   * Xây dựng danh sách tham số
   */
  build() {
    return [...this.params]
  }
}

/**
 * Tạo đối tượng tìm kiếm phân trang
 */
export function createSearch(options = {}) {
  const {
    companyId = currentUser?.company?.id,
    limit = 20
  } = options

  return {
    params: companyId ? [{ columnName: 'company_id', queryType: '=', value: companyId }] : [],
    offset: 0,
    limit,
    columnName: '',
    order: ''
  }
}

/**
 * Tạo đối tượng quyền
 */
export function createPermissions() {
  return {
    view: false,
    add: false,
    edit: false,
    remove: false,
    export: false
  }
}

/**
 * Ánh xạ dữ liệu quyền
 */
export function mapPermissions(permissionData, prefix) {
  return {
    view: permissionData.find(item => item.permission === `${prefix}:read`),
    add: permissionData.find(item => item.permission === `${prefix}:create`),
    edit: permissionData.find(item => item.permission === `${prefix}:update`),
    remove: permissionData.find(item => item.permission === `${prefix}:delete`),
    export: permissionData.find(item => item.permission === `${prefix}:export`)
  }
}

export default {
  SearchParamsBuilder,
  createSearch,
  createPermissions,
  mapPermissions
}
