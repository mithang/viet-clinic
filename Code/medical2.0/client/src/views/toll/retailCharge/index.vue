<template>
    <el-row v-loading="loading">
    <el-card class="retailCharge">
        <!-- Chuyển đổi thuốc/vật tư -->
        <div style="margin-top:-20px;margin-left:-20px">
            <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
                <el-tab-pane label="Thuốc bán lẻ" name="0">

                </el-tab-pane>
                <el-tab-pane label="Vật tư bán lẻ" name="1">

                </el-tab-pane>
            </el-tabs>
        </div>

        <!-- Thuốc bán lẻ -->
        <div v-if="activeName==='0'">
        <el-form
        :model="bizFormModel"
        :rules="formRules"
        ref="bizFormModel"
        label-width="120px"
        label-position="right"
        style="marginTop: 10px"
        class="edit-form"
        >
            <h4>Thông tin bệnh nhân</h4>
            <div class="patientMessage">
                <el-row>
                    <el-col :span='24/4'>
                         <el-form-item label="Tên bệnh nhân" prop="name">
                            <el-select
                            v-model="bizFormModel.name"
                            value-key="id"
                            filterable
                            allow-create
                            @change="GetPatientInfo()"
                            placeholder="Vui lòng chọn bệnh nhân"
                            >
                            <el-option
                                v-for="patientId in patientId_List"
                                :key="patientId.id"
                                :label="patientId.name"
                                :value="patientId"
                            >
                                <span style="float: left">{{ patientId.name }}</span>
                                <span
                                style="float: right; color: #8492a6; font-size: 13px"
                                >{{ patientId.phone }}</span
                                >
                            </el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                     <el-col :span="24 / 4">
                        <el-form-item label="Ngày sinh" prop="birthday">
                            <el-date-picker
                            v-model="bizFormModel.birthday"
                            type="date"
                            @change="birthdayChanges"
                            :picker-options="setDisabled"
                            value-format="yyyy-MM-dd"
                            :placeholder="
                                'Vui lòng nhập ngày sinh'
                            "

                            ></el-date-picker>

                        </el-form-item>
                    </el-col>
                     <el-col :span="24 / 4">
                    <el-form-item label="Tuổi" prop="age">
                        <el-col :span="24 / 2">
                        <el-input

                            v-model="bizFormModel.age"
                            :maxlength="20"
                            :placeholder="
                            'Tuổi'
                            "
                        >
                        <template slot="append">
                            tuổi
                        </template>
                        </el-input>
                        </el-col>
                        <el-col :span="24 / 2">
                        <el-input

                            v-model="bizFormModel.month"
                            :maxlength="20"
                            :placeholder="'tháng'"
                        >
                        <template slot="append">
                            tháng
                        </template>
                        </el-input>
                        </el-col>
                    </el-form-item>
                    </el-col>
                    <el-col :span="24 / 4">
                    <el-form-item label="Giới tính" prop="gender.name">
                        <el-select
                        v-model="bizFormModel.gender"
                        value-key="value"
                        filterable
                        placeholder="Vui lòng chọn giới tính"
                        @clear="
                            bizFormModel.gender = {
                            value: null,
                            name: null,
                            }
                        "
                        >
                        <el-option
                            v-for="gender in gender_List"
                            :key="gender.value"
                            :label="gender.name"
                            :value="gender"
                        ></el-option>
                        </el-select>
                    </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                     <el-col :span="24 / 4">
                        <el-form-item label="Thông tin liên hệ" prop="phone">
                            <el-input
                            v-model="bizFormModel.phone"
                            :maxlength="45"
                            :placeholder="
                               'Vui lòng nhập thông tin liên hệ'
                            "
                            ></el-input>
                        </el-form-item>
                        </el-col>
                </el-row>
            </div>

        <h4>Thông tin thuốc</h4>
        <!-- 关于药品 -->
         <el-popover
            placement="top-start"
            width="700"
            trigger="focus"
            >
            <el-table
                :data="DrugMedicineTable"
                :height="300"
                border
                highlight-current-row
                @row-click="RowClickWesternTable"
            >
                <el-table-column
                prop="drug.type.name"
                label="Loại thuốc"
                ></el-table-column>
                <el-table-column
                prop="drug.goodsName"
                label="Tên thuốc"
                ></el-table-column>
                <el-table-column prop="gg" label="Quy cách" width="120">
                <template slot-scope="scope">
                    {{ scope.row.drug.dosis
                    }}{{ scope.row.drug.dosisUnit.name }} *
                    {{ scope.row.drug.preparation
                    }}{{ scope.row.drug.preparationUnit.name }}/{{
                    scope.row.drug.pack.name
                    }}
                </template>
                </el-table-column>
                <el-table-column
                prop="drug.factory.name"
                label="Nhà sản xuất"
                width="100"
                ></el-table-column>
                <el-table-column label="Giá đóng gói" width="80">
                <template slot-scope="scope">
                    {{ scope.row.drug.price + "/" + scope.row.drug.pack.name }}
                </template>
                </el-table-column>
                <el-table-column label="Giá bán lẻ" width="80">
                <template slot-scope="scope">
                    {{
                    scope.row.drug.isUnpackSell === 1
                        ? scope.row.drug.retailPrice +
                        "/" +
                        scope.row.drug.preparationUnit.name
                        : "--"
                    }}
                </template>
                </el-table-column>
                <el-table-column
                prop="surplusStock"
                label="Tồn kho khả dụng"
                width="100"
                >
                <template slot-scope="scope">
                    {{
                    Math.floor(
                        scope.row.surplusStock / scope.row.drug.preparation
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
            <el-input
                prefix-icon="el-icon-plus"
                suffix-icon="el-icon-search"
                style="width: 30%"
                slot="reference"
                ref="WesternInput"
                v-model="SearchDrugInput"
                @input="GetWesternTable"
                @focus="GetWesternTable"
                placeholder="Nhập tên thuốc hoặc mã pinyin"
            ></el-input>
            </el-popover>
            <!-- 药品点击回显表格 -->
            <div style="padding-top:20px;">
                  <el-table
                :data="DrugList"
                :height="300"
                border
                highlight-current-row
            >
               <el-table-column
                prop="drug.goodsName"
                label="Tên thuốc"
                ></el-table-column>
                <el-table-column
                prop=""
                label="Quy cách thuốc"
                >
                <template slot-scope="scope">
                    {{scope.row.drug.dosis+scope.row.drug.dosisUnit.name+'*'+scope.row.drug.preparation+scope.row.drug.preparationUnit.name+'/'+scope.row.drug.pack.name}}
                </template>
                </el-table-column>
                <el-table-column
                prop="drug.type.name"
                label="Loại thuốc"
                ></el-table-column>
                 <el-table-column
                prop="drug.factory.name"
                label="Nhà sản xuất"
                ></el-table-column>
                <!-- <el-table-column
                prop="drug.goodsName"
                label="Tên thuốc"
                ></el-table-column>
                <el-table-column prop="gg" label="Quy cách" width="120">
                <template slot-scope="scope">
                    {{ scope.row.drug.dosis
                    }}{{ scope.row.drug.dosisUnit.name }} *
                    {{ scope.row.drug.preparation
                    }}{{ scope.row.drug.preparationUnit.name }}/{{
                    scope.row.drug.pack.name
                    }}
                </template>
                </el-table-column> -->
                <el-table-column label="Giá đóng gói" >
                <template slot-scope="scope">
                    {{ scope.row.drug.price + "/" + scope.row.drug.pack.name }}
                </template>
                </el-table-column>
                <el-table-column label="Giá bán lẻ" >
                <template slot-scope="scope">
                    {{
                    scope.row.drug.isUnpackSell === 1
                        ? scope.row.drug.retailPrice +
                        "/" +
                        scope.row.drug.preparationUnit.name
                        : "--"
                    }}
                </template>
                </el-table-column>
                <el-table-column
                prop="surplusStock"
                label="Tồn kho khả dụng"
                >
                <template slot-scope="scope">
                    {{
                    Math.floor(
                        scope.row.surplusStock / scope.row.drug.preparation
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
                    <el-table-column
                      prop="drugTracCodg"
                      label="Mã truy xuất thuốc"
                    > <template slot-scope="scope">
                      <el-button :type="scope.row.completed ? 'success' : 'warning'" @click="openDrugDialog(scope)">Nhập mã truy xuất</el-button>
                    </template></el-table-column>
                <el-table-column
                prop=""
                label="Số lượng">
                <template slot-scope="scope">
                    <el-input
                    v-model="scope.row.number"
                    @input="compute(scope.row)"
                    oninput="value=value.replace(/[^\d.]/g,'')"
                    >

                    </el-input>
                </template>
                </el-table-column>

                 <el-table-column
                prop="type"
                label="Đơn vị">
                <template slot-scope="scope">
                    <el-input
                    disabled
                    v-if="scope.row.drug.isUnpackSell==='0'"
                    v-model="scope.row.drug.pack.name"
                    >

                    </el-input>
                     <el-select
                              v-else
                              v-model="scope.row.type"
                              placeholder=""
                              @change="compute(scope.row)"
                            >
                              <el-option
                                :key="scope.row.drug.pack.value"
                                :label="scope.row.drug.pack.name"
                                :value="scope.row.drug.pack"
                              >
                              </el-option>
                               <el-option
                                :key="scope.row.drug.preparationUnit.value"
                                :label="scope.row.drug.preparationUnit.name"
                                :value="scope.row.drug.preparationUnit"
                              >
                              </el-option>
                            </el-select>
                </template>
                </el-table-column>
                 <el-table-column
                    prop="allFee"
                    label="Tổng số tiền(VNĐ)"
                    width="120"
                    align="center"
                    ></el-table-column>
                <el-table-column label="Thao tác" >
                <template slot-scope="scope">
                   <el-button type="text" @click="deleteDrugList(scope.row,scope.$index)">Xóa</el-button>
                </template>
                </el-table-column>
            </el-table>
            </div>

            <!-- Khung tổng -->

            <div class="statistical_box">
                <div style="padding-top:0px;margin-top:20px;">
                    <div style="float:left;">
                    <span>Tổng số tiền:{{totalAllFee}}VNĐ</span>
                </div>
                  <el-form-item>
                <el-button type="primary" @click="notarize('bizFormModel')">
                    Xác nhận thu phí
                </el-button>
                  </el-form-item>
                </div>
            </div>

        </el-form>
        </div>

        <!-- Vật tư bán lẻ -->
        <div v-else>
             <el-form
        :model="stuffBizFormModel"
        :rules="stuffFormRules"
        ref="stuffBizFormModel"
        label-width="120px"
        label-position="right"
        style="marginTop: 10px"
        class="edit-form"
        >
            <h4>Thông tin bệnh nhân</h4>
            <div class="patientMessage">
                <el-row>
                    <el-col :span='24/4'>
                         <el-form-item label="Tên bệnh nhân" prop="name">
                            <el-select
                            v-model="stuffBizFormModel.name"
                            value-key="id"
                            filterable
                            allow-create
                            @change="GetPatientInfo()"
                            placeholder="Vui lòng chọn bệnh nhân"
                            >
                            <el-option
                                v-for="patientId in patientId_List"
                                :key="patientId.id"
                                :label="patientId.name"
                                :value="patientId"
                            >
                                <span style="float: left">{{ patientId.name }}</span>
                                <span
                                style="float: right; color: #8492a6; font-size: 13px"
                                >{{ patientId.phone }}</span
                                >
                            </el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                     <el-col :span="24 / 4">
                        <el-form-item label="Ngày sinh" prop="birthday">
                            <el-date-picker
                            v-model="stuffBizFormModel.birthday"
                            type="date"
                            @change="birthdayChanges"
                            :picker-options="setDisabled"
                            value-format="yyyy-MM-dd"
                            :placeholder="
                                'Vui lòng nhập ngày sinh'
                            "

                            ></el-date-picker>

                        </el-form-item>
                    </el-col>
                     <el-col :span="24 / 4">
                    <el-form-item label="Tuổi" prop="age">
                        <el-col :span="24 / 2">
                        <el-input

                            v-model="stuffBizFormModel.age"
                            :maxlength="20"
                            :placeholder="
                            'Tuổi'
                            "
                        >
                        <template slot="append">
                            tuổi
                        </template>
                        </el-input>
                        </el-col>
                        <el-col :span="24 / 2">
                        <el-input

                            v-model="stuffBizFormModel.month"
                            :maxlength="20"
                            :placeholder="'tháng'"
                        >
                        <template slot="append">
                            tháng
                        </template>
                        </el-input>
                        </el-col>
                    </el-form-item>
                    </el-col>
                    <el-col :span="24 / 4">
                    <el-form-item label="Giới tính" prop="gender.name">
                        <el-select
                        v-model="stuffBizFormModel.gender"
                        value-key="value"
                        filterable
                        placeholder="Vui lòng chọn giới tính"
                        @clear="
                            stuffBizFormModel.gender = {
                            value: null,
                            name: null,
                            }
                        "
                        >
                        <el-option
                            v-for="gender in gender_List"
                            :key="gender.value"
                            :label="gender.name"
                            :value="gender"
                        ></el-option>
                        </el-select>
                    </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                     <el-col :span="24 / 4">
                        <el-form-item label="Thông tin liên hệ" prop="phone">
                            <el-input
                            v-model="stuffBizFormModel.phone"
                            :maxlength="45"
                            :placeholder="
                               'Vui lòng nhập thông tin liên hệ'
                            "
                            ></el-input>
                        </el-form-item>
                        </el-col>
                </el-row>
            </div>

        <h4>Thông tin vật tư</h4>
        <!-- 关于材料 -->
         <el-popover
            placement="top-start"
            width="700"
            trigger="focus"
            >
            <el-table
                :data="StuffMedicineTable"
                :height="300"
                border
                highlight-current-row
                @row-click="RowClickStuffTable"
            >
                <el-table-column
                prop="stuff.type.name"
                label="Loại vật tư"
                ></el-table-column>
                <el-table-column
                prop="stuff.name"
                label="Tên vật tư"
                ></el-table-column>
                <el-table-column prop="gg" label="Quy cách" width="120">
                <template slot-scope="scope">
                    {{ scope.row.stuff.packNumber
                    }}*{{ scope.row.stuff.minUnit.name }} /
                    {{ scope.row.stuff.packUnit.name
                    }}
                </template>
                </el-table-column>
                <el-table-column
                prop="stuff.factory.name"
                label="Nhà sản xuất"
                width="100"
                ></el-table-column>
                <el-table-column label="Giá đóng gói" width="80">
                <template slot-scope="scope">
                    {{ scope.row.stuff.priceOutSell +"/" +scope.row.stuff.packUnit.name }}
                </template>
                </el-table-column>
                <el-table-column label="Giá bán lẻ" width="80">
                <template slot-scope="scope">
                    {{
                    scope.row.stuff.isUnpackSell === "1"
                                  ? scope.row.stuff.retailPrice +
                                    "/" +
                                    scope.row.stuff.minUnit.name
                                  : "--"
                    }}
                </template>
                </el-table-column>
                <el-table-column
                prop="surplusStock"
                label="Tồn kho khả dụng"
                width="100"
                >
                <template slot-scope="scope">
                    {{
                    Math.floor(
                        scope.row.surplusStock / scope.row.stuff.packNumber
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
                    }}
                </template>
                </el-table-column>
            </el-table>
            <el-input
                prefix-icon="el-icon-plus"
                suffix-icon="el-icon-search"
                style="width: 30%"
                slot="reference"
                ref="WesternInput"
                v-model="SearchStuffInput"
                @input="GetStuffTable"
                @focus="GetStuffTable"
                placeholder="输入材料名称或拼音码"
            ></el-input>
            </el-popover>
            <!-- 材料点击回显表格 -->
            <div style="padding-top:20px;">
                  <el-table
                :data="StuffList"
                :height="300"
                border
                highlight-current-row
            >
               <el-table-column
                prop="stuff.name"
                label="Tên vật tư"
                ></el-table-column>
                <el-table-column
                prop=""
                label="材料规格"
                >
                <template slot-scope="scope">
                    {{scope.row.stuff.packNumber+'*'+scope.row.stuff.minUnit.name+'/'+scope.row.stuff.packUnit.name}}
                </template>
                </el-table-column>
                <el-table-column
                prop="stuff.type.name"
                label="Loại vật tư"
                ></el-table-column>
                 <el-table-column
                prop="stuff.factory.name"
                label="Nhà sản xuất"
                ></el-table-column>

                <el-table-column label="Giá đóng gói" >
                <template slot-scope="scope">
                    {{ scope.row.stuff.priceOutSell +"/" +scope.row.stuff.packUnit.name }}
                </template>
                </el-table-column>
                <el-table-column label="Giá bán lẻ" >
                <template slot-scope="scope">
                    {{
                    scope.row.stuff.isUnpackSell === "1"
                                  ? scope.row.stuff.retailPrice +
                                    "/" +
                                    scope.row.stuff.minUnit.name
                                  : "--"
                    }}
                </template>
                </el-table-column>
                <el-table-column
                prop="surplusStock"
                label="Tồn kho khả dụng"
                >
                <template slot-scope="scope">
                    {{
                    Math.floor(
                        scope.row.surplusStock / scope.row.stuff.packNumber
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
                    }}
                </template>
                </el-table-column>
                <el-table-column
                prop=""
                label="Số lượng">
                <template slot-scope="scope">
                    <el-input
                    v-model="scope.row.number"
                    @input="compute(scope.row)"
                    oninput="value=value.replace(/[^\d.]/g,'')"
                    >

                    </el-input>
                </template>
                </el-table-column>
                 <el-table-column
                prop="type"
                label="Đơn vị">
                <template slot-scope="scope">
                    <el-input
                    disabled
                    v-if="scope.row.stuff.isUnpackSell==='0'"
                    v-model="scope.row.stuff.packUnit.name"
                    >

                    </el-input>
                     <el-select
                              v-else
                              v-model="scope.row.type"
                              placeholder=""
                              @change="compute(scope.row)"
                            >
                              <el-option
                                :key="scope.row.stuff.packUnit.value"
                                :label="scope.row.stuff.packUnit.name"
                                :value="scope.row.stuff.packUnit"
                              >
                              </el-option>
                               <el-option
                                :key="scope.row.stuff.minUnit.value"
                                :label="scope.row.stuff.minUnit.name"
                                :value="scope.row.stuff.minUnit"
                              >
                              </el-option>
                            </el-select>
                </template>
                </el-table-column>
                 <el-table-column
                    prop="allFee"
                    label="Tổng số tiền(VNĐ)"
                    width="120"
                    align="center"
                    ></el-table-column>
                <el-table-column label="Thao tác" >
                <template slot-scope="scope">
                   <el-button type="text" @click="deleteDrugList(scope.row,scope.$index)">Xóa</el-button>
                </template>
                </el-table-column>
            </el-table>
            </div>

            <!-- Khung tổng -->

            <div class="statistical_box">
                <div style="padding-top:0px;margin-top:20px;">
                    <div style="float:left;">
                    <span>Tổng số tiền:{{totalAllFee}}VNĐ</span>
                </div>
                  <el-form-item>
                <el-button type="primary" @click="notarize('stuffBizFormModel')">
                    Xác nhận thu phí
                </el-button>
                  </el-form-item>
                </div>
            </div>

        </el-form>
        </div>
      <el-dialog @keydown.native="handleKeydown" :modal="false"  :visible.sync="drugDialogVisible" title="Nhập mã truy xuất thuốc" @open="focusInput" @close="resetInput">
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


        <!-- 确认收费弹出框 -->
        <el-dialog
      title="Thu phí"
      :visible.sync="chargeDialogVisible"
      width="50%"
      :close-on-click-modal="false"
      :before-close="chargeFormCancel"
    >
      <div style="margin-top:-50px">
        <el-divider></el-divider>
        <div id="chargeTotalFee_Container">
          <div id="chargeTotalFee_Title">Số tiền thực thu（VNĐ）：</div>
          <div id="chargeTotalFee_Fee" v-if="this.practicalTotalAllFee">
            {{  this.practicalTotalAllFee}}VNĐ
          </div>
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
           <div id="chargeTotalFee_Title">Số tiền phải thu（VNĐ）：</div>
            <div id="chargeTotalFee_Fee" v-if="this.totalAllFee">
            {{  this.totalAllFee}}VNĐ
          </div>
        </div>
        <el-form ref="chargeForm" :model="totalAllFee" label-width="80px">
          <el-row>

            <el-col :span="8">
              <el-form-item label="Chiết khấu">
                <el-input
                  v-model="discount"
                  @input="GetAmountReceived"
                  placeholder="Chiết khấu"
                >
                  <template slot="append">折</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8" :offset="1">
              <el-form-item label="Số tiền miễn giảm">
                <el-input
                  v-model="amountOfRemission"
                  placeholder="Số tiền miễn giảm"
                  disabled
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
          <el-row v-if="paymentType==='payType_0'">
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
            <el-col :span="20">
              <el-form-item label="Phương thức thanh toán">
                <el-radio-group v-model="paymentType">
                  <el-radio label="payType_0">Tiền mặt</el-radio>
                  <el-radio label="payType_1">支付宝</el-radio>
                  <el-radio label="payType_2">WeChat</el-radio>
                  <el-radio label="payType_3">银行卡</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="Ghi chú">
                <el-input type="textarea" :rows="3" v-model="meno"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <el-divider></el-divider>
      </div>
      <div slot="footer">
        <el-button @click="chargeFormCancel">Hủy</el-button>
        <el-button type="primary" @click="chargeBtn">Xác nhận thu phí</el-button>
      </div>
    </el-dialog>
    </el-card>
    </el-row>
</template>
<script>

import { validatenull } from "@/utils/validate";
import { listClinicOfficeAll } from "@/api/org/clinicOffice";
import {
  listPatientAll,
  savePatient,
  getPatientById,
} from "@/api/outpatient/patient";
import { getDictItemsByCode, DICT_CODE } from "@/utils/dictCache";
import { saveRegistration,listDoctorsAll } from "@/api/outpatient/registration";
import {listAll} from "@/api/stock/medicinalStockControl";
import BaseUI from "@/views/components/baseUI";
import { saveTollInfo } from "@/api/toll/tollInfo";
import { BigNumber } from "bignumber.js";
import { saveTollTollInfo } from "@/api/toll/tollInfo";
export default {
    extends: BaseUI,
    data(){
        return{
            activeName:"0",
            bizFormModel: this.initPatientModel() ,
            PatientFormModel: this.initPatientModel(),
            stuffBizFormModel:this.initPatientModel(),
            StuffPatientForModel:this.initPatientModel(),
            formRules:{
                name: [{ required: true, message: "Vui lòng nhập họ tên", trigger: "blur" }],
                birthday:[{required: true, message: "Vui lòng chọn ngày sinh", trigger: "blur"}],
                age: [{ required: true, message: "Vui lòng nhập tuổi", trigger: "blur" }],
                'gender.name': [{ required: true, message: "Vui lòng chọn giới tính", trigger: 'change' }],
            },
            stuffFormRules:{
                name: [{ required: true, message: "Vui lòng nhập họ tên", trigger: "blur" }],
                birthday:[{required: true, message: "Vui lòng chọn ngày sinh", trigger: "blur"}],
                age: [{ required: true, message: "Vui lòng nhập tuổi", trigger: "blur" }],
                'gender.name': [{ required: true, message: "Vui lòng chọn giới tính", trigger: 'change' }],
            },
            patientId_List:[],  //Lấy thông tin bệnh nhân
            gender_List:[],   //获取性别
            SearchDrugInput:"", //药品搜索
            SearchStuffInput:"", //Tìm vật tư
            DrugMedicineTable:[], //药品表格
            StuffMedicineTable:[], //材料表格
            DrugList:[],//药品集合
            StuffList:[], //Tập vật tư
            number:0,
            totalAllFee:0,  //应收总金额
            practicalTotalAllFee:0, //实收总金额
            discount:0,   //Chiết khấu
            amountOfRemission:"0", //Số tiền ưu đãi
            chargeDialogVisible:false,
            paymentType:"payType_0",    //Phương thức thanh toán
            meno:"",            //Ghi chú
            amountPaid:0, //Số tiền thực trả
            amountOfChange:0,//Số tiền trả lại
           //Điều khiển hộp thoại mã truy xuất
           drugDialogVisible: false,
           currentInput: '',
           inputCount: 0, // Số lần nhập hiện tại
           maxInputCount: 0, // Số lần nhập tối đa
           currentRow: null, // Dòng đang thao tác
           allInputs: [], // Tất cả mã truy xuất đã nhập
        }
    },
    methods:{
      handleKeydown(event) {
        if (event.key === 'Enter') {
          // Gọi thao tác xác nhận
          this.submitInput();
        }
      },
      focusInput() {
        // Sử dụng ref để truy cập ô nhập và gọi focus() Phương pháp
        this.$nextTick(() => {
          this.$refs.inputFocus.focus(); // Tự động focus vào ô nhập
        });
      },
      openDrugDialog(scope) {
        if ( scope.row.number <= 0 || ! scope.row.number) {
          this.$message.error('Vui lòng nhập trước số lượng hợp lệ và có tách hộp không！');
          return; // Nếu số lượng không hợp lệ，Dừng thực hiện
        }
        this.currentRow = scope.row; // Ghi lại dòng hiện tại
        this.inputCount = 0; // Đặt lại bộ đếm nhập liệu
        this.allInputs = []; // Xóa dữ liệu nhập trước đó
        this.drugDialogVisible = true;
        // Đặt số lần nhập cơ bản và tối đa theo số lượng của dòng hiện tại
        const quantity = this.currentRow.number;
        this.maxInputCount = this.currentRow.isSplit ? this.currentRow.preparation * quantity: quantity;
      },

      submitInput() {
        if (this.currentInput) {
          this.allInputs.push(this.currentInput); // Thêm dữ liệu nhập hiện tại
          this.currentRow.drugTracCodg = this.allInputs.join(','); // Cập nhật mã truy xuất thuốc của dòng hiện tại
          this.currentInput = ''; // Xóa ô nhập
          this.inputCount++; // Tăng bộ đếm nhập

          // Kiểm tra đã đạt số lần nhập tối đa chưa
          if (this.inputCount >= this.maxInputCount) {
            this.$set(this.currentRow, 'completed', true); // Đặt thành trạng thái hoàn thành
            this.drugDialogVisible = false; // Đóng hộp thoại
          }
          this.$nextTick(() => {
            this.$refs.inputFocus.focus(); // Tự động focus vào ô nhập
          });
        }
      },
      resetInput() {
        this.currentInput = ''; // Xóa ô nhập
        this.inputCount = 0; // Đặt lại bộ đếm nhập liệu
        this.currentRow = null; // Xóa bản ghi dòng hiện tại
      },
      updateMaxInputCount(row) {
        this.$set(this.currentRow, 'isSplit', true); // Đặt thành trạng thái hoàn thành
        // Mỗi khi trạng thái công tắc thay đổi thì cập nhật số lần nhập tối đa
        const quantity = row.packAmount;
        this.maxInputCount = row.isSplit ? quantity * row.preparationAmount: quantity;
      },
        //Hủy thu phí toàn bộ
        chargeFormCancel(){
        this.chargeDialogVisible = false
        this.discount = 0
        this.amountPaid = 0
        this.amountOfRemission = 0
        this.amountOfChange = 0
        },
        //Tính số tiền trả lại khi thanh toán tiền mặt
        inputAmountPaid(){
        this.amountOfChange = 0
        let amount = this.amountPaid?this.amountPaid:0
        if(amount>0){
            this.amountOfChange = (this.amountPaid - this.practicalTotalAllFee).toFixed(2)
        }
        },

        //Xác nhận thu phí
        chargeBtn(){
            //进行数据封装
            let tollInfo = this.tollEncapsulation()
            let recipelInfos = this.recipelInfosEncapsulation()
            let count =0;
            if(this.activeName==='0'){
                 this.DrugList.forEach((item)=>{
                let recipelDetail = {
                    company:currentUser.company,
                    drugStuffId:{
                        drugStuffId:item.drugStuffId,
                        name:item.drug.goodsName,
                        price:item.drug.price,
                        retailPrice:item.drug.retailPrice,
                        dosisUnit:item.drug.dosisUnit,
                        preparationUnit:item.drug.preparationUnit,
                        pack:item.drug.pack,
                        isUnpackSell:item.drug.isUnpackSell,
                        durg:item.drug,
                        drugTracCodg:item.drugTracCodg
                    },
                    total:item.total,
                    drugTracCodg:item.drugTracCodg,//Mã truy xuất bảo hiểm y tế
                    allFee:item.allFee,
                    isExtra:0,
                    seq:count+=1,
                    isUnpackSell:item.isUnpackSell,
                    unitPrice:item.isUnpackSell===0?item.drug.price:item.drug.retailPrice,
                    minTotal:item.total,
                    stuffType:item.drug.type.value==="medicalType_1"?"1":"0"
                }
                recipelInfos[0].recipelDetailEvtList.push(recipelDetail)
            })

            }else{
                 this.StuffList.forEach((item)=>{
                let recipelDetail = {
                    company:currentUser.company,
                    drugStuffId:{
                        drugStuffId:item.drugStuffId,
                        name:item.stuff.name,
                        price:item.stuff.priceOutSell,
                        retailPrice:item.stuff.retailPrice,
                        preparationUnit:item.stuff.minUnit,
                        pack:item.stuff.packUnit,
                        isUnpackSell:item.stuff.isUnpackSell,
                        stuff:item.stuff
                    },
                    total:item.total,
                    allFee:item.allFee,
                    isExtra:0,
                    seq:count+=1,
                    isUnpackSell:item.isUnpackSell,
                    unitPrice:item.isUnpackSell===0?item.stuff.priceOutSell:item.stuff.retailPrice,
                    minTotal:item.total,
                    stuffType:'4'
                }
                recipelInfos[0].recipelDetailEvtList.push(recipelDetail)
            })

            }
            let model = {
                tollInfo: tollInfo,
                recipelInfos: recipelInfos,
                chargeStatus: '2',
            }
           // return
            saveTollTollInfo(model,null).then((res)=>{
                if (res.code === 100) {

                    this.$message.success("Thao tác thành công！");
                    this.chargeDialogVisible = false;
                    this.bizFormModel = this.initPatientModel()
                    this.stuffBizFormModel = this.initPatientModel()
                    this.number = 0
                    this.totalAllFee = 0
                    this.practicalTotalAllFee = 0
                    this.discount = 0
                    this.amountOfRemission = '0';
                    this.amountPaid = 0
                    this.amountOfChange = 0
                    if(this.activeName==="0"){
                          this.DrugList=[]
                          this.$nextTick(() => {
                               this.$refs.bizFormModel.resetFields();
                           })
                    }else{
                        this.StuffList = []
                        this.$nextTick(() => {
                            this.$refs.stuffBizFormModel.resetFields();
                        })
                    }


                }
            }).catch((error)=>{
                this.outputError(error)
            })
        },

        recipelInfosEncapsulation(){
            return [
                {
                    recipelInfo:{
                        company:currentUser.company,  //Phòng khámid
                        fee:this.totalAllFee,         //Tổng giá đơn thuốc
                        isPay:'0',                    //是否支付
                        name:'Đơn thuốc bán lẻ',
                        remarks:'Đơn thuốc bán lẻ',
                        seq:1,                    //Sắp xếp
                        smallType:{
                            value:"recipelSmallType_1",  //处方小类
                        },
                        status:1,                 //Trạng thái đơn thuốc
                        chargeStatus:1,          //Trạng thái thu phí
                        dispensionStatus:0,      //Trạng thái phát thuốc

                    },
                    recipelDetailEvtList:[

                    ],
                }
            ]
        },

        tollEncapsulation(){
            return{
                company:currentUser.company,  //Phòng khámid
                patient:this.activeName==='0'?this.bizFormModel:this.stuffBizFormModel,    //Bệnh nhân
                amountReceivable:this.totalAllFee,  //Số tiền phải thu
                amountReceived:this.practicalTotalAllFee,  //Số tiền thực thu
                amountDiscounted:this.amountOfRemission,  //Số tiền ưu đãi
                discount:this.discount,    //Mức chiết khấu
                paymentType:{value:this.paymentType},  //Loại thanh toán
                amountStatus:{value:'amountStatus_1'}, //单据类型
                tollType:{value:'tollType_5'},      //Loại thu phí
            }
        },

        //根据出生日期计算岁数
        birthdayChanges(){
           if(this.activeName==='0'){
                if(!this.bizFormModel.birthday){
                this.bizFormModel.age="";
                this.bizFormModel.month=""
                return
                };
            const duration =  this.$moment.duration(this.$moment().diff(this.bizFormModel.birthday));
            this.bizFormModel.age = duration.years();
            this.bizFormModel.month = duration.months();
           }else{
                if(!this.stuffBizFormModel.birthday){
                this.stuffBizFormModel.age="";
                this.stuffBizFormModel.month=""
                return
                };
            const stuffation =  this.$moment.duration(this.$moment().diff(this.stuffBizFormModel.birthday));
            this.stuffBizFormModel.age = stuffation.years();
            this.stuffBizFormModel.month = stuffation.months();
           }
        },

        //Tính phí thực tế
        GetAmountReceived() {
            this.practicalTotalAllFee = this.totalAllFee
            let discount = this.discount;
            if (this.discount === 0) {
            discount = 10;
            }
            this.practicalTotalAllFee =
            this.practicalTotalAllFee * discount * 0.1;
            this.practicalTotalAllFee =
            this.practicalTotalAllFee.toFixed(2);
            this.amountOfRemission=(this.totalAllFee-this.practicalTotalAllFee).toFixed(2)
            // this.chargeForm.amountDiscounted=this.chargeForm.amountReceived
            this.inputAmountPaid()
        },

        //进行收费确认
        notarize(formName){
             if(this.activeName==='0'){
                 this.$refs[formName].validate((valid) => {
                if (valid) {
                    let flag = false
                    let flag1 =false
                    if(!this.DrugList.length>0){
                        this.$message.error("Vui lòng chọn thuốc")
                        return
                    }
                    //Thực hiện kiểm tra số lượng trong bảng
                    this.DrugList.forEach((item)=>{

                        if(item.number==null||item.number===0){
                            this.$message.error("Vui lòng điền"+item.drug.goodsName+"số lượng")
                            flag = true
                        }
                    })
                    if(flag){

                        return
                    }
                    //  if(!this.StuffList.length>0){
                    //     this.$message.error("Vui lòng chọn vật tư")
                    //     return
                    // }
                    // //Thực hiện kiểm tra số lượng trong bảng
                    // this.StuffList.forEach((item)=>{

                    //     if(item.number==null||item.number==0){
                    //         this.$message.error("Vui lòng điền"+item.stuff.name+"số lượng")
                    //         flag = true
                    //     }
                    // })
                    // if(flag){

                    //     return
                    // }

                    this.GetAmountReceived()
                     this.chargeDialogVisible=true

               // alert('submit!');
                } else {
                return false;
                }
            });
             }else{
                 this.$refs[formName].validate((valid) => {
                if (valid) {
                    let flag = false
                    if(!this.StuffList.length>0){
                        this.$message.error("Vui lòng chọn vật tư")
                        return
                    }
                    //Thực hiện kiểm tra số lượng trong bảng
                    this.StuffList.forEach((item)=>{

                        if(item.number==null||item.number===0){
                            this.$message.error("Vui lòng điền"+item.stuff.name+"số lượng")
                            flag = true
                        }
                    })
                    if(flag){

                        return
                    }
                    this.GetAmountReceived()
                     this.chargeDialogVisible=true
               // alert('submit!');
                } else {
                return false;
                }
            });
             }

        },

        // 进行删除操作
        deleteDrugList(row,index){
           if(this.activeName==='0'){
            this.DrugList.splice(index,1);
            this.compute(row)
           }else{
               this.StuffList.splice(index,1);
               this.compute(row)
           }
        },

        compute(row){
            this.totalAllFee = 0
           if(this.activeName==='0'){
               this.DrugList.forEach((item)=>{
                let type = item.type.value.split("_")[0]

                if(type === 'medicalPackUnit'){
                    item.allFee = BigNumber(item.number>0?item.number:0).multipliedBy(item.drug.price).toNumber()
                    item.total = item.number*item.drug.preparation
                    item.isUnpackSell = 0
                }else{
                    item.allFee = BigNumber(item.number>0?item.number:0).multipliedBy(item.drug.retailPrice).toNumber()
                    item.total = item.number
                    item.isUnpackSell = 1
                }
                this.totalAllFee = BigNumber(this.totalAllFee).plus(item.allFee).toNumber()
            })
           }else{
               this.StuffList.forEach((item)=>{
                   let type = item.type.value.split("_")[0]

                if(type === 'medicalPackUnit'){
                    item.allFee = BigNumber(item.number>0?item.number:0).multipliedBy(item.stuff.priceOutSell).toNumber()
                    item.total = item.number*item.stuff.packNumber
                    item.isUnpackSell = 0
                }else{
                    item.allFee = BigNumber(item.number>0?item.number:0).multipliedBy(item.stuff.retailPrice).toNumber()
                    item.total = item.number
                    item.isUnpackSell = 1
                }
                this.totalAllFee = BigNumber(this.totalAllFee).plus(item.allFee).toNumber()
               })
           }
           this.totalAllFee = this.totalAllFee.toFixed(2)
           this.practicalTotalAllFee = this.totalAllFee
        },
        //获取病人
        getPatient(){
             //Truy vấn danh sách bệnh nhân
                let patientId_search = {
                    params: [],
                };
                // Trên bảng tương ứng của trườngfilterĐiều kiện
                patientId_search.params.push.apply(patientId_search.params, [
                    {
                    columnName: "company_id",
                    queryType: "=",
                    value: currentUser.company.id,
                    },
                ]);
                // Quyền dữ liệu: Bảng bệnh nhânpatient
                this.pushDataPermissions(
                    patientId_search.params,
                    this.$route.meta.routerId,
                    "1008489176147648530"
                );
                this.patientId_List.splice(0, this.patientId_List.length);
                listPatientAll(patientId_search).then((responseData) => {
                    this.patientId_List = responseData.data;
                });
        },
        // 数据初始化
        initPatientModel() {
            return {
                company: {
                // Phòng khámid
                id: currentUser.company.id,
                name: null,
                },
                name: "", // Tên bệnh nhân
                gender: {
                // Giới tínhnull
                value: null,
                name: null,
                },
                age: "", // Tuổi
                month: "", // tháng
                birthday: "", // Ngày sinh
                phone: "", // Thông tin liên hệ
                card: "", // Số CMND/CCCD
                withPatientNexus: {
                // Quan hệ với bệnh nhân
                value: null,
                name: null,
                },
                healthCardNo: "", // Số thẻ sức khỏe
                province: "", // Tỉnh/Thành phố
                city: "", // Thành phố
                area: "", // Quận/Huyện
                address: "", // Địa chỉ chi tiết
                remarks: "", // Thông tin ghi chú
                id:'',
                openId:'',
                updateDate:'',
                createDate:''
            };
            },
        // 关于新增患者
        GetPatientInfo(){
            if(this.activeName === '0'){
                if(this.bizFormModel.name.id){
                this.PatientFormModel = this.bizFormModel.name
                this.bizFormModel = {...this.bizFormModel,...this.PatientFormModel}
                }else{
                    this.PatientFormModel = {
                        ...this.initPatientModel(),
                        name:this.bizFormModel.name
                    }
                    this.bizFormModel = {...this.bizFormModel,...this.PatientFormModel}
                }
            }else{
                if(this.stuffBizFormModel.name.id){
                    this.StuffPatientForModel = this.stuffBizFormModel.name
                    this.stuffBizFormModel = {...this.stuffBizFormModel,...this.StuffPatientForModel}
                }else{
                    this.StuffPatientForModel = {...this.initPatientModel(),name:this.stuffBizFormModel.name}
                    this.stuffBizFormModel = {...this.stuffBizFormModel,...this.StuffPatientForModel}
                }
            }

        },

        //初始化获取字典数据
        initOptions(){
             //Truy vấn giới tính bệnh nhân
                getDictItemsByCode(DICT_CODE.GENDER).then((data) => {
                    this.gender_List = data;
                });
        },

        //获取药品
        GetWesternTable(){
            let search = {
                params:[
                    {
                        columnName:"company_id",
                        queryType:"=",
                        value:currentUser.company.id
                    },
                    {
                        columnName:"goods_name",
                        queryType:"like",
                        value:""
                    }

                ]
            }
              //Kiểm tra có phải nhập tiếng Anh không
                const pattern2 = new RegExp("[A-Za-z]+");
                if (pattern2.test(this.SearchDrugInput)) {
                    search.params[1].value =
                    this.SearchDrugInput.toUpperCase();
                    search.params[1].columnName = "drug.pinyin_code";

                } else {
                    search.params[1].value = this.SearchWesternInput;
                    search.params[1].columnName = "drug.goods_name";
                }

                    search.params.push({
                    columnName:"surplus_stock",
                    queryType:">",
                    value:0,
                    })
                    search.params.push({
                    columnName:"drug.status",
                    queryType:"=",
                    value:'1',
                    })
                    search.params.push({
                    columnName:"drug.del_flag",
                    queryType:"=",
                    value:'0',
                    })
                listAll(search).then((responseData) => {
                    if (responseData.code === 100) {
                    this.DrugMedicineTable = responseData.data;
                    }
                }).catch((error)=>{
                    this.outputError(error)
                });
        },

        //获取材料
        GetStuffTable(){
             let SearchModel = {
                columnName: "",
                limit: 10,
                offset: 0,
                order: "",
                params: [
                {
                    columnName: "company_id",
                    queryType: "=",
                    value: currentUser.company.id,
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
            if (pattern2.test(this.SearchStuffInput)) {
                SearchModel.params[1].value = this.SearchStuffInput.toUpperCase();
                SearchModel.params[1].columnName = "stuff.pinyin_code";
            } else {
                SearchModel.params[1].value = this.SearchStuffInput;
                SearchModel.params[1].columnName = "stuff.name";
            }
            SearchModel.params.push({
                    columnName: "stuff.status",
                    queryType: "=",
                    value: '1',
            })
            SearchModel.params.push({
                    columnName: "stuff.del_flag",
                    queryType: "=",
                    value: '0',
            })
            listAll(SearchModel).then((responseData) => {
                if (responseData.code === 100) {
                    this.StuffMedicineTable=responseData.data
                }
            });
        },

        //药品点击
        RowClickWesternTable(row){
            let flag = false
            this.DrugList.forEach((item)=>{
                if(item.id===row.id){
                    flag = true
                }
            })
            if(flag){
                this.$message.warning("Thuốc này đã được thêm, vui lòng không thêm trùng lặp!")
                return;
            }
            row.number = null
            row.allFee = null
            row.type = row.drug.pack
            row.drugTracCodg = null
            this.DrugList.push(JSON.parse(JSON.stringify(row)));
            this.$message.success("Thêm thuốc thành công")
        },

        //材料点击
        RowClickStuffTable(row){
            let flag = false
            this.StuffList.forEach((item)=>{
                if(item.id===row.id){
                    flag = true
                }
            })
            if(flag){
                this.$message.warning("Vật tư này đã được thêm, vui lòng không thêm trùng lặp!")
                return;
            }
            row.number = null
            row.allFee = null
            row.type = row.stuff.packUnit
            this.StuffList.push(JSON.parse(JSON.stringify(row)));
            this.$message.success("Thêm vật tư thành công")
        }
    },
    watch:{
        activeName:function(newVal,oldVal){
            if(newVal==='0'){
                this.bizFormModel = this.initPatientModel()
                    this.DrugList=[]
                    this.StuffList = [];
                    this.number = 0
                    this.totalAllFee = 0
                    this.practicalTotalAllFee = 0
                    this.discount = 0
                    this.amountOfRemission = '0';
                   this.$nextTick(() => {
                       this.$refs.bizFormModel.resetFields();
                   })
            }else{
                this.stuffBizFormModel = this.initPatientModel();
                this.StuffList = [];
                this.DrugList=[];
                 this.number = 0
                this.totalAllFee = 0
                this.practicalTotalAllFee = 0
                this.discount = 0
                this.amountOfRemission = '0';
                 this.$nextTick(() => {
                       this.$refs.stuffBizFormModel.resetFields();
                   })
            }
        }
    },
    mounted(){
        this.getPatient();
        this.initOptions();
    }

}
</script>
<style scoped>
.retailCharge{
    padding: 0px;
}
.patientMessage{
    margin-left: -53px;
}
.statistical_box{
    position: fixed;
    z-index: 100;
    bottom: 16px;
    background: #fff;
    padding: 1px 16px;
    box-sizing: border-box;
    border: 1px solid #71c7ad;
    box-shadow: 0 0 4px 0 rgb(81 165 140 / 15%), 0 2px 17px 0 rgb(113 199 173 / 11%);
    border-radius: 4px;
    font-size: 10;
    text-align: right;
    /* visibility: visible; */
    width: 1450px;
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
</style>
