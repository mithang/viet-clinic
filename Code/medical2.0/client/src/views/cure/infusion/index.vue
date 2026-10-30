<template>
  <el-container>
    <el-aside width="400px">
      <el-card style="height: 100%">
        <!-- <el-row style="text-align:right">
          <el-button
            style="width: 100px"
            @click="changeToRetail"
            >Thu phí bán lẻ</el-button
          >
        </el-row> -->
        <el-row style="display: flex; justify-content: flex-start">
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
          <el-tabs stretch v-model="chargeStatusActiveName">
            <el-tab-pane name="1">
              <span slot="label">
                Chờ hoàn thành
                <el-badge
                  :value="patientTotalCount"
                  :max="99"
                  type="primary"
                  style="line-height: normal"
                ></el-badge>
              </span>
              <el-scrollbar view-style="height:calc(100vh - 267px)">
                <el-collapse
                  v-model="patientInfoRow"
                  accordion
                  @change="selectPatientChange()"
                >
                  <div v-for="(item, index) in patientList" :key="item.id">
                    <el-collapse-item class="already-medocal" :name="item">
                      <template slot="title">
                        <span
                          style="
                            font-weight: bold;
                            font-size: 12px;
                            color: #606266;
                            flex: 1;
                          "
                          >{{ patientQueryCondition.offset + index + 1 }}. {{ item.patientId.name }}</span
                        >
                        <span style="font-size: 12px; color: #606266">{{
                          item.receptionEndDate
                        }}</span>
                      </template>
                      <div
                        v-for="(o,index) in prescriptionMainList"
                        :key="o.id"
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
                @current-change="handlePatientPage"
                :page-size="patientPageSize"
                layout="prev, pager, next"
                :total="patientTotalCount"
              >
              </el-pagination>
            </el-tab-pane>
            <el-tab-pane  name="2">
              <span slot="label">
                Đã hoàn thành
                <el-badge
                  :value="isPayTotalCount"
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
                  <div v-for="(item, index) in isPayPatientList" :key="item.id">
                    <el-collapse-item class="already-medocal" :name="item">
                      <template slot="title">
                        <span
                          style="
                            font-weight: bold;
                            font-size: 12px;
                            color: #606266;
                            flex: 1;
                          "
                          >{{ patientQueryCondition.offset + index + 1 }}. {{ item.patientId.name }}</span
                        >
                        <span style="font-size: 12px; color: #606266">{{
                          item.chargeDate
                        }}</span>
                      </template>
                      <div
                        v-for="(o,index) in prescriptionMainList"
                        :key="o.id"
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
                            >{{ o.recipelInfo.name }} <span v-if="o.recipelInfo.dispensionStatus===-1" style="font-weight: bold;margin-left: 15px;color:red">Đã trả thuốc</span> </el-descriptions-item
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
                @current-change="handleIsPayPage"
                :page-size="patientPageSize"
                layout="prev, pager, next"
                :total="isPayTotalCount"
              >
              </el-pagination>
            </el-tab-pane>
            <!-- <el-tab-pane label="Đã hoàn phí" name="3">
              <el-scrollbar view-style="height:calc(100vh - 267px);">
                <el-collapse
                  v-model="patientInfoRow"
                  accordion
                  @change="selectPatientChange()"
                >
                  <div
                    @click="AlreadyPatientDescriptionsQuery(item)"
                    v-for="(item, index) in returnPatientList"
                    :key="item.id"
                  >
                    <el-collapse-item class="already-medocal" :name="item">
                      <template slot="title">
                        <span
                          style="
                            font-weight: bold;
                            font-size: 12px;
                            color: #606266;
                            flex: 1;
                          "
                          >{{ index + 1 }}. {{ item.patientId.name }}</span
                        >
                        <span style="font-size: 12px; color: #606266">{{
                          item.retreatsDate
                        }}</span>
                      </template>
                      <div v-for="o in prescriptionMainList" :key="o.id">
                        <el-descriptions
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
                            <span style="position: relative">
                                            <div v-if="o.recipelInfo.status === -1" class="refundBox">Hủy bỏ</div>
                                            <div v-if="o.recipelInfo.status === 1 && o.recipelInfo.chargeStatus === 0" class="refundBox">Chưa thu phí</div>
                                            <div v-if="o.recipelInfo.status === 1 && o.recipelInfo.chargeStatus === 1" class="refundBox">Đã thu phí</div>
                                            <div v-if="o.recipelInfo.status === 1 && o.recipelInfo.chargeStatus === -1" class="refundBox">Đã hoàn phí</div>
                                        </span>
                          </el-descriptions-item>
                        </el-descriptions>
                      </div>
                    </el-collapse-item>
                  </div>
                </el-collapse>
              </el-scrollbar>
              <el-pagination
                small
                background
                @current-change="handleReturnPage"
                :page-size="patientPageSize"
                layout="prev, pager, next"
                :total="returnTotalCount"
              >
              </el-pagination>
            </el-tab-pane>  -->
          </el-tabs>
        </el-row>
      </el-card>
    </el-aside>
    <el-main>
      <el-card class="box-card sub-card" style="height: 50px; display: flex">
        <!-- <div style="font-weight: bold">Thu phí</div> -->
        <div class="flex-space-between">
          <!-- <div class="goods-back" v-if="chargeStatusActiveName == '3'&&!isRetail">
            Đã hoàn phí
          </div>
          <div class="goods-back" v-if="chargeStatusActiveName === '2'&&!isRetail">
            Đã thu phí
          </div> -->
          <div  style="margin-right:10px">
            Thực hiện y lệnh
          </div>
          <!-- <el-button
            type="primary"
            @click="chargeClick(1)"
            v-if="chargeStatusActiveName === '1' && payState && !isRetail"
            >Thu phí</el-button
          >
          <el-popconfirm
            style="margin-right:10px"
            title="Bạn có chắc muốn hoàn phí không？"
            @confirm="chargeClick(2)"
            v-if="chargeStatusActiveName === '2' && selRowChargeStatus !== 4 && medicalEditTabs.length>0"
          >
            <el-button style="height:30px" slot="reference" type="primary" plain>Hoàn phí</el-button>

          </el-popconfirm> -->

          <!-- <el-button
            v-if="chargeStatusActiveName === '2' && medicalEditTabs.length>0"
            type="primary"
            @click="print"
            >In</el-button
          > -->
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
              :rules="isRetail ? patientInfoRules : patientInfoNoRules"
              label-width="auto"
            >
              <el-row :gutter="24">
                <el-col :span="8">
                  <el-form-item label="Tên bệnh nhân" prop="name">
                    <el-input
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.name"
                    ></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="Giới tính" prop="gender">
                    <el-select
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.gender.name"
                      style="width: 100%"
                    >
                      <el-option label="Nam" value="男"></el-option>
                      <el-option label="Nữ" value="女"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="Tuổi" prop="age">
                    <el-input
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.age"
                      style="width: 49%"
                    >
                      <template slot="append">tuổi</template>
                    </el-input>
                    <el-input
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.month"
                      style="width: 49%"
                    >
                      <template slot="append">tháng</template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="6">
                  <el-form-item label="Nhiệt độ cơ thể" prop="temperature">
                    <div class="ipt-box">
                      <el-input
                        :maxlength="45"
                        :disabled="isBaseReadOnly"
                        v-model="patientInfoForm.temperature"
                      ></el-input>
                      <span>&nbsp;℃</span>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="Mạch" prop="pulse">
                    <div class="ipt-box">
                      <el-input
                        :maxlength="45"
                        :disabled="isBaseReadOnly"
                        v-model="patientInfoForm.pulse"
                      ></el-input>
                      <span>&nbsp;lần/min</span>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="Nhịp thở" prop="breathe">
                    <div class="ipt-box">
                      <el-input
                        :maxlength="45"
                        :disabled="isBaseReadOnly"
                        v-model="patientInfoForm.breathe"
                      ></el-input>
                      <span>&nbsp;lần/min</span>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="Huyết áp" prop="bloodPressure">
                    <div class="ipt-box">
                      <el-input
                        :maxlength="45"
                        :disabled="isBaseReadOnly"
                        v-model="patientInfoForm.bloodPressure"
                      ></el-input>
                      <span>&nbsp;mmHg</span>
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="Số điện thoại liên hệ" prop="phone">
                    <el-input
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.phone"
                    ></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Số CMND/CCCD" prop="card">
                    <el-input
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.card"
                    ></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="Chẩn đoán tây y" prop="westernDiagnose">
                    <el-input
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.westernDiagnose"
                    ></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Chẩn đoán đông y" prop="chinaDiagnose">
                    <el-input
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.chinaDiagnose"
                    ></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-card>
          <el-card class="box-card main-card" v-if="medicalEditTabs.length > 0">
            <!-- <div>
              <el-popover
                placement="top-start"
                width="700"
                trigger="click"
                v-if="!IsOnlyRead"
              >
                <el-table
                  :data="DrugAllTable"
                  border
                  highlight-current-row
                  @row-click="RowClickDrugTable"
                >
                  <el-table-column prop="goodsName" label="Tên thuốc">
                  </el-table-column>
                  <el-table-column prop="type.name" label="Loại thuốc" width="100">
                  </el-table-column>
                  <el-table-column prop="gg" label="Quy cách" width="100">
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
                        scope.row.isUnpackSell === "1" ||
                        scope.row.type.value === "medicalType_1"
                          ? scope.row.preparationUnit.name
                          : scope.row.pack.name
                      }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="price" label="Giá bán lẻ" width="80">
                    <template slot-scope="scope">
                      {{
                        scope.row.isUnpackSell === "1" ||
                        scope.row.type.value === "medicalType_1"
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
                  v-model="SearchDrugName"
                  @input="GetDrugTable"
                  placeholder="Nhập tên thuốc hoặc mã pinyin"
                ></el-input>
              </el-popover>
            </div> -->
            <div v-if="!isRetail">
              <el-tabs
                v-model="medicalEditTabsValue"
                type="card"
                @tab-click="clickMedicalEditTab"
              >
                <el-tab-pane
                  v-for="(item, index) in medicalEditTabs"
                  :key="item.key"
                  :label="item.title"
                  :name="item"
                >
                  <span slot="label">
                    <!-- <el-checkbox :disabled="isReadOnly"></el-checkbox>  -->
                    {{ item.title }}
                  </span>
                  <el-row style="padding-top:8px" type="flex" justify="space-between" align="center" v-if="chargeStatusActiveName === '1' && payState">
                    <!-- <div class="single">
                      Chi phí của đơn thuốc này：{{ item.content.recipelInfo.fee }}VNĐ
                    </div>
                    <el-popconfirm
                        title="Bạn có chắc muốn thu phí không？"
                        @confirm="singleCharge(1)"
                        style="padding-top:7px"
                      >
                        <el-button style="height:30px" slot="reference" type="primary" plain>Thu phí đơn thuốc này</el-button>

                      </el-popconfirm> -->
                  </el-row>
                  <el-row type="flex" justify="space-between" align="center" style="padding-top:8px" v-if="chargeStatusActiveName === '2' && medicalEditTabs.length>0">
                    <!-- <div class="single">
                      Chi phí của đơn thuốc này：{{ item.content.recipelInfo.fee }}VNĐ
                    </div>
                    <el-popconfirm
                        title="Bạn có chắc muốn hoàn phí không？"
                        @confirm="singleCharge(2)"
                        style="padding-top:7px"
                      >
                        <el-button style="height:30px" slot="reference" type="primary" plain>Hoàn phí đơn thuốc này</el-button>

                      </el-popconfirm> -->
                  </el-row>
                  <el-row style="padding-top:8px" v-if="chargeStatusActiveName === '3' && medicalEditTabs.length>0">
                    <!-- <div class="single">
                      Chi phí của đơn thuốc này：{{ item.content.recipelInfo.fee }}VNĐ
                    </div> -->
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
                    <el-table-column label="Số thứ tự" type="index" align="center">
                    </el-table-column>
                    <el-table-column
                      prop="drugStuffId.name"
                      label="Thông tin thuốc"
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
                      <!-- <template slot-scope="scope">
                  <template>{{ GetDrugFee(scope.$index, scope.row) }}</template>
                </template> -->
                    </el-table-column>
                    <el-table-column
                      prop="singleDosage"
                      label="Liều dùng mỗi lần"
                      width="width"
                      v-if="item.type!=='recipelType_3'"
                    >
                      <template slot-scope="scope">
                  <template>{{ scope.row.singleDosage+''+scope.row.drugStuffId.preparationUnit.name }}</template>
                </template>
                    </el-table-column>
                    <el-table-column prop="total" label="Số lượng" width="150">
                      <!-- <template slot-scope="scope">
                  <el-input
                    v-model="scope.row.total"
                    @input="GetAllDrugFee(scope.$index, scope.row)"
                    style="width: 140px"
                  >
                    <template slot="append">{{GetDrugUnit(scope.$index, scope.row)}}</template>
                    <el-select
                      v-model="scope.row.selectDrugTypeItem"
                      slot="append"
                      placeholder="Vui lòng chọn"
                      @change="chengeDrugTypeItems"
                      style="width: 60px"
                    >
                      <el-option
                        v-for="item in scope.row.selectDrugTypeItems"
                        :key="item.value"
                        :label="item.name"
                        :value="{
                          name: item.name,
                          value: item.value,
                          isUnpackSell: item.isUnpackSell,
                        }"
                      >
                      </el-option>
                    </el-select>
                  </el-input>
                </template> -->

                    </el-table-column>
                    <el-table-column prop="allFee" label="Tổng tiền">
                    </el-table-column>
                    <!-- <el-table-column label="Thao tác" align="center" v-if="!IsOnlyRead">
                <template slot-scope="scope">
                  <i
                    class="el-icon-circle-close"
                    @click="DeleteDrugItem(scope.$index, scope.row)"
                  ></i>
                </template>
              </el-table-column> -->
                  </el-table>
                  </div>

                  <div v-else style="margin-left:-10px;">
                      <div v-for="(items,index) in item.infusion.zushu" :key="index" style="border:1px solid #DCDCDC;padding:10px;margin:10px;">

                        <div>
                        <span  style="float:left;height:25px;line-height:25px;font-size:14px;">&nbsp;&nbsp;thứ{{index+1}}nhóm</span>
                        <el-button style="margin-left:10px;" v-if="item.infusion.infusionProject[index][0].executions!==item.infusion.infusionProject[index][0].days.name-0&&item.infusion.infusionProject[index][0].infuseType===0" type="warning" round>Chờ truyền hôm nay</el-button>
                         <el-button style="margin-left:10px;" v-else-if="item.infusion.infusionProject[index][0].executions===item.infusion.infusionProject[index][0].days.name-0||item.infusion.infusionProject[index][0].infuseType===1" type="success" round>Đã hoàn thành</el-button>
                        <el-button style="float:right;width:10%" type="primary" @click="execute(item.infusion.infusionProject[index])">执行</el-button>
                      <!-- <span  style="float:right;height:25px;line-height:25px;font-size:14px;">&nbsp;&nbsp;giọt/Phút</span>

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

                            </el-input> -->
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

                            <!-- <span  style="float:right;height:25px;line-height:25px;font-size:18px;">&nbsp;&nbsp;</span>
                                <el-input
                              style="float:right;width:10%"
                              :value="item.infusion.frequency[index].name"
                              placeholder=""
                              :disabled="
                               true
                              "
                            >

                            </el-input> -->
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

                            <!-- <span  style="float:right;height:25px;line-height:25px;font-size:18px;">&nbsp;&nbsp;</span>
                            <el-input
                              style="float:right;width:10%"
                              :value="item.infusion.infuseUse[index].name"
                              placeholder=""
                              :disabled="
                               true
                              "
                            >

                            </el-input> -->
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
                    <!-- <el-table-column label="Số nhóm" prop="infuseGroup" width="width">

                    </el-table-column> -->
                    <el-table-column label="Số thứ tự" type="index" align="center">
                    </el-table-column>
                    <el-table-column
                      prop="drugStuffId.name"
                      label="Thông tin thuốc"
                      width="width"
                    >
                    </el-table-column>
                    <el-table-column
                      prop="skinTest.name"
                      label="Test da"
                      width="width"
                    >
                    </el-table-column>
                      <el-table-column
                      prop="singleDosage"
                      label="Liều dùng mỗi lần"
                      width="width"
                    >
                      <template slot-scope="scope">
                  <template>{{ scope.row.singleDosage+''+scope.row.drugStuffId.preparationUnit.name }}</template>
                </template>
                    </el-table-column>
                     <el-table-column
                      prop="frequency.name"
                      label="Tần suất"
                      width="width"
                    >

                    </el-table-column>
                    <el-table-column
                      prop="infuseUse.name"
                      label="Cách dùng"
                      width="width"
                    >

                    </el-table-column>
                    <el-table-column prop="drippingSpeed" label="Tốc độ truyền" width="width">
                        <template slot-scope="scope">
                        <template>{{scope.row.drippingSpeed?scope.row.drippingSpeed:0}}giọt/Phút</template>
                      </template>
                    </el-table-column>
                    <el-table-column prop="days.name" label="Số ngày" width="width">
                      <template slot-scope="scope">
                        <template>{{scope.row.days.name}}ngày</template>
                      </template>
                    </el-table-column>
                     <el-table-column prop="executions" label="Tiến độ thực hiện"  width="width">
                      <template slot-scope="scope">
                        <template>{{scope.row.executions}}/{{scope.row.days.name}}</template>
                      </template>
                    </el-table-column>
                  </el-table>
                      </div>
                  </div>
                  <!-- <el-divider style="margin-top: 5px" content-position="left"
                    >Phí phụ thêm</el-divider
                  >
                  <el-table :data="item.content.isExtra" style="width: 100%">
                    <el-table-column label="Số thứ tự" type="index" align="center">
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
                    > -->
                      <!-- <template slot-scope="scope">
                  <template>{{ GetDrugFee(scope.$index, scope.row) }}</template>
                </template> -->
                    <!-- </el-table-column>
                    <el-table-column prop="total" label="Số lượng" width="150"> -->
                      <!-- <template slot-scope="scope">
                  <el-input
                    v-model="scope.row.total"
                    @input="GetAllDrugFee(scope.$index, scope.row)"
                    style="width: 140px"
                  >
                    <template slot="append">{{GetDrugUnit(scope.$index, scope.row)}}</template>
                    <el-select
                      v-model="scope.row.selectDrugTypeItem"
                      slot="append"
                      placeholder="Vui lòng chọn"
                      @change="chengeDrugTypeItems"
                      style="width: 60px"
                    >
                      <el-option
                        v-for="item in scope.row.selectDrugTypeItems"
                        :key="item.value"
                        :label="item.name"
                        :value="{
                          name: item.name,
                          value: item.value,
                          isUnpackSell: item.isUnpackSell,
                        }"
                      >
                      </el-option>
                    </el-select>
                  </el-input>
                </template> -->
                    <!-- </el-table-column>
                    <el-table-column prop="allFee" label="Tổng tiền">
                    </el-table-column> -->
                    <!-- <el-table-column label="Thao tác" align="center" v-if="!IsOnlyRead">
                <template slot-scope="scope">
                  <i
                    class="el-icon-circle-close"
                    @click="DeleteDrugItem(scope.$index, scope.row)"
                  ></i>
                </template>
              </el-table-column> -->
                  <!-- </el-table> -->
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
    </el-main>
    <el-dialog
      title="Thu phí"
      :visible.sync="chargeDialogVisible"
      width="40%"
      :close-on-click-modal="false"
      :before-close="dialogBeforeClose"
    >

      <div slot="footer">
        <el-button @click="singleDialogVisible = false">Hủy</el-button>
        <el-button type="primary" @click="singleChargeBtn(1)">Xác nhận thu phí</el-button>
      </div>
    </el-dialog>

     <!-- Hộp thoại chờ thực hiện -->
    <el-dialog
    title="Thực hiện khấu trừ"
    :visible.sync="executeDialogVisible"
    width="30%"
    :before-close="handleClose">
    <div style="border:1px solid #CCCC"></div>

    <div style="margin-top:10px;" >
      <!-- <el-link v-if="this.compare" type="warning" :underline="false" >Chờ truyền hôm nay</el-link>
      <el-link v-else type="warning" :underline="false" >Đã hoàn thành</el-link> -->
       <span style="float:left;margin-top:5px;">Thời gian thực hiện：</span>
        <el-date-picker
         style="float:left;"
          v-model="executeDate"
          type="datetime"
          :picker-options="pickerOptions"
          placeholder="Chọn ngày giờ">
        </el-date-picker>

    </div>
    <div style="margin-top:10px;">
     <el-table :data="updateExecute" style="width: 100%">
                    <el-table-column label="Số thứ tự" type="index" align="center"  width="width">
                    </el-table-column>
                    <el-table-column
                      prop="patientIdName"
                      label="患者名称"
                      width="width"
                      align="center"
                    >
                    </el-table-column>
                  <el-table-column prop="executions" label="Tiến độ thực hiện"  width="width" align="center">
                      <template slot-scope="scope">
                        <template>{{scope.row.executions}}/{{scope.row.days.name}}</template>
                      </template>
                  </el-table-column>
                    <el-table-column
                      prop="excuteNum"
                      label="Thực hiện lần này"
                      width="width"
                      align="center"
                    >
                     <template slot-scope="scope">
                    <el-input-number v-model="scope.row.excuteNum" :min=1 :max="(scope.row.days.name-0)-scope.row.executions" @change="handleChange" size="minin"  label=""></el-input-number>
                  </template>
              </el-table-column>
      </el-table>
      </div>
      <div style="margin-top:10px;">
        <div>Bản ghi thực hiện：</div>

        <div v-for="(item,index) in recordList" :key="index" style="margin-top:10px;">
          <span style="font-siza:10px;">{{GetCreateBy(index, item)}} &nbsp; {{item.executeDate}}
            &nbsp; <el-button v-if="item.delType==='0' && dateChange(item.updateDate)" type="text" @click="revocation(index,item)">Thu hồi</el-button>
            <el-button v-else-if="item.delType==='0' && !dateChange(item.updateDate)" disabled type="text">Đã hoàn thành</el-button>
            <el-button v-else-if="item.delType==='1'" disabled type="text">Đã thu hồi</el-button>
          </span>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="executeDialogVisible = false">Hủy</el-button>
        <el-button type="primary" v-if="executeDisable"  @click="save">Lưu</el-button>
      </span>
      </el-dialog>
  </el-container>
