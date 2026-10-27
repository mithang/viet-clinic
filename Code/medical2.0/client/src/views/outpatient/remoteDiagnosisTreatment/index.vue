<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow'></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <patient-form ref='patientForm' :permission='permission' @save-finished='getPatientList'></patient-form>
    <el-card class="page-container">
      <!--  Thanh tìm kiếm  Bắt đầu -->
      <div class='query-form-container'>
        <el-row v-if='!moreCodition' class='search-row'>
          <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm'
                   :inline-message='true'>
            <el-col :span="4">
              <el-form-item label='Tên bệnh nhân' prop='name'>
                <el-input v-model='queryModel.name' :clearable='true' placeholder='Vui lòng nhập tên bệnh nhân'></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label='Số CMND/CCCD' prop='card'>
                <el-input v-model='queryModel.card' :clearable='true' placeholder='Vui lòng nhập số CMND/CCCD'></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label='Khoa' prop='department'>
                <el-input v-model='queryModel.department' :clearable='true' placeholder='请输入科室'></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label='Bác sĩ' prop='medic'>
                <el-input v-model='queryModel.medic' :clearable='true' placeholder='Vui lòng nhập họ tên bác sĩ'></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label='Trạng thái' prop='status'>
                <el-select v-model='queryModel.status' :clearable='true' placeholder='Vui lòng chọn trạng thái' clearable>
                  <el-option
                    v-for="item in options"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
                <!--
                <el-input v-model='queryModel.status' :clearable='true' placeholder='Vui lòng chọn trạng thái'></el-input>
                -->
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="Thời gian khám" prop="diagnosisTime">
                <el-date-picker
                  :picker-options="filerData.pickerOptions"
                  v-model="queryModel.diagnosisTime"
                  type="datetimerange"
                  range-separator="đến"
                  start-placeholder="Ngày bắt đầu"
                  end-placeholder="Ngày kết thúc"
                  :default-time="['00:00:00', '23:59:59']"
                >
                </el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="3" style="display:flex;justivy-content:space-around">
              <el-button
                type="primary"
                icon="el-icon-search"
                @click="onSearch()"
                :plain="true"
              >Tìm kiếm
              </el-button>
              <el-button
                type="info"
                icon="el-icon-refresh-left"
                @click="reset"
                :plain="true"
              >Đặt lại
              </el-button>
            </el-col>
            <el-col :span="3" style="text-align:right;padding-right:5px">
              <el-button-group>
                <el-button
                  v-show="permission.add"
                  type="primary"
                  icon="el-icon-plus"
                  @click="onCreatePatient()"
                >Thêm
                </el-button
                >
              </el-button-group>
            </el-col>
          </el-form>
        </el-row>
        <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId' :routerId='$route.meta.routerId'
                   @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm>
      </div>
      <!--  Thanh tìm kiếm  Kết thúc -->

      <!-- Thanh công cụ Bắt đầu -->
      <!-- Thanh công cụ Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>
            <el-table ref="patientTableRef" :data='patientList' border @sort-change='onSortChange'
                      @header-dragend='onChangeWidth' :cell-class-name='cellClassName'
                      :header-cell-class-name='headerCellClassName' highlight-current-row>
              <el-table-column
                label="Số thứ tự"
                width="70px"
                type="index"
                :index="indexMethod"
                align="center">
              </el-table-column>
              <el-table-column prop="name" label="Họ tên"></el-table-column>
              <el-table-column prop="gender" label="Giới tính"></el-table-column>
              <el-table-column prop="card" label="Số giấy tờ"></el-table-column>
              <el-table-column prop="department" label="Khoa/phòng đề nghị"></el-table-column>
              <el-table-column prop="medic" label="Bác sĩ đề nghị"></el-table-column>
              <el-table-column prop="diagnosisTime" label="诊疗时间"></el-table-column>
              <el-table-column prop="createDate" label="Thời gian tạo"></el-table-column>
              <el-table-column prop="status" label="Trạng thái">
                <template slot-scope='scope'>
                  <span v-if="scope.row.status === '0'">Chưa gửi</span>
                  <span v-if="scope.row.status === '1'">Chờ xác nhận</span>
                  <span v-if="scope.row.status === '2'">Đang khám</span>
                  <span v-if="scope.row.status === '3'">Đã kết thúc</span>
                  <span v-if="scope.row.status === '4'">Đã phát thuốc</span>
                  <span v-if="scope.row.status === '5'">Đã trả lại</span>
                </template>
              </el-table-column>

              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :key="Math.random()" :width='150'>
                <template slot-scope='scope'>
                  <span v-if="scope.row.status === '0'" style="color: #0a7be0"
                        @click="submit(scope.$index, scope.row)">Gửi&nbsp;</span>
                  <span v-if="scope.row.status === '1'" style="color: #0a7be0"
                        @click="cancel(scope.$index, scope.row)">Thu hồi&nbsp;</span>
                  <span v-if="scope.row.status === '2'" style="color: #0a7be0"
                        @click="conference(scope.$index, scope.row)">Tham gia cuộc họp&nbsp;</span>
                <!--  <span v-if="scope.row.status === '2'" style="color: #0a7be0"
                        @click="sponsorConference(scope.$index, scope.row)">发起会议&nbsp;</span>-->
                  <span style="color: #0a7be0" @click="viewDiagnosis(scope.$index, scope.row)">Chi tiết&nbsp;</span>
                  <!--
                  <span style="color: #0a7be0" @click="statusEdit(scope.$index, scope.row)">Trạng thái&nbsp;</span>
                  -->
                  <span v-if="scope.row.status === '0'" style="color: red"
                        @click="onDeleteDiagnosis(scope.$index, scope.row)">Xóa&nbsp;</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
      <!-- Thanh bảng  Kết thúc -->
      <!-- Thanh phân trang  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <el-pagination
            background
            @size-change='onSizeChange'
            @current-change='onCurrentChange'
            :current-page.sync='currentPage'
            :page-sizes='[ 20, 50, 100, patientTotal]'
            :page-size='20'
            layout='total, sizes, prev, pager, next, jumper'
            :total='patientTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->     </el-card>
  </el-row>
