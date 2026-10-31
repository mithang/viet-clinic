<template>
  <el-row v-loading="loading" class="outpatient">
    <div class="leftInfo">
      <div class="leftTitle">Khám hôm nay</div>
      <div class="searchItemBox">
        <el-select
          v-model="SearchPatientId"
          value-key="id"
          filterable
          clearable
          style="width: calc(100% - 50px)"
          placeholder="Vui lòng chọn bệnh nhân"
        >
          <el-option
            v-for="patientId in AllPatientOption"
            :key="patientId.id"
            :label="patientId.name"
            :value="patientId.id"
          ></el-option>
        </el-select>
        <span style="width: 40px" @click="GetVisitList">Tìm kiếm</span>
      </div>
      <div class="btn">Tiếp nhận khám nhanh</div>
      <el-tabs v-model="TodayActiveName">
        <el-tab-pane
          :label="'Chờ khám(' + PreparePatientTotal + ')'"
          name="prepare"
        >
          <div class="todayVisitBox">
            <div
              class="todayPatientItem"
              @click="GoVisit(item, index)"
              :class="{ click: index === NowPreIndex }"
              v-for="(item, index) in PreparePatientList"
              :key="index"
            >
              <div class="patientInfo">
                <p>
                  {{ SearchPreModel.offset * 5 + index + 1 }}.{{
                    item.patientId.name
                  }}
                  / {{ item.patientInfo.gender.name }} /
                  {{ item.patientInfo.age }}tuổi
                </p>
                <p>
                  {{ item.company.name }} /
                  <span :class="{ isNet: item.source.name === 'Trực tuyến' }">{{
                    item.source.name
                  }}</span>
                </p>
              </div>
              <div class="patientTime">{{ item.time }}</div>
              <div class="patientType">Khám bệnh</div>
            </div>
            <div class="pageBox">
              <el-button type="text" @click="PreparePrePage">Trang trước</el-button>
              <el-button type="text" @click="PrepareNextPage">Trang sau</el-button>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane
          :label="'Đã khám(' + AlreadyPatientListTotal + ')'"
          name="already"
        >
          <div class="todayVisitBox">
            <div
              :class="{ click: index === NowAlreadyIndex }"
              v-for="(item, index) in AlreadyPatientList"
              :key="index"
            >
              <div class="todayPatientItem" @click="GoAlready(item, index)">
                <div class="patientInfo">
                  <p>
                    {{ SearchAlreadyModel.offset * 5 + index + 1 }}.
                    {{ item.patientId.name }} /
                    {{ item.patientInfo.gender.name }} /
                    {{ item.patientInfo.age }}tuổi
                    <span style="position: relative">
                      <div
                        v-if="
                          item.chargeStatus === '0' &&
                          item.status.value === 'registrationStatus_4'
                        "
                        class="refundBox"
                      >
                        Hủy bỏ
                      </div>
                    </span>
                  </p>
                  <p>
                    {{ item.company.name }} /
                    <span class="alreadytype">{{
                      item.status.value === "registrationStatus_4"
                        ? "Đã tiếp nhận"
                        : item.status.name
                    }}</span>
                  </p>
                </div>
                <div class="patientTime">{{ item.time }}</div>
                <div class="patientType">Tái khám</div>
              </div>
              <div
                class="prescription"
                :class="NowAlreadyIndex === index ? 'active' : ''"
              >
                <div class="prescription-box">
                  <p>Đơn thuốc tây</p>
                  <p><span>12312313</span><span>Chưa thu phí</span></p>
                </div>
                <div class="prescription-box">
                  <p>Đơn thuốc đông y</p>
                  <p><span>12312313</span><span>Đã thu phí</span></p>
                </div>
              </div>
            </div>
            <div class="pageBox">
              <el-button type="text" @click="AlreadyPrePage">Trang trước</el-button>
              <el-button type="text" @click="AlreadyNextPage">Trang sau</el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      <el-button class="visitRecordBtn">Hồ sơ khám</el-button>
      <ul class="visitRecordsBox" v-if="VisitRecordsList.length">
        <li
          v-for="(item, index) in VisitRecordsList"
          :key="index"
          @click="OpenVisitRecords(item)"
        >
          <span class="visitRecordTime"
            ><i class="el-icon-timer"></i
            >{{ item.medicalRecord.diagnoseDate }}</span
          >
          Chẩn đoán: <span class="diagnosisTitel">{{
            item.medicalRecord.diagnose
          }}</span>
        </li>
      </ul>
      <el-dialog
        :title="RecordsTitle"
        :visible.sync="IsShowRecords"
        :close-on-click-modal="false"
        width="50%"
        v-loading="RecordsLoading"
      >
        <VisitRecords
          v-if="IsShowRecords"
          :NowVisitInfo="NowVisitInfo"
          :checkedForm="checkedForm"
          :InfusionUseOption="InfusionUseOption"
          :InfusionOption="InfusionOption"
          :DayNumOption="DayNumOption"
          :FrequencyOption="FrequencyOption"
        />
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" v-if="!IsOnlyRead" @click="UseRecord"
            >Gọi</el-button
          >
          <el-button @click="IsShowRecords = false">Đóng</el-button>
        </span>
      </el-dialog>
    </div>
    <!-- Khối thông tin bên phải -->
    <div class="rightInfo">
      <!-- Vùng thông tin phía trên -->
      <div class="topInfoBox">
        <!-- Khối thông tin cơ bản -->
        <div class="infoItem">
          <div class="infoTitleBox">
            <h4>Thông tin cơ bản</h4>
          </div>
          <el-form
            :model="BasicInfoModel"
            ref="BasicInfoForm"
            :rules="BasicInfoFormRules"
            label-width="90px"
            disabled
          >
            <el-row :gutter="24">
              <el-col :span="7">
                <el-form-item label="Loại điều trị" prop="treatType">
                  <el-radio-group v-model="BasicInfoModel.treatType">
                    <el-radio label="初诊">Khám lần đầu</el-radio>
                    <el-radio label="Tái khám">Tái khám</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="Tên bệnh nhân" prop="name">
                  <el-input v-model="BasicInfoModel.name"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="Giới tính" prop="gender">
                  <el-radio-group v-model="BasicInfoModel.gender.name">
                    <el-radio label="男">Nam</el-radio>
                    <el-radio label="女">Nữ</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="Tuổi" prop="age">
                  <el-row>
                    <el-col :span="12">
                      <el-input v-model="BasicInfoModel.age">
                        <span slot="suffix">năm</span>
                      </el-input>
                    </el-col>
                    <el-col :span="12">
                      <el-input v-model="BasicInfoModel.month">
                        <span slot="suffix">tháng</span>
                      </el-input>
                    </el-col>
                  </el-row>
                </el-form-item>
              </el-col>
              <el-col :span="7">
                <el-form-item label="Thông tin liên hệ" prop="phone">
                  <el-input v-model="BasicInfoModel.phone"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="Số CMND/CCCD" prop="card">
                  <el-input v-model="BasicInfoModel.card"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="Quan hệ với bệnh nhân" prop="withPatientNexus">
                  <el-select
                    v-model="BasicInfoModel.withPatientNexus.name"
                    placeholder="Vui lòng chọn"
                  >
                    <el-option label="Bản thân">Bản thân</el-option>
                    <el-option label="Chủ hộ">Chủ hộ</el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="Số thẻ sức khỏe" prop="healthCardNo">
                  <el-input v-model="BasicInfoModel.healthCardNo"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <!-- Khối bệnh án -->
        <div class="infoItem">
          <div class="infoTitleBox">
            <h4>Bệnh án</h4>
            <div class="infoTitleRight" v-if="!IsOnlyRead">
              <el-popover placement="right" width="380" trigger="click">
                <div class="configureBox">
                  <div class="recordTitleBox">
                    <span>Cài đặt bệnh án</span>
                    <el-button type="text" @click="ClearMedicineCofigure"
                      >Khôi phục mặc định</el-button
                    >
                  </div>
                  <div class="recordLabelBox">
                    <h5>Chọn trường bệnh án cần thiết</h5>
                    <el-checkbox-group
                      v-model="ChooseRecordList"
                      class="checkLabelBox"
                    >
                      <el-checkbox
                        class="checkItem"
                        v-for="(item, index) in MedicineRecordLabelList"
                        :key="index"
                        :label="item"
                        >{{ item.label }}</el-checkbox
                      >
                    </el-checkbox-group>
                  </div>
                </div>
              </el-popover>
              <div class="bl-btn">
                <p @click="IsShowRecordFile = true">
                  <i class="el-icon-upload2" />Tải tệp đính kèm lên
                </p>
                <p><i class="el-icon-s-order" />Gọi mẫu</p>
                <p><i class="el-icon-s-order" />Lưu thành mẫu</p>
                <p><i class="el-icon-s-tools" />Cấu hình</p>
              </div>
            </div>
          </div>
          <el-form
            :disabled="IsOnlyRead"
            :model="MedicalRecordModel"
            ref="BasicInfoForm"
            :rules="BasicInfoFormRules"
            label-width="90px"
            size="medium"
          >
            <el-form-item label="Lý do khám chính: " prop="patientTell">
              <el-input v-model="MedicalRecordModel.patientTell"></el-input>
            </el-form-item>
            <el-form-item label="Bệnh sử hiện tại: " prop="nowHistory">
              <el-input v-model="MedicalRecordModel.nowHistory"></el-input>
            </el-form-item>
            <el-form-item label="Tiền sử bệnh: " prop="beforeHistory">
              <el-input v-model="MedicalRecordModel.beforeHistory"></el-input>
            </el-form-item>
            <el-form-item label="Xét nghiệm khác: " prop="otherCheck">
              <el-input v-model="MedicalRecordModel.otherCheck"></el-input>
            </el-form-item>
            <el-form-item label="Chẩn đoán: " prop="diagnose">
              <el-input v-model="MedicalRecordModel.diagnose"></el-input>
            </el-form-item>
            <el-form-item
              v-for="(item, index) in ChooseRecordList"
              :key="index"
              :label="item.label + ': '"
              :prop="item.name"
            >
              <el-input v-model="item.value"></el-input>
            </el-form-item>
          </el-form>
        </div>
        <!-- Khối thêm đơn thuốc -->
        <div v-if="!IsOnlyRead" class="addPrescriptionBox">
          <!-- <h4><span class="titleSign"></span>Thêm đơn thuốc</h4> -->
          <!-- <el-button-group>
            <el-button
              icon="el-icon-plus"
              @click="OpenPrescription('WesternInput')"
              >Đơn thuốc tây</el-button
            >
            <el-button
              icon="el-icon-plus"
              @click="OpenPrescription('ChineseInput')"
              >Đơn thuốc đông y</el-button
            >
            <el-button
              icon="el-icon-plus"
              @click="OpenPrescription('InfusionBtn')"
              >Đơn truyền dịch</el-button
            >
            <el-button
              icon="el-icon-plus"
              @click="OpenPrescription('TreatmentInput')"
              >Hạng mục khám chữa bệnh</el-button
            >
          </el-button-group> -->

          <div
            class="prescription-nav"
            v-for="(i, ind) in addPrescriptionList"
            :key="ind"
            @click="OpenPrescription(i.val)"
          >
            {{ i.name }}
            <span @click.stop="closePrescription(i.val)">X</span>
          </div>
          <el-dropdown trigger="click" @command="changeItem">
            <span class="el-dropdown-link">
              Thêm đơn thuốc<i class="el-icon-plus"></i>
            </span>
            <el-dropdown-menu
              slot="dropdown"
              style="width: 100px; font-size: 16px"
            >
              <el-dropdown-item
                v-for="(i, ind) in prescriptionList"
                :key="ind"
                :command="i"
                style="font-size: 16px"
                >{{ i.name }}</el-dropdown-item
              >
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <!-- Khối đơn thuốc Tây y -->
        <div class="infoItem" v-if="IsShowWestern">
          <el-popover
            placement="right"
            v-if="!IsOnlyRead"
            width="700"
            trigger="focus"
          >
            <el-table
              :data="WesternMedicineTable"
              border
              highlight-current-row
              @row-click="RowClickWesternTable"
            >
              <el-table-column prop="goodsName" label="Tên thuốc">
              </el-table-column>
              <el-table-column prop="gg" label="Quy cách" width="120">
                <template slot-scope="scope">
                  {{ scope.row.dosis }}{{ scope.row.dosisUnit.name }} *
                  {{ scope.row.preparation
                  }}{{ scope.row.preparationUnit.name }}/{{
                    scope.row.pack.name
                  }}
                </template>
              </el-table-column>
              <el-table-column prop="factory.name" label="Nhà sản xuất" width="100">
              </el-table-column>
              <el-table-column label="Đơn vị" width="80">
                <template slot-scope="scope">
                  {{
                    scope.row.isUnpackSell === "1"
                      ? scope.row.preparationUnit.name
                      : scope.row.pack.name
                  }}
                </template>
              </el-table-column>
              <el-table-column label="Giá bán lẻ" width="80">
                <template slot-scope="scope">
                  {{
                    scope.row.isUnpackSell === "1"
                      ? scope.row.retailPrice +
                        "/" +
                        scope.row.preparationUnit.name
                      : scope.row.price + "/" + scope.row.pack.name
                  }}
                </template>
              </el-table-column>
              <el-table-column prop="stockText" label="Tồn kho" width="100">
              </el-table-column>
            </el-table>
            <el-input
              prefix-icon="el-icon-plus"
              suffix-icon="el-icon-search"
              style="width: 30%"
              slot="reference"
              ref="WesternInput"
              v-model="SearchWesternInput"
              @input="GetWesternTable"
              placeholder="Nhập tên thuốc hoặc mã pinyin"
            ></el-input>
          </el-popover>

          <div class="infoTitleBox">
            <h4>Đơn thuốc tây</h4>
          </div>
          <div class="itemContent">
            <el-table
              :data="ChooseWesternMedicineTable"
              border
              style="width: 100%"
              class="tableStyle"
            >
              <el-table-column
                type="index"
                label="Số thứ tự"
                width="50"
                align="center"
              >
              </el-table-column>
              <el-table-column prop="goodsName" label="Tên" align="center">
                <template slot-scope="scope">
                  {{
                    scope.row.goodsName
                      ? scope.row.goodsName
                      : scope.row.drugStuffId.name
                  }}
                  <span style="color: forestgreen">
                    {{
                      scope.row.goodsName
                        ? scope.row.dosis +
                          scope.row.dosisUnit.name +
                          "*" +
                          scope.row.preparation +
                          scope.row.preparationUnit.name +
                          "/" +
                          scope.row.pack.name
                        : scope.row.drugStuffId.drug.dosis +
                          scope.row.drugStuffId.drug.dosisUnit.name +
                          "*" +
                          scope.row.drugStuffId.drug.preparation +
                          scope.row.drugStuffId.drug.preparationUnit.name +
                          "/" +
                          scope.row.drugStuffId.drug.pack.name
                    }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="singleDosage" label="Liều dùng mỗi lần" width="100">
                <template slot-scope="scope">
                  <el-input
                    v-model="scope.row.singleDosage"
                    @input="GetWesternFee(scope.$index, scope.row)"
                    :disabled="IsOnlyRead"
                  >
                    <template slot="append">{{
                      scope.row.preparationUnit
                        ? scope.row.preparationUnit.name
                        : scope.row.drugStuffId.drug.preparationUnit.name
                    }}</template>
                  </el-input>
                </template>
              </el-table-column>
              <el-table-column
                prop="westernMedicineUse"
                label="Cách dùng"
                width="100"
              >
                <template slot-scope="scope">
                  <el-select
                    v-model="scope.row.westernMedicineUse"
                    :disabled="IsOnlyRead"
                    placeholder=""
                  >
                    <el-option
                      v-for="item in WesternUseOption"
                      :key="item.value"
                      :label="item.name"
                      :value="{ name: item.name, value: item.value }"
                    >
                    </el-option>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="frequency" label="Tần suất" width="120">
                <template slot-scope="scope">
                  <el-select
                    v-model="scope.row.frequency"
                    :disabled="IsOnlyRead"
                    @change="GetWesternFee(scope.$index, scope.row)"
                    placeholder=""
                  >
                    <el-option
                      v-for="item in FrequencyOption"
                      :key="item.value"
                      :label="item.name"
                      :value="{ name: item.name, value: item.value }"
                    >
                    </el-option>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="days" label="Số ngày" width="100">
                <template slot-scope="scope">
                  <el-select
                    v-model="scope.row.days"
                    :disabled="IsOnlyRead"
                    @change="GetWesternFee(scope.$index, scope.row)"
                    placeholder=""
                  >
                    <el-option
                      v-for="item in DayNumOption"
                      :key="item.value"
                      :label="item.name"
                      :value="{ name: item.name, value: item.value }"
                    >
                    </el-option>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="total" label="Tổng số lượng" width="100">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.total" disabled>
                    <template slot="append">{{
                      GetWesternUnit(scope.$index, scope.row)
                    }}</template>
                  </el-input>
                </template>
              </el-table-column>
              <el-table-column
                prop="price"
                label="Đơn giá"
                width="100"
                align="center"
              >
                <template slot-scope="scope">{{
                  GetWesternPrice(scope.$index, scope.row) +
                  "/" +
                  GetWesternUnit(scope.$index, scope.row)
                }}</template>
              </el-table-column>
              <el-table-column
                prop="allFee"
                label="Tổng cộng"
                width="100"
                align="center"
              >
                <template slot-scope="scope">
                  <span>{{ scope.row.allFee }}</span>
                </template>
              </el-table-column>
              <el-table-column v-if="!IsOnlyRead" label="Thao tác" align="center">
                <template slot-scope="scope">
                  <i
                    class="el-icon-circle-close"
                    @click="DeleteWestern(scope.$index, scope.row)"
                  ></i>
                </template>
              </el-table-column>
            </el-table>
            <div class="itemBottomBox">
              <p>Tiền thuốc tây:{{ WesternFee }}VNĐ</p>
              <div style="width: 70%">
                Ghi chú:<el-input
                  v-model="WesternRemarks"
                  :disabled="IsOnlyRead"
                  style="width: 90%; margin-left: 10px"
                  placeholder="Vui lòng nhập ghi chú"
                ></el-input>
              </div>
            </div>
          </div>
        </div>
        <!-- Khối đơn thuốc Đông y -->
        <div class="infoItem" v-if="IsShowChinese">
          <div class="infoTitleBox">
            <h4>Đơn thuốc đông y</h4>
            <div class="infoTitleRight"></div>
          </div>
          <div class="itemContent">
            <el-popover
              placement="right"
              v-if="!IsOnlyRead"
              width="700"
              trigger="focus"
            >
              <el-table
                :data="ChineseMedicineTable"
                border
                highlight-current-row
                @row-click="RowClickChineseTable"
              >
                <el-table-column prop="goodsName" label="Tên thuốc">
                </el-table-column>
                <el-table-column prop="factory.name" label="Nhà sản xuất" width="100">
                </el-table-column>
                <el-table-column
                  prop="preparationUnit.name"
                  label="Đơn vị"
                  width="80"
                >
                </el-table-column>
                <el-table-column prop="retailPrice" label="Giá bán lẻ" width="80">
                </el-table-column>
                <el-table-column prop="stockNumber" label="Tồn kho" width="100">
                </el-table-column>
              </el-table>
              <el-input
                prefix-icon="el-icon-plus"
                suffix-icon="el-icon-search"
                style="width: 30%"
                slot="reference"
                ref="ChineseInput"
                v-model="SearchChineseInput"
                @input="GetChineseTable"
                placeholder="Nhập tên thuốc hoặc mã pinyin"
              ></el-input>
            </el-popover>
            <div class="chineseContentCenter">
              <div
                class="chineseContentCenterItem"
                v-for="(item, index) in ChooseChineseMedicineTable"
                :key="index"
              >
                <i
                  class="closeBtn el-icon-close"
                  v-if="!IsOnlyRead"
                  @click="DeleteChinese(index)"
                ></i>
                <ChineseMedicine
                  :IsOnlyRead="IsOnlyRead"
                  :ChineseUseTimeOption="ChineseUseTimeOption"
                  :ChineseMedicine="item"
                  @GetChineseFee="GetChineseFee"
                />
              </div>
            </div>
            <div class="itemBottomBox">
              <p>Số tiền thuốc Đông y: {{ ChinesePrescription.fee }}VNĐ</p>
              <div style="width: 70%">
                <el-input
                  v-model="ChinesePrescription.dosage"
                  :disabled="IsOnlyRead"
                  @input="GetChineseFee"
                  style="width: 10%"
                ></el-input
                >thang Cách dùng: 
                <el-select
                  v-model="ChinesePrescription.recipelUse"
                  :disabled="IsOnlyRead"
                  style="width: 100px"
                  placeholder=""
                >
                  <el-option
                    v-for="item in ChineseUseOption"
                    :key="item.value"
                    :label="item.name"
                    :value="{ name: item.name, value: item.value }"
                  >
                  </el-option>
                </el-select>
                <el-select
                  v-model="ChinesePrescription.frequency"
                  :disabled="IsOnlyRead"
                  style="width: 100px"
                  placeholder=""
                >
                  <el-option
                    v-for="item in ChineseTimeOption"
                    :key="item.value"
                    :label="item.name"
                    :value="{ name: item.name, value: item.value }"
                  >
                  </el-option>
                </el-select>
                <el-select
                  v-model="ChinesePrescription.takeFrequency"
                  :disabled="IsOnlyRead"
                  style="width: 100px"
                  placeholder=""
                >
                  <el-option
                    v-for="item in FrequencyOption"
                    :key="item.value"
                    :label="item.name"
                    :value="{ name: item.name, value: item.value }"
                  >
                  </el-option>
                </el-select>
                một lần<el-input
                  v-model="ChinesePrescription.singleDosage"
                  :disabled="IsOnlyRead"
                  style="width: 10%"
                ></el-input
                >ml
              </div>
            </div>
          </div>
        </div>
        <!-- Khối đơn thuốc truyền dịch -->
        <div class="infoItem" v-if="IsShowInfusion">
          <div class="infoTitleBox">
            <h4>Đơn truyền dịch</h4>
            <div class="infoTitleRight">
              <el-button type="text" v-if="!IsOnlyRead" @click="AddInfusionItem"
                >Thêm một nhóm</el-button
              >
            </div>
          </div>
          <div class="itemContent">
            <div
              class="InfusionContentItem"
              v-for="(item, index) in InfusionItemList"
              :key="index"
            >
              <i
                class="closeBtn el-icon-close"
                v-if="!IsOnlyRead"
                @click="DeleteInfusionItem(index)"
              ></i>
              <InfusionPrescription
                @GetInfusionFee="GetInfusionFee"
                :InfusionUseOption="InfusionUseOption"
                :InfusionOption="InfusionOption"
                :DayNumOption="DayNumOption"
                :FrequencyOption="FrequencyOption"
                :InfoModel="item"
                :IsOnlyRead="IsOnlyRead"
                :GroupNumber="index + 1"
                :key="index"
              />
            </div>
            <div class="itemBottomBox">
              <p>Tiền truyền dịch:{{ InfusionFee }}VNĐ</p>
              <div style="width: 70%">
                Ghi chú:<el-input
                  style="width: 90%; margin-left: 10px"
                  :disabled="IsOnlyRead"
                  v-model="InfusionRemarks"
                  placeholder="Vui lòng nhập ghi chú"
                  ref="InfusionBtn"
                ></el-input>
              </div>
            </div>
          </div>
        </div>
        <!-- Khối hạng mục khám chữa bệnh -->
        <div class="infoItem" v-if="IsShowTreatment">
          <div class="infoTitleBox">
            <h4>Hạng mục khám chữa bệnh</h4>
          </div>
          <div class="itemContent">
            <el-popover
              v-if="!IsOnlyRead"
              placement="right"
              width="400"
              trigger="click"
            >
              <el-table
                :data="TreatmentTable"
                border
                highlight-current-row
                @row-click="RowClickTreatmentTable"
              >
                <el-table-column prop="itemName" label="Tên hạng mục">
                </el-table-column>
                <el-table-column prop="itemType.name" label="Loại hạng mục">
                </el-table-column>
                <el-table-column prop="unit.name" label="Đơn vị">
                </el-table-column>
                <el-table-column prop="salePrice" label="Đơn giá">
                </el-table-column>
              </el-table>
              <el-input
                prefix-icon="el-icon-plus"
                suffix-icon="el-icon-search"
                style="width: 30%"
                slot="reference"
                ref="TreatmentInput"
                v-model="SearchCostItemInput"
                @input="GetCostItemTable"
                placeholder="Nhập tên thuốc hoặc mã pinyin"
              ></el-input>
            </el-popover>

            <el-table
              :data="ChooseTreatmentTable"
              border
              style="width: 100%"
              class="tableStyle"
            >
              <el-table-column
                type="index"
                label="Số thứ tự"
                width="50"
                align="center"
              >
              </el-table-column>
              <el-table-column prop="itemName" label="Tên hạng mục">
                <template slot-scope="scope">
                  {{
                    scope.row.itemName
                      ? scope.row.itemName
                      : scope.row.drugStuffId.name
                  }}
                </template>
              </el-table-column>
              <el-table-column prop="total" label="Số lượng" width="100">
                <template slot-scope="scope">
                  <el-input
                    v-model="scope.row.total"
                    @input="GetCostItemFee(scope.$index, scope.row)"
                  >
                    <template slot="append">{{
                      scope.row.unit
                        ? scope.row.unit.name
                        : scope.row.drugStuffId.dosisUnit.name
                    }}</template>
                  </el-input>
                </template>
              </el-table-column>
              <el-table-column prop="salePrice" label="Đơn giá" width="80">
                <template slot-scope="scope">
                  {{
                    TodayActiveName === "prepare"
                      ? scope.row.salePrice
                      : scope.row.drugStuffId.price
                  }}
                </template>
              </el-table-column>
              <el-table-column
                prop="allFee"
                label="Tổng cộng"
                width="100"
                align="center"
              >
                <template slot-scope="scope">
                  {{ scope.row.allFee }}
                </template>
              </el-table-column>
              <el-table-column v-if="!IsOnlyRead" label="Thao tác" align="center">
                <template slot-scope="scope">
                  <i
                    class="el-icon-circle-close"
                    @click="DeleteTreatment(scope.$index, scope.row)"
                  ></i>
                </template>
              </el-table-column>
            </el-table>
            <div class="itemBottomBox">
              <p>Tiền khám chữa bệnh:{{ CostItemFee }}VNĐ</p>
            </div>
          </div>
        </div>
        <!-- Khối phụ phí -->
        <div class="infoItem">
          <div class="infoTitleBox">
            <h4>Phí phụ thêm</h4>
          </div>
          <div class="itemContent">
            <el-popover
              placement="right"
              v-if="!IsOnlyRead"
              width="400"
              trigger="click"
            >
              <el-table
                :data="SurchargeTable"
                border
                highlight-current-row
                @row-click="RowClickSurchargeTable"
              >
                <el-table-column prop="name" label="Tên chi phí">
                </el-table-column>
                <el-table-column prop="type.name" label="Loại chi phí" width="100">
                </el-table-column>
                <el-table-column prop="kc" label="Tồn kho" width="100">
                </el-table-column>
                <el-table-column prop="priceOutSell" label="Đơn giá" width="80">
                </el-table-column>
              </el-table>
              <el-input
                prefix-icon="el-icon-plus"
                suffix-icon="el-icon-search"
                style="width: 30%"
                slot="reference"
                v-model="SearchSurchargeInput"
                @input="GetSurchargeTable"
                placeholder="Nhập tên thuốc hoặc mã pinyin"
              ></el-input>
            </el-popover>

            <el-table
              :data="ChooseSurchargeTable"
              border
              style="width: 100%"
              class="tableStyle"
            >
              <el-table-column
                type="index"
                label="Số thứ tự"
                width="50"
                align="center"
              >
              </el-table-column>
              <el-table-column prop="name" label="Tên chi phí">
                <template slot-scope="scope">
                  {{
                    scope.row.name ? scope.row.name : scope.row.drugStuffId.name
                  }}
                </template>
              </el-table-column>
              <el-table-column prop="total" label="Số lượng" width="100">
                <template slot-scope="scope">
                  <el-input
                    :disabled="IsOnlyRead"
                    v-model="scope.row.total"
                    @input="GetSurchargeFee(scope.$index, scope.row)"
                  >
                    <template slot="append">suất</template>
                  </el-input>
                </template>
              </el-table-column>
              <el-table-column prop="priceOutSell" label="Đơn giá" width="80">
                <template slot-scope="scope">
                  {{
                    scope.row.priceOutSell
                      ? scope.row.priceOutSell
                      : scope.row.drugStuffId.price
                  }}
                </template>
              </el-table-column>
              <el-table-column
                prop="allFee"
                label="Tổng cộng"
                width="100"
                align="center"
              >
                <template slot-scope="scope">
                  {{ scope.row.allFee }}
                </template>
              </el-table-column>
              <el-table-column label="Xóa" v-if="!IsOnlyRead" align="center">
                <template slot-scope="scope">
                  <i
                    class="el-icon-circle-close"
                    @click="DeleteSurchargeTable(scope.$index, scope.row)"
                  ></i>
                </template>
              </el-table-column>
            </el-table>
            <div class="itemBottomBox">
              <p>Tiền phụ thu:{{ SurchargeFee }}VNĐ</p>
            </div>
          </div>
        </div>
        <el-input v-model="DoctorAdvice" :disabled="IsOnlyRead">
          <template slot="prepend">Hạng mục y lệnh</template>
        </el-input>
      </div>
      <!-- Vùng cố định phía dưới -->
      <div class="bottomInfoBox">
        <h5>Tổng số tiền: {{ AllTotal }}VNĐ</h5>
        <div class="btnGroup" v-if="!IsOnlyRead">
          <el-button class="visitRecordBtn" @click="FinishVisit"
            >Hoàn thành tiếp nhận khám</el-button
          >
          <el-button>Xem trước</el-button>
          <el-button type="text"><i class="el-icon-printer"></i>In</el-button>
        </div>
        <div class="btnGroup" v-else>
          <el-button
            @click="invalidPrescription"
            :disabled="IsInvalidPrescription"
            >Hủy đơn thuốc</el-button
          >
        </div>
      </div>
    </div>
    <el-dialog
      title="Tệp đính kèm bệnh án"
      :visible.sync="IsShowRecordFile"
      width="30%"
      :before-close="handleClose"
    >
      <upload-file
        v-model="fileIdFile"
        titile="Tệp đính kèm bệnh án"
        :action="fileIdFile.action"
        :objectId="fileIdFile.fileId"
      ></upload-file>
    </el-dialog>
    <!--                  Tạo mã                  -->
    <!-- Lịch sử  -->
    <History :bussObject="curentRow"></History>
    <!-- Cửa sổ chỉnh sửa  -->
  </el-row>
</template>

<script>
import { listDictItemAll } from "@/api/sys/dictItem";
import { getDictItemsByCode, DICT_CODE } from "@/utils/dictCache";
import { getFrequencyCount } from "@/utils/dataUtils";
import {
  listRegistrationPage,
  getRegistrationById,
  updateStatus,
} from "@/api/outpatient/registration";
import { getPatientById, listPatientAll } from "@/api/outpatient/patient";
import { listAllStock } from "@/api/stock/drug";
import { listCostItemPage } from "@/api/treatment/costItem";
import { listStuffPage} from "@/api/stock/stuff";
import {
  allSaveMedicalRecord,
  allQueryMedicalRecord,
} from "@/api/outpatient/medicalRecord";
import MedicalRecordForm from "./medicalRecordForm";
import MainUI from "@/views/components/mainUI";
import History from "@/views/components/history";
import ChineseMedicine from "./components/ChineseMedicine";
import InfusionPrescription from "./components/InfusionPrescription.vue";
import VisitRecords from "./components/VisitRecords.vue";
import indexVue from "../../../components/Clipboard/index.vue";
import uploadFile from "@/views/components/uploadFile";
import { getCurrentUser, getCurrentUserId } from "@/utils/userCache";
export default {
  extends: MainUI,
  components: {
    uploadFile,
    MedicalRecordForm,
    History,
    ChineseMedicine,
    InfusionPrescription,
    VisitRecords,
  },
  data() {
    return {
      //Danh sách đơn thuốc
      prescriptionList: [
        {
          name: "Đơn thuốc đông y",
          val: "ChineseInput",
        },
        {
          name: "Đơn thuốc tây",
          val: "WesternInput",
        },
        {
          name: "Đơn truyền dịch",
          val: "InfusionBtn",
        },
        {
          name: "Hạng mục khám chữa bệnh",
          val: "TreatmentInput",
        },
      ],
      // Đơn thuốc được chọn
      addPrescriptionList: [],
      prescriptionIsShow: false,
      IsShowRecordFile: false,
      fileIdFile: {
        fileId: "",
        action: "add",
        deletes: [], // Chờ xóa (Đã tải lên)danh sách tệp
        uploads: [], // Danh sách tệp chờ tải lên
      },
      //Tất cả bệnh nhân
      AllPatientOption: [],
      //Id lượt khám hiện tại
      VisitId: "",
      //Chỉ số hiện tại của bệnh nhân chờ khám
      NowPreIndex: 5,
      //Chỉ số hiện tại của bệnh nhân đã khám
      NowAlreadyIndex: 5,
      //Có chỉ đọc không
      IsOnlyRead: false,
      //Y lệnh
      DoctorAdvice: "",
      //Điều kiện tìm kiếm khám bệnh
      SearchPreModel: {
        columnName: "",
        limit: 5,
        offset: 0,
        order: "",
      },
      SearchAlreadyModel: {
        columnName: "",
        limit: 5,
        offset: 0,
        order: "",
      },
      //Điều kiện tìm thuốc
      SearchWesternModel: {
        columnName: "",
        limit: 10,
        offset: 0,
        order: "",
      },
      SearchChineseModel: {
        columnName: "",
        limit: 10,
        offset: 0,
        order: "",
      },
      //Tên bệnh nhân
      SearchPatientId: "",
      //Tìm thuốc Tây y
      SearchWesternInput: "",
      //Tìm thuốc Đông y
      SearchChineseInput: "",
      //Tìm hạng mục khám chữa bệnh
      SearchCostItemInput: "",
      //Tìm phí phụ thêm
      SearchSurchargeInput: "",
      //Ghi chú thuốc Tây y
      WesternRemarks: "",
      //Ghi chú truyền dịch
      InfusionRemarks: "",
      //Tổng giá thuốc Tây y
      WesternFee: 0,
      //Tổng giá truyền dịch
      InfusionFee: 0,
      //Tổng giá hạng mục khám chữa bệnh
      CostItemFee: 0,
      //Tổng giá phụ phí
      SurchargeFee: 0,
      //Đơn thuốc đông y
      ChinesePrescription: this.ReturnChineseModel(),
      //Trường cấu hình bệnh án
      MedicineRecordLabelList: [
        {
          label: "Tiền sử cá nhân",
          name: "personalHistory",
          value: "",
        },
        {
          label: "Tiền sử dị ứng",
          name: "allergyHistory",
          value: "",
        },
        {
          label: "Tiền sử bệnh",
          name: "diseaseHistory",
          value: "",
        },
        {
          label: "Tiền sử bệnh truyền nhiễm",
          name: "infectiousHistory",
          value: "",
        },
        {
          label: "Tiền sử phẫu thuật",
          name: "surgeryHistory",
          value: "",
        },
        {
          label: "Tiền sử truyền máu",
          name: "transfusionHistory",
          value: "",
        },
        {
          label: "Khám thực thể",
          name: "physiqueCheck",
          value: "",
        },
        {
          label: "Chẩn đoán cấp cứu",
          name: "emergencyDiagnose",
          value: "",
        },
        {
          label: "Hiệu quả cấp cứu",
          name: "emergencyEffect",
          value: "",
        },
        {
          label: "Tiền sử gia đình",
          name: "familyHistory",
          value: "",
        },
        {
          label: "Tiền sử kinh nguyệt",
          name: "lunariaHistory",
          value: "",
        },
        {
          label: "Tiền sử hôn nhân/sinh đẻ",
          name: "pregnancyHistory",
          value: "",
        },
        {
          label: "Xét nghiệm bổ sung",
          name: "assistCheck",
          value: "",
        },
      ],
      //Cấu hình bệnh án đang chọn
      ChooseRecordList: [],
      //Tiêu đề hộp thoại lịch sử khám
      RecordsTitle: "Chi tiết lượt khám",
      //Có hiển thị hộp thoại lịch sử khám không
      IsShowRecords: false,
      //Trạng thái tải hộp thoại lịch sử khám
      RecordsLoading: false,
      //Có hiển thị đơn thuốc Tây y-Đơn thuốc đông y-Đơn truyền dịch-Hạng mục khám chữa bệnh
      IsShowWestern: false,
      IsShowChinese: false,
      IsShowInfusion: false,
      IsShowTreatment: false,
      //tabsTrạng thái mặc định prepare:Chờ khám already:Đã khám
      TodayActiveName: "prepare",
      //Danh sách bệnh nhân chờ khám hôm nay
      PreparePatientList: [],
      //Tổng số bệnh nhân chờ khám hôm nay
      PreparePatientTotal: 0,
      //Danh sách bệnh nhân đã khám hôm nay
      AlreadyPatientList: [],
      //Tổng số bệnh nhân đã khám hôm nay
      AlreadyPatientListTotal: 0,
      //Danh sách lịch sử khám
      VisitRecordsList: [],
      //Thông tin lịch sử khám hiện tại
      NowVisitInfo: {},
      checkedForm: {
        record: false,
        Prescription: false,
      },
      //Thông tin cơ bản
      BasicInfoModel: {
        treatType: "",
        name: "",
        gender: "",
        age: "",
        month: "",
        phone: "",
        card: "",
        withPatientNexus: {
          name: "",
        },
        healthCardNo: "",
      },
      //Biểu mẫu bệnh án
      MedicalRecordModel: this.ReturnMedicalModel(),
      InfusionUseOption: [],
      InfusionOption: [],
      //Dữ liệu bảng đơn thuốc Tây y
      WesternMedicineTable: [],
      //Đơn thuốc Tây y đã chọn
      ChooseWesternMedicineTable: [],
      //Dữ liệu bảng đơn thuốc Đông y
      ChineseMedicineTable: [],
      //Danh sách thả xuống cách dùng thuốc Tây y
      WesternUseOption: [],
      ChineseUseTimeOption: [],
      //Danh sách thả xuống tần suất thuốc Tây y
      FrequencyOption: [],
      //Danh sách thả xuống số ngày thuốc Tây y
      DayNumOption: [],
      //Đơn thuốc Đông y đã chọn
      ChooseChineseMedicineTable: [],
      //Danh sách thả xuống cách dùng thuốc Đông y
      ChineseUseOption: [],
      ChineseTimeOption: [],
      //Bảng nhóm đơn thuốc truyền dịch
      InfusionItemList: [],
      //Bảng hạng mục khám chữa bệnh
      TreatmentTable: [
        {
          BrandName: "Xét nghiệm máu thường quy",
          dj: "3",
        },
        {
          BrandName: "Khám sức khỏe tổng quát",
          dj: "2",
        },
      ],
      //Bảng hạng mục khám chữa bệnh đã chọn
      ChooseTreatmentTable: [],
      //Dữ liệu bảng phí phụ thêm có thể chọn
      SurchargeTable: [],
      //Dữ liệu bảng phụ phí đã chọn
      ChooseSurchargeTable: [],
      //Xác thực biểu mẫu bệnh nhân
      BasicInfoFormRules: {
        treatType: [
          { required: true, message: "Vui lòng chọn loại điều trị", trigger: "blur" },
        ],
        name: [{ required: true, message: "Vui lòng nhập tên bệnh nhân", trigger: "blur" }],
        gender: [{ required: true, message: "Vui lòng chọn giới tính", trigger: "blur" }],
        age: [{ required: true, message: "Vui lòng nhập tuổi", trigger: "blur" }],
        phone: [{ required: true, message: "Vui lòng nhập thông tin liên hệ", trigger: "blur" }],
        patientTell: [
          { required: true, message: "Vui lòng nhập lý do khám chính", trigger: "blur" },
        ],
        diagnose: [{ required: true, message: "Vui lòng nhập chẩn đoán", trigger: "blur" }],
      },
      tableId: "1007027220819460134",
      schemeId: "1007027220819460165",
      //Có cho phép huỷ đơn thuốc không
      IsInvalidPrescription: false,
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
    UserId() {
      return getCurrentUserId();
    },
    AllTotal() {
      return (
        this.WesternFee +
        this.ChinesePrescription.fee +
        this.InfusionFee +
        this.CostItemFee +
        this.SurchargeFee
      );
    },
  },
  watch: {
    InfusionItemList() {
      this.InfusionItemList.map((item, index) => {
        item.infuseGroup = index + 1;
      });
    },
    ChooseChineseMedicineTable() {
      if (!this.IsOnlyRead) this.GetChineseFee(1);
    },
  },
  methods: {
    // Chọn đơn thuốc
    changeItem(val) {
      const list = [...this.addPrescriptionList, val];
      const listString = list.map((i) => JSON.stringify(i));
      const addPrescriptionList = Array.from(new Set(listString));
      this.addPrescriptionList = addPrescriptionList.map((i) => JSON.parse(i));

      this.OpenPrescription(val.val);
    },
    Init() {
      this.GetAllPatient();
      this.GetAllOption();
      this.GetMedicalRecordAll();
      this.GetVisitList();
      this.GetCostItemTable();
      this.GetSurchargeTable();
    },
    //Lấy tất cả bệnh nhân
    GetAllPatient() {
      let searchModel = {
        params: [
          {
            columnName: "company_id",
            queryType: "=",
            value: this.Company.id,
          },
        ],
      };
      listPatientAll(searchModel).then((responseData) => {
        if (responseData.code === 100) {
          this.AllPatientOption = responseData.data;
        }
      });
    },
    GetVisitList() {
      this.GetPreparePatientList();
      this.GetAlreadyPatientList();
    },
    //Lấy danh sách bệnh nhân chờ khám
    GetPreparePatientList() {
      let params = [
        {
          columnName: "company_id",
          queryType: "=",
          value: this.Company.id,
        },
        {
          columnName: "status",
          queryType: "=",
          value: "",
        },
        {
          columnName: "patient_id",
          queryType: "=",
          value: this.SearchPatientId,
        },
        {
          columnName: "doctor_id",
          queryType: "=",
          value: this.UserId,
        },
      ];
      this.SearchPreModel.params = params;
      this.SearchPreModel.params[1].value = "registrationStatus_0";
      let model = { age: "", gender: "" };
      listRegistrationPage(this.SearchPreModel).then((responseData) => {
        if (responseData.code === 100) {
          this.PreparePatientTotal = responseData.data.total;
          this.PreparePatientList = responseData.data.rows;
          if (this.PreparePatientTotal === 0) return;
          this.PreparePatientList.map((item, index) => {
            item.patientInfo = model;
            this.GetPatientById(item.patientId.id, index, "pre");
          });
          this.$forceUpdate();
        }
      });
    },
    //Lấy danh sách bệnh nhân đã khám
    GetAlreadyPatientList() {
      let params = [
        {
          columnName: "company_id",
          queryType: "=",
          value: this.Company.id,
        },
        {
          columnName: "status",
          queryType: "in",
          value: ["registrationStatus_1", "registrationStatus_4"],
        },
        {
          columnName: "patient_id",
          queryType: "=",
          value: this.SearchPatientId,
        },
        {
          columnName: "doctor_id",
          queryType: "=",
          value: this.UserId,
        },
      ];
      this.SearchAlreadyModel.params = params;
      //this.SearchAlreadyModel.params[1].value = "registrationStatus_1";
      let model = { age: "", gender: "" };
      listRegistrationPage(this.SearchAlreadyModel).then((responseData) => {
        if (responseData.code === 100) {
          this.AlreadyPatientListTotal = responseData.data.total;
          this.AlreadyPatientList = responseData.data.rows;
          if (this.AlreadyPatientListTotal === 0) return;
          this.AlreadyPatientList.map((item, index) => {
            item.patientInfo = model;
            this.GetPatientById(item.patientId.id, index, "already");
          });
        }
      });
    },
    //Lấy thông tin cá nhân của bệnh nhân đang khám
    GetPatientById(id, index, type) {
      this.AllPatientOption.map((item) => {
        if (item.id === id) {
          if (type === "pre") {
            this.PreparePatientList[index].patientInfo = item;
          } else {
            this.AlreadyPatientList[index].patientInfo = item;
          }
        }
      });
    },
    //Lấy giá trị từ điển dữ liệu
    GetAllOption() {
      getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_RECIPEL_USE).then((data) => { this.ChineseUseOption = data; });
      getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_RECIPEL_TAKE_FREQUENCY).then((data) => { this.FrequencyOption = data; });
      getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_RECIPEL_FREQUENCY).then((data) => { this.ChineseTimeOption = data; });
      getDictItemsByCode(DICT_CODE.WESTERN_MEDICINE_USE).then((data) => { this.WesternUseOption = data; });
      getDictItemsByCode(DICT_CODE.RECIPEL_DETAIL_DAYS).then((data) => { this.DayNumOption = data; });
      getDictItemsByCode(DICT_CODE.INFUSE_USE).then((data) => { this.InfusionUseOption = data; });
      getDictItemsByCode(DICT_CODE.SKIN_TEST).then((data) => { this.InfusionOption = data; });
      getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_USE).then((data) => { this.ChineseUseTimeOption = data; });
    },
    GetClearData() {
      this.VisitId = "";
      this.MedicalRecordModel = this.ReturnMedicalModel();
      this.ChooseWesternMedicineTable = [];
      this.WesternFee = 0;
      this.WesternRemarks = "";
      this.ChooseChineseMedicineTable = [];
      this.ChinesePrescription = this.ReturnChineseModel();
      this.InfusionItemList = [];
      this.InfusionFee = 0;
      this.InfusionRemarks = "";
      this.ChooseTreatmentTable = [];
      this.CostItemFee = 0;
      this.ChooseSurchargeTable = [];
      this.SurchargeFee = 0;
      this.DoctorAdvice = "";
    },
    ReturnMedicalModel() {
      return {
        patientTell: "",
        nowHistory: "",
        beforeHistory: "",
        diagnose: "",
        otherCheck: "",
        personalHistory: "",
        allergyHistory: "",
        diseaseHistory: "",
        infectiousHistory: "",
        surgeryHistory: "",
        transfusionHistory: "",
        physiqueCheck: "",
        emergencyDiagnose: "",
        emergencyEffect: "",
        familyHistory: "",
        lunariaHistory: "",
        pregnancyHistory: "",
        assistCheck: "",
      };
    },
    ReturnChineseModel() {
      return {
        recipelType: { name: "Đơn thuốc đông y", value: "recipelType_1" },
        isPay: "0",
        dosage: 0,
        recipelUse: {},
        frequency: {},
        takeFrequency: {},
        singleDosage: "",
        fee: 0,
      };
    },
    //Chuyển trang bệnh nhân chờ khám
    PreparePrePage() {
      if (this.SearchPreModel.offset === 0) return;
      this.SearchPreModel.offset -= 1;
      this.GetPreparePatientList();
    },
    PrepareNextPage() {
      this.SearchPreModel.limit / this.PreparePatientTotal;
      if (
        this.SearchPreModel.offset + 1 >=
        this.PreparePatientTotal / this.SearchPreModel.limit
      )
        return;
      this.SearchPreModel.offset += 1;
      this.GetPreparePatientList();
    },
    //Chuyển trang bệnh nhân đã khám
    AlreadyPrePage() {
      if (this.SearchAlreadyModel.offset === 0) return;
      this.SearchAlreadyModel.offset -= 1;
      this.GetAlreadyPatientList();
    },
    AlreadyNextPage() {
      if (
        this.SearchAlreadyModel.offset + 1 >=
        this.AlreadyPatientListTotal / this.SearchAlreadyModel.limit
      )
        return;
      this.SearchAlreadyModel.offset += 1;
      this.GetAlreadyPatientList();
    },
    //Lấy tổng giá đơn thuốc
    GetChineseFee(type) {
      if (type !== 1 && !this.ChinesePrescription.dosage) return;
      this.ChinesePrescription.fee = 0;
      this.ChooseChineseMedicineTable.map((item) => {
        this.ChinesePrescription.fee += item.allFee;
      });
      this.ChinesePrescription.fee =
        this.ChinesePrescription.fee * this.ChinesePrescription.dosage;
    },
    //Lấy đơn vị theo bán lẻ hay thêm mới/xem
    GetWesternUnit(index, row) {
      if (row.preparationUnit) {
        if (row.isUnpackSell === "1") {
          return row.preparationUnit.name;
        } else {
          return row.pack.name;
        }
      } else {
        if (row.drugStuffId.drug.isUnpackSell === "1") {
          return row.drugStuffId.drug.preparationUnit.name;
        } else {
          return row.drugStuffId.drug.pack.name;
        }
      }
    },
    //Lấy giá theo bán lẻ hay thêm mới/xem
    GetWesternPrice(index, row) {
      if (row.preparationUnit) {
        if (row.isUnpackSell === "1") {
          return row.retailPrice;
        } else {
          return row.price;
        }
      } else {
        if (row.drugStuffId.drug.isUnpackSell === "1") {
          return row.drugStuffId.drug.retailPrice;
        } else {
          return row.drugStuffId.drug.price;
        }
      }
    },
    //Tính tổng lượng và giá theo bán lẻ hay không
    GetWesternFee(index, row) {
      if (row) {
        if (row.frequency && row.days) {
          row.singleDosage = row.singleDosage ? row.singleDosage : 0;
          if (row.isUnpackSell === "1") {
            row.total = Math.ceil(
              row.singleDosage *
                getFrequencyCount(row.frequency.value) *
                row.days.name
            );
            row.allFee =
              row.total *
              (row.retailPrice
                ? row.retailPrice
                : row.drugStuffId.drug.retailPrice);
          } else {
            let total = Math.ceil(
              row.singleDosage *
                getFrequencyCount(row.frequency.value) *
                row.days.name
            );
            row.total = Math.ceil(total / row.preparation);
            row.allFee =
              row.total * (row.price ? row.price : row.drugStuffId.drug.price);
          }
        }
      }
      this.WesternFee = 0;
      this.$nextTick(() => {
        this.ChooseWesternMedicineTable.map((item) => {
          this.WesternFee += item.allFee;
        });
      });
    },
    GetInfusionFee() {
      this.InfusionFee = 0;
      this.$nextTick(() => {
        this.InfusionItemList.map((item) => {
          item.infusionList.map((row) => {
            this.InfusionFee += row.allFee;
          });
        });
      });
    },
    GetCostItemFee(index, row) {
      if (row)
        row.allFee =
          row.total * (row.salePrice ? row.salePrice : row.drugStuffId.price);
      this.CostItemFee = 0;
      this.$nextTick(() => {
        this.ChooseTreatmentTable.map((item) => {
          this.CostItemFee += item.allFee;
        });
      });
    },
    GetSurchargeFee(index, row) {
      if (row)
        row.allFee =
          row.total *
          (row.priceOutSell ? row.priceOutSell : row.drugStuffId.price);
      this.SurchargeFee = 0;
      this.$nextTick(() => {
        this.ChooseSurchargeTable.map((item) => {
          this.SurchargeFee += item.allFee;
        });
      });
    },
    //Lấy dữ liệu bảng đơn thuốc
    GetSurchargeTable() {
      let SearchModel = {
        columnName: "",
        limit: 10,
        offset: 0,
        order: "",
        params: [
          {
            columnName: "company_id",
            queryType: "=",
            value: this.Company.id,
          },
          {
            columnName: "name",
            queryType: "like",
            value: this.SearchSurchargeInput,
          },
          {
            columnName: "is_out_sell",
            queryType: "=",
            value: "1",
          },
        ],
      };
      listStuffPage(SearchModel).then((responseData) => {
        if (responseData.code === 100) {
          this.SurchargeTable = responseData.data.rows;
        }
      });
    },
    GetCostItemTable() {
      let SearchModel = {
        columnName: "",
        limit: 10,
        offset: 0,
        order: "",
        params: [
          {
            columnName: "company_id",
            queryType: "=",
            value: this.Company.id,
          },
          {
            columnName: "item_name",
            queryType: "like",
            value: this.SearchCostItemInput,
          },
          {
            columnName: "item_type",
            queryType: "<>",
            value: "treatmentItemType_4",
          },
        ],
      };
      listCostItemPage(SearchModel).then((responseData) => {
        if (responseData.code === 100) {
          this.TreatmentTable = responseData.data.rows;
        }
      });
    },
    GetMedicalRecordAll() {
      let params = [
        {
          columnName: "company_id",
          queryType: "=",
          value: this.Company.id,
        },
        {
          columnName: "type",
          queryType: "=",
          value: "",
        },
        {
          columnName: "goods_name",
          queryType: "like",
          value: "",
        },
      ];
      this.SearchWesternModel.params = JSON.parse(JSON.stringify(params));
      this.SearchChineseModel.params = JSON.parse(JSON.stringify(params));
      this.GetWesternTable();
      this.GetChineseTable();
    },
    GetWesternTable() {
      this.SearchWesternModel.params[1].value = "medicalType_0";
      this.SearchWesternModel.params[2].value = this.SearchWesternInput;
      listAllStock(this.SearchWesternModel).then((responseData) => {
        if (responseData.code === 100 && Array.isArray(responseData.data)) {
          responseData.data.forEach((element) => {
            let isUnpackSell = element.isUnpackSell; //Cho phép bán lẻ tách hộp
            let stockNumber = element.stockNumber; //Số lượng tồn kho
            let packName = element.pack.name; //Đóng gói (Đơn vị)
            let preparation = element.preparation; //Chế phẩm
            let preparationUnit = element.preparationUnit.name; //Đơn vị bào chế
            let price = element.price; //Giá bán
            let retailPrice = element.retailPrice; //Giá bán lẻ sau khi tách hộp

            let stockText = "";
            if (isUnpackSell === "1") {
              //Làm tròn xuống
              let p1 = Math.floor(stockNumber / preparation);
              //Lấy phần dư
              let p2 = stockNumber % preparation;
              if (p1 > 0) {
                stockText = p1 + packName;
              }
              if (p2 > 0) {
                if (p1 > 0) {
                  stockText = stockText + p2 + preparationUnit;
                } else {
                  stockText = p2 + preparationUnit;
                }
              }
            } else {
              stockText = stockNumber + packName;
            }

            element["stockText"] = stockText;
          });
          this.WesternMedicineTable = responseData.data;
        }
      });
    },
    GetChineseTable() {
      this.SearchChineseModel.params[1].value = "medicalType_1";
      this.SearchChineseModel.params[2].value = this.SearchChineseInput;
      listAllStock(this.SearchChineseModel).then((responseData) => {
        if (responseData.code === 100) {
          this.ChineseMedicineTable = responseData.data;
        }
      });
    },
    //Click bệnh nhân chờ khám để bắt đầu khám
    GoVisit(item, index) {
      this.GetClearData();
      this.ClearMedicineCofigure();
      // this.prescriptionIsShow=!this.prescriptionIsShow
      this.IsShowWestern = false;
      this.IsShowChinese = false;
      this.IsShowInfusion = false;
      this.IsShowTreatment = false;
      this.NowAlreadyIndex = 5;
      this.NowPreIndex = index;
      this.BasicInfoModel = item.patientInfo;
      this.BasicInfoModel.treatType = item.treatType.name;
      this.VisitId = item.id;
      this.IsOnlyRead = false;
      this.GetSaveInfo();
    },
    GoAlready(item, index) {
      this.IsShowWestern = false;
      this.IsShowChinese = false;
      this.IsShowInfusion = false;
      this.IsShowTreatment = false;
      this.NowPreIndex = 5;
      this.NowAlreadyIndex = index;
      this.BasicInfoModel = item.patientInfo;
      this.BasicInfoModel.treatType = item.treatType.name;
      this.VisitId = item.id;
      this.IsOnlyRead = true;
      this.IsInvalidPrescription = !(
        item.status.value === "registrationStatus_1" &&
        item.chargeStatus === "0"
      );

      this.GetSaveInfo();
    },
    //Theo người khámidTruy vấn bệnh án đơn thuốc
    GetSaveInfo() {
      this.VisitRecordsList = [];
      let model = {
        company: {
          id: this.Company.id,
        },
        doctor: {
          id: this.UserId,
        },
        id: this.VisitId,
        registrationId:''
      };
      allQueryMedicalRecord(this.VisitId).then((responseData) => {
        if (responseData.code === 100) {
          this.GetVisitRecordData(responseData.data);
          if (this.IsOnlyRead === true) this.GetOnlyReadData(responseData.data);
        }
      });
    },
    GetVisitRecordData(allData) {
      this.VisitRecordsList[0] = allData;
      this.VisitRecordsList[0].info = this.BasicInfoModel;
      this.$forceUpdate();
    },
    //Gọi bệnh án lịch sử
    UseRecord() {
      let NeedData = JSON.parse(JSON.stringify(this.NowVisitInfo));
      if (!this.checkedForm.record) NeedData.medicalRecord = {};
      if (!this.checkedForm.Prescription) NeedData.recipelInfoEvtList = [];
      this.GetOnlyReadData(NeedData);
      this.IsShowRecords = false;
    },
    //Lấy dữ liệu chỉ đọc
    GetOnlyReadData(allData) {
      this.ClearMedicineCofigure();
      //Lấy bệnh án và y lệnh
      this.DoctorAdvice = allData.medicalRecord.doctorAdvice;
      this.MedicalRecordModel.patientTell = allData.medicalRecord.patientTell;
      this.MedicalRecordModel.nowHistory = allData.medicalRecord.nowHistory;
      this.MedicalRecordModel.beforeHistory =
        allData.medicalRecord.beforeHistory;
      this.MedicalRecordModel.otherCheck = allData.medicalRecord.otherCheck;
      this.MedicalRecordModel.diagnose = allData.medicalRecord.diagnose;
      this.MedicineRecordLabelList.map((item) => {
        item.value = allData.medicalRecord[`${item.name}`];
        if (item.value) this.ChooseRecordList.push(item);
      });
      //Lấy đơn thuốc
      allData.recipelInfoEvtList.map((prescriptionItem) => {
        switch (prescriptionItem.recipelInfo.recipelType.name) {
          case "Đơn thuốc tây":
            this.IsShowWestern = true;
            this.WesternRemarks = prescriptionItem.recipelInfo.remarks;
            this.WesternFee = prescriptionItem.recipelInfo.fee;
            this.ChooseWesternMedicineTable =
              prescriptionItem.recipelDetailEvtList;
            break;
          case "Đơn thuốc đông y":
            this.IsShowChinese = true;
            this.ChooseChineseMedicineTable =
              prescriptionItem.recipelDetailEvtList;
            this.ChinesePrescription = {
              recipelType: prescriptionItem.recipelInfo.recipelType,
              dosage: prescriptionItem.recipelInfo.dosage,
              recipelUse: prescriptionItem.recipelInfo.recipelUse,
              frequency: prescriptionItem.recipelInfo.frequency,
              takeFrequency: prescriptionItem.recipelInfo.takeFrequency,
              singleDosage: prescriptionItem.recipelInfo.singleDosage,
              fee: prescriptionItem.recipelInfo.fee,
            };

            break;
          case "Đơn truyền dịch":
            this.IsShowInfusion = true;
            this.InfusionItemList = [];
            let GroupArr = [];
            prescriptionItem.recipelDetailEvtList.map((item) => {
              GroupArr.push(item.infuseGroup);
            });
            GroupArr = [...new Set(GroupArr)].sort();
            GroupArr.map((group) => {
              let InfoModel = {
                infuseUse: {},
                frequency: {},
                days: {},
                drippingSpeed: "",
                infuseGroup: group,
                infusionList: [],
              };
              this.InfusionItemList.push(JSON.parse(JSON.stringify(InfoModel)));
              prescriptionItem.recipelDetailEvtList.map((item) => {
                if (group === item.infuseGroup) {
                  this.InfusionItemList[group - 1].infuseUse = item.infuseUse;
                  this.InfusionItemList[group - 1].frequency = item.frequency;
                  this.InfusionItemList[group - 1].days = item.days;
                  this.InfusionItemList[group - 1].drippingSpeed =
                    item.drippingSpeed;
                  this.InfusionItemList[group - 1].infuseGroup = item.group;
                  this.InfusionItemList[group - 1].infusionList.push(item);
                }
              });
            });
            this.InfusionFee = prescriptionItem.recipelInfo.fee;
            this.InfusionRemarks = prescriptionItem.recipelInfo.remarks;
            break;
          case "Hạng mục khám chữa bệnh":
            this.IsShowTreatment = true;
            this.CostItemFee = prescriptionItem.recipelInfo.fee;
            this.ChooseTreatmentTable = prescriptionItem.recipelDetailEvtList;
            break;
          case "Phí phụ thêm":
            this.SurchargeFee = prescriptionItem.recipelInfo.fee;
            this.ChooseSurchargeTable = prescriptionItem.recipelDetailEvtList;
            break;
          default:
            break;
        }
      });
    },
    //Xoá đơn thuốc đã thêm
    closePrescription(val) {
      const list = this.addPrescriptionList.filter((i) => {
        return i.val !== val;
      });
      this.addPrescriptionList = list;
      if (list.length !== 0) {
        if (val === "WesternInput") {
          this.IsShowWestern = false;
          this.ChooseWesternMedicineTable = [];
        }
        if (val === "ChineseInput") {
          this.IsShowChinese = false;
          this.ChooseChineseMedicineTable = [];
        }
        if (val === "InfusionBtn") {
          this.IsShowInfusion = false;
          this.InfusionItemList = [];
        }
        if (val === "TreatmentInput") {
          this.IsShowTreatment = false;
          this.ChooseTreatmentTable = [];
        }
        if (list[list.length - 1].val === "WesternInput")
          this.IsShowWestern = true;
        if (list[list.length - 1].val === "ChineseInput")
          this.IsShowChinese = true;
        if (list[list.length - 1].val === "InfusionBtn")
          this.IsShowInfusion = true;
        if (list[list.length - 1].val === "TreatmentInput")
          this.IsShowTreatment = true;
      } else {
        // Xoá trắng đơn thuốc
        this.IsShowWestern = false;
        this.IsShowChinese = false;
        this.IsShowInfusion = false;
        this.IsShowTreatment = false;
        this.ChooseWesternMedicineTable = [];
        this.ChooseChineseMedicineTable = [];
        this.InfusionItemList = [];
        this.ChooseTreatmentTable = [];
      }
    },
    //Thêm đơn thuốc và focus
    OpenPrescription(name) {
      //Đóng tất cả trước rồi hiển thị mục được click
      this.IsShowWestern = false;
      this.IsShowChinese = false;
      this.IsShowInfusion = false;
      this.IsShowTreatment = false;
      switch (name) {
        case "WesternInput":
          this.IsShowWestern = true;
          this.$nextTick(() => {
            if (this.IsShowWestern) this.$refs.WesternInput.focus();
          });
          break;
        case "ChineseInput":
          this.IsShowChinese = true;
          this.$nextTick(() => {
            if (this.IsShowChinese) this.$refs.ChineseInput.focus();
          });
          break;
        case "InfusionBtn":
          this.IsShowInfusion = true;
          this.$nextTick(() => {
            if (this.IsShowInfusion) this.$refs.InfusionBtn.focus();
          });
          break;
        case "TreatmentInput":
          this.IsShowTreatment = true;
          this.$nextTick(() => {
            if (this.IsShowTreatment) this.$refs.TreatmentInput.focus();
          });
          break;
        default:
          break;
      }
    },
    //Cấu hình bệnh án khôi phục mặc định
    ClearMedicineCofigure() {
      this.ChooseRecordList.map((item) => {
        item.value = "";
      });
      this.ChooseRecordList = [];
    },
    //Mở hộp thoại lịch sử khám
    OpenVisitRecords(row) {
      this.IsShowRecords = true;
      this.NowVisitInfo = row;
    },
    //Nhấn dòng bảng thuốc Tây y để chọn và thêm vào bảng đơn thuốc Tây y đã chọn
    RowClickWesternTable(row) {
      this.ChooseWesternMedicineTable.push({
        allFee: 0,
        ...JSON.parse(JSON.stringify(row)),
      });
    },
    //Nhấn nút xóa sau bảng thuốc Tây y để xóa dữ liệu có chỉ số chỉ định
    DeleteWestern(index, row) {
      this.ChooseWesternMedicineTable.splice(index, 1);
      this.GetWesternFee();
    },
    //Click dòng bảng Đông y để thêm vào bảng đơn thuốc Đông y đã chọn
    RowClickChineseTable(row) {
      let flag = false;
      this.ChooseChineseMedicineTable.forEach((element) => {
        if (element.code === row.code) {
          flag = true;
        }
      });

      if (!flag) {
        this.ChooseChineseMedicineTable.push({
          allFee: 0,
          ...JSON.parse(JSON.stringify(row)),
        });
      }
    },
    //Click nút đóng của đơn thuốc Đông y để xoá đơn thuốc này
    DeleteChinese(index) {
      this.ChooseChineseMedicineTable.splice(index, 1);
    },
    //Thêm một nhóm đơn truyền dịch
    AddInfusionItem() {
      let InfoModel = {
        infuseUse: {},
        frequency: {},
        days: {},
        drippingSpeed: "",
        infuseGroup: 0,
        infusionList: [],
      };
      this.InfusionItemList.push(JSON.parse(JSON.stringify(InfoModel)));
    },
    //Xoá một nhóm trong đơn thuốc truyền dịch
    DeleteInfusionItem(index) {
      this.InfusionItemList.splice(index, 1);
      this.GetInfusionFee();
    },
    //Click dòng bảng hạng mục khám chữa bệnh để thêm vào bảng đã chọn
    RowClickTreatmentTable(row) {
      let flag = false;
      this.ChooseTreatmentTable.forEach((element) => {
        if (element.itemCode === row.itemCode) {
          flag = true;
        }
      });

      if (!flag) {
        this.ChooseTreatmentTable.push({
          allFee: 0,
          ...JSON.parse(JSON.stringify(row)),
        });
      }
    },
    //Click nút xoá sau bảng hạng mục khám chữa bệnh để xoá dữ liệu tại chỉ số chỉ định
    DeleteTreatment(index, row) {
      this.ChooseTreatmentTable.splice(index, 1);
      this.GetCostItemFee();
    },
    //Thêm dữ liệu bảng phụ phí
    RowClickSurchargeTable(row) {
      let flag = false;
      this.ChooseSurchargeTable.forEach((element) => {
        if (element.code === row.code) {
          flag = true;
        }
      });

      if (!flag) {
        this.ChooseSurchargeTable.push({
          allFee: 0,
          ...JSON.parse(JSON.stringify(row)),
        });
      }
    },
    //Click nút xoá sau bảng phụ phí để xoá dữ liệu tại chỉ số chỉ định
    DeleteSurchargeTable(index, row) {
      this.ChooseSurchargeTable.splice(index, 1);
      this.GetSurchargeFee();
    },
    FinishVisit() {
      let FinishModel = {
        medicalRecord: {},
        recipelInfoEvtList: [],
      };
      //Lấy bệnh án cuối cùng
      this.ChooseRecordList.map((item) => {
        this.MedicalRecordModel[`${item.name}`] = item.value;
      });
      this.MedicalRecordModel.doctorAdvice = this.DoctorAdvice;
      this.MedicalRecordModel.company = this.Company;
      this.MedicalRecordModel.fee = this.AllTotal;
      this.MedicalRecordModel.registration = {
        id: this.VisitId,
      };
      this.MedicalRecordModel.doctor = {
        id: this.UserId,
      };
      this.MedicalRecordModel.fileId = this.fileIdFile.fileId;
      this.MedicalRecordModel.fileIdFile = {
        deletes: this.fileIdFile.deletes,
        uploads: this.fileIdFile.uploads,
      };
      FinishModel.medicalRecord = this.MedicalRecordModel;

      let FinishWesternMedicineList = [];
      this.ChooseWesternMedicineTable.map((row) => {
        FinishWesternMedicineList.push({
          company: row.company,
          drugStuffId: {
            drugStuffId: row.id,
            name: row.goodsName ? row.goodsName : row.drugStuffId.name,
            price: row.price ? row.price : row.drugStuffId.price,
            dosisUnit: row.preparationUnit
              ? row.preparationUnit
              : row.drugStuffId.dosisUnit,
            pack: row.pack ? row.pack : row.drugStuffId.pack,
          },
          singleDosage: row.singleDosage,
          total: row.total,
          allFee: row.allFee,
          westernMedicineUse: row.westernMedicineUse,
          frequency: row.frequency,
          days: row.days,
        });
      });
      let WesternPrescription = {
        recipelInfo: {
          company: this.Company,
          recipelType: { name: "Đơn thuốc tây", value: "recipelType_0" },
          isPay: "0",
          remarks: this.WesternRemarks,
          fee: this.WesternFee,
        },
        recipelDetailEvtList: FinishWesternMedicineList,
      };
      if (FinishWesternMedicineList.length)
        FinishModel.recipelInfoEvtList.push(WesternPrescription);

      let FinishChineseMedicineList = [];
      this.ChooseChineseMedicineTable.map((row) => {
        FinishChineseMedicineList.push({
          company: row.company,
          drugStuffId: {
            drugStuffId: row.id,
            name: row.goodsName ? row.goodsName : row.drugStuffId.name,
            price: row.price ? row.price : row.drugStuffId.price,
            dosisUnit: row.dosisUnit
              ? row.dosisUnit
              : row.drugStuffId.dosisUnit,
            pack: row.pack ? row.pack : row.drugStuffId.pack,
          },
          singleDosage: row.singleDosage,
          chineseMedicineUse: row.chineseMedicineUse,
          allFee: row.allFee,
          total: row.total,
        });
      });
      this.ChinesePrescription.company = this.Company;
      let ChinesePrescription = {
        recipelInfo: this.ChinesePrescription,
        recipelDetailEvtList: FinishChineseMedicineList,
      };
      if (FinishChineseMedicineList.length)
        FinishModel.recipelInfoEvtList.push(ChinesePrescription);

      let FinishInfusion = [];
      this.InfusionItemList.map((item) => {
        item.infusionList.map((row) => {
          FinishInfusion.push({
            company: this.Company,
            drugStuffId: {
              drugStuffId: row.id,
              name: row.goodsName ? row.goodsName : row.drugStuffId.name,
              price: row.price ? row.price : row.drugStuffId.price,
              dosisUnit: row.preparationUnit
                ? row.preparationUnit
                : row.drugStuffId.dosisUnit,
              pack: row.pack ? row.pack : row.drugStuffId.pack,
            },
            infuseUse: item.infuseUse,
            frequency: item.frequency,
            days: item.days,
            drippingSpeed: item.drippingSpeed,
            infuseGroup: item.infuseGroup,
            singleDosage: row.singleDosage,
            total: row.total,
            skinTest: row.skinTest,
            allFee: row.allFee,
          });
        });
      });
      let InfusionPrescription = {
        recipelInfo: {
          company: this.Company,
          recipelType: { name: "Đơn truyền dịch", value: "recipelType_2" },
          isPay: "0",
          remarks: this.InfusionRemarks,
          fee: this.InfusionFee,
        },
        recipelDetailEvtList: FinishInfusion,
      };
      if (FinishInfusion.length)
        FinishModel.recipelInfoEvtList.push(InfusionPrescription);

      let FinishCostList = [];
      this.ChooseTreatmentTable.map((item) => {
        FinishCostList.push({
          company: this.Company,
          drugStuffId: {
            drugStuffId: item.id,
            name: item.itemName ? item.itemName : item.drugStuffId.name,
            price: item.salePrice ? item.salePrice : item.drugStuffId.price,
            dosisUnit: item.unit ? item.unit : item.drugStuffId.dosisUnit,
          },
          allFee: item.allFee,
          total: item.total,
        });
      });
      let CostItemPrescription = {
        recipelInfo: {
          company: this.Company,
          recipelType: { name: "Hạng mục khám chữa bệnh", value: "recipelType_3" },
          isPay: "0",
          fee: this.CostItemFee,
        },
        recipelDetailEvtList: FinishCostList,
      };
      if (FinishCostList.length)
        FinishModel.recipelInfoEvtList.push(CostItemPrescription);

      let FinishSurchargeList = [];
      this.ChooseSurchargeTable.map((item) => {
        FinishSurchargeList.push({
          company: item.company,
          drugStuffId: {
            drugStuffId: item.id,
            name: item.name ? item.name : item.drugStuffId.name,
            price: item.priceOutSell
              ? item.priceOutSell
              : item.drugStuffId.price,
          },
          allFee: item.allFee,
          total: item.total,
        });
      });
      let SurchargePrescription = {
        recipelInfo: {
          company: this.Company,
          recipelType: { name: "Phí phụ thêm", value: "recipelType_4" },
          isPay: "0",
          fee: this.SurchargeFee,
        },
        recipelDetailEvtList: FinishSurchargeList,
      };
      if (FinishSurchargeList.length)
        FinishModel.recipelInfoEvtList.push(SurchargePrescription);

      this.$refs.BasicInfoForm.validate((valid) => {
        if (valid) {
          let model = this.createFormData(FinishModel);
          allSaveMedicalRecord(model).then((responseData) => {
            if (responseData.code === 100) {
              this.$message.success("Thao tác thành công!");
              this.GetClearData();
              this.GetVisitList();
            }
          });
        }
      });
    },
    invalidPrescription() {
      getRegistrationById(this.VisitId).then((responseData) => {
        if (responseData.code === 100) {
          let status = responseData.data.status.value;
          let chargeStatus = responseData.data.chargeStatus; //Trạng thái thu phí 0- Chờ thu phí   1- Thu phí một phần   2- Đã thu phí   3-  Hoàn phí một phần    4- Đã hoàn phí
          if (chargeStatus === "0" && status === "registrationStatus_1") {
            updateStatus(this.VisitId, "registrationStatus_4").then(
              (responseData) => {
                if (responseData.code === 100) {
                  this.GetAlreadyPatientList();
                  this.$message.success("Thao tác thành công!");
                }
              }
            );
          }
        }
      });
    },
    createFormData(FinishModel) {
      let formData = new FormData();
      let deletes = [];
      for (let idx in FinishModel.medicalRecord.fileIdFile.uploads) {
        formData.append(
          "fileIdUploads",
          FinishModel.medicalRecord.fileIdFile.uploads[idx].raw
        );
      }
      deletes = deletes.concat(FinishModel.medicalRecord.fileIdFile.deletes);
      formData.append("entity", JSON.stringify(FinishModel));
      formData.append("deleteIds", JSON.stringify(deletes));

      return formData;
    },
  },
  mounted() {
    this.Init();
  },
};
</script>
<style lang='scss'>
.addPrescriptionBox .el-dropdown-menu__item {
  // font-size: 15px !important;
  padding: 0 15px !important;
}
.outpatient {
  .leftInfo {
    .el-tabs__nav {
      width: 100%;
      .el-tabs__item {
        width: 50%;
        text-align: center;
      }
    }
    .el-dialog__header {
      border-bottom: 1px solid rgb(219, 219, 219);
    }
    .el-dialog__footer {
      border-top: 1px solid rgb(219, 219, 219);
      padding: 10px;
    }
    .el-dialog__body {
      padding: 0;
    }
  }
  .tableStyle {
    .el-input-group__append {
      padding: 0 10px;
    }
  }
}
</style>
<style lang='scss' scoped>
$borderColor: rgb(219, 219, 219); //Gạch chân viền xám
$fontAndBgColor: #169bd5; //Nút và chữ màu xanh dương
.btn {
  border: 1px solid #169bd5;
  text-align: center;
  line-height: 40px;
  border-radius: 8px;
}
.configureBox {
  width: 100%;
  h5 {
    margin: 4px 0;
  }
  .recordTitleBox {
    width: 100%;
    height: 30px;
    display: flex;
    justify-content: space-between;
    border-bottom: 1px solid $borderColor;
  }
  .recordTypeBox {
    width: 100%;
    height: 60px;
    border-bottom: 1px solid $borderColor;
  }
  .recordLabelBox {
    width: 100%;
    .checkLabelBox {
      width: 100%;
      display: flex;
      flex-wrap: wrap;
      .checkItem {
        width: 24%;
      }
    }
  }
}
.outpatient {
  width: 100%;
  height: calc(100vh - 113px);
  background-color: white;
  display: flex;
  .visitRecordBtn {
    background-color: $fontAndBgColor;
    color: white;
  }
  .leftInfo {
    width: 30%;
    border-right: 1px solid $borderColor;
    height: 100%;
    padding-right: 10px;
    overflow-y: scroll;
    .leftTitle {
      width: 100%;
      height: 40px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid $borderColor;
      color: $fontAndBgColor;
      margin-bottom: 10px;
    }
    .searchItemBox {
      display: flex;
      justify-content: space-between;
      color: $fontAndBgColor;
      margin-bottom: 10px;
      line-height: 50px;
    }
    .todayVisitBox {
      width: 100%;
      height: 430px;
      .todayPatientItem {
        position: relative;
        width: 100%;
        height: 80px;
        border-bottom: 1px solid $borderColor;
        padding: 0px 0 4px 0;
        display: flex;
        .patientImg {
          display: none;
          width: 64px;
          height: 64px;
          font-size: 64px;
          margin-top: 4px;
          color: $fontAndBgColor;
        }
        .patientInfo {
          flex-grow: 1;
          margin-left: 5px;
          span {
            color: rgb(87, 208, 146);
          }
          .isNet {
            color: $fontAndBgColor;
          }
          .alreadytype {
            color: $fontAndBgColor;
          }
          .refundBox {
            position: absolute;
            top: -10px;
            right: -50px;
            width: 55px;
            height: 38px;
            border: 2px solid red;
            text-align: center;
            font-size: 18px;
            border-radius: 80%;
            color: red;
            transform: scale(0.5);
          }
        }
        .patientTime {
          width: 100px;
          height: 100%;
          line-height: 140px;
          font-size: 14px;
          text-align: right;
          color: #979797;
        }
        .patientType {
          display: none;
          width: 100px;
          height: 100%;
          line-height: 130px;
          font-size: 14px;
          text-align: right;
          color: $fontAndBgColor;
          margin-right: 10px;
        }
      }
      .prescription {
        height: 0;
        overflow: hidden;
        .prescription-box:hover {
          background: #b5deef;
          border-radius: 4px;
        }
        .prescription-box {
          padding: 5px 40px;
          border-bottom: 1px solid #dbdbdb;
          p:nth-child(1) {
            margin: 5px !important;
          }
          p:nth-child(2) {
            margin: 5px !important;
            color: #c3bebe;
            span:nth-child(2) {
              color: forestgreen;
              margin-left: 30px;
            }
          }
        }
        &.active {
          height: auto;
        }
      }
      .todayPatientItem.click :after,
      .todayPatientItem:hover :after {
        content: "";
        width: 3px;
        height: 100%;
        background-color: rgb(40, 120, 255);
        position: absolute;
        left: 0;
        top: 0;
      }
      .todayPatientItem.click,
      .todayPatientItem:hover {
        background-color: rgb(204, 236, 248);
        .patientImg {
          display: block;
        }
        .patientTime {
          display: none;
        }
        .patientType {
          display: block;
        }
      }
      .pageBox {
        width: 30%;
        height: 30px;
        float: right;
        display: flex;
        justify-content: space-between;
      }
    }
    .visitRecordsBox {
      margin-top: 10px;
      padding: 4px 8px;
      width: 100%;
      max-height: 200px;
      overflow-x: auto;
      border: 1px solid $borderColor;
      border-radius: 4px;
      list-style: none;
      font-size: 14px;
      li {
        margin: 4px 0;
      }
      .visitRecordTime {
        display: inline-block;
        width: 200px;
      }
      .diagnosisTitel {
        color: $fontAndBgColor;
      }
    }
  }
  .rightInfo {
    width: 70%;
    height: 100%;
    .topInfoBox {
      width: 100%;
      height: calc(100% - 40px);
      overflow-x: auto;
      padding: 10px;
      .infoItem {
        width: 100%;
        border: 1px solid $borderColor;
        border-radius: 4px;
        margin-bottom: 10px;
        .infoTitleBox {
          width: 100%;
          height: 42px;
          background-color: rgb(234, 234, 234);
          border-bottom: 1px solid $borderColor;
          padding: 0 10px;
          display: flex;
          justify-content: space-between;
          h4 {
            margin: 0;
            line-height: 42px;
          }
          .infoTitleRight {
            height: 42px;
            display: flex;
            flex-direction: row-reverse;
            justify-content: space-between;
            align-items: center;
          }
        }
        .el-form {
          padding: 10px;
        }
        .itemContent {
          width: 100%;
          padding: 10px;
          .chineseContentCenter {
            width: 100%;
            display: flex;
            flex-wrap: wrap;
            .chineseContentCenterItem {
              position: relative;
              width: 24%;
              height: 60px;
              margin: 10px 1% 0 0;
              .closeBtn {
                position: absolute;
                right: 10px;
                top: 10px;
                font-size: 14px;
                font-weight: bolder;
                color: #414141;
              }
            }
          }
          .InfusionContentItem {
            width: 100%;
            min-height: 140px;
            border: 1px solid $borderColor;
            position: relative;
            .closeBtn {
              position: absolute;
              right: 10px;
              top: 10px;
              font-size: 14px;
              font-weight: bolder;
              color: #414141;
            }
          }
          .itemBottomBox {
            width: 100%;
            height: 30px;
            display: flex;
            flex-direction: row-reverse;
            justify-content: space-between;
            align-items: center;
            padding: 0;
            margin-bottom: 0;
            margin-top: 10px;
            p {
              color: #5c5a5a;
            }
          }
        }
      }
      .addPrescriptionBox {
        width: 100%;
        height: 42px;
        display: flex;
        justify-content: left;
        align-items: center;
        line-height: 42px;
        margin-bottom: 10px;
        .prescription-nav {
          margin-right: 20px;
          background: #169bd5;
          color: white;
          padding: 0 25px;
          border-radius: 6px;
          position: relative;
          span {
            position: absolute;
            top: 1px;
            right: 5px;
            line-height: 24px;
          }
        }
        h4 {
          position: relative;
          padding-left: 10px;
        }
        .titleSign {
          position: absolute;
          display: inline-block;
          width: 5px;
          height: 24px;
          left: 0;
          top: 6px;
          background-color: $fontAndBgColor;
        }
      }
    }
    .bottomInfoBox {
      width: 100%;
      height: 40px;
      padding: 10px;
      border-top: 1px solid $borderColor;
      display: flex;
      justify-content: space-between;
      align-items: center;
      h5 {
        color: $fontAndBgColor;
      }
      .btnGroup {
        width: 210px;
        height: 40px;
        padding: 4px;
        display: flex;
        flex-direction: row-reverse;
        justify-content: space-between;
      }
    }
  }
  .bl-btn {
    display: flex;
    justify-content: center;
    align-items: center;
    p {
      line-height: 42px;
      margin-right: 30px;
    }
    p:hover {
      color: #169bd5;
    }
  }
}
</style>