</template>

<script>
import { listDrugPage, listAllStock } from "@/api/stock/drug";
import { saveTollTollInfo } from "@/api/toll/tollInfo";
import { listRegistrationPages } from "@/api/outpatient/registration";
import { getPatientById, listPatientAll } from "@/api/outpatient/patient";
import { getRecipelInfoById } from "@/api/outpatient/recipelInfo";
import { saveCureManagement,listCureManagement } from "@/api/cure/cureManagement";
import {
  allSaveMedicalRecord,
  allQueryMedicalRecord,
  getMedicalRecordById,
  listMedicalRecordPage,
} from "@/api/outpatient/medicalRecord";
import {selectdetailid} from '@/api/cure/stmanagement'
export default {
  data() {
    let cardIdNo = (rule, value, callback) => {
      if (!value) {
        return callback(new Error("Số CMND/CCCD không được để trống"));
      } else {
        let reg = /((^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$))/;
        if (!reg.test(value)) {
          callback(new Error("Vui lòng nhập đúng số CMND/CCCD"));
        } else {
          callback();
        }
      }
    };
    return {
      pickerOptions: {
          disabledDate(time) {
            return time.getTime() > Date.now();
          },
      },
      recordList:[],
      executeDisable:true,
      getUserId:"",
      saveInfusion:[],
      compare:true,
      executeDialogVisible:false,
      updateExecute:[],
      executeDate:new Date(),
      patientIdName:'',
      isBaseReadOnly: true,
      isRetail: false, //Có bán lẻ không
      selectCondition: "", //Điều kiện truy vấn bệnh nhân
      chargeStatusActiveName: "1", //Tab trạng thái thu phí
      chargeDialogVisible: false,
      singleDialogVisible: false,
      chargeTotalFee: 100,
      chargeForm: {
        amountReceivable: 0,
        amountDiscounted: 0,
        discount: 0,
        amountReceived: 0,
        paymentType: "payType_0",
        meno: "",
      },
      singleForm: {
        amountReceivable: 0,
        amountDiscounted: 0,
        discount: 0,
        amountReceived: 0,
        paymentType: "payType_0",
        meno: "",
      },
      refundDialogVisible: false,
      patientQueryCondition: {
        columnName: "",
        limit: 0,
        offset: 0,
        order: "",
        companyId: "",
        recipeStatus: "0",
        updateDate: "2022-06-01 00:00:00",
        status: "registrationStatus_1",
        chargeStatus: 1,
        dispensionStatus:1,
        patientName:null,
        patientCode:null,
        recipelType:'recipelType_11',
        infuseType:0

      },
      patientList: [], //Danh sách bệnh nhân chưa thu phí
      isPayPatientList: [], //Danh sách bệnh nhân đã thu phí
      returnPatientList: [], //Danh sách bệnh nhân đã hoàn phí
      patientTotalCount: 0, //Tổng số bệnh nhân chưa thu phí
      isPayTotalCount: 0, //Tổng số bệnh nhân đã thu phí
      returnTotalCount: 0, //Tổng số bệnh nhân đã hoàn phí
      patientPageSize: 15, //Số bệnh nhân hiển thị mỗi trang
      patientCurrentPage: 1, //Trang hiện tại của bệnh nhân chưa thu phí
      isPayPatientCurrentPage: 1, //Trang hiện tại của bệnh nhân đã thu phí
      returnPatientCurrentPage: 1, //Trang hiện tại của bệnh nhân đã hoàn phí
      patientInfoForm: {
        name: "",
        gender: {
          name: "",
          value: "",
        },
        age: "",
        month: "",
        phone: "",
        phone: "",
        card: "",
        westernDiagnose: "Không có",
        chinaDiagnose: "Không có",
      },
      patientInfoNoRules: null,
      patientInfoRules: {
        name: [
          { required: true, message: "Vui lòng nhập họ tên", trigger: "blur" },
          { min: 1, max: 50, message: "Độ dài trong khoảng1đến50ký tự", trigger: "blur" },
        ],
        gender: [{ required: true, message: "Vui lòng chọn giới tính", trigger: "blur" }],
        age: [{ required: true, message: "Vui lòng nhập tuổi", trigger: "blur" }],
        month: [{ required: true, message: "Vui lòng nhập tuổi (tháng)", trigger: "blur" }],
        phone: [
          { required: true, message: "Vui lòng nhập số điện thoại liên hệ", trigger: "blur" },
          { min: 1, max: 20, message: "Độ dài trong khoảng1-20ký tự", trigger: "blur" },
        ],
        card: [
          {
            required: true,
            trigger: "blur",
            validator: cardIdNo,
          },
        ],
      },
      genderList: [
        {
          value: "gender_0",
          name: "Nam",
        },
        {
          value: "gender_1",
          name: "Nữ",
        },
      ],
      /*Danh sách đơn thuốc chờ thu phí*/
      prescriptionMainList: [],
      refundPrescriptionMainList: [
        {
          prescriptionType: 1,
          isSelect: true,
          totalCost: 50,
          prescriptionDetail: [
            {
              goodsName: "test",
              factoryName: "",
              price: 10,
              quantity: 5,
              unit: "",
              totalCost: 50,
            },
          ],
        },
        {
          prescriptionType: 2,
          isSelect: true,
          totalCost: 150,
          prescriptionDetail: [
            {
              goodsName: "test2",
              factoryName: "",
              tisanesMethod: "",
              price: 10,
              quantity: 5,
              totalQuantity: 15,
              totalCost: 150,
            },
          ],
        },
        {
          prescriptionType: 3,
          isSelect: true,
          totalCost: 50,
          prescriptionDetail: [
            {
              goodsName: "test3",
              price: 10,
              quantity: 5,
              totalCost: 50,
            },
          ],
        },
      ],
      //Tất cả bệnh nhân
      AllPatientOption: [],
      //Chọn đơn thuốc
      WesternCheck: true,
      ChineseCheck: true,
      InfusionCheck: true,
      SurchargeCheck: true,
      CostItemCheck: true,
      //Bệnh án hiện tại
      medicalRecord: {},
      //Số khám hiện tại
      VisitId: "",
      //Trạng thái thu phí của bản ghi thu phí bên trái đang chọn
      selRowChargeStatus: false,
      DrugAllTable: [],
      SearchDrugName: "",
      RetailTable: [],
      //Danh sách đơn thuốc hiển thị khi nhấn vào bệnh nhân
      AlreadyPatientDescriptions: [],
      //Chỉ đọc
      isOnlyRead: false,
      BASE_API: process.env.BASE_API,
      totalMoney: 0, //Tổng số tiền
      payState: true,
      medicalEditTabs: [],
      medicalEditTabsValue: {},
      medicalClickTabsValue: {},
      //Lớp thực thể tham số mới
      PageRegistration: {
        columnName: "",
        limit: 0,
        offset: 0,
        order: "",
        companyId: "",
        recipeStatus: "0",
        updateDate: "2022-06-01 00:00:00",
        status: "registrationStatus_1",
        chargeStatus: "",
        patientName:null,
        patientCode:null,
      },
      defaultNumber:1,//Số nhóm mặc định
      infusionProject:[[]],
      drippingSpeed:[""],  //Tốc độ truyền
      days:[{}],    //Số ngày
      frequency:[{}],   //Tần suất
      infuseUse:[{}],     //Cách dùng
      zushu:[1],
      excharge:[],  //Phí phụ thêm truyền dịch
      overColor:0,
      //查询皮试结果
      strc:{
        companyId: currentUser.company.id,
        detailid:'',
      },
    };
  },
  computed: {
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
    AllTotal() {
      return (
        // this.WesternFee +
        // this.ChinesePrescription.fee +
        // this.InfusionFee +
        // this.CostItemFee +
        // this.SurchargeFee
        0
      );
    },

  },
  watch: {
    chargeStatusActiveName() {
      this.isRetail = false
      this.search();
      this.overColor=0
    },
  },
  methods: {
     assignment(){
      let executeRow={
        allFee:"",
        chineseMedicineUse:"",
        company:"",
        createBy:"",
        createDate:"",
        days:"",
        drugStuffId:"",
        excuteNum:"",
        executions:"",
        frequency:"",
        id:"",
        infuseGroup:"",
        infuseUse:"",
        isExtra:"",
        isUnpackSell:"",
        minTotal:"",
        recipelInfo:"",
        seq:"",
        skinTest:"",
        stuffType:"",
        total:"",
        unitPrice:"",
        updateBy:"",
        updateDate:"",
        westernMedicineUse:"",
        patientIdName:"",
        infuseType:""
       }
       this.updateExecute.push(executeRow)
    },
     GetCreateBy(index,row){
      let str=""
      let count=0
      for (let i = 0; i < row.createBy.length; i++) {
       if(row.createBy[i]==='('){
          count=i
       }
      }
      str=row.createBy.substring(0,count)
      return str;
    },
     revocation(index,item){
       let count1=0
       let count2=0
       for (let i = 0; i < this.recordList.length; i++) {
         count1+=this.recordList[i].addNumber
          if(this.recordList[i].delType==='1'){

                count2+=this.recordList[i].addNumber

          }
       }
      item.executions=item.executions
      item.delType='1'
      item.recipelDetail.executions=count1-count2-item.addNumber
      //return
      saveCureManagement(item).then((res)=>{
        if(res.code===100){
          this.$message.success("Thu hồi thành công")
          this.getSaveInfo(this.getUserId)
          this.search()
          this.executeDialogVisible=false
        }
      }).catch()
    },
    dateChange(e){
        let date = new Date(e);

          date = date.getFullYear() + '-'
              + (date.getMonth()+1) + '-'   // 这里加1là vìgetMonth()giá trị trả về là0~11
              + date.getDate() + ' '
                  + date.getHours() + ':'
                  + date.getMinutes() + ':'
                  + date.getSeconds();
          date = Date.parse(new Date(date))/1000; // Chuyển thành dấu thời gian，Giá trị trả về làms，chia1000sau đó chuyển thành giây
          date += (86400); // Một ngày là86400giây
          let newDate = new Date(parseInt(date) * 1000); // Chuyển dấu thời gian thành ngày
          // let myDate = date.getFullYear() + '-'
          //               + (date.getMonth()+1) + '-'
          //               + date.getDate() + ' '
          //                 + date.getHours() + ':'
          //                 + date.getMinutes() + ':'
          //                 + date.getSeconds();
         if(newDate>new Date()){
           return true;
         }else{
           return false;
         }
      },

    //Thực hiện lưu
    save(){
      this.updateExecute[0].executions=this.updateExecute[0].executions+this.updateExecute[0].excuteNum
      let executeModel={
        company:JSON.parse(sessionStorage.getItem("currentCompany")),
        recipelDetail:this.updateExecute[0],
        executions:this.updateExecute[0].executions,
        executeDate:this.executeDate
          ? this.$moment(this.executeDate).format(
              "YYYY-MM-DD HH:mm:ss"
            )
          : "",
        addNumber:this.updateExecute[0].excuteNum
      }
      //return
      saveCureManagement(executeModel).then((res)=>{
        if(res.code===100){
          this.$message.success("Thực hiện thành công")
          this.saveInfusion

          this.compare=false
          this.getSaveInfo(this.getUserId)
          this.search()
          this.executeDialogVisible=false
        }
      }).catch((error)=>{
        this.$message.error(error)
      })

    },
    execute(row){
      //判断是否皮试
      this.strc.detailid=row[0].id;
      selectdetailid(this.strc).then((responseData)=>{
        if(responseData.code===100){
          if(row[0].skinTest.value==="skinTest_0" && responseData.data[0].stresult==null){
            this.$message.error("Vui lòng thử phản ứng da trước khi thực hiện!")
            return;
          }
          else{
       let executeRow={
        allFee:row[0].allFee,
        chineseMedicineUse:row[0].chineseMedicineUse,
        company:row[0].company,
        createBy:row[0].createBy,
        createDate:row[0].createDate,
        days:row[0].days,
        drugStuffId:row[0].drugStuffId,
        excuteNum:1,
        executions:row[0].executions,
        frequency:row[0].frequency,
        id:row[0].id,
        infuseGroup:row[0].infuseGroup,
        infuseUse:row[0].infuseUse,
        isExtra:row[0].isExtra,
        isUnpackSell:row[0].isUnpackSell,
        minTotal:row[0].minTotal,
        recipelInfo:row[0].recipelInfo,
        seq:row[0].seq,
        skinTest:row[0].skinTest,
        stuffType:row[0].stuffType,
        total:row[0].total,
        unitPrice:row[0].unitPrice,
        updateBy:row[0].updateBy,
        updateDate:row[0].updateDate,
        westernMedicineUse:row[0].westernMedicineUse,
        patientIdName:this.patientIdName,
        infuseType:row[0].infuseType
       }
       let cureSearch={
        params: [
          {
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
          {
            columnName: "recipel_info_id",
            queryType: "=",
            value: row[0].recipelInfo.id,
          },
          {
            columnName: "infuse_group",
            queryType: "=",
            value: row[0].infuseGroup,
          },
        ],
        offset: 0,
        limit: 100,
        columnName: "", // Tên trường sắp xếp
        order: "", // Sắp xếp
      }

     if(executeRow.executions===executeRow.days.name-0||executeRow.infuseType===1){
       this.executeDisable=false
     }else{
       this.executeDisable=true
     }
      listCureManagement(cureSearch).then((res)=>{
        if(res.code===100){
          this.recordList=res.data.rows
        }
      }).catch(()=>{})
      //  if(row[0].days.name==row[0].executions){
      //    this.compare=false
      //  }else{
      //    this.compare=true
      //  }
       this.updateExecute=[]
      this.executeDialogVisible=true

      this.updateExecute.push(executeRow)
      this.saveInfusion=row
          }
        }
      }).catch(()=>{})

    },
    clickMedicalEditTab() {
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
    search() {
      this.patientCurrentPage = 1;
      this.isPayPatientCurrentPage = 1;
      this.returnPatientCurrentPage = 1;
      this.patientInfoForm = {
        name: "",
        gender: {
          name: "",
          value: "",
        },
        age: "",
        month: "",
        phone: "",
        phone: "",
        card: "",
        westernDiagnose: "Không có",
        chinaDiagnose: "Không có",
      }
      this.medicalEditTabs = []
      this.medicalEditTabsValue = {}
      this.medicalClickTabsValue = {}
      this.payState = false
      if (this.chargeStatusActiveName === "1") {
        this.selectPatientList();
      } else if (this.chargeStatusActiveName === "2") {
        this.getIsPayPatientList();
      } else {
        this.returnPayPatientList();
      }
    },
    print() {
      if (this.prescriptionMainList.length === 0) {
        this.$message.error("Chưa chọn dữ liệu");
        return;
      }
      let arr = []
      this.prescriptionMainList.forEach((item, index) => {
        let params = {
          recipelInfoId: item.recipelInfo.id,
          type: item.recipelInfo.recipelType.value,
        };
        let str = "";
        if (params.recipelInfoId) {
          str = "&recipelInfoId=" + params.recipelInfoId;
        }
        arr[index] = window.open(
          this.BASE_API +
            "/ureport/preview?_u=Newtouch:charge.ureport.xml&_t=1,6,7,8,9" +
            str,
          index
        );
        arr[index].onload=()=>{
          arr[index].document.title = "Hệ thống Quản lý Phòng khám"
        }
      });
    },
    pageInit() {
      this.selectPatientList();
      this.GetAllPatient();
    },
    handlePatientPage(pageNum) {
      this.patientCurrentPage = pageNum;
      this.selectPatientList();
    },
    handleIsPayPage(pageNum) {
      this.isPayPatientCurrentPage = pageNum;
      this.getIsPayPatientList();
    },
    handleReturnPage(pageNum) {
      this.returnPatientCurrentPage = pageNum;
      this.returnPayPatientList();
    },
    isPayChange(val) {
    },
    //Thu phí riêng
    singleCharge(type){
      if(type===1){
        this.IsSingleCorrect();
        this.singleDialogVisible = true
      }else{
        this.singleChargeBtn(2)
      }
    },
    /*Nhấn nút thu phí/hoàn phí*/
    chargeClick(type) {
      if (this.prescriptionMainList.length === 0) {
        this.$message.error("Chưa chọn dữ liệu");
        return;
      }
      if (type === 1) {
        if (!this.isRetail) {
          let IsOpen = this.IsChargeCorrect("0");
          if (!IsOpen) {
            this.$message("Đơn thuốc đang chọn không đúng！");
            return;
          }
        } else {
          this.GetRetailRecipelDetailEvtList(1);
        }
        this.chargeDialogVisible = true;
      } else {
        if (
          this.prescriptionMainList[0].recipelInfo.recipelType.name ===
          "Đơn thuốc bán lẻ"
        ) {
          let IsOpen = this.IsChargeCorrect("1");
          if (!IsOpen) this.$message("Đơn thuốc đang chọn không đúng！");
          else this.OutRetailSaveRetailPrescription();
        } else {
          let IsOpen = this.IsChargeCorrect("1");
          if (!IsOpen) this.$message("Đơn thuốc đang chọn không đúng！");
          else this.chargeBtn(2);
        }
      }
    },
    getPrescriptionTotalCost(data) {
      let totalCost = 0;
      data.forEach((element) => {
        if (element.totalCost) {
          totalCost += element.totalCost;
        }
      });
      return totalCost;
    },
    /*Tính tổng đơn thuốc hoàn phí hiện tại*/
    getRefundTotalCost() {
      let totalCost = 0;
      this.refundPrescriptionMainList.forEach((element) => {
        if (element.totalCost) {
          totalCost += element.totalCost;
        }
      });
      return totalCost;
    },
    /*Chuyển sang bán lẻ*/
    changeToRetail() {
      //Xóa bệnh nhân
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
      };
      this.RetailTable = [];
      this.isRetail = true;
      this.$nextTick(() => {
        this.$refs.patientInfoForm.resetFields();
      });
    },
    /*Nhấn vào danh sách bệnh nhân*/
    selectPatientChange() {
      if (!this.patientInfoRow) {
        return;
      }
      this.isRetail = false;
      this.$nextTick(() => {
        this.$refs["patientInfoForm"].clearValidate();
      });
      if (this.chargeStatusActiveName === "1") this.payState = true;
      this.prescriptionMainList = [];
      //Lấy thông tin bệnh án theo thông tin đăng ký
      // let medicalSearch={
      //   params: [
      //      {
      //       columnName: "company_id",
      //       queryType: "=",
      //       value: currentUser.company.id,
      //     },
      //     {

      //       columnName: "registration_id",
      //       queryType: "=",
      //       value: this.patientInfoRow.id,

      //     }
      //   ],
      //   offset: 0,
      //   limit: 10,
      //   columnName: "", // Tên trường sắp xếp
      //   order: "", // Sắp xếp
      // }

      // listMedicalRecordPage(medicalSearch).then((res)=>{
      //   if(res.code=="100"){
      //     console.log(res,'woailuo');
      //     if(res.data.rows[0].westernDiagnose!=undefined){
      //         this.patientInfoForm.westernDiagnose=res.data.rows[0].westernDiagnose
      //       }else{
      //         this.patientInfoForm.westernDiagnose="Không có"
      //       }
      //       console.log(res.data.rows[0].chinaDiagnose);
      //       if(res.data.rows[0].chinaDiagnose!=undefined){
      //         this.patientInfoForm.chinaDiagnose=res.data.rows[0].chinaDiagnose
      //       }else{
      //         this.patientInfoForm.chinaDiagnose="Không có"
      //       }
      //   }
      // }).catch(()=>{})
      this.selRowChargeStatus = this.patientInfoRow.chargeStatus;
      this.patientIdName=this.patientInfoRow.patientId.name
      this.getSaveInfo(this.patientInfoRow.id);
      this.overColor=0
    },
    //Lấy thông tin cá nhân của bệnh nhân đang khám
    getPatientById(id) {
      return this.AllPatientOption.find((item) => item.id === id);
    },
    /*Truy vấn bệnh nhân*/
    selectPatientList() {
      const now = new Date();
      this.patientQueryCondition.limit = this.patientPageSize;
      this.patientQueryCondition.companyId = this.Company.id;
      /*Chờ thu phí*/
      this.isOnlyRead = false;
      this.patientQueryCondition.offset =
        (this.patientCurrentPage - 1) * this.patientPageSize;
      this.patientList = [];
      this.patientQueryCondition.columnName = "dispensing_date";
      this.patientQueryCondition.order = "desc";
      this.patientQueryCondition.updateDate = "2022-06-01 00:00:00";
      this.patientQueryCondition.status = "registrationStatus_1";
      this.patientQueryCondition.infuseType=0;
      if (this.selectCondition.trim()) {
          this.patientQueryCondition.patientName=this.selectCondition
          this.patientQueryCondition.patientCode=this.selectCondition
      }else{
        this.patientQueryCondition.patientName=''
        this.patientQueryCondition.patientCode=''
      }
      listRegistrationPages(this.patientQueryCondition)
        .then((responseData) => {
          if (responseData.code === 100) {
           if(responseData.data.rows!=null){
              this.patientList = responseData.data.rows.filter(
              (item) => item.recipeStatus !== 1

            );
            let num = responseData.data.rows.length - this.patientList.length;
            if (this.chargeStatusActiveName === "1") {
              this.patientTotalCount = responseData.data.total - num;
            } else {
              this.isPayTotalCount = responseData.data.total - num;
            }
           }else{
             this.patientList=responseData.data.rows
             if (this.chargeStatusActiveName === "1") {
              this.patientTotalCount = 0;
            } else {
              this.isPayTotalCount = 0;
            }
           }

          } else {
            this.$message.error(responseData);
          }
        })
        .catch((error) => {
          this.$message.error(error);
        });
    },
    //Truy vấn danh sách đã thu phí
    getIsPayPatientList() {
      const now = new Date();
      this.patientQueryCondition.limit = this.patientPageSize;
      this.patientQueryCondition.companyId = this.Company.id;
      this.isOnlyRead = true;
      this.patientQueryCondition.offset =
        (this.isPayPatientCurrentPage - 1) * this.patientPageSize;
      this.isPayPatientList = [];
      this.patientQueryCondition.columnName = "treatment_date";
      this.patientQueryCondition.order = "desc";
      this.patientQueryCondition.updateDate = "2022-06-01 00:00:00";
      this.patientQueryCondition.status = "registrationStatus_1";
      this.patientQueryCondition.infuseType=1;
      if (this.selectCondition.trim()) {
          this.patientQueryCondition.patientName=this.selectCondition
          this.patientQueryCondition.patientCode=this.selectCondition
      }else{
        this.patientQueryCondition.patientName=''
        this.patientQueryCondition.patientCode=''
      }
      listRegistrationPages(this.patientQueryCondition)
        .then((responseData) => {
          if (responseData.code === 100) {
            this.isPayTotalCount = responseData.data.total;
            this.isPayPatientList = responseData.data.rows;
          } else {
            this.$message.error(responseData);
          }
        })
        .catch((error) => {
          this.$message.error(error);
        });
    },

    //Truy vấn danh sách đã hoàn phí
    returnPayPatientList() {
      const now = new Date();
      this.patientQueryCondition.limit = this.patientPageSize;
      this.patientQueryCondition.companyId = this.Company.id;
      this.patientQueryCondition.offset =
        (this.returnPatientCurrentPage - 1) * this.patientPageSize;
      this.returnPatientList = [];
      this.patientQueryCondition.columnName = "retreats_date";
      this.patientQueryCondition.order = "desc";
      this.patientQueryCondition.chargeStatus = -1;
      this.patientQueryCondition.updateDate = "2022-06-01 00:00:00";
      this.patientQueryCondition.status = "registrationStatus_1";

       if (this.selectCondition.trim()) {
          this.patientQueryCondition.patientName=this.selectCondition
          this.patientQueryCondition.patientCode=this.selectCondition
      }else{
        this.patientQueryCondition.patientName=''
        this.patientQueryCondition.patientCode=''
      }

      listRegistrationPages(this.patientQueryCondition)
        .then((responseData) => {
          if (responseData.code === 100) {
            this.returnTotalCount = responseData.data.total;
            this.returnPatientList = responseData.data.rows;
          } else {
            this.$message.error(responseData);
          }
        })
        .catch((error) => {
          this.$message.error(error);
        });
    },

    //Theo người khámidTruy vấn bệnh án đơn thuốc
    getSaveInfo(visitId) {
      this.getUserId=visitId
      this.VisitRecordsList = [];
      allQueryMedicalRecord(visitId).then((responseData) => {
        if (responseData.code === 100) {
          if (this.chargeStatusActiveName !== "3") {
            this.prescriptionMainList =
              responseData.data.recipelInfoEvtList.filter(
                (item) => item.recipelInfo.status !== -1
              );
          } else {
            this.prescriptionMainList = responseData.data.recipelInfoEvtList;
          }
          if (this.chargeStatusActiveName === "1") {
             this.prescriptionMainList = this.prescriptionMainList.filter(
              (item) => item.recipelInfo.status !== -1
            );
            this.prescriptionMainList = this.prescriptionMainList.filter(
              (item) => item.recipelInfo.chargeStatus === 1
            );
             this.prescriptionMainList = this.prescriptionMainList.filter(
              (item) => item.recipelInfo.dispensionStatus === 1
            );
             this.prescriptionMainList = this.prescriptionMainList.filter(
              (item) => item.recipelInfo.recipelType.value === 'recipelType_2'
            );
             this.prescriptionMainList = this.prescriptionMainList.filter(
              (item) => item.recipelInfo.infuseType === 0
            );
          } else if (this.chargeStatusActiveName === "2") {
            this.prescriptionMainList = this.prescriptionMainList.filter(
              (item) => item.recipelInfo.status !== -1
            );
            this.prescriptionMainList = this.prescriptionMainList.filter(
              (item) => item.recipelInfo.chargeStatus === 1
            );
            this.prescriptionMainList = this.prescriptionMainList.filter(
              (item) => item.recipelInfo.dispensionStatus === 1
            );
             this.prescriptionMainList = this.prescriptionMainList.filter(
              (item) => item.recipelInfo.recipelType.value === 'recipelType_2'
            );
             this.prescriptionMainList = this.prescriptionMainList.filter(
              (item) => item.recipelInfo.infuseType === 1
            );
          } else {
            this.prescriptionMainList = this.prescriptionMainList.filter(
              (item) => item.recipelInfo.chargeStatus === -1
            );
          }
          this.prescriptionMainList.forEach((item) => {
            item.isExtra = [];
            item.notExtra = [];
            item.recipelDetailEvtList.forEach((items) => {
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
          this.VisitId = this.patientInfoForm.id;
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
            compare:true
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
    //Lấy chi tiết tất cả bệnh nhân
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
          // responseData.data.filter(item=>)
          this.AllPatientOption = responseData.data;
        }
      });
    },
    clinicDateFormatter(row, column) {
      const date = new Date(row[column.property]);
      return (
        this.padleftZero(date.getMonth() + 1) +
        "/" +
        this.padleftZero(date.getDate()) +
        " " +
        this.padleftZero(date.getHours()) +
        ":" +
        this.padleftZero(date.getMinutes())
      );
    },
    padleftZero(str) {
      str = "0000000000" + str;
      return str.substr(str.length - 2);
    },
    GetDrugTable() {
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
            columnName: "goods_name",
            queryType: "like",
            value: this.SearchDrugName,
          },
        ],
      };

      const pattern2 = new RegExp("[A-Za-z]+");
      if (pattern2.test(this.SearchDrugName)) {
        // console.log(this.SearchWesternInput,'Ký tự');
        SearchModel.params[1].value = this.SearchDrugName.toUpperCase();
        SearchModel.params[1].columnName = "pinyin_code";
      } else {
        SearchModel.params[1].value = this.SearchDrugName;
        SearchModel.params[1].columnName = "goods_name";
      }
      listAllStock(SearchModel).then((responseData) => {
        if (responseData.code === 100) {
          responseData.data.forEach((element) => {
            let isUnpackSell = element.isUnpackSell; //Cho phép bán lẻ tách hộp
            let stockNumber = element.stockNumber; //Số lượng tồn kho
            let packName = element.pack.name; //Đóng gói（Đơn vị）
            let preparation = element.preparation; //Chế phẩm
            let preparationUnit = element.preparationUnit.name; //Đơn vị bào chế
            let price = element.price; //Giá bán
            let retailPrice = element.retailPrice; //Giá bán lẻ sau khi tách hộp
            let type = element.type.value; //Loại thuốc： medicalType_1 - Thuốc đông y   medicalType_0 - Thuốc tây

            let stockText = "";
            let selectDrugTypeItems = [];
            if (type === "medicalType_1") {
              stockText = stockNumber + preparationUnit;
              selectDrugTypeItems.push({
                value: element.preparationUnit.value,
                name: element.preparationUnit.name,
                isUnpackSell: 1,
              });
            } else if (isUnpackSell === "1" && type === "medicalType_0") {
              selectDrugTypeItems.push({
                value: element.preparationUnit.value,
                name: element.preparationUnit.name,
                isUnpackSell: 1,
              });
              selectDrugTypeItems.push({
                value: element.pack.value,
                name: element.pack.name,
                isUnpackSell: 0,
              });
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
              selectDrugTypeItems.push({
                value: element.pack.value,
                name: element.pack.name,
                isUnpackSell: 0,
              });
            }

            element["stockText"] = stockText;
            element["selectDrugTypeItems"] = selectDrugTypeItems;
            element["selectDrugTypeItem"] = selectDrugTypeItems[0];
          });

          this.DrugAllTable = responseData.data;
        }
      });
    },
    RowClickDrugTable(row) {
      let flag = false;
      this.RetailTable.forEach((element) => {
        if (element.id === row.id) {
          flag = true;
        }
      });

      if (!flag) {
        this.RetailTable.push({
          allFee: 0,
          ...JSON.parse(JSON.stringify(row)),
        });
      }
    },

    DeleteDrugItem(index) {
      this.RetailTable.splice(index, 1);
    },

    chengeDrugTypeItems(sel) {
    },
    //Lấy đơn vị theo bán lẻ hay thêm mới/xem
    GetDrugUnit(index, row) {
      if (row.preparationUnit) {
        if (row.isUnpackSell === "1") {
          return row.preparationUnit.name;
        } else if (row.type.name === "Thuốc đông y") {
          return row.dosisUnit.name;
        } else {
          return row.pack.name;
        }
      } else {
        if (
          row.drugStuffId.drug.isUnpackSell === "1" ||
          row.drugStuffId.drug.type.name === "Thuốc đông y"
        ) {
          return row.drugStuffId.drug.preparationUnit.name;
        } else {
          return row.drugStuffId.drug.pack.name;
        }
      }
    },

    GetAllDrugFee(index, row) {
      if (row.isUnpackSell === "1" && row.type.name !== "Thuốc đông y") {
        row.allFee = row.total * row.retailPrice;
      } else {
        row.allFee = row.total * row.price;
      }
    },

    GetDrugFee(index, row) {
      if (row.isUnpackSell === "1" || row.type.value === "medicalType_1") {
        return row.retailPrice + "/" + row.preparationUnit.name;
      } else {
        return row.price + "/" + row.pack.name;
      }
    },

    GetStatusColor(status) {
      return {
        redStateStyle: status === 2,
        greenStateStyle: status === 1,
      };
    },
    GetPrescriptionStatus(status) {
      let returnStatus;
      if (status === 0) returnStatus = "Chờ thu phí";
      else if (status === 1) returnStatus = "Đã thu phí";
      else if (status === -1) returnStatus = "Đã hoàn phí";
      return returnStatus;
    },

    //Tính phí thực tế
    GetAmountReceived() {
      let discount = this.chargeForm.discount;
      if (this.chargeForm.discount === 0) {
        discount = 10;
      }
      this.chargeForm.amountReceived =
        this.chargeForm.amountReceivable * discount * 0.1;
      this.chargeForm.amountReceived =
        this.chargeForm.amountReceived.toFixed(2);
    },
    GetSingleAmountReceived() {
      let discount = this.singleForm.discount;
      if (this.singleForm.discount === 0) {
        discount = 10;
      }
      this.singleForm.amountReceived =
        this.singleForm.amountReceivable * discount * 0.1;
      this.singleForm.amountReceived =
        this.singleForm.amountReceived.toFixed(2);
    },
    IsSingleCorrect(){
      let allFee = this.medicalEditTabsValue.content.recipelInfo.fee
      this.singleForm.amountReceivable = allFee.toFixed(2);
      this.singleForm.amountReceived = allFee.toFixed(2);
    },
    //Dữ liệu thu phí có đạt không
    IsChargeCorrect(type) {
      let IsSure = true;
      let allFee = 0;
      const typeNum = Number(type);
      this.prescriptionMainList.map((item) => {
        if (
          item.recipelInfo.recipelType.name === "Đơn thuốc tây" &&
          this.WesternCheck
        ) {
          if (item.recipelInfo.chargeStatus !== typeNum) {
            IsSure = false;
          } else {
            allFee += item.recipelInfo.fee;
          }
        } else if (
          item.recipelInfo.recipelType.name === "Đơn thuốc đông y" &&
          this.ChineseCheck
        ) {
          if (item.recipelInfo.chargeStatus !== typeNum) {
            IsSure = false;
          } else {
            allFee += item.recipelInfo.fee;
          }
        } else if (
          item.recipelInfo.recipelType.name === "Đơn truyền dịch" &&
          this.InfusionCheck
        ) {
          if (item.recipelInfo.chargeStatus !== typeNum) {
            IsSure = false;
          } else {
            allFee += item.recipelInfo.fee;
          }
        } else if (
          item.recipelInfo.recipelType.name === "Hạng mục khám chữa bệnh" &&
          this.CostItemCheck
        ) {
          if (item.recipelInfo.chargeStatus !== typeNum) {
            IsSure = false;
          } else {
            allFee += item.recipelInfo.fee;
          }
        } else if (
          item.recipelInfo.recipelType.name === "Phí phụ thêm" &&
          this.SurchargeCheck
        ) {
          if (item.recipelInfo.chargeStatus !== typeNum) {
            IsSure = false;
          } else {
            allFee += item.recipelInfo.fee;
          }
        } else if (item.recipelInfo.recipelType.name === "Đơn thuốc bán lẻ") {
          if (typeNum === 0) {
            IsSure = false;
          } else if (
            typeNum === 1 &&
            this.prescriptionMainList[0].recipelInfo.chargeStatus === 1
          ) {
            IsSure = false;
          }
        }
      });
      this.chargeForm.amountReceivable = allFee.toFixed(2);
      this.chargeForm.amountReceived = allFee.toFixed(2);
      return IsSure;
    },
    getChargeData(){

    },
    GetRetailRecipelDetailEvtList(type) {
      let recipelDetailEvtList = [];
      let allFee = 0;
      if (this.patientInfoForm.gender.value === "gender_0") {
        this.patientInfoForm.gender.name = "男";
      } else {
        this.patientInfoForm.gender.name = "女";
      }
      this.RetailTable.map((item) => {
        allFee += item.allFee;
        let retailType = "0";
        switch (item.type.value) {
          case "medicalType_0":
            retailType = "0";
            break;
          case "medicalType_1":
            retailType = "1";
            break;
          default:
            break;
        }
        recipelDetailEvtList.push({
          company: this.Company,

          drugStuffId: {
            drugStuffId: item.id,
          },
          total: item.total,
          allFee: item.allFee,
          retailType: retailType,
        });
      });
      if (type === 1) {
        this.chargeForm.amountReceivable = allFee.toFixed(2);
        this.chargeForm.amountReceived = allFee.toFixed(2);
      }
      return recipelDetailEvtList;
    },

    //Xác nhận thu phí-Nút hoàn phí(type: 1Thu phí 2Hoàn phí)
    chargeBtn(type) {
      if (!this.isRetail) {
        this.chargeSave(type);
      } else {
        this.SaveRetailPrescription(type);
      }
    },

    //Hoàn phí bán lẻ
    OutRetailSaveRetailPrescription() {
      let tollInfo = {
        company: {
          id: this.Company.id,
        },
        medical: this.medicalRecord,
        amountReceivable: this.prescriptionMainList[0].recipelInfo.fee,
        amountReceived: this.prescriptionMainList[0].recipelInfo.fee,
        amountDiscounted: 0,
        discount: this.chargeForm.discount * 0.1,
        paymentType: {
          name: "Tiền mặt",
          value: "payType_0",
        },
        amountStatus: {
          name: "Đã hoàn phí",
          value: "amountStatus_2",
        },
        tollType: {
          name: "Bán lẻ",
          value: "tollType_2",
        },
        patient: {
          id: this.patientInfoForm.id,
        },
      };
      let recipelInfos = [
        {
          recipelInfo: {
            id: this.prescriptionMainList[0].recipelInfo.id,
          },
        },
      ];
      let model = {
        tollInfo: tollInfo,
        recipelInfos: recipelInfos,
        chargeStatus: "4",
      };
      this.saveTollTollInfo(model);
    },

    //Thu phí bán lẻ
    SaveRetailPrescription() {
      let recipelDetailEvtList = this.GetRetailRecipelDetailEvtList(2);
      let paymentType = {
        name: "",
        value: this.chargeForm.paymentType,
      };
      switch (this.chargeForm.paymentType) {
        case "payType_0":
          paymentType.name = "Tiền mặt";
          break;
        case "payType_1":
          paymentType.name = "支付宝";
          break;
        case "payType_2":
          paymentType.name = "WeChat";
          break;
        case "payType_3":
          paymentType.name = "银行卡";
          break;
        case "payType_5":
          paymentType.name = "Chuyển khoản ngân hàng";
          break;
        case "payType_6":
          paymentType.name = "Ví điện tử";
          break;
        default:
          paymentType = {
            name: "Tiền mặt",
            value: "payType_0",
          };
          break;
      }
      let tollInfo = {
        company: {
          id: this.Company.id,
        },
        amountReceivable: this.chargeForm.amountReceivable,
        amountReceived: this.chargeForm.amountReceived,
        amountDiscounted: this.chargeForm.amountDiscounted,
        discount: this.chargeForm.discount * 0.1,
        paymentType: paymentType,
        amountStatus: {
          name: "Đã thu phí",
          value: "amountStatus_1",
        },
        tollType: {
          name: "Bán lẻ",
          value: "tollType_2",
        },
        patient: {
          ...this.patientInfoForm,
          company: { ...this.Company },
          id: "",
        },
      };
      let recipelInfo = {
        company: this.Company,
        recipelType: {
          name: "Đơn thuốc bán lẻ",
          value: "recipelType_5",
        },
        fee: this.chargeForm.amountReceivable,
      };
      let model = {
        tollInfo: tollInfo,
        recipelInfos: [
          {
            recipelInfo: recipelInfo,
            recipelDetailEvtList: recipelDetailEvtList,
          },
        ],
        chargeStatus: "2",
      };
      this.saveTollTollInfo(model);
    },
    singleChargeBtn(type){
      let paymentType = {
        name: "",
        value: this.singleForm.paymentType,
      };
      switch (this.singleForm.paymentType) {
        case "payType_0":
          paymentType.name = "Tiền mặt";
          break;
        case "payType_1":
          paymentType.name = "支付宝";
          break;
        case "payType_2":
          paymentType.name = "WeChat";
          break;
        case "payType_3":
          paymentType.name = "银行卡";
          break;
        case "payType_5":
          paymentType.name = "Chuyển khoản ngân hàng";
          break;
        case "payType_6":
          paymentType.name = "Ví điện tử";
          break;
        default:
          paymentType = {
            name: "Tiền mặt",
            value: "payType_0",
          };
          break;
      }
      let tollInfo = {
        company: {
          id: this.Company.id,
        },
        medical: this.medicalRecord,
        amountReceivable: this.singleForm.amountReceivable,
        amountReceived: this.singleForm.amountReceived,
        amountDiscounted: this.singleForm.amountDiscounted,
        discount: this.singleForm.discount * 0.1,
        paymentType: paymentType,
        patient: {
          id: this.patientInfoForm.id,
        },
        tollType: {
          name: "",
          value: "",
        },
        amountStatus: {},
      };
      let recipelInfos = [];
      let chargeStatus = 0
      recipelInfos.push({
        recipelInfo: {
          id: this.medicalEditTabsValue.content.recipelInfo.id,
        },
      });
      if(type===1){
        tollInfo.amountStatus = {
          name: "Đã thu phí",
          value: "amountStatus_1",
        };
        chargeStatus = "2"
      }else{
        tollInfo.amountStatus = {
            name: "Đã hoàn phí",
            value: "amountStatus_2",
          };
          chargeStatus = "4";
      }

      let model = {
        tollInfo: tollInfo,
        recipelInfos: recipelInfos,
        chargeStatus: chargeStatus,
      };
     // console.log(this.medicalEditTabsValue.content,'封杀iOS发哈夫');
      //return
      this.saveTollTollInfo(model);
    },
    //Xác nhận thu phí-Nút hoàn phí(type: 1Thu phí 2Hoàn phí)
    chargeSave(type) {
      let paymentType = {
        name: "",
        value: this.chargeForm.paymentType,
      };
      switch (this.chargeForm.paymentType) {
        case "payType_0":
          paymentType.name = "Tiền mặt";
          break;
        case "payType_1":
          paymentType.name = "支付宝";
          break;
        case "payType_2":
          paymentType.name = "WeChat";
          break;
        case "payType_3":
          paymentType.name = "银行卡";
          break;
        case "payType_5":
          paymentType.name = "Chuyển khoản ngân hàng";
          break;
        case "payType_6":
          paymentType.name = "Ví điện tử";
          break;
        default:
          paymentType = {
            name: "Tiền mặt",
            value: "payType_0",
          };
          break;
      }
      let tollInfo = {
        company: {
          id: this.Company.id,
        },
        medical: this.medicalRecord,
        amountReceivable: this.chargeForm.amountReceivable,
        amountReceived: this.chargeForm.amountReceived,
        amountDiscounted: this.chargeForm.amountDiscounted,
        discount: this.chargeForm.discount * 0.1,
        paymentType: paymentType,
        patient: {
          id: this.patientInfoForm.id,
        },
        tollType: {
          name: "",
          value: "",
        },
        amountStatus: {},
      };
      let recipelInfos = [];
      let IsAll = 0;

      this.prescriptionMainList.map((item) => {
        switch (item.recipelInfo.recipelType.name) {
          case "Đơn thuốc đông y":
            if (type === 1 && item.recipelInfo.chargeStatus === 1) IsAll += 1;
            else if (type === 2 && item.recipelInfo.chargeStatus === -1) IsAll += 1;
            if (this.ChineseCheck) {
              IsAll += 1;
              recipelInfos.push({
                recipelInfo: {
                  id: item.recipelInfo.id,
                },
              });
            }
            break;
          case "Đơn thuốc tây":
            if (type === 1 && item.recipelInfo.chargeStatus === 1) IsAll += 1;
            else if (type === 2 && item.recipelInfo.chargeStatus === -1) IsAll += 1;
            if (this.WesternCheck) {
              IsAll += 1;
              recipelInfos.push({
                recipelInfo: {
                  id: item.recipelInfo.id,
                },
              });
            }
            break;
          case "Đơn truyền dịch":
            if (type === 1 && item.recipelInfo.chargeStatus === 1) IsAll += 1;
            else if (type === 2 && item.recipelInfo.chargeStatus === -1) IsAll += 1;
            if (this.InfusionCheck) {
              IsAll += 1;
              recipelInfos.push({
                recipelInfo: {
                  id: item.recipelInfo.id,
                },
              });
            }
            break;
          case "Hạng mục khám chữa bệnh":
            if (type === 1 && item.recipelInfo.chargeStatus === 1) IsAll += 1;
            else if (type === 2 && item.recipelInfo.chargeStatus === -1) IsAll += 1;
            if (this.CostItemCheck) {
              IsAll += 1;
              recipelInfos.push({
                recipelInfo: {
                  id: item.recipelInfo.id,
                },
              });
            }
            break;
          case "Phí phụ thêm":
            if (type === 1 && item.recipelInfo.chargeStatus === 1) IsAll += 1;
            else if (type === 2 && item.recipelInfo.chargeStatus === -1) IsAll += 1;
            if (this.SurchargeCheck) {
              IsAll += 1;
              recipelInfos.push({
                recipelInfo: {
                  id: item.recipelInfo.id,
                },
              });
            }
            break;
          default:
            break;
        }
      });

      let chargeStatus = "0";
      if (IsAll === this.prescriptionMainList.length) {
        if (type === 1) {
          tollInfo.amountStatus = {
            name: "Đã thu phí",
            value: "amountStatus_1",
          };
          chargeStatus = "2";
        } else {
          tollInfo.amountStatus = {
            name: "Đã hoàn phí",
            value: "amountStatus_2",
          };
          chargeStatus = "4";
        }
      } else {
        if (type === 1) {
          tollInfo.amountStatus = {
            name: "Đã thu phí",
            value: "amountStatus_1",
          };
          chargeStatus = "1";
        } else {
          tollInfo.amountStatus = {
            name: "Đã hoàn phí",
            value: "amountStatus_2",
          };
          chargeStatus = "3";
        }
      }
      let state = false
      if(type===2){
        this.prescriptionMainList.forEach(item=>{
        if(item.recipelInfo.dispensionStatus===1){
          state = true
        }
      })

      }

      if(state){
        this.$message.error('Có đơn thuốc đã phát thuốc nhưng chưa trả thuốc，Vui lòng trả thuốc trước')
        return
      }
      let model = {
        tollInfo: tollInfo,
        recipelInfos: recipelInfos,
        chargeStatus: chargeStatus,
      };
      this.saveTollTollInfo(model);
    },
    //Thu phí
    saveTollTollInfo(model) {
      let type = "null";
      saveTollTollInfo(model, type)
        .then((responseData) => {
          if (responseData.code === 100) {
            this.$message.success("Thao tác thành công！");
            this.selectPatientList();
            // if (this.VisitId) {
            //   this.getSaveInfo();
            // } else {
            //   this.GetAllPatient();
            // }
            this.search();
            this.payState = false;
            this.chargeDialogVisible = false;
            this.singleDialogVisible = false;
            this.chargeForm= {
              amountReceivable: 0,
              amountDiscounted: 0,
              discount: 0,
              amountReceived: 0,
              paymentType: "payType_0",
              meno: "",
            }
            this.singleForm={
                amountReceivable: 0,
                amountDiscounted: 0,
                discount: 0,
                amountReceived: 0,
                paymentType: "payType_0",
                meno: "",
            }
          } else {
            this.$message.error(responseData.msg);
            this.showMessage(responseData.msg);
          }
        })
        .catch((error) => {
          this.outputError(error);
        });
    },
  },
  mounted() {
    this.pageInit();
    this.GetDrugTable();
    this.assignment();

  },
};
</script>


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

.medical-type-ul {
  list-style-type: none;
  font-size: 13px;
  color: #303133;
  margin: 5px;
  padding: 0;
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
<style scoped>
.single{
  padding: 8px 0 0;
    font-size: 14px;
    font-weight: 700;
    color: midnightblue;
    line-height: 28px;
}
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

#chargeTotalFee_Container {
  display: flex;
  justify-content: flex-start;
  padding: 20px;
  margin: 10px;
  background-color: rgba(207, 227, 236, 0.803921568627451);
  border-radius: 7px;
}
.chargeTotalFee_Container {
  display: flex;
  justify-content: flex-start;
  padding: 20px;
  margin: 10px;
  background-color: rgba(207, 227, 236, 0.803921568627451);
  border-radius: 7px;
}

#chargeTotalFee_Title {
  font-family: "Arial Normal", "Arial", sans-serif;
  font-weight: 400;
  font-style: normal;
  font-size: 21px;
  margin-right: 8px;
}
.chargeTotalFee_Title {
  font-family: "Arial Normal", "Arial", sans-serif;
  font-weight: 400;
  font-style: normal;
  font-size: 21px;
  margin-right: 8px;
}

#chargeTotalFee_Fee {
  font-weight: 800;
  font-style: black;
  font-size: 25px;
  margin-top: -4px;
}
.chargeTotalFee_Fee {
  font-weight: 800;
  font-style: black;
  font-size: 25px;
  margin-top: -4px;
}

.flex-space-between {
  display: flex;
  justify-content: space-between;
}

.flex-start {
  display: flex;
  justify-content: flex-start;
}

.prescriptionCard {
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
::v-deep .el-dialog__body{
  padding:0 7px;
  margin:0
}
::v-deep .el-form-item__label{
  text-align: left;

}
::v-deep .el-dialog .el-row{
  padding: 0 10px;
}
.clickTest{
  background-color: rgb(234, 242, 251) !important;
  color: #fff;
}
</style>
