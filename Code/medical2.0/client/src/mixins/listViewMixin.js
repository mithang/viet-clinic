/**
 * Mixin danh sách
 * Đóng gói logic phân trang, sắp xếp, điều phối CRUD và tải quyền chung
 *
 * Cách sử dụng: 
 * 1. Trong thành phần mixins: [listViewMixin]
 * 2. Tại data định nghĩa listApi, getApi, deleteApi, entityName, permissionPrefix
 * 3. Thực hiện loadData() để tải danh sách dữ liệu (không bao gồm quyền)
 * 4. Thực hiện handleListResponse() để xử lý phản hồi danh sách
 *
 * Lưu ý: pageInit() chỉ gọi một lần khi khởi tạo trang (Tải dữ liệu+Quyền), 
 *       loadData() gọi khi tìm kiếm/Phân trang/sắp xếp (chỉ tải danh sách dữ liệu).
 */
import { listResourcePermission } from '@/api/resourcePermission'
import { mapPermissions } from '@/utils/searchParamsBuilder'

export default {
  data() {
    return {
      search: {
        params: [],
        offset: 0,
        limit: 20,
        columnName: '',
        order: ''
      },
      currentPage: 1,
      permission: {
        view: false,
        add: false,
        edit: false,
        remove: false,
        export: false,
        skip: false
      },
      // Thành phần con cần định nghĩa các thuộc tính sau
      // listApi: Function,       // API truy vấn danh sách
      // getApi: Function,        // API lấy một bản ghi
      // deleteApi: Function,     // API xóa
      // entityName: String,      // Tên thực thể (dùng cho ánh xạ quyền, như 'Drug')
      // permissionPrefix: String // Tiền tố quyền (như 'drug')
    }
  },
  methods: {
    /**
     * Tính số thứ tự
     */
    indexMethod(index) {
      return (this.currentPage - 1) * this.search.limit + index + 1
    },

    /**
     * Tìm kiếm
     */
    onSearch() {
      if (this.moreCodition) {
        this.search.offset = 0
        this.currentPage = 1
        this.loadData()
      } else {
        this.$refs['queryForm'].validate(valid => {
          if (valid) {
            this.search.offset = 0
            this.currentPage = 1
            this.loadData()
          } else {
            return false
          }
        })
      }
    },

    /**
     * Số lượng mỗi trang thay đổi
     */
    onSizeChange(val) {
      this.currentPage = 1
      this.search.limit = val
      this.search.offset = 0
      this.loadData()
    },

    /**
     * Số trang thay đổi
     */
    onCurrentChange(val) {
      this.search.offset = (val - 1) * this.search.limit
      this.currentPage = val
      this.loadData()
    },

    /**
     * Thay đổi sắp xếp — Nhận el-table sort-change của đối tượng orderby
     * @param {Object} orderby - { prop: string, order: string }
     */
    onSortChange(orderby) {
      if (!orderby.prop) {
        this.search.columnName = ''
        this.search.order = ''
      } else {
        this.search.columnName = orderby.prop
        this.search.order = orderby.order === 'descending' ? 'desc' : 'asc'
      }
      this.loadData()
    },

    /**
     * Đặt lại form tìm kiếm
     */
    reset() {
      if (this.$refs.queryForm) {
        this.$refs.queryForm.resetFields()
      }
      this.onSearch()
    },

    /**
     * Khởi tạo trang — Chỉ gọi một lần khi mounted
     * Tải dữ liệu danh sách + thông tin quyền
     * Thành phần con có thể ghi đè phương thức này để thêm logic tùy chỉnh
     */
    async pageInit() {
      this.setLoad()
      try {
        // Khởi tạo tùy chọn — phải await vì nhiều trang dùng dữ liệu async
        // trong appendSearchParams (ví dụ loginCompany.parent ở trang
        // tổng hợp rà soát đơn thuốc); không await sẽ chạy tìm kiếm
        // khi dữ liệu chưa về và văng lỗi TypeError.
        if (this.initOptions) {
          await this.initOptions(this.queryModel)
        }

        // Xây dựng tham số tìm kiếm — Thành phần con nên thêm điều kiện truy vấn trong appendSearchParams()
        this.search.params = []
        if (this.appendSearchParams) {
          this.appendSearchParams()
        }

        // Tải song song dữ liệu và quyền — routerId có thể thiếu khi route không có
        // meta (mở bằng hash tới route chưa đăng ký): khi đó bỏ qua lệnh
        // gọi quyền (trước đây gọi với undefined → 400 + alert), quyền mặc
        // định rỗng (fail-closed: không hiện nút thao tác), danh sách vẫn tải.
        const prefix = this.permissionPrefix || this.entityName
        const routerId = typeof this.resolveRouterId === 'function'
          ? this.resolveRouterId()
          : (this.$route.meta.routerId)
        const [listRespData, permissionRespData] = await Promise.all([
          this.listApi(this.search),
          routerId ? listResourcePermission(routerId) : Promise.resolve({ code: 100, data: [] })
        ])

        if (listRespData.code === 100 && permissionRespData.code === 100) {
          this.handleListResponse(listRespData)
          this.permission = mapPermissions(permissionRespData.data, prefix)
        } else {
          this.showMessage(permissionRespData.code !== 100 ? permissionRespData : listRespData)
        }
        this.resetLoad()
      } catch (error) {
        this.outputError(error)
      }
    },

    /**
     * Xử lý phản hồi danh sách — Thành phần con cần ghi đè
     * @param {Object} responseData Dữ liệu phản hồi
     */
    handleListResponse(responseData) {
      // Thành phần con thực hiện: thiết lập dữ liệu danh sách và tổng số
      // Ví dụ: this.supplierTotal = responseData.data.total
      //       this.supplierList = responseData.data.rows
    },

    /**
     * Thêm tham số tìm kiếm — Thành phần con cần ghi đè
     * Trong phương thức này xây dựng this.search.params
     *
 * Ví dụ: 
     * appendSearchParams() {
     *   this.search.params = [
     *     { columnName: 'company_id', queryType: '=', value: currentUser.company.id },
     *     { columnName: 'name', queryType: 'like', value: this.queryModel.name },
     *     { columnName: 'linkman', queryType: 'like', value: this.queryModel.linkman }
     *   ]
     * }
     */
    appendSearchParams() {
      // Thành phần con thực hiện
    },

    /**
     * Tải dữ liệu danh sách — Gọi khi tìm kiếm/Phân trang/sắp xếp
     * Thành phần con có thể ghi đè phương thức này để thêm logic tùy chỉnh
     * Thực hiện mặc định: Gọi listApi và kích hoạt handleListResponse
     */
    loadData() {
      this.setLoad()
      // Xây dựng lại tham số tìm kiếm
      this.search.params = []
      if (this.appendSearchParams) {
        this.appendSearchParams()
      }
      this.listApi(this.search).then(responseData => {
        if (responseData.code === 100) {
          this.handleListResponse(responseData)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },

    /**
     * Xem thực thể
     */
    onViewEntity(index, row, formRef) {
      this.setLoad()
      this.getApi(row.id).then(responseData => {
        if (responseData.code === 100) {
          this.$refs[formRef][`openView${this.entityName}Dialog`](responseData.data)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },

    /**
     * Chỉnh sửa thực thể
     */
    onEditEntity(index, row, formRef) {
      this.setLoad()
      this.getApi(row.id).then(responseData => {
        if (responseData.code === 100) {
          this.$refs[formRef][`openEdit${this.entityName}Dialog`](responseData.data)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },

    /**
     * Sao chép thực thể
     */
    onCopyEntity(index, row, formRef) {
      this.setLoad()
      this.getApi(row.id).then(responseData => {
        if (responseData.code === 100) {
          this.$refs[formRef][`openCopy${this.entityName}Dialog`](responseData.data)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },

    /**
     * Xóa thực thể
     */
    onDeleteEntity(index, row, deleteApi) {
      this.$confirm('Xác nhận xóa?', 'Nhắc nhở', {
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy',
        type: 'warning'
      }).then(() => {
        this.setLoad()
        deleteApi(row).then(responseData => {
          if (responseData.code === 100) {
            this.$message({ type: 'success', message: 'Xóa thành công' })
            this.loadData()
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      }).catch(() => {})
    },

    /**
     * Tạo thực thể — Thành phần con có thể ghi đè
     */
    onCreateEntity(formRef) {
      this.$refs[formRef][`openAdd${this.entityName}Dialog`]()
    }
  }
}
