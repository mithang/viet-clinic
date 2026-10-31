<template>
  <el-container>
    <el-aside width="400px">
      <el-card style="height: 100%">
        <el-row style="display: flex; justify-content: flex-start">
          <!-- <el-select
            v-model="SearchPatientId"
            value-key="id"
            filterable
            clearable
            style="width: 100%"
            placeholder="Vui lòng chọn bệnh nhân"
            @change="GetDispensingTable"
          >
            <el-option
              v-for="patientId in AllPatientOption"
              :key="patientId.id"
              :label="patientId.name"
              :value="patientId.id"
            ></el-option>
          </el-select> -->
          <el-input
            v-model="selectCondition"
            placeholder="Vui lòng nhập tên bệnh nhân/Số thẻ"
            prefix-icon="el-icon-search"
            class="input-with-select"
            @keydown.enter.native="search"
          ></el-input>
          <!-- <el-button
            style="width: 100px"
            slot="append"
            icon="el-icon-search"
            @click="search"
            >Tìm kiếm</el-button
          > -->
        </el-row>
        <el-row>
          <el-tabs stretch v-model="ActiveName">
            <el-tab-pane name="IsWaitingDrug">
              <span slot="label"
                >Chờ phát thuốc<el-badge
                  :value="WaitingTotal"
                  :max="99"
                  type="primary"
                  style="line-height: normal"
                ></el-badge></span
              >
              <el-scrollbar view-style="height:calc(100vh - 267px)">
                <el-collapse
                  v-model="patientInfoRow"
                  accordion
                  @change="selectPatientChange()"
                >
                  <div v-for="(item, index) in WaitingTable" :key="item">
                    <el-collapse-item class="already-medocal" :name="item">
                      <template slot="title">
                        <span
                          style="
                            font-weight: bold;
                            font-size: 12px;
                            color: #606266;
                            flex: 1;
                          "
                          >{{ PageRegistration.offset + index + 1 }}. {{ item.patientId.name }}/ {{ item.patientId.gender.name }} /
                      {{ item.patientId.age }}tuổi</span
                        >
                        <span class="redStateStyle">{{
                          item.chargeDate
                        }}</span>
                      </template>
                      <div
                        v-for="(o,index) in prescriptionMainList"
                        :key="index"
                        @click="AlreadyPatientDescriptionsQuery(o,index)"
                        :class="{clickTest:index===overColor}"
                      >
                       <div  style="

                            border-top: 1px dashed #dcdfe6;
                            margin: 0px 30px 0 15px;"></div>
                          <!-- <br> -->
                      <p style="font-weight: bold;margin-top: 15px;margin-left:30px;font-size: 12px;
                            color: #606266;">
                        {{ o.recipelInfo.name }}
                      </p>

                      <p style="font-weight: bold;margin-top: 6px;margin-left:30px;font-size: 12px;
                            color: #606266;">
                        {{ o.recipelInfo.id }}

                      </p>
                       <div style="
                            border-top: 1px dashed #dcdfe6;
                            margin: 0px 30px 0 15px;"></div>
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
                            >{{ o.recipelInfo.name }}</el-descriptions-item
                          >
                          <el-descriptions-item
                            >{{ o.recipelInfo.id }}
                          </el-descriptions-item>
                        </el-descriptions> -->
                      </div>
                    </el-collapse-item>
                  </div>
                </el-collapse>
              </el-scrollbar>
              <el-pagination
                small
                background
                @current-change="DispensingPage"
                :page-size="dispensingPageSize"
                layout="prev, pager, next"
                :total="WaitingTotal"
              >
              </el-pagination>
            </el-tab-pane>
            <el-tab-pane name="IsAll">
              <span slot="label">
                Đã phát thuốc
                <el-badge
                  :value="OtherTotal"
                  :max="99"
                  type="primary"
                  style="line-height: normal"
                ></el-badge>
              </span>
              <el-scrollbar view-style="height:calc(100vh - 267px);">
                <el-collapse
                  v-model="patientInfoRow"
                  accordion
                  @change="selectPatientChange()"
                >
                  <div v-for="(item, index) in dispensingTable" :key="item">
                    <el-collapse-item class="already-medocal" :name="item">
                      <template slot="title">
                        <span
                          style="
                            font-weight: bold;
                            font-size: 12px;
                            color: #606266;
                            flex: 1;
                          "
                          >{{ PageRegistration.offset + index + 1 }}. {{ item.patientId.name }}/ {{ item.patientId.gender.name }} /
                      {{ item.patientId.age }}tuổi</span
                        >
                        <span>{{
                          item.dispensingDate
                        }}</span>
                      </template>
                      <div
                        v-for="(o,index) in prescriptionMainList"
                        :key="index"
                        @click="AlreadyPatientDescriptionsQuery(o,index)"
                        :class="{clickTest:index===overColor}"
                      >
                       <div  style="

                            border-top: 1px dashed #dcdfe6;
                            margin: 0px 30px 0 15px;"></div>
                          <!-- <br> -->
                      <p style="font-weight: bold;margin-top: 15px;margin-left:30px;font-size: 12px;
                            color: #606266;">
                        {{ o.recipelInfo.name }}
                      </p>

                      <p style="font-weight: bold;margin-top: 6px;margin-left:30px;font-size: 12px;
                            color: #606266;">
                        {{ o.recipelInfo.id }}

                      </p>
                       <div style="
                            border-top: 1px dashed #dcdfe6;
                            margin: 0px 30px 0 15px;"></div>
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
                            >{{ o.recipelInfo.name }}</el-descriptions-item
                          >
                          <el-descriptions-item
                            >{{ o.recipelInfo.id }}
                          </el-descriptions-item>
                        </el-descriptions> -->
                      </div>
                    </el-collapse-item>
                  </div>
                </el-collapse>
              </el-scrollbar>
              <el-pagination
                small
                background
                @current-change="allPage"
                :page-size="dispensingPageSize"
                layout="prev, pager, next"
                :total="OtherTotal"
              >
              </el-pagination>
            </el-tab-pane>
            <el-tab-pane name="return">
              <span slot="label">
                Đã trả thuốc
                <el-badge
                  :value="returnTotal"
                  :max="99"
                  type="primary"
                  style="line-height: normal"
                ></el-badge>
              </span>
              <el-scrollbar view-style="height:calc(100vh - 267px);">
                <el-collapse
                  v-model="patientInfoRow"
                  accordion
                  @change="selectPatientChange()"
                >
                  <div v-for="(item, index) in returnTable" :key="item">
                    <el-collapse-item class="already-medocal" :name="item">
                      <template slot="title">
                        <span
                          style="
                            font-weight: bold;
                            font-size: 12px;
                            color: #606266;
                            flex: 1;
                          "
                          >{{ PageRegistration.offset + index + 1 }}. {{ item.patientId.name }}/ {{ item.patientId.gender.name }} /
                      {{ item.patientId.age }}tuổi</span
                        >
                        <span>{{
                          item.returnDate
                        }}</span>
                      </template>
                      <div
                        v-for="(o,index) in prescriptionMainList"
                        :key="index"
                        @click="AlreadyPatientDescriptionsQuery(o,index)"
                        :class="{clickTest:index===overColor}"
                      >
                       <div  style="

                            border-top: 1px dashed #dcdfe6;
                            margin: 0px 30px 0 15px;"></div>
                          <!-- <br> -->
                      <p style="font-weight: bold;margin-top: 15px;margin-left:30px;font-size: 12px;
                            color: #606266;">
                        {{ o.recipelInfo.name }}
                      </p>

                      <p style="font-weight: bold;margin-top: 6px;margin-left:30px;font-size: 12px;
                            color: #606266;">
                        {{ o.recipelInfo.id }}

                      </p>
                       <div style="
                            border-top: 1px dashed #dcdfe6;
                            margin: 0px 30px 0 15px;"></div>
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
                            >{{ o.recipelInfo.name }}</el-descriptions-item
                          >
                          <el-descriptions-item
                            >{{ o.recipelInfo.id }}
                          </el-descriptions-item>
                        </el-descriptions> -->
                      </div>
                    </el-collapse-item>
                  </div>
                </el-collapse>
              </el-scrollbar>
              <el-pagination
                small
                background
                @current-change="returnPage"
                :page-size="dispensingPageSize"
                layout="prev, pager, next"
                :total="returnTotal"
              >
              </el-pagination>
            </el-tab-pane>
          </el-tabs>
        </el-row>
      </el-card>
    </el-aside>
    <el-main>
      <el-card class="box-card sub-card" style="height: 50px; display: flex">
        <div class="flex-space-between">
          <div class="goods-back" v-if="ActiveName === 'IsAll'">
            Đã phát thuốc
          </div>
          <el-popconfirm
          style="margin-right:10px"
            title="Bạn có chắc muốn phát thuốc không？"
            @confirm="DispensingMedicineClick(0)"
            v-if="ActiveName === 'IsWaitingDrug' && buttonState">
            <el-button slot="reference" type="primary">Phát thuốc</el-button>
          </el-popconfirm>
          <el-popconfirm
          style="margin-right:10px"
            title="Bạn có chắc muốn trả thuốc không？"
            @confirm="DispensingMedicineClick(1)"
            v-if="ActiveName === 'IsAll' && buttonState">
            <el-button slot="reference" type="primary" plain>Trả thuốc</el-button>
          </el-popconfirm>
          <el-button
            type="primary"
            v-if="ActiveName === 'IsAll'"
            @click="print"
            >In</el-button
          >
        </div>
      </el-card>
      <el-card class="box-card" shadow="always">
        <el-scrollbar
          view-style="height:calc(100vh - 198px);padding-right: 20px;"
        >
          <el-card class="box-card main-card">
            <el-form
              ref="patientInfoForm"
              :model="patientInfoForm"
              label-width="auto"
            >
              <el-row :gutter="24">
                <el-col :span="5">
                  <el-form-item label="Tên bệnh nhân" prop="name">
                    <!-- <el-input
                      disabled
                      v-model="patientInfoForm.name"
                    ></el-input> -->
                     <div class="nameStyle">
                      {{patientInfoForm.name?patientInfoForm.name:""}}
                      <div class="imgStyle" v-if="allMember.length>0" >
                       <el-popover
                          placement="top-start"
                          title=""
                          width="200"
                          trigger="hover"
                          >
                           <div v-for="(item,index) in allMember" :key="index" style="padding:5px">
                                    <el-tag>{{item.memberName}}</el-tag>
                            </div>
                           <img  slot="reference"  src="../../../assets/images/vip.png" style="width:25px;height:25px"/>
                        </el-popover>
                    </div>

                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="3">
                  <el-form-item label="Giới tính" prop="gender">
                    <!-- <el-select
                      disabled
                      v-model="patientInfoForm.gender.name"
                      style="width: 100%"
                    >
                      <el-option label="Nam" value="男"></el-option>
                      <el-option label="Nữ" value="女"></el-option>
                    </el-select> -->
                      <span>{{patientInfoForm.gender.name?patientInfoForm.gender.name:""}}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="5">
                  <el-form-item label="Tuổi" prop="age">
                    <!-- <el-input
                      disabled
                      v-model="patientInfoForm.age"
                      style="width: 49%"
                    >
                      <template slot="append">tuổi</template>
                    </el-input>
                    <el-input
                      disabled
                      v-model="patientInfoForm.month"
                      style="width: 49%"
                    >
                      <template slot="append">tháng</template>
                    </el-input> -->
                     <span>
                      {{patientInfoForm.age?patientInfoForm.age:0}}tuổi{{patientInfoForm.month?patientInfoForm.month:0}}tháng
                    </span>
                  </el-form-item>
                </el-col>
                <el-col :span="5">
                  <el-form-item label="Số điện thoại liên hệ" prop="phone">
                    <!-- <el-input
                      disabled
                      v-model="patientInfoForm.phone"
                    ></el-input> -->
                     <span>
                      {{patientInfoForm.phone}}
                    </span>
                  </el-form-item>
                </el-col>
                <el-col :span="5">
                  <el-form-item label="Số CMND/CCCD" prop="card">
                    <!-- <el-input
                      disabled
                      v-model="patientInfoForm.card"
                    ></el-input> -->
                    <span>{{patientInfoForm.card}}</span>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="Chẩn đoán tây y" prop="westernDiagnose">
                    <!-- <el-input
                      disabled
                      v-model="patientInfoForm.westernDiagnose"
                    ></el-input> -->
                     <span>
                      {{patientInfoForm.westernDiagnose?patientInfoForm.westernDiagnose:""}}
                    </span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Chẩn đoán đông y" prop="chinaDiagnose">
                    <!-- <el-input
                      disabled
                      v-model="patientInfoForm.chinaDiagnose"
                    ></el-input> -->
                    <span>
                      {{patientInfoForm.chinaDiagnose?patientInfoForm.chinaDiagnose:""}}
                    </span>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-card>
          <el-card class="box-card main-card" v-if="medicalEditTabs.length > 0">
            <div>
              <el-tabs
                v-model="medicalEditTabsValue"
                type="card"
                @tab-click="clickMedicalEditTab"
              >
                <el-tab-pane
                  v-for="(item, index) in medicalEditTabs"
                  :key="item"
                  :label="item.title"
                  :name="item"
                >
                  <span slot="label">
                    {{ item.title }}
                  </span>
                  <el-row type="flex" justify="space-between" align="center" style="padding-top:10px" v-if="ActiveName === 'IsWaitingDrug' && buttonState">
                    <div class="single">
                      Chi phí của đơn thuốc này: {{ item.content.recipelInfo.amountReceivedTotal }}VNĐ
                    </div>
                    <el-popconfirm
                      title="Bạn có chắc muốn phát thuốc không？"
                      @confirm="singleMedicineClick(0)"
                      style="padding-top:7px"
                      >
                      <el-button slot="reference" type="primary" plain>Phát thuốc theo đơn này</el-button>
                    </el-popconfirm>
                  </el-row>
                  <el-row type="flex" justify="space-between" align="center" style="padding-top:10px" v-if="ActiveName === 'IsAll'">
                    <div class="single">
                      Chi phí của đơn thuốc này: {{ item.content.recipelInfo.amountReceivedTotal }}VNĐ
                    </div>
                    <el-popconfirm
                      title="Bạn có chắc muốn trả thuốc không？"
                      @confirm="singleMedicineClick(1)"
                      style="padding-top:7px"
                      >
                      <el-button slot="reference" type="primary" plain>Trả thuốc theo đơn này</el-button>
                    </el-popconfirm>
                  </el-row>
                  <el-row style="padding-top:8px" v-if="ActiveName === 'return'">
                    <div class="single">
                      Chi phí của đơn thuốc này: {{ item.content.recipelInfo.amountReceivedTotal }}VNĐ
                    </div>
                  </el-row>
                 <div v-if="item.type!=='recipelType_2'">
                    <el-row>
                    <el-divider content-position="left">Thông tin đơn thuốc</el-divider>

                    <el-input
                      disabled
                      v-if="item.type!=='recipelType_3'"
                      :value="item.content.recipelInfo.smallType.name"
                      style="width:100px"
                    >
                    </el-input>
                      <el-checkbox v-if="item.type === 'recipelType_0'" style="margin-left: 20px;" v-model="item.content.recipelInfo.chronicDisease" disabled >Có phải bệnh mạn tính không</el-checkbox>
                  </el-row>
                 </div>
                  <div v-else style="">
                     <el-row>
                    <el-divider content-position="left">Thông tin đơn thuốc</el-divider>

                    <el-input
                      disabled
                      v-if="item.type!=='recipelType_3'"
                      :value="item.content.recipelInfo.smallType.name"
                      style="width:100px"
                    >
                    </el-input>
                  </el-row>
                  </div>

                  <div v-if="item.type!=='recipelType_2'">
                    <el-table :data="item.content.notExtra" style="width: 100%">
                    <el-table-column label="Số thứ tự" type="index" align="center" width="width">
                    </el-table-column>
                    <el-table-column
                      prop="drugStuffId.name"
                      label="Tên"
                      width="width"
                    >
                    <template slot-scope="scope">
                      {{ scope.row.drugStuffId.name }}
                      <div v-if="item.type==='recipelType_0'">
                        <span class="nameStyle"
                          >{{ scope.row.drugStuffId.drug.dosis
                          }}{{ scope.row.drugStuffId.drug.dosisUnit.name }} *
                          {{ scope.row.drugStuffId.drug.preparation
                          }}{{ scope.row.drugStuffId.drug.preparationUnit.name }}/{{
                            scope.row.drugStuffId.drug.pack.name
                          }}</span
                        >
                      </div>
                    </template>
                    </el-table-column>
                    <!-- <el-table-column
                      prop="drugStuffId.name"
                      label="Tên"
                      v-else-if="item.type.value === 'recipelType_1'"
                    >

                    </el-table-column> -->
                    <el-table-column
                      prop="drugStuffId.drug.factory.name"
                      label="Nhà sản xuất"
                      width="width"
                    >
                    </el-table-column>
                    <el-table-column
                      prop="westernMedicineUse.name"
                      label="Cách dùng"
                      width="width"
                      v-if="item.type === 'recipelType_0'"
                    >
                    </el-table-column>
                    <el-table-column
                      prop="singleDosage"
                      label="Liều dùng mỗi lần"
                      align="center"
                      v-if="item.type === 'recipelType_0'"
                      width="width"
                    >
                      <template slot-scope="scope"
                        >{{ scope.row.singleDosage
                        }}{{ scope.row.drugStuffId.dosisUnit.name }}</template
                      >
                    </el-table-column>
                    <el-table-column
                      prop="frequency"
                      label="Tần suất"
                      align="center"
                      width="width"
                      v-if="item.type === 'recipelType_0'"
                    >
                      <template slot-scope="scope"
                        >{{ scope.row.infuseUse.name
                        }}{{ scope.row.frequency.name }}</template
                      >
                    </el-table-column>
                    <el-table-column
                      prop="chineseMedicineUse.name"
                      label="Cách sắc thuốc"
                      align="center"
                      width="width"
                      v-if="item.chineseMedicineUse !== undefined&&item.chineseMedicineUse !== ''"
                    >
                    </el-table-column>
                    <el-table-column prop="days.name" label="Số ngày" align="center" v-if="item.type === 'recipelType_0'">
                    </el-table-column>
                    <el-table-column
                      prop="unitPrice"
                      label="Đơn giá"
                      width="width"
                    >
                    <template slot-scope="scope">
                      <span
                        >{{ (scope.row.unitPrice).toFixed(4) }}VNĐ/{{
                          scope.row.isUnpackSell === 1
                            ? scope.row.drugStuffId.preparationUnit.name
                            : scope.row.drugStuffId.pack.name
                        }}</span
                      >
                    </template>
                    </el-table-column>
                    <el-table-column prop="total" label="Tổng số lượng" width="width">
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
                      <el-table-column prop="drug_trac_codg" label="Mã truy xuất thuốc" width="width">
                        <template slot-scope="scope">