</template>

<script>
  import {validatenull} from '@/utils/validate'
  // import {listPatientPage, getPatientById, deletePatient} from '@/api/outpatient/patient'
  import {listDiagnosisPage, getDiagnosisById, saveDiagnosis, deleteDiagnosis } from '@/api/outpatient/remoteDiagnosisTreatment'
  import {listResourcePermission} from '@/api/admin/common/permission'
  import PatientForm from './patientForm'
  import QueryForm from '@/views/components/queryForm'
  import MainUI from '@/views/components/mainUI'
  import History from '@/views/components/history'
  import {getLocalToken} from '@/utils/auth';
  import axios from 'axios'
  import Vue from "vue";

  Vue.prototype.$axios = axios;

  const loginForm = {Data: "API.Manage", AppId: "Oh_Newtouch_Clinic"}
  const config = {
    headers: {
      Token: getLocalToken()
    }
  }
  export default {
    extends: MainUI,
    components: {
      PatientForm,
      QueryForm,
      History
    },
    data() {
      return {
        tokenData: null,
        hisPatient: null,
        conferenceReturn: {}, // 会议申请返回内容
        permission: {
          caseDialogVisible: false,
          view: false,
          add: false,
          edit: false,
          remove: false,
          export: false
        },
        filerData: {
          pickerOptions: {
          },
        },
        options: [{
          value: '0',
          label: 'Chưa gửi'
        }, {
          value: '1',
          label: 'Chờ xác nhận'
        }, {
          value: '2',
          label: 'Đang khám'
        }, {
          value: '3',
          label: 'Đã kết thúc'
        }, {
          value: '4',
          label: 'Đã phát thuốc'
        }, {
          value: '5',
          label: 'Đã trả lại'
        }],
        queryTypes: {
          'name': 'like',
          'phone': 'like',
          'card': '=',
        },
        queryModel: {
          'name': '',   // Tên bệnh nhân
          'phone': '',   // Thông tin liên hệ
          'card': '',   // Số CMND/CCCD
          'department': '',   // Khoa
          'medic': '',   // Bác sĩ
          'diagnosisTime': [this.addCreateDate(), this.$moment(new Date()).endOf("day")],//Khoảng thời gian
          'createDate': [this.addCreateDate(), this.$moment(new Date()).endOf("day")],//Khoảng thời gian

        },
        search: {
          params: [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}],
          offset: 0,
          limit: 20,
          columnName: '',      // Tên trường sắp xếp
          order: ''            // Sắp xếp
        },
        currentPage: 1,
        patientTotal: 0,
        patientList: [],
        adetailsList: {},


        oprColumnWidth: 140,  // Độ rộng cột thao tác
        tableId: '1008489176147648530',
        schemeId: '1008489176147648553'
      }
    },
    methods: {
      reset() {
        this.$refs.queryForm.resetFields()
        this.onSearch()
      },
      getPatientList() {
        this.setLoad()
        /* Tham số truy vấn và quyền dữ liệu */
        this.search.params = [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}]
        if (this.moreCodition) {
          this.search.params = this.search.params.concat(this.compositeCondition())
        } else {
          // Tham số truy vấn: Tên bệnh nhân
          this.search.params.push({
            columnName: 'name',
            queryType: 'like',
            value: this.queryModel.name
          })
          // Tham số truy vấn: Trạng thái
          this.search.params.push({
            columnName: 'status',
            queryType: 'like',
            value: this.queryModel.status
          })
          // Tham số truy vấn: Số CMND/CCCD
          this.search.params.push({
            columnName: 'card',
            queryType: '=',
            value: this.queryModel.card
          })
          this.search.params.push({
            columnName: 'department',
            queryType: '=',
            value: this.queryModel.department
          })
          this.search.params.push({
            columnName: 'medic',
            queryType: '=',
            value: this.queryModel.medic
          })
          this.search.params.push({
            columnName: "diagnosis_time",
            queryType: ">=",
            value: this.queryModel.diagnosisTime
              ? this.$moment(this.queryModel.diagnosisTime[0]).format(
                "YYYY-MM-DD HH:mm:ss"
              )
              : "",
          });
          this.search.params.push({
            columnName: "diagnosis_time",
            queryType: "<=",
            value: this.queryModel.diagnosisTime
              ? this.$moment(this.queryModel.diagnosisTime[1]).format(
                "YYYY-MM-DD HH:mm:ss"
              )
              : "",
          });
          this.search.params.push({
            columnName: "create_date",
            queryType: ">=",
            value: this.queryModel.createDate
              ? this.$moment(this.queryModel.createDate[0]).format(
                "YYYY-MM-DD HH:mm:ss"
              )
              : "",
          });
          this.search.params.push({
            columnName: "create_date",
            queryType: "<=",
            value: this.queryModel.createDate
              ? this.$moment(this.queryModel.createDate[1]).format(
                "YYYY-MM-DD HH:mm:ss"
              )
              : "",
          });
        }
        // Quyền dữ liệu: Bảng bệnh nhânpatient
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
        listDiagnosisPage(this.search).then(responseData => {
          if (responseData.code === 100) {
            this.patientTotal = responseData.data.total
            this.patientList = responseData.data.rows
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      },
      indexMethod(index) {
        return (this.currentPage - 1) * this.search.limit + index + 1;
      },
      addCreateDate() {

        let myDate = new Date();
        let lw = new Date(myDate.getTime() - 1000 * 60 * 60 * 24 * 30); //Chữ số cuối cùng30có thể sửa，30nghĩa là ngày
        let lastY = lw.getFullYear();
        let lastM = lw.getMonth() + 1;
        let lastD = lw.getDate();
        let housrs = lw.getHours();
        let minutes = lw.getMinutes()
        let seconds = lw.getSeconds()
        let startData =
          lastY +
          "-" +
          (lastM < 10 ? "0" + lastM : lastM) +
          "-" +
          (lastD < 10 ? "0" + lastD : lastD) +
          " " + housrs + ":" + minutes + ":" + seconds //Ngày của 30 ngày trước
        return startData;

      },
      onSearch() {
        if (this.moreCodition) {
          this.search.offset = 0
          this.currentPage = 1
          this.getPatientList()
        } else {
          this.$refs['queryForm'].validate(valid => {
            if (valid) {
              this.search.offset = 0
              this.currentPage = 1
              this.getPatientList()
            } else {
              return false
            }
          })
        }
      },
      onSizeChange(val) {
        this.currentPage = 1
        this.search.limit = val;
        this.search.offset = (this.currentPage - 1) * val
        this.getPatientList()
      },
      onCurrentChange(val) {
        this.search.offset = (val - 1) * this.search.limit
        this.currentPage = val
        this.getPatientList()
      },
      async pageInit() {
        this.setLoad()
        try {
          this.initOptions(this.queryModel)
          this.search.params = [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}]
          // Quyền dữ liệu: Bảng bệnh nhânpatient
          this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
          let [listPatientRespData, listPermissionRespData] = await Promise.all([
            listDiagnosisPage(this.search),
            listResourcePermission(this.$route.meta.routerId)
          ])
          if (listPatientRespData.code === 100 && listPermissionRespData.code === 100) {
            this.patientTotal = listPatientRespData.data.total
            this.patientList = listPatientRespData.data.rows
            for (let i = 0; i < this.patientList.length; i++) {
              if (this.patientList[i].diagnosisTime) {
                this.patientList[i].diagnosisTime = this.patientList[i].diagnosisTime.replace("T", " ")
              }
            }
            this.permission.view = listPermissionRespData.data.find(item => {
              return item.permission === 'patient:read'
            })
            this.permission.export = listPermissionRespData.data.find(item => {
              return item.permission === 'patient:export'
            })
            this.permission.add = listPermissionRespData.data.find(item => {
              return item.permission === 'patient:create'
            })
            this.permission.edit = listPermissionRespData.data.find(item => {
              return item.permission === 'patient:update'
            })
            this.permission.remove = listPermissionRespData.data.find(item => {
              return item.permission === 'patient:delete'
            })
          } else {
            this.showMessage(listPermissionRespData.code !== 100 ? listPermissionRespData : listPatientRespData)
          }
          this.resetLoad()
        } catch (error) {
          this.outputError(error)
        }
      },
      // 提交保存申请
      submit(index, row) {
        this.setLoad()
        getDiagnosisById(row.id).then(responseData => {
          if (responseData.code === 100) {
            this.$refs.patientForm.openViewPatientDialog(responseData.data)
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      },
      // 查看详情信息
      viewDiagnosis(index, row) {
        this.setLoad()
        getDiagnosisById(row.id).then(responseData => {
          if (responseData.code === 100) {
            this.$refs.patientForm.openCopyPatientDialog(responseData.data)
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      },
      // Lấy phòng khám từ xatoken
      async getToken() {
        await this.$axios.post('/token/Auth/GetAppFrienAuthToken', loginForm, config)
          .then((response) => {
            this.tokenData = response.data.BusData.data.Token
          })
          .catch((error) => {
          });
      },
      // 重新申请发起会议
      async sponsorConference(index, row){
        await this.getToken()
        await this.$axios.post('apis/RemoteTreated/DoctorMeetingApply', {
          Data: {
            usercode: row.medicId,
            username: row.medic,
            applyId: row.id,
            roomReset: true,
            roomid: row.conferenceId
          },
          OrganizeId: row.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
            if (response.data.BusData.code === 20000) {
              this.$message.warning("Thông tin đề nghị khám chữa bệnh bất thường，请重新发送申请！")
            }else if (response.data.BusData.code === 10000){

            }
          })
          .catch((error) => {
          });
      },

      // Tham gia cuộc họp
      async conference(index, row) {
        await this.getToken();
        await this.$axios.post('apis/RemoteTreated/UserJoinMeetingApply', {
          Data: {
            usercode: currentUser.id,
            username: currentUser.name,
            applyId: row.id,
            roomid: row.conferenceId
          },
          OrganizeId: row.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
            if (response.data.BusData.code === 40004) {
              this.$message.warning("机构信息及关键申请信息不可为空！")
            } else if(response.data.BusData.code === 20000){
              this.$message.warning("音视频会议接口返回异常：会议房间已过期，请使用其他房间号！")

            } else if (response.data.BusData.code === 10000) {
              this.conferenceReturn = response.data.BusData
              // window.open('https://61.172.179.30:17015/' + this.conferenceReturn.data.roompath,'_blank')
              const i = {
                "rtcuserid": this.conferenceReturn.data.rtcuserid,
                  "roomid": this.conferenceReturn.data.roomid,
                  "username": this.conferenceReturn.data.username,
                  "roompath": this.conferenceReturn.data.roompath,
                  "organization": this.conferenceReturn.data.organization,
                  "device": this.conferenceReturn.data.device
              }
              // var a = 'https://61.172.179.30:17015/' + this.conferenceReturn.data.roompath
              const child = window.open('https://61.172.179.30:17015/' + this.conferenceReturn.data.roompath);
              setTimeout(() => {
                child.postMessage(i, "*");
              }, 3000);

              this.$message.success("会议加入成功！")
              /*setTimeout(() =>{
                window.postMessage(i,a,i)
              },2000)*/

            }
          })
          .catch((error) => {
          });
      },

      // 撤销申请
      cancel(index, row){
        this.$confirm('确定撤销申请吗？', 'Xác nhận', {
          confirmButtonText: 'Xác nhận',
          cancelButtonText: 'Hủy',
          type: 'warning'
        }).then(() => {
          row.status = "0"
          this.statusEdit(index, row)
        }).catch(() => {})
      },

      // Sửa trạng thái
      async statusEdit(index, row){
        await this.getToken();
        await this.$axios.post('apis/RemoteTreated/TreatedApplyInfo', {
          Data: {
            sqlsh: row.id // Số thứ tự đề nghị
          },
          OrganizeId: row.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
            this.hisPatient = response.data.BusData.data
          })
          .catch((error) => {
          });
        await this.$axios.post('apis/RemoteTreated/TreatedApplyCancel', {
          Data: {
            ApplyId: this.hisPatient.Id
          },
          OrganizeId: row.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        }).then((response) => {
          if (response.data.BusData.code === 10000){
            row.diagnosisTime = null;
            saveDiagnosis(row).then(responseData => {
              if (responseData.code === 100) {
                this.reset()
              } else {
                this.reset()
                this.showMessage(responseData)
              }
              this.resetLoad()
            }).catch(error => {
              this.outputError(error)
            })
          } else {
            this.$message.warning("Thông tin đề nghị khám chữa bệnh bất thường，Vui lòng liên hệ quản trị viên rồi gửi lại đề nghị!")
          }
        })
          .catch((error) => {
          });
      },
      onCreatePatient() {
        this.$refs.patientForm.openAddPatientDialog()
      },
      onEditPatient(index, row) {
        this.setLoad()
        getDiagnosisById(row.id).then(responseData => {
          if (responseData.code === 100) {
            this.$refs.patientForm.openEditPatientDialog(responseData.data)
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      },
      onCopyPatient(index, row) {
        this.setLoad()
        getDiagnosisById(row.id).then(responseData => {
          if (responseData.code === 100) {
            this.$refs.patientForm.openCopyPatientDialog(responseData.data)
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      },
      onDeleteDiagnosis(index, row) {
        this.$confirm('Bạn có chắc muốn xóa không？', 'Xác nhận', {
          confirmButtonText: 'Xác nhận',
          cancelButtonText: 'Hủy',
          type: 'warning'
        }).then(() => {
          this.setLoad()
          row.diagnosisTime = null;
          deleteDiagnosis(row).then(responseData => {
            if (responseData.code === 100) {
              this.getPatientList()
              this.showMessage({type: 'success', msg: 'Xóa thành công'})
            } else {
              this.showMessage(responseData)
            }
            this.resetLoad()
          }).catch(error => {
            this.outputError(error)
          })
        }).catch(() => {
        })
        this.reset();
      },
      onSortChange(orderby) {
        if (validatenull(orderby.prop)) {
          this.search.columnName = ''
          this.search.order = ''
        } else {
          this.search.columnName = orderby.prop
          this.search.order = orderby.order === 'descending' ? 'desc' : 'asc'
        }

        this.getPatientList()
      },
      initOptions(This) {
      }
    },
    watch: {
      // tableDataCóel-tableDữ liệu đã liên kết
      tableData: {
        // Khắc phục lỗi hiển thị lệch bảng
        handler() {
          this.$nextTick(() => {
            // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
            this.$refs.patientTableRef.doLayout()// Đối với Table Thực hiện bố cục lại
          })
        },
        deep: true
      }
    },
    updated() {
      //  if(this.$refs.patientTableRef){
      this.$nextTick(() => {
        // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
        this.$refs.patientTableRef.doLayout()// Đối với Table Thực hiện bố cục lại
      })
      //}
    },
    mounted() {
      this.pageInit()
    }
  }
</script>
<style lang="scss" scoped>
  .page-container {
    padding: 0;
  }

  ;

  ::v-deep .el-table {
    .el-table__fixed-body-wrapper {
      top: 47px !important;
    }
  }

  ;

  ::v-deep .el-table__fixed-right-patch {
    width: 5px !important
  }

  ;

  ::v-deep .el-table colgroup col[name='gutter'] {
    width: 5px !important
  }

  ;

  ::v-deep .el-table__body {
    width: 100% !important
  }

  ;

  .drag_table {
    // Cài đặt bảngheaderChiều cao của
    ::v-deep th {
      height: 44px;
    }

    ::v-deep th.gutter:last-of-type {
      height: 0 !important;
    }

    // Cài đặt bảngbodyChiều cao của
    ::v-deep .el-table__body-wrapper {
      //Khắc phục hiển thị dữ liệu vượt quábodychiều cao không cuộn đượcbug
      overflow-y: auto;
      // Phần trừ đi là bảngheaderChiều cao của
      height: calc(100% - 44px) !important;
    }

    .el-table__fixed-right {
      height: 100% !important;
    }

  }
</style>
<style scoped>
  ::v-deep .el-table__body-wrapper {
    height: calc(100% - 44px) !important;
  }
</style>
