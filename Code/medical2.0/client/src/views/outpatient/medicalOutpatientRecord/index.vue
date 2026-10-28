<template>
  <el-container class="changeMedicalOutPatient">
    <el-aside width="360px">
      <el-card class="box-card" shadow="always" body-style="{width: '100%',height: '100%'}">
        <div slot="header" class="aside-header">
          <span>Khám hôm nay</span>
          <el-button icon="el-icon-plus"
                     style="  position: absolute; top: -8px; right: -15px;" type="primary"
                     @click="onCreateRegistration()"

          >
            Tiếp nhận khám nhanh
          </el-button>
          <!-- Cửa sổ chỉnh sửa  -->
          <registration-form
            ref="registrationForm"
            :permission="permission"
            @save-finished="handlePatientClick()"

          ></registration-form>
        </div>
        <el-row>
          <el-input placeholder="Vui lòng nhập tên bệnh nhân/Thông tin liên hệ/Số CMND/CCCD" v-model="SearchPatientInfo" class="input-with-select">
            <el-button style="width: 100px" slot="append" icon="el-icon-search" @click="handlePatientClick">
              Tìm kiếm
            </el-button>
          </el-input>
        </el-row>
        <!-- <el-row>
                    <el-button style="width:100%;">Tiếp nhận khám nhanh</el-button>
                </el-row> -->
        <el-row>
          <el-tabs v-model="TodayActiveName" @tab-click="handlePatientClick" stretch="true">
            <el-tab-pane name="prepare">
							<span slot="label">
								Chờ khám
								<el-badge :value="PreparePatientTotal" :max="99" class="mark"
                          style="line-height: normal"></el-badge>
							</span>
              <el-scrollbar view-style="height:calc(100vh - 308px);padding-top: 15px;">
                <div v-for="(item, index) in PreparePatientList" :key="item.id"
                     @click="clickPreparePatient(item, index)"
                     :class="{ click: index === backgroundColors }">
                  <p style="
                      font-weight: bold;
                      font-size: 12px;
                      color: #606266;
                      margin-left: 10px;
                      margin-top: 10px;
                      padding-top: 6px;
                    ">
										<span style="margin-top: 10px">
											{{ index + 1 }}. {{ item.patientId.name }} /
											{{ item.patientId.gender.name }} /
											{{ item.patientId.age }}tuổi
										</span>
                  </p>
                  <p style="
                      font-weight: bold;
                      font-size: 12px;
                      color: #606266;
                      flex: 1;
                      margin-left: 10px;
                    ">
                    {{ item.doctor.name }} /
                    <span style="color: #018cb7">{{ item.source.name }}</span>
                  </p>
                  <p style="
                      margin-top: -40px;
                      margin-left: 250px;
                      font-weight: bold;
                      font-size: 12px;
                      color: #606266;
                      flex: 1;
                    ">
                    {{ item.formatUpdateDate }}
                  </p>
                  <!-- <el-descriptions :column="2" :colon="false">
                    <el-descriptions-item
                      labelStyle="margin-right: 0px;"
                      contentStyle="font-weight: bold;"
                      >{{ index + 1 }}. {{ item.patientId.name }} /
                      {{ item.patientId.gender.name }} /
                      {{ item.patientId.birthdayText }}tuổi</el-descriptions-item
                    >
                    <el-descriptions-item  ></el-descriptions-item>
                    <el-descriptions-item labelStyle="margin-right: 0px;"
                      >{{ item.doctor.name }} /
                      <span style="color: #018cb7">{{
                        item.source.name
                      }}</span></el-descriptions-item
                    >
                    <el-descriptions-item  contentStyle="margin-left: 90px;">{{
                      item.formatUpdateDate
                    }}</el-descriptions-item>

                  </el-descriptions> -->
                  <el-divider></el-divider>
                  <!-- </div> -->
                </div>
              </el-scrollbar>
              <el-pagination small background @current-change="handleCurrentPreparePatientChange"
                             :page-size="SearchPreModel.limit" layout="prev, pager, next"
                             :total="PreparePatientTotal">
              </el-pagination>
            </el-tab-pane>
            <el-tab-pane name="already">
							<span slot="label">
								Đã khám
								<el-badge :value="AlreadyPatientListTotal" :max="99" type="primary"
                          style="line-height: normal"></el-badge>
							</span>
              <el-scrollbar view-style="height:calc(100vh - 293px);">
                <el-collapse v-model="activeSelAlreadyPatientItem" accordion
                             @change="AlreadyPatientDeletilQuery">
                  <div v-for="(item, index) in AlreadyPatientList" :key="item.id">
                    <div class="test">
                      <el-collapse-item :class="{ alreadyMedocal: item.id === clickChangeColor }"
                                        :name="item">
                        <template slot="title">
													<span style="
                              font-weight: bold;
                              font-size: 12px;
                              color: #606266;
                              flex: 1;
                            ">&nbsp;&nbsp;&nbsp;&nbsp;{{ index + 1 }}.
														{{ item.patientId.name }} /
														{{ item.patientId.gender.name }} /
														{{ item.patientId.age }}tuổi</span>
                          <span style="font-size: 12px; color: #606266">{{
                              item.formatReceptionEndDate
                            }}</span>
                        </template>
                        <div v-for="(o, num) in AlreadyPatientDescriptions" :key="num"
                             @click="AlreadyPatientDescriptionsQuery(o, num)"
                             :class="{ clickTest: num === overColor }">
                          <div style="
                              border-top: 1px dashed #dcdfe6;
                              margin: 0px 30px 0 15px;
                            "></div>
                          <p style="
                              font-weight: bold;
                              margin-top: 15px;
                              margin-left: 30px;
                              font-size: 12px;
                              color: #606266;
                            ">
                            {{ o.name }}
                          </p>

                          <p style="
                              font-weight: bold;
                              margin-top: 6px;
                              margin-left: 30px;
                              font-size: 12px;
                              color: #606266;
                            ">
                            {{ o.code }}
                            <span style="position: relative">
                          <div v-if="o.status === -1" class="refundBox">
                            Đã hủy bỏ
                          </div>
                          <div v-if="o.status === 1 && o.chargeStatus === 0"
                               class="refundBox">
                            Chưa thu phí
                          </div>
                          <div v-if="o.status === 1 && o.chargeStatus === 1"
                               class="refundBox">
                            Đã thu phí
                          </div>
                          <div v-if="o.status === 1 && o.chargeStatus === -1"
                               class="refundBox">
                            Đã hoàn phí
                          </div>
                          </span>
                          </p>
                          <div style="
                              border-top: 1px dashed #dcdfe6;
                              margin: 0px 30px 0 15px;
                            "></div>
                          <!-- <el-descriptions
                          :column="1"
                          :colon="false"
                          style="
                            border-top: 1px dashed #dcdfe6;
                            margin: 0px 30px 0 15px;
                          "
                        >
                          <el-descriptions-item
                            contentStyle="font-weight: bold;margin-top: 6px;"
                            >{{ o.name }}</el-descriptions-item
                          >
                          <el-descriptions-item
                            >{{ o.code }}
                            <span style="position: relative">
                              <div v-if="o.status === -1" class="refundBox">
                                Đã hủy bỏ
                              </div>
                              <div
                                v-if="o.status === 1 && o.chargeStatus === 0"
                                class="refundBox"
                              >
                                Chưa thu phí
                              </div>
                              <div
                                v-if="o.status === 1 && o.chargeStatus === 1"
                                class="refundBox"
                              >
                                Đã thu phí
                              </div>
                              <div
                                v-if="o.status === 1 && o.chargeStatus === -1"
                                class="refundBox"
                              >
                                Đã hoàn phí
                              </div>
                            </span>
                          </el-descriptions-item>
                        </el-descriptions> -->
                        </div>
                      </el-collapse-item>
                    </div>
                  </div>
                </el-collapse>
              </el-scrollbar>
              <el-pagination small background @current-change="handleCurrentAlreadyPatientChange"
                             :page-size="SearchAlreadyModel.limit" layout="prev, pager, next"
                             :total="AlreadyPatientListTotal">
              </el-pagination>
            </el-tab-pane>
          </el-tabs>
        </el-row>
      </el-card>
    </el-aside>
    <el-main>
      <el-card class="box-card" shadow="always">
        <el-scrollbar view-style="height:calc(100vh - 210px);padding-right: 20px;">
          <el-card class="box-card main-card">
            <div slot="header" style="padding: 5px 0px; font-size: 14px; font-weight: bold">
              <span>Thông tin cơ bản</span>
              <!-- <el-button v-if="isReadOnly===false" style="float: right; padding: 3px 0" type="text">清空</el-button> -->
            </div>
            <el-form :model="BasicInfoModel" ref="BasicInfoForm" label-width="auto"
                     :rules="BasicInfoFormRules" class="medical-base-form">
              <el-row :gutter="12">
                <el-col :span="3">
                  <el-form-item label="Tên bệnh nhân:" prop="name">
                    <div class="info-content">
                      <span class="patient-name">{{ BasicInfoModel.name ? BasicInfoModel.name : "" }}</span>
                      <div class="vip-tag" v-if="member.length > 0">
                        <el-popover placement="top-start" title="" width="200" trigger="hover">
                          <div v-for="(item, index) in member" :key="index"
                               style="padding: 5px">
                            <el-tag size="mini">{{ item.memberName }}</el-tag>
                          </div>
                          <img slot="reference" src="../../../assets/images/vip.png"
                               style="width: 20px; height: 20px; vertical-align: middle;"/>
                        </el-popover>
                      </div>
                      <span class="poverty-tag" v-if="poverty.length > 0">[Hộ nghèo]</span>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="2">
                  <el-form-item label="Giới tính:" prop="gender">
                    <span class="info-text">{{ BasicInfoModel.gender.name ? BasicInfoModel.gender.name : "" }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="3">
                  <el-form-item label="Tuổi:" prop="name">
                    <span class="info-text">{{ BasicInfoModel.age ? BasicInfoModel.age : 0 }}tuổi{{ BasicInfoModel.month ? BasicInfoModel.month : 0 }}tháng</span>
                  </el-form-item>
                </el-col>
                <el-col :span="3">
                  <el-form-item label="Thông tin liên hệ:" prop="phone">
                    <span class="info-text">{{ BasicInfoModel.phone ? BasicInfoModel.phone : "" }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item label="Số CMND/CCCD:" prop="card">
                    <span class="info-text">{{ BasicInfoModel.card ? BasicInfoModel.card : "" }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item label="Loại điều trị:">
                    <el-radio-group :disabled="isReadOnly" v-model="registration.treatType.value" size="mini">
                      <el-radio label="treatType_0">初诊</el-radio>
                      <el-radio label="treatType_1">Tái khám</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="5" style="text-align: right;">
                  <el-button @click="jzjlclick" type="primary" class="visit-record-btn">Hồ sơ khám</el-button>
                </el-col>
              </el-row>
              <el-alert
                v-if="displayAllergyHistory"
                title="Cảnh báo dị ứng"
                type="error"
                :closable="false"
                show-icon
                style="margin-top: 8px"
              >
                <div>
                  Bệnh nhân có tiền sử dị ứng: <strong>{{ displayAllergyHistory }}</strong>
                  — kiểm tra kỹ trước khi kê đơn.
                </div>
              </el-alert>

              <!-- <el-row :gutter="24">
                <el-col :span="4">
                  <el-form-item label="Bệnh truyền nhiễm:" prop="infectType">
                    <el-radio-group :disabled="isReadOnly" v-model="registration.infectType.value" size="small">
                      <el-radio label="infectType_0">Không</el-radio>
                      <el-radio label="infectType_1">Có</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="发病时间:">
                    <el-date-picker
                      :disabled="isReadOnly"
                      v-model="registration.morbidityTime"
                      type="datetime"
                      placeholder="Chọn ngày giờ"
                      size="small"
                      align="right"
                      :picker-options="pickerOptions">
                    </el-date-picker>
                  </el-form-item>
                </el-col>
              </el-row> -->
                  <el-drawer title="Nhật ký khám" :visible.sync="drawer" :direction="direction"
                             :before-close="JZrecordclick">
                    <el-table :data="recordtableData" border style="width: 100%">
                      <el-table-column fixed prop="jzsj" label="Ngày" :show-overflow-tooltip="true"
                                       width="140">
                      </el-table-column>
                      <el-table-column prop="jbxx" :show-overflow-tooltip="true" label="Thông tin cơ bản">
                      </el-table-column>
                      <el-table-column label="Thao tác" width="60">
                        <template slot-scope="scope">
                          <el-button @click="jzjlhandleClick(scope.row)" type="text"
                                     size="small">Xem
                          </el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </el-drawer>
            </el-form>
          </el-card>

          <el-card class="box-card main-card">
            <div slot="header" style="padding: 5px 0px; font-size: 14px; font-weight: bold">
              <span>Bệnh án</span>
              <div style="float: right; margin-top: -3px; display: flex">
                <!-- <el-upload
                                    class="upload-demo"
                                    ref="upload"
                                    action="https://jsonplaceholder.typicode.com/posts/"
                                    :on-preview="handleFilePreview"
                                    :on-remove="handleFileRemove"
                                    :on-progress="handleFileProgress"
                                    :on-change="handleFileBeforeUpload"
                                    :file-list="fileList"
                                    :show-file-list="true"
                                    :auto-upload="false">
                                    <el-button type="text" slot="trigger"><i class="el-icon-upload2 el-icon--right"></i>Tải tệp đính kèm lên</el-button>
                                </el-upload> -->

                <!-- <el-button type="text"><i class="el-icon-upload2 el-icon--right"></i>Tải tệp đính kèm lên</el-button> -->
                <div>
                  <!-- <el-button type="text"><i class="el-icon-document el-icon--right"></i>Gọi mẫu</el-button>
                                    <el-button type="text"><i class="el-icon-document-add el-icon--right"></i>Lưu thành mẫu</el-button> -->
                  <el-button v-show="blmbVisit" @click="saveblmbclick" type="primary"
                             style="margin-left: 16px">Lưu mẫu
                  </el-button>
                  <el-button v-show="blmbVisit" @click="blmbclick" type="primary"
                             style="margin-left: 16px">Mẫu bệnh án
                  </el-button>
                  <el-button v-show="blmbVisit" @click="HistoricalCases" type="primary"
                             style="margin-left: 16px">Bệnh án cũ
                  </el-button>

                  <el-popover placement="bottom-end" width="400" popper-class="medical-edit-popover"
                              trigger="click">
                    <el-card class="box-card">
                      <div slot="header" class="clearfix">
                        <span>Cài đặt bệnh án</span>
                        <el-button style="float: right; padding: 3px 0" type="text"
                                   @click="ClearMedicineCofigure()">Khôi phục mặc định
                        </el-button>
                      </div>
                      <div>
                        <el-row style="margin-bottom: 15px;">Chọn loại bệnh án</el-row>
                        <el-radio-group v-model="medicalType" @change="handleMedicalTypeChange" style="margin-bottom: 15px;">
                          <el-radio label="western">Tây y</el-radio>
                          <el-radio label="chinese">Đông y</el-radio>
                        </el-radio-group>
                        <el-row>Chọn trường bệnh án cần thiết</el-row>
                        <el-checkbox-group v-model="ChooseRecordList">
                          <el-checkbox :disabled="isReadOnly"
                                       v-for="item in MedicineRecordLabelList" :label="item"
                                       :key="item.name">{{ item.label }}
                          </el-checkbox>
                        </el-checkbox-group>
                      </div>
                    </el-card>
                    <el-button :disabled="isReadOnly" slot="reference" type="text"><i
                      class="el-icon-setting el-icon--right"></i>Cấu hình bệnh án
                    </el-button>
                  </el-popover>
                </div>
              </div>
            </div>
            <el-form :disabled="isReadOnly" :model="MedicalRecordModel" ref="BasicInfoForm"
                     :rules="BasicInfoFormRules" label-width="auto" label-suffix="：">
              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="Lý do khám chính" prop="patientTell">
                    <el-input v-model="MedicalRecordModel.patientTell"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="6">
                  <el-form-item label="Nhiệt độ cơ thể" prop="">
                    <div class="ipt-box">
                      <el-input :maxlength="45" :disabled="isReadOnly"
                                v-model="BasicInfoModel.temperature" @input="refresh"></el-input>
                      <span>&nbsp;℃</span>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="Mạch" prop="">
                    <div class="ipt-box">
                      <el-input :maxlength="45" :disabled="isReadOnly"
                                v-model="BasicInfoModel.pulse" @input="refresh"></el-input>
                      <span>&nbsp;lần/min</span>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="Nhịp thở" prop="">
                    <div class="ipt-box">
                      <el-input :maxlength="45" :disabled="isReadOnly"
                                v-model="BasicInfoModel.breathe" @input="refresh"></el-input>
                      <span>&nbsp;lần/min</span>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="Huyết áp" prop="">
                    <div class="ipt-box">
                      <el-input :maxlength="45" :disabled="isReadOnly"
                                v-model="BasicInfoModel.bloodPressure" @input="refresh"></el-input>
                      <span>&nbsp;mmHg</span>
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24" v-for="(item, index) in ChooseRecordList" :key="index">
                <el-col :span="24">
                  <el-form-item :label="item.label" :prop="item.name">
                    <el-input v-model="MedicalRecordModel[`${item.name}`]"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="Chẩn đoán tây y" prop="westernDiagnose" v-if="medicalType === 'western'">
                <span slot="label" >
                      Chẩn đoán tây y
                    <el-popover v-model:visible="WMpopoverVisible" placement="bottom-start"
                                width="700" trigger="manual"
                           >
                      <el-table :data="diagnosesList" @row-click="selectDiagnosis"
                                style="width: 100%"  size="small">
                        <el-table-column label="ICD10" prop="icd10" width="180"></el-table-column>
                        <el-table-column label="Tên chẩn đoán" prop="zdmc"></el-table-column>
                        <el-table-column label="Pinyin" prop="py"></el-table-column>
                      </el-table>
                    </el-popover>
                </span>
                <el-input
                  v-model="MedicalRecordModel.westernDiagnose"
                  placeholder="Nhập mã/tên/pinyin để truy vấn"
                  @input="onInputChange('WM')"
                  @focus="onInputChange('WM')"
                  @blur="WMpopoverVisible = false"
                >
                </el-input>
              </el-form-item>
              <el-form-item label="Chẩn đoán đông y" prop="chinaDiagnose" v-if="medicalType === 'chinese'">
                <span slot="label" >
                      Chẩn đoán đông y
                    <el-popover v-model:visible="TCMpopoverVisible" placement="top-start"
                                width="700" trigger="focus"
                    >
                      <el-table :data="diagnosesList" @row-click="selectDiagnosis"
                                style="width: 100%"  size="small">
                        <el-table-column label="ICD10" prop="icd10" width="180"></el-table-column>
                        <el-table-column label="Tên chẩn đoán" prop="zdmc"></el-table-column>
                        <el-table-column label="Pinyin" prop="py"></el-table-column>
                      </el-table>
                    </el-popover>
                </span>
                    <el-input
                      v-model="MedicalRecordModel.chinaDiagnose"
                      placeholder="Nhập mã/tên/pinyin để truy vấn"
                      @input="onInputChange('TCM')"
                      @focus="onInputChange('TCM')"
                      @blur="TCMpopoverVisible = false">
                    </el-input>
                  </el-form-item>
                  <el-form-item label="Chứng hậu Đông y" prop="chinaSyndrome" v-if="medicalType === 'chinese'">
                    <span slot="label" >
                      Chứng hậu Đông y
                    <el-popover v-model:visible="ZHpopoverVisible" placement="top-start"
                                width="700" trigger="focus"
                    >
                      <el-table :data="zyzhList" @row-click="selectZH"
                                style="width: 100%"  size="small">
                        <el-table-column label="Mã chứng hậu" prop="zhcode" width="180"></el-table-column>
                        <el-table-column label="Tên chứng hậu" prop="zhmc"></el-table-column>
                        <el-table-column label="Pinyin" prop="py"></el-table-column>
                      </el-table>
                    </el-popover>
                  </span>
                    <el-input
                      v-model="MedicalRecordModel.chinaSyndrome"
                      placeholder="Nhập mã/tên/pinyin để truy vấn"
                      @input="onInputChange('ZYZH')"
                      @focus="onInputChange('ZYZH')"
                      @blur="ZHpopoverVisible = false">
                    </el-input>
                  </el-form-item>
              <!--<el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="Hạng mục y lệnh" prop="doctorAdvice">
                    <el-input v-model="MedicalRecordModel.doctorAdvice"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>-->
              <el-row :gutter="24">
                <el-col :span="24">
                  <upload-file :objectId="MedicalFileObjId" ref="medicalFile" @getFileList="getFileList"
                               :action="MedicalFlags" @beforeRemove="beforeRemove"
                               @deleteFile="deleteFile"></upload-file>
                </el-col>
              </el-row>
            </el-form>
          </el-card>

          <el-card class="box-card main-card">
            <div slot="header" style="padding: 5px 0px; font-size: 14px; font-weight: bold">
              <span>Kê đơn thuốc</span>
            </div>
            <el-alert
              v-if="displayAllergyHistory"
              title="Cảnh báo dị ứng"
              type="error"
              :closable="false"
              show-icon
              style="margin-bottom: 10px"
            >
              <div>
                Bệnh nhân có tiền sử dị ứng: <strong>{{ displayAllergyHistory }}</strong>
                — kiểm tra kỹ trước khi kê đơn.
              </div>
            </el-alert>
            <div style="min-height: 400px">
              <el-tabs v-model="medicalEditTabsValue" type="card" @tab-remove="removeMedicalEditTab"
                       @tab-click="clickMedicalEditTab">
                <el-tab-pane :closable="false" v-if="!isReadOnly" disabled key="add" name="add">
									<span slot="label">
										<el-popover placement="bottom-start" popper-class="medical-type-popover"
                                trigger="hover">
											<el-button type="text" slot="reference"><i
                        class="el-icon-plus el-icon--right"></i>Kê đơn</el-button>
											<ul class="medical-type-ul">
												<li v-for="(item, index) in medicalTypeList" :key="item.type">
													<el-button @click="confirmAllergyBeforePrescribe(item)" plain size="mini"
                                     style="border: none">{{ item.name }}</el-button>
												</li>
											</ul>
										</el-popover>
									</span>
                </el-tab-pane>
                <el-tab-pane v-for="(item, index) in medicalEditTabs" :key="item.key" :label="item.title"
                             :name="item" :closable="item.closable">
									<span slot="label">
										<!-- <el-checkbox :disabled="isReadOnly"></el-checkbox>  -->
										{{ item.title }}
									</span>
                  <!-- Đơn thuốc tây -->
                  <div v-if="
                      item.type === 'recipelType_0' ||
                      item.type === 'recipelType_5'
                    " style="margin-top: 10px">
                    <el-row>
                      <el-divider content-position="left">Thông tin đơn thuốc</el-divider>
                      <div v-if="item.type === 'recipelType_0' || item.type === 'recipelType_2'">
                        <el-select
                          :disabled="
                          isReadOnly ||
                          item.content.recipelInfo.chargeStatus !== 0 ||
                          item.content.recipelInfo.status === -1
                        " v-model="item.content.recipelInfo.smallType" placeholder="Vui lòng chọn phân loại đơn thuốc" style="width: 100px">
                          <el-option v-for="smallTypeItem in RecipelSmallTypeList"
                                     :key="smallTypeItem.value" :label="smallTypeItem.name" :value="{
                            name: smallTypeItem.name,
                            value: smallTypeItem.value,
                          }">
                          </el-option>
                        </el-select>
                      </div>
                      <el-popover placement="top-start" v-if="
                          !isReadOnly &&
                          item.content.recipelInfo.chargeStatus === 0 &&
                          item.content.recipelInfo.status !== -1
                        " width="700" trigger="focus">
                        <el-table :data="WesternMedicineTable" :height="300" border
                                  highlight-current-row @row-click="RowClickWesternTable">
                          <el-table-column prop="drug.type.name" label="Loại thuốc">
                          </el-table-column>
                          <el-table-column prop="drug.goodsName" label="Tên thuốc">
                          </el-table-column>
                          <el-table-column prop="gg" label="Quy cách" width="120">
                            <template slot-scope="scope">
                              {{
                                scope.row.drug.dosis
                              }}{{ scope.row.drug.dosisUnit.name }} *
                              {{
                                scope.row.drug.preparation
                              }}{{ scope.row.drug.preparationUnit.name }}/{{
                                scope.row.drug.pack.name
                              }}
                            </template>
                          </el-table-column>
                          <el-table-column prop="drug.factory.name" label="Nhà sản xuất" width="100">
                          </el-table-column>
                          <el-table-column label="Giá bán" width="80">
                            <template slot-scope="scope">
                              {{
                                (scope.row.drug.price).toFixed(2) +
                                "/" +
                                scope.row.drug.pack.name
                              }}
                            </template>
                          </el-table-column>
                          <el-table-column label="Giá bán lẻ tách hộp" width="80">
                            <template slot-scope="scope">
                              {{
                                scope.row.drug.isUnpackSell === 1
                                  ? (scope.row.drug.retailPrice).toFixed(4) +
                                  "/" +
                                  scope.row.drug.preparationUnit.name
                                  : "--"
                              }}
                            </template>
                          </el-table-column>
                          <el-table-column  v-if="!item.content.recipelInfo.isPre" prop="surplusStock" label="Tồn kho khả dụng" width="100">
                            <template slot-scope="scope">
                              {{
                                Math.floor(
                                  scope.row.surplusStock /
                                  scope.row.drug.preparation
                                ) > 0
                                  ? Math.floor(
                                  scope.row.surplusStock /
                                  scope.row.drug.preparation
                                  ) +
                                  scope.row.drug.pack.name +
                                  (scope.row.surplusStock %
                                  scope.row.drug.preparation >
                                  0
                                    ? (scope.row.surplusStock %
                                    scope.row.drug.preparation) +
                                    scope.row.drug.preparationUnit.name
                                    : "")
                                  : scope.row.surplusStock +
                                  scope.row.drug.preparationUnit.name
                              }}
                            </template>
                          </el-table-column>
                        </el-table>
                        <el-input prefix-icon="el-icon-plus" suffix-icon="el-icon-search"
                                  style="width: 30%" slot="reference" ref="WesternInput"
                                  v-model="SearchWesternInput" @input="GetWesternTable(item.content.recipelInfo.isPre)"
                                  @focus="GetWesternTable(item.content.recipelInfo.isPre)" placeholder="Nhập tên thuốc hoặc mã pinyin"></el-input>

                      </el-popover>


                      <el-checkbox style="margin-left: 20px;" v-model="item.content.recipelInfo.chronicDisease"
                                   :disabled="isReadOnly ||
                                   item.content.recipelInfo.chargeStatus !== 0 ||
                                   item.content.recipelInfo.status === -1">
                        Có phải bệnh mạn tính không{{
                          item.content.recipelInfo.chronicDisease ? "(Đơn thuốc bệnh mạn tính tối đa kê được" + systemParamConfig.chronicDays + "ngày)" : "(Đơn thuốc không phải bệnh mạn tính tối đa kê được" + systemParamConfig.normalDays + "ngày)"
                        }}
                      </el-checkbox>
                      <el-checkbox style="margin-left: 20px;" v-model="item.content.recipelInfo.isPre"
                                   :disabled="isReadOnly ||
                                   item.content.recipelInfo.chargeStatus !== 0 ||
                                   item.content.recipelInfo.status === -1"
                                   @change="DeleteMedicalRow('','',item)"
                      >
                        Đơn thuốc điện tử
                      </el-checkbox>
                      <el-button type="primary" v-if="!isReadOnly" style="float: right" plain
                                 @click="historyRecipel(item.type, index)">Đơn thuốc cũ
                      </el-button>
                      <span style="float: right">&nbsp;&nbsp;&nbsp;</span>
                      <el-button type="primary" v-if="!isReadOnly"
                                 style="float: right; margin-left: 120px" plain
                                 @click="templateRecipel(item.type, index)">Đơn thuốc mẫu
                      </el-button>
                      <!-- <span style="float:right;">&nbsp;</span> -->
                      <el-button type="primary" v-if="!isReadOnly"
                                 style="float: right; margin-right: -105px" plain
                                 @click="saveTemplate(item.type, index)">Lưu thành mẫu
                      </el-button>
                    </el-row>
<!--                    处方详细-->
                    <el-row  >
                      <el-table   :data="
                          getDataFilterTable(
                            item.content.recipelDetailEvtList,
                            0,
                            item
                          )
                        " border style="width: 100%" class="tableStyle" @cell-click="checkInventory">
                        <el-table-column type="index" label="Số thứ tự" align="center">
                        </el-table-column>
                        <el-table-column prop="drugStuffId" label="Tên thuốc" align="center">
                          <template slot-scope="scope">
                            {{ scope.row.drugStuffId.name }}
                            <span style="color: forestgreen">
															{{
                                scope.row.drugStuffId.drug.dosis +
                                scope.row.drugStuffId.drug.dosisUnit.name +
                                "*" +
                                scope.row.drugStuffId.drug.preparation +
                                scope.row.drugStuffId.drug.preparationUnit
                                  .name +
                                "/" +
                                scope.row.drugStuffId.drug.pack.name
                              }}
														</span>
                          </template>
                        </el-table-column>
                        <!-- <el-table-column

                          prop="drugStuffId"
                          label="Tên vật tư"
                          align="center"
                        >
                          <template slot-scope="scope">
                            {{ scope.row.drugStuffId.name }}
                            <span style="color: forestgreen">
                              {{
                                scope.row.drugStuffId.stuff.packNumber +
                                scope.row.drugStuffId.stuff.minUnit.name +
                                "/" +

                                scope.row.drugStuffId.stuff.packUnit.name
                              }}
                            </span>
                          </template>
                        </el-table-column> -->
                        <el-table-column prop="singleDosage" label="Liều dùng mỗi lần" align="center"
                        >
                          <template slot-scope="scope">
                            <div style="display: flex; align-items: center;">
                              <el-input v-model="scope.row.singleDosage"
                                        ref="westernMedicine"
                                        oninput="value=value.replace(/[^\d.]/g,'')"
                                        @input="MedicalCalculate()" :disabled="
                                  isReadOnly ||
                                  item.content.recipelInfo.chargeStatus !== 0 ||
                                  item.content.recipelInfo.status === -1"
                                        style="width: 150px;">
                                <template slot="append">
                                  <el-dropdown @command="(command) => changeDosageUnit(scope.row, command)">
                                    <span class="el-dropdown-link">
                                      {{ scope.row.dosageUnitType === 'preparation' ? scope.row.drugStuffId.preparationUnit.name : scope.row.drugStuffId.dosisUnit.name }}
                                      <i class="el-icon-arrow-down el-icon--right"></i>
                                    </span>
                                    <el-dropdown-menu slot="dropdown">
                                      <el-dropdown-item command="dosis">Đơn vị liều lượng ({{ scope.row.drugStuffId.dosisUnit.name }})</el-dropdown-item>
                                      <el-dropdown-item command="preparation">Đơn vị bào chế ({{ scope.row.drugStuffId.preparationUnit.name }})</el-dropdown-item>
                                    </el-dropdown-menu>
                                  </el-dropdown>
                                </template>
                              </el-input>
                            </div>
                          </template>
                        </el-table-column>
                        <el-table-column prop="westernMedicineUse" label="Cách dùng" align="center"
                        >
                          <template slot-scope="scope">
                            <el-select v-model="scope.row.westernMedicineUse" :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              " placeholder="" @change="changeUse(scope.row.westernMedicineUse)">
                              <el-option v-for="item in WesternUseOption"
                                         :key="item.value" :label="item.name"
                                         :value="{ name: item.name, value: item.value }">
                              </el-option>
                            </el-select>
                          </template>
                        </el-table-column>
                        <el-table-column prop="frequency" label="Tần suất" align="center">
                          <template slot-scope="scope">
                            <el-select v-model="scope.row.frequency" :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              " @change="MedicalCalculate()" placeholder="">
                              <el-option v-for="item in FrequencyOption" :key="item.value"
                                         :label="item.name" :value="{
                                  name: item.name,
                                  value: item.value,
                                }">
                              </el-option>
                            </el-select>
                          </template>
                        </el-table-column>
                        <el-table-column prop="days" label="Số ngày" align="center">
                          <template slot-scope="scope">
                            <el-input-number v-model="scope.row.days.name" :min='0'
                                             :max="item.content.recipelInfo.chronicDisease? systemParamConfig.chronicDays: systemParamConfig.normalDays"
                                             :disabled="isReadOnly || item.content.recipelInfo.chargeStatus !== 0 || item.content.recipelInfo.status === -1"
                                             :controls="false" @change="calculateTotalByDays(scope.row)">
                            </el-input-number>
                            <!-- <el-select v-model="scope.row.days" :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              " @change="MedicalCalculate()" placeholder="">
                              <el-option v-for="item in DayNumOption" :key="item.value"
                                         :label="item.name"
                                         :value="{ name: item.name, value: item.value }">
                              </el-option>
                            </el-select> -->
                          </template>
                        </el-table-column>
                        <el-table-column v-if="item.content.recipelInfo.isPre" prop="total" label="Tổng số lượng" align="center">
                          <template slot-scope="scope">
                            <span>{{ scope.row.total }} {{ scope.row.isUnpackSell === 1 ? scope.row.drugStuffId.preparationUnit.name : scope.row.drugStuffId.pack.name }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column v-if="!item.content.recipelInfo.isPre" prop="total" label="Tổng số lượng" align="center">
                          <template slot-scope="scope">
                            <span>{{ scope.row.total }} {{ scope.row.isUnpackSell === 1 ? scope.row.drugStuffId.preparationUnit.name : scope.row.drugStuffId.pack.name }}</span>
                          </template>
                        </el-table-column>
                        <!-- <el-table-column

                          prop="total"
                          label="Tổng số lượng"
                          align="center"
                          width="100"
                        >
                          <template slot-scope="scope">
                            {{
                               Math.floor(
                                  scope.row.total /
                                    scope.row.drugStuffId.stuff.packNumber
                                ) > 0
                                  ? Math.floor(
                                      scope.row.total /
                                        scope.row.drugStuffId.stuff.packNumber
                                    ) +
                                    scope.row.drugStuffId.pack.name +
                                    (scope.row.total %
                                      scope.row.drugStuffId.stuff.packNumber >
                                    0
                                      ? (scope.row.total %
                                          scope.row.drugStuffId.stuff
                                            .packNumber) +
                                        scope.row.drugStuffId.preparationUnit.name
                                      : "")
                                  : scope.row.total +
                                    scope.row.drugStuffId.preparationUnit.name
                                      }}
                          </template>
                        </el-table-column> -->
                        <el-table-column v-if="!item.content.recipelInfo.isPre"  prop="isUnpackSell" label="Đơn giá"
                                         align="center">
                          <template slot-scope="scope">
                            <el-select v-model="scope.row.isUnpackSell" :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              " placeholder="Vui lòng chọn" @change="calculateTotalByDays(scope.row)" style="width: 110px">
                              <el-option :label="
                                  (scope.row.drugStuffId.price).toFixed(4) +
                                  '/' +
                                  scope.row.drugStuffId.pack.name
                                " :value="0"></el-option>
                              <el-option :label="
                                  (scope.row.drugStuffId.retailPrice) +
                                  '/' +
                                  scope.row.drugStuffId.preparationUnit.name
                                " :value="1"></el-option>
                            </el-select>
                          </template>
                        </el-table-column>

                        <el-table-column  v-if="!item.content.recipelInfo.isPre"  prop="allFee" label="Số tiền" align="center">
                        </el-table-column>
                        <el-table-column v-if="
                            !isReadOnly &&
                            item.content.recipelInfo.chargeStatus === 0 &&
                            item.content.recipelInfo.status !== -1
                          " label="Thao tác" align="center" width="80">
                          <template slot-scope="scope">
                            <i class="el-icon-circle-close"
                               @click="DeleteMedicalRow(scope.$index, scope.row)"></i>
                            <span style="display: none">{{ changeData }}</span>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-row>
                  </div>
                  <!-- Đơn thuốc đông y -->
                  <div v-if="item.type === 'recipelType_1'" style="margin-top: 15px">
                    <el-row>
                      <el-divider content-position="left">Thông tin đơn thuốc</el-divider>
                      <!--<el-select :disabled="
                          isReadOnly ||
                          item.content.recipelInfo.chargeStatus !== 0 ||
                          item.content.recipelInfo.status === -1
                        " v-model="item.content.recipelInfo.smallType" placeholder="Vui lòng chọn phân loại đơn thuốc" style="width: 100px">
                        <el-option v-for="smallTypeItem in RecipelSmallTypeList"
                                   :key="smallTypeItem.value" :label="smallTypeItem.name" :value="{
                            name: smallTypeItem.name,
                            value: smallTypeItem.value,
                          }">
                        </el-option>
                      </el-select>-->

                      <el-popover placement="top-start"
                                  v-if="
                          !isReadOnly &&
                          item.content.recipelInfo.chargeStatus === 0 &&
                          item.content.recipelInfo.status !== -1
                        " width="700" trigger="focus">
                        <el-table :data="ChineseMedicineTable" :height="300" border
                                  highlight-current-row @row-click="RowClickChineseTable">
                          <el-table-column prop="drug.type.name" label="Loại thuốc">
                          </el-table-column>
                          <el-table-column prop="drug.goodsName" label="Tên thuốc">
                          </el-table-column>
                          <el-table-column prop="gg" label="Quy cách" width="120">
                            <template slot-scope="scope">
                              {{
                                scope.row.drug.dosis
                              }}{{ scope.row.drug.dosisUnit.name }} *
                              {{
                                scope.row.drug.preparation
                              }}{{ scope.row.drug.preparationUnit.name }}/{{
                                scope.row.drug.pack.name
                              }}
                            </template>
                          </el-table-column>
                          <el-table-column prop="drug.factory.name" label="Nhà sản xuất" width="100">
                          </el-table-column>
                          <el-table-column label="Giá bán" width="80">
                            <template slot-scope="scope">
                              {{
                                (scope.row.drug.price).toFixed(4) +
                                "/" +
                                scope.row.drug.pack.name
                              }}
                            </template>
                          </el-table-column>
                          <!--
                                                    <el-table-column label="Giá bán lẻ tách hộp" width="80">
                                                        <template slot-scope="scope">
                                                            {{scope.row.isUnpackSell === "1" ? (scope.row.retailPrice + "/" + scope.row.preparationUnit.name) : "--"}}
                                                        </template>
                                                    </el-table-column>
                                                    -->
                          <el-table-column prop="surplusStock" label="Tồn kho" width="100">
                            <template slot-scope="scope">
                              {{
                                Math.floor(
                                  scope.row.surplusStock /
                                  scope.row.drug.preparation
                                ) > 0
                                  ? Math.floor(
                                  scope.row.surplusStock /
                                  scope.row.drug.preparation
                                  ) +
                                  scope.row.drug.pack.name +
                                  (scope.row.surplusStock %
                                  scope.row.drug.preparation >
                                  0
                                    ? (scope.row.surplusStock %
                                    scope.row.drug.preparation) +
                                    scope.row.drug.preparationUnit.name
                                    : "")
                                  : scope.row.surplusStock +
                                  scope.row.drug.preparationUnit.name
                              }}
                            </template>
                          </el-table-column>
                        </el-table>
                        <el-input prefix-icon="el-icon-plus" suffix-icon="el-icon-search"
                                  style="width: 30%" slot="reference" id="WesternInput"
                                  v-model="SearchChineseInput" @input="GetChineseTable(item.content.recipelInfo.isPre)"
                                  @focus="GetChineseTable(item.content.recipelInfo.isPre)"
                                  @keydown.enter.native="focusNextInput('2')"
                                  placeholder="Nhập tên thuốc hoặc mã pinyin"></el-input>
                      </el-popover>
                      <el-button type="primary" v-if="!isReadOnly" style="float: right" plain
                                 @click="historyRecipel(item.type, index)">Đơn thuốc cũ
                      </el-button>
                      <span style="float: right">&nbsp;&nbsp;&nbsp;</span>
                      <el-button type="primary" v-if="!isReadOnly"
                                 style="float: right; margin-left: 120px" plain
                                 @click="templateRecipel(item.type, index)">Đơn thuốc mẫu
                      </el-button>
                      <!-- <span style="float:right;">&nbsp;&nbsp;&nbsp;</span> -->
                      <el-button type="primary" v-if="!isReadOnly"
                                 style="float: right; margin-right: -105px" plain
                                 @click="saveTemplate(item.type, index)">Lưu thành mẫu
                      </el-button>
                    </el-row>
                    <el-row :gutter="24" style="min-height: 100px; display: flex; flex-wrap: wrap">
                      <el-col :span="6" v-for="(citem, index) in getDataFilterTable(
                          item.content.recipelDetailEvtList,
                          0,
                          item
                        )" :key="citem" :offset="0">
                        <el-card :index="index" class="box-card chinese-medicine-card"
                                 style="width: 240px; margin-bottom: 10px">
                          <div slot="header" class="clearfix"
                               style="padding: 5px 0px; font-size: 14px">
                            <span>{{ citem.drugStuffId.name }}</span>
                            <el-button v-if="
                                !isReadOnly &&
                                item.content.recipelInfo.chargeStatus === 0 &&
                                item.content.recipelInfo.status !== -1
                              " :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              " @click="DeleteMedicalRow(index, citem, item)" icon="el-icon-close"
                                       style="float: right; padding: 3px 0" type="text">
                            </el-button>
                          </div>
                          <div>
                            <el-input
                              :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              " placeholder="Số lượng" size="mini" ref='herbMedicine'
                                      oninput="value=value.replace(/[^\d.]/g,'')" @input="
                                changeSingleDosage(
                                  citem.singleDosage,
                                  index,
                                  item
                                )
                              " v-model="citem.singleDosage" style="width: 80px"
                              @keyup.enter.native="focusNextInput('1')"
                            >
                              <template slot="append">{{
                                  citem.drugStuffId.pack.name
                                }}
                              </template>
                            </el-input>
                            <el-select :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              " size="mini" v-model="citem.chineseMedicineUse" placeholder="" style="width: 105px">
                              <el-option v-for="oitem in ChineseUseTimeOption"
                                         :key="oitem.value" :label="oitem.name" :value="{
                                  name: oitem.name,
                                  value: oitem.value,
                                }">
                              </el-option>
                            </el-select>
                          </div>
                        </el-card>
                      </el-col>
                    </el-row>
                    <el-row>
                      <el-input :disabled="
                          isReadOnly ||
                          item.content.recipelInfo.chargeStatus !== 0 ||
                          item.content.recipelInfo.status === -1
                        " v-model="item.content.recipelInfo.dosage" oninput="value=value.replace(/[^\d.]/g,'')"
                                @input="MedicalCalculate()"  id ='dosageInput' style="width: 60px"
                       @keydown.enter.native="focusNextInput('')"
                      ></el-input>

                      &nbsp;thang &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Cách dùng：
                      <el-select
                         :disabled="
                          isReadOnly ||
                          item.content.recipelInfo.chargeStatus !== 0 ||
                          item.content.recipelInfo.status === -1
                        " v-model="item.content.recipelInfo.recipelUse" placeholder="Vui lòng chọn" style="width: 110px"
                                 @change="exchage"
                      >
                        <el-option v-for="pitem in ChineseUseOption" :key="pitem.value"
                                   :label="pitem.name"
                                   :value="{ name: pitem.name, value: pitem.value }">
                        </el-option>
                      </el-select>
                      <el-select
                          :disabled="
                          isReadOnly ||
                          item.content.recipelInfo.chargeStatus !== 0 ||
                          item.content.recipelInfo.status === -1
                        " v-model="item.content.recipelInfo.frequency" placeholder="Vui lòng chọn" style="width: 110px"
                                 @change="exchage">
                        <el-option v-for="pitem in ChineseTimeOption" :key="pitem.value"
                                   :label="pitem.name"
                                   :value="{ name: pitem.name, value: pitem.value }">
                        </el-option>
                      </el-select>
                      <el-select
                          :disabled="
                          isReadOnly ||
                          item.content.recipelInfo.chargeStatus !== 0 ||
                          item.content.recipelInfo.status === -1
                        " v-model="item.content.recipelInfo.takeFrequency" placeholder="Vui lòng chọn" style="width: 110px"
                                 @change="exchage">
                        <el-option v-for="pitem in JSON.parse(
                            JSON.stringify(FrequencyOption)
                          )" :key="pitem.value" :label="pitem.name" :value="{
                            name: pitem.name,
                            value: pitem.value,
                          }">
                        </el-option>
                      </el-select>
                      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; một lần&nbsp;
                      <el-input
                           id="singleDosageInput"
                           @keydown.enter.native='focusNextInput("fj")'
                           :disabled="
                          isReadOnly ||
                          item.content.recipelInfo.chargeStatus !== 0 ||
                          item.content.recipelInfo.status === -1
                        " v-model="item.content.recipelInfo.singleDosage" style="width: 60px">
                      </el-input>&nbsp;
                      <span
                      v-if="isSpecial">ml</span><span v-else>ml</span>
                      <span style="float: right">Số tiền thuốc Đông y:{{
                          item.content.recipelInfo.medicalAmount
                            ? item.content.recipelInfo.medicalAmount
                            : 0
                        }}VNĐ</span>
                      <el-input :disabled="
                          isReadOnly ||
                          item.content.recipelInfo.chargeStatus!== 0 ||
                          item.content.recipelInfo.status === -1
                        " v-model="item.content.recipelInfo.chinessNotes" placeholder="Nhập tùy chọn ghi chú"
                                style="margin-top:10px"></el-input>
                    </el-row>
                  </div>
                  <!-- Đơn truyền dịch -->
                  <div v-if="item.type === 'recipelType_2'" style="margin-top: 15px">
                    <div>
                      <el-row>
                        <el-divider content-position="left">Thông tin đơn thuốc</el-divider>

                        <el-select :disabled="
                            isReadOnly ||
                            item.content.recipelInfo.chargeStatus !== 0 ||
                            item.content.recipelInfo.status === -1
                          " v-model="item.content.recipelInfo.smallType" placeholder="Vui lòng chọn phân loại đơn thuốc" style="width: 100px">
                          <el-option v-for="smallTypeItem in RecipelSmallTypeList"
                                     :key="smallTypeItem.value" :label="smallTypeItem.name" :value="{
                              name: smallTypeItem.name,
                              value: smallTypeItem.value,
                            }">
                          </el-option>
                        </el-select>

                        <el-button v-if="
                            !isReadOnly &&
                            item.content.recipelInfo.chargeStatus === 0 &&
                            item.content.recipelInfo.status !== -1
                          " @click="addGroup(item)" type="primary" plain>Thêm một nhóm
                        </el-button>
                        <!-- <button v-if="
                            !isReadOnly &&
                            item.content.recipelInfo.chargeStatus === 0 &&
                            item.content.recipelInfo.status !== -1
                          " @click="minusGroup(item)">减一组</button> -->
                        <el-button type="primary" v-if="!isReadOnly" style="float: right" plain
                                   @click="historyRecipel(item.type, index)">Đơn thuốc cũ
                        </el-button>
                        <span style="float: right">&nbsp;&nbsp;&nbsp;</span>
                        <el-button type="primary" v-if="!isReadOnly"
                                   style="float: right; margin-left: 120px" plain
                                   @click="templateRecipel(item.type, index)">Đơn thuốc mẫu
                        </el-button>
                        <!-- <span style="float:right;">&nbsp;&nbsp;&nbsp;</span> -->
                        <el-button type="primary" v-if="!isReadOnly"
                                   style="float: right; margin-right: -105px" plain
                                   @click="saveTemplate(item.type, index)">Lưu thành mẫu
                        </el-button>
                      </el-row>
                    </div>
                    <div style="margin-left: -10px">
                      <div v-for="(items, index) in item.infusion.zushu" :key="index" style="
                          border: 1px solid #dcdcdc;
                          padding: 10px;
                          margin: 10px;
                        " class="shuzhuClass">
                        <el-row>
                          <div>
                            <el-popover placement="top-start" v-if="
                                !isReadOnly &&
                                item.content.recipelInfo.chargeStatus === 0 &&
                                item.content.recipelInfo.status !== -1
                              " width="700" trigger="focus">
                              <el-table :data="InfusionTable" :height="300" border
                                        highlight-current-row @row-click="
                                  (row, column, e) =>
                                    RowClickInfusionTable(row, column, e, index)
                                ">
                                <el-table-column prop="drug.type.name" label="Loại thuốc">
                                </el-table-column>
                                <el-table-column prop="drug.goodsName" label="Tên thuốc">
                                </el-table-column>
                                <el-table-column prop="gg" label="Quy cách" width="120">
                                  <template slot-scope="scope">
                                    {{
                                      scope.row.drug.dosis
                                    }}{{ scope.row.drug.dosisUnit.name }} *
                                    {{
                                      scope.row.drug.preparation
                                    }}{{
                                      scope.row.drug.preparationUnit.name
                                    }}/{{ scope.row.drug.pack.name }}
                                  </template>
                                </el-table-column>
                                <el-table-column prop="drug.factory.name" label="Nhà sản xuất"
                                                 width="100"></el-table-column>
                                <el-table-column label="Giá bán" width="80">
                                  <template slot-scope="scope">
                                    {{
                                      (scope.row.drug.price).toFixed(2) +
                                      "/" +
                                      scope.row.drug.pack.name
                                    }}
                                  </template>
                                </el-table-column>
                                <el-table-column label="Giá bán lẻ tách hộp" width="80">
                                  <template slot-scope="scope">
                                    {{
                                      scope.row.drug.isUnpackSell === 1
                                        ? (scope.row.drug.retailPrice).toFixed(4) +
                                        "/" +
                                        scope.row.drug.preparationUnit.name
                                        : "--"
                                    }}
                                  </template>
                                </el-table-column>
                                <el-table-column prop="surplusStock" label="Tồn kho"
                                                 width="100">
                                  <template slot-scope="scope">
                                    {{
                                      Math.floor(
                                        scope.row.surplusStock /
                                        scope.row.drug.preparation
                                      ) > 0
                                        ? Math.floor(
                                        scope.row.surplusStock /
                                        scope.row.drug.preparation
                                        ) +
                                        scope.row.drug.pack.name +
                                        (scope.row.surplusStock %
                                        scope.row.drug.preparation >
                                        0
                                          ? (scope.row.surplusStock %
                                          scope.row.drug.preparation) +
                                          scope.row.drug.preparationUnit
                                            .name
                                          : "")
                                        : scope.row.surplusStock +
                                        scope.row.drug.preparationUnit.name
                                    }}
                                  </template>
                                </el-table-column>
                              </el-table>
                              <el-input prefix-icon="el-icon-plus"
                                        suffix-icon="el-icon-search" style="width: 20%"
                                        slot="reference"
                                        v-model="SearchInfusion[index]"
                                        @input="GetInfusionTable(index)"
                                        @focus="GetInfusionTable(index)"
                                        placeholder="Nhập tên thuốc hoặc mã pinyin"></el-input>
                            </el-popover>
                            <span>&nbsp;&nbsp;thứ{{ index + 1 }}nhóm</span>
                            <span style="
                                float: right;
                                height: 25px;
                                line-height: 25px;
                                font-size: 14px;
                              ">&nbsp;&nbsp;</span>
                            <el-button style="float: right" type="primary" plain
                                       icon="el-icon-delete" circle v-if="
                                !isReadOnly &&
                                item.content.recipelInfo.chargeStatus === 0 &&
                                item.content.recipelInfo.status !== -1
                              " @click="minusGroup(item, index)"></el-button>
                            <span style="
                                float: right;
                                height: 25px;
                                line-height: 25px;
                                font-size: 14px;
                              ">&nbsp;&nbsp;</span>
                            <span style="
                                float: right;
                                height: 25px;
                                line-height: 25px;
                                font-size: 14px;
                              ">&nbsp;&nbsp;giọt/Phút</span>

                            <el-input style="float: right; width: 10%"
                                      v-model="item.infusion.drippingSpeed[index]"
                                      placeholder="Vui lòng chọn tốc độ nhỏ giọt" :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              ">
                            </el-input>

                            <span style="
                                float: right;
                                height: 25px;
                                line-height: 25px;
                                font-size: 14px;
                              ">&nbsp;&nbsp;ngày&nbsp;&nbsp;</span>

                            <el-input-number v-model="item.infusion.days[index].name" style="float: right; width: 10%"
                                             :disabled="isReadOnly || item.content.recipelInfo.chargeStatus !== 0 || item.content.recipelInfo.status === -1"
                                             @change="MedicalCalculate()" :controls="false" placeholder="Vui lòng chọn số ngày">
                            </el-input-number>

                            <!-- <el-select style="float: right; width: 10%"
                                       v-model="item.infusion.days[index]" :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              " @change="MedicalCalculate()" placeholder="Vui lòng chọn số ngày">
                              <el-option v-for="item in DayNumOption" :key="item.value"
                                         :label="item.name"
                                         :value="{ name: item.name, value: item.value }">
                              </el-option>
                            </el-select> -->

                            <span style="
                                float: right;
                                height: 25px;
                                line-height: 25px;
                                font-size: 18px;
                              ">&nbsp;&nbsp;</span>
                            <el-select style="float: right; width: 10%"
                                       v-model="item.infusion.frequency[index]" :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              " @change="MedicalCalculate()" placeholder="Vui lòng chọn tần suất">
                              <el-option v-for="item in FrequencyOption" :key="item.value"
                                         :label="item.name" :value="{
                                  name: item.name,
                                  value: item.value,
                                }">
                              </el-option>
                            </el-select>

                            <span style="
                                float: right;
                                height: 25px;
                                line-height: 25px;
                                font-size: 18px;
                              ">&nbsp;&nbsp;</span>

                            <el-select style="float: right; width: 10%"
                                       v-model="item.infusion.infuseUse[index]" :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              " placeholder="Vui lòng chọn cách dùng">
                              <el-option v-for="item in infuseUseOption" :key="item.value"
                                         :label="item.name"
                                         :value="{ name: item.name, value: item.value }">
                              </el-option>
                            </el-select>
                          </div>
                          <div style="border: 7px solid #0000"></div>
                          <el-table :data="
                              getDataFilterTable(
                                item.infusion.infusionProject[index],
                                0,
                                item
                              )
                            " border style="width: 100%" class="tableStyle" @cell-click="checkInventory">
                            <el-table-column type="index" label="Số thứ tự"
                                             align="center"></el-table-column>
                            <!-- <el-table-column
                         v-show="true"
                          prop="infuseGroup"
                          label="Số nhóm"
                          width="100"
                          align="center"
                        ></el-table-column> -->
                            <el-table-column prop="drugStuffId" label="Tên thuốc" align="center" width="260">
                              <template slot-scope="scope">
                                {{ scope.row.drugStuffId.name }}
                                <span style="color: forestgreen">
																	{{
                                    scope.row.drugStuffId.drug.dosis +
                                    scope.row.drugStuffId.drug.dosisUnit.name +
                                    "*" +
                                    scope.row.drugStuffId.drug.preparation +
                                    scope.row.drugStuffId.drug.preparationUnit
                                      .name +
                                    "/" +
                                    scope.row.drugStuffId.drug.pack.name
                                  }}
																</span>
                              </template>
                            </el-table-column>
                            <el-table-column prop="skinTest" label="Test da" align="center"
                            >
                              <template slot-scope="scope">
                                <el-select v-model="scope.row.skinTest" :disabled="
                                    isReadOnly ||
                                    item.content.recipelInfo.chargeStatus !==
                                      0 ||
                                    item.content.recipelInfo.status === -1
                                  " placeholder="">
                                  <el-option v-for="item in InfusionOption"
                                             :key="item.value" :label="item.name" :value="{
                                      name: item.name,
                                      value: item.value,
                                    }">
                                  </el-option>
                                </el-select>
                              </template>
                            </el-table-column>
                            <el-table-column prop="singleDosage" label="Liều dùng mỗi lần" align="center"
                            >
                              <template slot-scope="scope">
                                <el-input v-model="scope.row.singleDosage"
                                          ref='transfusion'
                                          oninput="value=value.replace(/[^\d.]/g,'')"
                                          @input="MedicalCalculate()" :disabled="
                                    isReadOnly ||
                                    item.content.recipelInfo.chargeStatus !==
                                      0 ||
                                    item.content.recipelInfo.status === -1
                                  ">
                                  <template slot="append">{{
                                      scope.row.drugStuffId.dosisUnit.name
                                    }}
                                  </template>
                                </el-input>
                              </template>
                            </el-table-column>
                            <!-- <el-table-column
                          prop="drippingSpeed"
                          label="Tốc độ truyền"
                          align="center"
                          width="80"
                        >
                          <template slot-scope="scope">
                            <el-input
                              v-model="scope.row.drippingSpeed"

                              :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              "
                            >

                            </el-input>
                            <span >
                                giọt/Phút
                              </span>
                          </template>
                        </el-table-column> -->
                            <!-- <el-table-column
                          prop="infuseUse"
                          label="Cách dùng"
                          align="center"
                          width="120"
                        >
                          <template slot-scope="scope">
                            <el-select
                              v-model="scope.row.infuseUse"
                              :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              "
                              placeholder=""
                            >
                              <el-option
                                v-for="item in infuseUseOption"
                                :key="item.value"
                                :label="item.name"
                                :value="{ name: item.name, value: item.value }"
                              >
                              </el-option>
                            </el-select>
                          </template>
                        </el-table-column> -->
                            <!-- <el-table-column
                          prop="frequency"
                          label="Tần suất"
                          align="center"
                          width="120"
                        >
                          <template slot-scope="scope">
                            <el-select
                              v-model="scope.row.frequency"
                              :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              "
                              @change="MedicalCalculate()"
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
                        </el-table-column> -->
                            <!-- <el-table-column
                          prop="days"
                          label="Số ngày"
                          align="center"
                          width="100"
                        >
                          <template slot-scope="scope">
                            <el-select
                              v-model="scope.row.days"
                              :disabled="
                                isReadOnly ||
                                item.content.recipelInfo.chargeStatus !== 0 ||
                                item.content.recipelInfo.status === -1
                              "
                              @change="MedicalCalculate()"
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
                        </el-table-column> -->
                            <el-table-column prop="total" label="Tổng số lượng" align="center">
                              <template slot-scope="scope">
                                <span>{{ scope.row.total }} {{ scope.row.isUnpackSell === 1 ? scope.row.drugStuffId.preparationUnit.name : scope.row.drugStuffId.pack.name }}</span>
                              </template>
                            </el-table-column>
                            <el-table-column prop="isUnpackSell" label="Đơn giá"
                                             align="center">
                              <template slot-scope="scope">
                                <el-select v-model="scope.row.isUnpackSell" :disabled="
                                    isReadOnly ||
                                    item.content.recipelInfo.chargeStatus !==
                                      0 ||
                                    item.content.recipelInfo.status === -1
                                  " placeholder="Vui lòng chọn" @change="MedicalCalculate()" style="width: 100%">
                                  <el-option :label="
                                      (scope.row.drugStuffId.price).toFixed(4) +
                                      '/' +
                                      scope.row.drugStuffId.pack.name
                                    " :value="0"></el-option>
                                  <el-option :label="
                                      (scope.row.drugStuffId.retailPrice).toFixed(4) +
                                      '/' +
                                      scope.row.drugStuffId.preparationUnit.name
                                    " :value="1"></el-option>
                                </el-select>
                              </template>
                            </el-table-column>
                            <el-table-column prop="allFee" label="Số tiền"
                                             align="center"></el-table-column>
                            <el-table-column v-if="
                                !isReadOnly &&
                                item.content.recipelInfo.chargeStatus === 0 &&
                                item.content.recipelInfo.status !== -1
                              " label="Thao tác" align="center" width="50">
                              <template slot-scope="scope">
                                <i class="el-icon-circle-close" @click="
                                    DeleteMedicalRow(scope.$index, scope.row)
                                  "></i>
                              </template>
                            </el-table-column>
                          </el-table>
                        </el-row>
                      </div>
                    </div>
                  </div>
                  <!-- Hạng mục khám chữa bệnh -->
                  <div v-if="item.type === 'recipelType_3'" style="margin-top: 15px">
                    <el-row>
                      <el-divider content-position="left">Thông tin đơn thuốc</el-divider>
                      <el-popover placement="top-start" v-if="
                          !isReadOnly &&
                          item.content.recipelInfo.chargeStatus === 0 &&
                          item.content.recipelInfo.status !== -1
                        " width="540" trigger="focus">
                        <el-table :data="TreatmentTable" :height="300" border
                                  highlight-current-row @row-click="RowClickTreatmentTable">
                          <el-table-column prop="itemName" label="Tên hạng mục" width="200">
                          </el-table-column>
                          <el-table-column prop="itemType.name" label="Loại hạng mục" width="100">
                          </el-table-column>
                          <el-table-column prop="isPackage" label="Hạng mục gói dịch vụ" width="100">
                            <template slot-scope="scope">
                              {{ scope.row.isPackage === "1" ? "Có" : "Không" }}
                            </template>
                          </el-table-column>
                          <el-table-column prop="salePrice" label="Đơn giá" width="120">
                            <template slot-scope="scope">
                              {{ (scope.row.salePrice).toFixed(4) }}/{{
                                scope.row.unit.name
                              }}
                            </template>
                          </el-table-column>
                        </el-table>
                        <el-input prefix-icon="el-icon-plus" suffix-icon="el-icon-search"
                                  style="width: 30%" slot="reference" ref="TreatmentInput"
                                  v-model="SearchCostItemInput" @input="GetCostItemTable"
                                  @focus="GetCostItemTable" placeholder="Nhập tên hạng mục khám chữa bệnh hoặc mã pinyin"></el-input>
                      </el-popover>
                      <el-button type="primary" v-if="!isReadOnly" style="float: right" plain
                                 @click="historyRecipel(item.type, index)">Đơn thuốc cũ
                      </el-button>
                      <span style="float: right">&nbsp;&nbsp;&nbsp;</span>
                      <el-button type="primary" v-if="!isReadOnly"
                                 style="float: right; margin-left: 120px" plain
                                 @click="templateRecipel(item.type, index)">Đơn thuốc mẫu
                      </el-button>
                      <!-- <span style="float:right;">&nbsp;&nbsp;&nbsp;</span> -->
                      <el-button type="primary" v-if="!isReadOnly"
                                 style="float: right; margin-right: -105px" plain
                                 @click="saveTemplate(item.type, index)">Lưu thành mẫu
                      </el-button>
                    </el-row>
                    <el-row>
                      <el-table :data="
                          getDataFilterTable(
                            item.content.recipelDetailEvtList,
                            0,
                            item
                          )
                        " border style="width: 100%" class="tableStyle">
                        <el-table-column type="index" label="Số thứ tự" align="center">
                        </el-table-column>
                        <el-table-column prop="drugStuffId.name" label="Tên hạng mục" align="center">
                        </el-table-column>
                        <el-table-column prop="drugStuffId.costItem.itemType.name" label="Loại hạng mục"
                                         align="center"></el-table-column>
                        <el-table-column prop="drugStuffId.costItem.isPackage" label="Hạng mục gói dịch vụ"
                                         align="center">
                          <template slot-scope="scope">
                            {{
                              scope.row.drugStuffId.costItem.isPackage === "1"
                                ? "Có"
                                : "Không"
                            }}
                          </template>
                        </el-table-column>
                        <el-table-column prop="total" label="Số lượng" align="center">
                          <template slot-scope="scope">
                            {{ scope.row.total }} {{ scope.row.isUnpackSell === 1 ? scope.row.drugStuffId.preparationUnit.name : scope.row.drugStuffId.pack.name }}
                          </template>
                        </el-table-column>
                        <el-table-column label="Đơn giá" align="center">
                          <template slot-scope="scope">
                            {{ (scope.row.drugStuffId.price).toFixed(4) }}/{{
                              scope.row.drugStuffId.pack.name
                            }}
                          </template>
                        </el-table-column>
                        <el-table-column prop="allFee" label="Tổng số tiền" align="center">
                        </el-table-column>
                        <el-table-column v-if="
                            !isReadOnly &&
                            item.content.recipelInfo.chargeStatus === 0 &&
                            item.content.recipelInfo.status !== -1
                          " label="Thao tác" align="center" width="50">
                          <template slot-scope="scope">
                            <i class="el-icon-circle-close"
                               @click="DeleteMedicalRow(scope.$index, scope.row)"></i>
                          </template>
                        </el-table-column>
                        <el-table-column v-else-if="inspectionSign === 1" label="Thao tác" align="center" width="80">
                          <template slot-scope="scope">
                            <div v-if="inspectionType[scope.$index] === 1">
                              <el-button @click="lookInspection(scope.row.drugStuffId)"
                                         type="text">Xem
                              </el-button>
                            </div>
                            <div v-else>
                              <el-button type="text" disabled>Chưa điền</el-button>
                            </div>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-row>
                  </div>
                  <div style="margin-top: 10px">
                    <el-row>
                      <el-divider content-position="left">Phí phụ thêm</el-divider>
                      <el-popover
                          placement="top-start" width="550" trigger="focus" v-if="
                          !isReadOnly &&
                          item.content.recipelInfo.chargeStatus === 0 &&
                          item.content.recipelInfo.status !== -1
                        ">
                        <el-table :data="SurchargeTable" :max-height="300" border
                                  highlight-current-row @row-click="RowClickSurchargeTable">
                          <el-table-column label="Tên chi phí">
                            <template slot-scope="scope">
                              {{
                                scope.row.stuffType === "4"
                                  ? scope.row.stuff.name
                                  : scope.row.itemName
                              }}
                            </template>
                          </el-table-column>
                          <el-table-column label="Loại chi phí" width="130">
                            <template slot-scope="scope">
                              {{
                                scope.row.stuffType === "4"
                                  ? scope.row.stuff.type.name
                                  : "Khám chữa bệnh（Chi phí khác）"
                              }}
                            </template>
                          </el-table-column>
                          <el-table-column label="Giá bán" width="100">
                            <template slot-scope="scope">
                              {{
                                scope.row.stuffType === "4"
                                  ? (scope.row.stuff.priceOutSell).toFixed(2) +
                                  "/" +
                                  scope.row.stuff.packUnit.name
                                  : scope.row.salePrice +
                                  "/" +
                                  scope.row.unit.name
                              }}
                            </template>
                          </el-table-column>
                          <el-table-column label="Giá bán lẻ tách hộp" width="100">
                            <template slot-scope="scope">
                              {{
                                scope.row.stuffType === "4" &&
                                scope.row.stuff.isUnpackSell === 1
                                  ? scope.row.stuff.retailPrice +
                                  "/" +
                                  scope.row.stuff.minUnit.name
                                  : "--"
                              }}
                            </template>
                          </el-table-column>
                          <el-table-column label="Tồn kho" width="100">
                            <template slot-scope="scope">
                              {{
                                scope.row.stuffType === "4"
                                  ? Math.floor(
                                  scope.row.surplusStock /
                                  scope.row.stuff.packNumber
                                  ) > 0
                                  ? Math.floor(
                                  scope.row.surplusStock /
                                  scope.row.stuff.packNumber
                                  ) +
                                  scope.row.stuff.packUnit.name +
                                  (scope.row.surplusStock %
                                  scope.row.stuff.packNumber >
                                  0
                                    ? (scope.row.surplusStock %
                                    scope.row.stuff.packNumber) +
                                    scope.row.stuff.minUnit.name
                                    : "")
                                  : scope.row.surplusStock +
                                  scope.row.stuff.minUnit.name
                                  : "Không giới hạn"
                              }}
                            </template>
                          </el-table-column>
                        </el-table>
                        <el-input prefix-icon="el-icon-plus" suffix-icon="el-icon-search"
                                  id = 'fjinput'
                                  style="width: 30%" slot="reference" v-model="SearchSurchargeInput"
                                  @input="GetSurchargeTable" @focus="GetSurchargeTable"
                                  placeholder="Nhập tên phí phụ thêm hoặc mã pinyin">
                        </el-input>
                      </el-popover>
                    </el-row>
                    <div v-if="item.type !== 'recipelType_2'">
                      <el-row>
                        <el-table :data="
                            getDataFilterTable(
                              item.content.recipelDetailEvtList,
                              1,
                              item
                            )
                          " border style="width: 100%" class="tableStyle">
                          <el-table-column type="index" label="Số thứ tự" width="50" align="center">
                          </el-table-column>
                          <el-table-column prop="drugStuffId.name" label="Tên chi phí" width="200"
                                           align="center"></el-table-column>
                          <el-table-column label="Loại chi phí" width="130" align="center">
                            <template slot-scope="scope">
                              {{
                                scope.row.stuffType === "4"
                                  ? scope.row.drugStuffId.stuff.type.name
                                  : "Khám chữa bệnh（Chi phí khác）"
                              }}
                            </template>
                          </el-table-column>
                          <el-table-column prop="price" label="Đơn giá" width="100" align="center">
                            <template slot-scope="scope">
															<span v-if="scope.row.stuffType === '3'">
																{{
                                  (scope.row.drugStuffId.price).toFixed(4) +
                                  "/" +
                                  scope.row.drugStuffId.pack.name
                                }}
															</span>
                              <span v-if="scope.row.stuffType === '4'">
																{{
                                  scope.row.isUnpackSell === 0
                                    ? (scope.row.drugStuffId.price).toFixed(4) +
                                    "/" +
                                    scope.row.drugStuffId.pack.name
                                    : scope.row.drugStuffId.retailPrice +
                                    "/" +
                                    scope.row.drugStuffId.preparationUnit.name
                                }}
															</span>
                            </template>
                          </el-table-column>
                          <el-table-column label="Số lượng" align="center" width="200">
                            <template slot-scope="scope">
                              <el-input :disabled="
                                  isReadOnly ||
                                  item.content.recipelInfo.chargeStatus !== 0 ||
                                  item.content.recipelInfo.status === -1
                                " ref="additionalCharge" v-model="scope.row.singleDosage" @input="MedicalCalculate()"
                                        style="width: 60px">
                              </el-input>
                              <el-select :disabled="
                                  isReadOnly ||
                                  item.content.recipelInfo.chargeStatus !== 0 ||
                                  item.content.recipelInfo.status === -1
                                " v-model="scope.row.isUnpackSell" @change="handleUnpackSellChange(scope.row, scope.row.isUnpackSell === 1 ? 0 : 1)" placeholder="Vui lòng chọn"
                                         style="width: 80px">
                                <el-option :label="scope.row.drugStuffId.pack.name"
                                           :value="0"></el-option>
                                <el-option
                                  v-if="scope.row.drugStuffId.isUnpackSell === 1"
                                  :label="
                                    scope.row.drugStuffId.preparationUnit.name
                                  " :value="1"></el-option>
                              </el-select>
                            </template>
                          </el-table-column>
                          <el-table-column prop="allFee" label="Số tiền" align="center">
                          </el-table-column>
                          <el-table-column v-if="
                              !isReadOnly &&
                              item.content.recipelInfo.chargeStatus === 0 &&
                              item.content.recipelInfo.status !== -1
                            " label="Thao tác" width="80" align="center">
                            <template slot-scope="scope">
                              <i class="el-icon-circle-close" @click="
                                  DeleteMedicalRow(scope.$index, scope.row)
                                "></i>
                            </template>
                          </el-table-column>
                        </el-table>
                      </el-row>
                    </div>
                    <div v-else>
                      <el-row>
                        <el-table :data="
                            getDataFilterTable(item.infusion.excharge, 1, item)
                          " border style="width: 100%" class="tableStyle">
                          <el-table-column type="index" label="Số thứ tự" width="50" align="center">
                          </el-table-column>
                          <el-table-column prop="drugStuffId.name" label="Tên chi phí" width="200"
                                           align="center"></el-table-column>
                          <el-table-column label="Loại chi phí" width="130" align="center">
                            <template slot-scope="scope">
                              {{
                                scope.row.stuffType === "4"
                                  ? scope.row.drugStuffId.stuff.type.name
                                  : "Khám chữa bệnh（Chi phí khác）"
                              }}
                            </template>
                          </el-table-column>
                          <el-table-column prop="price" label="Đơn giá" width="120" align="center">
                            <template slot-scope="scope">
															<span v-if="scope.row.stuffType === '3'">
																{{
                                  (scope.row.drugStuffId.price).toFixed(4) +
                                  "/" +
                                  scope.row.drugStuffId.pack.name
                                }}
															</span>
                              <span v-if="scope.row.stuffType === '4'">
																{{
                                  scope.row.isUnpackSell === 0
                                    ? (scope.row.drugStuffId.price).toFixed(4) +
                                    "/" +
                                    scope.row.drugStuffId.pack.name
                                    : scope.row.drugStuffId.retailPrice +
                                    "/" +
                                    scope.row.drugStuffId.preparationUnit.name
                                }}
															</span>
                            </template>
                          </el-table-column>
                          <el-table-column label="Số lượng" align="center" width="200">
                            <template slot-scope="scope">
                              <el-input :disabled="
                                  isReadOnly ||
                                  item.content.recipelInfo.chargeStatus !== 0 ||
                                  item.content.recipelInfo.status === -1
                                " ref="additionalCharge" v-model="scope.row.singleDosage" @input="MedicalCalculate()"
                                        style="width: 60px">
                              </el-input>
                              <el-select :disabled="
                                  isReadOnly ||
                                  item.content.recipelInfo.chargeStatus !== 0 ||
                                  item.content.recipelInfo.status === -1
                                " v-model="scope.row.isUnpackSell" @change="handleUnpackSellChange(scope.row, scope.row.isUnpackSell === 1 ? 0 : 1)" placeholder="Vui lòng chọn"
                                         style="width: 80px">
                                <el-option :label="scope.row.drugStuffId.pack.name"
                                           :value="0"></el-option>
                                <el-option
                                  v-if="scope.row.drugStuffId.isUnpackSell === 1"
                                  :label="
                                    scope.row.drugStuffId.preparationUnit.name
                                  " :value="1"></el-option>
                              </el-select>
                            </template>
                          </el-table-column>
                          <el-table-column prop="allFee" label="Số tiền" align="center">
                          </el-table-column>
                          <el-table-column v-if="
                              !isReadOnly &&
                              item.content.recipelInfo.chargeStatus === 0 &&
                              item.content.recipelInfo.status !== -1
                            " label="Thao tác" width="80" align="center">
                            <template slot-scope="scope">
                              <i class="el-icon-circle-close" @click="
                                  DeleteExMedicalRow(scope.$index, scope.row)
                                "></i>
                            </template>
                          </el-table-column>
                        </el-table>
                      </el-row>
                    </div>
                    <el-row style="margin-top: 10px">
                      <el-divider content-position="left">Hạng mục y lệnh</el-divider>
                      <el-input type="textarea" :rows="4" placeholder="Vui lòng nhập nội dung y lệnh"
                                v-model="item.content.recipelInfo.entrust" :disabled="
                          isReadOnly ||
                          item.content.recipelInfo.chargeStatus !== 0 ||
                          item.content.recipelInfo.status === -1
                        ">
                      </el-input>
                    </el-row>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </el-card>
          <div style="height: 10px"></div>
        </el-scrollbar>
      </el-card>
      <el-card class="box-card sub-card" style="height: 50px">
        <span style="line-height: 27px">Tổng số tiền：{{ payAmount }}VNĐ</span>
        <div style="float: right">
          <el-button type="primary" plain v-if="
              medicalEditTabsValue.content &&
              medicalEditTabsValue.content.recipelInfo.status === 1 &&
              medicalEditTabsValue.content.recipelInfo.chargeStatus === 0 &&
              TodayActiveName === 'already'
            " @click="invalidMedical">Hủy bỏ
          </el-button>
          <el-button :type="timeOut ? 'info' : 'primary'" v-if="isReadOnly && TodayActiveName === 'already'"
                     @click="edit">Sửa
          </el-button>
          <el-button type="primary" v-if="!isReadOnly && TodayActiveName === 'already'" @click="save(2)">Lưu
          </el-button>
          <el-button type="primary" v-if="hasBtnPrint" @click="print">In</el-button>
          <el-button v-show="hasBtnFinishVisit" type="primary" @click="save(1)">Hoàn thành tiếp nhận khám</el-button>
        </div>
      </el-card>
    </el-main>
    <!-- 历史处方弹出框 -->
    <el-dialog title="Đơn thuốc cũ" :visible.sync="historyDialogVisible" width="60%" class="ones"
               :before-close="handleClose">
      <div style="border: 1px solid #eee9e9; margin-top: -30px"></div>
      <br/>
      <div>
        <el-row>
          <el-col :span="24 / 2">
            <el-form :model="queryModel" @submit.native.prevent label-position="left" label-width="70px"
                     ref="queryForm" :inline-message="true">
              <el-form-item label="Chọn ngày" prop="time">
                <el-date-picker v-model="queryModel.dateRange" type="datetimerange" range-separator="đến"
                                format="yyyy-MM-dd HH:mm:ss">
                </el-date-picker>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="24 / 4"></el-col>
          <el-col :span="24 / 4" style="float: right">
            <!-- <div class="bg_btn">
                <el-button type="primary" @click="onSearch"
                  >Truy vấn</el-button
                >
                <el-button @click="resetCondition()">Đặt lại</el-button>
              </div> -->
            <el-button type="primary" icon="el-icon-search" @click="onSearch" :plain="true">Tìm kiếm</el-button>
            <el-button type="info" icon="el-icon-refresh-left" @click="resetCondition" :plain="true">Đặt lại
            </el-button>
          </el-col>
        </el-row>
        <el-row></el-row>
      </div>
      <br/>
      <el-table :data="historyRecipelList" style="width: 100%" :row-key="getRowKeys" :expand-row-keys="expands"
                @expand-change="expandSelect">
        <el-table-column type="expand">
          <template slot-scope="props">
            <!-- Đơn thuốc tây -->
            <div class="two" v-if="props.row.recipelType.value === 'recipelType_0'">
              <el-table :data="filterRecipel(props.row.recipelDetail)" style="width: 100%"
                        header-cell-style="{
      background:'#e2d2d2'}">
                <el-table-column label="Số thứ tự" type="index" align="center">
                  <template slot-scope="scope">
										<span v-if="scope.row.isExtra === 0">
											{{ scope.$index + 1 }}
										</span>
                  </template>
                </el-table-column>
                <el-table-column prop="drugStuffId.name" label="Thông tin thuốc" width="width">
                  <template slot-scope="scope">
										<span v-if="
                        scope.row.isExtra === 0 && scope.row.stuffType === '0'
                      ">
											{{ scope.row.drugStuffId.name }}
										</span>
                  </template>
                </el-table-column>
                <el-table-column prop="unitPrice" label="Đơn giá" width="120">
                  <template slot-scope="scope">
										<span v-if="
                        (scope.row.isExtra === 0 &&
                          scope.row.isUnpackSell === 0 &&
                          scope.row.stuffType === '0') ||
                        scope.row.stuffType === '2'
                      ">
											{{ (scope.row.unitPrice).toFixed(4) }}/{{
                        scope.row.drugStuffId.drug.pack.name
                      }}
										</span>
                    <span v-else-if="
                        (scope.row.isExtra === 0 &&
                          scope.row.isUnpackSell === 1 &&
                          scope.row.stuffType === '0') ||
                        scope.row.stuffType === '2'
                      ">
											{{ (scope.row.unitPrice).toFixed(4) }}/{{
                        scope.row.drugStuffId.drug.preparationUnit.name
                      }}
										</span>
                  </template>
                </el-table-column>
                <el-table-column prop="singleDosage" label="Liều dùng mỗi lần" width="width">
                  <template slot-scope="scope">
                    <template v-if="scope.row.isExtra === 0">{{
                        scope.row.singleDosage +
                        "" +
                        scope.row.drugStuffId.preparationUnit.name
                      }}
                    </template>
                  </template>
                </el-table-column>
                <el-table-column prop="westernMedicineUse.name" label="Cách dùng" width="width">
                  <template slot-scope="scope">
										<span v-if="
                        scope.row.westernMedicineUse !== undefined &&
                        scope.row.isExtra === 0
                      ">
											{{ scope.row.westernMedicineUse.name }}
										</span>
                  </template>
                </el-table-column>
                <el-table-column prop="frequency.name" label="Tần suất" width="width">
                  <template slot-scope="scope">
										<span v-if="scope.row.isExtra === 0">
											{{ scope.row.frequency.name }}
										</span>
                  </template>
                </el-table-column>

                <!-- <el-table-column prop="drippingSpeed" label="Tốc độ truyền" width="width">
                        <template slot-scope="scope">
                        <template>{{scope.row.drippingSpeed?scope.row.drippingSpeed:0}}giọt/Phút</template>
                      </template>
                    </el-table-column> -->
                <el-table-column prop="days.name" label="Số ngày" width="width">
                  <template slot-scope="scope">
                    <template v-if="scope.row.isExtra === 0">{{ scope.row.days.name }}ngày</template>
                  </template>
                </el-table-column>
                <el-table-column prop="total" label="Tổng số lượng" width="width">
                  <template slot-scope="scope">
                    <template v-if="
                        scope.row.isExtra === 0 &&
                        scope.row.stuffType !== '4' &&
                        scope.row.stuffType !== '3'
                      ">
                      {{
                        Math.floor(
                          scope.row.total /
                          scope.row.drugStuffId.drug.preparation -
                          0
                        ) >= 0
                          ? Math.floor(
                          scope.row.total /
                          scope.row.drugStuffId.drug.preparation -
                          0
                          ) +
                          scope.row.drugStuffId.drug.pack.name +
                          ((scope.row.total %
                            scope.row.drugStuffId.drug.preparation) -
                          0 >
                          0
                            ? (scope.row.total %
                            scope.row.drugStuffId.drug.preparation) -
                            0 +
                            scope.row.drugStuffId.drug.preparationUnit.name
                            : "")
                          : scope.row.total +
                          scope.row.drugStuffId.drug.preparationUnit.name
                      }}
                    </template>
                  </template>
                </el-table-column>
              </el-table>
              <!-- <br>
         <el-divider style="margin-top: 20px" content-position="left"
                    >Phí phụ thêm</el-divider
                  >
                  <el-table :data="filterAdditional(props.row.recipelDetail)" style="width: 100%">
                    <el-table-column label="Số thứ tự" type="index" align="center" width="width">

                    </el-table-column>
                    <el-table-column
                      prop="drugStuffId.name"
                      label="Tên"
                      width="width"
                    >
                    <template slot-scope="scope">
                        <span >
                          {{scope.row.drugStuffId.name}}
                        </span>
                      </template>
                    </el-table-column>

                    <el-table-column
                      prop="unitPrice"
                      label="Đơn giá"
                      width="width"
                    >
                      <template slot-scope="scope">
                      <span
                        >{{ scope.row.unitPrice }}VNĐ/{{
                          scope.row.isUnpackSell === 1
                            ? scope.row.drugStuffId.preparationUnit.name
                            : scope.row.drugStuffId.pack.name
                        }}</span
                      >
                    </template>
                    </el-table-column>
                    <el-table-column prop="total" label="Số lượng" width="width">
                    <template slot-scope="scope">
                        <span v-if="scope.row.drugStuffId.costItem != null">
                          {{ scope.row.total
                          }}{{ scope.row.drugStuffId.dosisUnit.name }}
                        </span>
                        <span v-else-if="scope.row.drugStuffId.stuff != null">
                          {{
                            Math.floor(
                              scope.row.total / scope.row.drugStuffId.stuff.packNumber
                            ) > 0
                              ? Math.floor(
                                  scope.row.total /
                                    scope.row.drugStuffId.stuff.packNumber
                                ) +
                                scope.row.drugStuffId.stuff.packUnit.name +
                                (scope.row.total %
                                  scope.row.drugStuffId.stuff.packNumber >
                                0
                                  ? (scope.row.total %
                                      scope.row.drugStuffId.stuff.packNumber) +
                                    scope.row.drugStuffId.stuff.minUnit.name
                                  : "")
                              : scope.row.total +
                                scope.row.drugStuffId.stuff.minUnit.name
                          }}
                        </span>
                        <span v-else-if="scope.row.drugStuffId.drug != null">
                          {{
                            Math.floor(
                              scope.row.total /
                                (scope.row.drugStuffId.drug.preparation - 0)
                            ) > 0
                              ? Math.floor(
                                  scope.row.total /
                                    (scope.row.drugStuffId.drug.preparation - 0)
                                ) +
                                scope.row.drugStuffId.drug.pack.name +
                                (scope.row.total %
                                  (scope.row.drugStuffId.drug.preparation - 0) >
                                0
                                  ? (scope.row.total %
                                      (scope.row.drugStuffId.drug.preparation - 0)) +
                                    scope.row.drugStuffId.drug.preparationUnit.name
                                  : "")
                              : scope.row.total +
                                scope.row.drugStuffId.drug.preparationUnit.name
                          }}
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="allFee" label="Tổng tiền" width="width">
                      <template slot-scope="scope">
                        <span >
                          {{scope.row.allFee}}VNĐ
                        </span>

                      </template>
                    </el-table-column>
                  </el-table> -->
            </div>

            <!-- Đơn thuốc đông y -->
            <div class="two" v-else-if="props.row.recipelType.value === 'recipelType_1'">
              <el-table :data="filterRecipel(props.row.recipelDetail)" style="width: 100%">
                <el-table-column label="Số thứ tự" type="index" align="center">
                </el-table-column>
                <el-table-column prop="drugStuffId.name" label="Thông tin thuốc" width="width">
                </el-table-column>
                <el-table-column prop="unitPrice" label="Đơn giá" width="120">
                  <template slot-scope="scope">
										<span v-if="
                        scope.row.isUnpackSell === 0 &&
                        scope.row.stuffType === '1'
                      ">
											{{ (scope.row.unitPrice).toFixed(4) }}/{{
                        scope.row.drugStuffId.drug.pack.name
                      }}
										</span>
                    <span v-else-if="
                        scope.row.isUnpackSell === 1 &&
                        scope.row.stuffType === '1'
                      ">
											{{ (scope.row.unitPrice).toFixed(4) }}/{{
                        scope.row.drugStuffId.drug.preparationUnit.name
                      }}
										</span>
                  </template>
                </el-table-column>
                <el-table-column prop="singleDosage" label="Liều dùng mỗi lần" width="width">
                  <template slot-scope="scope">
                    <template v-if="scope.row.stuffType === '1'">{{
                        scope.row.singleDosage +
                        "" +
                        scope.row.drugStuffId.preparationUnit.name
                      }}
                    </template>
                  </template>
                </el-table-column>
                <el-table-column prop="chineseMedicineUse.name" label="Cách dùng" width="width">
                  <template slot-scope="scope">
                    <template v-if="scope.row.stuffType === '1'">{{
                        scope.row.chineseMedicineUse.name
                      }}
                    </template>
                  </template>
                </el-table-column>

                <!-- <el-table-column prop="drippingSpeed" label="Tốc độ truyền" width="width">
                        <template slot-scope="scope">
                        <template>{{scope.row.drippingSpeed?scope.row.drippingSpeed:0}}giọt/Phút</template>
                      </template>
                    </el-table-column> -->
                <el-table-column prop="total" label="Tổng số lượng" width="width">
                  <template slot-scope="scope">
                    <template v-if="scope.row.stuffType === '1'">
                      {{
                        Math.floor(
                          scope.row.total /
                          scope.row.drugStuffId.drug.preparation -
                          0
                        ) >= 0
                          ? Math.floor(
                          scope.row.total /
                          scope.row.drugStuffId.drug.preparation -
                          0
                          ) +
                          scope.row.drugStuffId.drug.pack.name +
                          ((scope.row.total %
                            scope.row.drugStuffId.drug.preparation) -
                          0 >
                          0
                            ? (scope.row.total %
                            scope.row.drugStuffId.drug.preparation) -
                            0 +
                            scope.row.drugStuffId.drug.preparationUnit.name
                            : "")
                          : scope.row.total +
                          scope.row.drugStuffId.drug.preparationUnit.name
                      }}
                    </template>
                  </template>
                </el-table-column>
              </el-table>
              <div style="margin: 10px 0">
                <el-input style="width: 50px" disabled v-model="props.row.dosage">
                </el-input>
                thang Cách dùng：
                <el-input style="width: 100px" disabled v-model="props.row.recipelUse.name">
                </el-input>
                <el-input style="width: 100px" disabled v-model="props.row.takeFrequency.name">
                </el-input>
                <el-input style="width: 100px" disabled v-model="props.row.frequency.name">
                </el-input>
                Mỗi lần uống
                <el-input style="width: 80px" disabled v-model="props.row.singleDosage">
                </el-input>
                {{ "ml" }}
              </div>
              <!-- <br>
         <el-divider style="margin-top: 20px" content-position="left"
                    >Phí phụ thêm</el-divider
                  >
                  <el-table :data="filterAdditional(props.row.recipelDetail)" style="width: 100%">
                    <el-table-column label="Số thứ tự" type="index" align="center" width="width">

                    </el-table-column>
                    <el-table-column
                      prop="drugStuffId.name"
                      label="Tên"
                      width="width"
                    >
                    <template slot-scope="scope">
                        <span >
                          {{scope.row.drugStuffId.name}}
                        </span>
                      </template>
                    </el-table-column>

                    <el-table-column
                      prop="unitPrice"
                      label="Đơn giá"
                      width="width"
                    >
                      <template slot-scope="scope">
                      <span
                        >{{ scope.row.unitPrice }}VNĐ/{{
                          scope.row.isUnpackSell === 1
                            ? scope.row.drugStuffId.preparationUnit.name
                            : scope.row.drugStuffId.pack.name
                        }}</span
                      >
                    </template>
                    </el-table-column>
                    <el-table-column prop="total" label="Số lượng" width="width">
                    <template slot-scope="scope">
                        <span v-if="scope.row.drugStuffId.costItem != null">
                          {{ scope.row.total
                          }}{{ scope.row.drugStuffId.dosisUnit.name }}
                        </span>
                        <span v-else-if="scope.row.drugStuffId.stuff != null">
                          {{
                            Math.floor(
                              scope.row.total / scope.row.drugStuffId.stuff.packNumber
                            ) > 0
                              ? Math.floor(
                                  scope.row.total /
                                    scope.row.drugStuffId.stuff.packNumber
                                ) +
                                scope.row.drugStuffId.stuff.packUnit.name +
                                (scope.row.total %
                                  scope.row.drugStuffId.stuff.packNumber >
                                0
                                  ? (scope.row.total %
                                      scope.row.drugStuffId.stuff.packNumber) +
                                    scope.row.drugStuffId.stuff.minUnit.name
                                  : "")
                              : scope.row.total +
                                scope.row.drugStuffId.stuff.minUnit.name
                          }}
                        </span>
                        <span v-else-if="scope.row.drugStuffId.drug != null">
                          {{
                            Math.floor(
                              scope.row.total /
                                (scope.row.drugStuffId.drug.preparation - 0)
                            ) > 0
                              ? Math.floor(
                                  scope.row.total /
                                    (scope.row.drugStuffId.drug.preparation - 0)
                                ) +
                                scope.row.drugStuffId.drug.pack.name +
                                (scope.row.total %
                                  (scope.row.drugStuffId.drug.preparation - 0) >
                                0
                                  ? (scope.row.total %
                                      (scope.row.drugStuffId.drug.preparation - 0)) +
                                    scope.row.drugStuffId.drug.preparationUnit.name
                                  : "")
                              : scope.row.total +
                                scope.row.drugStuffId.drug.preparationUnit.name
                          }}
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="allFee" label="Tổng tiền" width="width">
                      <template slot-scope="scope">
                        <span >
                          {{scope.row.allFee}}VNĐ
                        </span>

                      </template>
                    </el-table-column>
                  </el-table> -->
            </div>

            <!-- Đơn truyền dịch -->
            <div class="two" style="border: 1px solid #dcdcdc"
                 v-else-if="props.row.recipelType.value === 'recipelType_2'">
              <div v-for="(items, index) in getInfuSion(props.row.recipelDetail)
                  .zushu" :key="index" style="border: 1px solid #dcdcdc; padding: 10px; margin: 10px">
                <div>
									<span style="
                      float: left;
                      height: 25px;
                      line-height: 25px;
                      font-size: 14px;
                    ">&nbsp;&nbsp;thứ{{ index + 1 }}nhóm</span>
                  <span style="
                      float: right;
                      height: 25px;
                      line-height: 25px;
                      font-size: 14px;
                    ">&nbsp;&nbsp;giọt/Phút</span>

                  <el-input style="float: right; width: 10%" :value="
                      getInfuSion(props.row.recipelDetail).drippingSpeed[index]
                    " placeholder="" :disabled="true">
                  </el-input>

                  <span style="
                      float: right;
                      height: 25px;
                      line-height: 25px;
                      font-size: 14px;
                    ">&nbsp;&nbsp;ngày&nbsp;&nbsp;</span>
                  <el-input style="float: right; width: 10%" :value="
                      getInfuSion(props.row.recipelDetail).days[index].name
                    " placeholder="" :disabled="true">
                  </el-input>

                  <span style="
                      float: right;
                      height: 25px;
                      line-height: 25px;
                      font-size: 18px;
                    ">&nbsp;&nbsp;</span>
                  <el-input style="float: right; width: 10%" :value="
                      getInfuSion(props.row.recipelDetail).frequency[index].name
                    " placeholder="" :disabled="true">
                  </el-input>

                  <span style="
                      float: right;
                      height: 25px;
                      line-height: 25px;
                      font-size: 18px;
                    ">&nbsp;&nbsp;</span>
                  <el-input style="float: right; width: 10%" :value="
                      getInfuSion(props.row.recipelDetail).infuseUse[index].name
                    " placeholder="" :disabled="true">
                  </el-input>
                </div>
                <div style="border: 17px solid #0000"></div>
                <el-table :data="
                    getInfuSion(props.row.recipelDetail).infusionProject[index]
                  " style="width: 100%">
                  <el-table-column label="Số thứ tự" type="index" align="center" width="width">
                  </el-table-column>
                  <el-table-column prop="drugStuffId.name" label="Tên thuốc" width="width">
                  </el-table-column>
                  <el-table-column prop="skinTest.name" label="Test da" width="width">
                  </el-table-column>
                  <el-table-column prop="drugStuffId.drug.factory.name" label="Nhà sản xuất" width="width">
                  </el-table-column>
                  <el-table-column prop="unitPrice" label="Đơn giá" width="120">
                    <template slot-scope="scope">
											<span>{{ (scope.row.unitPrice).toFixed(4) }}VNĐ/{{
                          scope.row.isUnpackSell === 1
                            ? scope.row.drugStuffId.preparationUnit.name
                            : scope.row.drugStuffId.pack.name
                        }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="singleDosage" label="Liều dùng mỗi lần" width="width">
                    <template slot-scope="scope">
                      <template>{{
                          scope.row.singleDosage +
                          "" +
                          scope.row.drugStuffId.preparationUnit.name
                        }}
                      </template>
                    </template>
                  </el-table-column>
                  <el-table-column prop="total" label="Số lượng" width="150">
                    <template slot-scope="scope">
											<span v-if="scope.row.drugStuffId.costItem != null">
												{{
                          scope.row.total
                        }}{{ scope.row.drugStuffId.dosisUnit.name }}
											</span>
                      <span v-else-if="scope.row.drugStuffId.stuff != null">
												{{
                          Math.floor(
                            scope.row.total /
                            scope.row.drugStuffId.stuff.packNumber
                          ) > 0
                            ? Math.floor(
                            scope.row.total /
                            scope.row.drugStuffId.stuff.packNumber
                            ) +
                            scope.row.drugStuffId.stuff.packUnit.name +
                            (scope.row.total %
                            scope.row.drugStuffId.stuff.packNumber >
                            0
                              ? (scope.row.total %
                              scope.row.drugStuffId.stuff.packNumber) +
                              scope.row.drugStuffId.stuff.minUnit.name
                              : "")
                            : scope.row.total +
                            scope.row.drugStuffId.stuff.minUnit.name
                        }}
											</span>
                      <span v-else-if="scope.row.drugStuffId.drug != null">
												{{
                          Math.floor(
                            scope.row.total /
                            (scope.row.drugStuffId.drug.preparation - 0)
                          ) > 0
                            ? Math.floor(
                            scope.row.total /
                            (scope.row.drugStuffId.drug.preparation - 0)
                            ) +
                            scope.row.drugStuffId.drug.pack.name +
                            (scope.row.total %
                            (scope.row.drugStuffId.drug.preparation - 0) >
                            0
                              ? (scope.row.total %
                              (scope.row.drugStuffId.drug.preparation -
                                0)) +
                              scope.row.drugStuffId.drug.preparationUnit
                                .name
                              : "")
                            : scope.row.total +
                            scope.row.drugStuffId.drug.preparationUnit.name
                        }}
											</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="allFee" label="Tổng tiền" width="width">
                    <template slot-scope="scope">
                      {{ scope.row.allFee }}VNĐ
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
            <!-- Hạng mục khám chữa bệnh -->

            <div class="two" v-else-if="props.row.recipelType.value === 'recipelType_3'">
              <el-table :data="filterRecipel(props.row.recipelDetail)" style="width: 100%">
                <el-table-column type="index" label="Số thứ tự" width="50" align="center"></el-table-column>
                <el-table-column prop="drugStuffId.name" label="Tên hạng mục" align="center"></el-table-column>
                <el-table-column prop="drugStuffId.costItem.itemType.name" label="Loại hạng mục" align="center"
                                 width="100"></el-table-column>
                <el-table-column prop="drugStuffId.costItem.isPackage" label="Hạng mục gói dịch vụ" align="center"
                                 width="100">
                  <template slot-scope="scope">
                    {{
                      scope.row.drugStuffId.costItem.isPackage === "1"
                        ? "Có"
                        : "Không"
                    }}
                  </template>
                </el-table-column>
                <el-table-column prop="total" label="Số lượng" align="center" width="120">
                  <template slot-scope="scope">
                    {{ scope.row.total }} {{ scope.row.drugStuffId.pack.name }}
                  </template>
                </el-table-column>
                <el-table-column label="Đơn giá" width="120" align="center">
                  <template slot-scope="scope">
                    {{ (scope.row.drugStuffId.price).toFixed(4) }}/{{
                      scope.row.drugStuffId.pack.name
                    }}
                  </template>
                </el-table-column>
                <el-table-column prop="allFee" label="Tổng số tiền" width="120" align="center">
                </el-table-column>
              </el-table>
            </div>
            <br/>
            <div class="two" style="margin: 10px">
              <el-divider style="margin-top: 20px" content-position="left">Phí phụ thêm</el-divider>
              <el-table :data="filterAdditional(props.row.recipelDetail)" style="width: 100%">
                <el-table-column label="Số thứ tự" type="index" align="center" width="width">
                </el-table-column>
                <el-table-column prop="drugStuffId.name" label="Tên" width="width">
                  <template slot-scope="scope">
										<span>
											{{ scope.row.drugStuffId.name }}
										</span>
                  </template>
                </el-table-column>

                <el-table-column prop="unitPrice" label="Đơn giá" width="120">
                  <template slot-scope="scope">
										<span>{{ (scope.row.unitPrice).toFixed(4) }}VNĐ/{{
                        scope.row.isUnpackSell === 1
                          ? scope.row.drugStuffId.preparationUnit.name
                          : scope.row.drugStuffId.pack.name
                      }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="total" label="Số lượng" width="width">
                  <template slot-scope="scope">
										<span v-if="scope.row.drugStuffId.costItem != null">
											{{
                        scope.row.total
                      }}{{ scope.row.drugStuffId.dosisUnit.name }}
										</span>
                    <span v-else-if="scope.row.drugStuffId.stuff != null">
											{{
                        Math.floor(
                          scope.row.total /
                          scope.row.drugStuffId.stuff.packNumber
                        ) > 0
                          ? Math.floor(
                          scope.row.total /
                          scope.row.drugStuffId.stuff.packNumber
                          ) +
                          scope.row.drugStuffId.stuff.packUnit.name +
                          (scope.row.total %
                          scope.row.drugStuffId.stuff.packNumber >
                          0
                            ? (scope.row.total %
                            scope.row.drugStuffId.stuff.packNumber) +
                            scope.row.drugStuffId.stuff.minUnit.name
                            : "")
                          : scope.row.total +
                          scope.row.drugStuffId.stuff.minUnit.name
                      }}
										</span>
                    <span v-else-if="scope.row.drugStuffId.drug != null">
											{{
                        Math.floor(
                          scope.row.total /
                          (scope.row.drugStuffId.drug.preparation - 0)
                        ) > 0
                          ? Math.floor(
                          scope.row.total /
                          (scope.row.drugStuffId.drug.preparation - 0)
                          ) +
                          scope.row.drugStuffId.drug.pack.name +
                          (scope.row.total %
                          (scope.row.drugStuffId.drug.preparation - 0) >
                          0
                            ? (scope.row.total %
                            (scope.row.drugStuffId.drug.preparation -
                              0)) +
                            scope.row.drugStuffId.drug.preparationUnit.name
                            : "")
                          : scope.row.total +
                          scope.row.drugStuffId.drug.preparationUnit.name
                      }}
										</span>
                  </template>
                </el-table-column>
                <el-table-column prop="allFee" label="Tổng tiền" width="width">
                  <template slot-scope="scope">
                    <span> {{ scope.row.allFee }}VNĐ </span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-row>
              <el-divider content-position="left">Hạng mục y lệnh</el-divider>
              <el-input type="textarea" :rows="4" v-model="props.row.entrust" disabled>
              </el-input>
            </el-row>
          </template>
        </el-table-column>
        <el-table-column label="Số thứ tự" type="index"></el-table-column>
        <el-table-column prop="patient.name" label="Họ tên"></el-table-column>
        <el-table-column prop="patient.gender.name" label="Giới tính">
        </el-table-column>
        <el-table-column prop="patient.age" label="Tuổi"></el-table-column>
        <el-table-column prop="name" label="Tên đơn thuốc"></el-table-column>
        <el-table-column prop="medicalRecord.westernDiagnose" label="Chẩn đoán tây y">
        </el-table-column>
        <el-table-column prop="medicalRecord.chinaDiagnose" label="Chẩn đoán đông y">
        </el-table-column>
        <el-table-column prop="updateDate" label="Thời gian tiếp nhận khám"></el-table-column>

        <el-table-column prop="updateBy" label="Họ tên bác sĩ">
          <template slot-scope="scope">
            {{ GetCreateBy(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column prop="" label="Thao tác">
          <template slot-scope="scope">
            <el-button type="text" @click="invoking(scope.row)">Gọi</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- <span slot="footer" class="dialog-footer">
        <el-button @click="historyDialogVisible = false">Hủy</el-button>
        <el-button type="primary" @click="historyDialogVisible = false">Xác nhận</el-button>
      </span> -->
    </el-dialog>

    <el-dialog title="Bệnh án cũ" :visible.sync="caseDialogVisible" width="60%" class="ones"
               :before-close="handleClose">
      <div style="border: 1px solid #eee9e9; margin-top: -30px"></div>
      <br/>
      <div>
        <el-row>
          <el-col :span="24 / 2">
            <el-form :model="queryModel" @submit.native.prevent label-position="left" label-width="70px"
                     ref="queryForm" :inline-message="true">
              <el-form-item label="Chọn ngày" prop="time">
                <el-date-picker v-model="queryModel.dateRange" type="datetimerange" range-separator="đến"
                                format="yyyy-MM-dd HH:mm:ss">
                </el-date-picker>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="24 / 4"></el-col>
          <el-col :span="24 / 4" style="float: right">
            <el-button type="primary" icon="el-icon-search" @click="medicalHistoryOnSearch" :plain="true">Tìm kiếm</el-button>
            <el-button type="info" icon="el-icon-refresh-left" @click="medicalHistoryReset" :plain="true">Đặt lại
            </el-button>
          </el-col>
        </el-row>
        <el-row></el-row>
      </div>
      <br/>
      <el-table :data="historicalCasesList" style="width: 100%" :row-key="getRowKeys" :expand-row-keys="expands"
                @expand-change="expandSelect">
        <el-table-column type="expand">
          <template slot-scope="props">
            <el-row v-if="props.row.medicalRecord.patientTell">
              <el-divider content-position="left">Lý do khám chính</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.patientTell" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.personalHistory">
              <el-divider content-position="left">Tiền sử cá nhân</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.personalHistory" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.allergyHistory">
              <el-divider content-position="left">Tiền sử dị ứng</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.allergyHistory" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.diseaseHistory">
              <el-divider content-position="left">Tiền sử bệnh</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.diseaseHistory" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.infectiousHistory">
              <el-divider content-position="left">Tiền sử bệnh truyền nhiễm</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.infectiousHistory"
                        disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.surgeryHistory">
              <el-divider content-position="left">Tiền sử phẫu thuật</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.surgeryHistory" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.transfusionHistory">
              <el-divider content-position="left">Tiền sử truyền máu</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.transfusionHistory"
                        disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.physiqueCheck">
              <el-divider content-position="left">Khám thực thể</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.physiqueCheck" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.emergencyDiagnose">
              <el-divider content-position="left">Chẩn đoán cấp cứu</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.emergencyDiagnose"
                        disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.emergencyEffect">
              <el-divider content-position="left">Hiệu quả cấp cứu</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.emergencyEffect" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.familyHistory">
              <el-divider content-position="left">Tiền sử gia đình</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.familyHistory" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.lunariaHistory">
              <el-divider content-position="left">Tiền sử kinh nguyệt</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.lunariaHistory" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.pregnancyHistory">
              <el-divider content-position="left">Tiền sử hôn nhân/sinh đẻ</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.pregnancyHistory" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.assistCheck">
              <el-divider content-position="left">Xét nghiệm bổ sung</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.assistCheck" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.nowHistory">
              <el-divider content-position="left">Bệnh sử hiện tại</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.nowHistory" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.beforeHistory">
              <el-divider content-position="left">Tiền sử bệnh</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.beforeHistory" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.epidemicDisease">
              <el-divider content-position="left">Tiền sử dịch tễ</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.epidemicDisease" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.otherCheck">
              <el-divider content-position="left">Xét nghiệm khác</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.otherCheck" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.handlingSituation">
              <el-divider content-position="left">Tình hình xử lý</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.handlingSituation"
                        disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.healthEducation">
              <el-divider content-position="left">Giáo dục sức khỏe cá nhân hóa</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.healthEducation" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.chinaDiagnose">
              <el-divider content-position="left">Chẩn đoán đông y</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.chinaDiagnose" disabled></el-input>
            </el-row>
            <el-row v-if="props.row.medicalRecord.westernDiagnose">
              <el-divider content-position="left">Chẩn đoán tây y</el-divider>
              <el-input type="textarea" autosize v-model="props.row.medicalRecord.westernDiagnose" disabled></el-input>
            </el-row>
          </template>
        </el-table-column>
        <el-table-column label="Số thứ tự" type="index"></el-table-column>
        <el-table-column prop="patient.name" label="Họ tên"></el-table-column>
        <el-table-column prop="patient.gender.name" label="Giới tính">
        </el-table-column>
        <el-table-column prop="patient.age" label="Tuổi"></el-table-column>
        <el-table-column prop="medicalRecord.westernDiagnose" label="Chẩn đoán tây y">
        </el-table-column>
        <el-table-column prop="medicalRecord.chinaDiagnose" label="Chẩn đoán đông y">
        </el-table-column>
        <el-table-column prop="updateDate" label="Thời gian tiếp nhận khám"></el-table-column>

        <el-table-column prop="updateBy" label="Họ tên bác sĩ">
          <template slot-scope="scope">
            {{ GetCreateBy(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column prop="" label="Thao tác">
          <template slot-scope="scope">
            <el-button type="text" @click="medicalHistoryInvoking(scope.row)">Gọi</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- <span slot="footer" class="dialog-footer">
        <el-button @click="historyDialogVisible = false">Hủy</el-button>
        <el-button type="primary" @click="historyDialogVisible = false">Xác nhận</el-button>
      </span> -->
    </el-dialog>

    <!-- Hộp thoại mẫu đơn thuốc -->
    <el-dialog title="Gọi mẫu đơn thuốc" :visible.sync="templateDialogVisible" width="50%" :before-close="handleClose">
      <div>
        <el-row>
          <el-form :model="templateQueryModel" @submit.native.prevent label-position="left" label-width="70px"
                   :inline-message="true">
            <el-col :span="24">
              <el-form-item label="Danh mục mẫu" prop="category">
                <el-radio v-model="templateQueryModel.category" @change="searchTemplate" label="3">Tất cả
                </el-radio>
                <el-radio v-model="templateQueryModel.category" @change="searchTemplate" label="0">Mẫu riêng tư
                </el-radio>
                <el-radio v-model="templateQueryModel.category" @change="searchTemplate" label="1">Mẫu công khai
                </el-radio>
                <el-radio v-model="templateQueryModel.category" @change="searchTemplate" label="2">AIMẫu
                </el-radio>
              </el-form-item>
            </el-col>
          </el-form>
        </el-row>
      </div>
      <el-table :data="templateRecipelList" style="width: 100%" :row-key="getRowKeys" :expand-row-keys="expands"
                @expand-change="expandSelect">
        <el-table-column type="expand">
          <template slot-scope="props">
            <!-- Đơn thuốc tây -->
            <div class="two" v-if="props.row.type.value === 'recipelType_0'">
              <el-table :data="filterRecipel(props.row.recipeTemplateDetail)" style="width: 100%"
                        header-cell-style="{
      background:'#e2d2d2'}">
                <el-table-column label="Số thứ tự" type="index" align="center">
                  <template slot-scope="scope">
										<span v-if="scope.row.isExtra === 0">
											{{ scope.$index + 1 }}
										</span>
                  </template>
                </el-table-column>
                <el-table-column prop="drugStuffId.name" label="Thông tin thuốc" width="width">
                  <template slot-scope="scope">
										<span v-if="
                        scope.row.isExtra === 0 && scope.row.stuffType === '0'
                      ">
											{{ scope.row.drugStuffId.name }}
										</span>
                  </template>
                </el-table-column>
                <el-table-column prop="unitPrice" label="Đơn giá" width="120">
                  <template slot-scope="scope">
										<span v-if="
                        (scope.row.isExtra === 0 &&
                          scope.row.isUnpackSell === 0 &&
                          scope.row.stuffType === '0') ||
                        scope.row.stuffType === '2'
                      ">
											{{ (scope.row.unitPrice).toFixed(4) }}/{{
                        scope.row.drugStuffId.drug.pack.name
                      }}
										</span>
                    <span v-else-if="
                        (scope.row.isExtra === 0 &&
                          scope.row.isUnpackSell === 1 &&
                          scope.row.stuffType === '0') ||
                        scope.row.stuffType === '2'
                      ">
											{{ (scope.row.unitPrice).toFixed(4) }}/{{
                        scope.row.drugStuffId.drug.preparationUnit.name
                      }}
										</span>
                  </template>
                </el-table-column>
                <el-table-column prop="singleDosage" label="Liều dùng mỗi lần" width="width">
                  <template slot-scope="scope">
                    <template v-if="scope.row.isExtra === 0">{{
                        scope.row.singleDosage
                          ? scope.row.singleDosage
                          : 0 + "" + scope.row.drugStuffId.preparationUnit.name
                      }}
                    </template>
                  </template>
                </el-table-column>
                <el-table-column prop="westernMedicineUse.name" label="Cách dùng" width="width">
                  <template slot-scope="scope">
										<span v-if="
                        scope.row.westernMedicineUse !== undefined &&
                        scope.row.isExtra === 0
                      ">
											{{ scope.row.westernMedicineUse.name }}
										</span>
                  </template>
                </el-table-column>
                <el-table-column prop="frequency.name" label="Tần suất" width="width">
                  <template slot-scope="scope">
										<span v-if="scope.row.isExtra === 0">
											{{
                        scope.row.frequency.name ? scope.row.frequency.name : ""
                      }}
										</span>
                  </template>
                </el-table-column>

                <!-- <el-table-column prop="drippingSpeed" label="Tốc độ truyền" width="width">
                        <template slot-scope="scope">
                        <template>{{scope.row.drippingSpeed?scope.row.drippingSpeed:0}}giọt/Phút</template>
                      </template>
                    </el-table-column> -->
                <el-table-column prop="days.name" label="Số ngày" width="width">
                  <template slot-scope="scope">
                    <template v-if="scope.row.isExtra === 0">{{
                        scope.row.days.name ? scope.row.days.name : ""
                      }}ngày
                    </template>
                  </template>
                </el-table-column>
                <el-table-column prop="total" label="Tổng số lượng" width="width">
                  <template slot-scope="scope">
                    <template v-if="
                        scope.row.isExtra === 0 &&
                        scope.row.stuffType !== '4' &&
                        scope.row.stuffType !== '3'
                      ">
                      {{
                        Math.floor(
                          scope.row.total /
                          scope.row.drugStuffId.drug.preparation -
                          0
                        ) >= 0
                          ? Math.floor(
                          scope.row.total /
                          scope.row.drugStuffId.drug.preparation -
                          0
                          ) +
                          scope.row.drugStuffId.drug.pack.name +
                          ((scope.row.total %
                            scope.row.drugStuffId.drug.preparation) -
                          0 >
                          0
                            ? (scope.row.total %
                            scope.row.drugStuffId.drug.preparation) -
                            0 +
                            scope.row.drugStuffId.drug.preparationUnit.name
                            : "")
                          : scope.row.total +
                          scope.row.drugStuffId.drug.preparationUnit.name
                      }}
                    </template>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="two" v-else-if="props.row.type.value === 'recipelType_1'">
              <el-table :data="filterRecipel(props.row.recipeTemplateDetail)" style="width: 100%">
                <el-table-column label="Số thứ tự" type="index" align="center">
                </el-table-column>
                <el-table-column prop="drugStuffId.name" label="Thông tin thuốc" width="width">
                </el-table-column>
                <el-table-column prop="unitPrice" label="Đơn giá" width="120">
                  <template slot-scope="scope">
										<span v-if="
                        scope.row.isUnpackSell === 0 &&
                        scope.row.stuffType === '1'
                      ">
											{{ (scope.row.unitPrice).toFixed(4) }}/{{
                        scope.row.drugStuffId.drug.pack.name
                      }}
										</span>
                    <span v-else-if="
                        scope.row.isUnpackSell === 1 &&
                        scope.row.stuffType === '1'
                      ">
											{{ (scope.row.unitPrice).toFixed(4) }}/{{
                        scope.row.drugStuffId.drug.preparationUnit.name
                      }}
										</span>
                  </template>
                </el-table-column>
                <el-table-column prop="singleDosage" label="Liều dùng mỗi lần" width="width">
                  <template slot-scope="scope">
                    <template v-if="scope.row.stuffType === '1'">{{
                        scope.row.singleDosage +
                        "" +
                        scope.row.drugStuffId.preparationUnit.name
                      }}
                    </template>
                  </template>
                </el-table-column>
                <el-table-column prop="chineseMedicineUse.name" label="Cách dùng" width="width">
                  <template slot-scope="scope">
                    <template v-if="scope.row.stuffType === '1'">{{
                        scope.row.chineseMedicineUse.name
                      }}
                    </template>
                  </template>
                </el-table-column>

                <!-- <el-table-column prop="drippingSpeed" label="Tốc độ truyền" width="width">
                        <template slot-scope="scope">
                        <template>{{scope.row.drippingSpeed?scope.row.drippingSpeed:0}}giọt/Phút</template>
                      </template>
                    </el-table-column> -->
                <el-table-column prop="total" label="Tổng số lượng" width="width">
                  <template slot-scope="scope">
                    <template v-if="scope.row.stuffType === '1'">
                      {{
                        Math.floor(
                          scope.row.total /
                          scope.row.drugStuffId.drug.preparation -
                          0
                        ) >= 0
                          ? Math.floor(
                          scope.row.total /
                          scope.row.drugStuffId.drug.preparation -
                          0
                          ) +
                          scope.row.drugStuffId.drug.pack.name +
                          ((scope.row.total %
                            scope.row.drugStuffId.drug.preparation) -
                          0 >
                          0
                            ? (scope.row.total %
                            scope.row.drugStuffId.drug.preparation) -
                            0 +
                            scope.row.drugStuffId.drug.preparationUnit.name
                            : "")
                          : scope.row.total +
                          scope.row.drugStuffId.drug.preparationUnit.name
                      }}
                    </template>
                  </template>
                </el-table-column>
              </el-table>
              <div style="margin: 10px 0">
                <el-input style="width: 50px" disabled v-model="props.row.recipeTemplateInfo.dosage">
                </el-input>
                thang Cách dùng：
                <el-input style="width: 100px" disabled
                          v-model="props.row.recipeTemplateInfo.recipelUse.name">
                </el-input>
                <el-input style="width: 100px" disabled
                          v-model="props.row.recipeTemplateInfo.takeFrequency.name">
                </el-input>
                <el-input style="width: 100px" disabled
                          v-model="props.row.recipeTemplateInfo.frequency.name">
                </el-input>
                Mỗi lần uống
                <el-input style="width: 80px" disabled
                          v-model="props.row.recipeTemplateInfo.singleDosage">
                </el-input>
                {{ "ml" }}
              </div>
            </div>

            <!-- Đơn truyền dịch -->
            <div class="two" style="border: 1px solid #dcdcdc"
                 v-else-if="props.row.type.value === 'recipelType_2'">
              <div v-for="(items, index) in getInfuSion(
                  props.row.recipeTemplateDetail
                ).zushu" :key="index" style="border: 1px solid #dcdcdc; padding: 10px; margin: 10px">
                <div>
									<span style="
                      float: left;
                      height: 25px;
                      line-height: 25px;
                      font-size: 14px;
                    ">&nbsp;&nbsp;thứ{{ index + 1 }}nhóm</span>
                  <span style="
                      float: right;
                      height: 25px;
                      line-height: 25px;
                      font-size: 14px;
                    ">&nbsp;&nbsp;giọt/Phút</span>

                  <el-input style="float: right; width: 10%" :value="
                      getInfuSion(props.row.recipeTemplateDetail).drippingSpeed[
                        index
                      ]
                    " placeholder="" :disabled="true">
                  </el-input>

                  <span style="
                      float: right;
                      height: 25px;
                      line-height: 25px;
                      font-size: 14px;
                    ">&nbsp;&nbsp;ngày&nbsp;&nbsp;</span>
                  <el-input style="float: right; width: 10%" :value="
                      getInfuSion(props.row.recipeTemplateDetail).days[index]
                        .name
                    " placeholder="" :disabled="true">
                  </el-input>

                  <span style="
                      float: right;
                      height: 25px;
                      line-height: 25px;
                      font-size: 18px;
                    ">&nbsp;&nbsp;</span>
                  <el-input style="float: right; width: 10%" :value="
                      getInfuSion(props.row.recipeTemplateDetail).frequency[
                        index
                      ].name
                    " placeholder="" :disabled="true">
                  </el-input>

                  <span style="
                      float: right;
                      height: 25px;
                      line-height: 25px;
                      font-size: 18px;
                    ">&nbsp;&nbsp;</span>
                  <el-input style="float: right; width: 10%" :value="
                      getInfuSion(props.row.recipeTemplateDetail).infuseUse[
                        index
                      ].name
                    " placeholder="" :disabled="true">
                  </el-input>
                </div>
                <div style="border: 17px solid #0000"></div>
                <el-table :data="
                    getInfuSion(props.row.recipeTemplateDetail).infusionProject[
                      index
                    ]
                  " style="width: 100%">
                  <el-table-column label="Số thứ tự" type="index" align="center" width="width">
                  </el-table-column>
                  <el-table-column prop="drugStuffId.name" label="Tên thuốc" width="width">
                  </el-table-column>
                  <el-table-column prop="skinTest.name" label="Test da" width="width">
                  </el-table-column>
                  <el-table-column prop="drugStuffId.drug.factory.name" label="Nhà sản xuất" width="width">
                  </el-table-column>
                  <el-table-column prop="unitPrice" label="Đơn giá" width="120">
                    <template slot-scope="scope">
											<span>{{ (scope.row.unitPrice).toFixed(4) }}VNĐ/{{
                          scope.row.isUnpackSell === 1
                            ? scope.row.drugStuffId.preparationUnit.name
                            : scope.row.drugStuffId.pack.name
                        }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="singleDosage" label="Liều dùng mỗi lần" width="width">
                    <template slot-scope="scope">
                      <template>{{
                          scope.row.singleDosage +
                          "" +
                          scope.row.drugStuffId.preparationUnit.name
                        }}
                      </template>
                    </template>
                  </el-table-column>
                  <el-table-column prop="total" label="Số lượng" width="150">
                    <template slot-scope="scope">
											<span v-if="scope.row.drugStuffId.costItem != null">
												{{
                          scope.row.total
                        }}{{ scope.row.drugStuffId.dosisUnit.name }}
											</span>
                      <span v-else-if="scope.row.drugStuffId.stuff != null">
												{{
                          Math.floor(
                            scope.row.total /
                            scope.row.drugStuffId.stuff.packNumber
                          ) > 0
                            ? Math.floor(
                            scope.row.total /
                            scope.row.drugStuffId.stuff.packNumber
                            ) +
                            scope.row.drugStuffId.stuff.packUnit.name +
                            (scope.row.total %
                            scope.row.drugStuffId.stuff.packNumber >
                            0
                              ? (scope.row.total %
                              scope.row.drugStuffId.stuff.packNumber) +
                              scope.row.drugStuffId.stuff.minUnit.name
                              : "")
                            : scope.row.total +
                            scope.row.drugStuffId.stuff.minUnit.name
                        }}
											</span>
                      <span v-else-if="scope.row.drugStuffId.drug != null">
												{{
                          Math.floor(
                            scope.row.total /
                            (scope.row.drugStuffId.drug.preparation - 0)
                          ) > 0
                            ? Math.floor(
                            scope.row.total /
                            (scope.row.drugStuffId.drug.preparation - 0)
                            ) +
                            scope.row.drugStuffId.drug.pack.name +
                            (scope.row.total %
                            (scope.row.drugStuffId.drug.preparation - 0) >
                            0
                              ? (scope.row.total %
                              (scope.row.drugStuffId.drug.preparation -
                                0)) +
                              scope.row.drugStuffId.drug.preparationUnit
                                .name
                              : "")
                            : scope.row.total +
                            scope.row.drugStuffId.drug.preparationUnit.name
                        }}
											</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="allFee" label="Tổng tiền" width="width">
                    <template slot-scope="scope">
                      {{ scope.row.allFee }}VNĐ
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>

            <!-- Hạng mục khám chữa bệnh -->

            <div class="two" v-else-if="props.row.type.value === 'recipelType_3'">
              <el-table :data="filterRecipel(props.row.recipeTemplateDetail)" style="width: 100%">
                <el-table-column type="index" label="Số thứ tự" width="50" align="center"></el-table-column>
                <el-table-column prop="drugStuffId.name" label="Tên hạng mục" align="center"></el-table-column>
                <el-table-column prop="drugStuffId.costItem.itemType.name" label="Loại hạng mục" align="center"
                                 width="100"></el-table-column>
                <el-table-column prop="drugStuffId.costItem.isPackage" label="Hạng mục gói dịch vụ" align="center"
                                 width="100">
                  <template slot-scope="scope">
                    {{
                      scope.row.drugStuffId.costItem.isPackage === "1"
                        ? "Có"
                        : "Không"
                    }}
                  </template>
                </el-table-column>
                <el-table-column prop="total" label="Số lượng" align="center" width="120">
                  <template slot-scope="scope">
                    {{ scope.row.total }} {{ scope.row.drugStuffId.pack.name }}
                  </template>
                </el-table-column>
                <el-table-column label="Đơn giá" width="120" align="center">
                  <template slot-scope="scope">
                    {{ (scope.row.drugStuffId.price).toFixed(4) }}/{{
                      scope.row.drugStuffId.pack.name
                    }}
                  </template>
                </el-table-column>
                <el-table-column prop="allFee" label="Tổng số tiền" width="120" align="center">
                </el-table-column>
              </el-table>
            </div>

            <br/>
            <div class="two" style="margin: 10px">
              <el-divider style="margin-top: 20px" content-position="left">Phí phụ thêm</el-divider>
              <el-table :data="filterAdditional(props.row.recipeTemplateDetail)" style="width: 100%">
                <el-table-column label="Số thứ tự" type="index" align="center" width="120">
                </el-table-column>
                <el-table-column prop="drugStuffId.name" label="Tên" width="width">
                  <template slot-scope="scope">
										<span>
											{{ scope.row.drugStuffId.name }}
										</span>
                  </template>
                </el-table-column>

                <el-table-column prop="unitPrice" label="Đơn giá" width="120">
                  <template slot-scope="scope">
										<span>{{ (scope.row.unitPrice).toFixed(4) }}VNĐ/{{
                        scope.row.isUnpackSell === 1
                          ? scope.row.drugStuffId.preparationUnit.name
                          : scope.row.drugStuffId.pack.name
                      }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="total" label="Số lượng" width="width">
                  <template slot-scope="scope">
										<span v-if="scope.row.drugStuffId.costItem != null">
											{{
                        scope.row.total
                      }}{{ scope.row.drugStuffId.dosisUnit.name }}
										</span>
                    <span v-else-if="scope.row.drugStuffId.stuff != null">
											{{
                        Math.floor(
                          scope.row.total /
                          scope.row.drugStuffId.stuff.packNumber
                        ) > 0
                          ? Math.floor(
                          scope.row.total /
                          scope.row.drugStuffId.stuff.packNumber
                          ) +
                          scope.row.drugStuffId.stuff.packUnit.name +
                          (scope.row.total %
                          scope.row.drugStuffId.stuff.packNumber >
                          0
                            ? (scope.row.total %
                            scope.row.drugStuffId.stuff.packNumber) +
                            scope.row.drugStuffId.stuff.minUnit.name
                            : "")
                          : scope.row.total +
                          scope.row.drugStuffId.stuff.minUnit.name
                      }}
										</span>
                    <span v-else-if="scope.row.drugStuffId.drug != null">
											{{
                        Math.floor(
                          scope.row.total /
                          (scope.row.drugStuffId.drug.preparation - 0)
                        ) > 0
                          ? Math.floor(
                          scope.row.total /
                          (scope.row.drugStuffId.drug.preparation - 0)
                          ) +
                          scope.row.drugStuffId.drug.pack.name +
                          (scope.row.total %
                          (scope.row.drugStuffId.drug.preparation - 0) >
                          0
                            ? (scope.row.total %
                            (scope.row.drugStuffId.drug.preparation -
                              0)) +
                            scope.row.drugStuffId.drug.preparationUnit.name
                            : "")
                          : scope.row.total +
                          scope.row.drugStuffId.drug.preparationUnit.name
                      }}
										</span>
                  </template>
                </el-table-column>
                <el-table-column prop="allFee" label="Tổng tiền" width="width">
                  <template slot-scope="scope">
                    <span> {{ scope.row.allFee }}VNĐ </span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-row>
              <el-divider content-position="left">Hạng mục y lệnh</el-divider>
              <el-input type="textarea" :rows="4" v-model="props.row.recipeTemplateInfo.entrust" disabled>
              </el-input>
            </el-row>
          </template>
        </el-table-column>

        <el-table-column label="Số thứ tự" type="index"></el-table-column>
        <el-table-column prop="recipetemplateName" label="Tên mẫu" width="width">
        </el-table-column>
        <el-table-column prop="category" label="Danh mục mẫu" width="width">
          <template slot-scope="scope">
            <span v-if="scope.row.category === '0'"> Mẫu riêng tư </span>
            <span v-if="scope.row.category === '1'"> Mẫu dùng chung </span>
            <span v-if="scope.row.category === '2'"> AIMẫu </span>
          </template>
        </el-table-column>
        <el-table-column prop="diagnosis" label="Chẩn đoán" width="width">
        </el-table-column>
        <el-table-column prop="createDate" label="Thời gian tạo" width="width">
        </el-table-column>
        <el-table-column prop="" label="Thao tác">
          <template slot-scope="scope">
            <el-button type="text" @click="invoking2(scope.row)">Gọi</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- <span slot="footer" class="dialog-footer">
        <el-button @click="templateDialogVisible = false">Hủy</el-button>
        <el-button type="primary" @click="templateDialogVisible = false">Xác nhận</el-button>
      </span> -->
    </el-dialog>

    <!-- Lưu mẫu -->
    <el-dialog title="Lưu mẫu đơn thuốc" :visible.sync="saveTemplateDialogVisible" width="40%" :before-close="handleClose">
      <el-form :model="saveTemplateBizFormModel" :rules="templateFormRules" ref="saveTemplateBizFormModel"
               label-width="120px" label-position="right" class="edit-form">
        <div class="tab-item">
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Tên mẫu" prop="recipetemplateName">
                <el-input v-model="saveTemplateBizFormModel.recipetemplateName" :maxlength="100"
                          :placeholder="'Vui lòng nhập tên mẫu'"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Danh mục mẫu" prop="category">
                <el-radio v-model="saveTemplateBizFormModel.category" label="0">Mẫu riêng tư</el-radio>
                <el-radio v-model="saveTemplateBizFormModel.category" label="1">Mẫu công khai</el-radio>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Chẩn đoán" prop="diagnosis">
                <el-input v-model="saveTemplateBizFormModel.diagnosis" :maxlength="100"
                          :placeholder="'Vui lòng nhập chẩn đoán'"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Giải thích mẫu" prop="remarks">
                <el-input v-model="saveTemplateBizFormModel.remarks" :maxlength="100"
                          :placeholder="'Vui lòng nhập giải thích mẫu'"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
      <span slot="footer" class="dialog-footer">
				<el-button @click="saveTemplateDialogVisible = false">Hủy</el-button>
				<el-button type="primary" @click="saveRecipeTemplate('saveTemplateBizFormModel')">Lưu</el-button>
			</span>
    </el-dialog>

    <!-- Hộp thoại điền báo cáo -->
    <el-dialog :title="typeName" :visible.sync="inspectionDialogVisible" v-if="inspectionDialogVisible" width="75%"
               :before-close="handleClose">
      <div slot="title" class="dialog-header">
        {{ typeName }}
      </div>
      <div v-if="costInspection.costItem.itemType.value === 'treatmentItemType_0'">
        <el-form :model="costInspection.inspectionCheckInfo" :rules="formRules" ref="bizFormModel"
                 label-width="120px" label-position="right" class="edit-form">
          <!-- <el-card class="box-card main-card"> -->

          <div style="margin-left: -60px">
            <el-row>
              <el-col :span="24 / 4">
                <el-form-item label="Họ tên" prop="name">
                  <el-input :disabled="true" v-model="costInspection.inspectionCheckInfo.name"
                            :maxlength="120">
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 4">
                <el-form-item label="Thời gian lấy mẫu" prop="samplingDate">
                  <!-- <el-input :disabled='true' v-model='bizFormModel.samplingDate'></el-input> -->
                  <el-date-picker :disabled="true"
                                  v-model="costInspection.inspectionCheckInfo.samplingDate" type="datetime"
                                  placeholder="Chọn ngày giờ" value-format="yyyy-MM-dd HH:mm:ss"
                                  format="yyyy-MM-dd HH:mm:ss">
                  </el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 4">
                <el-form-item label="Số mẫu" prop="samplingNum">
                  <el-input placeholder="Vui lòng nhập nội dung" :disabled="true"
                            v-model="costInspection.inspectionCheckInfo.samplingNum" :maxlength="100">
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 4">
                <el-form-item label="Thiết bị xét nghiệm" prop="instrument">
                  <el-input placeholder="Vui lòng nhập nội dung" :disabled="true"
                            v-model="costInspection.inspectionCheckInfo.instrument" :maxlength="100">
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24 / 4">
                <el-form-item label="Số xét nghiệm" prop="checkoutNum">
                  <el-input placeholder="Vui lòng nhập nội dung" :disabled="true"
                            v-model="costInspection.inspectionCheckInfo.checkoutNum" :maxlength="100">
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 4">
                <el-form-item label="Thời gian xét nghiệm" prop="checkoutDate">
                  <el-date-picker :disabled="true"
                                  v-model="costInspection.inspectionCheckInfo.checkoutDate" type="datetime"
                                  placeholder="Chọn ngày giờ" value-format="yyyy-MM-dd HH:mm:ss"
                                  format="yyyy-MM-dd HH:mm:ss">
                  </el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 4">
                <el-form-item label="Khoa xét nghiệm" prop="checkoutOffice">
                  <el-input placeholder="Vui lòng nhập nội dung" :disabled="true"
                            v-model="costInspection.inspectionCheckInfo.checkoutOffice" :maxlength="100">
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- </el-card> -->
          <br/>
          <el-table :data="costInspection.inspectionCheckInfo.inspectionCheckDetails" style="width: 100%">
            <el-table-column type="index" width="50" label="Số thứ tự">
            </el-table-column>
            <el-table-column prop="costItem.itemName" label="Tên hạng mục" width="width">
            </el-table-column>
            <el-table-column prop="conclusion" label="Kết quả" width="width">
              <template slot-scope="scope">
                <el-input placeholder="Vui lòng nhập nội dung" :disabled="true" v-model="scope.row.conclusion"
                          :maxlength="100">
                </el-input>
              </template>
            </el-table-column>
            <el-table-column prop="sign" label="Dấu cao/thấp" width="width">
              <template slot-scope="scope">
                <el-select :disabled="true" v-model="scope.row.sign" placeholder="Vui lòng chọn">
                  <el-option v-for="item in signList" :key="item.value" :label="item.name"
                             :value="item.value">
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="costItem.costItemPackages[0].referenceValue" label="Dấu tham khảo" width="width">
              <template slot-scope="scope">
                <el-input :disabled="true" type="textarea" v-model="
                    scope.row.costItem.costItemPackages[0].referenceValue
                  ">
                </el-input>
              </template>
            </el-table-column>
            <el-table-column prop="costItem.costItemPackages[0].referenceUnit" label="Đơn vị" width="width">
            </el-table-column>

            <el-table-column prop="describes" label="Mô tả" width="width">
              <template slot-scope="scope">
                <el-input :disabled="true" v-model="scope.row.describes" :maxlength="100">
                </el-input>
              </template>
            </el-table-column>
          </el-table>
          <br/>
          <div style="margin-left: -50px">
            <el-form-item label="Kết luận báo cáo" prop="reportConclusion">
              <el-input :disabled="true" type="textarea" :autosize="{ minRows: 2, maxRows: 100 }"
                        placeholder="Vui lòng nhập nội dung" v-model="costInspection.inspectionCheckInfo.reportConclusion">
              </el-input>
            </el-form-item>
          </div>
          <br/>

          <el-form-item label="" prop="">
            <div style="margin-left: -50px">
              <upload-file :objectId="uploadFiles" :action="true"></upload-file>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <div v-if="costInspection.costItem.itemType.value === 'treatmentItemType_1'">
        <el-form :model="costInspection.inspectionCheckInfo" :rules="formRules" ref="bizFormModel"
                 label-width="120px" label-position="right" class="edit-form">
          <div style="margin-left: -60px">
            <el-row>
              <el-col :span="24 / 4">
                <el-form-item label="Họ tên" prop="name">
                  <el-input :disabled="true" v-model="costInspection.inspectionCheckInfo.name"
                            :maxlength="120">
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 4">
                <el-form-item label="Thời gian đăng ký" prop="samplingDate">
                  <el-date-picker :disabled="flags"
                                  v-model="costInspection.inspectionCheckInfo.samplingDate" type="datetime"
                                  placeholder="Chọn ngày giờ" value-format="yyyy-MM-dd HH:mm:ss"
                                  format="yyyy-MM-dd HH:mm:ss">
                  </el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 4">
                <el-form-item label="Thiết bị xét nghiệm" prop="instrument">
                  <el-input :disabled="true" placeholder="Vui lòng nhập nội dung"
                            v-model="costInspection.inspectionCheckInfo.instrument" :maxlength="100">
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 4">
                <el-form-item label="Loại kiểm tra" prop="checkType">
                  <el-input :disabled="true" placeholder="Vui lòng nhập nội dung"
                            v-model="costInspection.inspectionCheckInfo.checkType" :maxlength="100">
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24 / 4">
                <el-form-item label="Vị trí kiểm tra" prop="checkPart">
                  <el-input :disabled="true" placeholder="Vui lòng nhập nội dung"
                            v-model="costInspection.inspectionCheckInfo.checkPart" :maxlength="100">
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 4">
                <el-form-item label="Bác sĩ báo cáo" prop="doctor">
                  <el-input :disabled="true" placeholder="Vui lòng nhập nội dung"
                            v-model="costInspection.inspectionCheckInfo.doctor" :maxlength="100"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 4">
                <el-form-item label="Hạng mục kiểm tra" prop="checkProject">
                  <el-input :disabled="true" placeholder="Vui lòng nhập nội dung"
                            v-model="costInspection.inspectionCheckInfo.checkProject" :maxlength="100">
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 4">
                <el-form-item label="Thời gian báo cáo" prop="checkoutDate">
                  <el-date-picker :disabled="true"
                                  v-model="costInspection.inspectionCheckInfo.checkoutDate" type="datetime"
                                  placeholder="Chọn ngày giờ" value-format="yyyy-MM-dd HH:mm:ss"
                                  format="yyyy-MM-dd HH:mm:ss">
                  </el-date-picker>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24 / 1">
                <el-form-item label="Kết quả kiểm tra thấy" prop="checkAdvise">
                  <el-input type="textarea" width="100%" :disabled="true"
                            v-model="costInspection.inspectionCheckInfo.checkAdvise" :maxlength="100">
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24 / 1">
                <el-form-item label="Ý kiến" prop="opinion">
                  <el-input type="textarea" width="100%" :disabled="true"
                            v-model="costInspection.inspectionCheckInfo.opinion" :maxlength="100">
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="" prop="">
              <div>
                <upload-file :objectId="uploadFiles" :action="false"></upload-file>
              </div>
            </el-form-item>
          </div>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
				<el-button v-if="true" @click="inspectionDialogVisible = false">Hủy</el-button>
        <!-- <el-button type="primary" v-if="!flags" @click="save">Lưu</el-button> -->
			</span>
    </el-dialog>

    <!-- Hộp thoại mẫu đơn thuốc -->
    <el-dialog title="Gọi mẫu bệnh án" :visible.sync="blmbDialogVisible" width="50%" :before-close="handleClose">
      <el-car>
        <el-row>
          <el-col :span="24">
            <el-form :inline="true" :model="blmbcxrc" class="demo-form-inline">
              <el-form-item label="Tên mẫu" style="margin-right: 3px">
                <el-input v-model="blmbcxrc.mbmc" placeholder="Vui lòng nhập tên mẫu"></el-input>
              </el-form-item>

              <el-form-item style="margin-right: 3px">
                <el-button type="primary" @click="blmbdialogonSubmit">Truy vấn</el-button>
                <el-button type="info" icon="el-icon-refresh-left" @click="blmbdialogresetCondition"
                           :plain="true">Đặt lại
                </el-button>
              </el-form-item>
            </el-form>
          </el-col>
          <el-rol :span="24">
            <el-table :data="blmbtableData" :row-key="blmbgetRowKeys" :expand-row-keys="blmbexpands"
                      @expand-change="blmbexpandSelect" style="width: 100%">
              <el-table-column type="expand">
                <template slot-scope="props">
                  <el-form label-position="left" inline class="demo-table-expand">
                    <el-form-item label="Mã mẫu" v-if="blmbshow">
                      <el-tooltip :content="props.row.mbbm" placement="top-start" effect="dark"
                                  popper-class="atooltip">
												<span style="
                            width: 100%;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            white-space: nowrap;
                            display: inline-block;
                          ">
													{{ props.row.mbbm }}
												</span>
                      </el-tooltip>
                    </el-form-item>

                    <el-form-item style="padding-left: 10%" v-for="(item, index) in blmbList"
                                  :key="index" :label="item.label"
                                  v-show="item.value !== '' && item.value != null">
                      <el-tooltip :content="item.value" placement="top-start" effect="dark"
                                  popper-class="atooltip">
												<span style="
                            width: 100%;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            white-space: nowrap;
                            display: inline-block;
                          ">
													{{ item.value }}
												</span>
                      </el-tooltip>
                    </el-form-item>
                  </el-form>
                </template>
              </el-table-column>
              <el-table-column label="Số thứ tự" type="index" :index="blmbindexMethod" align="center">
              </el-table-column>
              <el-table-column prop="mbbm" v-if="blmbshow" label="Mã mẫu">
              </el-table-column>
              <el-table-column prop="mbmc" label="Tên mẫu"></el-table-column>
              <el-table-column prop="mblx" label="Loại mẫu"></el-table-column>
              <el-table-column prop="bllx" label="Loại bệnh án"></el-table-column>
              <el-table-column label="Thao tác" width="80">
                <template slot-scope="scope">
                  <el-button @click.native="diaoyong(scope.row)" type="text" size="small">Gọi
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-rol>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-pagination background @size-change="blmbonSizeChange" @current-change="blmbonCurrentChange"
                           :current-page.sync="blmbcurrentPage" :page-sizes="[20, 50, 100, blmbpatientTotal]"
                           :page-size="20" layout="total, sizes, prev, pager, next, jumper" :total="blmbpatientTotal">
            </el-pagination>
          </el-col>
        </el-row>
      </el-car>
    </el-dialog>

    <el-dialog title="Loại lưu mẫu bệnh án" :visible.sync="blmblxDialogVisible" width="30%" :before-close="handleClose">
      <el-car>
        <el-row>
          <el-form :inline="true" :model="blmbmbmc" class="demo-form-inline" :rules="blmbrules"
                   ref="blmbmbmc">
            <el-col :span="24">
              <el-form-item prop="mbmc" label="Tên mẫu">
                <el-input v-model="blmbmbmc.mbmc"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="Loại mẫu">
                <el-radio v-model="blmbmblx" label="0">Dùng chung</el-radio>
                <el-radio v-model="blmbmblx" label="1">Cá nhân</el-radio>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="Loại bệnh án">
                <el-radio v-model="blmbbllx" label="0">Bệnh án Tây y</el-radio>
                <el-radio v-model="blmbbllx" label="1">Bệnh án Đông y</el-radio>
              </el-form-item>
            </el-col>
          </el-form>
        </el-row>
      </el-car>
      <span slot="footer" class="dialog-footer">
				<el-button :disabled="flag" type="primary" :plain="true" @click="saveblmbsubmit('blmbmbmc')">Lưu
				</el-button>
				<el-button :plain="true" type="primary" @click="blmbonDialogClose()">Hủy</el-button>
			</span>
    </el-dialog>
  </el-container>
</template>

<script>
  import {
    listDictItemAll
  } from "@/api/sys/dictItem";
  import {
    listAllStock
  } from "@/api/stock/drug";
  import {
    listCostItemAll
  } from "@/api/treatment/costItem";
  import {
    listStuffAll
  } from "@/api/stock/stuff";
  import UploadFile from "../../components/uploadFile.vue";
  import {
    getblmblist,
    saveblmb
  } from "@/api/outpatient/blmb";
  import {
    listRegistrationPage,
    getRegistrationById,
    updateStatus,
  } from "@/api/outpatient/registration";
  import {
    allSaveMedicalRecord,
    allQueryMedicalRecord,
    getLatestAllergyHistory,
    ureport,
    editSave,
    getHistoryRecipel,
    getrecordpatlist,
    getZdList,
    getZhList
  } from "@/api/outpatient/medicalRecord";
  import {
    listAll
  } from "@/api/stock/medicinalStockControl";
  import {
    listRecipelInfoAll,
    invalidStatus,
  } from "@/api/outpatient/recipelInfo";
  import {
    listRecipetemplatePage,
    getRecipetemplateById,
    deleteRecipetemplate,
  } from "@/api/outpatient/recipetemplate";
  import {
    getByRecipelInfoId
  } from "@/api/outpatient/recipelDetail";
  import {
    getPatientById
  } from "@/api/outpatient/patient";
  import {
    saveRecipetemplate,
    allSave
  } from "@/api/outpatient/recipetemplate";
  import NP from "number-precision";
  import {
    BigNumber
  } from "bignumber.js";
  import {
    getByPatientId, getPoverty
  } from "@/api/member/memberManagement";
  import {listSysParamConfigAll} from '@/api/sys/sysParamConfig';
  import {getDictItemsByCode, DICT_CODE} from '@/utils/dictCache';
  import {getLocalCurrentCompany} from "@/utils/auth";
  import moment from "moment";
  import RegistrationForm from "../registration/registrationForm.vue";

  export default {
    components: {
      RegistrationForm,
      UploadFile,
    },
    name: "index",
    methods: {
      // 处理医疗类型切换
      handleMedicalTypeChange() {
        // 根据模板类型调整必填项
        if (this.medicalType === 'chinese') {
          // 中医模板：中医诊断和中医证候必填
          this.BasicInfoFormRules.chinaDiagnose = [
            { required: true, message: '请输入中医诊断', trigger: 'blur' }
          ];
          this.BasicInfoFormRules.chinaSyndrome = [
            { required: true, message: '请输入中医证候', trigger: 'blur' }
          ];
          // 移除西医诊断必填
          delete this.BasicInfoFormRules.westernDiagnose;
        } else {
          // 西医模板：西医诊断必填
          this.BasicInfoFormRules.westernDiagnose = [
            { required: true, message: '请输入西医诊断', trigger: 'blur' }
          ];
          // 移除中医诊断和证候必填
          delete this.BasicInfoFormRules.chinaDiagnose;
          delete this.BasicInfoFormRules.chinaSyndrome;
        }
      },
      getFileList(fileList) {
      },
      deleteFile(fileId) {
        this.deleteFileIdList.push(fileId);
      },
      beforeRemove(file, fileList) {
      },
      onChronicDiseaseChange(newVal, oldVal) {
        this.MedicalCalculate();
      },
      //Xem xét nghiệm/kiểm tra
      lookInspection(row) {
        if (row.costItem.itemType.value === "treatmentItemType_0") {
          this.costInspection = row;
          this.typeName = "查看检验报告";
          this.costInspection.inspectionCheckInfo.name =
            this.BasicInfoModel.name +
            "/" +
            this.BasicInfoModel.gender.name +
            "/" +
            this.BasicInfoModel.age +
            "tuổi";

          this.uploadFiles = this.costInspection.inspectionCheckInfo.id;
          this.inspectionDialogVisible = true;
        } else if (row.costItem.itemType.value === "treatmentItemType_1") {
          this.costInspection = row;
          this.typeName = "查看检查报告";
          this.costInspection.inspectionCheckInfo.name =
            this.BasicInfoModel.name +
            "/" +
            this.BasicInfoModel.gender.name +
            "/" +
            this.BasicInfoModel.age +
            "tuổi";

          this.uploadFiles = this.costInspection.inspectionCheckInfo.id;
          this.inspectionDialogVisible = true;
        }
      },

      //基础资料刷新
      refresh() {
        this.$forceUpdate();
      },
      exchage() {
        this.$forceUpdate();
      },
      saveRecipeTemplate(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.saveTemplateBizFormModel.company = this.Company;
            this.saveTemplateBizFormModel.type.value = this.saveTemplateType;
            this.saveTemplateBizFormModel.createId = currentUser.id;
            this.saveTemplateBizFormModel.recipeTemplateInfo =
              this.medicalEditTabs[this.saveTemplateIndex].content.recipelInfo;
            // Cách ly cấp phòng khám：仅保存当前诊所的药品/Vật tư/Hạng mục khám chữa bệnh
            var currentCompanyId = currentUser.company.id;
            var allDetails = this.medicalEditTabs[this.saveTemplateIndex].content.recipelDetailEvtList;
            var filteredDetails = allDetails.filter(function(d) {
              if (d.drugStuffId && d.drugStuffId.drug && d.drugStuffId.drug.company) {
                return d.drugStuffId.drug.company.id === currentCompanyId;
              }
              if (d.drugStuffId && d.drugStuffId.stuff && d.drugStuffId.stuff.company) {
                return d.drugStuffId.stuff.company.id === currentCompanyId;
              }
              if (d.drugStuffId && d.drugStuffId.costItem && d.drugStuffId.costItem.company) {
                return d.drugStuffId.costItem.company.id === currentCompanyId;
              }
              return true;
            });
            this.saveTemplateBizFormModel.recipeTemplateDetail = filteredDetails;
            allSave(this.saveTemplateBizFormModel)
              .then((responseData) => {
                if (responseData.code === 100) {
                  this.$message.success("Lưu thành công");

                  this.saveTemplateDialogVisible = false;
                } else {
                  //this.showMessage(responseData)
                }
              })
              .catch((error) => {
                this.outputError(error);
                this.$message.error(
                  "Dữ liệu hệ thống bất thường，Vui lòng kiểm tra dữ liệu có sai không hoặc liên hệ quản trị viên giải quyết！"
                );
              });
          } else {
            this.$message.error("Vui lòng điền tên mẫu hoặc mô tả chẩn đoán!");
            return false;
          }
        });
      },

      //Lưu mẫu
      saveTemplate(item, index) {
        this.saveTemplateType = item;
        this.saveTemplateIndex = index;
        this.saveTemplateBizFormModel = {
          type: {
            // Loại mẫu
            value: null,
            name: null,
          },
          company: {
            // Phòng khámid
            id: "",
            name: null,
          },
          recipetemplateName: "",
          category: "0",
          diagnosis: "",
          remarks: "",
          recipeTemplateInfo: {},
          recipeTemplateDetail: [{}],
          createId: "",
        };
        this.saveTemplateDialogVisible = true;
      },

      //模板处方搜索事件
      searchTemplate() {
        let search = {
          columnName: "",
          limit: 1000,
          offset: 0,
          order: "",
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
            {
              columnName: "type",
              queryType: "=",
              value: this.templateType,
            },
          ],
        };

        if (this.templateQueryModel.category === "0") {
          search.params.push({
            columnName: "category",
            queryType: "=",
            value: "0",
          });
          search.params.push({
            columnName: "create_id",
            logic: "and",
            queryType: "=",
            value: currentUser.id,
          });
        } else if (this.templateQueryModel.category === "1") {
          search.params.push({
            columnName: "category",
            queryType: "=",
            value: "1",
          });
        } else if (this.templateQueryModel.category === "2") {
          search.params.push({
            columnName: "category",
            queryType: "=",
            value: "2",
          }, {
            columnName: "patient_id",
            logic: "and",
            queryType: "=",
            value: this.BasicInfoModel.id,
          });
        } else {
          search.params.push({
            logic: "AND",
            queryType: "(",
          }, {
            columnName: "category",
            logic: "",
            queryType: "=",
            value: 0,
          }, {
            columnName: "create_id",
            logic: "and",
            queryType: "=",
            value: currentUser.id,
          }, {
            columnName: "category",
            logic: "or",
            queryType: "=",
            value: 1,
          }, {
            logic: "",
            queryType: ")",
          });
        }
        this.templateRecipelList = null;
        listRecipetemplatePage(search)
          .then((responseData) => {
            if (responseData.code === 100) {
              responseData.data.rows.forEach((item) => {
                item.recipeTemplateDetail.forEach((recipeTemplateDetailItem) => {
                  if (
                    recipeTemplateDetailItem.frequency.value === undefined ||
                    recipeTemplateDetailItem.frequency.value == null
                  ) {
                    recipeTemplateDetailItem.frequency.value = "";
                  }
                });
              });
              this.templateRecipelList = responseData.data.rows;
            } else {
            }
            this.resetLoad();
          })
          .catch((error) => {
          });
      },
      //获取输液处方
      getInfuSion(element) {
        let infusion = {
          defaultNumber: 1, //Số nhóm mặc định
          infusionProject: [
            []
          ],
          drippingSpeed: [""], //Tốc độ truyền
          days: [{}], //Số ngày
          frequency: [{}], //Tần suất
          infuseUse: [{}], //Cách dùng
          zushu: [1],
          excharge: [],
        };
        let count = 0;
        for (let i = 0; i < element.length; i++) {
          if (element[i].isExtra === 0) {
            if (count < element[i].infuseGroup) {
              count = element[i].infuseGroup;
            }
          }
        }

        for (let i = 1; i < count; i++) {
          infusion.defaultNumber = infusion.defaultNumber + 1;
          infusion.zushu.push(infusion.defaultNumber);
          infusion.infusionProject.push([]);
          infusion.drippingSpeed.push("");
          infusion.days.push({});
          infusion.frequency.push({});
          infusion.infuseUse.push({});
        }
        //this.excharge=null
        let arr = [];
        for (let i = 0; i < element.length; i++) {
          if (element[i].isExtra !== 1) {
            infusion.infusionProject[element[i].infuseGroup - 1].push(element[i]);
            infusion.drippingSpeed[element[i].infuseGroup - 1] =
              element[i].drippingSpeed;
            infusion.days[element[i].infuseGroup - 1] = element[i].days;
            infusion.frequency[element[i].infuseGroup - 1] = element[i].frequency;
            infusion.infuseUse[element[i].infuseGroup - 1] = element[i].infuseUse;
          } else {
            infusion.excharge.push(element[i]);
            //arr.push(element.recipelDetailEvtList[i])
          }
        }
        return infusion;
      },
      //过滤处方
      filterRecipel(row) {
        let addObject = row;
        addObject = addObject.filter((item) => item.isExtra !== 1);
        return addObject;
      },
      //过滤附加费
      filterAdditional(row) {
        let addObject = row;
        addObject = addObject.filter((item) => item.isExtra !== 0);
        return addObject;
      },

      invoking2(row) {
        // Cách ly cấp phòng khám：过滤掉非本诊所的模板详情
        var currentCompanyId = currentUser.company.id;
        var safeDetails = (row.recipeTemplateDetail || []).filter(function(d) {
          if (d.drugStuffId && d.drugStuffId.drug && d.drugStuffId.drug.company) {
            return d.drugStuffId.drug.company.id === currentCompanyId;
          }
          if (d.drugStuffId && d.drugStuffId.stuff && d.drugStuffId.stuff.company) {
            return d.drugStuffId.stuff.company.id === currentCompanyId;
          }
          if (d.drugStuffId && d.drugStuffId.costItem && d.drugStuffId.costItem.company) {
            return d.drugStuffId.costItem.company.id === currentCompanyId;
          }
          return true;
        });
        if (this.medicalEditTabs[this.singleRecipel].type === "recipelType_0") {
          let recipelDetailEvtLists = safeDetails.filter(
            (item) => item.isExtra === 0
          );
          if (
            this.medicalEditTabs[this.singleRecipel].content.recipelDetailEvtList
              .length +
            recipelDetailEvtLists.length >
            5
          ) {
            this.$message.error("Sau khi gọi đơn thuốc, số lượng thuốc vượt quá5cái，Vui lòng lập lại đơn thuốc！");
            return;
          }
        }

        if (
          this.medicalEditTabs[this.singleRecipel].content.recipelDetailEvtList
            .length === 0
        ) {
          this.medicalEditTabs[this.singleRecipel].content.recipelDetailEvtList =
            safeDetails;
          setTimeout(() => {
            this.MedicalCalculate();
          }, 0);
        } else if (
          this.medicalEditTabs[this.singleRecipel].type === "recipelType_0"
        ) {
          this.medicalEditTabs[this.singleRecipel].content.recipelDetailEvtList =
            safeDetails;
          setTimeout(() => {
            this.MedicalCalculate();
          }, 0);
        } else if (
          this.medicalEditTabs[this.singleRecipel].type === "recipelType_2"
        ) {
          this.medicalEditTabs[this.singleRecipel].content.recipelDetailEvtList =
            safeDetails;
          setTimeout(() => {
            this.MedicalCalculate();
          }, 0);
        } else {
          this.$confirm("Đơn thuốc này đã điền，Vui lòng chọn ghi đè hay thêm", "Nhắc nhở", {
            confirmButtonText: "Ghi đè",
            cancelButtonText: "Thêm",
            type: "warning",
          })
            .then(() => {
              this.medicalEditTabs[
                this.singleRecipel
                ].content.recipelDetailEvtList = safeDetails;
              setTimeout(() => {
                this.MedicalCalculate();
              }, 0);
            })
            .catch(() => {
              let flag = true;
              let arr = [];
              safeDetails.forEach((item) => {
                this.medicalEditTabs[
                  this.singleRecipel
                  ].content.recipelDetailEvtList = this.medicalEditTabs[
                  this.singleRecipel
                  ].content.recipelDetailEvtList.filter(
                  (recipelDetai) =>
                    //Giữ logic báo lỗi khi có thuốc giống nhau
                    // if(item.drugStuffId.drugStuffId===recipelDetai.drugStuffId.drugStuffId){
                    //   this.$message({
                    //     type: 'error',
                    //     message: 'Có thuốc giống nhau，Không thể gọi đơn thuốc！'
                    //   });
                    //   flag=false;
                    //   return;
                    // }
                    // if(){

                    // }

                    //Dưới đây là ghi đè trực tiếp
                    item.drugStuffId.drugStuffId !==
                    recipelDetai.drugStuffId.drugStuffId
                );
              });

              row.recipeTemplateDetail.forEach((item) => {
                this.medicalEditTabs[
                  this.singleRecipel
                  ].content.recipelDetailEvtList.push(item);
              });

              setTimeout(() => {
                this.MedicalCalculate();
              }, 0);
            });
        }
        if (this.medicalEditTabs[this.singleRecipel].type === "recipelType_1") {
          this.medicalEditTabs[this.singleRecipel].content.recipelInfo.dosage =
            row.recipeTemplateInfo.dosage;
          this.medicalEditTabs[this.singleRecipel].content.recipelInfo.frequency =
            row.recipeTemplateInfo.frequency;
          this.medicalEditTabs[
            this.singleRecipel
            ].content.recipelInfo.takeFrequency =
            row.recipeTemplateInfo.takeFrequency;
          this.medicalEditTabs[
            this.singleRecipel
            ].content.recipelInfo.singleDosage =
            row.recipeTemplateInfo.singleDosage;
          this.medicalEditTabs[
            this.singleRecipel
            ].content.recipelInfo.recipelUse = row.recipeTemplateInfo.recipelUse;
        } else if (
          this.medicalEditTabs[this.singleRecipel].type === "recipelType_2"
        ) {
          let infusion = {
            defaultNumber: 1, //Số nhóm mặc định
            infusionProject: [
              []
            ],
            drippingSpeed: [""], //Tốc độ truyền
            days: [{}], //Số ngày
            frequency: [{}], //Tần suất
            infuseUse: [{}], //Cách dùng
            zushu: [1],
            excharge: [],
          };
          let count = 0;
          for (let i = 0; i < row.recipeTemplateDetail.length; i++) {
            if (row.recipeTemplateDetail[i].isExtra === 0) {
              if (count < row.recipeTemplateDetail[i].infuseGroup) {
                count = row.recipeTemplateDetail[i].infuseGroup;
              }
            }
          }

          for (let i = 1; i < count; i++) {
            infusion.defaultNumber = infusion.defaultNumber + 1;
            infusion.zushu.push(infusion.defaultNumber);
            infusion.infusionProject.push([]);
            infusion.drippingSpeed.push("");
            infusion.days.push({});
            infusion.frequency.push({});
            infusion.infuseUse.push({});
          }
          //this.excharge=null
          let arr = [];
          for (let i = 0; i < row.recipeTemplateDetail.length; i++) {
            if (row.recipeTemplateDetail[i].isExtra !== 1) {
              infusion.infusionProject[
              row.recipeTemplateDetail[i].infuseGroup - 1
                ].push(row.recipeTemplateDetail[i]);
              infusion.drippingSpeed[
              row.recipeTemplateDetail[i].infuseGroup - 1
                ] = row.recipeTemplateDetail[i].drippingSpeed;
              infusion.days[row.recipeTemplateDetail[i].infuseGroup - 1] =
                row.recipeTemplateDetail[i].days;
              infusion.frequency[row.recipeTemplateDetail[i].infuseGroup - 1] =
                row.recipeTemplateDetail[i].frequency;
              infusion.infuseUse[row.recipeTemplateDetail[i].infuseGroup - 1] =
                row.recipeTemplateDetail[i].infuseUse;
            } else {
              infusion.excharge.push(row.recipeTemplateDetail[i]);
              //arr.push(element.recipelDetailEvtList[i])
            }
          }
          this.medicalEditTabs[this.singleRecipel].infusion = infusion;
        }
        if (
          row.recipeTemplateInfo.entrust !== undefined &&
          row.recipeTemplateInfo.entrust !== ""
        ) {
          this.medicalEditTabs[this.singleRecipel].content.recipelInfo.entrust =
            row.recipeTemplateInfo.entrust;
        }
        setTimeout(() => {
          this.MedicalCalculate();
        }, 0);
        this.templateDialogVisible = false;
      },
      medicalHistoryInvoking(row) {
        this.ChooseRecordList = [];
        this.MedicalRecordModel = row.medicalRecord
        this.MedicineRecordLabelList.map((item) => {
          if (this.MedicalRecordModel[`${item.name}`]) {
            this.ChooseRecordList.push(item);
          }
        });
        this.caseDialogVisible = false;
      },

      invoking(row) {
        if (this.medicalEditTabs[this.singleRecipel].type === "recipelType_0") {
          if (
            this.medicalEditTabs[this.singleRecipel].content.recipelDetailEvtList
              .length +
            row.recipelDetail.length >
            5
          ) {
            this.$message.error("Sau khi gọi đơn thuốc, số lượng thuốc vượt quá5cái，Vui lòng lập lại đơn thuốc！");
            return;
          }
        }
        if (
          this.medicalEditTabs[this.singleRecipel].content.recipelDetailEvtList
            .length === 0
        ) {
          this.medicalEditTabs[this.singleRecipel].content.recipelDetailEvtList =
            row.recipelDetail;
        } else if (
          this.medicalEditTabs[this.singleRecipel].type === "recipelType_2"
        ) {
          this.medicalEditTabs[this.singleRecipel].content.recipelDetailEvtList =
            row.recipelDetail;
          setTimeout(() => {
            this.MedicalCalculate();
          }, 0);
        } else {
          this.$confirm("Đơn thuốc này đã điền，Vui lòng chọn ghi đè hay thêm", "Nhắc nhở", {
            confirmButtonText: "Ghi đè",
            cancelButtonText: "Thêm",
            type: "warning",
          })
            .then(() => {
              this.medicalEditTabs[
                this.singleRecipel
                ].content.recipelDetailEvtList = row.recipelDetail;
              setTimeout(() => {
                this.MedicalCalculate();
              }, 0);
            })
            .catch(() => {
              let flag = true;
              let arr = [];
              row.recipelDetail.forEach((item) => {
                this.medicalEditTabs[
                  this.singleRecipel
                  ].content.recipelDetailEvtList = this.medicalEditTabs[
                  this.singleRecipel
                  ].content.recipelDetailEvtList.filter(
                  (recipelDetai) =>
                    //Giữ logic báo lỗi khi có thuốc giống nhau
                    // if(item.drugStuffId.drugStuffId===recipelDetai.drugStuffId.drugStuffId){
                    //   this.$message({
                    //     type: 'error',
                    //     message: 'Có thuốc giống nhau，Không thể gọi đơn thuốc！'
                    //   });
                    //   flag=false;
                    //   return;
                    // }
                    // if(){

                    // }

                    //Dưới đây là ghi đè trực tiếp
                    item.drugStuffId.drugStuffId !==
                    recipelDetai.drugStuffId.drugStuffId
                );
              });

              row.recipelDetail.forEach((item) => {
                this.medicalEditTabs[
                  this.singleRecipel
                  ].content.recipelDetailEvtList.push(item);
              });

              setTimeout(() => {
                this.MedicalCalculate();
              }, 0);
            });
        }
        if (this.medicalEditTabs[this.singleRecipel].type === "recipelType_1") {
          this.medicalEditTabs[this.singleRecipel].content.recipelInfo.dosage =
            row.dosage;
          this.medicalEditTabs[this.singleRecipel].content.recipelInfo.frequency =
            row.frequency;
          this.medicalEditTabs[
            this.singleRecipel
            ].content.recipelInfo.takeFrequency = row.takeFrequency;
          this.medicalEditTabs[
            this.singleRecipel
            ].content.recipelInfo.singleDosage = row.singleDosage;
          this.medicalEditTabs[
            this.singleRecipel
            ].content.recipelInfo.recipelUse = row.recipelUse;
        } else if (
          this.medicalEditTabs[this.singleRecipel].type === "recipelType_2"
        ) {
          let infusion = {
            defaultNumber: 1, //Số nhóm mặc định
            infusionProject: [
              []
            ],
            drippingSpeed: [""], //Tốc độ truyền
            days: [{}], //Số ngày
            frequency: [{}], //Tần suất
            infuseUse: [{}], //Cách dùng
            zushu: [1],
            excharge: [],
          };
          let count = 0;
          for (let i = 0; i < row.recipelDetail.length; i++) {
            if (row.recipelDetail[i].isExtra === 0) {
              if (count < row.recipelDetail[i].infuseGroup) {
                count = row.recipelDetail[i].infuseGroup;
              }
            }
          }

          for (let i = 1; i < count; i++) {
            infusion.defaultNumber = infusion.defaultNumber + 1;
            infusion.zushu.push(infusion.defaultNumber);
            infusion.infusionProject.push([]);
            infusion.drippingSpeed.push("");
            infusion.days.push({});
            infusion.frequency.push({});
            infusion.infuseUse.push({});
          }
          //this.excharge=null
          let arr = [];
          for (let i = 0; i < row.recipelDetail.length; i++) {
            if (row.recipelDetail[i].isExtra !== 1) {
              infusion.infusionProject[row.recipelDetail[i].infuseGroup - 1].push(
                row.recipelDetail[i]
              );
              infusion.drippingSpeed[row.recipelDetail[i].infuseGroup - 1] =
                row.recipelDetail[i].drippingSpeed;
              infusion.days[row.recipelDetail[i].infuseGroup - 1] =
                row.recipelDetail[i].days;
              infusion.frequency[row.recipelDetail[i].infuseGroup - 1] =
                row.recipelDetail[i].frequency;
              infusion.infuseUse[row.recipelDetail[i].infuseGroup - 1] =
                row.recipelDetail[i].infuseUse;
            } else {
              infusion.excharge.push(row.recipelDetail[i]);
              //arr.push(element.recipelDetailEvtList[i])
            }
          }
          this.medicalEditTabs[this.singleRecipel].infusion = infusion;
        }
        if (row.entrust !== undefined && row.entrust !== "") {
          this.medicalEditTabs[this.singleRecipel].content.recipelInfo.entrust =
            row.entrust;
        }
        setTimeout(() => {
          this.MedicalCalculate();
        }, 0);
        this.historyDialogVisible = false;
      },
      resetCondition() {
        this.queryModel = {
          dateRange: [],
        };
        this.historyRecipel(this.historyType);
      },
      onSearch() {
        this.historyRecipel(this.historyType);
      },
      medicalHistoryOnSearch() {
        this.HistoricalCases();
      },
      medicalHistoryReset() {
        this.queryModel = {
          dateRange: [],
        };
        this.HistoricalCases();
      },
      checkDetails(row, expandedRows) {
        getByRecipelInfoId(expandedRows[0].id)
          .then((res) => {
            if (res.code === 100) {
              this.historyRecipelDetailList = res.data;
            }
          })
          .catch(() => {
          });
      },
      GetCreateBy(row) {
        let str = "";
        let count = 0;
        for (let i = 0; i < row.updateBy.length; i++) {
          if (row.updateBy[i] === "(") {
            count = i;
          }
        }
        str = row.updateBy.substring(0, count);
        return str;
      },
      historyRecipel(item, index) {
        this.singleRecipel = index;
        this.historyType = item;
        let search = {
          columnName: "",
          limit: 1000,
          offset: 0,
          order: this.clickId,
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
            {
              columnName: "recipel_type",
              queryType: "=",
              value: item,
            },
          ],
        };
        if (this.queryModel.dateRange.length > 0) {
          search.params.push({
            logic: "AND",
            queryType: "(",
          }, {
            columnName: "update_date",
            logic: "",
            queryType: "between",
            value: this.queryModel.dateRange,
          }, {
            logic: "",
            queryType: ")",
          });
        }
        //  {
        //       logic: "AND",
        //       queryType: "("
        //     },
        //     {
        //       columnName: "create_date",
        //       logic: "",
        //       queryType: 'between',
        //       value: [],
        //     },
        //     {
        //       logic: "",
        //       queryType: ")"
        //     },
        getHistoryRecipel(search)
          .then((res) => {
            if (res.code === 100) {
              this.historyDialogVisible = true;
              this.historyRecipelList = res.data;
            }
          })
          .catch((error) => {
            this.$message.error(error);
          });
      },
      HistoricalCases() {
        let search = {
          columnName: "",
          limit: 1000,
          offset: 0,
          order: this.clickId,
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          }],
        };
        if (this.queryModel.dateRange.length > 0) {
          search.params.push({
            logic: "AND",
            queryType: "(",
          }, {
            columnName: "update_date",
            logic: "",
            queryType: "between",
            value: this.queryModel.dateRange,
          }, {
            logic: "",
            queryType: ")",
          });
        }
        getHistoryRecipel(search)
          .then((res) => {
            if (res.code === 100) {
              this.caseDialogVisible = true;
              this.historicalCasesList = res.data;
            }
          })
          .catch((error) => {
            this.$message.error(error);
          });
      },

      //Đơn thuốc mẫu
      templateRecipel(item, index) {
        this.templateType = item;
        this.singleRecipel = index;
        let search = {
          columnName: "",
          limit: 1000,
          offset: 0,
          order: "",
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
            {
              columnName: "type",
              queryType: "=",
              value: item,
            },
          ],
        };
        search.params.push({
          logic: "AND",
          queryType: "(",
        }, {
          columnName: "category",
          logic: "",
          queryType: "=",
          value: 0,
        }, {
          columnName: "create_id",
          logic: "and",
          queryType: "=",
          value: currentUser.id,
        }, {
          columnName: "category",
          logic: "or",
          queryType: "=",
          value: 1,
        }, {
          logic: "",
          queryType: ")",
        });
        listRecipetemplatePage(search)
          .then((responseData) => {
            if (responseData.code === 100) {
              this.templateRecipelList = responseData.data.rows;
              this.templateDialogVisible = true;
              // this.recipetemplateTotal = responseData.data.total
              // this.recipetemplateList = responseData.data.rows
            } else {
              // this.showMessage(responseData)
            }
            this.resetLoad();
          })
          .catch((error) => {
            //this.$message.error("Dữ liệu hệ thống bất thường, vui lòng xác nhận dữ liệu có sai sót hay không hoặc liên hệ quản trị viên để xử lý!")
          });
      },

      changesDosage(row, index) {
        if (row === "") {
          this.medicalEditTabs[index].recipelInfo.dosage = " ";
        } else {
          this.medicalEditTabs[index].recipelInfo.dosage = row - 0;
        }

        this.MedicalCalculate();
      },
      changeSingleDosage(row, index, item) {
        this.medicalEditTabs.forEach((tabElement) => {
          if (tabElement.type === "recipelType_1" && tabElement.key === item.key) {
            //for (let i = 0; i < tabElement.content.recipelDetailEvtList.length; i++) {
            if (row === "") {
              tabElement.content.recipelDetailEvtList[index].singleDosage = " ";
            } else {
              tabElement.content.recipelDetailEvtList[index].singleDosage = row;
            }
            //}
          }
        });
        this.MedicalCalculate();
      },
      changeUse(row) {
        this.changeData = row;
      },
      addGroup(item) {
        item.infusion.defaultNumber = item.infusion.defaultNumber + 1;
        item.infusion.zushu.push(item.infusion.defaultNumber);
        item.infusion.infusionProject.push([]);
        item.infusion.drippingSpeed.push("");
        item.infusion.days.push({});
        item.infusion.frequency.push({});
        item.infusion.infuseUse.push({});
        this.SearchInfusion.push("");
      },
      minusGroup(item, index) {
        if (item.infusion.defaultNumber > 1) {
          item.infusion.defaultNumber = item.infusion.defaultNumber - 1;
          item.infusion.zushu.splice(index, 1);
          item.infusion.infusionProject.splice(index, 1);
          for (let i = 0; i < item.infusion.infusionProject.length; i++) {
            for (let j = 0; j < item.infusion.infusionProject[i].length; j++) {
              item.infusion.infusionProject[i][j].infuseGroup = i + 1;
            }
          }
          item.infusion.drippingSpeed.splice(index, 1);
          item.infusion.days.splice(index, 1);
          item.infusion.frequency.splice(index, 1);
          item.infusion.infuseUse.splice(index, 1);
          this.SearchInfusion.splice(index, 1);
        }
      },
      indexMethod(index) {
        index + 1;
        return this.defaultNumber;
      },

      //编辑保存
      save(type) {
        if (this.savelock) {
          this.$message.error("Đang lưu, vui lòng đợi");
          return;
        }
        let FormCheck = true;
        this.$refs["BasicInfoForm"].validate((valid) => {
          if (valid) {
            //alert('submit!');
          } else {
            FormCheck = false;
            return false;
          }
        });

        if (!FormCheck) {
          this.$message.error("Vui lòng điền lý do khám chính");
          return;
        }

        if (this.beyondInventoryType) {
          this.$message.error("Có số lượng thuốc vượt quá tổng tồn kho khả dụng,Vui lòng điền lại!");
          return;
        }

        if (this.beyondStuffInventoryType) {
          this.$message.error("Có số lượng vật tư vượt quá tổng tồn kho khả dụng，Vui lòng điền lại!");
          return;
        }

        let infuseFlage = false;
        let flag1 = false;
        let flag2 = false;
        let treatment = false;
        let chargeflag = false;
        let flagChinese1 = false;
        let flagChinese2 = false;
        let recipelInfoEvtList = [];
        let returnForEach = false;
        let hasSelsmallType = false;
        let numberCheck = false;
        let totalCheck = false;
        let paramsCheck = false;
        // if (this.medicalEditTabs.length === 0) {
        //   this.$message.error("Số lượng đơn thuốc không được là0");
        //   return;
        // }
        this.medicalEditTabs.forEach((tabElement) => {
          //Đơn thuốc tây
          if (
            tabElement.type === "recipelType_0" &&
            !this.MedicalRecordModel.westernDiagnose
          ) {
            returnForEach = true;
          }
          if (tabElement.type === "recipelType_0") {
            let total = 0;
            tabElement.content.recipelDetailEvtList.forEach((item) => {
              if (item.isExtra === 0) {
                if (
                  item.singleDosage &&
                  item.frequency &&
                  item.frequency.value &&
                  item.days &&
                  item.days.name
                ) {
                  total = Math.ceil(
                    BigNumber(item.singleDosage)
                      .multipliedBy(item.frequency.value.split("_")[1])
                      .multipliedBy(item.days.name)
                      .toNumber()
                  );
                  if (
                    item.drugStuffId.drug.total !== 0 &&
                    total > item.drugStuffId.drug.total
                  ) {
                    this.$message.error(
                      item.drugStuffId.name + "Giới hạn tổng lượng phải nhỏ hơn tổng lượng tính theo hướng dẫn công dụng，Giới hạn tổng lượng kê thuốc là" + item.drugStuffId.drug.total + item.drugStuffId.drug.dosisUnit.name
                    );
                    totalCheck = true;
                  }
                } else {
                  this.$message.error(tabElement.title + "Thông tin chưa điền đầy đủ");
                  paramsCheck = true;
                }
              }
            });
          }
          //Đơn thuốc đông y
          if (
            tabElement.type === "recipelType_1" &&
            !this.MedicalRecordModel.chinaDiagnose
          ) {
            returnForEach = true;
          }
          if (tabElement.type === "recipelType_1") {
            if (tabElement.content.recipelInfo.dosage === "") {
              flagChinese1 = true;
            }

            for (
              let i = 0; i < tabElement.content.recipelDetailEvtList.length; i++
            ) {
              if (tabElement.content.recipelDetailEvtList[i].isExtra === 0) {
                if (
                  tabElement.content.recipelDetailEvtList[i].singleDosage ===
                  " " ||
                  tabElement.content.recipelDetailEvtList[i].singleDosage === ""
                ) {
                  flagChinese2 = true;
                }
              }
              if (tabElement.content.recipelDetailEvtList[i].stuffType === '1'){
                if (tabElement.content.recipelDetailEvtList[i].drugStuffId.drug.total !== 0 &&
                  tabElement.content.recipelDetailEvtList[i].total > tabElement.content.recipelDetailEvtList[i].drugStuffId.drug.total) {
                  this.$message.error(
                    tabElement.content.recipelDetailEvtList.drugStuffId.name + "Giới hạn tổng lượng phải nhỏ hơn tổng lượng tính theo hướng dẫn công dụng，Giới hạn tổng lượng kê thuốc là" + tabElement.content.recipelDetailEvtList[i].drugStuffId.drug.total + tabElement.content.recipelDetailEvtList[i].drugStuffId.drug.dosisUnit.name
                  );
                  totalCheck = true;
                }
              }


              /* tabElement.content.recipelDetailEvtList.forEach((item) => {
                 if (item.drugStuffId.drug.total !== 0 &&
                   tabElement.content.recipelDetailEvtList[i].total > item.drugStuffId.drug.total) {
                   this.$message.error(
                     item.drugStuffId.name + "Giới hạn tổng lượng phải nhỏ hơn tổng lượng tính theo hướng dẫn công dụng，Giới hạn tổng lượng kê thuốc là" + item.drugStuffId.drug.total + item.drugStuffId.drug.dosisUnit.name
                   );
                   totalCheck = true;
                 }
               })*/
            }


          }
          //Đơn truyền dịch
          if (tabElement.type === "recipelType_2") {
            tabElement.content.recipelDetailEvtList = [];
            let arr = 0;
            for (let i = 0; i < tabElement.infusion.infusionProject.length; i++) {
              for (
                let j = 0; j < tabElement.infusion.infusionProject[i].length; j++
              ) {
                tabElement.infusion.infusionProject[i][j].drippingSpeed =
                  tabElement.infusion.drippingSpeed[
                  tabElement.infusion.infusionProject[i][j].infuseGroup - 1
                    ];
                tabElement.infusion.infusionProject[i][j].days =
                  tabElement.infusion.days[
                  tabElement.infusion.infusionProject[i][j].infuseGroup - 1
                    ];
                tabElement.infusion.infusionProject[i][j].frequency =
                  tabElement.infusion.frequency[
                  tabElement.infusion.infusionProject[i][j].infuseGroup - 1
                    ];
                tabElement.infusion.infusionProject[i][j].infuseUse =
                  tabElement.infusion.infuseUse[
                  tabElement.infusion.infusionProject[i][j].infuseGroup - 1
                    ];
                tabElement.content.recipelDetailEvtList[arr] =
                  tabElement.infusion.infusionProject[i][j];
                arr++;

                if (
                  tabElement.infusion.days[
                  tabElement.infusion.infusionProject[i][j].infuseGroup - 1
                    ].name === "" ||
                  tabElement.infusion.days[
                  tabElement.infusion.infusionProject[i][j].infuseGroup - 1
                    ].name === undefined
                ) {
                  flag1 = true;
                }
                if (
                  tabElement.infusion.frequency[
                  tabElement.infusion.infusionProject[i][j].infuseGroup - 1
                    ].value === "" ||
                  tabElement.infusion.frequency[
                  tabElement.infusion.infusionProject[i][j].infuseGroup - 1
                    ].value === undefined
                ) {
                  flag2 = true;
                }
              }
            }
            for (let i = 0; i < tabElement.infusion.excharge.length; i++) {
              tabElement.content.recipelDetailEvtList.push(
                tabElement.infusion.excharge[i]
              );
            }
            tabElement.content.recipelDetailEvtList.forEach((item) => {
              if (item.isExtra === 0) {
                if (
                  item.singleDosage &&
                  item.frequency &&
                  item.frequency.value &&
                  item.days &&
                  item.days.name
                ) {
                } else {
                  this.$message.error(tabElement.title + "Thông tin chưa điền đầy đủ");
                  infuseFlage = true;
                }
              }
            });
          }
          //Hạng mục khám chữa bệnh
          if (tabElement.type === "recipelType_3") {
            for (
              let i = 0; i < tabElement.content.recipelDetailEvtList.length; i++
            ) {
              if (tabElement.content.recipelDetailEvtList[i].isExtra === 0) {
                if (
                  tabElement.content.recipelDetailEvtList[i].total === "" ||
                  tabElement.content.recipelDetailEvtList[i].singleDosage === "" ||
                  tabElement.content.recipelDetailEvtList[i].total === 0 ||
                  tabElement.content.recipelDetailEvtList[i].singleDosage === 0
                ) {
                  treatment = true;
                }
              }
            }
          }
          //Phí phụ thêm
          for (
            let i = 0; i < tabElement.content.recipelDetailEvtList.length; i++
          ) {
            if (tabElement.content.recipelDetailEvtList[i].isExtra === 1) {
              if (
                tabElement.content.recipelDetailEvtList[i].total === "" ||
                tabElement.content.recipelDetailEvtList[i].singleDosage === "" ||
                tabElement.content.recipelDetailEvtList[i].total === 0 ||
                tabElement.content.recipelDetailEvtList[i].singleDosage === 0
              ) {
                chargeflag = true;
              }
            }
          }
          if (
            !tabElement.content.recipelInfo.smallType &&
            tabElement.type !== "recipelType_3"
          ) {
            hasSelsmallType = true;
          }
          if (tabElement.content.recipelDetailEvtList.length === 0) {
            numberCheck = true;
          }
          recipelInfoEvtList.push(tabElement.content);
        });

        if (returnForEach) {
          this.$message.error("Có đơn thuốc Tây y hoặc Đông y，Vui lòng điền giải thích chẩn đoán tương ứng。");
          return;
        }

        if (hasSelsmallType) {
          this.$message.error("Vui lòng kiểm tra phân loại trong mỗi đơn thuốc đã chọn chưa。");
          return;
        }

        if (numberCheck) {
          this.$message.error("Vui lòng kiểm tra mỗi đơn thuốc có thuốc chưa。");
          return;
        }
        if (totalCheck) {
          return;
        }
        if (paramsCheck) {
          return;
        }
        if (infuseFlage) {
          return;
        }
        if (flag1) {
          this.$message.error("Vui lòng điền số ngày truyền dịch");
          return;
        }
        if (flag2) {
          this.$message.error("Vui lòng điền tần suất truyền dịch");
          return;
        }
        if (flagChinese1) {
          this.$message.error("Vui lòng điền liều thuốc Đông y");
          return;
        }
        if (flagChinese2) {
          this.$message.error("Vui lòng điền số lượng thuốc Đông y");
          return;
        }
        if (treatment) {
          this.$message.error("Vui lòng điền số lượng hạng mục của đơn khám chữa bệnh");
          return;
        }
        if (chargeflag) {
          this.$message.error("Vui lòng điền số lượng hạng mục phụ phí");
          return;
        }

        this.MedicalRecordModel.company = this.Company;
        this.MedicalRecordModel.doctor.id = this.UserId;

        let medicalRecipelEvt = {
          id: this.registration.id,
          registration: this.registration,
          medicalRecord: this.MedicalRecordModel,
          patient: this.BasicInfoModel,
          recipelInfoEvtList: recipelInfoEvtList,
          type: 0,
          deleteFileIdList: this.deleteFileIdList,
        };
        // return
        let formData = new FormData();
        formData.append("entity", JSON.stringify(medicalRecipelEvt));
        this.pushMedicalFiles(formData)
        this.savelock = true;
        editSave(formData).then((responseData) => {
          if (responseData.code === 100) {
            this.$message.success("Thao tác thành công！");
            this.hasBtnFinishVisit = false;
            this.hasBtnPrint = false;
            this.isReadOnly = true;
            this.medicalEditTabs.forEach((item) => {
              item.closable = false;
            });
            this.hint = false;
            this.endHint = true;
            if (type === 2) {
              let itemCopy = JSON.parse(
                JSON.stringify(this.activeSelAlreadyPatientItem)
              );
              this.registration = JSON.parse(
                JSON.stringify(this.activeSelAlreadyPatientItem)
              );
              this.$nextTick(() => {
                this.MedicalRecordModel = this.ReturnMedicalModel();

                this.hasBtnFinishVisit = false;
                this.hasBtnPrint = true;
                this.isReadOnly = true;
                this.recipelInfoId = "";
                this.AlreadyPatientDescriptions = [];

                allQueryMedicalRecord(this.registration.id).then((responseData) => {
                    let arr = responseData.data;
                    if (responseData.code === 100) {
                      let recipelInfoEvtList =
                        responseData.data.recipelInfoEvtList;
                      recipelInfoEvtList.forEach((element) => {
                        this.AlreadyPatientDescriptions.push(
                          JSON.parse(JSON.stringify(element
                            .recipelInfo))
                        );
                      });

                      this.AlreadyPatientDescriptions.sort((a, b) => {
                        a.seq = a.seq ? a.seq : 0;
                        b.seq = b.seq ? b.seq : 0;
                        return a.seq - b.seq;
                      });
                      this.MedicalRecordModel = responseData.data.medicalRecord;
                      this.MedicalRecordModel.registration.id = itemCopy.id;
                      this.MedicalRecordModel.registration.treatType =
                        itemCopy.treatType;
                      this.MedicalRecordModel.registration.morbidityTime =
                        itemCopy.morbidityTime;
                      this.MedicalRecordModel.registration.infectType =
                        itemCopy.infectType;
                      this.BasicInfoModel = responseData.data.patient;
                      this.BasicInfoModel.breathe = this.registration.breathe;
                      this.BasicInfoModel.temperature =
                        this.registration.temperature;
                      this.BasicInfoModel.pulse = this.registration.pulse;
                      this.BasicInfoModel.bloodPressure =
                        this.registration.bloodPressure;
                      this.GetMedicineCofigure();
                      this.createMedicalEditTab(recipelInfoEvtList);
                      //Tệp đính kèm
                      this.changeMedicalFile("view", this.registration.id)
                    }
                  }
                );
              });
            }
            this.GetPreparePatientList();
            this.GetAlreadyPatientList();
          } else if (responseData.code === "50001") {
            this.$message.warning(responseData.msg);
          } else {
            this.$message.error("Cập nhật thất bại!");
          }
          this.savelock = false;
        });
        this.blmbVisit = false;
      },
      pushMedicalFiles(formData) {
        let fileList = this.$refs.medicalFile.getFileList()
        fileList.forEach((item, index) => {
          formData.append("medicalFiles", item.raw)
        })
        return formData
      },
      //Chuyển đổi tải tệp đính kèm lên
      changeMedicalFile(type, objectId) {
        if ("view" === type) {
          //Đã khám
          this.MedicalFileObjId = objectId;
          this.MedicalFlags = "view"
        } else if ("add" === type) {
          //Chờ khám
          this.MedicalFlags = "add"
        } else if ("edit" === type) {
          //Thêm mới
          this.MedicalFlags = "edit"
          this.deleteFileIdList = [];
        }
      },
      //Sửa
      edit() {
        // &&this.medicalClickTabsValue.content.recipelInfo.id
        if (
          (this.medicalClickTabsValue && this.BasicInfoModel.name) ||
          (this.medicalClickTabsValue.content.recipelInfo.id &&
            this.medicalClickTabsValue)
        ) {
          let now = new Date();
          let newDate = new Date(this.activeSelAlreadyPatientItem.createDate);
          now = Date.parse(now);
          newDate = Date.parse(newDate);
          if (this.timeOut) {
            this.$message.error("Dữ liệu quá một ngày không thể chỉnh sửa");
            return;
          }
          this.isReadOnly = false;
          this.endHint = false;
          this.blmbVisit = true;
          // this.chineseTest=[[]]
          //  for (let i = 1; i < this.medicalEditTabs.length; i++) {
          //   if(this.medicalEditTabs[i].type==="recipelType_1"){
          //    this.chineseTest.push([])
          //   }
          // }

          // for (let i = 0; i < this.medicalEditTabs.length; i++) {
          //   if(this.medicalEditTabs[i].type==="recipelType_1"){
          //     for (let j = 0; j < this.medicalEditTabs[i].content.recipelDetailEvtList.length; j++) {
          //       this.chineseTest[this.medicalEditTabs[i].key-1].push(this.medicalEditTabs[i].content.recipelDetailEvtList[j].drugStuffId.name)
          //     }
          //   }
          // }
        } else {
          this.$message.error("Chưa chọn người dùng");
        }
        //Chuyển đổi tải tệp đính kèm lên
        this.changeMedicalFile("edit", "");
      },
      print() {
        if (!this.activeSelAlreadyPatientItem) {
          return;
        }
        let filterArr = this.medicalEditTabs.filter(
          (item) => item.content.recipelInfo.status !== -1
        );
        let arr = [];
        for (let i = 0; i < filterArr.length; i++) {
          arr.push("");
        }
        filterArr.forEach((item, index) => {
          let params = {
            recipelInfoId: item.content.recipelInfo.id,
            type: item.content.recipelInfo.recipelType.value,
          };
          let str = "";
          let type =
            params.type === "recipelType_0" ?
              // "westMedicine" :
              "chronicDisease" :
              params.type === "recipelType_1" ?
                "chineseMedicine" :
                params.type === "recipelType_2" ?
                  "infuse" :
                  "costItem";
          if (params.recipelInfoId) {
            str = "&recipelInfoId=" + params.recipelInfoId;
          }

          arr[index] = window.open(
            this.BASE_API +
            "/ureport/preview?_u=Newtouch:" +
            type +
            ".ureport.xml&_t=0" +
            str +
            "&type=0",
            index
          );

          arr[index].onload = () => {
            arr[index].document.title = "Hệ thống Quản lý Phòng khám";
          };
        });
      },
      handleFileRemove(file, fileList) {
      },
      handleFileProgress(event, file, fileList) {
      },
      handleFileBeforeUpload(file) {
      },
      handlePatientClick(tab, event) {
        this.blmbVisit = false;
        if (this.TodayActiveName === "prepare") {
          this.isReadOnly = true;
          this.ChineseButton = false;
          this.$refs["BasicInfoForm"].clearValidate();
          this.GetPreparePatientList();
          this.clickId = "";
        } else {
          this.userClickID = "";
          this.isReadOnly = true;
          this.ChineseButton = false;
          this.$refs["BasicInfoForm"].clearValidate();
          this.GetAlreadyPatientList();
          this.hint = false;
          this.clickId = "";
        }

        this.backgroundColors = 10000;
        this.overColor = 0;
      },
      //Lấy danh sách bệnh nhân chờ khám
      GetPreparePatientList() {
        let params = [{
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
            columnName: "doctor_id",
            queryType: "=",
            value: this.UserId,
          },
        ];
        if (this.SearchPatientInfo) {
          params.push({
            logic: "AND",
            queryType: "(",
          });
          params.push({
            logic: "",
            columnName: "patientId.name",
            queryType: "like",
            value: this.SearchPatientInfo,
          });
          params.push({
            logic: "OR",
            columnName: "patientId.phone",
            queryType: "like",
            value: this.SearchPatientInfo,
          });
          params.push({
            logic: "OR",
            columnName: "patientId.card",
            queryType: "like",
            value: this.SearchPatientInfo,
          });
          params.push({
            logic: "",
            queryType: ")",
          });
        }
        this.SearchPreModel.params = params;
        this.SearchPreModel.params[1].value = "registrationStatus_0";
        listRegistrationPage(this.SearchPreModel).then((responseData) => {
          if (responseData.code === 100) {
            this.PreparePatientTotal = responseData.data.total;
            if (responseData.data.rows) {
              responseData.data.rows.forEach((element) => {
                let birthday = element.patientId.birthday;
                if (birthday) {
                  const duration = moment.duration(moment().diff(birthday));
                  element.patientId.birthdayText = duration.years();
                } else {
                  element.patientId.birthdayText = "--";
                }
                element.formatUpdateDate = moment(element.updateDate).format(
                  "MM/DD HH:mm"
                );
              });
            }
            this.PreparePatientList = responseData.data.rows;
          }
        });
      },
      //Lấy danh sách bệnh nhân đã khám
      GetAlreadyPatientList() {
        let params = [{
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
            columnName: "doctor_id",
            queryType: "=",
            value: this.UserId,
          },
        ];
        if (this.SearchPatientInfo) {
          params.push({
            logic: "AND",
            queryType: "(",
          });
          params.push({
            logic: "",
            columnName: "patientId.name",
            queryType: "like",
            value: this.SearchPatientInfo,
          });
          params.push({
            logic: "OR",
            columnName: "patientId.phone",
            queryType: "like",
            value: this.SearchPatientInfo,
          });
          params.push({
            logic: "OR",
            columnName: "patientId.card",
            queryType: "like",
            value: this.SearchPatientInfo,
          });
          params.push({
            logic: "",
            queryType: ")",
          });
        }
        this.SearchAlreadyModel.params = params;
        listRegistrationPage(this.SearchAlreadyModel).then((responseData) => {
          if (responseData.code === 100) {
            this.AlreadyPatientListTotal = responseData.data.total;
            if (responseData.data.rows) {
              responseData.data.rows.forEach((element) => {
                let birthday = element.patientId.birthday;
                if (birthday) {
                  const duration = moment.duration(moment().diff(birthday));
                  element.patientId.birthdayText = duration.years();
                } else {
                  element.patientId.birthdayText = "--";
                }
                element.formatReceptionEndDate = moment(
                  element.receptionEndDate
                ).format("MM/DD HH:mm");
              });
            }
            this.AlreadyPatientList = responseData.data.rows;
          }
        });
      },
      handleCurrentPreparePatientChange(pageNum) {
        this.SearchPreModel.offset = (pageNum - 1) * this.SearchPreModel.limit;
        this.GetPreparePatientList();
      },
      handleCurrentAlreadyPatientChange(pageNum) {
        this.SearchAlreadyModel.offset =
          (pageNum - 1) * this.SearchAlreadyModel.limit;
        this.GetAlreadyPatientList();
      },
      AlreadyPatientDeletilQuery(row) {
        if (row.patientId === undefined) {
          return;
        }
        this.recordpatid = row.patientId.id;
        this.clickChangeColor = row.id;

        if (row) {
          this.recordpatid = row.patientId.id;
          this.clickChangeColor = row.id;
        }
        if (
          this.clickId !== "" &&
          this.clickId === this.activeSelAlreadyPatientItem.id &&
          !this.cancellation
        ) {
          return;
        } else if (this.activeSelAlreadyPatientItem.id !== undefined) {
          this.cancellation = false;
          if (this.endHint) {
            this.clickId = this.activeSelAlreadyPatientItem.id;
            if (!this.activeSelAlreadyPatientItem) {
              return;
            }
            let now = new Date();
            let newDate = new Date(
              this.activeSelAlreadyPatientItem.receptionEndDate
            );
            now = Date.parse(now);
            newDate = Date.parse(newDate);
            if (now - newDate > 86400000) {
              this.timeOut = true;
            } else {
              this.timeOut = false;
            }
            let itemCopy = JSON.parse(
              JSON.stringify(this.activeSelAlreadyPatientItem)
            );
            this.registration = JSON.parse(
              JSON.stringify(this.activeSelAlreadyPatientItem)
            );
            this.$nextTick(() => {
              this.MedicalRecordModel = this.ReturnMedicalModel();

              this.hasBtnFinishVisit = false;
              this.hasBtnPrint = true;
              this.isReadOnly = true;
              this.recipelInfoId = "";
              this.AlreadyPatientDescriptions = [];
              //Theo bệnh nhânidLấy thông tin thành viên
              getByPatientId(this.activeSelAlreadyPatientItem.patientId.id)
                .then((res) => {
                  if (res.code === 100) {
                    this.member = res.data;
                  } else {
                    this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！");
                  }
                })
                .catch();
              getPoverty(this.activeSelAlreadyPatientItem.patientId.id)
                .then((res) => {
                  if (res.code === 100) {
                    this.poverty = res.data;
                  } else {
                    this.$message.error("Dữ liệu hộ nghèo bất thường, vui lòng liên hệ quản trị viên！");
                  }
                })
                .catch();
              allQueryMedicalRecord(this.registration.id).then((responseData) => {
                let arr = responseData.data;
                if (responseData.code === 100) {
                  let recipelInfoEvtList = responseData.data.recipelInfoEvtList;
                  recipelInfoEvtList.forEach((element) => {
                    this.AlreadyPatientDescriptions.push(
                      JSON.parse(JSON.stringify(element.recipelInfo))
                    );
                  });
                  this.AlreadyPatientDescriptions.sort((a, b) => {
                    a.seq = a.seq ? a.seq : 0;
                    b.seq = b.seq ? b.seq : 0;
                    return a.seq - b.seq;
                  });
                  this.MedicalRecordModel = responseData.data.medicalRecord;
                  this.MedicalRecordModel.registration.id = itemCopy.id;
                  this.MedicalRecordModel.registration.treatType =
                    itemCopy.treatType;
                  this.MedicalRecordModel.registration.morbidityTime =
                    itemCopy.morbidityTime;
                  this.MedicalRecordModel.registration.infectType =
                    itemCopy.infectType;
                  this.BasicInfoModel = responseData.data.patient;
                  this.BasicInfoModel.breathe = this.registration.breathe;
                  this.BasicInfoModel.temperature = this.registration.temperature;
                  this.BasicInfoModel.pulse = this.registration.pulse;
                  this.BasicInfoModel.bloodPressure =
                    this.registration.bloodPressure;
                  this.GetMedicineCofigure();
                  this.createMedicalEditTab(recipelInfoEvtList);
                }
              });
              // this.GetPatient(itemCopy);
            });
            this.overColor = 0;
          } else {
            this.$confirm(
              "Bệnh nhân hiện tại đang được chỉnh sửa,Chưa lưu, bạn có chắc muốn chuyển không?",
              "Nhắc nhở", {
                confirmButtonText: "Xác nhận",
                cancelButtonText: "Hủy",
                type: "warning",
              }
            ).then(() => {
              this.clickId = this.activeSelAlreadyPatientItem.id;
              if (!this.activeSelAlreadyPatientItem) {
                return;
              }
              let now = new Date();
              let newDate = new Date(
                this.activeSelAlreadyPatientItem.receptionEndDate
              );
              now = Date.parse(now);
              newDate = Date.parse(newDate);

              if (now - newDate > 86400000) {
                this.timeOut = true;
              } else {
                this.timeOut = false;
              }
              let itemCopy = JSON.parse(
                JSON.stringify(this.activeSelAlreadyPatientItem)
              );
              this.registration = JSON.parse(
                JSON.stringify(this.activeSelAlreadyPatientItem)
              );
              this.$nextTick(() => {
                this.MedicalRecordModel = this.ReturnMedicalModel();

                this.hasBtnFinishVisit = false;
                this.hasBtnPrint = true;
                this.isReadOnly = true;
                this.recipelInfoId = "";
                this.AlreadyPatientDescriptions = [];
                //Theo bệnh nhânidLấy thông tin thành viên
                getByPatientId(this.activeSelAlreadyPatientItem.patientId.id)
                  .then((res) => {
                    if (res.code === 100) {
                      this.member = res.data;
                    } else {
                      this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！");
                    }
                  })
                  .catch();
                getPoverty(this.activeSelAlreadyPatientItem.patientId.id)
                  .then((res) => {
                    if (res.code === 100) {
                      this.poverty = res.data;
                    } else {
                      this.$message.error("Dữ liệu hộ nghèo bất thường, vui lòng liên hệ quản trị viên！");
                    }
                  })
                  .catch();
                allQueryMedicalRecord(this.registration.id).then(
                  (responseData) => {
                    let arr = responseData.data;
                    if (responseData.code === 100) {
                      let recipelInfoEvtList =
                        responseData.data.recipelInfoEvtList;
                      recipelInfoEvtList.forEach((element) => {
                        this.AlreadyPatientDescriptions.push(
                          JSON.parse(JSON.stringify(element
                            .recipelInfo))
                        );
                      });
                      this.AlreadyPatientDescriptions.sort((a, b) => {
                        a.seq = a.seq ? a.seq : 0;
                        b.seq = b.seq ? b.seq : 0;
                        return a.seq - b.seq;
                      });
                      this.MedicalRecordModel = responseData.data.medicalRecord;
                      this.MedicalRecordModel.registration.id = itemCopy.id;
                      this.MedicalRecordModel.registration.treatType =
                        itemCopy.treatType;
                      this.MedicalRecordMode
                      this.MedicalRecordModel.registration.morbidityTime =
                        itemCopy.morbidityTime;
                      this.MedicalRecordModel.registration.infectType =
                        itemCopy.infectType;
                      this.BasicInfoModel = responseData.data.patient;
                      this.BasicInfoModel.breathe = this.registration.breathe;
                      this.BasicInfoModel.temperature =
                        this.registration.temperature;
                      this.BasicInfoModel.pulse = this.registration.pulse;
                      this.BasicInfoModel.bloodPressure =
                        this.registration.bloodPressure;
                      this.GetMedicineCofigure();
                      this.createMedicalEditTab(recipelInfoEvtList);
                    }
                  }
                );
              });
              this.overColor = 0;
              this.endHint = true;
            })
              .catch(() => {
                this.endHint = false;
              });
          }
        }
        //Tải tệp đính kèm lên
        this.changeMedicalFile("view", row.id)
      },
      AlreadyPatientDescriptionsQuery(item, index) {
        this.hasBtnFinishVisit = false;
        this.medicalEditTabs.forEach((element) => {
          if (element.content.uuid === item.id) {
            this.medicalEditTabsValue = element;
            this.medicalClickTabsValue = this.medicalEditTabsValue;
          }
        });

        this.recipelInfoId = item.id;
        this.overColor = index;
      },
      clickPreparePatient(item, index) {
        // if(item.id===this.clickId){
        //   return
        // }
        this.blmbVisit = true;
        this.recordpatid = item.patientId.id; //获取用户id
        if (this.userClickID !== "" && this.userClickID === item.id) {
          this.hint = true;
          return;
        }
        if (!this.hint) {
          this.clickId = item.id;
          let itemCopy = JSON.parse(JSON.stringify(item));
          this.registration = JSON.parse(JSON.stringify(item));
          this.$nextTick(() => {
            this.hasBtnFinishVisit = true;
            this.hasBtnPrint = false;
            this.isReadOnly = false;
            this.ChineseButton = true;
            // this.MedicalRecordModel = this.ReturnMedicalModel();
            // this.MedicalRecordModel.registration.id = itemCopy.id;
            // this.MedicalRecordModel.registration.treatType = itemCopy.treatType;
            this.backgroundColors = index;
            this.GetPatient(itemCopy);
            this.GetMedicineCofigure();
            this.medicalEditTabs = [];
            this.MedicalCalculate();
            this.userClickID = item.id;
            this.hint = true;
            // this.MedicalRecordModel=[];
            allQueryMedicalRecord(item.id).then((responseData) => {
              this.MedicalRecordModel = responseData.data.medicalRecord;
              if (responseData.data.medicalRecord != null) {
                this.ChooseRecordList = [];
                this.MedicineRecordLabelList.map((item) => {
                  if (this.MedicalRecordModel[`${item.name}`]) {
                    this.ChooseRecordList.push(item);
                  }
                });
              } else {
                this.MedicalRecordModel = this.ReturnMedicalModel();
                this.MedicalRecordModel.registration.id = itemCopy.id;
                this.MedicalRecordModel.registration.treatType =
                  itemCopy.treatType;
                this.MedicalRecordModel.registration.morbidityTime =
                  itemCopy.morbidityTime;
                this.MedicalRecordModel.registration.infectType =
                  itemCopy.infectType;
                //  this.patientTell="",
                //  this.ClearMedicineCofigure();
              }
            });
            //Theo bệnh nhânidLấy thông tin thành viên
            getByPatientId(item.patientId.id)
              .then((res) => {
                if (res.code === 100) {
                  this.member = res.data;
                } else {
                  this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！");
                }
              })
              .catch();
            getPoverty(item.patientId.id)
              .then((res) => {
                if (res.code === 100) {
                  this.poverty = res.data;
                } else {
                  this.$message.error("Dữ liệu hộ nghèo bất thường, vui lòng liên hệ quản trị viên！");
                }
              })
              .catch();
            // this.addMedicalEditTab(this.medicalTypeList[0]);
          });
        } else {
          this.open(item, index);
        }
        //Tải tệp đính kèm lên
        this.changeMedicalFile("add");
      },
      open(item, index) {
        this.$confirm("Bệnh nhân hiện tại chưa hoàn thành tiếp nhận, bạn có chắc muốn chuyển không?", "Nhắc nhở", {
          confirmButtonText: "Xác nhận",
          cancelButtonText: "Hủy",
          type: "warning",
        })
          .then(() => {
            this.hint = false;
            this.clickPreparePatient(item, index);
          })
          .catch(() => {
            this.hint = true;
          });
      },
      GetPatient(patientId) {
        getPatientById(patientId.patientId.id).then((responseData) => {
          if (responseData.code === 100) {
            this.BasicInfoModel = responseData.data;
            this.BasicInfoModel.breathe = patientId.breathe;
            this.BasicInfoModel.temperature = patientId.temperature;
            this.BasicInfoModel.pulse = patientId.pulse;
            this.BasicInfoModel.bloodPressure = patientId.bloodPressure;
          }
        });
      },
      ReturnMedicalModel() {
        return {
          company: this.Company,
          doctor: {
            id: this.UserId,
          },
          patientTell: "",
          westernDiagnose: "",
          chinaDiagnose: "",
          chinaSyndrome:"",
          diseaseId: "",
          syndromeId: "",
          nowHistory: "",
          beforeHistory: "",
          diagnose: "",
          epidemicDisease: "",
          otherCheck: "",
          handlingSituation: "",
          healthEducation: "",
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
          registration: {
            morbidityTime: "",
            treatType: {
              name: "初诊",
              value: "treatType_0",
            },
            infectType: {
              name: "Không",
              value: "infectType_0",
            }
          },

          basics: {},
        };
      },
      //Thêm mới khi kê đơnTabTab
      // Tải tiền sử dị ứng của bệnh nhân từ hồ sơ khám đã lưu gần nhất.
      // Chỉ đọc dữ liệu sẵn có, không tạo dữ liệu dị ứng mới.
      loadPatientAllergy(patientId) {
        this.patientAllergyHistory = ''
        this.allergyWarningPatientId = ''
        if (!patientId) {
          return
        }
        getLatestAllergyHistory(patientId).then((responseData) => {
          if ((responseData.code === 100 || responseData.code === '100') && responseData.data) {
            this.patientAllergyHistory = responseData.data
          }
        }).catch(() => {})
      },
      // Chặn nhẹ trước khi kê đơn: nếu bệnh nhân có tiền sử dị ứng thì hỏi
      // xác nhận một lần cho mỗi bệnh nhân, hủy thì không mở tab kê đơn.
      confirmAllergyBeforePrescribe(tabTypeData) {
        let patientId = this.BasicInfoModel ? this.BasicInfoModel.id : ''
        if (this.displayAllergyHistory && this.allergyWarningPatientId !== patientId) {
          this.$confirm('Bệnh nhân có tiền sử dị ứng: ' + this.displayAllergyHistory + '. Vẫn tiếp tục kê đơn?', 'Cảnh báo dị ứng', {
            confirmButtonText: 'Vẫn kê đơn',
            cancelButtonText: 'Hủy',
            type: 'warning'
          }).then(() => {
            this.allergyWarningPatientId = patientId
            this.addMedicalEditTab(tabTypeData)
          }).catch(() => {})
        } else {
          this.addMedicalEditTab(tabTypeData)
        }
      },
      addMedicalEditTab(tabTypeData) {
        let tabType = tabTypeData.type;
        let typeMaxTabIndex = 0;
        this.medicalEditTabs.forEach((element) => {
          if (element.type === tabType) {
            typeMaxTabIndex++;
          }
        });

        let isFollowUp =
          (this.MedicalRecordModel.registration.treatType &&
            this.MedicalRecordModel.registration.treatType.value === "treatType_0") ?
            0 :
            1;

        let isFollowUpTo =
          (this.MedicalRecordModel.registration.infectType &&
            this.MedicalRecordModel.registration.infectType.value === "infectType_0") ||
          !this.MedicalRecordModel.registration.infectType ?
            0 :
            1;
        let recipelInfoEvt = {};
        if (tabType === "recipelType_1") {
          recipelInfoEvt = {
            uuid: require("uuid").v1(),
            recipelInfo: {
              company: this.Company,
              recipelType: {
                name: tabTypeData.name,
                value: tabTypeData.type,
              },
              remarks: tabTypeData.name + (typeMaxTabIndex + 1),
              fee: 0,
              status: 1,
              chargeStatus: 0,
              dispensionStatus: 0,
              isFollowUp: isFollowUp,
              isFollowUpTo: isFollowUpTo,
              recipelUse: this.isSpecial ?
                {
                  name: this.ChineseUseOption[0].name,
                  value: this.ChineseUseOption[0].value,
                } :
                {
                  name: this.ChineseUseOption[0].name,
                  value: this.ChineseUseOption[0].value,
                },
              frequency:{
                name: this.ChineseTimeOption[0].name,
                value: this.ChineseTimeOption[0].value,
              },
              takeFrequency:{
                 name: '',
                 value: '',
              },
              smallType: this.RecipelSmallTypeList[0],
              chronicDisease: true,
              chinessNotes: "",

            },
            recipelDetailEvtList: [],
          };
        } else {
          recipelInfoEvt = {
            uuid: require("uuid").v1(),
            recipelInfo: {
              company: this.Company,
              recipelType: {
                name: tabTypeData.name,
                value: tabTypeData.type,
              },
              remarks: tabTypeData.name + (typeMaxTabIndex + 1),
              fee: 0,
              status: 1,
              chargeStatus: 0,
              dispensionStatus: 0,
              isFollowUp: isFollowUp,
              isFollowUpTo: isFollowUpTo,
              smallType: this.RecipelSmallTypeList[0],
            },
            recipelDetailEvtList: [],
          };
        }

        // if (tabTypeData.type === "recipelType_1") {
        //   recipelInfoEvt.recipelInfo["dosage"] = 1;
        // }
        let infusion = {};
        if (tabTypeData.type === "recipelType_2") {
          infusion = {
            // infusionProject:this.infusionProject,
            // drippingSpeed:this.drippingSpeed,  //Tốc độ truyền
            // days:this.days,    //Số ngày
            // frequency:this.frequency,   //Tần suất
            // infuseUse:this.infuseUse,     //Cách dùng
            // zushu:this.zushu,
            defaultNumber: 1, //Số nhóm mặc định
            infusionProject: [
              []
            ],
            drippingSpeed: [""], //Tốc độ truyền
            days: [{}], //Số ngày
            frequency: [{}], //Tần suất
            infuseUse: [{}], //Cách dùng
            zushu: [1],
            excharge: [],
          };
        }
        let costDebug = {
          chineseTest: [],
        };

        this.recipelInfoEvtList.push(recipelInfoEvt);

        let tab = {
          title: tabTypeData.name + (typeMaxTabIndex + 1),
          key: this.medicalEditTabs.length + 1,
          content: recipelInfoEvt,
          closable: true,
          type: tabType,
          infusion: infusion,
          costDebug: costDebug,
        };
        // if(tab.type==="recipelType_1"){
        //   this.chineseTest[tab.key-1]=[]
        // }
        this.SearchWesternInput = "";
        this.SearchChineseInput = "";
        this.SearchCostItemInput = "";
        this.SearchSurchargeInput = "";
        this.SearchInfusion = [""];
        this.medicalEditTabs.push(tab);
        this.medicalEditTabsValue = tab;
        this.medicalClickTabsValue = this.medicalEditTabsValue;
        // // 获取第一个input的焦点
        //  this.$nextTick(() => {
        //   this.$refs.singleDosage-input[0].focus();
        // });
      },
      //开具处方删除TabTab
      removeMedicalEditTab(targetName) {
        let tabs = this.medicalEditTabs;
        let activeName = this.medicalEditTabsValue;
        if (activeName === targetName) {
          tabs.forEach((tab, index) => {
            if (tab === targetName) {
              let nextTab = tabs[index + 1] || tabs[index - 1];
              if (nextTab) {
                activeName = nextTab;
              }
            }
          });
        }

        this.medicalEditTabsValue = activeName;
        this.medicalEditTabs = tabs.filter((tab) => tab !== targetName);
        this.SearchWesternInput = "";
        this.SearchChineseInput = "";
        this.SearchInfusion = [""];
        if (this.medicalEditTabs.length > 0) {
          this.medicalClickTabsValue = this.medicalEditTabsValue;
        } else {
          this.medicalClickTabsValue = {};
        }

        this.MedicalCalculate();
        this.medicalEditTabs = this.medicalEditTabs;
      },

      //开具处方切换Tab标签页触发事件
      clickMedicalEditTab() {
        if (this.medicalEditTabsValue === this.medicalClickTabsValue) {
          return;
        }
        this.SearchWesternInput = "";
        this.medicalClickTabsValue = this.medicalEditTabsValue;
        if (this.TodayActiveName === "already") {
          let num = 0;
          for (let i = 0; i < this.medicalEditTabs.length; i++) {
            if (this.medicalEditTabs[i].key === this.medicalEditTabsValue.key) {
              num = i;
            }
          }
          this.AlreadyPatientDescriptionsQuery(
            this.medicalClickTabsValue.content,
            num
          );
        }
      },
      createMedicalEditTab(recipelInfoEvtList) {
        this.medicalEditTabs = [];
        if (
          recipelInfoEvtList === undefined ||
          recipelInfoEvtList == null ||
          !recipelInfoEvtList.length > 0
        ) {
          this.MedicalCalculate();
          return;
        } else {
          recipelInfoEvtList.forEach((element) => {
            element.uuid = element.recipelInfo.id;
            let costDebug = {
              chineseTest: [],
            };
            for (let i = 0; i < this.medicalEditTabs.length; i++) {
              for (
                let j = 0; j < this.medicalEditTabs[i].content.recipelDetailEvtList.length; j++
              ) {
                costDebug.chineseTest.push(
                  this.medicalEditTabs[i].content.recipelDetailEvtList[j]
                    .drugStuffId.name
                );
              }
            }
            if (element.recipelInfo.recipelType.value === "recipelType_3") {
              this.inspectionType = [];
              this.inspectionSign = 0;

              for (let i = 0; i < element.recipelDetailEvtList.length; i++) {
                if (element.recipelDetailEvtList[i].drugStuffId.costItem) {
                  if (
                    element.recipelDetailEvtList[i].drugStuffId.costItem.itemType
                      .value === "treatmentItemType_0" ||
                    element.recipelDetailEvtList[i].drugStuffId.costItem.itemType
                      .value === "treatmentItemType_1"
                  ) {
                    this.inspectionSign = 1;
                    this.inspectionType.push(0);
                  }
                }
              }
              for (let i = 0; i < element.recipelDetailEvtList.length; i++) {
                if (
                  element.recipelDetailEvtList[i].drugStuffId
                    .inspectionCheckInfo !== undefined
                ) {
                  if (
                    element.recipelDetailEvtList[i].drugStuffId.costItem.itemType
                      .value === "treatmentItemType_0" ||
                    element.recipelDetailEvtList[i].drugStuffId.costItem.itemType
                      .value === "treatmentItemType_1"
                  ) {
                    this.inspectionType[i] = 1;
                  }
                }
              }
            }
            if (element.recipelInfo.recipelType.value === "recipelType_2") {
              let infusion = {
                defaultNumber: 1, //Số nhóm mặc định
                infusionProject: [
                  []
                ],
                drippingSpeed: [""], //Tốc độ truyền
                days: [{}], //Số ngày
                frequency: [{}], //Tần suất
                infuseUse: [{}], //Cách dùng
                zushu: [1],
                excharge: [],
              };
              let count = 0;
              for (let i = 0; i < element.recipelDetailEvtList.length; i++) {
                if (element.recipelDetailEvtList[i].isExtra === 0) {
                  if (count < element.recipelDetailEvtList[i].infuseGroup) {
                    count = element.recipelDetailEvtList[i].infuseGroup;
                  }
                }
              }

              for (let i = 1; i < count; i++) {
                infusion.defaultNumber = infusion.defaultNumber + 1;
                infusion.zushu.push(infusion.defaultNumber);
                infusion.infusionProject.push([]);
                infusion.drippingSpeed.push("");
                infusion.days.push({});
                infusion.frequency.push({});
                infusion.infuseUse.push({});
              }
              //this.excharge=null
              let arr = [];
              for (let i = 0; i < element.recipelDetailEvtList.length; i++) {
                if (element.recipelDetailEvtList[i].isExtra !== 1) {
                  infusion.infusionProject[
                  element.recipelDetailEvtList[i].infuseGroup - 1
                    ].push(element.recipelDetailEvtList[i]);
                  infusion.drippingSpeed[
                  element.recipelDetailEvtList[i].infuseGroup - 1
                    ] = element.recipelDetailEvtList[i].drippingSpeed;
                  infusion.days[element.recipelDetailEvtList[i].infuseGroup - 1] =
                    element.recipelDetailEvtList[i].days;
                  infusion.frequency[
                  element.recipelDetailEvtList[i].infuseGroup - 1
                    ] = element.recipelDetailEvtList[i].frequency;
                  infusion.infuseUse[
                  element.recipelDetailEvtList[i].infuseGroup - 1
                    ] = element.recipelDetailEvtList[i].infuseUse;
                } else {
                  infusion.excharge.push(element.recipelDetailEvtList[i]);
                  //arr.push(element.recipelDetailEvtList[i])
                }
              }

              // this.excharge=arr
              let tab = {
                title: element.recipelInfo.name,
                key: element.recipelInfo.seq,
                content: element,
                closable: false,
                type: element.recipelInfo.recipelType.value,
                infusion: infusion,
                costDebug: costDebug,
              };
              this.medicalEditTabs.push(tab);
            } else {
              let tab = {
                title: element.recipelInfo.name,
                key: element.recipelInfo.seq,
                content: element,
                closable: false,
                type: element.recipelInfo.recipelType.value,
                costDebug: costDebug,
              };
              this.medicalEditTabs.push(tab);
            }
          });

          // this.infusionProject=[]
          // this.infusionProject[0].push([])

          // this.medicalEditTabs.forEach(items=>{
          //   if(items.type==="recipelType_2"){
          //       let count=0
          //       for (let i = 0; i < items.content.recipelDetailEvtList; i++) {
          //         if(this.medicalEditTabs[i].type==='recipelType_2'){
          //           if(count<this.medicalEditTabs[i].content.recipelDetailEvtList.infuseGroup){
          //             count=this.medicalEditTabs[i].content.recipelDetailEvtList.infuseGroup
          //           }
          //         }

          //       }
          //       for (let i = 1; i < count; i++) {
          //         this.zushu.push(i+1)
          //         }
          //     items.content.recipelDetailEvtList.forEach(infusionItem=>{
          //       if(infusionItem.infuseGroup-1===0){
          //         this.infusionProject[0].push(infusionItem)
          //       }else{
          //         this.infusionProject[infusionItem.infuseGroup-1].push(infusionItem)
          //       }

          //     // items.content.recipelDetailEvtList.infuseGroup
          //     })

          //   }
          // })

          // for (let i = 0; i < this.infusionProject.length; i++) {
          //      for (let j = 0; j < this.infusionProject[i].length; j++) {
          //     this.drippingSpeed[this.infusionProject[i][j].infuseGroup-1]=this.infusionProject[i][j].drippingSpeed
          //     this.days[this.infusionProject[i][j].infuseGroup-1]=this.infusionProject[i][j].days
          //     this.frequency[this.infusionProject[i][j].infuseGroup-1]=this.infusionProject[i][j].frequency
          //     this.infuseUse[this.infusionProject[i][j].infuseGroup-1]=this.infusionProject[i][j].infuseUse

          //     }

          // }
          this.medicalEditTabsValue = this.medicalEditTabs[0];
          this.medicalClickTabsValue = this.medicalEditTabsValue;
          this.MedicalCalculate();
        }
      },
      //Lấy giá trị từ điển dữ liệu
      GetAllOption() {
        this.GetSysParamConfig();
        // 批量加载字典
        getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_RECIPEL_USE).then((data) => {
          this.ChineseUseOption = data;
          if (this.isSpecial) {
            this.ChineseUseOption = this.ChineseUseOption.filter((item) => item.name === "水冲");
          }
        });
        getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_RECIPEL_TAKE_FREQUENCY).then((data) => { this.FrequencyOption = data; });
        getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_RECIPEL_FREQUENCY).then((data) => { this.ChineseTimeOption = data; });
        getDictItemsByCode(DICT_CODE.WESTERN_MEDICINE_USE).then((data) => { this.WesternUseOption = data; });
        getDictItemsByCode(DICT_CODE.RECIPEL_DETAIL_DAYS).then((data) => { this.DayNumOption = data; });
        getDictItemsByCode(DICT_CODE.INFUSE_USE).then((data) => { this.infuseUseOption = data; });
        getDictItemsByCode(DICT_CODE.SKIN_TEST).then((data) => { this.InfusionOption = data; });
        getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_USE).then((data) => { this.ChineseUseTimeOption = data; });
        getDictItemsByCode(DICT_CODE.RECIPEL_SMALL_TYPE).then((data) => { this.RecipelSmallTypeList = data; });
        getDictItemsByCode(DICT_CODE.TREAT_TYPE).then((data) => {
          this.TreatTypeOption = (data || []).map(e => ({ name: e.name, value: e.value }));
        });
        getDictItemsByCode(DICT_CODE.INFECT_TYPE).then((data) => {
          this.InfectTypeOption = (data || []).map(e => ({ name: e.name, value: e.value }));
        });
      },
      //获取系统参数配置
      GetSysParamConfig() {
        listSysParamConfigAll(this.systemParamConfigSearch).then(responseData => {
          if (responseData.code === 100) {
            if (responseData.data.length >= 1) {
              responseData.data.forEach(data => {
                if (data.configName === "chronicDays") {
                  this.systemParamConfig.chronicDays = new Number(data.configValue);
                }
                if (data.configName === "normalDays") {
                  this.systemParamConfig.normalDays = new Number(data.configValue);
                }
              })
            }
          } else {
            this.showMessage(responseData)
          }
        }).catch(error => {
          this.outputError(error)
        })
      },

      //Cấu hình bệnh án khôi phục mặc định
      ClearMedicineCofigure() {
        this.ChooseRecordList.map((item) => {
          this.MedicalRecordModel[`${item.name}`] = "";
        });
        this.ChooseRecordList = [];
      },
      GetMedicineCofigure() {
        this.ChooseRecordList = [];
        this.MedicineRecordLabelList.map((item) => {
          if (this.MedicalRecordModel[`${item.name}`]) {
            this.ChooseRecordList.push(item);
          }
        });
      },
      GetMedicalRecordAll() {
        let params = [{
          columnName: "company_id",
          queryType: "=",
          value: this.Company.id,
        },
          {
            columnName: "type",
            queryType: "in",
            value: [],
          },
          {
            columnName: "goods_name",
            queryType: "like",
            value: "",
          },
        ];
        this.SearchWesternModel.params = JSON.parse(JSON.stringify(params));
        this.SearchChineseModel.params = JSON.parse(JSON.stringify(params));
        //this.GetWesternTable();
        //this.GetChineseTable();
      },
      GetWesternTable(type) {
        if(type === undefined){
         type = false;
        }
        this.SearchWesternModel.params[1].columnName = "drug.type";
        this.SearchWesternModel.params[1].value = [
          "medicalType_0",
          "medicalType_2",
        ];
        //Kiểm tra có phải nhập tiếng Anh không
        const pattern2 = new RegExp("[A-Za-z]+");
        if (pattern2.test(this.SearchWesternInput)) {
          this.SearchWesternModel.params[2].value =
            this.SearchWesternInput.toUpperCase();
          this.SearchWesternModel.params[2].columnName = "drug.pinyin_code";
        } else {
          this.SearchWesternModel.params[2].value = this.SearchWesternInput;
          this.SearchWesternModel.params[2].columnName = "drug.goods_name";
        }
        if (!this.SearchWesternModel.params.some(param => param.columnName === "surplus_stock")) {
          this.SearchWesternModel.params.push({
            columnName: "surplus_stock",
            queryType: ">",
            value: 0,
          });
        }
// Tránh thêm trùng lặp "drug.status"
        if (!this.SearchWesternModel.params.some(param => param.columnName === "drug.status")) {
          this.SearchWesternModel.params.push({
            columnName: "drug.status",
            queryType: "=",
            value: "1",
          });
        }
// Tránh thêm trùng lặp "drug.del_flag"
        if (!this.SearchWesternModel.params.some(param => param.columnName === "drug.del_flag")) {
          this.SearchWesternModel.params.push({
            columnName: "drug.del_flag",
            queryType: "=",
            value: "0",
          });
        }
// Kiểm tra trường đã tồn tại chưa，Nếu chưa tồn tại mới thêm
                listAll(this.SearchWesternModel,type)
          .then((responseData) => {
            if (responseData.code === 100) {
              // responseData.data.forEach((element) => {
              //     let isUnpackSell = element.isUnpackSell; //Cho phép bán lẻ tách hộp
              //     let stockNumber = element.stockNumber; //Số lượng tồn kho
              //     let packName = element.pack.name; //Đóng gói（Đơn vị）
              //     let preparation = element.preparation; //Chế phẩm
              //     let preparationUnit = element.preparationUnit.name; //Đơn vị bào chế
              //     let price = element.price; //Giá bán
              //     let retailPrice = element.retailPrice; //Giá bán lẻ tách hộp sau khi mở hộp
              //     let inventory = element.inventory;    //Tồn kho

              //     let stockText = "";
              //     //Làm tròn xuống
              //     let packNumber = Math.floor(inventory / preparation);
              //     //Lấy phần dư
              //     let retailNumber = inventory % preparation;
              //     if(packNumber > 0)
              //     {
              //         stockText = packNumber + packName;
              //     }
              //     if(retailNumber > 0)
              //     {
              //         if(packNumber > 0)
              //         {
              //             stockText = stockText + retailNumber + preparationUnit;
              //         }
              //         else
              //         {
              //             stockText = retailNumber + preparationUnit;
              //         }
              //     }

              //     element["stockText"] = stockText;
              // });
              this.WesternMedicineTable = responseData.data;
            }
          })
          .catch((error) => {
            this.outputError(error);
          });

        // listAll(search).then((res)=>{
        //   if(res.code===100){
        //   }
        // }).catch((error)=>{
        //   this.outputError(error)
        // })
      },
      GetInfusionTable(index) {
        this.SearchWesternModel.params[1].columnName = "drug.type";
        this.SearchWesternModel.params[1].value = [
          "medicalType_0",
          "medicalType_2",
        ];
        //Kiểm tra có phải nhập tiếng Anh không
        const pattern2 = new RegExp("[A-Za-z]+");
        if (pattern2.test(this.SearchInfusion[index])) {
          this.SearchWesternModel.params[2].value =
            this.SearchInfusion[index].toUpperCase();
          this.SearchWesternModel.params[2].columnName = "drug.pinyin_code";
        } else {
          this.SearchWesternModel.params[2].value = this.SearchInfusion[index];
          this.SearchWesternModel.params[2].columnName = "drug.goods_name";
        }

        // Kiểm tra trường đã tồn tại chưa，Nếu chưa tồn tại mới thêm
        if (!this.SearchChineseModel.params.some(param => param.columnName === "surplus_stock")) {
          this.SearchChineseModel.params.push({
            columnName: "surplus_stock",
            queryType: ">",
            value: 0,
          });
        }
        if (!this.SearchWesternModel.params.some(param => param.columnName === "drug.status")) {
          this.SearchWesternModel.params.push({
            columnName: "drug.status",
            queryType: "=",
            value: "1",
          });
        }
        if (!this.SearchWesternModel.params.some(param => param.columnName === "drug.del_flag")) {
          this.SearchWesternModel.params.push({
            columnName: "drug.del_flag",
            queryType: "=",
            value: "0",
          });
        }
        listAll(this.SearchWesternModel, 0).then((responseData) => {
          if (responseData.code === 100) {
            // responseData.data.forEach((element) => {
            //     let isUnpackSell = element.isUnpackSell; //Cho phép bán lẻ tách hộp
            //     let stockNumber = element.stockNumber; //Số lượng tồn kho
            //     let packName = element.pack.name; //Đóng gói（Đơn vị）
            //     let preparation = element.preparation; //Chế phẩm
            //     let preparationUnit = element.preparationUnit.name; //Đơn vị bào chế
            //     let price = element.price; //Giá bán
            //     let retailPrice = element.retailPrice; //Giá bán lẻ tách hộp sau khi mở hộp
            //     let inventory = element.inventory;    //Tồn kho

            //     let stockText = "";
            //     //Làm tròn xuống
            //     let packNumber = Math.floor(inventory / preparation);
            //     //Lấy phần dư
            //     let retailNumber = inventory % preparation;
            //     if(packNumber > 0)
            //     {
            //         stockText = packNumber + packName;
            //     }
            //     if(retailNumber > 0)
            //     {
            //         if(packNumber > 0)
            //         {
            //             stockText = stockText + retailNumber + preparationUnit;
            //         }
            //         else
            //         {
            //             stockText = retailNumber + preparationUnit;
            //         }
            //     }

            //     element["stockText"] = stockText;
            // });
            this.InfusionTable = responseData.data;
          }
        });
      },
      GetChineseTable(type) {
        if(type === undefined){
          type = false;
        }
        this.popoverVisible = true;
        this.SearchChineseModel.params[1].columnName = "drug.type";
        this.SearchChineseModel.params[1].value = ["medicalType_1"];
        //Kiểm tra có phải nhập tiếng Anh không
        const pattern3 = new RegExp("[A-Za-z]+");
        if (pattern3.test(this.SearchChineseInput)) {
          this.SearchChineseModel.params[2].value =
            this.SearchChineseInput.toUpperCase();
          this.SearchChineseModel.params[2].columnName = "drug.pinyin_code";
        } else {
          this.SearchChineseModel.params[2].value = this.SearchChineseInput;
          this.SearchChineseModel.params[2].columnName = "drug.goods_name";
        }
        // Kiểm tra trường đã tồn tại chưa，Nếu chưa tồn tại mới thêm
        if (!this.SearchChineseModel.params.some(param => param.columnName === "surplus_stock")) {
          this.SearchChineseModel.params.push({
            columnName: "surplus_stock",
            queryType: ">",
            value: 0,
          });
        }
        if (!this.SearchWesternModel.params.some(param => param.columnName === "drug.status")) {
          this.SearchWesternModel.params.push({
            columnName: "drug.status",
            queryType: "=",
            value: "1",
          });
        }
        if (!this.SearchWesternModel.params.some(param => param.columnName === "drug.del_flag")) {
          this.SearchWesternModel.params.push({
            columnName: "drug.del_flag",
            queryType: "=",
            value: "0",
          });
        }
        listAll(this.SearchChineseModel, type).then((responseData) => {
          if (responseData.code === 100) {
            this.ChineseMedicineTable = responseData.data;
          }
        });
      },
      //Nhấn dòng bảng thuốc Tây y để chọn và thêm vào bảng đơn thuốc Tây y đã chọn
      RowClickWesternTable(row) {
        let recipelDetailEvtList = this.medicalClickTabsValue.content.recipelDetailEvtList;
        let count = 0;
        let hasFlag = false;
        if (
          row.drug.days.value &&
          row.drug.frequency.value &&
          row.drug.singleDosage &&
          row.drug.total !== 0
        ) {
          let total = Math.ceil(
            BigNumber(row.drug.singleDosage)
              .multipliedBy(row.drug.frequency.value.split("_")[1])
              .multipliedBy(row.drug.days.name)
              .toNumber()
          );
          if (total > row.drug.total) {
            this.$message.error("Tổng lượng tối đa đã đặt cho thuốc này phải nhỏ hơn tổng lượng tính theo hướng dẫn công dụng mặc định");
            return;
          }
        }

        recipelDetailEvtList.forEach((element) => {
          if (element.isExtra === 0) {
            count++;
            if (row.drug.id === element.drugStuffId.drugStuffId) {
              hasFlag = true;
            }
          }
        });
        if (hasFlag) {
          this.$message.error("Đã có thuốc này");
          return;
        }
        if (count === 5) {
          this.$message.error("Mỗi đơn thuốc Tây y tối đa chỉ thêm được5thuốc");
          return;
        }

        let drugStuff = {
          drugStuffId: row.drug.id,
          name: row.drug.goodsName,
          price: row.drug.price,
          retailPrice: row.drug.retailPrice,
          isUnpackSell: row.drug.isUnpackSell,
          dosisUnit: row.drug.dosisUnit,
          preparationUnit: row.drug.preparationUnit,
          pack: row.drug.pack,
          drug: row.drug,
          surplusStock: row.surplusStock,
        };

        let recipelDetailEvt = {
          allFee: 0,
          total: 0,
          singleDosage: 0,
          dosageUnitType: 'dosis', // Đơn vị liều lượng mặc định
          company: this.Company,
          stuffType: 0,
          isExtra: 0,
          isUnpackSell: BigNumber(row.drug.isUnpackSell).toNumber(),
          drugStuffId: drugStuff,
          days: {
            name: 0,
            value: 'recipelDetailDays_0'
          }
        };

        this.medicalClickTabsValue.content.recipelDetailEvtList.push(
          JSON.parse(JSON.stringify(recipelDetailEvt))
        );
        setTimeout(() => {
          this.MedicalCalculate();
        }, 0);
        this.$message.success("Thêm thành công！");
        this.$nextTick(() => {
          this.$refs['westernMedicine'].slice(-2, -1)[0].focus()
        })
      },

      //Nhấn dòng bảng truyền dịch để chọn và thêm vào bảng đơn thuốc Tây y đã chọn
      RowClickInfusionTable(row, column, event, index) {
        let content = this.medicalClickTabsValue.content;
        let recipelDetailEvtList =
          this.medicalClickTabsValue.infusion.infusionProject[index];
        let count = 0;
        let hasFlag = false;

        recipelDetailEvtList.forEach((element) => {
          if (element.isExtra === 0) {
            count++;
            if (row.id === element.drugStuffId.drugStuffId) {
              hasFlag = true;
            }
          }
        });
        // if (hasFlag) {
        //   this.$message.error("Đã có thuốc này");
        //   return;
        // }
        if (count === 5) {
          this.$message.error("Mỗi đơn truyền dịch tối đa chỉ thêm được5thuốc");
          return;
        }

        let drugStuff = {
          drugStuffId: row.drug.id,
          name: row.drug.goodsName,
          price: row.drug.price,
          retailPrice: row.drug.retailPrice,
          isUnpackSell: row.drug.isUnpackSell,
          dosisUnit: row.drug.dosisUnit,
          preparationUnit: row.drug.preparationUnit,
          pack: row.drug.pack,
          drug: row.drug,
          surplusStock: row.surplusStock,
        };

        let recipelDetailEvt = {
          allFee: 0,
          total: 0,
          dosageUnitType: 'dosis', // Đơn vị liều lượng mặc định
          company: this.Company,
          stuffType: 0,
          isExtra: 0,
          isUnpackSell: BigNumber(row.drug.isUnpackSell).toNumber(),
          singleDosage: 0,
          skinTest: {},
          drugStuffId: drugStuff,
          infuseGroup: index + 1,
          drippingSpeed: "",
          days: {},
          frequency: {},
          infuseUse: {},
        };

        // this.medicalClickTabsValue.content.recipelDetailEvtList.push(
        //   JSON.parse(JSON.stringify(recipelDetailEvt))
        // );
        this.medicalClickTabsValue.infusion.infusionProject[index].push(
          JSON.parse(JSON.stringify(recipelDetailEvt))
        );
        this.$message.success("Thêm thành công！");
        this.$nextTick(() => {
          this.$refs['transfusion'].slice(-2, -1)[0].focus()
        })
      },

      //Nhấn nút xóa sau bảng thuốc Tây y để xóa dữ liệu có chỉ số chỉ định
      DeleteMedicalRow(index, row, item) {
        if(item!=null && item.content.recipelInfo.isPre){
          //电子处方切换删除所有
          this.medicalClickTabsValue.content.recipelDetailEvtList.splice
          (0, this.medicalClickTabsValue.content.recipelDetailEvtList.length);
          return
        }
        //   this.chineseTest,
        //   "就是"
        // );
        // if(row.stuffType===1){
        //   for (let i = 0; i < this.chineseTest[item.key-1].length; i++) {
        //     if(row.drugStuffId.drug.name===this.chineseTest[item.key-1][i]){
        //       this.chineseTest[item.key-1].splice(i,1)
        //       //i--;
        //     }
        //   }
        // }
        this.medicalClickTabsValue.content.recipelDetailEvtList =
          this.medicalClickTabsValue.content.recipelDetailEvtList.filter(
            (item) => item !== row
          );
        if (row.infuseGroup > 0) {
          this.medicalClickTabsValue.content.recipelDetailEvtList =
            this.medicalClickTabsValue.content.recipelDetailEvtList.filter(
              (item) =>
                item.infuseGroup !== row.infuseGroup &&
                this.medicalClickTabsValue.content.recipelDetailEvtList.indexOf(
                  item
                ) !== index
            );
          this.medicalClickTabsValue.infusion.infusionProject[
          row.infuseGroup - 1
            ].splice(index, 1);
        }
        this.MedicalCalculate();
      },
      DeleteExMedicalRow(index, row) {
        this.medicalClickTabsValue.content.recipelDetailEvtList =
          this.medicalClickTabsValue.content.recipelDetailEvtList.filter(
            (item) => item.id !== row.id
          );
        this.medicalClickTabsValue.infusion.excharge.splice(index, 1);
        this.MedicalCalculate();
      },
      RowClickChineseTable(row) {
        let recipelDetailEvtList =
          this.medicalClickTabsValue.content.recipelDetailEvtList;
        let hasFlag = false;

        recipelDetailEvtList.forEach((element) => {
          if (element.isExtra === 0) {
            if (row.id === element.drugStuffId.drugStuffId) {
              hasFlag = true;
            }
          }
        });

        if (hasFlag) {
          this.$message.error("Đã có thuốc này");
          return;
        }

        let drugStuff = {
          drugStuffId: row.drug.id,
          name: row.drug.goodsName,
          price: row.drug.price,
          retailPrice: row.drug.retailPrice,
          isUnpackSell: row.drug.isUnpackSell,
          dosisUnit: row.drug.dosisUnit,
          preparationUnit: row.drug.preparationUnit,
          pack: row.drug.pack,
          drug: row.drug,
          surplusStock: row.surplusStock,
        };

        let recipelDetailEvt = {
          allFee: 0,
          total: 0,
          company: this.Company,
          stuffType: 1,
          isExtra: 0,
          isUnpackSell: 0, //Đơn thuốc Đông y mặc định kê theo bán nguyên hộp
          drugStuffId: drugStuff,
          singleDosage: 0,
        };
        this.medicalClickTabsValue.content.recipelDetailEvtList.push(
          JSON.parse(JSON.stringify(recipelDetailEvt))
        );
        setTimeout(() => {
          this.MedicalCalculate();
        }, 0);
        this.$message.success("Thêm thành công！");
        setTimeout(()=>{
          this.$nextTick(() => {
            // //保持到第一个
            // this.$refs['herbMedicine'][0].focus()
            this.$refs['herbMedicine'].slice(-1)[0].focus()
          })
        },100);

      },
      GetChineseFee(tabData, rowData) {
        if (rowData) {
          let recipelInfo = tabData.content.recipelInfo;
          tabData.content.recipelInfo.dosage = recipelInfo.dosage ?
            recipelInfo.dosage :
            "";
          rowData.singleDosage = rowData.singleDosage ? rowData.singleDosage : 0;
          rowData.allFee =
            rowData.drugStuffId.price * rowData.singleDosage * recipelInfo.dosage;
        }

        this.GetChineseSumFee(tabData);
      },
      GetCostItemTable() {
        let SearchModel = {
          columnName: "",
          limit: 10,
          offset: 0,
          order: "",
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: this.Company.id,
          },
            {
              columnName: "item_name",
              queryType: "like",
              value: "",
            },
            {
              columnName: "item_type",
              queryType: "<>",
              value: "treatmentItemType_4",
            },
          ],
        };
        const pattern2 = new RegExp("[A-Za-z]+");
        if (pattern2.test(this.SearchCostItemInput)) {
          SearchModel.params[1].value = this.SearchCostItemInput.toUpperCase();
          SearchModel.params[1].columnName = "pinyin_code";
        } else {
          SearchModel.params[1].value = this.SearchCostItemInput;
          SearchModel.params[1].columnName = "item_name";
        }
        listCostItemAll(SearchModel).then((responseData) => {
          if (responseData.code === 100) {
            this.TreatmentTable = responseData.data;
          }
        });
      },
      RowClickTreatmentTable(row) {
        let recipelDetailEvtList =
          this.medicalClickTabsValue.content.recipelDetailEvtList;
        let hasFlag = false;

        recipelDetailEvtList.forEach((element) => {
          if (element.isExtra === 0) {
            if (row.id === element.drugStuffId.drugStuffId) {
              hasFlag = true;
            }
          }
        });

        if (hasFlag) {
          return;
        }

        let drugStuff = {
          drugStuffId: row.id,
          name: row.itemName,
          price: row.salePrice,
          isUnpackSell: 0,
          pack: row.unit,
          costItem: row,
        };

        let recipelDetailEvt = {
          allFee: 0,
          total: 1, // 诊疗项目数量默认为1
          company: this.Company,
          stuffType: 3,
          isExtra: 0,
          isUnpackSell: 0, //Đơn thuốc khám chữa bệnh mặc định kê theo bán nguyên hộp
          drugStuffId: drugStuff,
        };

        this.medicalClickTabsValue.content.recipelDetailEvtList.push(
          JSON.parse(JSON.stringify(recipelDetailEvt))
        );
        this.$message.success("Thêm thành công！");
        this.$nextTick(() => {
          this.$refs['DiagnosisTreatment'].slice(-2, -1)[0].focus()
        })
        this.MedicalCalculate();
      },
      GetCostItemFee(index, row) {
        row.allFee = row.drugStuffId.price * row.total;
      },
      GetSurchargeTable() {
        this.SurchargeTable = [];
        this.GetSurchargeCostItemTable();
        this.GetSurchargeStuffTable();
      },
      GetSurchargeCostItemTable() {
        let SearchModel = {
          columnName: "",
          limit: 10,
          offset: 0,
          order: "",
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: this.Company.id,
          },
            {
              columnName: "item_name",
              queryType: "like",
              value: "",
            },
            {
              columnName: "item_type",
              queryType: "=",
              value: "treatmentItemType_4",
            },
          ],
        };
        const pattern3 = new RegExp("[A-Za-z]+");
        if (pattern3.test(this.SearchSurchargeInput)) {
          SearchModel.params[1].value = this.SearchSurchargeInput.toUpperCase();
          SearchModel.params[1].columnName = "pinyin_code";
        } else {
          SearchModel.params[1].value = this.SearchSurchargeInput;
          SearchModel.params[1].columnName = "item_name";
        }
        listCostItemAll(SearchModel).then((responseData) => {
          if (responseData.code === 100) {
            responseData.data.forEach((element) => {
              element["stuffType"] = "3";
              this.SurchargeTable.push(element);
            });
          }
        });
      },
      GetSurchargeStuffTable() {
        let SearchModel = {
          columnName: "",
          limit: 10,
          offset: 0,
          order: "",
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: this.Company.id,
          },
            {
              columnName: "stuff.name",
              queryType: "like",
              value: "",
            },
            {
              columnName: "stuff.is_out_sell",
              queryType: "=",
              value: "1",
            },
            {
              columnName: "surplus_stock",
              queryType: ">",
              value: 0,
            },
          ],
        };
        const pattern2 = new RegExp("[A-Za-z]+");
        if (pattern2.test(this.SearchSurchargeInput)) {
          SearchModel.params[1].value = this.SearchSurchargeInput.toUpperCase();
          SearchModel.params[1].columnName = "stuff.pinyin_code";
        } else {
          SearchModel.params[1].value = this.SearchSurchargeInput;
          SearchModel.params[1].columnName = "stuff.name";
        }
        SearchModel.params.push({
          columnName: "stuff.status",
          queryType: "=",
          value: "1",
        });
        SearchModel.params.push({
          columnName: "stuff.del_flag",
          queryType: "=",
          value: "0",
        });
        listAll(SearchModel).then((responseData) => {
          if (responseData.code === 100) {
            responseData.data.forEach((element) => {
              element["stuffType"] = "4";
              this.SurchargeTable.push(element);
            });
          }
        });
      },
      RowClickSurchargeTable(row) {
        let recipelDetailEvtList =
          this.medicalClickTabsValue.content.recipelDetailEvtList;
        let hasFlag = false;

        recipelDetailEvtList.forEach((element) => {
          if (element.isExtra === 1) {
            if (row.id === element.drugStuffId.drugStuffId) {
              hasFlag = true;
            }
          }
        });

        if (hasFlag) {
          return;
        }

        let stuffType = row.stuffType;
        let drugStuff = {};
        if (stuffType === "3") {
          drugStuff = {
            drugStuffId: row.id,
            name: row.itemName,
            price: row.salePrice,
            isUnpackSell: 0,
            pack: row.unit,
            costItem: row,
          };
        } else if (stuffType === "4") {
          drugStuff = {
            drugStuffId: row.stuff.id,
            name: row.stuff.name,
            price: row.stuff.priceOutSell,
            retailPrice: row.stuff.retailPrice,
            isUnpackSell: row.stuff.isUnpackSell ?
              BigNumber(row.stuff.isUnpackSell).toNumber() :
              0,
            preparationUnit: row.stuff.minUnit,
            pack: row.stuff.packUnit,
            stuff: row.stuff,
            surplusStock: row.surplusStock,
          };
        }

        let recipelDetailEvt = {
          allFee: 0,
          total: 0,
          singleDosage: 0,
          company: this.Company,
          stuffType: stuffType,
          isExtra: 1,
          isUnpackSell: 0,
          drugStuffId: drugStuff,
        };

        if(row.itemName === '煎药费'){
          let totalDosage = 0;
          this.medicalEditTabs.forEach(item => {
            totalDosage += Number(item.content.recipelInfo.dosage);
          })
          recipelDetailEvt.singleDosage = totalDosage;
          // 使用售价而非成本价
          recipelDetailEvt.allFee = recipelDetailEvt.drugStuffId.costItem.salePrice * totalDosage;
        }

        if (this.medicalClickTabsValue.type === "recipelType_2") {
          this.medicalClickTabsValue.infusion.excharge.push(
            JSON.parse(JSON.stringify(recipelDetailEvt))
          );
        } else {
          this.medicalClickTabsValue.content.recipelDetailEvtList.push(
            JSON.parse(JSON.stringify(recipelDetailEvt))
          );
        }
        this.$message.success("Thêm thành công！");
        this.$nextTick(() => {
          this.$refs['additionalCharge'].slice(-2, -1)[0].focus()
          this.MedicalCalculate();
        })

      },
      getDataFilterTable(data, isExtra, item) {
        let arr = [];
        if (data.length === undefined) {
          arr.push(data);
        } else if (data.length >= 1) {
          for (let i = 0; i < data.length; i++) {
            data[i].drugOrder = i+1;
            // 确保 isUnpackSell 有值，默认不拆零
            if (data[i].isUnpackSell === undefined) {
              data[i].isUnpackSell = data[i].drugStuffId && data[i].drugStuffId.isUnpackSell === 1 ? 1 : 0;
            }
            // Theo isUnpackSell 正确设置总量单位
            if (data[i].minTotal !== undefined) {
              // Lấy quy cách đóng gói：药品用 drug.preparation，材料用 stuff.packNumber
              let preparation = 1;
              if (data[i].stuffType === '4') {
                // Loại vật tư
                preparation = (data[i].drugStuffId && data[i].drugStuffId.stuff && data[i].drugStuffId.stuff.packNumber) || 1;
              } else {
                // Loại thuốc：优先使用 preparation，如果为 0 hoặc undefined，则尝试使用 stuff.packNumber
                preparation = (data[i].drugStuffId && data[i].drugStuffId.drug && data[i].drugStuffId.drug.preparation);
                if (!preparation) {
                  preparation = (data[i].drugStuffId && data[i].drugStuffId.stuff && data[i].drugStuffId.stuff.packNumber) || 1;
                }
              }
              
              if (data[i].isUnpackSell === 1) {
                // Bán lẻ：Tổng số lượng = 制剂单位总量
                data[i].total = data[i].minTotal;
              } else {
                // Không bán lẻ：Tổng số lượng = 制剂单位总量 ÷ Quy cách đóng gói = 包装单位总量
                data[i].total = Math.ceil(data[i].minTotal / (preparation || 1));
              }
            }
            if (data[i].isExtra === 0 && data[i].drugStuffId.drug) {
              if (data[i].drugStuffId.drug.type.value === "medicalType_0") {
                //   if(data[i].singleDosage===0&&data[i].singleDosage===" "&&data[i].drugStuffId.drug.singleDosage){
                //   data[i].singleDosage = data[i].drugStuffId.drug.singleDosage
                // }
                if (
                  data[i].singleDosage === 0 &&
                  data[i].singleDosage === " " &&
                  data[i].drugStuffId.drug.singleDosage
                ) {
                  let flags = false;
                  for (let i = 0; i < item.costDebug.chineseTest.length; i++) {
                    if (
                      item.costDebug.chineseTest[i] ===
                      data[i].drugStuffId.drug.name
                    ) {
                      flags = true;
                      break;
                    }
                  }
                  if (!flags) {
                    data[i].singleDosage = data[i].drugStuffId.drug.singleDosage ?
                      data[i].drugStuffId.drug.singleDosage :
                      "";
                    item.costDebug.chineseTest.push(
                      data[i].drugStuffId.drug.name
                    );
                  }
                }
                if (
                  !data[i].frequency &&
                  data[i].drugStuffId.drug.frequency.value
                ) {
                  data[i].frequency = data[i].drugStuffId.drug.frequency;
                }
                if (!data[i].days && data[i].drugStuffId.drug.days.value) {
                  data[i].days = data[i].drugStuffId.drug.days;
                }
                if (
                  !data[i].westernMedicineUse &&
                  data[i].drugStuffId.drug.westernMedicineUse.value
                ) {
                  data[i].westernMedicineUse =
                    data[i].drugStuffId.drug.westernMedicineUse;
                }
              }
              if (data[i].drugStuffId.drug.type.value === "medicalType_1") {
                let flags = false;
                if (
                  data[i].singleDosage === 0 &&
                  data[i].singleDosage === " " &&
                  data[i].drugStuffId.drug.singleDosage
                ) {
                  for (let i = 0; i < item.costDebug.chineseTest.length; i++) {
                    if (
                      item.costDebug.chineseTest[i] ===
                      data[i].drugStuffId.drug.name
                    ) {
                      flags = true;
                      break;
                    }
                  }
                  if (!flags) {
                    data[i].singleDosage = data[i].drugStuffId.drug.singleDosage ?
                      data[i].drugStuffId.drug.singleDosage :
                      "";
                    item.costDebug.chineseTest.push(
                      data[i].drugStuffId.drug.name
                    );
                  }
                }
                if (
                  !data[i].chineseMedicineUse &&
                  data[i].drugStuffId.drug.chineseMedicineUse.value
                ) {
                  data[i].chineseMedicineUse =
                    data[i].drugStuffId.drug.chineseMedicineUse;
                }
              }
            }

            arr.push(data[i]);
          }
        }
        //  this.MedicalCalculate()

        // if(data.length>1){
        //   if(data[0].infuseGroup!==undefined){
        //    return data.filter((item) => item.isExtra === isExtra);

        // }else{
        //  return data.filter((item) => item.isExtra === isExtra);
        // }

        // }
        return arr.filter((item) => item.isExtra === isExtra);
      },
      GetSurchargeFee(index, row) {
        row.singleDosage = row.singleDosage ? row.singleDosage : 0;
        if (row.isUnpackSell === 1) {
          row.total = row.singleDosage;
          row.allFee = row.total * row.drugStuffId.retailPrice;
        } else {
          if (row.stuffType === "4") {
            row.total = row.singleDosage * row.drugStuffId.stuff.packNumber;
          } else if (row.stuffType === "3") {
            row.total = row.singleDosage;
          }

          row.allFee = row.singleDosage * row.drugStuffId.price;
        }
      },
      // 处理拆零销售切换时的总量转换
      handleUnpackSellChange(row, oldValue) {
        // 如果之前没有总量或总量为0，Không cần chuyển đổi
        if (!row.total || row.total === 0) {
          this.MedicalCalculate();
          return;
        }
        // Lấy quy cách đóng gói（每个包装包含多少制剂单位）
        let preparation = row.drugStuffId.drug.preparation || 1;
        if (oldValue === 0 && row.isUnpackSell === 1) {
          // 从不拆零切换为拆零：包装单位转为制剂单位
          row.total = Math.ceil(row.total * preparation);
        } else if (oldValue === 1 && row.isUnpackSell === 0) {
          // 从拆零切换为不拆零：制剂单位转为包装单位
          row.total = Math.ceil(row.total / preparation);
        }
        this.MedicalCalculate();
      },
      MedicalCalculate() {
        let n1 = 0; //西药处方个数
        let n2 = 0; //中药处方个数
        let n3 = 0; //输液处方个数
        let n4 = 0; //诊疗项目处方个数
        let seq = 0; //处方顺序
        let fullAmount = 0; //所有处方的费用汇总
        // if(!this.medicalEditTabs.length>0){
        //   fullAmount=
        // }
        this.medicalEditTabs.forEach((tabElement) => {
          let medicalFullAmount = 0; //Tất cả chi phí đơn thuốc = Tổng hợp chi phí đơn thuốc + Tổng hợp phí phụ thêm
          let medicalAmount = 0; //Tổng hợp chi phí đơn thuốc
          let surchargeAmount = 0; //Tổng hợp phí phụ thêm
          let detailSeq = 0; //Thứ tự chi tiết đơn thuốc
          let recipelInfo = tabElement.content.recipelInfo;
          let recipelTypeName = recipelInfo.recipelType.name;
          let recipelDetailEvtList = tabElement.content.recipelDetailEvtList ?
            tabElement.content.recipelDetailEvtList :
            [];
          seq++;
          //Đơn thuốc tây
          if (tabElement.type === "recipelType_0") {
            n1++;
            tabElement.title = recipelTypeName + n1;
            recipelDetailEvtList.forEach((rowElement) => {
              if (rowElement.isExtra === 0) {
                detailSeq++;
                rowElement.seq = detailSeq;

                rowElement.singleDosage = rowElement.singleDosage ? rowElement.singleDosage : "";
                rowElement.total = rowElement.total ? rowElement.total : 0;
                rowElement.allFee = rowElement.allFee ? rowElement.allFee : 0;
                rowElement.dosageUnitType = rowElement.dosageUnitType || 'dosis'; // Đơn vị liều lượng mặc định

                if (rowElement.frequency && rowElement.days) {
                  // Tính tổng lượng（Đơn vị bào chế）
                  let calculatedTotalInPreparation;
                  if (rowElement.dosageUnitType === 'preparation') {
                    // Tính theo đơn vị bào chế
                    calculatedTotalInPreparation = BigNumber(rowElement.singleDosage - 0)
                      .multipliedBy(rowElement.frequency.value.split("_")[1])
                      .multipliedBy(rowElement.days.name)
                      .toNumber();
                  } else {
                    // Tính theo đơn vị liều lượng
                    calculatedTotalInPreparation = BigNumber(rowElement.singleDosage - 0)
                      .multipliedBy(rowElement.frequency.value.split("_")[1])
                      .multipliedBy(rowElement.days.name)
                      .dividedBy(rowElement.drugStuffId.drug.dosis)
                      .toNumber();
                  }

                  // minTotal Thống nhất lưu tổng lượng theo đơn vị bào chế，Dùng để kiểm tra tồn kho ở backend
                  rowElement.minTotal = Math.ceil(calculatedTotalInPreparation);

                  if (rowElement.isUnpackSell === 1) {
                    // Bán lẻ：total Lưu tổng lượng theo đơn vị bào chế
                    rowElement.unitPrice = rowElement.drugStuffId.retailPrice;
                    rowElement.total = Math.ceil(calculatedTotalInPreparation);
                    // Chi phí = Tổng lượng bào chế × Đơn giá bào chế
                    rowElement.allFee = (BigNumber(calculatedTotalInPreparation)
                      .multipliedBy(rowElement.drugStuffId.retailPrice)
                      .toNumber()).toFixed(2);
                  } else {
                    // Không bán lẻ：total Lưu tổng lượng theo đơn vị đóng gói
                    rowElement.unitPrice = rowElement.drugStuffId.price;
                    rowElement.total = Math.ceil(
                      BigNumber(calculatedTotalInPreparation)
                        .dividedBy(rowElement.drugStuffId.drug.preparation)
                        .toNumber()
                    );
                    // Chi phí = Tổng lượng đóng gói × Đơn giá đóng gói
                    rowElement.allFee = BigNumber(rowElement.total)
                      .multipliedBy(rowElement.drugStuffId.price)
                      .toNumber();
                  }
                }
                if (
                  rowElement.drugStuffId.surplusStock < rowElement.minTotal &&
                  !this.isReadOnly &&
                  tabElement.content.recipelInfo.dispensionStatus === 0
                ) {
                  if (!this.beyondInventoryType) {
                    this.beyondInventoryType = true;
                  }
                  this.$message.error(
                    rowElement.drugStuffId.name +
                    "Tồn kho không đủ，Vui lòng sửa số lượng hoặc bổ sung tồn kho！"
                  );
                } else {
                  this.beyondInventoryType = false;
                }
                medicalAmount = BigNumber(medicalAmount)
                  .plus(rowElement.allFee)
                  .toNumber();
              }
            });
            medicalFullAmount = BigNumber(medicalFullAmount)
              .plus(medicalAmount)
              .toNumber();
          }
          //Đơn thuốc đông y
          else if (tabElement.type === "recipelType_1") {
            n2++;
            tabElement.title = recipelTypeName + n2;
            recipelInfo.dosage = recipelInfo.dosage ? recipelInfo.dosage : "";
            recipelDetailEvtList.forEach((rowElement) => {
              if (rowElement.isExtra === 0) {
                detailSeq++;
                rowElement.seq = detailSeq;
                if (
                  rowElement.singleDosage === " " ||
                  rowElement.singleDosage === ""
                ) {
                  if (recipelInfo.dosage === "") {
                    rowElement.singleDosage = "";
                    rowElement.total = BigNumber(0)
                      .multipliedBy(0)
                      .multipliedBy(rowElement.drugStuffId.drug.preparation)
                      .toNumber();
                    rowElement.allFee = BigNumber(rowElement.drugStuffId.price)
                      .multipliedBy(0)
                      .multipliedBy(0)
                      .toNumber();
                    rowElement.unitPrice = rowElement.drugStuffId.price;

                    medicalAmount = BigNumber(medicalAmount)
                      .plus(rowElement.allFee)
                      .toNumber();
                  } else {
                    rowElement.singleDosage = "";
                    rowElement.total = BigNumber(0)
                      .multipliedBy(recipelInfo.dosage)
                      .multipliedBy(rowElement.drugStuffId.drug.preparation)
                      .toNumber();
                    rowElement.allFee = BigNumber(rowElement.drugStuffId.price)
                      .multipliedBy(0)
                      .multipliedBy(recipelInfo.dosage)
                      .toNumber();
                    rowElement.unitPrice = rowElement.drugStuffId.price;

                    medicalAmount = BigNumber(medicalAmount)
                      .plus(rowElement.allFee)
                      .toNumber();
                  }
                } else {
                  if (recipelInfo.dosage === "") {
                    rowElement.singleDosage = rowElement.singleDosage ?
                      rowElement.singleDosage - 0 :
                      0;
                    rowElement.total = BigNumber(rowElement.singleDosage)
                      .multipliedBy(0)
                      .multipliedBy(rowElement.drugStuffId.drug.preparation)
                      .toNumber();
                    rowElement.allFee = BigNumber(rowElement.drugStuffId.price)
                      .multipliedBy(rowElement.singleDosage)
                      .multipliedBy(0)
                      .toNumber();
                    rowElement.unitPrice = rowElement.drugStuffId.price;

                    medicalAmount = BigNumber(medicalAmount)
                      .plus(rowElement.allFee)
                      .toNumber();
                  } else {
                    rowElement.singleDosage = rowElement.singleDosage ?
                      rowElement.singleDosage - 0 :
                      0;
                    rowElement.total = BigNumber(rowElement.singleDosage)
                      .multipliedBy(recipelInfo.dosage)
                      .multipliedBy(rowElement.drugStuffId.drug.preparation)
                      .toNumber();
                    rowElement.allFee = BigNumber(rowElement.drugStuffId.price)
                      .multipliedBy(rowElement.singleDosage)
                      .multipliedBy(recipelInfo.dosage)
                      .toNumber();
                    rowElement.unitPrice = rowElement.drugStuffId.price;

                    medicalAmount = BigNumber(medicalAmount)
                      .plus(rowElement.allFee)
                      .toNumber();
                  }
                }

                if (
                  rowElement.drugStuffId.surplusStock < (rowElement.minTotal || rowElement.total) &&
                  !this.isReadOnly &&
                  tabElement.content.recipelInfo.dispensionStatus === 0
                ) {
                  if (!this.beyondInventoryType) {
                    this.beyondInventoryType = true;
                  }
                  this.$message.error(
                    rowElement.drugStuffId.name +
                    "Tồn kho không đủ，Vui lòng sửa số lượng hoặc bổ sung tồn kho！"
                  );
                } else {
                  this.beyondInventoryType = false;
                }
              }
            });
            medicalFullAmount = BigNumber(medicalFullAmount)
              .plus(medicalAmount)
              .toNumber();
          }
          //Đơn truyền dịch
          else if (tabElement.type === "recipelType_2") {
            n3++;
            tabElement.title = recipelTypeName + n3;
            //  tabElement.content.recipelDetailEvtList=this.infusionProject

            let arr = 0;
            tabElement.content.recipelDetailEvtList = [];
            for (let i = 0; i < tabElement.infusion.infusionProject.length; i++) {
              for (
                let j = 0; j < tabElement.infusion.infusionProject[i].length; j++
              ) {
                tabElement.infusion.infusionProject[i][j].drippingSpeed =
                  tabElement.infusion.drippingSpeed[
                  tabElement.infusion.infusionProject[i][j].infuseGroup - 1
                    ];
                tabElement.infusion.infusionProject[i][j].days =
                  tabElement.infusion.days[
                  tabElement.infusion.infusionProject[i][j].infuseGroup - 1
                    ];
                tabElement.infusion.infusionProject[i][j].frequency =
                  tabElement.infusion.frequency[
                  tabElement.infusion.infusionProject[i][j].infuseGroup - 1
                    ];
                tabElement.infusion.infusionProject[i][j].infuseUse =
                  tabElement.infusion.infuseUse[
                  tabElement.infusion.infusionProject[i][j].infuseGroup - 1
                    ];
                tabElement.content.recipelDetailEvtList[arr] =
                  tabElement.infusion.infusionProject[i][j];
                arr++;
              }
            }
            // let countArr=0
            // countArr=arr
            // for (let i = 0; i < this.excharge.length; i++) {
            //   tabElement.content.recipelDetailEvtList.push(this.excharge[i])
            //  // tabElement.content.recipelDetailEvtList[countArr+1]=this.excharge[i]
            //   //countArr++;
            // }
            for (let i = 0; i < tabElement.infusion.excharge.length; i++) {
              tabElement.content.recipelDetailEvtList.push(
                tabElement.infusion.excharge[i]
              );
            }
            recipelDetailEvtList = tabElement.content.recipelDetailEvtList ?
              tabElement.content.recipelDetailEvtList :
              [];

            //  recipelInfo.frequency = recipelInfo.frequency ? recipelInfo.frequency : 0;
            let infusionRecipelDetailEvtList =
              tabElement.content.recipelDetailEvtList;
            infusionRecipelDetailEvtList.forEach((rowElement) => {
              if (rowElement.isExtra === 0) {
                detailSeq++;
                rowElement.seq = detailSeq;
                rowElement.singleDosage = rowElement.singleDosage ?
                  rowElement.singleDosage :
                  "";
                rowElement.total = rowElement.total ? rowElement.total : 0;
                rowElement.allFee = rowElement.allFee ? rowElement.allFee : 0;
                rowElement.dosageUnitType = rowElement.dosageUnitType || 'dosis'; // Đơn vị liều lượng mặc định
                if (rowElement.frequency && rowElement.frequency.value && rowElement.days && rowElement.days.name) {
                  // Tính tổng lượng（Đơn vị bào chế）
                  let calculatedTotalInPreparation;
                  if (rowElement.dosageUnitType === 'preparation') {
                    // Tính theo đơn vị bào chế
                    calculatedTotalInPreparation = BigNumber(rowElement.singleDosage - 0)
                      .multipliedBy(rowElement.frequency.value.split("_")[1])
                      .multipliedBy(rowElement.days.name)
                      .toNumber();
                  } else {
                    // Tính theo đơn vị liều lượng
                    calculatedTotalInPreparation = BigNumber(rowElement.singleDosage - 0)
                      .multipliedBy(rowElement.frequency.value.split("_")[1])
                      .multipliedBy(rowElement.days.name)
                      .dividedBy(rowElement.drugStuffId.drug.dosis)
                      .toNumber();
                  }

                  // minTotal Thống nhất lưu tổng lượng theo đơn vị bào chế，Dùng để kiểm tra tồn kho ở backend
                  rowElement.minTotal = Math.ceil(calculatedTotalInPreparation);

                  if (rowElement.isUnpackSell === 1) {
                    // Bán lẻ：total Lưu tổng lượng theo đơn vị bào chế
                    rowElement.unitPrice = rowElement.drugStuffId.retailPrice;
                    rowElement.total = Math.ceil(calculatedTotalInPreparation);
                    // Chi phí = Tổng lượng bào chế × Đơn giá bào chế
                    rowElement.allFee = BigNumber(calculatedTotalInPreparation)
                      .multipliedBy(rowElement.drugStuffId.retailPrice)
                      .toNumber();
                  } else {
                    rowElement.unitPrice = rowElement.drugStuffId.price;
                    rowElement.total = Math.ceil(
                      BigNumber(calculatedTotalInPreparation)
                        .dividedBy(rowElement.drugStuffId.drug.preparation)
                        .toNumber()
                    );
                    rowElement.allFee = BigNumber(rowElement.total)
                      .multipliedBy(rowElement.drugStuffId.price)
                      .toNumber();
                  }
                }
                if (
                  rowElement.drugStuffId.surplusStock < (rowElement.minTotal || rowElement.total) &&
                  !this.isReadOnly &&
                  tabElement.content.recipelInfo.dispensionStatus === 0
                ) {
                  if (!this.beyondInventoryType) {
                    this.beyondInventoryType = true;
                  }
                  this.$message.error(
                    rowElement.drugStuffId.name +
                    "Tồn kho không đủ，Vui lòng sửa số lượng hoặc bổ sung tồn kho！"
                  );
                } else {
                  this.beyondInventoryType = false;
                }
                medicalAmount = BigNumber(medicalAmount)
                  .plus(rowElement.allFee)
                  .toNumber();
              }
            });
            medicalFullAmount = BigNumber(medicalFullAmount)
              .plus(medicalAmount)
              .toNumber();
          }
          //诊疗项目处方
          else if (tabElement.type === "recipelType_3") {
            n4++;
            tabElement.title = recipelTypeName + n4;
            recipelDetailEvtList.forEach((rowElement) => {
              if (rowElement.isExtra === 0) {
                detailSeq++;
                rowElement.seq = detailSeq;
                rowElement.total = rowElement.total ? rowElement.total : 1; // 默认数量为1
                rowElement.allFee = BigNumber(rowElement.drugStuffId.price)
                  .multipliedBy(rowElement.total - 0)
                  .toNumber();
                rowElement.unitPrice = rowElement.drugStuffId.price;

                medicalAmount = BigNumber(medicalAmount)
                  .plus(rowElement.allFee)
                  .toNumber();
              }
            });
            medicalFullAmount = BigNumber(medicalFullAmount)
              .plus(medicalAmount)
              .toNumber();
          } else if (tabElement.type === "recipelType_5") {
            n1++;
            tabElement.title = recipelTypeName + n1;
            recipelDetailEvtList.forEach((rowElement) => {
              // if (rowElement.isExtra === 0) {
              //   detailSeq++;
              //   rowElement.seq = detailSeq;
              //   rowElement.total = rowElement.total ? rowElement.total : "";
              //   rowElement.allFee = BigNumber(rowElement.drugStuffId.price)
              //     .multipliedBy(rowElement.total-0)
              //     .toNumber();
              //   rowElement.unitPrice = rowElement.drugStuffId.price;

              medicalAmount = BigNumber(medicalAmount)
                .plus(rowElement.allFee)
                .toNumber();
              // }
            });
            medicalFullAmount = BigNumber(medicalFullAmount)
              .plus(medicalAmount)
              .toNumber();
          }

          //Phí phụ thêm của mỗi đơn thuốc
          recipelDetailEvtList.forEach((rowElement) => {
            // if(rowElement.infuseGroup===undefined){
            if (rowElement.isExtra === 1) {
              detailSeq++;
              rowElement.seq = detailSeq;
              // rowElement.singleDosage =
              //   rowElement.singleDosage && rowElement.singleDosage !== 0 ?
              //     rowElement.singleDosage :
              //     "";
              if (rowElement.isUnpackSell === 1) {
                rowElement.total = rowElement.singleDosage - 0;
                rowElement.allFee = BigNumber(rowElement.drugStuffId.retailPrice)
                  .multipliedBy(rowElement.total)
                  .toNumber();
                rowElement.unitPrice = rowElement.drugStuffId.retailPrice;
                if (
                  rowElement.drugStuffId.surplusStock < (rowElement.minTotal || rowElement.total) &&
                  !this.isReadOnly &&
                  tabElement.content.recipelInfo.dispensionStatus === 0
                ) {
                  if (!this.beyondInventoryType) {
                    this.beyondStuffInventoryType = true;
                  }
                  this.$message.error(
                    rowElement.drugStuffId.name +
                    "Tồn kho không đủ，Vui lòng sửa số lượng hoặc bổ sung tồn kho！"
                  );
                } else {
                  this.beyondStuffInventoryType = false;
                }
              } else {
                //Vật tư
                if (rowElement.stuffType === "4") {
                  rowElement.total = BigNumber(rowElement.singleDosage - 0)
                    .multipliedBy(rowElement.drugStuffId.stuff.packNumber)
                    .toNumber();

                  if (
                    rowElement.drugStuffId.surplusStock < (rowElement.minTotal || rowElement.total) &&
                    !this.isReadOnly &&
                    tabElement.content.recipelInfo.dispensionStatus === 0
                  ) {
                    if (!this.beyondInventoryType) {
                      this.beyondStuffInventoryType = true;
                    }
                    this.$message.error(
                      rowElement.drugStuffId.name +
                      "Tồn kho không đủ，Vui lòng sửa số lượng hoặc bổ sung tồn kho！"
                    );
                  } else {
                    this.beyondStuffInventoryType = false;
                  }
                }
                //Hạng mục khám chữa bệnh
                else if (rowElement.stuffType === "3") {
                  rowElement.total = rowElement.singleDosage - 0;
                }

                rowElement.unitPrice = rowElement.drugStuffId.price;
                rowElement.allFee = BigNumber(rowElement.singleDosage - 0)
                  .multipliedBy(rowElement.drugStuffId.price)
                  .toNumber();
              }

              surchargeAmount = BigNumber(surchargeAmount)
                .plus(rowElement.allFee)
                .toNumber();
            }
            //}
          });

          medicalFullAmount = BigNumber(medicalFullAmount)
            .plus(surchargeAmount)
            .toNumber();

          recipelInfo.name = tabElement.title;
          recipelInfo.seq = seq;
          recipelInfo.medicalAmount = medicalAmount;
          recipelInfo.surchargeAmount = surchargeAmount;
          recipelInfo.fee = medicalFullAmount;
          fullAmount = BigNumber(fullAmount).plus(recipelInfo.fee).toNumber();
        });

        this.payAmount = fullAmount;
      },
      changeDosageUnit(row, unitType) {
        if (row.dosageUnitType === unitType) return;

        // 保存当前值
        const currentValue = parseFloat(row.singleDosage) || 0;

        if (unitType === 'preparation') {
          // 从剂量单位转换为制剂单位
          if (row.drugStuffId.drug && row.drugStuffId.drug.dosis) {
            row.singleDosage = (currentValue / row.drugStuffId.drug.dosis).toFixed(2);
          }
        } else {
          // 从制剂单位转换为剂量单位
          if (row.drugStuffId.drug && row.drugStuffId.drug.dosis) {
            row.singleDosage = (currentValue * row.drugStuffId.drug.dosis).toFixed(2);
          }
        }

        // 更新单位类型
        row.dosageUnitType = unitType;
        // 重新计算
        this.MedicalCalculate();
      },
      calculateDaysByTotal(row) {
        if (!row.total || !row.singleDosage || !row.frequency) return;

        row.dosageUnitType = row.dosageUnitType || 'dosis';

        // Tính liều dùng mỗi lần（Chuyển đổi sang đơn vị bào chế）
        let singleDoseInPreparation;
        if (row.dosageUnitType === 'preparation') {
          // Đơn vị bào chế，Không cần chuyển đổi
          singleDoseInPreparation = parseFloat(row.singleDosage) || 0;
        } else {
          // Đơn vị liều lượng，Cần chuyển đổi sang đơn vị bào chế
          singleDoseInPreparation = (parseFloat(row.singleDosage) || 0) / (row.drugStuffId.drug.dosis || 1);
        }

        // Tính liều dùng mỗi ngày（Đơn vị bào chế）
        let dailyDosage = singleDoseInPreparation * (parseFloat(row.frequency.value.split("_")[1]) || 0);

        if (dailyDosage > 0) {
          // Theo isUnpackSell Kiểm tra đơn vị tổng lượng
          let totalInPreparation;
          if (row.isUnpackSell === 1) {
            // Đơn vị tổng lượng là đơn vị bào chế，Sử dụng trực tiếp
            totalInPreparation = row.total;
          } else {
            // Đơn vị tổng lượng là đơn vị đóng gói，Cần chuyển đổi sang đơn vị bào chế：Tổng số lượng × Quy cách đóng gói
            totalInPreparation = row.total * (row.drugStuffId.drug.preparation || 1);
          }

          // minTotal Thống nhất lưu tổng lượng theo đơn vị bào chế，Dùng để kiểm tra tồn kho ở backend
          row.minTotal = Math.ceil(totalInPreparation);

          // 计算天数 = Tổng số lượng (Đơn vị bào chế) / Liều dùng mỗi ngày
          // 向上取整，确保天数足够覆盖总量
          const days = Math.ceil(totalInPreparation / dailyDosage);
          if (row.days && row.days.name) {
            row.days.name = days;
          } else {
            row.days = { name: days };
          }

          // 计算费用并更新总金额
          if (row.isUnpackSell === 1) {
            row.allFee = (row.total * row.drugStuffId.retailPrice).toFixed(2);
          } else {
            row.allFee = (row.total * row.drugStuffId.price).toFixed(2);
          }
          // Cập nhật hiển thị tổng số tiền
          this.MedicalCalculate();
        }
      },
      calculateTotalByDays(row) {
        if (!row.singleDosage || !row.frequency || !row.days) return;

        row.dosageUnitType = row.dosageUnitType || 'dosis';

        // Tính liều dùng mỗi lần（Chuyển đổi sang đơn vị bào chế）
        let singleDoseInPreparation;
        if (row.dosageUnitType === 'preparation') {
          // Đơn vị bào chế，Không cần chuyển đổi
          singleDoseInPreparation = parseFloat(row.singleDosage) || 0;
        } else {
          // Đơn vị liều lượng，Cần chuyển đổi sang đơn vị bào chế
          singleDoseInPreparation = (parseFloat(row.singleDosage) || 0) / (row.drugStuffId.drug.dosis || 1);
        }

        // Tính liều dùng mỗi ngày（Đơn vị bào chế）
        let dailyDosage = singleDoseInPreparation * (parseFloat(row.frequency.value.split("_")[1]) || 0);

        // Tính tổng lượng (Đơn vị bào chế) = Liều dùng mỗi ngày × Số ngày
        let days = parseFloat(row.days.name) || 1;
        let totalInPreparation = dailyDosage * days;

        // minTotal Thống nhất lưu tổng lượng theo đơn vị bào chế，Dùng để kiểm tra tồn kho ở backend
        row.minTotal = Math.ceil(totalInPreparation);

        // Theo isUnpackSell Kiểm tra đơn vị tổng lượng
        if (row.isUnpackSell === 1) {
          // Đơn vị tổng lượng là đơn vị bào chế，Sử dụng trực tiếp
          row.total = Math.ceil(totalInPreparation);
          // Tính chi phí
          row.allFee = (row.total * row.drugStuffId.retailPrice).toFixed(2);
        } else {
          // Đơn vị tổng lượng là đơn vị đóng gói，需要转换为包装单位：Tổng số lượng (Đơn vị bào chế) ÷ Quy cách đóng gói
          let preparation = (row.drugStuffId.drug && row.drugStuffId.drug.preparation) || 
                            (row.drugStuffId.stuff && row.drugStuffId.stuff.packNumber) || 1;
          row.total = Math.ceil(totalInPreparation / preparation);
          // Tính chi phí
          row.allFee = (row.total * row.drugStuffId.price).toFixed(2);
        }
        // Cập nhật hiển thị tổng số tiền
        this.MedicalCalculate();
      },
      FinishVisit() {
        let FormCheck = true;
        this.$refs["BasicInfoForm"].validate((valid) => {
          if (valid) {
            //alert('submit!');
          } else {
            FormCheck = false;
            return false;
          }
        });

        if (!FormCheck) {
          return;
        }

        let recipelInfoEvtList = [];

        let returnForEach = false;
        let hasSelsmallType = false;
        let numberCheck = false;
        if (this.medicalEditTabs.length === 0) {
          this.$message.error("Số lượng đơn thuốc không được là0");
          return;
        }
        this.medicalEditTabs.forEach((tabElement) => {
          //Đơn thuốc tây
          if (
            tabElement.type === "recipelType_0" &&
            !this.MedicalRecordModel.westernDiagnose
          ) {
            returnForEach = true;
          }
          //Đơn thuốc đông y
          if (
            tabElement.type === "recipelType_1" &&
            !this.MedicalRecordModel.chinaDiagnose
          ) {
            returnForEach = true;
          }
          if (
            !tabElement.content.recipelInfo.smallType &&
            tabElement.type !== "recipelType_3"
          ) {
            hasSelsmallType = true;
          }
          if (tabElement.content.recipelDetailEvtList.length === 0) {
            numberCheck = true;
          }
          recipelInfoEvtList.push(tabElement.content);
        });

        if (returnForEach) {
          this.$message.error("Có đơn thuốc Tây y hoặc Đông y，Vui lòng điền giải thích chẩn đoán tương ứng。");
          return;
        }

        if (hasSelsmallType) {
          this.$message.error("Vui lòng kiểm tra phân loại trong mỗi đơn thuốc đã chọn chưa。");
          return;
        }

        if (numberCheck) {
          this.$message.error("Vui lòng kiểm tra mỗi đơn thuốc có thuốc chưa。");
          return;
        }
        this.MedicalRecordModel.company = this.Company;
        this.MedicalRecordModel.doctor.id = this.UserId;

        let medicalRecipelEvt = {
          medicalRecord: this.MedicalRecordModel,
          patient: this.BasicInfoModel,
          recipelInfoEvtList: recipelInfoEvtList,
          isInstant: this.isInstant,
        };
        let formData = new FormData();
        formData.append("entity", JSON.stringify(medicalRecipelEvt));
        this.hint = false;
        allSaveMedicalRecord(formData).then((responseData) => {
          if (responseData.code === 100) {
            this.$message.success("Thao tác thành công！");
            this.hasBtnFinishVisit = false;
            this.hasBtnPrint = false;
            this.isReadOnly = true;
            this.GetPreparePatientList();
            this.GetAlreadyPatientList();
          } else {
            this.$message.error("Hoàn thành tiếp nhận thất bại!");
          }
        });
      },
      invalidMedical() {
        if (
          !this.medicalClickTabsValue.content ||
          !this.medicalClickTabsValue.content.recipelInfo.id
        ) {
          this.$message.error("Chưa chọn đơn thuốc");
          return;
        }
        let id = this.medicalClickTabsValue.content.recipelInfo.id;

        invalidStatus(id).then((responseData) => {
          if (responseData.code === 100) {
            this.$message.success("Hủy đơn thuốc thành công!");
            this.cancellation = true;
            this.AlreadyPatientDeletilQuery();
          } else {
            this.$message.error("Hủy đơn thuốc thất bại!");
          }
        });
      },
      checkInventory(row) {
        // 用制剂单位总量和库存（Đơn vị bào chế）比较
        const minTotal = row.minTotal || (row.isUnpackSell === 1 ? row.total : row.total * (row.drugStuffId.drug.preparation || 1));
        if (minTotal > row.drugStuffId.surplusStock) {
          //
          if (!this.beyondInventoryType) {
            this.beyondInventoryType = true;
          }
          this.$message.error(
            row.drugStuffId.name + "超出库存总量，请重新输入或者入库药品!"
          );
        } else {
          this.beyondInventoryType = false;
        }
      },
      //默认之展开一行
      // 折叠面板每次只能展开一行
      expandSelect(row, expandedRows) {
        const that = this;
        if (expandedRows.length) {
          that.expands = [];
          if (row) {
            that.expands.push(row.id); // Mỗi lầnpushđi vào là của mỗi dòngID
            expandedRows;
          }
        } else {
          that.expands = []; // Mặc định không mở rộng
        }
      },

      blmbexpandSelect(row, expandedRows) {
        this.blmbtableData.forEach((item) => {
          if (item.mbbm === row.mbbm) {
            this.blmbList.map((mapitem) => {
              if (mapitem.name === "personalHistory") {
                mapitem.value = item.grs;
              } else if (mapitem.name === "allergyHistory") {
                mapitem.value = item.gms;
              } else if (mapitem.name === "diseaseHistory") {
                mapitem.value = item.jbs;
              } else if (mapitem.name === "infectiousHistory") {
                mapitem.value = item.crbs;
              } else if (mapitem.name === "surgeryHistory") {
                mapitem.value = item.sss;
              } else if (mapitem.name === "transfusionHistory") {
                mapitem.value = item.sxs;
              } else if (mapitem.name === "physiqueCheck") {
                mapitem.value = item.tgjc;
              } else if (mapitem.name === "emergencyDiagnose") {
                mapitem.value = item.jzzd;
              } else if (mapitem.name === "emergencyEffect") {
                mapitem.value = item.jzxg;
              } else if (mapitem.name === "familyHistory") {
                mapitem.value = item.jzs;
              } else if (mapitem.name === "lunariaHistory") {
                mapitem.value = item.yjz;
              } else if (mapitem.name === "pregnancyHistory") {
                mapitem.value = item.hys;
              } else if (mapitem.name === "assistCheck") {
                mapitem.value = item.fzjc;
              } else if (mapitem.name === "nowHistory") {
                mapitem.value = item.xbs;
              } else if (mapitem.name === "beforeHistory") {
                mapitem.value = item.jws;
              } else if (mapitem.name === "epidemicDisease") {
                mapitem.value = item.lxbxs;
              } else if (mapitem.name === "otherCheck") {
                mapitem.value = item.qtjc;
              } else if (mapitem.name === "handlingSituation") {
                mapitem.value = item.clqk;
              } else if (mapitem.name === "healthEducation") {
                mapitem.value = item.gthjkjy;
              }
            });
          }
        });

        const that = this;
        if (expandedRows.length) {
          that.blmbexpands = [];
          if (row) {
            that.blmbexpands.push(row.mbbm); // Mỗi lầnpushđi vào là của mỗi dòngID
            expandedRows;
          }
        } else {
          that.blmbexpands = []; // Mặc định không mở rộng
        }
      },
      //病历模板弹框事件
      blmbclick() {
        this.Getblmbtable();
        this.blmbDialogVisible = true;
      },
      //病历历史调用
      onMedicalHistory() {
        this.$message.warning("Đang phát triển...");
      },
      blmbonSizeChange(val) {
        this.blmbcurrentPage = 1;
        this.blmbcxrc.limit = val;
        this.blmbcxrc.offset = (this.blmbcurrentPage - 1) * val;
        this.Getblmbtable();
      },
      blmbonCurrentChange(val) {
        this.blmbcxrc.offset = (val - 1) * this.blmbcxrc.limit;
        this.blmbcurrentPage = val;
        this.Getblmbtable();
      },
      blmbindexMethod(index) {
        return (this.blmbcurrentPage - 1) * this.blmbcxrc.limit + index + 1;
      },
      Getblmbtable() {
        this.blmbtableData = [];
        this.blmbcxrc.mbmc = this.blmbcxrc.mbmc;
        getblmblist(this.blmbcxrc)
          .then((responseData) => {
            if (responseData.code === 100) {
              this.blmbpatientTotal = responseData.data.total;
              if (responseData.data.total > 0) {
                responseData.data.rows.forEach((item) => {
                  if (item.mblx === "0") {
                    item.mblx = "Dùng chung";
                  } else if (item.mblx === "1") {
                    item.mblx = "Cá nhân";
                  } else {
                    {
                      item.mblx = "";
                    }
                  }
                  if (item.bllx === "0") {
                    item.bllx = "Bệnh án Tây y";
                  } else if (item.bllx === "1") {
                    item.bllx = "Bệnh án Đông y";
                  } else {
                    {
                      item.bllx = "";
                    }
                  }
                  this.blmbtableData.push({
                    // mbbm:item.mbbm,
                    // mbmc: item.mbmc,
                    // mblx: item.mblx,
                    // bllx: item.bllx,
                    cjrq: item.createdTime,
                    cjr: item.cjr,
                    mbbm: item.mbbm,
                    companyId: item.companyId,
                    delFlag: item.delFlag,
                    createdBy: item.createdBy,
                    createdTime: item.createdTime,
                    updatedBy: item.updatedBy,
                    updatedTime: item.updatedTime,
                    createdID: item.createdID,
                    mbmc: item.mbmc,
                    mblx: item.mblx,
                    bllx: item.bllx,
                    zs: item.zs,
                    grs: item.grs,
                    gms: item.gms,
                    jbs: item.jbs,
                    crbs: item.crbs,
                    sss: item.sss,
                    sxs: item.sxs,
                    tgjc: item.tgjc,
                    jzzd: item.jzzd,
                    jzxg: item.jzxg,
                    jzs: item.jzs,
                    yjz: item.yjz,
                    hys: item.hys,
                    fzjc: item.fzjc,
                    xbs: item.xbs,
                    jws: item.jws,
                    lxbxs: item.lxbxs,
                    qtjc: item.qtjc,
                    clqk: item.clqk,
                    gthjkjy: item.gthjkjy,
                    temperature: item.temperature,
                    pulse: item.pulse,
                    breathe: item.breathe,
                    bloodPressure: item.bloodPressure,
                    westernDiagnose: item.westernDiagnose,
                    chinaDiagnose: item.chinaDiagnose,
                  });
                });
              }
            }
          })
          .catch((error) => {
            this.$message.error(error);
          });
      },
      blmbdialogonSubmit() {
        this.Getblmbtable();
      },
      blmbdialogresetCondition() {
        this.blmbcxrc = {
          limit: 20,
          offset: 0,
          companyId: currentUser.company.id,
          mbmc: "",
          cjrid: currentUser.id,
        };
        this.Getblmbtable();
      },
      diaoyong(row) {
        this.ChooseRecordList = [];
        this.MedicalRecordModel.patientTell = row.zs;
        this.MedicineRecordLabelList.map((item) => {
          if (
            row.grs !== "" &&
            row.grs != null &&
            item.name === "personalHistory"
          ) {
            this.MedicalRecordModel.personalHistory = row.grs;
            this.ChooseRecordList.push(item);
          } else if (
            row.gms !== "" &&
            row.gms != null &&
            item.name === "allergyHistory"
          ) {
            this.MedicalRecordModel.allergyHistory = row.gms;
            this.ChooseRecordList.push(item);
          } else if (
            row.jbs !== "" &&
            row.jbs != null &&
            item.name === "diseaseHistory"
          ) {
            this.MedicalRecordModel.diseaseHistory = row.jbs;
            this.ChooseRecordList.push(item);
          } else if (
            row.crbs !== "" &&
            row.crbs != null &&
            item.name === "infectiousHistory"
          ) {
            this.MedicalRecordModel.infectiousHistory = row.crbs;
            this.ChooseRecordList.push(item);
          } else if (
            row.sss !== "" &&
            row.sss != null &&
            item.name === "surgeryHistory"
          ) {
            this.MedicalRecordModel.surgeryHistory = row.sss;
            this.ChooseRecordList.push(item);
          } else if (
            row.sxs !== "" &&
            row.sxs != null &&
            item.name === "transfusionHistory"
          ) {
            this.MedicalRecordModel.transfusionHistory = row.sxs;
            this.ChooseRecordList.push(item);
          } else if (
            row.tgjc !== "" &&
            row.tgjc != null &&
            item.name === "physiqueCheck"
          ) {
            this.MedicalRecordModel.physiqueCheck = row.tgjc;
            this.ChooseRecordList.push(item);
          } else if (
            row.jzzd !== "" &&
            row.jzzd != null &&
            item.name === "emergencyDiagnose"
          ) {
            this.MedicalRecordModel.emergencyDiagnose = row.jzzd;
            this.ChooseRecordList.push(item);
          } else if (
            row.jzxg !== "" &&
            row.jzxg != null &&
            item.name === "emergencyEffect"
          ) {
            this.MedicalRecordModel.emergencyEffect = row.jzxg;
            this.ChooseRecordList.push(item);
          } else if (
            row.jzs !== "" &&
            row.jzs != null &&
            item.name === "familyHistory"
          ) {
            this.MedicalRecordModel.familyHistory = row.jzs;
            this.ChooseRecordList.push(item);
          } else if (
            row.yjz !== "" &&
            row.yjz != null &&
            item.name === "lunariaHistory"
          ) {
            this.MedicalRecordModel.lunariaHistory = row.yjz;
            this.ChooseRecordList.push(item);
          } else if (
            row.hys !== "" &&
            row.hys != null &&
            item.name === "pregnancyHistory"
          ) {
            this.MedicalRecordModel.pregnancyHistory = row.hys;
            this.ChooseRecordList.push(item);
          } else if (
            row.fzjc !== "" &&
            row.fzjc != null &&
            item.name === "assistCheck"
          ) {
            this.MedicalRecordModel.assistCheck = row.fzjc;
            this.ChooseRecordList.push(item);
          } else if (
            row.xbs !== "" &&
            row.xbs != null &&
            item.name === "nowHistory"
          ) {
            this.MedicalRecordModel.nowHistory = row.xbs;
            this.ChooseRecordList.push(item);
          } else if (
            row.jws !== "" &&
            row.jws != null &&
            item.name === "beforeHistory"
          ) {
            this.MedicalRecordModel.beforeHistory = row.jws;
            this.ChooseRecordList.push(item);
          } else if (
            row.lxbxs !== "" &&
            row.lxbxs != null &&
            item.name === "epidemicDisease"
          ) {
            this.MedicalRecordModel.epidemicDisease = row.lxbxs;
            this.ChooseRecordList.push(item);
          } else if (
            row.qtjc !== "" &&
            row.qtjc != null &&
            item.name === "otherCheck"
          ) {
            this.MedicalRecordModel.otherCheck = row.qtjc;
            this.ChooseRecordList.push(item);
          } else if (
            row.clqk !== "" &&
            row.clqk != null &&
            item.name === "handlingSituation"
          ) {
            this.MedicalRecordModel.handlingSituation = row.clqk;
            this.ChooseRecordList.push(item);
          } else if (
            row.gthjkjy !== "" &&
            row.gthjkjy != null &&
            item.name === "healthEducation"
          ) {
            this.MedicalRecordModel.healthEducation = row.gthjkjy;
            this.ChooseRecordList.push(item);
          }
        });
        this.BasicInfoModel.temperature = row.temperature
        this.BasicInfoModel.pulse = row.pulse
        this.BasicInfoModel.breathe = row.breathe
        this.BasicInfoModel.bloodPressure = row.bloodPressure
        this.MedicalRecordModel.westernDiagnose = row.westernDiagnose
        this.MedicalRecordModel.chinaDiagnose = row.chinaDiagnose
        this.blmbDialogVisible = false;
      },
      saveblmbclick() {
        this.blmblxDialogVisible = true;
      },
      //Đóng hộp thoại
      blmbonDialogClose() {
        this.blmbmbmc.mbmc = "";
        this.blmbmblx = "0";
        this.blmbmblx = "0";
        this.blmblxDialogVisible = false;
      },
      saveblmbsubmit(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.saveblmblist.zs = this.MedicalRecordModel.patientTell;
            this.saveblmblist.grs = this.MedicalRecordModel.personalHistory;
            this.saveblmblist.gms = this.MedicalRecordModel.allergyHistory;
            this.saveblmblist.jbs = this.MedicalRecordModel.diseaseHistory;
            this.saveblmblist.crbs = this.MedicalRecordModel.infectiousHistory;
            this.saveblmblist.sss = this.MedicalRecordModel.surgeryHistory;
            this.saveblmblist.sxs = this.MedicalRecordModel.transfusionHistory;
            this.saveblmblist.tgjc = this.MedicalRecordModel.physiqueChec;
            this.saveblmblist.jzzd = this.MedicalRecordModel.emergencyDiagnose;
            this.saveblmblist.jzxg = this.MedicalRecordModel.emergencyEffect;
            this.saveblmblist.jzs = this.MedicalRecordModel.familyHistory;
            this.saveblmblist.yjz = this.MedicalRecordModel.lunariaHistory;
            this.saveblmblist.hys = this.MedicalRecordModel.pregnancyHistory;
            this.saveblmblist.fzjc = this.MedicalRecordModel.assistChec;
            this.saveblmblist.xbs = this.MedicalRecordModel.nowHistory;
            this.saveblmblist.jws = this.MedicalRecordModel.beforeHistory;
            this.saveblmblist.lxbxs = this.MedicalRecordModel.epidemicDisease;
            this.saveblmblist.qtjc = this.MedicalRecordModel.otherCheck;
            this.saveblmblist.clqk = this.MedicalRecordModel.handlingSituation;
            this.saveblmblist.gthjkjy = this.MedicalRecordModel.healthEducation;
            this.saveblmblist.mbmc = this.blmbmbmc.mbmc;
            this.saveblmblist.mblx = this.blmbmblx;
            this.saveblmblist.bllx = this.blmbbllx;
            this.saveblmblist.temperature = this.BasicInfoModel.temperature
            this.saveblmblist.pulse = this.BasicInfoModel.pulse
            this.saveblmblist.breathe = this.BasicInfoModel.breathe
            this.saveblmblist.bloodPressure = this.BasicInfoModel.bloodPressure
            this.saveblmblist.westernDiagnose = this.MedicalRecordModel.westernDiagnose
            this.saveblmblist.chinaDiagnose = this.MedicalRecordModel.chinaDiagnose
            saveblmb(this.saveblmblist)
              .then((responseData) => {
                if (responseData.code === 100) {
                  this.blmbmbmc.mbmc = "";
                  this.blmbmblx = "0";
                  this.blmbmblx = "0";
                  this.blmbonDialogClose();
                  this.$message.success(responseData.msg);
                }
              })
              .catch((error) => {
                this.$message.error(error);
              });
          } else {
            return false;
          }
        });
      },
      JZrecordclick(done) {
        done();
      },
      Getrecordlist() {
        this.recordtableData = [];
        getrecordpatlist(this.recordpatid).then((responseData) => {
          if (responseData.code === 100) {
            responseData.data.forEach((item) => {
              if (item.xyzd !== "" && item.xyzd != null) {
                item.xyzd;
              } else {
                item.xyzd = "Bán lẻ";
              }
              if (item.zyzd !== "" && item.zyzd != null) {
                item.zyzd;
              } else {
                item.zyzd = "";
              }
              this.recordtableData.push({
                jzsj: item.jzsj,
                jbxx: item.xm +
                  " /" +
                  item.nl +
                  "tuổi" +
                  " /" +
                  item.xyzd +
                  " /" +
                  item.zyzd,
                regid: item.regid,
                patid: item.patid,
              });
            });
          }
        });
        // .catch((error) => {
        //        this.$message.error(error);
        //      });
      },
      jzjlclick() {
        if (this.recordpatid !== "" && this.recordpatid != null) {
          this.drawer = true;
          this.Getrecordlist();
        } else {
          this.$message.warning("Vui lòng chọn bệnh nhân！");
        }
      },
      jzjlhandleClick(row) {
        this.drawer = false;

        this.clickId = row.regid;
        this.$nextTick(() => {
          this.MedicalRecordModel = this.ReturnMedicalModel();

          this.hasBtnFinishVisit = false;
          this.hasBtnPrint = true;
          this.isReadOnly = true;
          this.recipelInfoId = "";
          this.AlreadyPatientDescriptions = [];
          //Theo bệnh nhânidLấy thông tin thành viên
          getByPatientId(row.patid)
            .then((res) => {
              if (res.code === 100) {
                this.member = res.data;
              } else {
                this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！");
              }
            })
            .catch();
          getPoverty(row.patid)
            .then((res) => {
              if (res.code === 100) {
                this.poverty = res.data;
              } else {
                this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！");
              }
            })
            .catch();
          //   //Theo bệnh nhânidLấy thông tin thành viên
          // getByPatientId(row.patid).then(res=>{
          //   if(res.code==='100'){
          //     this.member=res.data
          //   }else{
          //     this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！")
          //   }
          // }).catch()
          allQueryMedicalRecord(row.regid).then((responseData) => {
            let arr = responseData.data;
            if (responseData.code === 100) {
              let recipelInfoEvtList = responseData.data.recipelInfoEvtList;
              recipelInfoEvtList.forEach((element) => {
                this.AlreadyPatientDescriptions.push(
                  JSON.parse(JSON.stringify(element.recipelInfo))
                );
              });
              this.AlreadyPatientDescriptions.sort((a, b) => {
                a.seq = a.seq ? a.seq : 0;
                b.seq = b.seq ? b.seq : 0;
                return a.seq - b.seq;
              });
              this.MedicalRecordModel = responseData.data.medicalRecord;
              this.MedicalRecordModel.registration.id =
                responseData.data.registration.id;
              this.MedicalRecordModel.registration.treatType =
                responseData.data.registration.treatType;
              this.MedicalRecordModel.registration.morbidityTime =
                responseData.data.registration.morbidityTime;
              this.MedicalRecordModel.registration.infectType =
                responseData.data.registration.infectType;
              this.BasicInfoModel = responseData.data.patient;
              this.BasicInfoModel.breathe =
                responseData.data.registration.breathe;
              this.BasicInfoModel.temperature =
                responseData.data.registration.temperature;
              this.BasicInfoModel.pulse = responseData.data.registration.pulse;
              this.BasicInfoModel.bloodPressure =
                responseData.data.registration.bloodPressure;
              this.GetMedicineCofigure();
              this.createMedicalEditTab(recipelInfoEvtList);
              this.hint = false;
              this.userClickID = "";
            }
          });
          // this.GetPatient(itemCopy);
        });
        this.overColor = 0;
        // //this.recordpatid=row.patientId.id;
        // //this.clickChangeColor=row.id
        // // if(this.clickId!=='' && this.clickId===this.activeSelAlreadyPatientItem.id){
        // //   return

        // // }else
        // if(row.regid!==undefined){

        //    if(this.endHint){
        //   this.clickId=row.regid
        // // if (!this.activeSelAlreadyPatientItem) {
        // //   return;
        // // }
        // let now = new Date()
        //   let newDate = new Date(this.activeSelAlreadyPatientItem.receptionEndDate)
        //   now = Date.parse(now)
        //   newDate = Date.parse(newDate)
        //   if(now-newDate>86400000){
        //     this.timeOut = true
        //   }else{
        //     this.timeOut = false
        //   }
        // let itemCopy = JSON.parse(
        //   JSON.stringify(this.activeSelAlreadyPatientItem)
        // );
        // this.registration = JSON.parse(
        //   JSON.stringify(this.activeSelAlreadyPatientItem)
        // );
        // this.$nextTick(() => {
        //   this.MedicalRecordModel = this.ReturnMedicalModel();

        //   this.hasBtnFinishVisit = false;
        //   this.hasBtnPrint = true;
        //   this.isReadOnly = true;
        //   this.recipelInfoId = "";
        //   this.AlreadyPatientDescriptions = [];
        //   //Theo bệnh nhânidLấy thông tin thành viên
        //   getByPatientId(row.patid).then(res=>{
        //     if(res.code==='100'){
        //       this.member=res.data
        //     }else{
        //       this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！")
        //     }
        //   }).catch()
        //   allQueryMedicalRecord(row.regid).then((responseData) => {

        //     let arr=responseData.data
        //     if (responseData.code === 100) {

        //       let recipelInfoEvtList = responseData.data.recipelInfoEvtList;
        //       recipelInfoEvtList.forEach((element) => {
        //         this.AlreadyPatientDescriptions.push(
        //           JSON.parse(JSON.stringify(element.recipelInfo))
        //         );
        //       });
        //       this.AlreadyPatientDescriptions.sort((a, b) => {
        //         a.seq = a.seq ? a.seq : 0;
        //         b.seq = b.seq ? b.seq : 0;
        //         return a.seq - b.seq;
        //       });
        //       this.MedicalRecordModel = responseData.data.medicalRecord;
        //       this.MedicalRecordModel.registration.id = responseData.registration.id;
        //       this.MedicalRecordModel.registration.treatType = responseData.registration.treatType;
        //       this.BasicInfoModel = responseData.data.patient;
        //       this.BasicInfoModel.breathe = responseData.registration.breathe;
        //       this.BasicInfoModel.temperature =
        //         responseData.registration.temperature;
        //       this.BasicInfoModel.pulse = responseData.registration.pulse;
        //       this.BasicInfoModel.bloodPressure = responseData.registration.bloodPressure;
        //       this.GetMedicineCofigure();
        //       this.createMedicalEditTab(recipelInfoEvtList);
        //     }
        //   });
        //   // this.GetPatient(itemCopy);
        // });
        // this.overColor=0
        // }else{
        //   this.$confirm('Bệnh nhân hiện tại đang được chỉnh sửa,Chưa lưu, bạn có chắc muốn chuyển không?', 'Nhắc nhở', {
        //     confirmButtonText: 'Xác nhận',
        //     cancelButtonText: 'Hủy',
        //     type: 'warning'
        //   }).then(() => {
        // this.clickId=this.activeSelAlreadyPatientItem.id
        // if (!this.activeSelAlreadyPatientItem) {
        //   return;
        // }
        // let now = new Date()
        //   let newDate = new Date(this.activeSelAlreadyPatientItem.receptionEndDate)
        //   now = Date.parse(now)
        //   newDate = Date.parse(newDate)

        //   if(now-newDate>86400000){
        //     this.timeOut = true
        //   }else{
        //     this.timeOut = false
        //   }
        // let itemCopy = JSON.parse(
        //   JSON.stringify(this.activeSelAlreadyPatientItem)
        // );
        // this.registration = JSON.parse(
        //   JSON.stringify(this.activeSelAlreadyPatientItem)
        // );
        // this.$nextTick(() => {
        //   this.MedicalRecordModel = this.ReturnMedicalModel();

        //   this.hasBtnFinishVisit = false;
        //   this.hasBtnPrint = true;
        //   this.isReadOnly = true;
        //   this.recipelInfoId = "";
        //   this.AlreadyPatientDescriptions = [];
        //    //Theo bệnh nhânidLấy thông tin thành viên
        //   getByPatientId(row.patid).then(res=>{
        //     if(res.code==='100'){
        //       this.member=res.data
        //     }else{
        //       this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！")
        //     }
        //   }).catch()
        //   allQueryMedicalRecord(row.regid).then((responseData) => {
        //     let arr=responseData.data
        //     if (responseData.code === 100) {

        //       let recipelInfoEvtList = responseData.data.recipelInfoEvtList;
        //       recipelInfoEvtList.forEach((element) => {
        //         this.AlreadyPatientDescriptions.push(
        //           JSON.parse(JSON.stringify(element.recipelInfo))
        //         );
        //       });
        //       this.AlreadyPatientDescriptions.sort((a, b) => {
        //         a.seq = a.seq ? a.seq : 0;
        //         b.seq = b.seq ? b.seq : 0;
        //         return a.seq - b.seq;
        //       });
        //       this.MedicalRecordModel = responseData.data.medicalRecord;
        //       this.MedicalRecordModel.registration.id = responseData.registration.id;
        //       this.MedicalRecordModel.registration.treatType = responseData.registration.treatType;
        //       this.BasicInfoModel = responseData.data.patient;
        //       this.BasicInfoModel.breathe = this.registration.breathe;
        //       this.BasicInfoModel.temperature =
        //         this.registration.temperature;
        //       this.BasicInfoModel.pulse = this.registration.pulse;
        //       this.BasicInfoModel.bloodPressure = this.registration.bloodPressure;
        //       this.GetMedicineCofigure();
        //       this.createMedicalEditTab(recipelInfoEvtList);
        //     }
        //   });
        // });
        // this.overColor=0
        // this.endHint=true
        //   }).catch(() => {
        //     this.endHint=false

        //   });
        // }
        // }
      },
      onInputChange(zdlx) {
        let searchParam;
        if (zdlx === 'WM') {
           searchParam = this.MedicalRecordModel.westernDiagnose
        } else if (zdlx === 'TCM') {
           searchParam = this.MedicalRecordModel.chinaDiagnose
        } else if(zdlx === 'ZYZH'){
          //Chứng hậu Đông y
          searchParam = this.MedicalRecordModel.chinaSyndrome
          this.ZHpopoverVisible = true;
          getZhList(searchParam.trim()).then(res => {
            if(res.code === 100){
              this.zyzhList = res.data.records
            }
        }).catch((res) => {
            this.showMessage(res);
          });
          return
        }
        // 防抖后触发查询，若没有输入则查询所有，若有输入则传入查询条件
        getZdList(searchParam.trim(),zdlx).then(res => {
          if(res.code === 100){
          if (zdlx === 'WM') {
            this.WMpopoverVisible = true;
          } else if (zdlx === 'TCM') {
            this.TCMpopoverVisible = true;
          }
          this.diagnosesList = res.data.records;
          }
        }).catch();
      },
      selectDiagnosis(row) {
        this.MedicalRecordModel.diseaseId = row.zdid;
        if(row.zdlx === 'WM'){
          this.MedicalRecordModel.westernDiagnose = row.zdmc;  // Điền tên chẩn đoán vào ô nhập
          this.WMpopoverVisible = false;  // Đóng hộp thoại
        }else if(row.zdlx === 'TCM'){
          this.MedicalRecordModel.chinaDiagnose = row.zdmc;  // Điền tên chẩn đoán vào ô nhập
          this.TCMpopoverVisible = false;  // Đóng hộp thoại
        }
        this.diagnosesList = [];
      },
      selectZH(row){
        this.MedicalRecordModel.chinaSyndrome = row.zhmc;  // Điền tên chẩn đoán vào ô nhập
        this.MedicalRecordModel.syndromeId = row.zhid;
        this.ZHpopoverVisible = false;  // Đóng hộp thoại
        this.zyzhList = [];

     },
      onCreateRegistration() {
        this.$refs.registrationForm.openAddRegistrationDialog(this.permission.remove,this.permission.view);
      },
      focusNextInput(index) {
        //先录入药品再回车流程
        if(index === ''){
          document.getElementById('singleDosageInput').focus();
          return
        }
        if (index === 'fj'){
          document.getElementById('fjinput').focus();
          return
        }
      //   const nextInput = this.$refs.herbMedicine[index + 1];  // 获取下一个 input 元素
      //   if (nextInput) {
      //     nextInput.focus();  // 聚焦到下一个输入框
      //   } else {
      //     this.$nextTick(() => {
      //       const dosageInput = document.getElementById('dosageInput');
      //       dosageInput.focus(); // 设置剂量焦点
      //   });
      // }
        //录入单个药品后回车流程
        if(index === '1'){
             document.getElementById('WesternInput').focus();
        }if(index === '2'){
          //Mặc định
          this.SearchChineseInput='';
          this.RowClickChineseTable(this.ChineseMedicineTable[0]);
          document.getElementById('WesternInput').blur();
        }
     }
    },
    data() {
      return {
        // Tiền sử dị ứng của bệnh nhân lấy từ hồ sơ khám cũ nhất gần nhất
        // (không tự bịa: chỉ hiển thị khi hồ sơ đã lưu có allergyHistory).
        patientAllergyHistory: '',
        // id bệnh nhân đã xác nhận cảnh báo dị ứng khi kê đơn (mỗi bệnh nhân
        // chỉ hỏi xác nhận một lần trong một ca khám).
        allergyWarningPatientId: '',
        permission: {
          view: false,
          add: false,
          edit: false,
          remove: false,
          export: false,
          skip: false,
        },
        //诊断证候控制
        WMpopoverVisible: false,
        TCMpopoverVisible:false,
        ZHpopoverVisible:false,
        syndromePopoverVisible:false,
        diagnosesList: [],//Chẩn đoán
        zyzhList:[],//Chứng hậu Đông y
        syndromesList:[],//中医证候列表
        //Loại y tế：western-Tây y，chinese-Đông y，默认西医
        medicalType: 'western',
        queryModel: {
          dateRange: [],
        },
        templateQueryModel: {
          category: "3",
        },
        templateType: "",
        historyRecipelDetailList: [],
        historyRecipelList: [],
        historicalCasesList: [],
        historyRecipelAdditionalList: [],
        historyDialogVisible: false,
        caseDialogVisible: false,
        gender: 'man',
        pickerOptions: {
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
        morbidityTime: '',
        registration: {
          morbidityTime: "",
          treatType: {
            name: "初诊",
            value: "treatType_0",
          },
          infectType: {
            name: "Không",
            value: "infectType_0",
          },
        },
        basics: {},
        savelock: false,
        timeOut: false,
        medicalEditTabsValue: {},
        medicalClickTabsValue: {},
        medicalTypeList: [{
          name: "Đơn thuốc tây",
          type: "recipelType_0",
        },
          {
            name: "Đơn thuốc đông y",
            type: "recipelType_1",
          },
          {
            name: "Đơn truyền dịch",
            type: "recipelType_2",
          },
          {
            name: "Hạng mục khám chữa bệnh",
            type: "recipelType_3",
          },
        ],
        //是否快速接诊
        isInstant: 0,
        ChineseButton: false,
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
        //Tìm thông tin bệnh nhân
        SearchPatientInfo: "",
        //Điều kiện tìm kiếm khám bệnh
        SearchPreModel: {
          columnName: "",
          limit: 20,
          offset: 0,
          order: "",
        },
        SearchAlreadyModel: {
          columnName: "reception_end_date",
          limit: 20,
          offset: 0,
          order: "desc",
        },
        activeSelAlreadyPatientItem: "",
        AlreadyPatientDescriptions: [],
        //Tùy chọn tiểu loại đơn thuốc
        RecipelSmallTypeList: [],
        //Danh sách thả xuống cách dùng thuốc Đông y
        ChineseUseOption: [],
        ChineseTimeOption: [],
        ChineseUseTimeOption: [],
        //Danh sách thả xuống tần suất thuốc Tây y
        FrequencyOption: [],
        //Danh sách thả xuống cách dùng thuốc Tây y
        WesternUseOption: [],
        //Danh sách thả xuống số ngày thuốc Tây y
        DayNumOption: [],
        infuseUseOption: [],
        InfusionOption: [],
        TreatTypeOption: [],
        InfectTypeOption: [],
        //Dữ liệu chính bảng đơn thuốc Tây y
        WesternMedicineTable: [],
        InfusionTable: [],
        //Dữ liệu bảng đơn thuốc Đông y
        ChineseMedicineTable: [],
        //Bảng hạng mục khám chữa bệnh
        TreatmentTable: [],
        //Dữ liệu bảng phí phụ thêm có thể chọn
        SurchargeTable: [],
        //Tìm thuốc Tây y
        SearchWesternInput: "",
        //Tìm thuốc Đông y
        SearchChineseInput: "",
        //Tìm hạng mục khám chữa bệnh
        SearchCostItemInput: "",
        //Tìm phí phụ thêm
        SearchSurchargeInput: "",
        //Tìm truyền dịch
        SearchInfusion: [""],
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
        fileIdFile: {
          fileId: "",
          action: "add",
          deletes: [], // Chờ xóa（Đã tải lên）danh sách tệp
          uploads: [], // Danh sách tệp chờ tải lên
        },
        //Trường cấu hình bệnh án
        MedicineRecordLabelList: [{
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
          {
            label: "Bệnh sử hiện tại",
            name: "nowHistory",
            value: "",
          },
          {
            label: "Tiền sử bệnh",
            name: "beforeHistory",
            value: "",
          },
          {
            label: "Tiền sử dịch tễ",
            name: "epidemicDisease",
            value: "",
          },
          {
            label: "Xét nghiệm khác",
            name: "otherCheck",
            value: "",
          },
          {
            label: "Tình hình xử lý",
            name: "handlingSituation",
            value: "",
          },
          {
            label: "Giáo dục sức khỏe cá nhân hóa",
            name: "healthEducation",
            value: "",
          },
        ],
        //Cấu hình bệnh án đang chọn
        ChooseRecordList: [],
        //Kê đơn thuốcTabsTab
        medicalEditTabs: [],
        //Thông tin cơ bản
        BasicInfoModel: {
          name: "",
          gender: {
            name: "",
          },
          age: "",
          month: "",
          phone: "",
          card: "",
          withPatientNexus: {
            name: "",
          },
          healthCardNo: "",
          pulse: "",
          breathe: "",
          temperature: "",
        },
        //Biểu mẫu bệnh án
        MedicalRecordModel: this.ReturnMedicalModel(),
        //Xác thực biểu mẫu bệnh nhân
        // BasicInfoFormRules: {
        //   treatType: [
        //     { required: true, message: "Vui lòng chọn loại điều trị", trigger: "blur" },
        //   ],
        //   name: [{ required: true, message: "Vui lòng nhập tên bệnh nhân", trigger: "blur" }],
        //   gender: [{ required: true, message: "Vui lòng chọn giới tính", trigger: "blur" }],
        //   age: [{ required: true, message: "Vui lòng nhập tuổi", trigger: "blur" }],
        //   phone: [{ required: true, message: "Vui lòng nhập thông tin liên hệ", trigger: "blur" }],
        //   card: [{ required: true, message: "Vui lòng nhập số CMND/CCCD", trigger: "blur" }],
        //   patientTell: [
        //     { required: true, message: "Vui lòng nhập lý do khám chính", trigger: "blur" },
        //   ],
        // },

        //开具处方数据集合
        recipelInfoEvtList: [],
        payAmount: 0,
        //打印时候的处方ID
        recipelInfoId: "",
        //是否显示完成接诊按钮
        hasBtnFinishVisit: true,
        //是否显示打印按钮
        hasBtnPrint: false,
        //Chỉ đọc
        isReadOnly: true,
        isBaseReadOnly: true,
        //Phòng khám đặc biệt
        isSpecial: false,
        BASE_API: process.env.BASE_API,
        defaultNumber: 1, //Số nhóm mặc định
        infusionProject: [
          []
        ],
        drippingSpeed: [""], //Tốc độ truyền
        days: [{}], //Số ngày
        frequency: [{}], //Tần suất
        infuseUse: [{}], //Cách dùng
        zushu: [1],
        excharge: [], //Phí phụ thêm truyền dịch
        changeData: {},
        hint: false, //用户切换提示
        clickId: "", //点击id
        chineseTest: [
          []
        ],
        timer: 0,
        historyType: "", //历史处方的处方类型
        singleRecipel: 0, //历史处方调用
        templateRecipelList: [],
        expands: [],
        getRowKeys(row) {
          return row.id;
        },
        templateDialogVisible: false, //Hộp thoại mẫu đơn thuốc
        saveTemplateDialogVisible: false, //Lưu mẫu đơn thuốc
        saveTemplateBizFormModel: {
          type: {
            // Loại mẫu
            value: null,
            name: null,
          },
          company: {
            // Phòng khámid
            id: "",
            name: null,
          },
          recipetemplateName: "",
          category: "0",
          diagnosis: "",
          remarks: "",
          recipeTemplateInfo: {},
          recipeTemplateDetail: [{}],
          createId: "",
        }, //保存模板对象
        templateFormRules: {
          diagnosis: [{
            required: true,
            message: "Vui lòng nhập chẩn đoán",
            trigger: "blur"
          }],
          recipetemplateName: [{
            required: true,
            message: "Vui lòng nhập tên mẫu",
            trigger: "blur"
          },],
        }, //保存模板校验
        saveTemplateType: "", //Loại mẫu
        saveTemplateIndex: 0, //对应的处方索引
        backgroundColors: 10000, //Màu nền
        overColor: 0, //完成就诊背景颜色
        userClickID: "", //Người dùngid
        endHint: true, //完成就诊后切换用户是否提示
        clickChangeColor: "",
        inspectionType: [],
        inspectionSign: 0,
        inspectionDialogVisible: false,
        typeName: "",
        costInspection: {}, //检验检查保存
        uploadFiles: "", //Tệpid
        member: [], //Thẻ hội viên
        poverty: [], //贫困

        //病历模板弹框
        blmbDialogVisible: false,
        blmblxDialogVisible: false,
        //病历模板查询条件

        blmbcurrentPage: 1,
        blmbpatientTotal: 0,
        blmbcxrc: {
          limit: 20,
          offset: 0,
          companyId: currentUser.company.id,
          mbmc: "",
        },
        blmbshow: false,
        blmbtableData: [],
        blmbgetRowKeys(row) {
          return row.mbbm;
        },
        blmbexpands: [],
        blmbList: [{
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
          {
            label: "Bệnh sử hiện tại",
            name: "nowHistory",
            value: "",
          },
          {
            label: "Tiền sử bệnh",
            name: "beforeHistory",
            value: "",
          },
          {
            label: "Tiền sử dịch tễ",
            name: "epidemicDisease",
            value: "",
          },
          {
            label: "Xét nghiệm khác",
            name: "otherCheck",
            value: "",
          },
          {
            label: "Tình hình xử lý",
            name: "handlingSituation",
            value: "",
          },
          {
            label: "Giáo dục sức khỏe cá nhân hóa",
            name: "healthEducation",
            value: "",
          },
        ],
        saveblmblist: {
          mbbm: "",
          companyId: currentUser.company.id,
          delFlag: "0",
          createdBy: currentUser.name + "(" + currentUser.loginname + ")",
          createdTime: "",
          updatedBy: "",
          updatedTime: "",
          createdID: currentUser.id,
          mbmc: "",
          mblx: "",
          bllx: "",
          zs: "",
          grs: "",
          gms: "",
          jbs: "",
          crbs: "",
          sss: "",
          sxs: "",
          tgjc: "",
          jzzd: "",
          jzxg: "",
          jzs: "",
          yjz: "",
          hys: "",
          fzjc: "",
          xbs: "",
          jws: "",
          lxbxs: "",
          qtjc: "",
          clqk: "",
          gthjkjy: "",
        },
        blmbmbmc: {
          mbmc: "",
        },
        blmbmblx: "0",
        blmbbllx: "0",
        blmbrules: {
          mbmc: [{
            required: true,
            message: "Vui lòng nhập tên mẫu",
            trigger: "blur"
          }],
        },
        drawer: false,
        direction: "rtl",
        recordpatid: "",
        recordtableData: [],
        blmbVisit: false,

        beyondInventoryType: false, //药品超出库存状态
        beyondStuffInventoryType: false, //材料超出库存
        cancellation: false, //
        chronicDisease: false, //Có phải bệnh mạn tính không
        systemParamConfig: {
          chronicDays: 90,
          normalDays: 7,
        }, //Cấu hình tham số hệ thống
        systemParamConfigSearch: {
          params: [
            {
              columnName: "category",
              queryType: "=",
              value: "western_medicine_disease_category"
            },
            {
              columnName: "company_id",
              queryType: "=",
              value: getLocalCurrentCompany().id
            }
          ]
        },
        //文件上传
        MedicalFlags: "add",
        MedicalFileObjId: "",
        deleteFileIdList: [],
      };
    },
    computed: {
      // Dị ứng hiển thị trên màn khám: ưu tiên dị ứng vừa ghi trong hồ sơ
      // của ca khám hiện tại, nếu chưa có thì lấy từ hồ sơ khám cũ.
      displayAllergyHistory() {
        return (this.MedicalRecordModel && this.MedicalRecordModel.allergyHistory) || this.patientAllergyHistory || ''
      },
      Company() {
        let company = JSON.parse(sessionStorage.getItem("currentUser")).company;
        return {
          id: company.id,
          label: company.label,
          name: company.name,
        };
      },
      UserId() {
        return JSON.parse(sessionStorage.getItem("currentUser")).id;
      },
    },
    mounted() {
      let specialCompany = JSON.parse(sessionStorage.getItem("currentCompany"));
      if (specialCompany && specialCompany.id === "1088657523871555640") {
        this.isSpecial = true;
      }
      this.GetAllOption();
      this.GetMedicalRecordAll();
      this.GetPreparePatientList();
      this.GetAlreadyPatientList();
      this.TodayActiveName = "prepare";

      // this.addMedicalEditTab(this.medicalTypeList[0]);


    },
    watch: {
      checkInventory(row) {
      },
      // Khi mở ca khám của bệnh nhân khác thì tải lại tiền sử dị ứng.
      'BasicInfoModel.id'(newId) {
        this.loadPatientAllergy(newId)
      },
    },
  };
</script>

<style>
  .el-input-number {
    width: 1px;
    line-height: 10px;
    height: 28px;
  }

  .ones .el-table thead th {
    background-color: #f7f1f1 !important;
  }

  .two .el-table thead th {
    background-color: #e8fcfc !important;
  }

  .changeMedicalOutPatient .alreadyMedocal .el-collapse-item__header {
    border-bottom: 1px solid #dcdfe6;
    margin-right: 15px;
    background-color: rgb(234, 242, 251) !important;
  }
</style>

<style scoped>
  .shuzhuClass .el-input-number {
    width: 10% !important;
  }

  ::v-deep .el-aside {
    /* background-color: #e6d6d3; */
    color: #333;
    /* text-align: center; */
    /* line-height: 200px; */
    margin-right: 5px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
    height: calc(100vh - 118px);
  }

  ::v-deep .el-main {
    /* background-color: #E9EEF3; */
    color: #333;
    /* text-align: center; */
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
    /* line-height: 200px; */
    height: calc(100vh - 118px);
  }

  ::v-deep .el-card__header {
    padding: 12px 20px;
  }

  ::v-deep .el-form-item--mini {
    margin-bottom: 8px !important;
  }

  ::v-deep .main-card {
    margin-bottom: 10px;
  }

  ::v-deep .main-card > .el-card__header {
    padding: 0px 20px;
  }

  ::v-deep .main-card > .el-card__body {
    font-size: 14px;
  }

  ::v-deep .sub-card > .el-card__body {
    padding: 12px 20px;
    font-size: 16px;
    font-weight: bold;
    color: midnightblue;
  }

  ::v-deep .chinese-medicine-card > .el-card__header {
    padding: 0px 20px;
  }

  ::v-deep .el-divider--horizontal {
    margin: 5px 0 10px 0px;
  }

  ::v-deep .el-tabs__header {
    margin: 0 0 0;
  }

  ::v-deep .el-collapse-item__content {
    padding-bottom: 0;
  }

  ::v-deep .el-divider--vertical {
    width: 5px;
    height: 2em;
    margin-left: 0;
    background-color: #409eff;
  }

  ::v-deep .el-input-group__append {
    padding: 4px;
  }
  .medical-type-ul {
    list-style-type: none;
    font-size: 13px;
    color: #303133;
    margin: 5px;
    padding: 0;
  }

  .aside-header {
    position: relative; /* 使子元素可以相对于它定位 */
    font-weight: bold;
    line-height: 10px;
    height: 10px;
    font-size: 14px;
  }

  /* .box-card {
    width: 100%;
    height: 100%;
  } */

  .el-row {
    margin-bottom: 8px;
  }

  .el-row:last-child {
    margin-bottom: 0;
  }

  /* ::v-deep .already-medocal .el-collapse-item__header{
  border-bottom: 1px solid #dcdfe6;
  margin-right: 15px;
  background: #409eff !important;
} */

  .refundBox {
    position: absolute;
    top: -23px;
    right: -80px;
    width: 80px;
    height: 38px;
    border: 2px solid #e6a23c;
    text-align: center;
    font-size: 18px;
    border-radius: 80%;
    color: #e6a23c;
    transform: scale(0.5);
  }
</style>
<style>
  .el-popover.medical-type-popover {
    min-width: 85px !important;
    margin: 0;
    padding: 0;
  }

  .el-popover.medical-edit-popover {
    margin: 0;
    padding: 0;
  }

  .ipt-box .el-input--mini {
    width: 70%;
  }

  .ipt-box {
    display: flex;
    align-items: center;
  }

  .click {
    background-color: rgb(234, 242, 251) !important;
    color: #fff;
  }

  .clickTest {
    background-color: rgb(234, 242, 251) !important;
    color: #fff;
  }

  .imgStyle {
    /* display: inline-flex; */
    position: absolute;
    top: 0;
    right: -30px;
  }

  .nameStyle {
    position: relative;
    display: inline-block;
  }

  /* 医疗基础信息表单样式*/
  .medical-base-form .el-form-item {
    margin-bottom: 10px;
  }

  .medical-base-form .el-form-item .el-form-item__label {
    font-weight: normal;
    color: #606266;
    font-size: 14px;
  }

  .medical-base-form .info-content {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .medical-base-form .info-content .patient-name {
    font-size: 14px;
    color: #303133;
    font-weight: 600;
  }

  .medical-base-form .info-content .vip-tag {
    display: inline-flex;
    align-items: center;
  }

  .medical-base-form .info-content .poverty-tag {
    color: #f56c6c;
    font-weight: 600;
    font-size: 13px;
  }

  .medical-base-form .info-text {
    font-size: 14px;
    color: #303133;
    font-weight: 600;
  }

  .medical-base-form .el-radio-group {
    display: inline-flex;
    align-items: center;
  }

  .medical-base-form .el-date-editor {
    width: 180px;
  }
</style>
<style lang="scss">
  .demo-table-expand {
    font-size: 0;
  }

  .demo-table-expand label {
    width: 90px;
    color: #99a9bf;
  }

  .demo-table-expand .el-form-item {
    margin-right: 0;
    margin-bottom: 0;
    width: 45%;
  }

  .demo-table-expand .el-form-item__content {
    margin-right: 0;
    margin-bottom: 0;
    width: 45%;
  }
</style>
