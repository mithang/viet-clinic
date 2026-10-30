<template>
  <el-card class="page-container" style="padding: 0px;">
    <el-row>
      <el-col :span="24">
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
          <el-form-item label="Thời gian khám" :span="8" style="margin-right: 3px;">
            <div class="block" style="width:50%">
              <el-date-picker
                v-model="TimeInterval"
                type="datetimerange"
                :picker-options="pickerOptions"
                :default-time="['00:00:00','23:59:59']"
                range-separator="đến"
                start-placeholder="Ngày bắt đầu"
                end-placeholder="Ngày kết thúc"
                align="right"
                :clearable="false">
              </el-date-picker>
            </div>
          </el-form-item>
          <el-form-item label="Tên" style="margin-right: 3px;">
            <el-input v-model="formInline.patientName" placeholder="Vui lòng nhập tên bệnh nhân"></el-input>
          </el-form-item>
          <el-form-item label="Số CMND/CCCD" style="margin-right: 3px;">
            <el-input v-model="formInline.certificate" placeholder="Vui lòng nhập số CMND/CCCD của bệnh nhân"></el-input>
          </el-form-item>
          <el-form-item style="margin-right: 3px;">
            <el-button type="primary" @click="onSubmit">Truy vấn</el-button>
            <el-button type="info" icon="el-icon-refresh-left" @click='resetCondition' :plain='true'>Đặt lại</el-button>
            <!-- <el-button @click="show3 = !show3">{{show3==true?"Thu gọn":"Mở rộng"}}</el-button> -->
            <el-button type="primary" icon="el-icon-download" @click='onExport' :plain='true' >Xuất</el-button>
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="24">
        <el-table
          :data="tableData"
          ref="tableData"
          border
          height="calc(100vh - 280px)"
        >
          <el-table-column
            label="Số thứ tự"
            type="index"
            :index="indexMethod"
            align="center">
          </el-table-column>

          <el-table-column align="center" prop="patientName" label="Tên bệnh nhân"></el-table-column>
          <el-table-column align="center" prop="patriarchName" label="Họ tên phụ huynh"></el-table-column>
          <el-table-column align="center" prop="sex" label="Giới tính" :formatter="stateFormat"></el-table-column>
          <el-table-column align="center" prop="age" label="Tuổi bệnh nhân"></el-table-column>
          <el-table-column align="center" prop="nation" label="Dân tộc"></el-table-column>
          <el-table-column align="center" prop="occupation" label="Nghề nghiệp"></el-table-column>
          <el-table-column align="center" prop="address" label="Địa chỉ chi tiết"></el-table-column>
          <el-table-column align="center" prop="visitDate" label="Ngày khám"></el-table-column>
          <el-table-column align="center" prop="initialVisit" label="Khám lần đầu/Tái khám">
            <template slot-scope="scope">
              <span>{{ scope.row.initialVisit === "registrationSource_0" ? 'Khám lần đầu' : 'Tái khám' }}</span>
            </template>
          </el-table-column>
          <el-table-column align="center" prop="bloodPressure" label="Huyết áp"></el-table-column>
          <el-table-column align="center" prop="symptom" label="Triệu chứng lâm sàng"></el-table-column>
          <el-table-column align="center" prop="fever" label="Sốt (nhiệt độ tăng)"></el-table-column>
          <el-table-column align="center" prop="epidemicDisease" label="Tiền sử dịch tễ"></el-table-column>
          <!--<el-table-column align="center" prop="diagnosis" label="Chẩn đoán"></el-table-column>-->
          <el-table-column align="center" prop="westernDiagnose,chinaDiagnose" label="Chẩn đoán">
            <template slot-scope="scope">
              <span v-show="scope.row.westernDiagnose!=null">Chẩn đoán tây y:</span>
              {{ scope.row.westernDiagnose }}
              <span v-show="scope.row.westernDiagnose!=null&&scope.row.chinaDiagnose !=null">/</span>
              <span v-show="scope.row.chinaDiagnose!=null">Chẩn đoán đông y:</span>
              {{ scope.row.chinaDiagnose }}
            </template>
          </el-table-column>

          <el-table-column align="center" prop="infect" label="Bệnh truyền nhiễm">
            <template slot-scope="scope">
              <span>{{ scope.row.infect === "infectType_0" ? 'Không' : 'Có' }}</span>
            </template>
          </el-table-column>
          <el-table-column align="center" prop="handle" label="Tình hình xử lý"></el-table-column>
          <el-table-column align="center" prop="certificate" label="Số giấy tờ hợp lệ"></el-table-column>
          <el-table-column align="center" prop="unit" label="Đơn vị công tác"></el-table-column>
          <el-table-column align="center" prop="signature" label="Chữ ký bác sĩ"></el-table-column>
          <el-table-column align="center" prop="poverty" label="Dấu hiệu hộ nghèo">
            <template slot-scope="scope">
              <span v-show="scope.row.poverty!=null">Nghèo</span>
            </template>
          </el-table-column>
          <el-table-column align="center" prop="telephone" label="Số điện thoại liên hệ"></el-table-column>
          <el-table-column align="center" prop="morbidityDate" label="Ngày phát bệnh"></el-table-column>
          <el-table-column align="center" prop="positiveResult" label="Kết quả dương tính phòng xét nghiệm"></el-table-column>
          <el-table-column align="center" prop="healthEducation" label="Giáo dục sức khỏe cá nhân hóa">
            <template slot-scope="scope">
              <span v-if="(scope.row.healthEducation) && scope.row.healthEducation.length<=10">{{scope.row.healthEducation}}</span>
              <el-tooltip v-else  effect="dark" :content="scope.row.healthEducation" placement="left">
                <span>{{showHealthEducation(scope.row.healthEducation)}}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="Thao tác" width="100px">
            <template slot-scope="scope">
              <!-- 放置修改、删除和分配角色按钮 -->
              <el-button
                size="mini"
                type="success" plain
                @click.native.stop="showEditDialog(scope.row.id)"
              >Sửa
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
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
    <!-- 修改用户的对话框 -->
    <el-dialog title="Sửa nhật ký" :visible.sync="editDialogVisible" width="50%" @close="editDialogClosed"
               :close-on-click-modal='false'>
      <!-- 内容主体区 -->
      <el-form :model="editUserForm" ref="editUserFormRef" label-width="120px">
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Tên bệnh nhân:" prop="patientName">
              <el-input v-model="editUserForm.patientName"/>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Họ tên phụ huynh:" prop="patriarchName">
              <el-input v-model="editUserForm.patriarchName"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2"><template slot-scope="scope">
            <span v-show="scope.row.poverty!=null">Nghèo</span>
          </template>
            <el-form-item label="Giới tính:" prop="sex">
                <el-select v-model="editUserForm.sex" placeholder="Vui lòng chọn giới tính">
                  <el-option
                    v-for="item in gender"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Tuổi bệnh nhân:" prop="age">
              <el-input v-model="editUserForm.age"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Dân tộc:" prop="nation">
              <el-input v-model="editUserForm.nation"/>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Nghề nghiệp:" prop="occupation">
              <el-input v-model="editUserForm.occupation"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Địa chỉ chi tiết:" prop="address">
              <el-input v-model="editUserForm.address"/>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Ngày khám:" prop="visitDate">
              <div class="block">
                <el-date-picker
                  v-model="editUserForm.visitDate"
                  type="datetime"
                  placeholder="Chọn ngày giờ"
                  align="right"
                  :picker-options="pickerOptions">
                </el-date-picker>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Khám lần đầu/Tái khám:" prop="initialVisit">
              <el-select v-model="editUserForm.initialVisit" placeholder="Vui lòng chọn">
                <el-option
                  v-for="item in initialVisit"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Huyết áp:" prop="bloodPressure">
              <el-input v-model="editUserForm.bloodPressure"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Triệu chứng lâm sàng:" prop="symptom">
              <el-input v-model="editUserForm.symptom"/>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Sốt (nhiệt độ tăng):" prop="fever">
              <el-input v-model="editUserForm.fever"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Tiền sử dịch tễ:" prop="epidemicDisease">
              <el-input v-model="editUserForm.epidemicDisease"/>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Chẩn đoán tây y:" prop="symptom">
              <el-input v-model="editUserForm.westernDiagnose"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Chẩn đoán đông y:" prop="symptom">
              <el-input v-model="editUserForm.chinaDiagnose"/>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Tình hình xử lý:" prop="handle">
              <el-input v-model="editUserForm.handle"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Số giấy tờ hợp lệ:" prop="certificate">
              <el-input v-model="editUserForm.certificate"/>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Đơn vị công tác:" prop="unit">
              <el-input v-model="editUserForm.unit"/>
            </el-form-item>
          </el-col>
        </el-row>
          <!--<el-col :span="24 / 2">
            <el-form-item label="Dấu hiệu hộ nghèo:" prop="poverty">
              <el-input v-model="editUserForm.signature" disabled/>
            </el-form-item>
          </el-col>-->
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Số điện thoại liên hệ:" prop="telephone">
              <el-input v-model="editUserForm.telephone"/>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Ngày phát bệnh:" prop="morbidityDate">
              <div class="block">
                <el-date-picker
                  v-model="editUserForm.morbidityDate"
                  type="datetime"
                  placeholder="Chọn ngày giờ"
                  align="right"
                  :picker-options="pickerOptions">
                </el-date-picker>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Kết quả dương tính phòng xét nghiệm:" prop="positiveResult">
              <el-input v-model="editUserForm.positiveResult"/>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Chữ ký bác sĩ:" prop="signature">
              <el-input v-model="editUserForm.signature"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Giáo dục sức khỏe cá nhân hóa:" prop="healthEducation">
              <el-input type="textarea" :autosize="{ minRows: 2, maxRows: 4}" v-model="editUserForm.healthEducation"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <!-- 底部区 -->
      <span slot="footer" class="dialog-footer">
     <el-button @click="editDialogVisible = false">Hủy</el-button>
     <el-button type="primary" @click="editUser()">Xác nhận</el-button>
     </span>
    </el-dialog>
    、
  </el-card>