<!--                          <el-input-->
<!--                            :disabled="!(ActiveName === 'IsAll' || ActiveName === 'IsWaitingDrug')"-->
<!--                            v-model="scope.row.drugTracCodg"-->
<!--                            style="width:100px"-->
<!--                          />-->
                          <el-button  :type="scope.row.completed ? 'success' : 'warning'"  :disabled="!(ActiveName === 'IsAll' || ActiveName === 'IsWaitingDrug')" @click="openDialog(scope)">Nhập mã truy xuất</el-button>
                        </template>
                      </el-table-column>
                      <el-table-column prop="allFee" width="width" label="Số tiền phải trả">
                        <template slot-scope="scope">
                          {{ (scope.row.allFee).toFixed(2) }}VNĐ
                        </template>
                      </el-table-column>
                      <el-table-column prop="actualPayment" width="width" label="Số tiền thực trả">
                        <template slot-scope="scope">
                          {{ (scope.row.actualPayment) == null ? (scope.row.allFee).toFixed(2) : (scope.row.actualPayment).toFixed(2)}}VNĐ
                          <!--{{ (scope.row.actualPayment).toFixed(2) }}VNĐ-->
                        </template>
                      </el-table-column>
                  </el-table>
                  <!--<div v-if="item.type==='recipelType_1'" style="margin:10px 0">
                    <el-input style="width:50px" disabled v-model="item.content.recipelInfo.dosage">
                    </el-input>
                    thang

                    <el-input style="width:100px" disabled v-model="item.content.recipelInfo.frequency.name">
                    </el-input>
                    Mỗi lần uống
                    <el-input style="width:80px" disabled v-model="item.content.recipelInfo.dosage">
                    </el-input>{{isSpecial?'gói':'ml'}}
                  </div>-->
                  <el-row v-if="item.type==='recipelType_1'">
                    <el-input disabled v-model="item.content.recipelInfo.dosage" oninput="value=value.replace(/[^\d.]/g,'')" style="width: 60px"></el-input>
                    &nbsp;thang &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Cách dùng: 
                    <el-select disabled v-model="item.content.recipelInfo.recipelUse" placeholder="Vui lòng chọn" style="width: 110px">
                      <el-option v-for="pitem in ChineseUseOption" :key="pitem.value"
                                 :label="pitem.name"
                                 :value="{ name: pitem.name, value: pitem.value }">
                      </el-option>
                    </el-select>
                    <el-select disabled v-model="item.content.recipelInfo.frequency" placeholder="Vui lòng chọn" style="width: 110px">
                      <el-option v-for="pitem in ChineseTimeOption" :key="pitem.value"
                                 :label="pitem.name"
                                 :value="{ name: pitem.name, value: pitem.value }">
                      </el-option>
                    </el-select>
                    <el-select disabled v-model="item.content.recipelInfo.takeFrequency" placeholder="Vui lòng chọn" style="width: 110px">
                      <el-option v-for="pitem in JSON.parse(
                          JSON.stringify(FrequencyOption)
                        )" :key="pitem.value" :label="pitem.name" :value="{
                          name: pitem.name,
                          value: pitem.value,
                        }">
                      </el-option>
                    </el-select>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; một lần&nbsp;<el-input disabled v-model="item.content.recipelInfo.singleDosage" style="width: 60px"></el-input>&nbsp;
                    <span>ml</span>
                  </el-row>
                  </div>
                    <div v-else style="margin-left:-10px;">
                      <div v-for="(items,index) in item.infusion.zushu" :key="index" style="border:1px solid #DCDCDC;padding:10px;margin:10px;">

                        <div>
                      <span  style="float:left;height:25px;line-height:25px;font-size:14px;">&nbsp;&nbsp;thứ{{index+1}}nhóm</span>
                      <span  style="float:right;height:25px;line-height:25px;font-size:14px;">&nbsp;&nbsp;giọt/Phút</span>

                             <el-input
                              style="float:right;width:10%"
                              :value="item.infusion.drippingSpeed[index]"
                              placeholder=""
                              :disabled="
                               true
                              "
                            >

                            </el-input>

                            <span  style="float:right;height:25px;line-height:25px;font-size:14px;">&nbsp;&nbsp;ngày&nbsp;&nbsp;</span>
                             <el-input
                              style="float:right;width:10%"
                              :value="item.infusion.days[index].name"
                              placeholder=""
                              :disabled="
                               true
                              "
                            >

                            </el-input>
                              <!-- <el-select
                               style="float:right;width:10%"
                              v-model="item.infusion.days[index]"
                              :disabled="
                               true
                              "

                              placeholder="Vui lòng chọn số ngày"
                            >
                              <el-option
                                v-for="item in DayNumOption"
                                :key="item.value"
                                :label="item.name"
                                :value="{ name: item.name, value: item.value }"
                              >
                              </el-option>
                            </el-select> -->

                            <span  style="float:right;height:25px;line-height:25px;font-size:18px;">&nbsp;&nbsp;</span>
                                <el-input
                              style="float:right;width:10%"
                              :value="item.infusion.frequency[index].name"
                              placeholder=""
                              :disabled="
                               true
                              "
                            >

                            </el-input>
                              <!-- <el-select
                               style="float:right;width:10%;"
                              v-model="item.infusion.frequency[index]"
                              :disabled="
                               true
                              "

                              placeholder="Vui lòng chọn tần suất"
                            >
                              <el-option
                                v-for="item in FrequencyOption"
                                :key="item.value"
                                :label="item.name"
                                :value="{ name: item.name, value: item.value }"
                              >
                              </el-option>
                            </el-select> -->

                            <span  style="float:right;height:25px;line-height:25px;font-size:18px;">&nbsp;&nbsp;</span>
                            <el-input
                              style="float:right;width:10%"
                              :value="item.infusion.infuseUse[index].name"
                              placeholder=""
                              :disabled="
                               true
                              "
                            >

                            </el-input>
                          <!-- <el-select
                              style="float:right;width:10%"
                              v-model="item.infusion.infuseUse[index]"
                              :disabled="
                                true
                              "
                              placeholder="Vui lòng chọn cách dùng"
                            >
                              <el-option
                                v-for="item in infuseUseOption"
                                :key="item.value"
                                :label="item.name"
                                :value="{ name: item.name, value: item.value }"
                              >
                              </el-option>
                            </el-select> -->
                        </div>
                        <div style="border:17px solid #0000"></div>
                    <el-table :data="item.infusion.infusionProject[index]" style="width: 100%">
                    <!-- <el-table-column label="Số nhóm" prop="infuseGroup" align="center">
                    </el-table-column> -->
                    <el-table-column label="Số thứ tự" type="index" align="center" width="width">
                    </el-table-column>
                    <el-table-column
                      prop="drugStuffId.name"
                      label="Tên"
                      width="width"
                    >
                    <template slot-scope="scope">
                       <!-- v-if="item.type.value === 'recipelType_2'" -->
                      {{ scope.row.drugStuffId.name }}
                      <div v-if="scope.row.drugStuffId.drug != null">
                        <span class="nameStyle"
                          >{{ scope.row.drugStuffId.drug.dosis
                          }}{{ scope.row.drugStuffId.drug.dosisUnit.name }} *
                          {{ scope.row.drugStuffId.drug.preparation
                          }}{{ scope.row.drugStuffId.drug.preparationUnit.name }}/{{
                            scope.row.drugStuffId.drug.pack.name
                          }}</span
                        >
                      </div>
                    </template>
                    </el-table-column>
                    <el-table-column
                      prop="skinTest.name"
                      label="Test da"
                      width="width"
                    >
                    </el-table-column>
                    <el-table-column
                      prop="drugStuffId.drug.factory.name"
                      label="Nhà sản xuất"
                      width="width"
                    >
                    </el-table-column>


                     <el-table-column
                      prop="singleDosage"
                      label="Liều dùng mỗi lần"
                      align="center"
                      width="width"
                      v-if="item.type === 'recipelType_2'"
                    >
                      <template slot-scope="scope"
                        >{{ scope.row.singleDosage
                        }}{{ scope.row.drugStuffId.preparationUnit.name }}</template
                      >
                    </el-table-column>
                    <el-table-column
                      prop="unitPrice"
                      label="Đơn giá"
                      width="width"
                    >
                    <template slot-scope="scope">
                      <span
                        >{{ (scope.row.unitPrice).toFixed(4) }}VNĐ/{{
                          scope.row.isUnpackSell === 1
                            ? scope.row.drugStuffId.preparationUnit.name
                            : scope.row.drugStuffId.pack.name
                        }}</span
                      >
                    </template>
                    </el-table-column>

                    <el-table-column prop="total" label="Tổng số lượng" width="width">
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
                      <el-table-column prop="allFee" width="width" label="Số tiền phải trả">
                        <template slot-scope="scope">
                          {{ (scope.row.allFee).toFixed(2) }}VNĐ
                        </template>
                      </el-table-column>
                      <el-table-column prop="allFee" width="width" label="Số tiền thực trả">
                        <template slot-scope="scope">
                          {{ (scope.row.actualPayment).toFixed(2) }}VNĐ
                        </template>
                      </el-table-column>

                  </el-table>
                  </div>
                  </div>

                  <el-divider style="margin-top: 5px" content-position="left"
                    >Phí phụ thêm</el-divider
                  >
                  <el-table :data="item.content.isExtra" style="width: 100%">
                    <el-table-column label="Số thứ tự" type="index" align="center" width="width">
                    </el-table-column>
                    <el-table-column
                      prop="drugStuffId.name"
                      label="Tên"
                      width="width"
                    >
                    </el-table-column>
                    <el-table-column
                      prop="drugStuffId.drug.factory.name"
                      label="Nhà sản xuất"
                      width="width"
                    >
                    </el-table-column>
                    <el-table-column
                      prop="unitPrice"
                      label="Đơn giá"
                      width="width"
                    >
                    <template slot-scope="scope">
                      <span
                        >{{ (scope.row.unitPrice).toFixed(4) }}VNĐ/{{
                          scope.row.isUnpackSell === 1
                            ? scope.row.drugStuffId.preparationUnit.name
                            : scope.row.drugStuffId.pack.name
                        }}</span
                      >
                    </template>
                    </el-table-column>
                    <el-table-column prop="total" label="Tổng số lượng" width="width" >
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
                    <el-table-column prop="allFee" width="width" label="Số tiền phải trả">
                      <template slot-scope="scope">
                        {{ (scope.row.allFee).toFixed(2) }}VNĐ
                      </template>
                    </el-table-column>
                    <el-table-column prop="allFee" width="width" label="Số tiền thực trả">
                      <template slot-scope="scope">
                        {{ (scope.row.actualPayment).toFixed(2) }}VNĐ
                      </template>
                    </el-table-column>
                  </el-table>
                  <el-row>
                    <el-divider content-position="left">Hạng mục y lệnh</el-divider>
                    <el-input
                      type="textarea"
                      :rows="4"
                      v-model="item.content.recipelInfo.entrust"
                      disabled
                    >
                    </el-input>
                  </el-row>
                </el-tab-pane>
              </el-tabs>
            </div>
          </el-card>
        </el-scrollbar>
      </el-card>
      <el-card v-if="allType !== 0">
        <span>Tổng cộng{{ allType }} loại, {{ totalMoney.toFixed(2) }}VNĐ</span>
      </el-card>
    </el-main>
    <!--  Hộp thoại mã truy xuất thuốc-->
    <el-dialog  @keydown.native="handleKeydown" :modal="false"  :visible.sync="drugDialogVisible" title="Nhập mã truy xuất thuốc" @open="focusInput" @close="resetInput">
      <el-input
        v-model="currentInput"
        placeholder="Vui lòng nhập mã truy xuất thuốc"
        ref="inputFocus"
      ></el-input>
      <span slot="footer" class="dialog-footer">
        <el-button @click="submitInput" type="primary">Xác nhận</el-button>
        <el-button @click="drugDialogVisible = false">Hủy</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>

