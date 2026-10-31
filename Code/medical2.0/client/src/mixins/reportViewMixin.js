/**
 * Mixin báo cáo
 * Cung cấp logic chung cho các báo cáo như báo cáo khối lượng công việc bác sĩ, báo cáo bán thuốc
 *
 * Cách sử dụng: 
 * 1. Trong thành phần mixins: [reportViewMixin]
 * 2. Tại data ghi đè các thuộc tính sau: 
 *    - listApi: Hàm API truy vấn danh sách
 *    - statApi: Hàm API thống kê (Có thể chọn)
 *    - exportApi: Hàm API xuất
 *    - dictTypeId: ID loại từ điển (dùng để tải tùy chọn dropdown)
 *    - searchParams: Tham số truy vấn bổ sung
 * 3. Nếu cần tùy chỉnh appendSearchParams, ghi đè phương thức đó
 */
import MainUI from '@/views/components/mainUI'
import listViewMixin from '@/mixins/listViewMixin'
import { listDictItemAll } from '@/api/sys/dictItem'
import { BigNumber } from 'bignumber.js'
import { daysAgo, formatDateTime } from '@/utils/common'
import { downloadBlob } from '@/utils/downloadBlob'

export default {
  extends: MainUI,
  mixins: [listViewMixin],
  data() {
    return {
      // Thành phần con bắt buộc ghi đè
      listApi: null,
      statApi: null,
      exportApi: null,
      // ID loại từ điển, Thành phần con có thể ghi đè
      dictTypeId: '1004078055755374603',
      // Thành phần con có thể ghi đè điều kiện truy vấn ban đầu
      queryModel: {
        dateRange: [daysAgo(30), new Date()]
      },
      search: {
        offset: 0,
        limit: 20,
        order: '',
        columnName: '',
        params: [
          {
            columnName: 'company_id',
            queryType: '=',
            value: currentUser.company.id
          },
          {
            logic: 'AND',
            queryType: '('
          },
          {
            columnName: 'create_date',
            logic: '',
            queryType: 'between',
            value: []
          },
          {
            logic: '',
            queryType: ')'
          }
        ]
      },
      pageSize: 20,
      dispensingTotal: 0,
      dispensingList: [],
      oprColumnWidth: 140,
      filerData: {
        pickerOptions: {
          disabledDate(time) {
            return time.getTime() > Date.now() - 8.64e6
          }
        }
      },
      allTotal: {},
      type_List: [],
      queryTypes: {
        goods_name: 'like',
        type: '=',
        bar_code: 'like'
      },
      amountTotal: {}
    }
  },
  methods: {
    /**
     * Xuất Excel (Thành phần con có thể ghi đè columnName)
     */
    exportExcel() {
      if (!this.exportApi) return
      this.exportApi(this.search).then((res) => {
        downloadBlob(res, 'Báo cáo xuất.xlsx')
      }).catch((error) => {
        this.outputError(error)
      })
    },

    /**
     * Lấy chuỗi ngày giờ 30 ngày trước
     * @returns {string} Chuỗi ngày giờ 30 ngày trước
     */
    addCreateDate() {
      return formatDateTime(daysAgo(30))
    },

    /**
     * Thành phần con ghi đè phương thức này để thêm điều kiện truy vấn tùy chỉnh
     */
    appendSearchParams() {
      // Mặc định không có điều kiện bổ sung, thành phần con ghi đè
    },

    /**
     * Xử lý dữ liệu phản hồi danh sách
     */
    handleListResponse(responseData) {
      this.dispensingList = responseData.data.rows
      this.dispensingTotal = responseData.data.total
    },

    /**
     * Tải dữ liệu (Danh sách + Thống kê)
     */
    loadData() {
      this.setLoad()
      this.search.params = []
      this.appendSearchParams()
      this.listApi(this.search).then(responseData => {
        if (responseData.code === 100) {
          this.handleListResponse(responseData)
          if (this.statApi) {
            this.statApi(this.search).then((res) => {
              if (res.code === 100) {
                this.amountTotal = res.data
                this.resetLoad()
              }
            }).catch(() => {})
          } else {
            this.resetLoad()
          }
        } else {
          this.showMessage(responseData)
          this.resetLoad()
        }
      }).catch(error => {
        this.outputError(error)
      })
    },

    /**
     * Khởi tạo tùy chọn từ điển
     */
    initOptions() {
      if (!this.dictTypeId) return
      const type_search = {
        params: [
          {
            columnName: 'dict_type_id',
            queryType: '=',
            value: this.dictTypeId
          }
        ]
      }
      type_search.params.forEach((item) => {
        if (this.queryTypes[item.columnName]) {
          item.queryType = this.queryTypes[item.columnName]
        }
      })
      this.pushDataPermissions(
        type_search.params,
        this.$route.meta.routerId,
        '4005'
      )
      this.type_List.splice(0, this.type_List.length)
      listDictItemAll(type_search).then((responseData) => {
        this.type_List = responseData.data
      })
    },

    /**
     * Đặt lại điều kiện truy vấn
     */
    resetCondition() {
      this.queryModel = {
        ...this.queryModel,
        dateRange: [this.addCreateDate(), new Date()]
      }
      this.currentPage = 1
      this.onSearch()
    },

    /**
     * Định dạng BigNumber (Phân tách hàng nghìn, 2 chữ số thập phân)
     */
    bigNum(num) {
      if (num || num === '0') {
        return new BigNumber(num).toFormat(2)
      }
      return ''
    }
  },
  mounted() {
    // pageInit() nội bộ đã gọi this.initOptions(this.queryModel),
    // ở đây không cần gọi lại, tránh lặp lại yêu cầu từ điển
    this.pageInit()
  }
}
