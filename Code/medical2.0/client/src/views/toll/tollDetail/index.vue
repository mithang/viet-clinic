<template>
  <el-card class="page-container">
    <el-row type="flex" justify="space-between">
      <el-col>
        <el-radio-group v-model="tabPosition">
          <el-radio-button label="main">Báo cáo tổng hợp</el-radio-button>
          <el-radio-button label="detail">Báo cáo chi tiết</el-radio-button>
        </el-radio-group>
      </el-col>
      <el-col style="text-align:right;padding-right:5px">
        <el-button type="primary" style="margin-left:0" icon="el-icon-s-order" @click='exportExcel'>Xuất</el-button>
      </el-col>
    </el-row>
    <div style="margin-top:8px;">
      <div v-if="tabPosition === 'main'">
        <div>
          <el-row>
            <el-col :span="7" class="flex-start">
              <div class="condition-name">Ngày thu phí: </div>
              <el-date-picker
                style="width:320px;"
                v-model="mainCondition.chargeDateRange"
                type="daterange"
                range-separator="đến"
                start-placeholder="Ngày bắt đầu"
                end-placeholder="Ngày kết thúc"
                :default-time="['00:00:00', '23:59:59']"
                value-format="yyyy-MM-dd HH:mm:ss"
                format="dd/MM/yyyy HH:mm:ss"
              ></el-date-picker>
            </el-col>
            <el-col :span="6" class="flex-start">
              <div class="condition-name">Loại chi phí: </div>
              <el-select
                v-model="mainCondition.tollType"
                placeholder="Loại chi phí"
              >
                <el-option
                  v-for="detailCostType in detailCostTypes"
                  v-show="detailCostType.value !== 'recipelType_2' || detailCostType.value !== 'recipelType_3'"
                  :key="detailCostType.value"
                  :label="detailCostType.name"
                  :value="detailCostType.value"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="6" class="flex-start">
              <div class="condition-name">Người thu phí: </div>
              <el-select
                v-model="mainCondition.create_by"
                placeholder="Người thu phí"
              >
                <el-option
                  v-for="chargeOper in createBys"
                  :key="chargeOper.createBy"
                  :label="chargeOper.createBy"
                  :value="chargeOper.createBy"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="5" class="flex-start">
              <div class="condition-name">Tên bệnh nhân: </div>
              <el-select
                v-model="mainCondition.name"
                value-key="id"
                filterable
                clearable
                style="width: 200px"
                placeholder="Vui lòng chọn bệnh nhân"
              >
                <el-option
                  v-for="patientId in AllPatientOption"
                  :key="patientId.id"
                  :label="patientId.name"
                  :value="patientId.id"
                ></el-option>
              </el-select>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="7" class="flex-start">
              <div class="condition-name">Số phiếu thu phí: </div>
              <el-input
                style="width:320px;"
                v-model="mainCondition.tollNumber"
                placeholder="Số phiếu thu phí"
              ></el-input>
            </el-col>
            <el-col :span="6" class="flex-start">
              <div class="condition-name">Khoa lập phiếu: </div>
              <el-select
                v-model="mainCondition.prescriptionDept"
                placeholder="Khoa lập phiếu"
              >
                <el-option
                  v-for="prescriptionDept in prescriptionDepts"
                  :key="prescriptionDept.id"
                  :label="prescriptionDept.name"
                  :value="prescriptionDept.id"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="6" class="flex-start">
              <div class="condition-name">Bác sĩ kê đơn: </div>
              <el-select
                v-model="mainCondition.prescriptionOper"
                placeholder="Bác sĩ kê đơn"
              >
                <el-option
                  v-for="prescriptionOper in prescriptionOpers"
                  :key="prescriptionOper.id"
                  :label="prescriptionOper.name"
                  :value="prescriptionOper.id"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="5" class="flex-start">
              <div class="condition-name">Trạng thái thu phí: </div>
              <el-select
                style="width:200px;"
                v-model="mainCondition.chargeStatu"
                placeholder="Trạng thái thu phí"
              >
                <el-option
                  v-for="chargeStatu in chargeStatus"
                  :key="chargeStatu.id"
                  :label="chargeStatu.name"
                  :value="chargeStatu.id"
                >
                </el-option>
              </el-select>
            </el-col>
          </el-row>
        </div>
        <div class="flex-end" style="margin-top:8px;margin-bottom:8px;padding-right:5px">
          <el-button type="primary" icon="el-icon-search" @click='getChargeMainData' :plain='true'>Tìm kiếm</el-button>
          <el-button type="info" style="margin-left:10px" icon="el-icon-refresh-left" @click='resetAll' :plain='true'>
            Đặt lại
          </el-button>
        </div>
        <div>
          <el-table
            :cell-class-name="cellClassName"
            :header-cell-class-name="headerCellClassName"
            height="calc(100vh - 353px)"
            ref="mutipleTable1"
            border id="exportTable" :data="tollList" :summary-method="getMainSummaries" show-summary
            style="width: 100%">
            <el-table-column type='index' width="width" align="center" label="Số thứ tự">
              <template slot-scope="scope">
                {{ chargeMainTablePagesize * (chargeMainTableCurrentPage - 1) + scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column prop="recipel.code" label="Số đơn thuốc" width="180px">
              <template slot-scope="scope">
                <div class="flex-container">
                  <el-tag class="custom-tag" v-if="scope.row.recipel.chronicDisease" primary disable-transitions>Mạn
                  </el-tag>
                  {{ scope.row.recipel.code }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="patient.name" label="Tên bệnh nhân" width="width">
            </el-table-column>
            <el-table-column prop="patient.gender.name" label="Giới tính" width="60px">
            </el-table-column>
            <el-table-column prop="patient.age" label="Tuổi" width="60px">
            </el-table-column>
            <el-table-column prop="tollNumber" label="Số phiếu thu phí" width="150px">
            </el-table-column>
            <el-table-column prop="tollType.name" label="Loại chi phí" width="width">
            </el-table-column>
            <el-table-column prop="doctor.department.name" label="Khoa lập phiếu" width="width">
            </el-table-column>
            <el-table-column prop="doctor.name" label="Bác sĩ kê đơn" width="100px">
            </el-table-column>
            <el-table-column prop="amountStatus.name" label="Trạng thái chứng từ" width="width">
            </el-table-column>
            <el-table-column prop="amountReceivable" label="Số tiền phải thu (hoàn lại)" width="100px">
              <template slot-scope="scope">
              <span v-if="scope.row.amountStatus.value==='amountStatus_1'">
                {{ scope.row.amountReceivable ? bigNum(scope.row.amountReceivable) : 0 }}VNĐ
              </span>
                <span style="color:red" v-else-if="scope.row.amountStatus.value==='amountStatus_2'">
                {{ scope.row.amountReceivable ? '-' + bigNum(scope.row.amountReceivable) : 0 }}VNĐ
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="amountReceived" label="Số tiền thực thu" width="width">
              <template slot-scope="scope">
              <span v-if="scope.row.amountStatus.value==='amountStatus_1'">
                {{ scope.row.amountReceived ? bigNum(scope.row.amountReceived) : 0 }}VNĐ
              </span>
                <span style="color:red" v-else-if="scope.row.amountStatus.value==='amountStatus_2'">
                {{ scope.row.amountReceived ? '-' + bigNum(scope.row.amountReceived) : 0 }}VNĐ
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="amountDiscounted" label="Số tiền ưu đãi" width="width">
              <template slot-scope="scope">
              <span v-if="scope.row.amountStatus.value==='amountStatus_1'">
                {{ scope.row.amountDiscounted ? bigNum(scope.row.amountDiscounted) : 0 }}VNĐ
              </span>
                <span style="color:red" v-else-if="scope.row.amountStatus.value==='amountStatus_2'">
                {{ scope.row.amountDiscounted ? '-' + bigNum(scope.row.amountDiscounted) : 0 }}VNĐ
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentType.name" label="Thanh toán tiền mặt" width="width">
              <template slot-scope="scope">
              <span v-if="scope.row.amountStatus.value==='amountStatus_1'">
                {{
                  scope.row.paymentType ? (scope.row.paymentType.value === 'payType_0' ? bigNum(scope.row.amountReceived) : 0) : 0
                }}VNĐ
              </span>
                <span style="color:red" v-else-if="scope.row.amountStatus.value==='amountStatus_2'">
                {{
                    scope.row.paymentType ? (scope.row.paymentType.value === 'payType_0' ? '-' + bigNum(scope.row.amountReceived) : 0) : 0
                  }}VNĐ
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentType.name" label="Thanh toán WeChat" width="width">
              <template slot-scope="scope">
              <span v-if="scope.row.amountStatus.value==='amountStatus_1'">
                {{
                  scope.row.paymentType ? (scope.row.paymentType.value === 'payType_2' ? bigNum(scope.row.amountReceived) : 0) : 0
                }}VNĐ
              </span>
                <span style="color:red" v-else-if="scope.row.amountStatus.value==='amountStatus_2'">
                {{
                    scope.row.paymentType ? (scope.row.paymentType.value === 'payType_2' ? '-' + bigNum(scope.row.amountReceived) : 0) : 0
                  }}VNĐ
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentType.name" label="Thanh toán Alipay" width="width">
              <template slot-scope="scope">
              <span v-if="scope.row.amountStatus.value==='amountStatus_1'">
                {{
                  scope.row.paymentType ? (scope.row.paymentType.value === 'payType_1' ? bigNum(scope.row.amountReceived) : 0) : 0
                }}VNĐ
              </span>
                <span style="color:red" v-else-if="scope.row.amountStatus.value==='amountStatus_2'">
                {{
                    scope.row.paymentType ? (scope.row.paymentType.value === 'payType_1' ? '-' + bigNum(scope.row.amountReceived) : 0) : 0
                  }}VNĐ
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentType.name" label="Thanh toán bằng thẻ ngân hàng" width="width">
              <template slot-scope="scope">
               <span v-if="scope.row.amountStatus.value==='amountStatus_1'">
                {{
                   scope.row.paymentType ? (scope.row.paymentType.value === 'payType_3' ? bigNum(scope.row.amountReceived) : 0) : 0
                 }}VNĐ
              </span>
                <span style="color:red" v-else-if="scope.row.amountStatus.value==='amountStatus_2'">
                {{
                    scope.row.paymentType ? (scope.row.paymentType.value === 'payType_3' ? '-' + bigNum(scope.row.amountReceived) : 0) : 0
                  }}VNĐ
              </span>
              </template>
            </el-table-column>
                        <el-table-column prop="paymentType.name" label="Chuyển khoản ngân hàng" width="width">
              <template slot-scope="scope">
              <span v-if="scope.row.amountStatus.value==='amountStatus_1'">
                {{
                  scope.row.paymentType ? (scope.row.paymentType.value === 'payType_5' ? bigNum(scope.row.amountReceived) : 0) : 0
                }}VNĐ
              </span>
                <span style="color:red" v-else-if="scope.row.amountStatus.value==='amountStatus_2'">
                  {{
                    scope.row.paymentType ? (scope.row.paymentType.value === 'payType_5' ? '-' + bigNum(scope.row.amountReceived) : 0) : 0
                  }}VNĐ
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentType.name" label="Ví điện tử" width="width">
              <template slot-scope="scope">
              <span v-if="scope.row.amountStatus.value==='amountStatus_1'">
                {{
                  scope.row.paymentType ? (scope.row.paymentType.value === 'payType_6' ? bigNum(scope.row.amountReceived) : 0) : 0
                }}VNĐ
              </span>
                <span style="color:red" v-else-if="scope.row.amountStatus.value==='amountStatus_2'">
                  {{
                    scope.row.paymentType ? (scope.row.paymentType.value === 'payType_6' ? '-' + bigNum(scope.row.amountReceived) : 0) : 0
                  }}VNĐ
              </span>
              </template>
            </el-table-column>
<el-table-column prop="paymentType.name" label="Thanh toán bảo hiểm y tế" width="width">
              <template slot-scope="scope">
                <!--Đã thu phí-->
                <span v-if="scope.row.amountStatus.value==='amountStatus_1'">
                {{
                    scope.row.paymentType ? (scope.row.paymentType.value === 'payType_4' ? bigNum(scope.row.amountReceived) : 0) : 0
                  }}VNĐ
              </span>
                <!--Đã hoàn phí-->
                <span style="color:red" v-else-if="scope.row.amountStatus.value==='amountStatus_2'">
                {{
                    scope.row.paymentType ? (scope.row.paymentType.value === 'payType_4' ? '-' + bigNum(scope.row.amountReceived) : 0) : 0
                  }}VNĐ
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="createBy" label="Người thu phí" width="100px">
              <template slot-scope="scope">
              <span>
                {{ getName(scope.row.createBy) }}
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="createDate" label="Ngày thu phí" width="150px">
            </el-table-column>
          </el-table>
        </div>
        <div class="flex-end">
          <el-pagination
            @size-change="handleSizeChangeMain"
            @current-change="handlePageChangeMain"
            :page-sizes="[20, 50, 100, 1000]"
            :page-size="chargeMainTablePagesize"
            :current-page="chargeMainTableCurrentPage"
            layout="total,sizes, prev, pager, next,jumper"
            :total="chargeMainTableTotal"
          >
          </el-pagination>
        </div>
      </div>
      <div v-else>
        <div>
          <el-row>
            <el-col :span="7" class="flex-start">
              <div class="condition-name">Ngày thu phí: </div>
              <el-date-picker
                style="width:320px;"
                v-model="detailCondition.chargeDateRange"
                type="daterange"
                range-separator="đến"
                start-placeholder="Ngày bắt đầu"
                end-placeholder="Ngày kết thúc"
                value-format="yyyy-MM-dd HH:mm:ss"
                format="dd/MM/yyyy HH:mm:ss"
              ></el-date-picker>
            </el-col>
            <el-col :span="6" class="flex-start">
              <div class="condition-name">Loại chi phí: </div>
              <el-select
                v-model="detailCondition.tollType"
                placeholder="Loại chi phí"
              >
                <el-option
                  v-for="detailCostType in detailCostTypes"
                  v-show="detailCostType.value !== 'recipelType_2' || detailCostType.value !== 'recipelType_3'"
                  :key="detailCostType.value"
                  :label="detailCostType.name"
                  :value="detailCostType.value"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="6" class="flex-start">
              <div class="condition-name">Người thu phí: </div>
              <el-select
                v-model="detailCondition.create_by"
                placeholder="Người thu phí"
              >
                <el-option
                  v-for="chargeOper in createBys"
                  :key="chargeOper.createBy"
                  :label="chargeOper.createBy"
                  :value="chargeOper.createBy"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="5" class="flex-start">
              <div class="condition-name">Tên bệnh nhân: </div>
              <el-select
                v-model="detailCondition.name"
                value-key="id"
                filterable
                clearable
                style="width: 200px"
                placeholder="Vui lòng chọn bệnh nhân"
              >
                <el-option
                  v-for="patientId in AllPatientOption"
                  :key="patientId.id"
                  :label="patientId.name"
                  :value="patientId.id"
                ></el-option>
              </el-select>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="7" class="flex-start">
              <div class="condition-name">Số phiếu thu phí: </div>
              <el-input
                style="width:320px;"
                v-model="detailCondition.tollNumber"
                placeholder="Số phiếu thu phí"
              ></el-input>
            </el-col>
            <el-col :span="6" class="flex-start">
              <div class="condition-name">Phân loại thu phí: </div>
              <el-select
                v-model="detailCondition.payType"
                placeholder="Phân loại thu phí"
              >
                <el-option
                  v-for="item in payTypes"
                  :key="item.value"
                  :label="item.name"
                  :value="item.value"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="6" class="flex-start">
              <div class="condition-name">Khoa lập phiếu: </div>
              <el-select
                v-model="detailCondition.prescriptionDept"
                placeholder="Khoa lập phiếu"
              >
                <el-option
                  v-for="prescriptionDept in prescriptionDepts"
                  :key="prescriptionDept.id"
                  :label="prescriptionDept.name"
                  :value="prescriptionDept.id"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="5" class="flex-start">
              <div class="condition-name">Bác sĩ kê đơn: </div>
              <el-select
                v-model="detailCondition.prescriptionOper"
                placeholder="Bác sĩ kê đơn"
                style="width: 200px"
              >
                <el-option
                  v-for="prescriptionOper in prescriptionOpers"
                  :key="prescriptionOper.id"
                  :label="prescriptionOper.name"
                  :value="prescriptionOper.id"
                >
                </el-option>
              </el-select>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="5" class="flex-start">
              <div class="condition-name">Trạng thái thu phí: </div>
              <el-select
                style="width:200px;"
                v-model="detailCondition.chargeStatu"
                placeholder="Người thu phí"
              >
                <el-option
                  v-for="chargeStatu in chargeStatus"
                  :key="chargeStatu.id"
                  :label="chargeStatu.name"
                  :value="chargeStatu.id"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="19" style="margin-bottom:8px;text-align:right;padding-right:5px">
              <el-button type="primary" icon="el-icon-search" @click='getChargeDetailData' :plain='true'>Tìm kiếm</el-button>
              <el-button type="info" style="margin-left:10px" icon="el-icon-refresh-left" @click='resetDetail'
                         :plain='true'>Đặt lại
              </el-button>
            </el-col>
          </el-row>
        </div>
        <div>
          <el-table
            :cell-class-name="cellClassName"
            :header-cell-class-name="headerCellClassName"
            ref="mutipleTable2"
            height="calc(100vh - 350px)"
            border id="exportTable" :data="detailList" :summary-method="getDetailSummaries" show-summary
            style="width: 100%">
            <el-table-column type="index" label="Số thứ tự" width="width" align="center">
              <template slot-scope="scope">
                {{ chargeDetailTablePagesize * (chargeDetailTableCurrentPage - 1) + scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column prop="recipel.code" label="Số đơn thuốc" width="180">
              <template slot-scope="scope">
                <div class="flex-container">
                  <el-tag class="custom-tag" v-if="scope.row.recipel.chronicDisease" primary disable-transitions>Mạn
                  </el-tag>
                  {{ scope.row.recipel.code }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="patient.name" label="Tên bệnh nhân" width="width">
            </el-table-column>
            <el-table-column prop="tollNumber" label="Số phiếu thu phí" width="150">
            </el-table-column>
            <el-table-column prop="doctor.department.name" label="Khoa" width="width">
            </el-table-column>
            <el-table-column prop="doctor.name" label="Bác sĩ" width="120px">
            </el-table-column>
            <el-table-column prop="recipelDetail.drugStuffId.name" label="Hạng mục thu phí" width="180px">
            </el-table-column>
            <el-table-column prop="tollType.name" label="Loại chi phí" width="width">
            </el-table-column>
            <el-table-column prop="recipelDetail.drugStuffId.isUnpackSell" label="Phân loại thu phí" width="width">
            </el-table-column>
            <el-table-column v-if="false" prop="recipelDetail.total" label="Số lượng" width="width">
              <template slot-scope="scope">
              <span v-if="scope.row.doseUnit==null">
                {{ scope.row.recipelDetail.total }}cái
              </span>
                <span v-if="scope.row.doseUnit!=null">
                {{ scope.row.recipelDetail.total }}{{ scope.row.doseUnit }}
              </span>
              </template>
            </el-table-column>
            <el-table-column label="Số lượng" width="width">
              <template slot-scope="scope">
              <span v-if="scope.row.doseUnit==null">
                {{ scope.row.recipelDetail.total }}cái
              </span>
                <span v-if="scope.row.doseUnit!=null">
                {{ scope.row.recipelDetail.total }}{{ scope.row.doseUnit }}
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="amountStatus.name" label="Trạng thái chứng từ" width="width">
            </el-table-column>
            <el-table-column prop="amountReceivable" label="Phải thu/Trả lại(VNĐ)" width="width">
              <template slot-scope="scope">
              <span v-if="scope.row.amountStatus.value==='amountStatus_1'">
                {{ scope.row.amountReceivable ? bigNum(scope.row.amountReceivable) : 0 }}VNĐ
              </span>
                <span style="color:red" v-else-if="scope.row.amountStatus.value==='amountStatus_2'">
                {{ scope.row.amountReceivable ? "-" + bigNum(scope.row.amountReceivable) : 0 }}VNĐ
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="amountReceived" label="Thực thu/Trả lại(VNĐ)" width="width">
              <template slot-scope="scope">
              <span v-if="scope.row.amountStatus.value==='amountStatus_1'">
                {{ scope.row.amountReceived ? bigNum(scope.row.amountReceived) : 0 }}VNĐ
              </span>
                <span style="color:red" v-else-if="scope.row.amountStatus.value==='amountStatus_2'">
                {{ scope.row.amountReceived ? "-" + bigNum(scope.row.amountReceived) : 0 }}VNĐ
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="createBy" label="Người thu phí" width="100">
              <template slot-scope="scope">
              <span>
                {{ getName(scope.row.createBy) }}
              </span>
              </template>
            </el-table-column>
            <el-table-column prop="createDate" label="Ngày thu phí" width="180">
            </el-table-column>
          </el-table>
        </div>

        <div class="flex-end">
          <el-pagination
            @size-change="handleSizeChangeDetail"
            @current-change="handlePageChangeDetail"
            :page-sizes="[5, 10, 15, 20]"
            :page-size="chargeDetailTablePagesize"
            :current-page="chargeDetailTableCurrentPage"
            layout="total,sizes, prev, pager, next,jumper"
            :total="chargeDetailTableTotal"
          >
          </el-pagination>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script>
  import MainUI from "@/views/components/mainUI";
  import {getDictItemsByCode, DICT_CODE} from "@/utils/dictCache";
  import {tollTotalForm, tollDetailForm, listTollInfoAll} from "@/api/toll/tollInfo";
  import {listUserAll} from "@/api/admin/user";
  import {getCreateBy} from "@/api/toll/tollDetail";
  import {listClinicOfficeAll} from "@/api/org/clinicOffice";
  import {listPatientAll} from "@/api/outpatient/patient";
  import {BigNumber} from "bignumber.js";
  import { getCurrentUser } from "@/utils/userCache";

  import FileSaver from 'file-saver'
  import * as XLSX from 'xlsx'
  import XLSXD from 'xlsx-style'

  export default {
    extends: MainUI,
    data() {
      return {
        tabPosition: "main",
        /*Điều kiện truy vấn báo cáo tổng hợp*/
        mainCondition: {
          chargeDateRange: [this.addCreateDate(), this.addCreateDate2()] /*Khoảng ngày thu phí*/,
          tollType: '',
          create_by: null,
          name: "",
          tollNumber: "",
          prescriptionDept: null,
          prescriptionOper: null,
          chargeStatu: '',
        },
        tollTypeOption: [
          {
            value: '',
            name: "Tất cả",
          },
          {
            value: 'tollType_2',
            name: "Bán lẻ thuốc",
          },
          {
            value: 'tollType_0',
            name: "Đơn thuốc",
          },
          {
            value: 'tollType_1',
            name: "Đăng ký khám",
          },
        ],
        detailCondition: {
          chargeDateRange: [this.addCreateDate(), this.addCreateDate2()] /*Khoảng ngày thu phí*/,
          detailCostType: '',
          create_by: null,
          name: "",
          tollNumber: "",
          chargeItem: "",
          prescriptionDept: null,
          prescriptionOper: null,
          chargeStatu: '',
          payType: ''
        },
        detailCostTypes: [{
          id: '',
          name: 'Tất cả'
        }],
        payTypes: [{
          value: '',
          name: 'Tất cả'
        }, {
          value: 'stuff',
          name: 'Vật tư'
        }],

        chargeOpers: [],
        prescriptionDepts: [],
        prescriptionOpers: [],
        createBys: [],
        chargeStatus: [
          {
            id: '',
            name: "Tất cả",
          },
          {
            id: 'amountStatus_1',
            name: "Thu phí",
          },
          {
            id: 'amountStatus_2',
            name: "Hoàn phí",
          },
        ],
        chargeMainTablePagesize: 20 /*Kích thước trang bảng chính*/,
        chargeMainTableCurrentPage: 1 /*Trang bảng chính hiện tại*/,
        chargeMainTableTotal: 0 /*Tổng dữ liệu bảng chính*/,

        chargeDetailTablePagesize: 20 /*Kích thước trang chi tiết*/,
        chargeDetailTableCurrentPage: 1 /*Trang chi tiết hiện tại*/,
        chargeDetailTableTotal: 0 /*Tổng dữ liệu chi tiết*/,

        listTollInfoAllOption: [],
        AllPatientOption: [],
        tollTotalTable: {
          page: {
            rows: []
          }
        },
        tollList: [],
        tollDetailTable: {
          page: {
            rows: []
          }
        },
        detailList: [],
        changeView: 0
      };
    },
    computed: {
      Company() {
        let company = getCurrentUser().company;
        return {
          id: company.id,
          label: company.label,
          name: company.name,
        };
      },
      Department() {
        let department = getCurrentUser().department;
        return {
          id: department.id,
          label: department.label,
          name: department.name,
        };
      }
    },
    mounted() {
      this.getChargeMainData()
      this.getChargeDetailData()
      this.GetOption()
      this.$nextTick(() => {
        this.changeView = 1
      })
    },
    updated() {
      this.$nextTick(() => {
        if (this.tabPosition === 'main') {
          this.$refs.mutipleTable1.doLayout();
        } else {
          this.$refs.mutipleTable2.doLayout();
        }

      });
    },
    methods: {

      getName(row) {
        let num = row.indexOf("(");
        let name = row.substring(0, num)
        return name;
      },

      // Giờ Việt Nam (+07) tính từ mốc UTC, không phụ thuộc múi giờ trình duyệt:
      // khoảng mặc định trước đây lấy giờ local của trình duyệt nên ở môi trường
      // UTC (VD container) mốc cuối chậm 7 giờ, khoản thu buổi tối +07 rơi ngoài.
      // Chỉ đổi tham số mặc định ở tầng hiển thị; value-format và định dạng
      // tham số API ("YYYY-MM-DD HH:mm:ss") giữ nguyên.
      saigonDateString(date) {
        return this.$moment
          .utc(date.getTime() + 7 * 60 * 60 * 1000)
          .format("YYYY-MM-DD HH:mm:ss");
      },
      addCreateDate2() {
        return this.saigonDateString(new Date());
      },
      addCreateDate() {
        // 30 ngày trước, tính trên mốc giờ +07
        return this.saigonDateString(
          new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
        );
      },
      resetAll() {
        this.mainCondition = {
          chargeDateRange: [this.addCreateDate(), this.addCreateDate2()] /*Khoảng ngày thu phí*/,
          tollType: '',
          create_by: null,
          name: "",
          tollNumber: "",
          prescriptionDept: null,
          prescriptionOper: null,
          chargeStatu: '',
        }
        this.getChargeMainData()
      },
      resetDetail() {
        this.detailCondition = {
          chargeDateRange: [this.addCreateDate(), this.addCreateDate2()] /*Khoảng ngày thu phí*/,
          detailCostType: '',
          create_by: null,
          name: "",
          tollNumber: "",
          chargeItem: "",
          prescriptionDept: null,
          prescriptionOper: null,
          chargeStatu: '',
          payType: ''
        }
        this.getChargeDetailData()
      },
      bigNum(num) {
        if (num || num === '0') {
          return new BigNumber(num).toFormat(2)
        } else {
          return ''
        }
      },
      getMainSummaries() {
        let arr = ['Tổng cộng', '', '', '', '', '', '', '', '', '',
          new BigNumber(this.tollTotalTable.amountReceivableTotal).toFormat(2) + "VNĐ",
          new BigNumber(this.tollTotalTable.amountReceivedTotal).toFormat(2) + "VNĐ",
          new BigNumber(this.tollTotalTable.amountReceivableTotal - this.tollTotalTable.amountReceivedTotal).toFormat(2) + "VNĐ",
          new BigNumber(this.tollTotalTable.cashTotal).toFormat(2) + "VNĐ",
          new BigNumber(this.tollTotalTable.weChatTotal).toFormat(2) + "VNĐ",
          new BigNumber(this.tollTotalTable.alipayTotal).toFormat(2) + "VNĐ",
          new BigNumber(this.tollTotalTable.bankCardPayTotal).toFormat(2) + "VNĐ",
          new BigNumber(this.tollTotalTable.transferTotal).toFormat(2) + "VNĐ",
          new BigNumber(this.tollTotalTable.ewalletTotal).toFormat(2) + "VNĐ",
          new BigNumber(this.tollTotalTable.ybTotal).toFormat(2) + "VNĐ"]
        return arr
      },
      getDetailSummaries() {
        let arr = ['Tổng cộng', '', '', '', '', '', '', '', '', '', '', new BigNumber(this.tollDetailTable.amountReceivableTotal).toFormat(2) + "VNĐ", new BigNumber(this.tollDetailTable.amountReceivedTotal).toFormat(2) + "VNĐ"]
        return arr
      },
      GetOption() {
        let SearchModel = {
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: this.Company.id,
          }]
        }
        getDictItemsByCode(DICT_CODE.TOLL_TYPE).then((data) => {
          this.detailCostTypes = [...this.detailCostTypes, ...data]
        })
        listTollInfoAll(SearchModel).then(responseData => {
          if (responseData.code === 100) {
            this.listTollInfoAllOption = responseData.data
          }
        })
        getDictItemsByCode(DICT_CODE.MEDICAL_TYPE).then((data) => {
          this.payTypes = [...this.payTypes, ...data]
          getDictItemsByCode(DICT_CODE.TREATMENT_ITEM_TYPE).then((data2) => {
            this.payTypes = [...this.payTypes, ...data2]
          })
        })
        listClinicOfficeAll(SearchModel).then(responseData => {
          if (responseData.code === 100) {
            this.prescriptionDepts = responseData.data
          }
        })
        listPatientAll(SearchModel).then((responseData) => {
          if (responseData.code === 100) {
            this.AllPatientOption = responseData.data;
          }
        });
        let DocorSearch = {
          params: [{
            columnName: "department_id",
            queryType: "=",
            value: this.Department.id,
          },
            {
              columnName: "company_id",
              queryType: "=",
              value: this.Company.id,
            }, {
              columnName: "is_locked",
              queryType: "=",
              value: 0,
            },
          ]
        }
        listUserAll(DocorSearch).then((responseData) => {
          this.prescriptionOpers = responseData.data;
        });
        let createBySearch = {
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: this.Company.id,
          }]
        }
        getCreateBy(createBySearch).then(res => {
          this.createBys = res.data
        })
      },
      handleSizeChangeMain(value) {
        this.chargeMainTablePagesize = value
        this.getChargeMainData()
      },
      handlePageChangeMain(value) {
        this.chargeMainTableCurrentPage = value
        this.getChargeMainData()
      },
      handleSizeChangeDetail(value) {
        this.chargeDetailTablePagesize = value
        this.getChargeDetailData()
      },
      handlePageChangeDetail(value) {
        this.chargeDetailTableCurrentPage = value
        this.getChargeDetailData()
      },
      getChargeMainData() {
        let SearchModel = {
          columnName: "",
          limit: this.chargeMainTablePagesize,
          offset: (this.chargeMainTableCurrentPage - 1) * this.chargeMainTablePagesize,
          order: "",
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: this.Company.id,
          }, {
            columnName: "toll_type",
            queryType: "=",
            value: this.mainCondition.tollType,
          }, {
            columnName: "toll_number",
            queryType: "=",
            value: this.mainCondition.tollNumber,
          }, {
            columnName: "patient_id",
            queryType: "=",
            value: this.mainCondition.name,
          }, {
            columnName: "department.id",
            queryType: "=",
            value: this.mainCondition.prescriptionDept,
          }, {
            columnName: "create_by",
            queryType: "=",
            value: this.mainCondition.create_by,
          }, {
            columnName: "amount_status",
            queryType: "=",
            value: this.mainCondition.chargeStatu,
          }, {
            columnName: "user.id",
            queryType: "=",
            value: this.mainCondition.prescriptionOper,
          }],
        };
        if (this.mainCondition.chargeDateRange && this.mainCondition.chargeDateRange.length) {
          this.mainCondition.chargeDateRange[0] = this.$moment(this.mainCondition.chargeDateRange[0]).format(
            "YYYY-MM-DD HH:mm:ss"
          )
          this.mainCondition.chargeDateRange[1] = this.$moment(this.mainCondition.chargeDateRange[1]).format(
            "YYYY-MM-DD HH:mm:ss"
          )
          SearchModel.params.push({
            logic: "AND",
            queryType: "("
          }, {
            columnName: "create_date",
            logic: "",
            queryType: 'between',
            value: this.mainCondition.chargeDateRange,
          }, {
            logic: "",
            queryType: ")"
          })
        }
        this.setLoad();
        tollTotalForm(SearchModel).then(responseData => {
          if (responseData.code === 100) {
            this.chargeMainTableTotal = responseData.data.page.total
            this.tollTotalTable = responseData.data
            this.tollList = this.tollTotalTable.page.rows
            this.resetLoad();
          }
        })
      },
      getChargeDetailData() {
        this.setLoad();
        let payName = ""
        let isStuff = false
        switch (this.detailCondition.payType) {
          case 'medicalType_0':
            payName = 'drug.type'
            break;
          case 'medicalType_1':
            payName = 'drug.type'
            break;
          case 'medicalType_2':
            payName = 'drug.type'
            break;
          case 'treatmentItemType_0':
            payName = 'costItem.item_type'
            break;
          case 'treatmentItemType_1':
            payName = 'costItem.item_type'
            break;
          case 'treatmentItemType_2':
            payName = 'costItem.item_type'
            break;
          case 'treatmentItemType_3':
            payName = 'costItem.item_type'
            break;
          case 'treatmentItemType_4':
            payName = 'costItem.item_type'
            break;
          case 'stuff':
            payName = 'stuff.id'
            isStuff = true
            break;
          default:
            break;
        }
        let SearchModel = {
          columnName: "",
          limit: this.chargeDetailTablePagesize,
          offset: (this.chargeDetailTableCurrentPage - 1) * this.chargeDetailTablePagesize,
          order: "",
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: this.Company.id,
          }, {
            columnName: "toll_type",
            queryType: "=",
            value: this.detailCondition.tollType,
          }, {
            columnName: "toll_number",
            queryType: "=",
            value: this.detailCondition.tollNumber,
          }, {
            columnName: "department.id",
            queryType: "=",
            value: this.detailCondition.prescriptionDept,
          }, {
            columnName: "create_by",
            queryType: "=",
            value: this.detailCondition.create_by,
          }, {
            columnName: "patient_id",
            queryType: "=",
            value: this.detailCondition.name,
          }, {
            columnName: "amount_status",
            queryType: "=",
            value: this.detailCondition.chargeStatu,
          }, {
            columnName: "user.id",
            queryType: "=",
            value: this.detailCondition.prescriptionOper,
          }],
        };
        if (this.detailCondition.chargeDateRange && this.detailCondition.chargeDateRange.length) {
          this.mainCondition.chargeDateRange[0] = this.$moment(this.mainCondition.chargeDateRange[0]).format(
            "YYYY-MM-DD HH:mm:ss"
          )
          this.mainCondition.chargeDateRange[1] = this.$moment(this.mainCondition.chargeDateRange[1]).format(
            "YYYY-MM-DD HH:mm:ss"
          )
          SearchModel.params.push({
            logic: "AND",
            queryType: "("
          }, {
            columnName: "create_date",
            logic: "",
            queryType: 'between',
            value: this.detailCondition.chargeDateRange,
          }, {
            logic: "",
            queryType: ")"
          })
        }
        if (this.detailCondition.payType) {
          if (isStuff) {
            SearchModel.params.push({
              columnName: payName,
              queryType: "!==",
              value: "''",
            })
          } else {
            SearchModel.params.push({
              columnName: payName,
              queryType: "=",
              value: this.detailCondition.payType,
            })
          }
        }
        tollDetailForm(SearchModel).then(responseData => {
          if (responseData.code === 100) {
            this.chargeDetailTableTotal = responseData.data.page.total
            this.tollDetailTable = responseData.data
            this.detailList = this.tollDetailTable.page.rows
            this.resetLoad();
          }
        })
      },
      //Xuất bảng
      exportExcel() {
        // var xlsxParam = { raw: true } // Nội dung xuất chỉ phân tích, không chuyển đổi định dạng
        // var table = document.querySelector('#exportTable').cloneNode(true)
        // // table.removeChild(table.querySelector('.el-table__fixed')) //Ở đây là dấu gạch dưới kép
        // var wb = XLSX.utils.table_to_book(table, xlsxParam)

        // var wbout = XLSX.write(wb, { bookType: 'xlsx', bookSST: true, type: 'array' })
        // try {
        //   FileSaver.saveAs(new Blob([wbout], { type: 'application/octet-stream' }), 'Báo cáo tổng hợp.xlsx')
        // } catch (e) {
        //   if (typeof console !== 'undefined') {
        //   }
        // }
        // return wbout
        let biaoge = []
        if (this.tabPosition === 'main') {
          biaoge[0] = {
            code: "Số đơn thuốc",
            name: "Tên bệnh nhân",
            gender: "Giới tính",
            age: "Tuổi",
            tollNumber: "Số phiếu thu phí",
            tollType: "Loại chi phí",
            department: "Khoa lập phiếu",
            doctor: "Bác sĩ kê đơn",
            batchNo: "Số lô sản xuất",
            produceDate: "Ngày sản xuất",
            amountStatus: "Trạng thái chứng từ",
            amountReceivable: "Số tiền phải thu (hoàn lại)",
            amountReceived: "Số tiền thực thu",
            amountDiscounted: "Số tiền ưu đãi",
            cash: "Thanh toán tiền mặt",
            wx: "Thanh toán WeChat",
            zfb: "Thanh toán Alipay",
            card: "Thanh toán bằng thẻ ngân hàng",
            transfer: "Chuyển khoản ngân hàng",
            ewallet: "Ví điện tử",
            yb: "Thanh toán bảo hiểm y tế",
            createBy: "Người thu phí",
            createDate: "Ngày thu phí"
          }
          for (let i = 0; i < this.tollList.length; i++) {
            let arr = {
              code: "",
              name: "",
              gender: "",
              age: "",
              tollNumber: "",
              tollType: "",
              department: "",
              doctor: "",
              batchNo: "",
              produceDate: "",
              amountStatus: "",
              amountReceivable: "",
              amountReceived: "",
              amountDiscounted: "",
              cash: "",
              wx: "",
              zfb: "",
              card: "",
              transfer: "",
              ewallet: "",
              yb: "",
              createBy: "",
              createDate: ""
            }
            arr.code = this.tollList[i].recipel.code
            arr.name = this.tollList[i].patient.name
            arr.gender = this.tollList[i].patient.gender.name
            arr.age = this.tollList[i].patient.age
            arr.tollNumber = this.tollList[i].tollNumber
            arr.tollType = this.tollList[i].tollType.name
            arr.department = this.tollList[i].doctor.department.name
            arr.doctor = this.tollList[i].doctor.name ? this.tollList[i].doctor.name : ''
            arr.amountStatus = this.tollList[i].amountStatus.name
            if (this.tollList[i].amountStatus.value === 'amountStatus_1') {
              arr.amountReceivable = this.tollList[i].amountReceivable + ''
              arr.amountReceived = this.tollList[i].amountReceived + ''
              arr.amountDiscounted = this.tollList[i].amountDiscounted !== 0 && this.tollList[i].amountDiscounted !== undefined ? this.tollList[i].amountDiscounted + '' : '0'
              arr.cash = this.tollList[i].paymentType.value === 'payType_0' ? this.tollList[i].amountReceived + '' : ''
              arr.wx = this.tollList[i].paymentType.value === 'payType_2' ? this.tollList[i].amountReceived + '' : ''
              arr.zfb = this.tollList[i].paymentType.value === 'payType_1' ? this.tollList[i].amountReceived + '' : ''
              arr.card = this.tollList[i].paymentType.value === 'payType_3' ? this.tollList[i].amountReceived + '' : ''
              arr.transfer = this.tollList[i].paymentType.value === 'payType_5' ? this.tollList[i].amountReceived + '' : ''
              arr.ewallet = this.tollList[i].paymentType.value === 'payType_6' ? this.tollList[i].amountReceived + '' : ''
              arr.yb = this.tollList[i].paymentType.value === 'payType_4' ? this.tollList[i].amountReceived + '' : ''
            } else if (this.tollList[i].amountStatus.value === 'amountStatus_2') {
              arr.amountReceivable = this.tollList[i].amountReceivable !== 0 ? '-' + this.tollList[i].amountReceivable + '' : '0'
              arr.amountReceived = this.tollList[i].amountReceived !== 0 ? '-' + this.tollList[i].amountReceived + '' : '0'
              arr.amountDiscounted = this.tollList[i].amountDiscounted !== 0 && this.tollList[i].amountDiscounted !== undefined ? '-' + this.tollList[i].amountDiscounted + '' : '0'
              arr.cash = this.tollList[i].paymentType.value === 'payType_0' ? '-' + this.tollList[i].amountReceived + '' : ''
              arr.wx = this.tollList[i].paymentType.value === 'payType_2' ? '-' + this.tollList[i].amountReceived + '' : ''
              arr.zfb = this.tollList[i].paymentType.value === 'payType_1' ? '-' + this.tollList[i].amountReceived + '' : ''
              arr.yb = this.tollList[i].paymentType.value === 'payType_4' ? '-' + this.tollList[i].amountReceived + '' : ''
              arr.card = this.tollList[i].paymentType.value === 'payType_3' ? '-' + this.tollList[i].amountReceived + '' : ''
              arr.transfer = this.tollList[i].paymentType.value === 'payType_5' ? '-' + this.tollList[i].amountReceived + '' : ''
              arr.ewallet = this.tollList[i].paymentType.value === 'payType_6' ? '-' + this.tollList[i].amountReceived + '' : ''
            }
            arr.createBy = this.tollList[i].createBy
            arr.createDate = this.tollList[i].createDate
            biaoge.push(arr)
          }
          let num = biaoge.length
          biaoge[num] = {
            code: "Tổng cộng",
            name: "",
            gender: "",
            age: "",
            tollNumber: "",
            tollType: "",
            department: "",
            doctor: "",
            batchNo: "",
            produceDate: "",
            amountStatus: "",
            amountReceivable: new BigNumber(this.tollTotalTable.amountReceivableTotal).toFormat(2) + "",
            amountReceived: new BigNumber(this.tollTotalTable.amountReceivedTotal).toFormat(2) + "",
            amountDiscounted: new BigNumber(this.tollTotalTable.amountReceivableTotal - this.tollTotalTable.amountReceivedTotal).toFormat(2) + "",
            cash: new BigNumber(this.tollTotalTable.cashTotal).toFormat(2) + "",
            wx: new BigNumber(this.tollTotalTable.weChatTotal).toFormat(2) + "",
            zfb: new BigNumber(this.tollTotalTable.alipayTotal).toFormat(2) + "",
            card: new BigNumber(this.tollTotalTable.bankCardPayTotal).toFormat(2) + "",
            transfer: new BigNumber(this.tollTotalTable.transferTotal).toFormat(2) + "",
            ewallet: new BigNumber(this.tollTotalTable.ewalletTotal).toFormat(2) + "",
            yb: new BigNumber(this.tollTotalTable.ybTotal).toFormat(2) + "",
            createBy: "",
            createDate: ""
          }
          let web = XLSX.utils.book_new();
          let contentWs = XLSX.utils.json_to_sheet(biaoge);
          contentWs["A1"] = {
            t: "s",
            v: this.Company.name + ' Báo cáo chi phí (Tổng hợp)',
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
            }
          }
          for (let i = 0; i < biaoge.length; i++) {
            contentWs["A" + (i + 2)] = {
              t: "s",
              v: biaoge[i].code,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["B" + (i + 2)] = {
              t: "s",
              v: biaoge[i].name,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},
                }
              }
            }
            contentWs["C" + (i + 2)] = {
              t: "s",
              v: biaoge[i].gender,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["D" + (i + 2)] = {
              t: "s",
              v: biaoge[i].age,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},
                }
              }
            }
            contentWs["E" + (i + 2)] = {
              t: "s",
              v: biaoge[i].tollNumber,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},
                }
              }
            }

            contentWs["F" + (i + 2)] = {
              t: "s",
              v: biaoge[i].tollType,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["G" + (i + 2)] = {
              t: "s",
              v: biaoge[i].department,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["H" + (i + 2)] = {
              t: "s",
              v: biaoge[i].doctor,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["I" + (i + 2)] = {
              t: "s",
              v: biaoge[i].batchNo,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["J" + (i + 2)] = {
              t: "s",
              v: biaoge[i].produceDate,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["K" + (i + 2)] = {
              t: "s",
              v: biaoge[i].amountStatus,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }

            contentWs["L" + (i + 2)] = {
              t: "s",
              v: biaoge[i].amountReceivable,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["M" + (i + 2)] = {
              t: "s",
              v: biaoge[i].amountReceived,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["N" + (i + 2)] = {
              t: "s",
              v: biaoge[i].amountDiscounted,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["O" + (i + 2)] = {
              t: "s",
              v: biaoge[i].cash,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["P" + (i + 2)] = {
              t: "s",
              v: biaoge[i].wx,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["Q" + (i + 2)] = {
              t: "s",
              v: biaoge[i].zfb,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["R" + (i + 2)] = {
              t: "s",
              v: biaoge[i].card,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["S" + (i + 2)] = {
              t: "s",
              v: biaoge[i].createBy,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["T" + (i + 2)] = {
              t: "s",
              v: biaoge[i].createDate,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["U" + (i + 2)] = {
              t: "s",
              v: biaoge[i].yb,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
          }
          const mergeTitle = [

            {
              s: {r: 0, c: 0},
              e: {r: 0, c: 19}
            },

          ]
          //Cố định dòng đầu tiên và cột đầu tiên: 
          contentWs['!freeze'] = {
            xSplit: "11",  //Cố định cột
            ySplit: "1",  //Cố định dòng
            topLeftCell: "A2", //Ô hiển thị ở góc trên bên trái của vùng chưa cố định, Mặc định là ô chưa cố định đầu tiên
            state: "frozen"
          }
          contentWs["!merges"] = mergeTitle

          contentWs["!cols"] = [{wch: 20}, {wch: 15}, {wch: 12}, {wch: 10}, {wch: 20}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 10}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 20}, {wch: 20}]
          contentWs["!rows"] = {}

          contentWs["!margins"] = {left: 1.0, right: 1.0, top: 1.0, bottom: 1.0, header: 0.5, footer: 0.5}
          XLSX.utils.book_append_sheet(web, contentWs, "Báo cáo chi phí (Tổng hợp)");
          // //Sử dụngxlsx-styleCách ghi tệp, để kiểu tùy chỉnh có hiệu lực
          const tmpDown = new Blob([
            this.s2ab(
              XLSXD.write(web, {
                bookType: "xlsx",
                BookSST: true,
                type: "binary",
                cellStyles: true,
              })
            )
          ])
          this.downExcel(tmpDown, 'Báo cáo chi phí (Tổng hợp).xlsx')
        } else {
          biaoge[0] = {
            code: "Số đơn thuốc",
            name: "Tên bệnh nhân",
            tollNumber: "Số phiếu thu phí",
            department: "Khoa",
            doctor: "Bác sĩ kê đơn",
            drugStuffId: "Hạng mục thu phí",
            tollType: "Loại chi phí",
            isUnpackSell: "Phân loại thu phí",
            total: 'Số lượng',
            doseUnit: 'Đơn vị',
            amountStatus: "Trạng thái chứng từ",
            amountReceivable: "Phải thu/Trả lại(VNĐ)",
            createBy: "Người thu phí",
            createDate: "Ngày thu phí"
          }
          for (let i = 0; i < this.detailList.length; i++) {
            let arr = {
              code: "",
              name: "",
              tollNumber: "",
              department: "",
              doctor: "",
              drugStuffId: "",
              tollType: "",
              isUnpackSell: "",
              total: '',
              doseUnit: '',
              amountStatus: "",
              amountReceivable: "",
              createBy: "",
              createDate: ""
            }
            arr.code = this.detailList[i].recipel.code
            arr.name = this.detailList[i].patient.name
            arr.tollNumber = this.detailList[i].tollNumber
            arr.tollType = this.detailList[i].tollType.name
            arr.department = this.detailList[i].doctor.department.name
            arr.total = this.detailList[i].recipelDetail.total
            arr.doseUnit = this.detailList[i].doseUnit
            arr.drugStuffId = this.detailList[i].recipelDetail.drugStuffId.name
            arr.isUnpackSell = this.detailList[i].recipelDetail.drugStuffId.isUnpackSell
            arr.doctor = this.detailList[i].doctor.name ? this.detailList[i].doctor.name : ''
            arr.amountStatus = this.detailList[i].amountStatus.name
            if (this.detailList[i].amountStatus.value === 'amountStatus_1') {
              arr.amountReceivable = this.detailList[i].amountReceivable + ''
            } else if (this.detailList[i].amountStatus.value === 'amountStatus_2') {
              arr.amountReceivable = this.detailList[i].amountReceivable !== 0 ? '-' + this.detailList[i].amountReceivable + '' : '0'
            }
            arr.createBy = this.detailList[i].createBy
            arr.createDate = this.detailList[i].createDate
            biaoge.push(arr)
          }
          let num = biaoge.length
          biaoge[num] = {
            code: "Tổng cộng",
            name: "",
            tollNumber: "",
            department: "",
            doctor: "",
            drugStuffId: "",
            tollType: "",
            isUnpackSell: "",
            total: '',
            doseUnit: '',
            amountStatus: "",
            amountReceivable: new BigNumber(this.tollTotalTable.amountReceivableTotal).toFormat(2) + "",
            createBy: "",
            createDate: ""
          }
          let web = XLSX.utils.book_new();
          let contentWs = XLSX.utils.json_to_sheet(biaoge);
          contentWs["A1"] = {
            t: "s",
            v: this.Company.name + ' Báo cáo chi phí (Chi tiết)',
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
            }
          }
          for (let i = 0; i < biaoge.length; i++) {
            contentWs["A" + (i + 2)] = {
              t: "s",
              v: biaoge[i].code,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["B" + (i + 2)] = {
              t: "s",
              v: biaoge[i].name,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["C" + (i + 2)] = {
              t: "s",
              v: biaoge[i].tollNumber,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["D" + (i + 2)] = {
              t: "s",
              v: biaoge[i].department,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["E" + (i + 2)] = {
              t: "s",
              v: biaoge[i].doctor,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }

            contentWs["F" + (i + 2)] = {
              t: "s",
              v: biaoge[i].drugStuffId,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["G" + (i + 2)] = {
              t: "s",
              v: biaoge[i].tollType,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["H" + (i + 2)] = {
              t: "s",
              v: biaoge[i].isUnpackSell,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["I" + (i + 2)] = {
              t: "s",
              v: biaoge[i].total,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["J" + (i + 2)] = {
              t: "s",
              v: biaoge[i].amountStatus,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["K" + (i + 2)] = {
              t: "s",
              v: biaoge[i].amountReceivable,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }

            contentWs["L" + (i + 2)] = {
              t: "s",
              v: biaoge[i].createBy,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }
            contentWs["M" + (i + 2)] = {
              t: "s",
              v: biaoge[i].createDate,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {style: 'thin'},
                  left: {style: 'thin'},
                  bottom: {style: 'thin'},
                  right: {style: 'thin'},

                }
              }
            }

          }
          const mergeTitle = [

            {
              s: {r: 0, c: 0},
              e: {r: 0, c: 12}
            },

          ]
          //Cố định dòng đầu tiên và cột đầu tiên: 
          contentWs['!freeze'] = {
            xSplit: "11",  //Cố định cột
            ySplit: "1",  //Cố định dòng
            topLeftCell: "A2", //Ô hiển thị ở góc trên bên trái của vùng chưa cố định, Mặc định là ô chưa cố định đầu tiên
            state: "frozen"
          }
          contentWs["!merges"] = mergeTitle

          contentWs["!cols"] = [{wch: 20}, {wch: 10}, {wch: 20}, {wch: 10}, {wch: 15}, {wch: 25}, {wch: 15}, {wch: 15}, {wch: 10}, {wch: 10}, {wch: 10}, {wch: 25}, {wch: 20}]
          contentWs["!rows"] = {}

          contentWs["!margins"] = {left: 1.0, right: 1.0, top: 1.0, bottom: 1.0, header: 0.5, footer: 0.5}
          XLSX.utils.book_append_sheet(web, contentWs, "Báo cáo chi phí (Chi tiết)");
          // //Sử dụngxlsx-styleCách ghi tệp, để kiểu tùy chỉnh có hiệu lực
          const tmpDown = new Blob([
            this.s2ab(
              XLSXD.write(web, {
                bookType: "xlsx",
                BookSST: true,
                type: "binary",
                cellStyles: true,
              })
            )
          ])
          this.downExcel(tmpDown, 'Báo cáo chi phí (Chi tiết).xlsx')
        }

      },
      downExcel(obj, fileName) {
        const a_node = document.createElement("a");
        a_node.download = fileName;
        if ("msSaveOrOpenBlob" in navigator) {
          window.navigator.msSaveOrOpenBlob(obj, fileName)
        } else {
          a_node.href = URL.createObjectURL(obj);
        }
        a_node.click()

        setTimeout(() => {
          URL.revokeObjectURL(obj)
        }, 2000)
      },
      s2ab(s) {
        if (typeof ArrayBuffer !== "undefined") {
          const buf = new ArrayBuffer(s.length);
          const view = new Uint8Array(buf);
          for (let i = 0; i !== s.length; ++i) {
            view[i] = s.charCodeAt(i) & 0xff
          }
          return buf;
        } else {
          const buf = new Array(s.length);
          for (let i = 0; i !== s.length; ++i) {
            buf[i] = s.charCodeAt(i) & 0xff;
          }
          return buf;
        }
      },
    },
    watch: {
      // tollList(val){
      //   if(val){
      //     this.$nextTick(() => {
      //         this.$refs.mutipleTable1.doLayout();
      //     });
      //   }
      // },
      // detailList(val){
      //   if(val){
      //     this.$nextTick(() => {
      //         this.$refs.mutipleTable2.doLayout();
      //     });
      //   }
      // }
    },
  };
</script>

<style lang="scss" scoped>
  .page-container {
    padding: 0;
  }

  .el-col {
    ::v-deep .el-range-separator {
      width: 10%;
    }
  }

  .cell {
    span {
      display: inline-block;
      width: 100%;
      text-align: right;
    }
  }

  ::v-deep .el-table__footer-wrapper {
    td:not(:nth-of-type(1)) {
      .cell {
        display: inline-block;
        width: 100%;
        text-align: right;
      }
    }

  }

  #report-container {
    width: 98%;
    height: 96%;
    margin: 10px 10px 0;
  }

  .flex-space-around {
    display: flex;
    justify-content: space-around;
  }

  .flex-space-between {
    display: flex;
    justify-content: space-between;
  }

  .flex-start {
    display: flex;
    justify-content: flex-start;
  }

  .flex-end {
    display: flex;
    justify-content: flex-end;
  }

  .condition-name {
    width: 70px;
    text-align: right;
    vertical-align: center;
    font-size: 70%;
    margin-top: 5px;
  }

  .el-row {
    margin-top: 8px;
  }

  .el-table::before {
    height: 0;
  }

  .el-table::before {
    height: 0;
  }

  ::v-deep .el-table colgroup col[name='gutter'] {
    width: 5px !important
  }

  ::v-deep .el-table__body {
    width: 100% !important
  }

  .flex-container {
    display: flex;
    align-items: center; /* Căn giữa theo chiều dọc */
  }

  .custom-tag {
    max-width: 20px; /* Đặt chiều rộng tối đa, điều chỉnh theo nhu cầu */
  }
</style>