<script>
import {
  allQueryMedicalRecord,
  listMedicalRecordPage,
} from "@/api/outpatient/medicalRecord";
import { listRegistrationPages } from "@/api/outpatient/registration";
import { getPatientById, listPatientAll } from "@/api/outpatient/patient";
import {listDictItemAll} from "@/api/sys/dictItem";
import { updateStockStockList } from "@/api/stock/supplierStock";
import SupplierStockForm from "./supplierStockForm";
import MainUI from "@/views/components/mainUI";
import History from "@/views/components/history";
import {getMember,getByPatientId} from "@/api/member/memberManagement"
import { getCurrentUser, getCurrentUserId, getCurrentCompanyId, getCurrentCompany } from "@/utils/userCache";
export default {
  extends: MainUI,
  components: {
    SupplierStockForm,
    History,
  },
  data() {
    return {
      dispensingPageSize: 15, //Số lượng mỗi trang
      //tab hiện tại là chờ phát thuốc
      ActiveName: "IsWaitingDrug",
      Offset: 0,
      SearchPatientId: "",
      SearchTime: [],
      //Tất cả bệnh nhân
      AllPatientOption: [],
      WaitingTotal: 0,
      OtherTotal: 0,
      returnTotal: 0,
      //Dữ liệu bảng chờ phát thuốc
      WaitingTable: [],
      //Dữ liệu bảng tất cả phát thuốc
      dispensingTable: [],
      //Bảng đã trả thuốc
      returnTable:[],
      //Thông tin lượt khám hiện tại
      NowVisitInfo: {},
      //Thông tin bệnh nhân hiện tại
      VisitId: "",
      NowPatientInfo: {
        address: "",
        age: "",
        area: "",
        birthday: "",
        card: "",
        city: "",
        company: "",
        createBy: "",
        createDate: "",
        healthCardNo: "",
        id: "",
        month: "",
        name: "",
        phone: "",
        province: "",
        remarks: "",
        updateBy: "",
        updateDate: "",
        withPatientNexus: "",
        gender: "",
        westernDiagnose: "",
        chinaDiagnose: "",
        dispensingStatus: "",
      },
      //Tất cả đơn thuốc
      prescriptionALllList: [],
      //Dữ liệu bảng được chọn bằng checkbox
      CheckMedicineList: [],
      BASE_API: process.env.BASE_API,
      buttonState: false,
      //Tổng số tiền
      totalMoney: 0,
      //Tổng số
      allType: 0,
      //Lớp thực thể tham số mới
      PageRegistration: {
        columnName: "",
        limit: 0,
        offset: 0,
        order: "desc",
        companyId: "",
        recipeStatus: "0",
        updateDate: "2022-06-01 00:00:00",
        status: "registrationStatus_1",
        chargeStatus: 1,
        dispensionStatus: 0,
        recipelType:'recipelType_3',
        patientName:'',
        patientCode:''
      },
      allCurrentPage: 1, //Trang đã phát thuốc
      dispensingCurrentPage: 1, //Trang chưa phát thuốc
      returnCurrentPage: 1, //Trang đã trả thuốc
      prescriptionMainList: [],//Danh sách đơn thuốc
      patientInfoRow:{},
      patientInfoForm: {
        name: "",
        gender: {
          name: "",
          value: "",
        },
        age: "",
        month: "",
        phone: "",
        card: "",
        westernDiagnose: "Không có",
        chinaDiagnose: "Không có",
      },
      medicalEditTabs: [],
      drugTracCodg:'',//Mã truy xuất
      medicalEditTabsValue: {},
      medicalClickTabsValue: {},
      //Phòng khám đặc biệt
      isSpecial: false,
      selectCondition:"",
      defaultNumber:1,//Số nhóm mặc định
      infusionProject:[[]],
      drippingSpeed:[""],  //Tốc độ truyền
      days:[{}],    //Số ngày
      frequency:[{}],   //Tần suất
      infuseUse:[{}],     //Cách dùng
      zushu:[1],
      overColor:0,
      allMember:[],  //Tất cả thẻ thành viên
      //Danh sách thả xuống cách dùng thuốc Đông y
      ChineseUseOption: [],
      ChineseTimeOption: [],
      ChineseUseTimeOption: [],
      //Danh sách thả xuống tần suất thuốc Tây y
      FrequencyOption: [],
      //Điều khiển hộp thoại mã truy xuất
      drugDialogVisible: false,
      currentInput: '',
      inputCount: 0, // Số lần nhập hiện tại
      maxInputCount: 0, // Số lần nhập tối đa
      currentRow: null, // Dòng đang thao tác
      allInputs: [], // Tất cả mã truy xuất đã nhập
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
    AllFee() {
      let WesternFee = this.WesternCheck ? this.WesternFee : 0;
      let ChineseFee = this.ChineseCheck ? this.ChineseFee : 0;
      let InfusionFee = this.InfusionCheck ? this.InfusionFee : 0;
      let SurchargeFee = this.SurchargeCheck ? this.SurchargeFee : 0;
      let allFee = WesternFee + ChineseFee + InfusionFee + SurchargeFee;
      return allFee;
    },
    AllTotal() {
      let WesternTotal = this.WesternCheck ? this.WesternTable.length : 0;
      let ChineseTotal = this.ChineseCheck ? this.ChineseTable.length : 0;
      let InfusionTotal = this.InfusionCheck ? this.InfusionTable.length : 0;
      let SurchargeTotal = this.SurchargeCheck ? this.SurchargeTable.length : 0;
      let AllTotal =
        WesternTotal + ChineseTotal + InfusionTotal + SurchargeTotal;

      return AllTotal;
    },
  },
  watch: {
    ActiveName() {
      this.SearchPatientId = "";
      this.allCurrentPage = 1
      this.medicalEditTabsValue = {}
      this.medicalEditTabs = []
      this.patientInfoForm = {
        name: "",
        gender: {
          name: "",
          value: "",
        },
        age: "",
        month: "",
        phone: "",
        card: "",
        westernDiagnose: "Không có",
        chinaDiagnose: "Không có",
      },
      this.buttonState = false
      this.dispensingCurrentPage = 1
      this.GetDispensingTable();
      this.overColor=0
    },
  },
  mounted() {
    this.Init();
    let specialCompany = getCurrentCompany();
    if (specialCompany && specialCompany.id === "1088657523871555640") {
      this.isSpecial = true;
    }

  },
  methods: {
    handleKeydown(event) {
      if (event.key === 'Enter' ||event.key === 'enter'  ) {
        // Xác nhận thao tác bằng phím Enter
        this.submitInput();
      }
    },
    focusInput() {
      // Sử dụng ref để truy cập ô nhập và gọi focus() Phương pháp
      this.$nextTick(() => {
        this.$refs.inputFocus.focus(); // Tự động focus vào ô nhập
      });
    },
    openDialog(scope) {
      this.currentRow = scope.row; // Ghi lại dòng hiện tại

      this.inputCount = 0; // Đặt lại bộ đếm nhập liệu
      this.allInputs = []; // Xóa dữ liệu nhập trước đó
      this.drugDialogVisible = true;
      this.maxInputCount = Math.floor(
        scope.row.total /
        (scope.row.drugStuffId.drug.preparation - 0)
      )
    },
    submitInput() {
      if (this.currentInput) {
        this.allInputs.push(this.currentInput); // Thêm dữ liệu nhập hiện tại
        this.currentRow.drugTracCodg = this.allInputs.join(','); // Cập nhật mã truy xuất thuốc của dòng hiện tại
        this.currentInput = ''; // Xóa ô nhập
        this.inputCount++; // Tăng bộ đếm nhập
        // Kiểm tra đã đạt số lần nhập tối đa chưa
        // this.drugDialogVisible = this.inputCount < this.maxInputCount;
        if (this.inputCount >= this.maxInputCount) {
          this.$set(this.currentRow, 'completed', true); // Đặt thành trạng thái hoàn thành
          this.drugDialogVisible = false; // Đóng hộp thoại
        }
      }
    },
    resetInput() {
      this.currentInput = ''; // Xóa ô nhập
      this.inputCount = 0; // Đặt lại bộ đếm nhập liệu
      this.currentRow = null; // Xóa bản ghi dòng hiện tại
    },




    selectPatientChange() {
      if (!this.patientInfoRow) {
        return;
      }
      this.buttonState = true
      this.$nextTick(() => {
        this.$refs["patientInfoForm"].clearValidate();
      });
      if (this.ActiveName === "1") this.buttonState = true;
      this.prescriptionMainList = [];
      this.getSaveInfo(this.patientInfoRow.id);
      this.overColor=0

      //Theo bệnh nhânidLấy thông tin thành viên
        getByPatientId(this.patientInfoRow.patientId.id).then(res=>{
          if(res.code === 100){
            this.allMember=res.data
          }else{
            this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên!")
          }
        }).catch()
    },
    //Theo người khámidTruy vấn bệnh án đơn thuốc
    getSaveInfo(visitId) {
      allQueryMedicalRecord(visitId).then((responseData) => {
        if (responseData.code === 100) {
            this.prescriptionMainList = responseData.data.recipelInfoEvtList.filter(item=>item.recipelInfo.recipelType.value!=='recipelType_3')
            if(this.ActiveName==='IsWaitingDrug'){
              this.prescriptionMainList =  this.prescriptionMainList.filter(item=>item.recipelInfo.dispensionStatus===0&&item.recipelInfo.chargeStatus===1)
            }else if(this.ActiveName==='IsAll'){
              this.prescriptionMainList =  this.prescriptionMainList.filter(item=>item.recipelInfo.dispensionStatus===1)
            }else{
              this.prescriptionMainList =  this.prescriptionMainList.filter(item=>item.recipelInfo.dispensionStatus===-1)
            }

            this.prescriptionMainList.forEach((item) => {
            item.isExtra = [];
            item.notExtra = [];
            item.recipelDetailEvtList.forEach((items) => {
              this.$set(items, 'drugTracCodg', ''); // Trường mã truy xuất thuốc
              if (items.isExtra === 1) {
                item.isExtra.push(items);
              } else {
                item.notExtra.push(items);
              }
            });
          });
          this.patientInfoForm = responseData.data.patient;
          this.patientInfoForm.westernDiagnose =
            responseData.data.medicalRecord.westernDiagnose;
          this.patientInfoForm.chinaDiagnose =
            responseData.data.medicalRecord.chinaDiagnose;
          this.patientInfoForm.pulse = responseData.data.registration.pulse;
          this.patientInfoForm.breathe = responseData.data.registration.breathe;
          this.patientInfoForm.temperature =
            responseData.data.registration.temperature;
          this.patientInfoForm.bloodPressure =
            responseData.data.registration.bloodPressure;
          this.VisitId = visitId;
          this.medicalRecord = responseData.data.medicalRecord;
          this.totalMoney = 0;
          let money = 0;
          this.prescriptionMainList.forEach((item) => {
            money += item.recipelInfo.fee;
          });
          this.totalMoney = money.toFixed(2);
          this.createMedicalEditTab(this.prescriptionMainList);
        }
      });
    },
    createMedicalEditTab(recipelInfoEvtList) {
      this.medicalEditTabs = [];
      if (recipelInfoEvtList === undefined || recipelInfoEvtList == null) {
        return;
      }

      recipelInfoEvtList.forEach((element) => {
        element.uuid = element.recipelInfo.id;
        if(element.recipelInfo.recipelType.value==="recipelType_2"){
           let infusion={
            defaultNumber:1,//Số nhóm mặc định
            infusionProject:[[]],
            drippingSpeed:[""],  //Tốc độ truyền
            days:[{}],    //Số ngày
            frequency:[{}],   //Tần suất
            infuseUse:[{}],     //Cách dùng
            zushu:[1],
         }
         let count=0
         for (let i = 0; i < element.recipelDetailEvtList.length; i++) {
              if(count<element.recipelDetailEvtList[i].infuseGroup){
                count=element.recipelDetailEvtList[i].infuseGroup
              }
         }

      for (let i = 1; i < count; i++) {
        infusion.defaultNumber=infusion.defaultNumber+1;
        infusion.zushu.push(infusion.defaultNumber)
        infusion.infusionProject.push([])
        infusion.drippingSpeed.push("")
        infusion.days.push({})
        infusion.frequency.push({})
        infusion.infuseUse.push({})
      }

          let arr=[]
       for (let i = 0; i < element.recipelDetailEvtList.length; i++) {


             if(element.recipelDetailEvtList[i].isExtra!==1){

             infusion.infusionProject[element.recipelDetailEvtList[i].infuseGroup-1].push(element.recipelDetailEvtList[i])
             infusion.drippingSpeed[element.recipelDetailEvtList[i].infuseGroup-1]=element.recipelDetailEvtList[i].drippingSpeed
             infusion.days[element.recipelDetailEvtList[i].infuseGroup-1]=element.recipelDetailEvtList[i].days
             infusion.frequency[element.recipelDetailEvtList[i].infuseGroup-1]=element.recipelDetailEvtList[i].frequency
             infusion.infuseUse[element.recipelDetailEvtList[i].infuseGroup-1]=element.recipelDetailEvtList[i].infuseUse
             }else{


               arr.push(element.recipelDetailEvtList[i])
             }

         }
        let tab = {
          title: element.recipelInfo.name,
          key: element.recipelInfo.seq,
          content: element,
          closable: false,
          type: element.recipelInfo.recipelType.value,
          infusion:infusion
        };
        this.medicalEditTabs.push(tab);
       }else{
          let tab = {
          title: element.recipelInfo.name,
          key: element.recipelInfo.seq,
          content: element,
          closable: false,
          type: element.recipelInfo.recipelType.value,
        };
        this.medicalEditTabs.push(tab);
       }
      });
      this.medicalEditTabsValue = this.medicalEditTabs[0];
      this.medicalClickTabsValue = this.medicalEditTabsValue;
    },
    AlreadyPatientDescriptionsQuery(item,index) {
      this.medicalEditTabs.forEach((element) => {
        if (element.content.uuid === item.uuid) {
          this.medicalEditTabsValue = element;
          this.medicalClickTabsValue = this.medicalEditTabsValue;
        }
      });
      this.recipelInfoId = item.id;
      this.overColor=index
    },
    print() {
      if (!this.medicalEditTabsValue.content) {
        this.$message.error("Chưa chọn dữ liệu");
        return;
      }
        let params = {
          recipelInfoId: this.medicalEditTabsValue.content.recipelInfo.id,
          type: this.medicalEditTabsValue.content.recipelInfo.recipelType.value,
        };
        let str = "";
        let type =
          params.type === "recipelType_0"
            // ? "westMedicine"
            ? "chronicDisease"
            : params.type === "recipelType_1"
            ? "chineseMedicine"
            : params.type === "recipelType_2"
            ? "infuse"
            : "costItem";
        if (params.recipelInfoId) {
          str = "&recipelInfoId=" + params.recipelInfoId;
        }
        let page = window.open(
          this.BASE_API +
            "/ureport/preview?_u=Newtouch:" +
            type +
            ".ureport.xml&_t=0" +
            str +
            "&type=0"
        );
        page.onload=()=>{
          page.document.title = "Hệ thống Quản lý Phòng khám"
        }
      //    let arr = []
      // this.prescriptionMainList.forEach((item, index) => {
      //   let params = {
      //     recipelInfoId: item.recipelInfo.id,
      //     type: item.recipelInfo.recipelType.value,
      //   };
      //   let str = "";
      //   if (params.recipelInfoId) {
      //     str = "&recipelInfoId=" + params.recipelInfoId;
      //   }
      //   arr[index] = window.open(
      //     this.BASE_API +
      //       "/ureport/preview?_u=Newtouch:charge.ureport.xml&_t=1,6,7,8,9" +
      //       str,
      //     index
      //   );
      //   arr[index].onload=()=>{
      //     arr[index].document.title = "Phòng khám Tân Trí Vân"
      //   }
      // });
    },
    Init() {
      this.GetAllPatient();
      this.GetDispensingTable();
      this.getAllPatientList();
      this.getReturnPatientList();
      this.initOptions()
    },
    getAllPatientList() {
      this.PageRegistration.limit = this.dispensingPageSize;
      this.PageRegistration.companyId = this.Company.id;
      this.PageRegistration.offset = (this.allCurrentPage - 1) * this.dispensingPageSize;
      this.PageRegistration.dispensionStatus = 1;
      this.PageRegistration.columnName = "dispensing_date";
      this.PageRegistration.chargeStatus = 1;
      this.PageRegistration.patientName = '';
      this.PageRegistration.patientCode = '';
      listRegistrationPages(this.PageRegistration).then((responseData) => {
        if (responseData.code === 100) {
          this.OtherTotal = responseData.data.total;
        }
      });
    },
    getReturnPatientList() {
      this.PageRegistration.limit = this.dispensingPageSize;
      this.PageRegistration.companyId = this.Company.id;
      this.PageRegistration.offset = (this.returnCurrentPage - 1) * this.dispensingPageSize;
      this.PageRegistration.dispensionStatus = -1;
      this.PageRegistration.columnName = "return_date";
      this.PageRegistration.chargeStatus = 3;
      this.PageRegistration.patientName = '';
      this.PageRegistration.patientCode = '';
      listRegistrationPages(this.PageRegistration).then((responseData) => {
        if (responseData.code === 100) {
          this.returnTotal = responseData.data.total;
        }
      });
    },
    initOptions(){
      this.getOption("1014474470772899981")
      this.getOption("1014474470772899985")
      this.getOption("1014474470772900058")
      this.getOption("1014474470772899990")
    },
    getOption(optionId){
      let model = {
        params: [{
          columnName: "dict_type_id",
          queryType: "=",
          value: optionId,
        },],
      };
      listDictItemAll(model).then((responseData) => {
        if (optionId === "1014474470772899981") {
          this.ChineseUseOption = responseData.data;
          if (this.isSpecial) {
            this.ChineseUseOption = this.ChineseUseOption.filter(
              (item) => item.name === "水冲"
            );
          }
        }else if (optionId === "1014474470772899985")
          this.ChineseTimeOption = responseData.data;
        else if (optionId === "1014474470772899990")
          this.FrequencyOption = responseData.data;
        else if (optionId === "1014474470772900058")
          this.ChineseUseTimeOption = responseData.data;
      });
    },
    GetSearchTime() {
      this.GetDispensingTable();
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
    DispensingPage(pageNum) {
      this.dispensingCurrentPage = pageNum;
      this.GetDispensingTable();
    },
    allPage(pageNum) {
      this.allCurrentPage = pageNum;
      this.GetDispensingTable();
    },
    returnPage(pageNum) {
      this.returnCurrentPage = pageNum;
      this.GetDispensingTable();
    },
    clickMedicalEditTab(tab, event)
    {
       if (this.medicalEditTabsValue === this.medicalClickTabsValue) {
        return;
      }

      this.medicalClickTabsValue = this.medicalEditTabsValue;
      let num=0
      for (let i = 0; i < this.medicalEditTabs.length; i++) {
        if(this.medicalEditTabs[i].key===this.medicalEditTabsValue.key){
          num=i
        }
      }
       this.AlreadyPatientDescriptionsQuery(this.medicalClickTabsValue.content,num)
    },
    GetDispensingTable() {
      this.PageRegistration.limit = this.dispensingPageSize;
      this.PageRegistration.companyId = this.Company.id;
      if(this.ActiveName==='IsWaitingDrug'){
        this.PageRegistration.offset =
        (this.dispensingCurrentPage - 1) * this.dispensingPageSize;
        this.PageRegistration.dispensionStatus = 0;
        this.PageRegistration.columnName="charge_date"
        this.PageRegistration.chargeStatus=1
        if (this.selectCondition.trim()) {
          this.PageRegistration.patientName=this.selectCondition
          this.PageRegistration.patientCode=this.selectCondition
        }else{
          this.PageRegistration.patientName=''
          this.PageRegistration.patientCode=''
        }
        this.allMember=[]
      }else if(this.ActiveName==='IsAll'){
        this.PageRegistration.offset =
        (this.allCurrentPage - 1) * this.dispensingPageSize;
        this.PageRegistration.dispensionStatus = 1;
        this.PageRegistration.columnName="dispensing_date"
        this.PageRegistration.chargeStatus=1
        if (this.selectCondition.trim()) {
          this.PageRegistration.patientName=this.selectCondition
          this.PageRegistration.patientCode=this.selectCondition
        }else{
          this.PageRegistration.patientName=''
          this.PageRegistration.patientCode=''
        }
        this.allMember=[]
      }else{
        this.PageRegistration.offset =
        (this.returnCurrentPage - 1) * this.dispensingPageSize;
        this.PageRegistration.dispensionStatus = -1;
        this.PageRegistration.columnName="return_date"
        this.PageRegistration.chargeStatus=3
        if (this.selectCondition.trim()) {
          this.PageRegistration.patientName=this.selectCondition
          this.PageRegistration.patientCode=this.selectCondition
        }else{
          this.PageRegistration.patientName=''
          this.PageRegistration.patientCode=''
        }
        this.allMember=[]
      }
      listRegistrationPages(this.PageRegistration).then((responseData) => {
        if (responseData.code === 100) {
          if (this.ActiveName==='IsWaitingDrug') {
            this.WaitingTable = responseData.data.rows
            this.WaitingTotal = responseData.data.total;
          } else if(this.ActiveName==='IsAll'){
            this.dispensingTable = responseData.data.rows;
            this.OtherTotal = responseData.data.total;
          }else{
            this.returnTable = responseData.data.rows;
            this.returnTotal = responseData.data.total;
          }
        }
      });
    },

    indexMethod(index) {
      return ((this.Offset + 10) / 10 - 1) * 10 + index + 1;
    },
    indexMethod2(index) {
      return ((this.Offset + 10) / 10 - 1) * 10 + index + 1;
    },
    //Phát thuốc / trả thuốc riêng lẻ
    singleMedicineClick(type){
      let prescriptionIdList = [];
      this.CheckMedicineList = [];
      prescriptionIdList.push(this.medicalClickTabsValue.content.recipelInfo.id);
      this.medicalClickTabsValue.content.recipelDetailEvtList.forEach((items) => {
          this.CheckMedicineList.push({
            recipelInfoId: items.recipelInfo.id,
            id: items.drugStuffId.drugStuffId,
            type: items.stuffType==='4'?1:0,
            number: items.total,
            drugTracCodg:items.drugTracCodg

          });
        });
      let model = {
        registrationId: this.VisitId,
        recipelInfoIdList: prescriptionIdList,
        dispensingStatus:2,
        dispensingType: type,
        dispensingDetailEvtList: this.CheckMedicineList,
      };
      updateStockStockList(model)
        .then((responseData) => {
          if (responseData.code === 100) {
            this.GetDispensingTable();
            this.buttonState = false;
            this.$message.success("Thao tác thành công!");
          } else {
            this.showMessage(responseData);
          }
        })
        .catch((error) => {
          this.outputError(error);
        });
    },
    //Bấm nút phát thuốc 0-Phát thuốc/1-Trả thuốc
    DispensingMedicineClick(type) {
      let prescriptionIdList = [];
      this.CheckMedicineList = [];
      this.medicalEditTabs.forEach((item) => {
        prescriptionIdList.push(item.content.recipelInfo.id);
        item.content.recipelDetailEvtList.forEach((items) => {
          this.CheckMedicineList.push({
            recipelInfoId: items.recipelInfo.id,
            id: items.drugStuffId.drugStuffId,
            type: items.stuffType==='4'?1:0,
            number: items.total,
            drugTracCodg:items.drugTracCodg
          });
        });
      });
      let model = {
        registrationId: this.VisitId,
        recipelInfoIdList: prescriptionIdList,
        dispensingStatus:2,
        dispensingType: type,
        dispensingDetailEvtList: this.CheckMedicineList,
      };
      // return
      updateStockStockList(model)
        .then((responseData) => {
          if (responseData.code === 100) {
            this.GetDispensingTable();
            this.buttonState = false;
            this.$message.success("Thao tác thành công!");
          }else if(responseData.code === 50001){
            this.$message.warning(responseData.msg)
          }
          else {
            this.showMessage(responseData);
          }
        })
        .catch((error) => {
          this.outputError(error);
        });
    },
  },
};
</script>
<style lang="scss" scoped>
$borderColor: rgb(219, 219, 219); //Gạch chân viền xám
.el-row {
  padding: 0;
}
</style>
<style scoped>
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
  font-size: 18px;
  font-weight: bold;
  color: midnightblue;
  line-height: 28px;
}

::v-deep .chinese-medicine-card > .el-card__header {
  padding: 0px 20px;
}

::v-deep .el-divider--horizontal {
  margin: 10px 0 10px 0px;
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

.aside-header {
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

.already-medocal {
  border-bottom: 1px solid #dcdfe6;
  margin-right: 15px;
}
</style>
<style>
.el-popover.medical-type-popover {
  min-width: 85px !important ;
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

.flex-space-between {
  display: flex;
  justify-content: space-between;
}

.flex-start {
  display: flex;
  justify-content: flex-start;
}

.el-divider--horizontal {
  margin: 5px 0;
}

.goods-back {
  position: absolute;
  top: 60px;
  right: 200px;
  width: 60px;
  height: 25px;
  text-align: center;
  vertical-align: center;
  color: red;
  border: 1px solid red;
  border-radius: 45%;
  cursor: none;
  -webkit-transform: rotate(-45deg);
  float: right;
  z-index: 1;
}
.single{
  padding: 8px 0 0;
    font-size: 14px;
    font-weight: 700;
    color: midnightblue;
    line-height: 28px;
}
.clickTest{
  background-color: rgb(234, 242, 251) !important;
  color: #fff;
}
.imgStyle{

  /* display: inline-flex; */
  position:absolute;
  top: 0;
  right: -30px;
}
.nameStyle{
  position:relative;
  display: inline-block;
  color: forestgreen;
}
</style>