</template>

<script>
  import {getStuffsalessummarylists, getStuffsalessummarysumss} from "@/api/toll/tollInfo";
  import {get, list, update,exportExcel} from "../../../api/toll/outpatientLog";
  import {BigNumber} from "bignumber.js";
  import RoleForm from "../../admin/role/roleForm";
  export default {

    data() {
      return {
        pickerOption: {
          shortcuts: [{
            text: 'Hôm nay',
            onClick(picker) {
              picker.$emit('pick', new Date());
            }
          }, {
            text: 'Hôm qua',
            onClick(picker) {
              const date = new Date();
              date.setTime(date.getTime() - 3600 * 1000 * 24);
              picker.$emit('pick', date);
            }
          }, {
            text: 'Một tuần trước',
            onClick(picker) {
              const date = new Date();
              date.setTime(date.getTime() - 3600 * 1000 * 24 * 7);
              picker.$emit('pick', date);
            }
          }]
        },
        gender: [{ // Giới tính
          value: 'gender_0',
          label: 'Nam'
        }, {
          value: 'gender_1',
          label: 'Nữ'
        }, {
        }],
        initialVisit: [{ // Giới tính
          value: 'registrationSource_0',
          label: 'Khám lần đầu'
        }, {
          value: 'registrationSource_1',
          label: 'Tái khám'
        }, {
        }],
        queryInfo: {
          query: "",
          pagenum: 1, //当前的页数
          pagesize: 2, //每页的数量
        },
        userList: [],
        total: 0,
        addDialogVisible: false, //控制添加用户对话框的显示与隐藏
        editDialogVisible: false, //控制修改用户对话框的显示与隐藏
        //添加用户的表单数据
        addUserForm: {},
        //修改用户的表单数据
        editUserForm: {},
        show3: false,
        //Chọn thời gian
        TimeInterval: this.getInitializeDate(),
        formInline: {
          patientName: "",
          certificate: "",
        },

        pickerOptions: {
          shortcuts: [{
            text: 'Tuần gần nhất',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: 'Tháng gần nhất',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '3 tháng gần nhất',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
              picker.$emit('pick', [start, end]);
            }
          }]
        },

        tableData: [],

        StuffsalessummaryRc: {
          limit: 20,
          offset: 0,
          companyId: currentUser.company.id,
          kssj: "",
          jssj: "",
          patientName: "",
          certificate: "",
        },
        currentPage: 1,
        patientList: [],
        patientTotal: 0,
        allTotal: {},

      };
    },
    updated() {
      this.$nextTick(() => {
        this.$refs['tableData'].doLayout();
      })
    },
    methods: {
      showHealthEducation(conetnt){
        if (!conetnt) {
          return "";
        }
        return conetnt.substring(0, 10)+"...";
      },
      //关闭编辑用户的对话框
      editDialogClosed() {
        this.$refs.editUserFormRef.resetFields();
      },
      async showEditDialog(id) {
        const res = await get(id);
        this.editUserForm = res.data;
        this.editDialogVisible = true;
      },
      //展示编辑用户的对话框
      /*showEditDialog(id) {
        this.editDialogVisible = true;
      },*/
      stateFormat(row, column) {
        if (row.sex === "gender_0") {
          return 'Nam'
        } else {
          return 'Nữ'
        }
        if (row.initialVisit === "registrationSource_0") {
          return 'Khám lần đầu'
        } else {
          return 'Tái khám'
        }
      },
      //Đặt ngày mặc định
      getInitializeDate() {
        let date = new Date()//Lấy thời gian mới
        //Lấy năm hiện tại,và chuyển thành chuỗi
        let year = date.getFullYear().toString()
        //Lấy tháng hiện tại，vì tháng bắt đầu từ0Bắt đầu，tại đây cần cộng1，
        //Sử dụng biểu thức ba ngôi，Kiểm tra có nhỏ hơn không10，Nếu vậy，thì nối thêm vào trước chuỗi'0'
        let month = date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1).toString() : (date.getMonth() + 1).toString()
        //Lấy ngày，Kiểm tra có nhỏ hơn không10，Nếu vậy，thì nối thêm vào trước chuỗi'0'
        let day = date.getDate() < 10 ? '0' + date.getDate().toString() : date.getDate().toString()
        //Nối chuỗi，Nối thời gian bắt đầu và thời gian kết thúc
        let start = year + '-' + month + '-01' + ' 00:00:00'    //Ngày đầu tiên của tháng hiện tại
        //let end=new Date(year, month, 0).getDate();
        let end = year + '-' + month + '-' + day + ' 23:59:59'  //Trong ngày
        return [start, end] //Gán giá trị cho thành phầnDatePicker Dữ liệu đã liên kết
      },
      //excelXuất
      onExport(){
        // if (this.TimeInterval) {
        //   this.StuffsalessummaryRc.kssj = this.TimeInterval[0];
        //   this.StuffsalessummaryRc.jssj = this.TimeInterval[1]
        // }
        // this.StuffsalessummaryRc.patientName = this.formInline.patientName;
        // this.StuffsalessummaryRc.certificate = this.formInline.certificate;
        exportExcel(this.StuffsalessummaryRc).then((res)=>{
          // const filename = decodeURI(res.headers.split(';')[1].split('=')[1]) || '.xls'
          const filename = '门诊日志.xlsx'
          const blob = new Blob([res.data], {
            type: 'application/octet-stream'
          })

          let url = window.URL.createObjectURL(blob);

          let link = document.createElement('a');

          link.style.display = 'none';

          link.href = url;

          link.setAttribute('download', filename);

          document.body.appendChild(link);

          link.click()
        }).catch((error)=>{
          this.outputError(error)
        })
      },
      onSizeChange(val) {
        this.currentPage = 1
        this.StuffsalessummaryRc.limit = val;
        this.StuffsalessummaryRc.offset = (this.currentPage - 1) * val
        this.Getypjxclist();
      },
      onCurrentChange(val) {
        this.StuffsalessummaryRc.offset = (val - 1) * this.StuffsalessummaryRc.limit
        this.currentPage = val
        this.Getypjxclist();
      },
      indexMethod(index) {
        return (this.currentPage - 1) * this.StuffsalessummaryRc.limit + index + 1;
      },
      resetCondition() {
        this.StuffsalessummaryRc = {
          limit: 20,
          offset: 0,
          companyId: currentUser.company.id,
          kssj: "",
          jssj: "",
          patientName: "",
          certificate: "",
        },
          this.formInline = {
            patientName: "",
            certificate: "",
          },
          this.TimeInterval = this.getInitializeDate(),
          this.Getypjxclist();
      },
      orderbyChange() {
        this.Getypjxclist();
      },
      Getypjxclist() {
        this.tableData = [];
        if (this.TimeInterval) {
          this.StuffsalessummaryRc.kssj = this.TimeInterval[0];
          this.StuffsalessummaryRc.jssj = this.TimeInterval[1]
        }
        this.StuffsalessummaryRc.patientName = this.formInline.patientName;
        this.StuffsalessummaryRc.certificate = this.formInline.certificate;
        list(this.StuffsalessummaryRc).then((responseData) => {
          if (responseData.code === 100) {
            this.patientTotal = responseData.data.total;
            if (responseData.data.total > 0) {
              responseData.data.rows.forEach((item) => {
                this.tableData.push({
                  id: item.id,  //Bệnh nhânid
                  patientName: item.patientName,  //Tên bệnh nhân
                  patriarchName: item.patriarchName,  //Họ tên phụ huynh
                  sex: item.sex,  //Giới tính
                  age: item.age,  //Tuổi bệnh nhân
                  nation: item.nation,  //Dân tộc
                  occupation: item.occupation,  //Nghề nghiệp
                  address: item.address,  //Địa chỉ chi tiết
                  visitDate: item.visitDate,  //Ngày khám
                  initialVisit: item.initialVisit,  //初/Tái khám
                  bloodPressure: item.bloodPressure,  //Huyết áp
                  symptom: item.symptom,  //Triệu chứng lâm sàng
                  fever: item.fever,  //Sốt (nhiệt độ tăng)
                  epidemicDisease: item.epidemicDisease,  //Tiền sử dịch tễ
                  westernDiagnose: item.westernDiagnose,  //Chẩn đoán tây y
                  chinaDiagnose: item.chinaDiagnose,  //Chẩn đoán đông y
                  diagnosis: item.diagnosis,  //Chẩn đoán
                  infect: item.infect,  //Bệnh truyền nhiễm
                  handle: item.handle,  //Tình hình xử lý
                  certificate: item.certificate,  //Số giấy tờ hợp lệ
                  unit: item.unit,  //Đơn vị công tác
                  signature: item.signature,  //Chữ ký bác sĩ
                  poverty: item.poverty,  //Dấu hiệu hộ nghèo
                  telephone: item.telephone,  //Số điện thoại liên hệ
                  morbidityDate: item.morbidityDate,  //Ngày phát bệnh
                  positiveResult: item.positiveResult,  //Kết quả dương tính phòng xét nghiệm
                  healthEducation: item.healthEducation  //Giáo dục sức khỏe cá nhân hóa
                })
              })

              getStuffsalessummarysumss(this.StuffsalessummaryRc).then((ref) => {
                if (ref.data.total > 0) {
                  this.allTotal = ref.data.rows[0];
                }

              })
            }
          }
        })
      },
      //点击按钮，修改用户信息
      editUser(){
        this.$refs.editUserFormRef.validate(async valid =>{
          if(!valid) return;//校验没通过，Quay lại
          //可以发起修改用户的网络请求
          await update(this.editUserForm).then(response => {
            this.$message.success('Sửa người dùng thành công!');
            // 隐藏修改用户的对话框
            this.editDialogVisible = false;
            //重新获取用户列表数据
            this.Getypjxclist();
          })

        })
      },
      onSubmit() {
        this.Getypjxclist();
      },
    },

    mounted() {
      this.Getypjxclist();
    }
  };
</script>
<style lang="scss">
  .el-picker-panel__footer .el-picker-panel__link-btn.el-button--text {
    display: none;
  }

  .el-card__body {
    padding: 0px;
  }
</style>
