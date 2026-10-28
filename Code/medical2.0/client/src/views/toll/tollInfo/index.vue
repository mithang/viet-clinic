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
                Chờ thu phí
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
                  <div v-for="(item, index) in patientList" :key="item">
                    <el-collapse-item class="already-medocal" :name="item">
                      <template slot="title">
                        <span
                          style="
                            font-weight: bold;
                            font-size: 12px;
                            color: #606266;
                            flex: 1;
                          "
                        >{{ patientQueryCondition.offset + index + 1 }}. {{
                            item.patientId && item.patientId.name ? item.patientId.name : 'Không xác định'
                          }}/ {{ item.patientId && item.patientId.gender && item.patientId.gender.name ? item.patientId.gender.name : '' }} /
                      {{ item.patientId && item.patientId.age ? item.patientId.age : '' }}tuổi</span
                        >
                        <span style="font-size: 12px; color: #606266">{{
                            item.receptionEndDate
                          }}</span>
                      </template>
                      <div
                        v-for="(o,index) in prescriptionMainList"
                        :key="index"
                        @click="AlreadyPatientDescriptionsQuery(o,index)"
                        :class="{clickTest:index===overColor}"
                      >
                        <div style="

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
                            >{{ o.recipelInfo.id }} -->
                        <!-- <span style="position: relative">
                                        <div v-if="o.recipelInfo.status === -1" class="refundBox">Hủy bỏ</div>
                                        <div v-if="o.recipelInfo.status === 1 && o.recipelInfo.chargeStatus === 0" class="refundBox">Chưa thu phí</div>
                                        <div v-if="o.recipelInfo.status === 1 && o.recipelInfo.chargeStatus === 1" class="refundBox">Đã thu phí</div>
                                        <div v-if="o.recipelInfo.status === 1 && o.recipelInfo.chargeStatus === -1" class="refundBox">Đã hoàn phí</div>
                                    </span> -->
                        <!-- </el-descriptions-item>
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
            <el-tab-pane name="2">
              <span slot="label">
                Đã thu phí
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
                  <div v-for="(item, index) in isPayPatientList" :key="item">
                    <el-collapse-item class="already-medocal" :name="item">
                      <template slot="title">
                        <span
                          style="
                            font-weight: bold;
                            font-size: 12px;
                            color: #606266;
                            flex: 1;
                          "
                        >{{ patientQueryCondition.offset + index + 1 }}. {{
                            item.patientId.name
                          }}/ {{ item.patientId.gender.name }} /
                      {{ item.patientId.age }}tuổi</span
                        >
                        <span style="font-size: 12px; color: #606266">{{
                            item.chargeDate
                          }}</span>
                      </template>
                      <div
                        v-for="(o,index) in prescriptionMainList"
                        :key="index"
                        @click="AlreadyPatientDescriptionsQuery(o,index)"
                        :class="{clickTest:index===overColor}"
                      >
                        <div style="

                            border-top: 1px dashed #dcdfe6;
                            margin: 0px 30px 0 15px;"></div>
                        <!-- <br> -->
                        <p style="font-weight: bold;margin-top: 15px;margin-left:30px;font-size: 12px;
                            color: #606266;">
                          {{ o.recipelInfo && o.recipelInfo.name ? o.recipelInfo.name : '' }}<span v-if="o.recipelInfo && o.recipelInfo.dispensionStatus===-1"
                                                        style="font-weight: bold;margin-left: 15px;color:red">Đã trả thuốc</span>
                        </p>

                        <p style="font-weight: bold;margin-top: 6px;margin-left:30px;font-size: 12px;
                            color: #606266;">
                          {{ o.recipelInfo && o.recipelInfo.id ? o.recipelInfo.id : '' }}

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
            <el-tab-pane name="3">
              <span slot="label">
                Đã hoàn phí
                <el-badge
                  :value="returnTotalCount"
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
                  <div

                    v-for="(item, index) in returnPatientList"
                    :key="item"
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
                        >{{ index + 1 }}. {{ item.patientId.name }}/ {{ item.patientId.gender.name }} /
                      {{ item.patientId.age }}tuổi</span
                        >
                        <span style="font-size: 12px; color: #606266">{{
                            item.retreatsDate
                          }}</span>
                      </template>
                      <div v-for="(o,index) in prescriptionMainList" :key="o"
                           :class="{clickTest:index===overColor}"
                           @click="AlreadyPatientDescriptionsQuery(o,index)"
                      >
                        <div style="

                            border-top: 1px dashed #dcdfe6;
                            margin: 0px 30px 0 15px;"></div>
                        <!-- <br> -->
                        <p style="font-weight: bold;margin-top: 15px;margin-left:30px;font-size: 12px;
                            color: #606266;">
                          {{ o.recipelInfo && o.recipelInfo.name ? o.recipelInfo.name : '' }}
                        </p>

                        <p style="font-weight: bold;margin-top: 6px;margin-left:30px;font-size: 12px;
                            color: #606266;">
                          {{ o.recipelInfo && o.recipelInfo.id ? o.recipelInfo.id : '' }}

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
                @current-change="handleReturnPage"
                :page-size="patientPageSize"
                layout="prev, pager, next"
                :total="returnTotalCount"
              >
              </el-pagination>
            </el-tab-pane>
          </el-tabs>
        </el-row>
      </el-card>
    </el-aside>
    <el-main>
      <el-card class="box-card sub-card" style="height: 50px; display: flex">
        <!-- <div style="font-weight: bold">Thu phí</div> -->
        <div class="flex-space-between">
          <div class="goods-back" v-if="chargeStatusActiveName === '3'&&!isRetail">
            Đã hoàn phí
          </div>
          <div class="goods-back" v-if="chargeStatusActiveName === '2'&&!isRetail">
            Đã thu phí
          </div>
          <div v-if="chargeStatusActiveName === '1' && payState &&!isRetail && diagnosisPrescription" style="margin-right:10px">
            Tổng cộng：{{ this.totalMoney }}VNĐ
          </div>
          <!--<div v-if="chargeStatusActiveName === '1' && payState &&!isRetail && diagnosisPrescription" style="margin-right:10px">
            Tổng cộng：{{ this.hisTotalMoney.toFixed(2) }}VNĐ
          </div>-->
          <el-button
            type="primary"
            @click="chargeClick(1)"
            v-if="chargeStatusActiveName === '1' && payState && !isRetail"
          >Thu phí
          </el-button
          >
          <!--<el-button
            type="primary"
            @click="addressee()"
            v-if="diagnosis !== null"
          >添加收件人
          </el-button
          >-->
          <el-popconfirm
            style="margin-right:10px"
            title="Bạn có chắc muốn hoàn phí không？"
            @confirm="chargeClick(2)"
            v-if="chargeStatusActiveName === '2' && selRowChargeStatus !== 4 && medicalEditTabs.length>0"
          >
            <el-button style="height:30px" slot="reference" type="primary" plain>Hoàn phí</el-button>

          </el-popconfirm>

          <el-button
            v-if="chargeStatusActiveName === '2' && medicalEditTabs.length>0"
            type="primary"
            @click="print"
          >In
          </el-button
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
              :rules="isRetail ? patientInfoRules : patientInfoNoRules"
              label-width="auto"
            >
              <el-row :gutter="24">
                <el-col :span="4">
                  <el-form-item label="Tên bệnh nhân" prop="name">
                    <div class="nameStyle">
                      {{ patientInfoForm.name ? patientInfoForm.name : "" }}
                      <div class="imgStyle" v-if="allMember.length>0">
                        <el-popover
                          placement="top-start"
                          title=""
                          width="200"
                          trigger="hover"
                        >
                          <div v-for="(item,index) in allMember" :key="index" style="padding:5px">
                            <el-tag>{{ item.memberName }}</el-tag>
                          </div>
                          <img slot="reference" src="../../../assets/images/vip.png" style="width:25px;height:25px"/>
                        </el-popover>
                      </div>

                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="3">
                  <el-form-item label="Giới tính" prop="gender">
                    <!-- <el-select
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.gender.name"
                      style="width: 100%"
                    >
                      <el-option label="Nam" value="男"></el-option>
                      <el-option label="Nữ" value="女"></el-option>
                    </el-select> -->
                    <span>{{ patientInfoForm.gender.name ? patientInfoForm.gender.name : "" }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="5">
                  <el-form-item label="Tuổi" prop="age">
                    <!-- <el-input
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
                    </el-input> -->
                    <span>
                      {{
                        patientInfoForm.age ? patientInfoForm.age : 0
                      }}tuổi{{ patientInfoForm.month ? patientInfoForm.month : 0 }}tháng
                    </span>
                  </el-form-item>
                </el-col>
                <el-col :span="5">
                  <el-form-item label="Thông tin liên hệ" prop="phone">
                    <!-- <el-input
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.phone"
                    ></el-input> -->
                    <span>
                      {{ patientInfoForm.phone }}
                    </span>
                  </el-form-item>
                </el-col>
                <el-col :span="5">
                  <el-form-item label="Số CMND/CCCD" prop="card">
                    <!-- <el-input
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.card"
                    ></el-input> -->
                    <span>{{ patientInfoForm.card }}</span>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="Chẩn đoán tây y" prop="westernDiagnose">
                    <!-- <el-input
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.westernDiagnose"
                    ></el-input> -->
                    <span>
                      {{ patientInfoForm.westernDiagnose ? patientInfoForm.westernDiagnose : "" }}
                    </span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Chẩn đoán đông y" prop="chinaDiagnose">
                    <!-- <el-input
                      :disabled="isBaseReadOnly"
                      v-model="patientInfoForm.chinaDiagnose"
                    ></el-input> -->
                    <span>
                      {{ patientInfoForm.chinaDiagnose ? patientInfoForm.chinaDiagnose : "" }}
                    </span>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24" v-if="diagnosis !== null">
                <el-col :span="4">
                  <el-form-item label="收件人">
                    <span>
                      {{ diagnosis.recipientName }}
                    </span>
                  </el-form-item>
                </el-col>
                <el-col :span="5">
                  <el-form-item label="Số điện thoại người nhận">
                    <span>
                      {{ diagnosis.recipientPhone }}
                    </span>
                  </el-form-item>
                </el-col>
                <el-col :span="15">
                  <el-form-item label="Địa chỉ người nhận">
                    <span>
                      {{ diagnosis.province + diagnosis.market + diagnosis.distinguish+diagnosis.recipientAddress }}
                    </span>
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
                  :key="item"
                  :label="item.title"
                  :name="item"
                >
                  <span slot="label">
                    <!-- <el-checkbox :disabled="isReadOnly"></el-checkbox>  -->
                    {{ item.title }}
                  </span>
                  <el-row style="padding-top:8px" type="flex" justify="space-between" align="center"
                          v-if="chargeStatusActiveName === '1' && payState">
                    <div class="single">
                      Chi phí của đơn thuốc này：{{ item.content.recipelInfo.fee }}VNĐ
                    </div>
                    <el-popconfirm
                      title="Bạn có chắc muốn thu phí không？"
                      @confirm="singleCharge(1)"
                      style="padding-top:7px"
                    >
                      <el-button v-if="!isRetail && !diagnosisPrescription" style="height:30px" slot="reference" type="primary" plain>Thu phí đơn thuốc này</el-button>

                    </el-popconfirm>
                  </el-row>
                  <el-row type="flex" justify="space-between" align="center" style="padding-top:8px"
                          v-if="chargeStatusActiveName === '2' && medicalEditTabs.length>0">
                    <div class="single">
                      Chi phí của đơn thuốc này：{{ item.content.recipelInfo.amountReceivedTotal }}VNĐ
                    </div>
                    <el-popconfirm
                      title="Bạn có chắc muốn hoàn phí không？"
                      @confirm="singleCharge(2)"
                      style="padding-top:7px"
                    >
                      <el-button style="height:30px" slot="reference" type="primary" plain>Hoàn phí đơn thuốc này</el-button>

                    </el-popconfirm>
                  </el-row>
                  <el-row style="padding-top:8px" v-if="chargeStatusActiveName === '3' && medicalEditTabs.length>0">
                    <div class="single">
                      Chi phí của đơn thuốc này：{{ item.content.recipelInfo.amountReceivedTotal }}VNĐ
                    </div>
                  </el-row>
                  <div v-if="item.type!=='recipelType_2'">
                    <el-row>
                      <el-divider content-position="left">Thông tin đơn thuốc</el-divider>

                      <el-input
                        disabled
                        v-if="item.type!=='recipelType_3'"
                        :value="item.content.recipelInfo.smallType.name"
                        style="width:100px">
                      </el-input>
                      <el-checkbox v-if="item.type === 'recipelType_0'" style="margin-left: 20px;"
                                   v-model="item.content.recipelInfo.chronicDisease" disabled>Có phải bệnh mạn tính không
                      </el-checkbox>
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
                        label="Tên thuốc"
                        width="width">
                        <template slot-scope="scope">
                          {{ scope.row.drugStuffId.name }}
                          <div v-if="item.type==='recipelType_0'">
                        <span class="nameStyle"
                        >{{
                            scope.row.drugStuffId.drug.dosis
                          }}{{ scope.row.drugStuffId.drug.dosisUnit.name }} *
                          {{
                            scope.row.drugStuffId.drug.preparation
                          }}{{ scope.row.drugStuffId.drug.preparationUnit.name }}/{{
                            scope.row.drugStuffId.drug.pack.name
                          }}</span
                        >
                          </div>
                        </template>
                      </el-table-column>
                      <el-table-column
                        prop="drugStuffId.drug.factory.name"
                        label="Nhà sản xuất"
                        width="width"
                        v-if="!isRetail && !diagnosisPrescription"
                      >
                      </el-table-column>

                      <el-table-column
                        prop="westernMedicineUse.name"
                        label="Cách dùng"
                        width="width"
                        v-if="item.type === 'recipelType_0' && !diagnosisPrescription"
                      >
                      </el-table-column>
                      <el-table-column
                        prop="singleDosage"
                        label="Liều dùng mỗi lần"
                        width="width"
                        v-if="item.type!=='recipelType_3'"
                      >
                        <template slot-scope="scope">
                          <template>{{
                              scope.row.singleDosage ? scope.row.singleDosage + '' + scope.row.drugStuffId.dosisUnit.name : ""
                            }}
                          </template>
                        </template>
                      </el-table-column>
                      <el-table-column
                        prop="frequency"
                        label="Tần suất"
                        align="center"
                        v-if="item.type === 'recipelType_0' && !diagnosisPrescription"
                        width="width"
                      >
                        <template slot-scope="scope"
                        >{{
                            scope.row.infuseUse.name
                          }}{{ scope.row.frequency.name }}
                        </template
                        >
                      </el-table-column>
                      <el-table-column prop="days.name" label="Số ngày" width="width" align="center"
                                       v-if="item.type === 'recipelType_0'">
                        <template slot-scope="scope">
                          {{ scope.row.days.name }}ngày
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
                      <el-table-column prop="allFee" width="width" label="Số tiền phải trả">
                          <template slot-scope="scope">
                            {{ (scope.row.allFee).toFixed(2) }}VNĐ
                          </template>
                      </el-table-column>
                      <el-table-column prop="actualPayment" width="width" label="Số tiền thực trả">
                          <template slot-scope="scope" v-if="chargeStatusActiveName !== '1'">
                            {{ (scope.row.actualPayment) == null ? (scope.row.allFee).toFixed(2) : (scope.row.actualPayment).toFixed(2)}}VNĐ
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
                     <el-input style="width:80px" disabled v-model="item.content.recipelInfo.singleDosage">
                     </el-input>{{'格'}}
                   </div>-->
                    <el-row v-if="item.type==='recipelType_1'">
                      <el-input disabled v-model="item.content.recipelInfo.dosage"
                                oninput="value=value.replace(/[^\d.]/g,'')" style="width: 60px"></el-input>
                      &nbsp;thang &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Cách dùng：
                      <el-select disabled v-model="item.content.recipelInfo.recipelUse" placeholder="Vui lòng chọn"
                                 style="width: 110px">
                        <el-option v-for="pitem in ChineseUseOption" :key="pitem.value"
                                   :label="pitem.name"
                                   :value="{ name: pitem.name, value: pitem.value }">
                        </el-option>
                      </el-select>
                      <el-select disabled v-model="item.content.recipelInfo.frequency" placeholder="Vui lòng chọn"
                                 style="width: 110px">
                        <el-option v-for="pitem in ChineseTimeOption" :key="pitem.value"
                                   :label="pitem.name"
                                   :value="{ name: pitem.name, value: pitem.value }">
                        </el-option>
                      </el-select>
                      <el-select disabled v-model="item.content.recipelInfo.takeFrequency" placeholder="Vui lòng chọn"
                                 style="width: 110px">
                        <el-option v-for="pitem in JSON.parse(
                            JSON.stringify(FrequencyOption)
                          )" :key="pitem.value" :label="pitem.name" :value="{
                            name: pitem.name,
                            value: pitem.value,
                          }">
                        </el-option>
                      </el-select>
                      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; một lần&nbsp;<el-input disabled
                                                                             v-model="item.content.recipelInfo.singleDosage"
                                                                             style="width: 60px"></el-input>&nbsp;
                      <span>ml</span>
                    </el-row>
                  </div>

                  <div v-else style="margin-left:-10px;">
                    <div v-for="(items,index) in item.infusion.zushu" :key="index"
                         style="border:1px solid #DCDCDC;padding:10px;margin:10px;">

                      <div>
                        <span
                          style="float:left;height:25px;line-height:25px;font-size:14px;">&nbsp;&nbsp;thứ{{
                            index + 1
                          }}nhóm</span>
                        <span style="float:right;height:25px;line-height:25px;font-size:14px;">&nbsp;&nbsp;giọt/Phút</span>

                        <el-input
                          style="float:right;width:10%"
                          :value="item.infusion.drippingSpeed[index]"
                          placeholder=""
                          :disabled="
                               true
                              "
                        >

                        </el-input>

                        <span
                          style="float:right;height:25px;line-height:25px;font-size:14px;">&nbsp;&nbsp;ngày&nbsp;&nbsp;</span>
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

                        <span style="float:right;height:25px;line-height:25px;font-size:18px;">&nbsp;&nbsp;</span>
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

                        <span style="float:right;height:25px;line-height:25px;font-size:18px;">&nbsp;&nbsp;</span>
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
                        <!-- <el-table-column label="Số nhóm" prop="infuseGroup" width="width">

                        </el-table-column> -->
                        <el-table-column label="Số thứ tự" type="index" align="center" width="width">
                        </el-table-column>
                        <el-table-column
                          prop="drugStuffId.name"
                          label="Tên thuốc"
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
                        <el-table-column
                          prop="singleDosage"
                          label="Liều dùng mỗi lần"
                          width="width"
                        >
                          <template slot-scope="scope">
                            <template>{{
                                scope.row.singleDosage + '' + scope.row.drugStuffId.dosisUnit.name
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
                        <el-table-column prop="allFee" label="Số tiền phải trả" width="width">
                          <template slot-scope="scope">
                            {{ (scope.row.allFee).toFixed(2) }}VNĐ
                          </template>
                        </el-table-column>
                        <el-table-column prop="actualPayment" label="Số tiền thực trả" width="width">
                          <template slot-scope="scope">
                            {{ (scope.row.actualPayment).toFixed(2) }}VNĐ
                          </template>
                        </el-table-column>

                      </el-table>
                    </div>
                  </div>
                  <el-divider style="margin-top: 5px" content-position="left">Phí phụ thêm</el-divider>
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
                    <el-table-column prop="allFee" label="Số tiền phải trả" width="width">
                      <template slot-scope="scope">
                        {{ (scope.row.allFee).toFixed(2) }}VNĐ
                      </template>
                    </el-table-column>
                    <el-table-column prop="actualPayment" label="Số tiền thực trả" width="width">
                      <template slot-scope="scope">
                        {{ (scope.row.actualPayment = null ? 0 : scope.row.actualPayment).toFixed(2) }}VNĐ
                      </template>
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
      width="50%"
      :close-on-click-modal="false"
      :before-close="chargeFormCancel"
    >
      <div>
        <el-divider></el-divider>
        <div id="chargeTotalFee_Container">
          <div id="chargeTotalFee_Title">Số tiền thực thu（VNĐ）：</div>
          <div id="chargeTotalFee_Fee" v-if="this.chargeForm.amountReceived && !diagnosisPrescription">
            {{ this.chargeForm.amountReceived }}VNĐ
          </div>
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <div id="chargeTotalFee_Title">Số tiền phải thu（VNĐ）：</div>
          <div id="chargeTotalFee_Fee" v-if="this.chargeForm.amountReceivable">
            {{ this.chargeForm.amountReceivable }}VNĐ
          </div>
        </div>
        <el-form ref="chargeForm" :rules="rules" :model="chargeForm" label-width="80px">
          <el-row>

            <el-col :span="8" v-if="!diagnosis">
              <el-form-item label="Chiết khấu">
                <el-input
                  v-model="chargeForm.discount"
                  @input="GetAmountReceived"
                  placeholder="Chiết khấu"
                >
                  <template slot="append">折</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8" :offset="1" v-if="!diagnosis">
              <el-form-item label="Số tiền chiết khấu">
                <el-input
                  v-model="amountOfRemission"
                  placeholder="Số tiền chiết khấu"
                  disabled
                >
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8" :offset="-1" v-if="!diagnosis">
              <el-form-item label="Số tiền miễn giảm">
                <el-input
                  v-model="reductionAmount"
                  placeholder="Số tiền miễn giảm"
                  @input="GetAmountReceived"
                >
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col>
            <!-- <el-col :span="10" :offset="1">
              <el-form-item label="Số tiền miễn giảm">
                <el-input v-model="chargeForm.amountReceivable">
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col> -->
          </el-row>
          <el-row v-if="chargeForm.paymentType==='payType_0'">
            <el-col :span="8">
              <el-form-item label="Số tiền thực trả" prop="amountPaidRule">
                <el-input
                  v-model="amountPaid"
                  @input=" inputAmountPaid"
                  placeholder="Số tiền thực trả">
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8" :offset="1">
              <el-form-item label="Số tiền trả lại">
                <el-input
                  v-model="amountOfChange"
                  placeholder="Số tiền trả lại"
                  disabled
                >
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="20">
              <el-form-item label="Phương thức thanh toán">
                <!--<el-radio-group v-model="chargeForm.paymentType">
                  <el-radio label="payType_0">Tiền mặt</el-radio>
                  <el-radio label="payType_1">支付宝</el-radio>
                  <el-radio label="payType_2">WeChat</el-radio>
                  <el-radio label="payType_3">银行卡</el-radio>
                </el-radio-group>-->
                <el-radio-group
                  v-model="chargeForm.paymentType"
                  class="pay-registration-content"
                >
                  <el-radio
                    v-for="ptype in payType_List"
                    :key="ptype.value"
                    :label="ptype.value"
                  >{{ ptype.name }}
                  </el-radio
                  >
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="Ghi chú">
                <el-input type="textarea" :rows="3" v-model="chargeForm.meno"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <el-divider></el-divider>
      </div>
      <div slot="footer">
        <el-button @click="chargeFormCancel">Hủy</el-button>
        <el-button type="primary" @click="submit">Xác nhận thu phí</el-button>
      </div>
    </el-dialog>
    <el-dialog
      title="单处方收费"
      :visible.sync="singleDialogVisible"
      width="50%"
      :close-on-click-modal="false"
      :before-close="singleFormCancel"
    >
      <div>
        <el-divider></el-divider>
        <div class="chargeTotalFee_Container">
          <div class="chargeTotalFee_Title">Số tiền thực thu（VNĐ）：</div>
          <div class="chargeTotalFee_Fee" v-if="this.singleForm.amountReceived">
            {{ this.singleForm.amountReceived }}VNĐ
          </div>
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <div id="chargeTotalFee_Title">Số tiền phải thu（VNĐ）：</div>
          <div id="chargeTotalFee_Fee" v-if="this.singleForm.amountReceivable">
            {{ this.singleForm.amountReceivable }}VNĐ
          </div>
        </div>
        <el-form ref="singleForm" :model="singleForm" label-width="80px">
          <el-row>

            <el-col :span="8">
              <el-form-item label="Chiết khấu">
                <el-input
                  v-model="singleForm.discount"
                  @input="GetSingleAmountReceived"
                  placeholder="Chiết khấu"
                >
                  <template slot="append">折</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8" :offset="1">
              <el-form-item label="Số tiền miễn giảm">
                <el-input
                  v-model="signleAmountOfRemission"
                  placeholder="Số tiền miễn giảm"
                  disabled
                  @input="GetSingleAmountReceived"
                >
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col>
            <!-- <el-col :span="10" :offset="1">
              <el-form-item label="Số tiền phải thu">
                <el-input v-model="singleForm.amountReceivable">
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col> -->
          </el-row>
          <el-row v-if="chargeForm.paymentType==='payType_0'">
            <el-col :span="8">
              <el-form-item label="Số tiền thực trả">
                <el-input
                  v-model="amountPaid"
                  @input=" inputAmountPaid"
                  placeholder="Số tiền thực trả"
                >
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8" :offset="1">
              <el-form-item label="Số tiền trả lại">
                <el-input
                  v-model="amountOfChange"
                  placeholder="Số tiền trả lại"
                  disabled
                >
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="15">
              <el-form-item label="Phương thức thanh toán">
                <!--<el-radio-group v-model="singleForm.paymentType">
                  <el-radio label="payType_0">Tiền mặt</el-radio>
                  <el-radio label="payType_1">支付宝</el-radio>
                  <el-radio label="payType_2">WeChat</el-radio>
                  <el-radio label="payType_3">银行卡</el-radio>
                </el-radio-group>-->
                <el-radio-group
                  v-model="chargeForm.paymentType"
                  class="pay-registration-content"
                >
                  <el-radio
                    v-for="ptype in payType_List"
                    :key="ptype.value"
                    :label="ptype.value"
                  >{{ ptype.name }}
                  </el-radio
                  >
                </el-radio-group>
              </el-form-item>

            </el-col>
          </el-row>
          <el-row v-if="member.length>0&&this.medicalEditTabsValue.type==='recipelType_3'">
            <el-col :span="24">
              <el-form-item label="Thẻ hội viên">
                <el-select
                  v-model="singleForm.member"
                  value-key="id"
                  filterable
                  style="width:300px"
                  allow-create
                  placeholder="Vui lòng chọn thẻ thành viên"
                  @change="chooseMember"
                >
                  <el-option
                    v-for="item in member"
                    :key="item.id"
                    :label="item.memberSet.name"
                    :value="item"
                  >
                    <el-row>
                      <el-col :span="8">
                        <div><span style="float: left">{{ item.memberSet.name }}</span></div>
                      </el-col>
                      <el-col :span="8">
                        <div><span

                        >{{ item.memberSet.type.name }}</span
                        ></div>
                      </el-col>
                      <el-col :span="8">
                        <div>
                     <span
                     >{{ item.card }}</span
                     >
                        </div>
                      </el-col>
                    </el-row>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="Ghi chú">
                <el-input type="textarea" :rows="3" v-model="singleForm.meno"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <el-divider></el-divider>
      </div>
      <div slot="footer">
        <el-button @click="singleFormCancel">Hủy</el-button>
        <el-button type="primary" @click="singleChargeBtn(1)">Xác nhận thu phí</el-button>
      </div>
    </el-dialog>
    <el-dialog
      title="添加收件人信息"
      :visible.sync="addresseeDialogVisible"
      width="50%"
      :close-on-click-modal="false"
      :before-close="chargeFormCancel"
      v-if="diagnosis !== null"
    >
      <div>
        <el-form ref="diagnosis" :rules="rules" :model="diagnosis" label-width="100px">
          <el-row>
            <el-col :span="12">
              <el-form-item label="Họ tên người nhận">
                <el-input :rows="3" v-model="diagnosis.recipientName"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <el-form-item label="Số điện thoại người nhận">
                <el-input :rows="3" v-model="diagnosis.recipientPhone"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <!--<el-form-item label='Khu vực' prop=''>
                <v-distpicker :province="province" :city="city" :area="area"
                              @province="onChangeProvince" @city="onChangeCity" @area="onChangeArea"></v-distpicker>
              </el-form-item>-->
              <el-form-item label="Địa chỉ người nhận">
                所属省：
                <el-input  :rows="3" v-model="diagnosis.province"></el-input>
                所属市：
                <el-input  :rows="3" v-model="diagnosis.market"></el-input>
                所属镇/Quận/Huyện：
                <el-input  :rows="3" v-model="diagnosis.distinguish"></el-input>
                Địa chỉ chi tiết：
                <el-input  :rows="3" v-model="diagnosis.recipientAddress"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <el-divider></el-divider>
      </div>
      <div slot="footer">
        <el-button @click="chargeFormCancel">Hủy</el-button>
        <el-button type="primary" @click="addresseeSave">Lưu</el-button>
      </div>
    </el-dialog>
  </el-container>
</template>

<script>
  import {listDrugPage, listAllStock} from "@/api/stock/drug";
  import {saveTollTollInfo} from "@/api/toll/tollInfo";
  import {listRegistrationPages} from "@/api/outpatient/registration";
  import {getMember, getByPatientId} from "@/api/member/memberManagement"
  import {
    listDiagnosisPage,
    saveDiagnosis,
    chargeState,
    getRegistrationId,
    modifiedState
  } from '@/api/outpatient/remoteDiagnosisTreatment'
  import {getPatientById, listPatientAll} from "@/api/outpatient/patient";
  import {getRecipelInfoById} from "@/api/outpatient/recipelInfo";
  import {
    allSaveMedicalRecord,
    allQueryMedicalRecord,
    getMedicalRecordById,
    listMedicalRecordPage,
  } from "@/api/outpatient/medicalRecord";
  import {getDictItemsByCode, DICT_CODE} from "@/utils/dictCache";
  import BaseUI from "@/views/components/baseUI";
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
    extends: BaseUI,
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
      let amountPaidRule = (rule, value, callback) => {
        //这里为什么不用 value,因为之前 写这个代码的人没有按照element-ui的规则写,他绑定的数据不是表单里面的，气人啊
        if (!this.amountPaid) {
          callback(new Error("请输入实付金额"));
        } else {
          if (Number(this.amountPaid) < Number(this.chargeForm.amountReceived)) {
            callback(new Error("实付金额不能低于实收金额"));
          } else {
            callback();
          }
        }
      };
      return {
        //Liên kết ba cấp tỉnh/thành phố/quận/huyện
        province:"",
        market:"",
        distinguish:"",

        hisPatient: null, //hisThông tin hội chẩn bệnh nhân
        tokenData: null, // hisToken
        orderForm: null, // 订单信息
        hisCheck: null, // hisHóa đơn chờ thanh toán
        hisCheckDetail: null, // hisChi tiết hóa đơn chờ thanh toán
        diagnosisPrescription: false, // 是否远程诊疗处方
        prescriptionCollection: [], // 远程诊疗处方集合

        isBaseReadOnly: true,
        isRetail: false, //Có bán lẻ không
        selectCondition: "", //Điều kiện truy vấn bệnh nhân
        chargeStatusActiveName: "1", //Tab trạng thái thu phí
        addresseeDialogVisible: false,
        chargeDialogVisible: false,
        singleDialogVisible: false,
        chargeTotalFee: 100,
        chargeForm: {
          amountReceivable: 0,
          amountDiscounted: 0,
          discount: 10,
          amountReceived: 0,
          paymentType: "payType_0",
          meno: "",
          amountPaid: 0,
        },
        singleForm: {
          amountReceivable: 0,
          amountDiscounted: 0,
          discount: 10,
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
          chargeStatus: "",
          dispensionStatus: 3,
          patientName: null,
          patientCode: null,
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
          card: "",
          westernDiagnose: "Không có",
          chinaDiagnose: "Không có",
        },
        patientInfoNoRules: null,
        patientInfoRules: {
          name: [
            {required: true, message: "Vui lòng nhập họ tên", trigger: "blur"},
            {min: 1, max: 50, message: "Độ dài trong khoảng1đến50ký tự", trigger: "blur"},
          ],
          gender: [{required: true, message: "Vui lòng chọn giới tính", trigger: "blur"}],
          age: [{required: true, message: "Vui lòng nhập tuổi", trigger: "blur"}],
          month: [{required: true, message: "Vui lòng nhập tuổi (tháng)", trigger: "blur"}],
          phone: [
            {required: true, message: "Vui lòng nhập số điện thoại liên hệ", trigger: "blur"},
            {min: 1, max: 20, message: "Độ dài trong khoảng1-20ký tự", trigger: "blur"},
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
        hisTotalMoney: 0, //远程诊疗总金额
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
          patientName: null,
          patientCode: null,
        },
        defaultNumber: 1,//Số nhóm mặc định
        infusionProject: [[]],
        drippingSpeed: [""],  //Tốc độ truyền
        days: [{}],    //Số ngày
        frequency: [{}],   //Tần suất
        infuseUse: [{}],     //Cách dùng
        zushu: [1],
        excharge: [],  //Phí phụ thêm truyền dịch
        overColor: 0,
        member: [],   //Thẻ hội viên
        allMember: [],  //Tất cả thẻ thành viên
        diagnosis: null,  //远程诊疗信息 Chẩn đoán
        memberCopy: [],  //本次使用次数暂存
        saveMember: {},  //保存会员体验卡
        amountOfRemission: "0",  //Số tiền chiết khấu
        reductionAmount:'0',   // Số tiền miễn giảm
        signleAmountOfRemission: "0", //Thu phí riêng

        amountPaid: 0, //Số tiền thực trả
        amountOfChange: 0,//Số tiền trả lại

        payType_List: [], // Phương thức thanh toán

        //Danh sách thả xuống cách dùng thuốc Đông y
        ChineseUseOption: [],
        ChineseTimeOption: [],
        ChineseUseTimeOption: [],
        //Phòng khám đặc biệt
        isSpecial: false,
        //Danh sách thả xuống tần suất thuốc Tây y
        FrequencyOption: [],
        rules: {
          amountPaidRule: [
            {
              required: true,
              trigger: "blur",
              validator: amountPaidRule,
            },
          ],
        },
      };
    },
    computed: {
      // Lấy giá trị liên kết ba cấp tỉnh/thành phố/quận/huyện
      onChangeProvince(data) {
        this.province = data.value

      },
      onChangeCity(data) {
        this.city = data.value

      },
      onChangeArea(data) {
        this.area = data.value
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
        this.overColor = 0
      },
    },
    methods: {

      // Lấy phòng khám từ xatoken
      async getToken() {
        await this.$axios.post('/token/Auth/GetAppFrienAuthToken', loginForm, config)
          .then((response) => {
            this.tokenData = response.data.BusData.data.Token
          })
          .catch((error) => {
          });
      },

      // hisThông tin hội chẩn bệnh nhân
      async inquireHis(){
        await this.getToken()
        await this.$axios.post('apis/RemoteTreated/TreatedApplyInfo', {
          Data: {
            sqlsh: this.diagnosis.id // Số thứ tự đề nghị
          },
          OrganizeId: this.diagnosis.hospitalId,
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
      },

      // 地址新增
      async PatientAddressAdd(){
        await this.getToken()
        await this.$axios.post('apis/patient/PatientAddressAdd', {
          Data: {
            Id: this.diagnosis.id, // Địa chỉid
            patid: this.hisPatient.patid, // Bệnh nhânid
            OrganizeId: this.diagnosis.hospitalId, // Cơ sở
            xm: this.diagnosis.recipientName, // Họ tên người nhận
            dh: this.diagnosis.recipientPhone, // Số điện thoại người nhận
            xian_sheng: this.diagnosis.province, // Tỉnh/Thành phố người nhận
            xian_shi: this.diagnosis.market, // Thành phố người nhận
            xian_xian: this.diagnosis.distinguish, // Huyện người nhận
            xian_dz: this.diagnosis.recipientAddress // Địa chỉ chi tiết người nhận
          },
          OrganizeId: this.diagnosis.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
          })
          .catch((error) => {
          });
      },

      // 地址修改
      async PatientAddressUpdate(){
        await this.getToken()
        await this.$axios.post('apis/patient/PatientAddressUpdate', {
          Data: {
            Id: this.diagnosis.id, // Địa chỉid
            patid: this.hisPatient.patid, // Bệnh nhânid
            OrganizeId: this.diagnosis.hospitalId, // Cơ sở
            xm: this.diagnosis.recipientName, // Họ tên người nhận
            dh: this.diagnosis.recipientPhone, // Số điện thoại người nhận
            xian_sheng: this.diagnosis.province, // Tỉnh/Thành phố người nhận
            xian_shi: this.diagnosis.market, // Thành phố người nhận
            xian_xian: this.diagnosis.distinguish, // Huyện người nhận
            xian_dz: this.diagnosis.recipientAddress // Địa chỉ chi tiết người nhận
          },
          OrganizeId: this.diagnosis.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
          })
          .catch((error) => {
          });
      },

      // hisHóa đơn chờ thanh toán
      async pendingBill(){
        this.hisTotalMoney = 0;
        await this.getToken()
        await this.$axios.post('apis/Outpatient/OutpBillUnpaidByMzh', {
          Data: {
            kh: this.hisPatient.kh, // Số thẻ
            mzh: this.hisPatient.mzh // Số khám ngoại trú
          },
          OrganizeId: this.diagnosis.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
            this.hisCheck = response.data.BusData.data
            for (let i = 0; i < this.hisCheck.length; i++) {
              this.prescriptionCollection[this.hisCheck[i].PresId]
              this.hisTotalMoney += this.hisCheck[i].PresAmt;
            }
          })
          .catch((error) => {
          });
      },

      // hisChi tiết hóa đơn chờ thanh toán
      async billingDetail(presIds){
        await this.getToken()
        await this.$axios.post('apis/Outpatient/OutpBillItemDetail', {
          Data: {
            PresIds: this.prescriptionCollection, // Đơn thuốcid集合
            mzh: this.hisPatient.mzh // Số khám ngoại trú
          },
          OrganizeId: this.diagnosis.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
            this.hisCheckDetail = response.data.BusData.data
            /*for (let i = 0; i < this.hisCheckDetail.length; i++) {
              this.hisTotalMoney += this.hisCheckDetail[i].Amt;
            }*/
          })
          .catch((error) => {
          });
      },

      // 查询订单
      async orderQuery(){
        await this.getToken();
        await this.$axios.post('apis/PayOrder/OrderQuery', {
          Data: {
            OrderNo: "", // Số đơn hàng
            OrderType: 1, // Loại đơn hàng
            CardNo: this.hisPatient.kh, //Số thẻ khám bệnh
            VisitNo: this.hisPatient.mzh,  // Số khám ngoại trú
            OrderStu: null // 订单状态
          },
          OrganizeId: this.diagnosis.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
            this.orderForm = response.data.BusData.data[0]
          })
          .catch((error) => {
          });
      },

      // 创建订单
      async createOrder(){
        await this.getToken()
        await this.$axios.post('apis/PayOrder/BillOrderCreate', {
          Data: {
            kh: this.hisPatient.kh, // Số thẻ
            mzh: this.hisPatient.mzh, // Số khám ngoại trú
            OrderType: 1, // 1 为门诊订单 2为住院订单
            addressInfo: {
              patid: this.hisPatient.patid, // Bệnh nhânid
              OrganizeId: this.diagnosis.hospitalId, // Cơ sở
              xm: this.diagnosis.recipientName, // Họ tên người nhận
              dh: this.diagnosis.recipientPhone, // Số điện thoại người nhận
              xian_sheng: this.diagnosis.province, // Tỉnh/Thành phố người nhận
              xian_shi: this.diagnosis.market, // Thành phố người nhận
              xian_xian: this.diagnosis.distinguish, // Huyện người nhận
              xian_dz: this.diagnosis.recipientAddress // Địa chỉ chi tiết người nhận
            }
          },
          OrganizeId: this.diagnosis.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
            this.orderForm = response.data.BusData.data
          })
          .catch((error) => {
          });

        await this.$axios.post('apis/PayOrder/BillOrderLockApply', {
          Data: {
            kh: this.hisPatient.kh, // Số thẻ
            OrderNo: this.orderForm.OrderNo, // Số đơn hàng
            OrderAmt: this.orderForm.OrderAmt // Số tiền đơn hàng
          },
          OrganizeId: this.diagnosis.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
          })
          .catch((error) => {
          });
      },

      // 锁定订单
      async lockedOrder(){
        await this.getToken();
        await this.$axios.post('apis/PayOrder/BillOrderLockApply', {
          Data: {
            kh: this.hisPatient.kh, // Số thẻ
            OrderNo: this.orderForm.OrderNo, // Số đơn hàng
            OrderAmt: this.orderForm.OrderAmt // Số tiền đơn hàng
          },
          OrganizeId: this.diagnosis.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
          })
          .catch((error) => {
          });
      },

      // 订单结算
      async orderSettlement(){
        await this.getToken();
        await this.$axios.post('apis/PayOrder/BillOrderPay', {
          Data: {
            CardNo: this.hisPatient.kh, // Số thẻ khám bệnh
            OrderNo: this.orderForm.OrderNo, // Số đơn hàng
            PayWay: "12", // Phương thức thanh toán(门诊支付)
            PayFee: this.chargeForm.amountReceivable, // Phí thanh toán
            PayLsh: this.diagnosis.registrationId, // 支付流水号(使用云诊所挂号id)
            PayerId: this.hisPatient.patid, // 支付者身份标识（hisBệnh nhânid）

            PayTime: new Date(), // 支付时间

            OrderType: 1 // Loại đơn hàng
          },
          OrganizeId: this.diagnosis.hospitalId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        }, {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
          })
          .catch((error) => {
          });
      },


      singleFormCancel() {
        this.singleDialogVisible = false
        this.singleForm.discount = 10
        this.signleAmountOfRemission = 0
        this.amountPaid = 0
        this.amountOfChange = 0

      },

      //Hủy thu phí toàn bộ
      chargeFormCancel() {
        this.addresseeDialogVisible = false
        this.selectPatientChange()
        this.chargeDialogVisible = false
        this.chargeForm.discount = 10
        this.reductionAmount = 0
        this.amountPaid = 0
        this.amountOfRemission = 0
        this.amountOfChange = 0
        this.$refs["chargeForm"].resetFields();
      },

      //Tính số tiền trả lại khi thanh toán tiền mặt
      inputAmountPaid() {
        this.amountOfChange = 0
        let amount = this.amountPaid ? this.amountPaid : 0
        if (amount > 0) {
          if (this.singleForm.amountReceived > 0) {
            this.amountOfChange = (this.amountPaid - this.singleForm.amountReceived).toFixed(2)
          } else {

            this.amountOfChange = (this.amountPaid - this.chargeForm.amountReceived).toFixed(2)
          }
        }
      },
      //选择体验卡进行扣减
      chooseMember(row) {
        this.saveMember = {};
        let memberCopy = JSON.parse(JSON.stringify(this.memberCopy))
        if (memberCopy) {
          for (let i = 0; i < memberCopy.length; i++) {
            if (memberCopy[i].id === row.id) {
              row = memberCopy[i]
            }
          }
        }
        this.singleForm.amountReceived = this.singleForm.amountReceivable
        for (let i = 0; i < this.medicalEditTabsValue.content.recipelDetailEvtList.length; i++) {
          for (let j = 0; j < row.memberItems.length; j++) {
            if (row.memberItems[j].costItem.id === this.medicalEditTabsValue.content.recipelDetailEvtList[i].drugStuffId.costItem.id) {
              for (let m = 0; m < row.memberManagementDetails.length; m++) {
                if (row.memberManagementDetails[m].number > row.memberManagementDetails[m].useNumber && row.memberManagementDetails[m].memberItem.id === row.memberItems[j].id) {
                  let recipelDetailNum = this.medicalEditTabsValue.content.recipelDetailEvtList[i].total
                  let memberNum = row.memberManagementDetails[m].number - row.memberManagementDetails[m].useNumber
                  if (memberNum >= recipelDetailNum) {
                    row.memberManagementDetails[m].useNumber += recipelDetailNum
                    this.singleForm.amountReceived = (this.singleForm.amountReceived - this.medicalEditTabsValue.content.recipelDetailEvtList[i].allFee).toFixed(2)
                  } else {
                    row.memberManagementDetails[m].useNumber += memberNum
                    this.singleForm.amountReceived = (this.singleForm.amountReceived - (this.medicalEditTabsValue.content.recipelDetailEvtList[i].drugStuffId.costItem.salePrice * memberNum)).toFixed(2)
                  }
                }
              }
            }
          }
        }
        this.saveMember = row

      },
      clickMedicalEditTab() {
        if (this.medicalEditTabsValue === this.medicalClickTabsValue) {
          return;
        }
        this.medicalClickTabsValue = this.medicalEditTabsValue;
        let num = 0
        for (let i = 0; i < this.medicalEditTabs.length; i++) {
          if (this.medicalEditTabs[i].key === this.medicalEditTabsValue.key) {
            num = i
          }
        }
        this.AlreadyPatientDescriptionsQuery(this.medicalClickTabsValue.content, num)
      },
      AlreadyPatientDescriptionsQuery(item, index) {
        this.medicalEditTabs.forEach((element) => {
          if (element.content.uuid === item.uuid) {
            this.medicalEditTabsValue = element;
            this.medicalClickTabsValue = this.medicalEditTabsValue;
          }
        });

        this.recipelInfoId = item.id;
        this.overColor = index
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
          this.allMember = []
        } else if (this.chargeStatusActiveName === "2") {
          this.allMember = []
          this.getIsPayPatientList();
        } else {
          this.allMember = []
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
            "/ureport/preview?_u=Newtouch:charge.ureport.xml&_t=0" +
            str,
            index
          );
          arr[index].onload = () => {
            arr[index].document.title = "Hệ thống Quản lý Phòng khám"
          }
        });
      },
      pageInit() {
        this.selectPatientList();
        this.GetAllPatient();
        this.getIsPayPatientList();
        this.returnPayPatientList();
        this.initOptions()
      },
      initOptions() {
        getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_RECIPEL_USE).then((data) => {
          this.ChineseUseOption = data;
          if (this.isSpecial) {
            this.ChineseUseOption = this.ChineseUseOption.filter(
              (item) => item.name === "水冲"
            );
          }
        });
        getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_RECIPEL_FREQUENCY).then((data) => {
          this.ChineseTimeOption = data;
        });
        getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_RECIPEL_TAKE_FREQUENCY).then((data) => {
          this.FrequencyOption = data;
        });
        getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_USE).then((data) => {
          this.ChineseUseTimeOption = data;
        });
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
      singleCharge(type) {
        if (type === 1) {
          this.IsSingleCorrect();
          this.singleForm.member = ""
          //如果单独收费时为诊疗项目，判断其项目是否存在体验项目
          if (this.medicalEditTabsValue.type === "recipelType_3") {
            let recipelInfoEvt = {
              recipelInfo: this.medicalEditTabsValue.content.recipelInfo,
              recipelDetailEvtList: this.medicalEditTabsValue.content.recipelDetailEvtList
            }

            getMember(recipelInfoEvt).then(res => {
              if (res.code === 100) {
                this.member = res.data
                this.memberCopy = JSON.parse(JSON.stringify(res.data))
              } else {
                this.showMessage(res)
              }
            }).catch((error) => {
              this.outputError(error)
            })
          }
          this.singleDialogVisible = true
          this.chargeForm.amountReceived = 0
        } else {
          this.singleChargeBtn(2)
        }
      },
      // 添加收费人信息
      addressee() {
        this.addresseeDialogVisible = true
      },
      // 收件人信息保存
      async addresseeSave() {
        /* this.diagnosis.province = this.province;
         this.diagnosis.market = this.city;
         this.diagnosis.distinguish = this.area;*/

        await modifiedState(this.diagnosis).then(res => {
          if (res.code === 100) {
            this.addresseeDialogVisible = false
            this.$message.success("Thêm thông tin người nhận thành công!")
            // 发起订单锁定
            // this.lockedOrder();
          } else {
            this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！")
          }
        }).catch()

        await this.$axios.post('apis/patient/PatientOrderAddressQuery',{
            Data: {
              visitNo: this.hisPatient.mzh, // Số khám ngoại trú
              OrganizeId: this.diagnosis.hospitalId,
              ks: "00000080" // Khoa
            },
            OrganizeId: this.diagnosis.hospitalId,
            AppId: "Oh_Newtouch_Clinic",
            Timestamp: new Date()
        },
        {
          headers: {
            Authorization: "Bearer " + this.tokenData
          }
        })
          .then((response) => {
            if(response.data.BusData.code === 10000){
              this.PatientAddressUpdate()
            }else if(response.data.BusData.code !== 10000) {
              // 创建订单并锁定
              this.createOrder();
              // this.PatientAddressAdd()
            }
          })
          .catch((error) => {
          });
      },
      /*Nhấn nút thu phí/hoàn phí*/
      chargeClick(type) {
        if (this.diagnosisPrescription){
          this.chargeForm.amountReceived = this.hisTotalMoney.toFixed(2)
          this.chargeForm.amountReceivable = this.hisTotalMoney.toFixed(2)
        }
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
          if (this.payType_List.length > 0) {
            this.chargeForm.paymentType = this.payType_List[0].value;
          }
          this.singleForm.amountReceived = 0
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
        if (this.diagnosisPrescription){
          this.chargeForm.amountReceived = this.hisTotalMoney.toFixed(2)
          this.chargeForm.amountReceivable = this.hisTotalMoney.toFixed(2)
        }
      },
      getPayTypeList() {
        //Truy vấn loại thanh toán
        getDictItemsByCode(DICT_CODE.PAY_TYPE).then((data) => {
          this.payType_List = data;
        });
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
        //   if(res.code==="100"){
        //     if(res.data.rows[0].westernDiagnose!==undefined){
        //         this.patientInfoForm.westernDiagnose=res.data.rows[0].westernDiagnose
        //       }else{
        //         this.patientInfoForm.westernDiagnose="Không có"
        //       }
        //       if(res.data.rows[0].chinaDiagnose!==undefined){
        //         this.patientInfoForm.chinaDiagnose=res.data.rows[0].chinaDiagnose
        //       }else{
        //         this.patientInfoForm.chinaDiagnose="Không có"
        //       }
        //   }
        // }).catch(()=>{})


        //Theo bệnh nhânidLấy thông tin thành viên
        if (this.patientInfoRow.patientId) {
          getByPatientId(this.patientInfoRow.patientId.id).then(res => {
            if (res.code === 100) {
              this.allMember = res.data
            } else {
              this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！")
            }
          }).catch()
        }

        this.selRowChargeStatus = this.patientInfoRow.chargeStatus;

        this.getSaveInfo(this.patientInfoRow.id);

        // 获取患者远程诊疗信息（已注释）
        // this.diagnosis = null
        // getRegistrationId(this.patientInfoRow.id).then(res => {
        //   if (res.data.id !== null) {
        //     // this.diagnosis = res.data;
        //     // this.diagnosisPrescription = true;
        //     this.inquireHis();
        //     setTimeout(() => {
        //       this.pendingBill();
        //       }, 1000);
        //     // this.billingDetail(this.prescriptionCollection);

        //   } else {
        //     this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！")
        //   }
        // }).catch()

        this.overColor = 0

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
        this.patientQueryCondition.columnName = "reception_end_date";
        this.patientQueryCondition.order = "desc";
        this.patientQueryCondition.chargeStatus = 0;
        this.patientQueryCondition.updateDate = "2022-06-01 00:00:00";
        this.patientQueryCondition.status = "registrationStatus_1";
        if (this.selectCondition.trim()) {
          this.patientQueryCondition.patientName = this.selectCondition
          this.patientQueryCondition.patientCode = this.selectCondition
        } else {
          this.patientQueryCondition.patientName = ''
          this.patientQueryCondition.patientCode = ''
        }
        listRegistrationPages(this.patientQueryCondition)
          .then((responseData) => {
            if (responseData.code === 100) {
              if (responseData.data.rows != null) {
                this.patientList = responseData.data.rows.filter(
                  (item) => item.recipeStatus !== 1
                );
                let num = responseData.data.rows.length - this.patientList.length;
                if (this.chargeStatusActiveName === "1") {
                  this.patientTotalCount = responseData.data.total - num;
                } else {
                  this.isPayTotalCount = responseData.data.total - num;
                }
              } else {
                this.patientList = responseData.data.rows
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
        this.patientQueryCondition.columnName = "charge_date";
        this.patientQueryCondition.order = "desc";
        this.patientQueryCondition.chargeStatus = 1;
        this.patientQueryCondition.updateDate = "2022-06-01 00:00:00";
        this.patientQueryCondition.status = "registrationStatus_1";
        if (this.selectCondition.trim()) {
          this.patientQueryCondition.patientName = this.selectCondition
          this.patientQueryCondition.patientCode = this.selectCondition
        } else {
          this.patientQueryCondition.patientName = ''
          this.patientQueryCondition.patientCode = ''
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
          this.patientQueryCondition.patientName = this.selectCondition
          this.patientQueryCondition.patientCode = this.selectCondition
        } else {
          this.patientQueryCondition.patientName = ''
          this.patientQueryCondition.patientCode = ''
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
                (item) => item.recipelInfo.chargeStatus === 0
              );
            } else if (this.chargeStatusActiveName === "2") {
              this.prescriptionMainList = this.prescriptionMainList.filter(
                (item) => item.recipelInfo.chargeStatus === 1
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
          if (element.recipelInfo.recipelType.value === "recipelType_2") {
            let infusion = {
              defaultNumber: 1,//Số nhóm mặc định
              infusionProject: [[]],
              drippingSpeed: [""],  //Tốc độ truyền
              days: [{}],    //Số ngày
              frequency: [{}],   //Tần suất
              infuseUse: [{}],     //Cách dùng
              zushu: [1],
            }
            let count = 0
            for (let i = 0; i < element.recipelDetailEvtList.length; i++) {
              if (count < element.recipelDetailEvtList[i].infuseGroup) {
                count = element.recipelDetailEvtList[i].infuseGroup
              }
            }

            for (let i = 1; i < count; i++) {
              infusion.defaultNumber = infusion.defaultNumber + 1;
              infusion.zushu.push(infusion.defaultNumber)
              infusion.infusionProject.push([])
              infusion.drippingSpeed.push("")
              infusion.days.push({})
              infusion.frequency.push({})
              infusion.infuseUse.push({})
            }

            let arr = []
            for (let i = 0; i < element.recipelDetailEvtList.length; i++) {


              if (element.recipelDetailEvtList[i].isExtra !== 1) {

                infusion.infusionProject[element.recipelDetailEvtList[i].infuseGroup - 1].push(element.recipelDetailEvtList[i])
                infusion.drippingSpeed[element.recipelDetailEvtList[i].infuseGroup - 1] = element.recipelDetailEvtList[i].drippingSpeed
                infusion.days[element.recipelDetailEvtList[i].infuseGroup - 1] = element.recipelDetailEvtList[i].days
                infusion.frequency[element.recipelDetailEvtList[i].infuseGroup - 1] = element.recipelDetailEvtList[i].frequency
                infusion.infuseUse[element.recipelDetailEvtList[i].infuseGroup - 1] = element.recipelDetailEvtList[i].infuseUse
              } else {


                arr.push(element.recipelDetailEvtList[i])
              }

            }
            let tab = {
              title: element.recipelInfo.name,
              key: element.recipelInfo.seq,
              content: element,
              closable: false,
              type: element.recipelInfo.recipelType.value,
              infusion: infusion
            };
            this.medicalEditTabs.push(tab);
          } else {
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
        this.chargeForm.amountReceived = null;
        let discount = this.chargeForm.discount;
        if (this.chargeForm.discount === 0) {
          discount = 10;
        }

        for (let i = 0; i < this.medicalEditTabsValue.content.recipelDetailEvtList.length; i++) {
          this.chargeForm.amountReceived += Number((this.medicalEditTabsValue.content.recipelDetailEvtList[i].allFee * discount * 0.1).toFixed(2))
        }
        this.signleAmountOfRemission = Number(this.chargeForm.amountReceivable - this.chargeForm.amountReceived).toFixed(2)
        this.chargeForm.amountReceived = Number(this.chargeForm.amountReceived-this.reductionAmount).toFixed(2)


        this.amountOfRemission = (this.chargeForm.amountReceivable - this.chargeForm.amountReceived).toFixed(2)

        if (this.amountPaid > 0) {
          this.inputAmountPaid()
        }
      },
      GetSingleAmountReceived() {
        this.singleForm.amountReceived = null;
        let discount = this.singleForm.discount;
        if (this.singleForm.discount === 0) {
          discount = 10;
        }
        for (let i = 0; i < this.medicalEditTabsValue.content.recipelDetailEvtList.length; i++) {
          this.singleForm.amountReceived += Number((this.medicalEditTabsValue.content.recipelDetailEvtList[i].allFee * discount * 0.1).toFixed(2))
        }
        this.signleAmountOfRemission = (this.singleForm.amountReceivable - this.singleForm.amountReceived).toFixed(2)
        this.singleForm.amountReceived = this.singleForm.amountReceived.toFixed(2)
        if (this.amountPaid > 0) {
          this.inputAmountPaid()
        }
      },
      IsSingleCorrect() {
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
      getChargeData() {

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
            value: "tollType_5",
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
          case "payType_4":
            paymentType.name = "Bảo hiểm y tế";
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
            value: "tollType_5",
          },
          patient: {
            ...this.patientInfoForm,
            company: {...this.Company},
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
      submit() {
        this.$refs["chargeForm"].validate((valid) => {
          if (valid) {
            this.chargeBtn(1);
          } else {
            return false;
          }
        });
      },
      singleChargeBtn(type) {
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
          case "payType_4":
            paymentType.name = "Bảo hiểm y tế";
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
        if (type === 1) {
          tollInfo.amountStatus = {
            name: "Đã thu phí",
            value: "amountStatus_1",
          };
          chargeStatus = "2"
        } else {
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
          memberManagement: this.saveMember
        };

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
          case "payType_4":
            paymentType.name = "Bảo hiểm y tế";
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
        if (type === 2) {
          this.prescriptionMainList.forEach(item => {
            if (item.recipelInfo.dispensionStatus === 1) {
              state = true
            }
          })

        }

        if (state) {
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
        if (this.diagnosis !== null){
          /*modifiedState(this.diagnosis).then(res => {
            if (res.code === 100) {
              this.$message.success("Thu phí thành công！")
            } else {
              this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！")
            }
          }).catch()*/

          chargeState(model).then(res => {
            if (res.code === 100) {
              this.$message.success("Thu phí thành công！")
              if (this.diagnosis != null){
                // 查询远程诊疗订单
                this.orderQuery();
                setTimeout(() => {
                  // 结算远程诊疗订单
                  this.orderSettlement();
                }, 1000);
                // 修改状态
                this.diagnosis.diagnosisTime = null;
                this.diagnosis.medicationStatus = "1"
                this.diagnosis.cardNo = this.hisPatient.kh // Số thẻ khám bệnh
                this.diagnosis.orderNo = this.orderForm.OrderNo // Số đơn hàng
                this.diagnosis.payFee = this.chargeForm.amountReceivable // Phí thanh toán
                modifiedState(this.diagnosis).then(responseData => {
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
              }
            } else {
              this.$message.error("Dữ liệu hệ thống bất thường, vui lòng liên hệ quản trị viên！")
            }
          }).catch()
          this.chargeDialogVisible = false;
          this.singleDialogVisible = false;
        }else {
          // return
          saveTollTollInfo(model, type)
            .then((responseData) => {
              if (responseData.code === 100) {
                this.$message.success("Thao tác thành công！");
                this.selectPatientList();
                this.search();
                this.payState = false;
                this.chargeDialogVisible = false;
                this.singleDialogVisible = false;
                this.chargeForm = {
                  amountReceivable: 0,
                  amountDiscounted: 0,
                  discount: 10,
                  amountReceived: 0,
                  paymentType: "payType_0",
                  meno: "",
                }
                this.singleForm = {
                  amountReceivable: 0,
                  amountDiscounted: 0,
                  discount: 10,
                  amountReceived: 0,
                  paymentType: "payType_0",
                  meno: "",
                }
              } else if (responseData.code === 50001) {
                this.$message.warning(responseData.msg)
              } else {
                this.$message.error(responseData.msg);
                this.showMessage(responseData.msg);
              }
            })
            .catch((error) => {
              this.outputError(error);
            });
        }
      },
    },
    mounted() {
      this.pageInit();
      this.GetDrugTable();
      this.getPayTypeList();
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
  .single {
    padding: 8px 0 0;
    font-size: 14px;
    font-weight: 700;
    color: midnightblue;
    line-height: 28px;
  }

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

  ::v-deep .el-dialog__body {
    padding: 0 7px;
    margin: 0
  }

  ::v-deep .el-form-item__label {
    text-align: left;

  }

  ::v-deep .el-dialog .el-row {
    padding: 0 10px;
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
    color: forestgreen;
  }
</style>
