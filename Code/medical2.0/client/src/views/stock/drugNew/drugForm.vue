<template>
  <el-dialog
    :title="dialogProps.title"
    :visible.sync="dialogProps.visible"
    :close-on-click-modal="false"
    width="50%"
    :append-to-body="true"
    @open="onDialogOpen()"
    v-loading="loading"
  >
    <!-- <div slot="title" class="dialog-header">
      {{ dialogProps.title }}
      <OperationIcon
        v-show="dialogProps.action === 'view' && permission.edit"
        type="primary"
        text="Sửa"
        placement="top-start"
        icon-name="el-icon-edit"
        @click="switchEdit"
      ></OperationIcon>
    </div> -->

    <el-form
      :model="bizFormModel"
      :rules="formRules"
      ref="drugForm"
      label-width="120px"
      label-position="right"
      class="edit-form"
    >
      <div class="tab-item" v-show="tabIndex === '1'">
        <div class="specifications">Thông tin thuốc</div>
        <div>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Tên thuốc" prop="goodsName">
                <el-input
                  :disabled="dialogProps.action === 'view' "
                  v-model="bizFormModel.goodsName"
                  @input="pinyinInput"
                  :maxlength="128"
                  :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập tên thuốc'
                "
                  autofocus
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Bí danh" prop="brandName">
                <el-input
                  :disabled="dialogProps.action === 'view' "
                  v-model="bizFormModel.brandName"
                  :maxlength="128"
                  :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập bí danh'
                "
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Mã pinyin" prop="pinyinCode">
                <el-input
                  disabled
                  v-model="bizFormModel.pinyinCode"
                  :maxlength="64"
                  placeholder="Vui lòng nhập mã pinyin"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Loại thuốc" prop="type.value">
                <el-input
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.type.name"
                ></el-input>
                <el-select
                  v-else

                  v-model="bizFormModel.type"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn loại thuốc"
                  @clear="
                  bizFormModel.type = {
                    value: null,
                    name: null,
                  }
                "
                >
                  <el-option
                    v-for="t in type_List"
                    :key="t.value"
                    :label="t.name"
                    :value="t"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>

          </el-row>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Nguồn gốc thuốc" prop="source">
                <el-input
                  :disabled="dialogProps.action === 'view' "
                  v-model="bizFormModel.source"
                  :maxlength="128"
                  :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập nguồn gốc thuốc'
                "
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Tính chất" prop="nature.value">
                <el-input
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.nature.name"
                ></el-input>
                <el-select
                  v-else

                  v-model="bizFormModel.nature"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn tính chất"
                  @clear="
                  bizFormModel.nature = {
                    value: null,
                    name: null,
                  }
                "
                >
                  <el-option
                    v-for="nature in nature_List"
                    :key="nature.value"
                    :label="nature.name"
                    :value="nature"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Nhà sản xuất" prop="factory.id">
                <el-input
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.factory.name"
                ></el-input>
                <el-select
                  v-else
                  v-model="bizFormModel.factory"

                  value-key="id"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn nhà sản xuất"
                  @clear="
                  bizFormModel.factory = {
                    id: null,
                    name: null,
                  }
                "
                >
                  <el-option
                    v-for="factory in factory_List"
                    :key="factory.id"
                    :label="factory.name"
                    :value="factory"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Số đăng ký thuốc quốc gia" prop="standardCode">
                <el-input
                  :disabled="dialogProps.action === 'view' "
                  v-model="bizFormModel.standardCode"
                  :maxlength="64"
                  :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập số chuẩn thuốc quốc gia'
                "
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Mã chuẩn" prop="bitCode">
                <el-input
                  :disabled="dialogProps.action === 'view' "
                  v-model="bizFormModel.bitCode"
                  :maxlength="64"
                  :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập mã chuẩn'
                "
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Mã vạch" prop="barCode">
                <el-input
                  :disabled="dialogProps.action === 'view' "
                  v-model="bizFormModel.barCode"
                  :maxlength="64"
                  :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập mã vạch'
                "
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Mã bảo hiểm y tế" prop="insuranceCode">
                <el-input
                  :disabled="dialogProps.action === 'view' "
                  v-model="bizFormModel.insuranceCode"
                  :maxlength="64"
                  :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập mã bảo hiểm y tế'
                "
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Mã thuốc" prop="code">
                <el-input
                  disabled
                  v-model="bizFormModel.code"
                  :maxlength="64"
                  placeholder="Tự động tạo sau khi lưu"
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label='Có kích hoạt không' prop='status'>
              <el-switch :disabled="(dialogProps.action === 'view' && updateType==='0') " v-model='value'
                         active-color='#13ce66' inactive-color='#dbdfe6' active-value='1'
                         inactive-value='0'></el-switch>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider class="line"></el-divider>
        <!-- Quy cách -->
        <div class="specifications">Thông tin quy cách &nbsp;&nbsp;<span style="color:#C0C0C0;">(ví dụ：2.5g*12 viên/túi)</span></div>
        <div class="specifications-box">
          <!-- Liều lượng -->
          <div class="dose-box">
            <div class="dose-count">
              <el-form-item label="Liều lượng" prop="dosis" style="display:flex;align-item:center">
                <el-input
                  style="width:100px"
                  :disabled="dialogProps.action === 'view' "
                  v-model="bizFormModel.dosis"
                  :maxlength="45"
                  :placeholder="
                    dialogProps.action === 'view' ? '' : 'Vui lòng nhập liều lượng'
                  "
                ></el-input>
              </el-form-item>
            </div>
            <div class="dose-unit">
              <el-form-item label="" prop="dosisUnit.value">
                <el-input
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.dosisUnit.name"
                ></el-input>
                <el-select
                  v-else

                  v-model="bizFormModel.dosisUnit"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Đơn vị"
                  @clear="
                    bizFormModel.dosisUnit = {
                      value: null,
                      name: null,
                    }
                  "
                >
                  <el-option
                    v-for="dosisUnit in dosisUnit_List"
                    :key="dosisUnit.value"
                    :label="dosisUnit.name"
                    :value="dosisUnit"
                  ></el-option>
                </el-select>
              </el-form-item>
            </div>
          </div>
          <div style="margin: 0 20px 10px">*</div>
          <div class="dose-box">
            <div class="dose-count">
              <el-form-item label="Chế phẩm" prop="preparation" label-width="50px" style="display:flex;align-item:center">
                <el-input
                  style="width:100px"
                  :disabled="dialogProps.action === 'view' "
                  v-model="bizFormModel.preparation"
                  :maxlength="45"
                  :placeholder="
                    dialogProps.action === 'view' ? '' : 'Vui lòng nhập bào chế'
                  "
                ></el-input>
              </el-form-item>
            </div>
            <div class="dose-unit">
              <el-form-item label="" prop="preparationUnit.value">
                <el-input
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.preparationUnit.name"
                ></el-input>
                <el-select
                  v-else

                  v-model="bizFormModel.preparationUnit"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Đơn vị"
                  @clear="
                    bizFormModel.preparationUnit = {
                      value: null,
                      name: null,
                    }
                  "
                >
                  <el-option
                    v-for="preparationUnit in preparationUnit_List"
                    :key="preparationUnit.value"
                    :label="preparationUnit.name"
                    :value="preparationUnit"
                  ></el-option>
                </el-select>
              </el-form-item>
            </div>
          </div>
          <div style="margin: 0 20px 20px">/</div>
          <div class="dose-box">
            <div class="dose-count">
              <el-form-item label="Đóng gói" prop="pack.value" style="display:flex;align-item:center">
                <el-input
                  style="width:100px"
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.pack.name"
                ></el-input>
                <el-select
                  v-else

                  v-model="bizFormModel.pack"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Đơn vị"
                  style="width:100px"
                  @clear="
                    bizFormModel.pack = {
                      value: null,
                      name: null,
                    }
                  "
                >
                  <el-option
                    v-for="pack in pack_List"
                    :key="pack.value"
                    :label="pack.name"
                    :value="pack"
                  ></el-option>
                </el-select>
              </el-form-item>
            </div>
            <div class="dose-unit"></div>
          </div>
          <!-- <el-row>
            <el-col :span="24 / 2">

            </el-col>
            <el-col :span="24 / 2">

            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24 / 2"> </el-col>
          </el-row> -->
        </div>

        <el-divider class="line"></el-divider>
        <div class="specifications">Thông tin bán hàng</div>
        <div class="specifications-box2">
          <el-row>
            <el-col :span="24 / 3">
              <el-form-item
                class="price"
                label="Giá bán"
                prop="price"
                :rules="
                bizFormModel.isUnpackSell === '1'
                  ? [
                      {
                        required: false,
                        message: 'Vui lòng nhập giá bán',
                        trigger: 'blur',
                      },
                    ]
                  : [
                      {
                        required: true,
                        message: 'Vui lòng nhập giá bán',
                        trigger: 'blur',
                      },
                    ]
              "
              >
                <el-input
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.price"
                >
                  <template slot="append">
                    {{ bizFormModel.pack.name }}
                  </template>
                </el-input>
                <el-input
                  :type="type"
                  v-else
                  placeholder="Vui lòng nhập giá bán"
                  v-model="bizFormModel.price"
                  @focus="
                  (e) => {
                    if (bizFormModel.price === 0) {
                      bizFormModel.price = '';
                    }  else if(bizFormModel.price !== 0 && bizFormModel.price !== ''){
                      myFunction(bizFormModel.price)
                    }

                  }
                "
                  @blur="
                  (e) => {
                    if (bizFormModel.price === '') {
                      bizFormModel.price = 0;
                    }
                  }
                "
                  @input="getPrice"
                  type="number"
                  currency="VND"
                  :precision="2"
                >
                  <template slot="append">
                    {{ bizFormModel.pack.name }}
                  </template>
                </el-input>
                <!-- <span class="unit3">/{{ bizFormModel.pack.name }}</span> -->
              </el-form-item>
            </el-col>
            <el-col :span="24 / 6">

            </el-col>
            <el-col :span="24 / 6">
              <el-form-item style="margin-letf:-1000px" label="Cho phép bán lẻ tách hộp" prop="isUnpackSell">
                <el-switch
                  :disabled="(dialogProps.action === 'view' && updateType==='0') "
                  v-model="bizFormModel.isUnpackSell"
                  active-color="#13ce66"
                  inactive-color="#dbdfe6"
                  active-value="1"
                  inactive-value="0"
                ></el-switch>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 3">
              <el-form-item
                class="price"
                label="Giá bán lẻ tách hộp"
                prop="retailPrice"
                :rules="
                bizFormModel.isUnpackSell === '1'
                  ? formRules.retailPrice
                  : [
                      {
                        required: false,
                        message: 'Vui lòng nhập giá bán lẻ tách hộp',
                        trigger: 'blur',
                      },
                    ]
              "
              >
                <el-input
                  v-if="dialogProps.action === 'view' && updateType==='0'"
                  type="number"
                  placeholder="Vui lòng nhập giá bán lẻ đã chiết khấu"
                  :disabled="true"
                  v-model="bizFormModel.retailPrice"
                >
                  <template slot="append">
                    {{ bizFormModel.preparationUnit.name }}
                  </template>
                </el-input>
                <el-input
                  v-else
                  type="number"
                  placeholder="Vui lòng nhập giá bán lẻ đã chiết khấu"
                  style="width:175px"
                  @focus="
                  (e) => {
                    if (bizFormModel.retailPrice === 0) {
                      bizFormModel.retailPrice = '';
                    }
                  }
                "
                  @blur="
                  (e) => {
                    if (bizFormModel.retailPrice === '') {
                      bizFormModel.retailPrice = 0;
                    }
                  }
                "
                  v-model="bizFormModel.retailPrice"
                  :disabled="(bizFormModel.isUnpackSell === '0') "
                  currency="VND"
                  :precision="2"
                >
                  <template slot="append">
                    {{ bizFormModel.preparationUnit.name }}
                  </template>
                </el-input>
                <!-- <span class="unit3">/{{ bizFormModel.preparationUnit.name }}</span> -->
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <el-divider class="line"></el-divider>
        <div class="specifications">Giải thích công dụng</div>
        <div class="specifications-box2">
          <el-row class="info">
            <el-col :span="5">
              <el-form-item :label="bizFormModel.type.value==='medicalType_1'?'Cách sắc thuốc':'Cách dùng'">
                <el-input
                  v-if="dialogProps.action === 'view'&&bizFormModel.type.value==='medicalType_1' && updateType==='0'"
                  :disabled="true"
                  v-model="bizFormModel.chineseMedicineUse.name"
                ></el-input>
                <el-input
                  v-else-if="dialogProps.action === 'view'&&bizFormModel.type.value!=='medicalType_1' && updateType==='0'"
                  :disabled="true"
                  v-model="bizFormModel.westernMedicineUse.name"
                ></el-input>
                <el-select
                  v-else-if="bizFormModel.type.value==='medicalType_1'"
                  v-model="bizFormModel.chineseMedicineUse"
                  value-key="value"
                  filterable

                  clearable
                  placeholder="Vui lòng chọn"
                  @clear="
                  bizFormModel.chineseMedicineUse = {
                    value: null,
                    name: null,
                  }
                "
                >
                  <el-option
                    v-for="chineseMedicineUse in ChineseUseTimeOption"
                    :key="chineseMedicineUse.value"
                    :label="chineseMedicineUse.name"
                    :value="chineseMedicineUse"
                  ></el-option>
                </el-select>
                <el-select
                  v-else-if="bizFormModel.type.value!=='medicalType_1'"
                  v-model="bizFormModel.westernMedicineUse"

                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn"
                  @clear="
                  bizFormModel.westernMedicineUse = {
                    value: null,
                    name: null,
                  }
                "
                >
                  <el-option
                    v-for="westernMedicineUse in WesternUseOption"
                    :key="westernMedicineUse.value"
                    :label="westernMedicineUse.name"
                    :value="westernMedicineUse"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item
                class="price"
                label="Liều dùng mỗi lần"
                prop="singleDosage"
              >
                <div>
                  <el-input
                    v-if="dialogProps.action === 'view' && updateType==='0'"
                    :disabled="true"
                    v-model="bizFormModel.singleDosage"
                    style="width:100px;float:left;"
                  >
                    <template slot="append">
                      {{ bizFormModel.preparationUnit.name }}
                    </template>
                  </el-input>
                  <el-input
                    v-else

                    placeholder="Vui lòng nhập"
                    v-model="bizFormModel.singleDosage"
                    style="width:80px;float:left;"
                    @focus="
                  (e) => {
                    if (bizFormModel.singleDosage === 0) {
                      bizFormModel.singleDosage = '';
                    }
                  }
                "
                    @blur="
                  (e) => {
                    if (bizFormModel.singleDosage === '') {
                      bizFormModel.singleDosage = 0;
                    }
                  }
                "

                    min="0"
                    oninput="value=value.replace(/[^\d]/g,'')"
                  >
                    <template slot="append">
<!--                      {{ bizFormModel.preparationUnit.name }}-->
                      {{ bizFormModel.dosisUnit.name }}
                    </template>
                  </el-input>
                  <!-- <span class="unit2">/{{ bizFormModel.preparationUnit.name }}</span> -->
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="5" v-if="bizFormModel.type.value!=='medicalType_1'">
              <el-form-item label="Tần suất">
                <el-input
                  v-if="dialogProps.action === 'view'&&updateType==='0'"
                  :disabled="true"
                  v-model="bizFormModel.frequency.name"
                ></el-input>
                <el-select
                  v-else
                  v-model="bizFormModel.frequency"

                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn"
                  @clear="
                  bizFormModel.frequency = {
                    value: null,
                    name: null,
                  }
                "
                >
                  <el-option
                    v-for="frequency in FrequencyOption"
                    :key="frequency.value"
                    :label="frequency.name"
                    :value="frequency"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="5" v-if="bizFormModel.type.value!=='medicalType_1'">
              <el-form-item label="Số ngày">
                <el-input
                  v-if="dialogProps.action === 'view'&&updateType==='0'"
                  :disabled="true"
                  v-model="bizFormModel.days.name"
                ></el-input>
                <el-select
                  v-else
                  v-model="bizFormModel.days"

                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn"
                  @clear="
                  bizFormModel.days = {
                    value: null,
                    name: null,
                  }
                "
                >
                  <el-option
                    v-for="days in DayNumOption"
                    :key="days.value"
                    :label="days.name"
                    :value="days"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item
                class="price"
                label="Giới hạn tổng lượng"
                prop="total"
              >
                <el-input
                  v-if="dialogProps.action === 'view'&&updateType==='0'"
                  :disabled="true"
                  v-model="bizFormModel.total"
                  style="width:90px"
                >
                  <template slot="append">
                    {{ bizFormModel.preparationUnit.name }}
                  </template>
                </el-input>
                <el-input
                  v-else
                  placeholder="Vui lòng nhập"

                  v-model="bizFormModel.total"
                  style="width:90px"
                  @focus="
                  (e) => {
                    if (bizFormModel.total === 0) {
                      bizFormModel.total = '';
                    }
                  }
                "
                  @blur="
                  (e) => {
                    if (bizFormModel.total === '') {
                      bizFormModel.total = 0;
                    }
                  }
                "
                  oninput="value=value.replace(/[^\d]/g,'')"
                  min="0"
                >
                  <template slot="append">
                    {{ bizFormModel.preparationUnit.name }}
                  </template>
                </el-input>
                <!-- <span class="unit4">/{{ bizFormModel.preparationUnit.name }}</span> -->
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <el-divider class="line"></el-divider>
        <div class="specifications">Thông tin khác</div>
        <div class="appendUnit">
          <el-row>
            <el-col :span="24 / 3">
              <el-form-item label="Cảnh báo hạn sử dụng" prop="indate">
                <el-input
                  :disabled="(dialogProps.action === 'view'&&updateType==='0') "
                  v-model="bizFormModel.indate"
                  :maxlength="64"
                  placeholder="Vui lòng nhập số ngày cảnh báo"
                >
                  <template slot="append">ngày</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 3">
              <el-form-item label="Ngưỡng tồn kho tối thiểu" prop="inventoryFloor">
                <el-input
                  :disabled="(dialogProps.action === 'view'&&updateType==='0') "
                  v-model="bizFormModel.inventoryFloor"
                  :maxlength="64"
                  placeholder="Vui lòng nhập ngưỡng tồn kho tối thiểu"
                >
                  <template slot="append">{{ bizFormModel.pack.name }}</template>
                </el-input>
                <!-- <span class="unit3">/{{ bizFormModel.pack.name }}</span> -->
              </el-form-item>
            </el-col>
            <el-col :span="24 / 3">
              <el-form-item label="Ngưỡng tồn kho tối đa" prop="inventoryLimit">
                <el-input
                  :disabled="(dialogProps.action === 'view'&&updateType==='0') "
                  v-model="bizFormModel.inventoryLimit"
                  :maxlength="64"
                  placeholder="Vui lòng nhập ngưỡng tồn kho tối đa"
                >
                  <template style="padding:0 8px" slot="append">{{ bizFormModel.pack.name }}</template>
                </el-input>
                <!-- <span class="unit3">/{{ bizFormModel.pack.name }}</span> -->
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24 / 3">
              <el-form-item label="Số vị trí kho" prop="locationNumber">
                <el-input
                  :disabled="(dialogProps.action === 'view'&&updateType==='0') "
                  v-model="bizFormModel.locationNumber"
                  :maxlength="64"
                  placeholder="Vui lòng nhập số vị trí kho"
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='updateType==="1"||dialogProps.action === "add"' :disabled="flag" type='primary' :plain='true'
                 @click='onSubmit("drugForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action === "add"' :disabled="flag" type='primary' :plain='true'
                 @click='onGoSubmit("drugForm")'>Lưu và tiếp tục</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
  import {validatenull} from "@/utils/validate";
  import {listDictItemAll} from "@/api/sys/dictItem";
  import {getDictItemsByCode, DICT_CODE} from "@/utils/dictCache";
  import {listManufactureFactoryAll} from "@/api/basicdata/manufactureFactory";
  import {saveDrug, getDrugById, inventory} from "@/api/stock/drug";
  import BaseUI from "@/views/components/baseUI";
  import OperationIcon from "@/components/OperationIcon";
  import {BigNumber} from "bignumber.js";



  let pinyin = require('js-pinyin');
  export default {
    extends: BaseUI,
    name: "drug-form",
    components: {
      OperationIcon,
    },
    data() {
      return {
        bizFormModel: this.initFormModel(),
        tabIndex: "1",
        type_List: [], // Loại thuốc
        nature_List: [], // Tính chất
        factory_List: [], // Nhà sản xuất
        dosisUnit_List: [], // Đơn vị liều lượng
        preparationUnit_List: [], // Đơn vị bào chế
        pack_List: [], // Đóng gói
        value: '1',
        flag: false,
        dialogProps: {
          visible: false,
          action: '',
          title: '',
        },
        formRules: {
          goodsName: [
            {required: true, message: "Vui lòng nhập tên thuốc", trigger: "blur"},
          ],
          "type.value": [
            {required: true, message: "Vui lòng chọn loại thuốc", trigger: "change"},
          ],
          standardCode: [
            {required: false, message: "Vui lòng nhập số chuẩn thuốc quốc gia", trigger: "blur"},
          ],
          "preparationUnit.value": [
            {required: true, message: "Vui lòng chọn đơn vị bào chế", trigger: "change"},
          ],
          "pack.value": [
            {required: true, message: "Vui lòng chọn đóng gói", trigger: "change"},
          ],
          price: [{required: true, message: "Vui lòng nhập giá bán", trigger: "blur"}],
          retailPrice: [
            {required: true, message: "Vui lòng nhập giá bán lẻ sau chiết khấu", trigger: "blur"},
          ],
          preparation: [
            {required: true, message: "Vui lòng nhập số lượng bào chế", trigger: "blur"},
          ],
        },
        FrequencyOption: [],
        WesternUseOption: [],
        DayNumOption: [],
        ChineseUseTimeOption: [],
        isSpecial: false,
        updateType: '0', //Sửa hiển thị
        // Truyền tham số cơ bản
      };
    },
    props: {
      // Quyền
      permission: {
        type: Object,
      },

    },
    methods: {
      myFunction(){
        this.bizFormModel.price = BigNumber(this.bizFormModel.price).toFormat(4)
      },
      //Sửa động giá bán lẻ tách hộp
      getPrice() {
        if (this.bizFormModel.isUnpackSell === '1' && this.bizFormModel.preparation !== '') {
          this.bizFormModel.retailPrice = ((this.bizFormModel.price - 0) / (this.bizFormModel.preparation - 0)).toFixed(4)
        }
      },


      onSubmit(formName) {
        /*if (this.bizFormModel.company.id !== this.currentUser.company.id) {
          this.$message.error('Không thể sửa thông tin thuốc của cơ sở，Vui lòng liên hệ cơ sở để sửa thông tin thuốc cơ bản')
          return
        }*/
        if (this.bizFormModel.type.value !== 'medicalType_1') {
          if (this.bizFormModel.days.value && this.bizFormModel.frequency.value && this.bizFormModel.singleDosage && this.bizFormModel.total !== 0) {

            let total = Math.ceil(
              BigNumber(this.bizFormModel.singleDosage)
                .multipliedBy(this.bizFormModel.frequency.value.split("_")[1])
                .multipliedBy(this.bizFormModel.days.name)
                .toNumber()
            );
            if (total > this.bizFormModel.total) {
              this.$message.error('Giới hạn tổng lượng nhỏ hơn tổng lượng tính theo hướng dẫn công dụng')
              return
            }
          }
        }
        this.flag = true
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.doSave();
          } else {
            this.flag = false
            return false
          }
        });
      },
      //Lưu và tiếp tục
      onGoSubmit(formName) {
        if (this.bizFormModel.type.value !== 'medicalType_1') {
          if (this.bizFormModel.days.value && this.bizFormModel.frequency.value && this.bizFormModel.singleDosage && this.bizFormModel.total !== 0) {

            let total = Math.ceil(
              BigNumber(this.bizFormModel.singleDosage)
                .multipliedBy(this.bizFormModel.frequency.value.split("_")[1])
                .multipliedBy(this.bizFormModel.days.name)
                .toNumber()
            );
            if (total > this.bizFormModel.total) {
              this.$message.error('Giới hạn tổng lượng nhỏ hơn tổng lượng tính theo hướng dẫn công dụng')
              return
            }
          }
        }
        this.flag = true
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.setLoad()
            this.bizFormModel.status = this.value
            saveDrug(this.bizFormModel).then(responseData => {
              this.flag = false
              if (responseData.code === 100) {
                //this.dialogProps.visible = false
                this.bizFormModel.barCode = ""
                this.bizFormModel.bitCode = ""
                this.bizFormModel.brandName = ""
                this.bizFormModel.code = ""

                this.bizFormModel.dosis = ""
                this.bizFormModel.dosisUnit = {}
                this.bizFormModel.factory = {}
                this.bizFormModel.goodsName = ""
                this.bizFormModel.insuranceCode = ""
                this.bizFormModel.isUnpackSell = "1"
                this.bizFormModel.nature = {}
                // this.bizFormModel.pack={}
                this.bizFormModel.pinyinCode = ""
                this.bizFormModel.preparation = ""
                // this.bizFormModel.preparationUnit={}
                this.bizFormModel.price = ""
                this.bizFormModel.remarks = ""
                this.bizFormModel.retailPrice = ""
                this.bizFormModel.source = ""
                this.bizFormModel.standardCode = ""
                this.bizFormModel.status = ""
                this.bizFormModel.singleDosage = null
                this.bizFormModel.frequency = {}
                this.bizFormModel.days = {}
                this.bizFormModel.total = 0
                // this.bizFormModel.type={}
                this.value = '1'
                this.$emit('save-finished', "1")
              } else {
                this.showMessage(responseData)
              }
              this.resetLoad()
            }).catch(error => {
              this.flag = false
              this.outputError(error)
            })

          } else {
            this.flag = false
            return false
          }
        });
      },
      doSave() {
        this.setLoad()
        this.bizFormModel.status = this.value
        saveDrug(this.bizFormModel).then(responseData => {
          this.flag = false
          if (responseData.code === 100) {
            this.dialogProps.visible = false
            this.$emit('save-finished', "1")
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.flag = false
          this.outputError(error)
        })
      },
      switchEdit() {
        this.dialogProps.action = "edit";
        this.dialogProps.title = "Sửa thông tin thuốc";
        this.initOptions(this.bizFormModel);
      },
      onDialogClose() {
        this.dialogProps.visible = false;
        this.$emit('save-finished', "1")
      },
      onDialogOpen() {
        this.$nextTick(() => {
          this.$refs["drugForm"].clearValidate();
        });
      },
      initFormModel(This) {
        return {
          id:"", // Thuốcid
          goodsName: "", // Tên thuốc
          company: {
            // Phòng khámid
            id: currentUser.company.id,
            name: null,
          },
          brandName: "", // Tên hàng hóa
          type: {
            // Loại thuốc
            value: null,
            name: null,
          },
          code: "", // Mã thuốc
          source: "", // Nguồn gốc thuốc
          nature: {
            // Tính chất
            value: null,
            name: null,
          },
          factory: {
            // Nhà sản xuất
            id: null,
            name: null,
          },
          standardCode: "", // Số đăng ký thuốc quốc gia
          bitCode: "", // Mã chuẩn
          barCode: "", // Mã vạch
          insuranceCode: "", // Mã bảo hiểm y tế
          remarks: "", // Thông tin ghi chú
          dosis: "", // Liều lượng
          dosisUnit: {
            // Đơn vị liều lượng
            value: null,
            name: null,
          },
          preparation: "", // Chế phẩm
          preparationUnit: {
            // Đơn vị bào chế
            value: null,
            name: null,
          },
          pack: {
            // Đóng gói
            value: null,
            name: null,
          },
          price: 0, // Giá bán
          isUnpackSell: "1", // Cho phép bán lẻ tách hộp
          retailPrice: 0, // Giá bán lẻ sau khi tách hộp
          chineseMedicineUse: {
            name: null,
            value: null
          },
          westernMedicineUse: {
            name: null,
            value: null
          },
          days: {
            name: null,
            value: null
          },
          frequency: {
            name: null,
            value: null
          },
          singleDosage: 0,
          total: 0
        };
      },
      async initOptions(This) {
        getDictItemsByCode(DICT_CODE.MEDICAL_TYPE).then((data) => {
          this.type_List = data;
        });
        getDictItemsByCode(DICT_CODE.MEDICAL_NATURE).then((data) => {
          this.nature_List = data;
        });
        let factory_search = {
          params: [],
          type: "1"
        };
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        factory_search.params.push.apply(factory_search.params, [
          {
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
          {
            columnName: "type",
            queryType: "=",
            value: '1',
          },
        ]);
        // Quyền dữ liệu: Nhà sản xuấtmanufacture_factory
        this.pushDataPermissions(
          factory_search.params,
          this.$route.meta.routerId,
          "1016206064147988493"
        );
        this.factory_List.splice(0, this.factory_List.length);
        listManufactureFactoryAll(factory_search).then((responseData) => {
          this.factory_List = responseData.data;
        });
        getDictItemsByCode(DICT_CODE.MEDICAL_DOSIS_UNIT).then((data) => {
          this.dosisUnit_List = data;
          if (this.dialogProps.action === "add") {
            this.bizFormModel.dosisUnit = this.dosisUnit_List[0];
          }
        });
        getDictItemsByCode(DICT_CODE.MEDICAL_PREPARATION_UNIT).then((data) => {
          this.preparationUnit_List = data;
          if (this.dialogProps.action === "add") {
            this.bizFormModel.preparationUnit = this.preparationUnit_List[0];
          }
        });
        getDictItemsByCode(DICT_CODE.MEDICAL_PACK_UNIT).then((data) => {
          this.pack_List = data;
          if (this.dialogProps.action === "add") {
            this.bizFormModel.pack = this.pack_List[0];
          }
        });
      },
      //Mã pinyin của ô nhập
      pinyinInput(value) {
        this.bizFormModel.pinyinCode = pinyin.getCamelChars(value)

      },
      GetAllOption() {
        this.GetOption("1014474470772899990");
        this.GetOption("1014474470772900028");
        this.GetOption("1014474470772900052");
        this.GetOption("1014474470772900058");
      },
      GetOption(optionId) {
        let model = {
          params: [
            {
              columnName: "dict_type_id",
              queryType: "=",
              value: optionId,
            },
          ],
        };
        listDictItemAll(model).then((responseData) => {
          if (optionId === "1014474470772899981") {
            this.ChineseUseOption = responseData.data;
            if (this.isSpecial) {
              this.ChineseUseOption = this.ChineseUseOption.filter(
                (item) => item.name === "水冲"
              );
            }
          } else if (optionId === "1014474470772899990")
            this.FrequencyOption = responseData.data;
          else if (optionId === "1014474470772900028")
            this.WesternUseOption = responseData.data;
          else if (optionId === "1014474470772900052")
            this.DayNumOption = responseData.data;
          else if (optionId === "1014474470772900058")
            this.ChineseUseTimeOption = responseData.data;
        });
      },
      openViewDrugDialog(drug) {
        this.dialogProps.action = "view";
        this.dialogProps.title = "Xem thông tin thuốc";
        this.bizFormModel = {...this.initFormModel(), ...drug};
        this.updateType = '0'
        this.initOptions(this.bizFormModel);
        this.value = this.bizFormModel.status
        this.tabIndex = "1";
        this.dialogProps.visible = true;
      },
      openEditDrugDialog(drug) {
        this.dialogProps.action = "edit";
        this.dialogProps.title = "Sửa thông tin thuốc";
        this.bizFormModel = {...this.initFormModel(), ...drug};
        this.initOptions(this.bizFormModel);
        this.value = this.bizFormModel.status
        this.tabIndex = "1";
        this.updateType = '1'
        this.dialogProps.visible = true;
      },
      openAddDrugDialog() {
        this.dialogProps.action = "add";
        this.dialogProps.title = "Thêm thông tin thuốc";
        this.bizFormModel = this.initFormModel();
        this.initOptions(this.bizFormModel);
        this.tabIndex = "1";
        this.dialogProps.visible = true;
      },
      openCopyDrugDialog(drug) {
        this.dialogProps.action = "add";
        this.dialogProps.title = "Thêm thông tin thuốc";
        this.bizFormModel = {...this.initFormModel(), ...drug};
        this.initOptions(this.bizFormModel);
        this.tabIndex = "1";
        this.bizFormModel.id = null;
        this.dialogProps.visible = true;
      },
    },
    watch: {
      "bizFormModel.isUnpackSell"(newVal, oldVal) {
        if (newVal === "0") {
          this.bizFormModel.retailPrice = "";
        }
      },
      // Giá bán
      "bizFormModel.price"(newValue, oldValue) {
        if (newValue < 0) {
          this.$message({
            type: "error",
            message: "Giá không được nhỏ hơn0VNĐ",
          });
          return;
        }
      },
      //Giá sau chiết khấu
      "bizFormModel.retailPrice"(newValue, oldValue) {
        if (this.bizFormModel.isUnpackSell === "1") {
          if (newValue < 0) {
            this.$message({
              type: "error",
              message: "Giá không được nhỏ hơn0VNĐ",
            });
            return;
          }
          // this.bizFormModel.price = newValue;
        }
      },
      // "bizFormModel.type.value": function (newValue, oldValue) {
      //   // Thuốc đông y
      //   if (newValue === "medicalType_1") {
      //     this.bizFormModel.dosis = 1;
      //     this.bizFormModel.isUnpackSell = "0";
      //     this.bizFormModel.dosisUnit = {
      //       value: "medicalDosisUnit_0",
      //       name: "g",
      //     };
      //     // this.bizFormModel.preparationUnit.value='medicalPreparationUnit_3'
      //     //  this.bizFormModel.preparationUnit.name='g'

      //     this.bizFormModel.preparation = "1";
      //   }
      // },

      //Theo dõi loại thuốc
      "bizFormModel.type.value"(newValue, oldValue) {
        if (newValue === "medicalType_0") {
          this.formRules.standardCode[0].required = true
        } else if (newValue === "medicalType_2") {
          this.formRules.standardCode[0].required = true
        } else {
          this.formRules.standardCode[0].required = false
        }
      }
    },
    mounted() {
      let specialCompany = JSON.parse(sessionStorage.getItem("currentCompany"));
      if (specialCompany && specialCompany.id === "1088657523871555640") {
        this.isSpecial = true;
      }
      this.GetAllOption();


    },
  };
</script>
<style scoped>
  ::v-deep .el-dialog__body {
    padding: 30px 20px 0;
  }

  ::v-deep .info .el-form-item__label {
    width: 80px !important;
  }

  ::v-deep .info .el-form-item__content {
    margin-left: 80px !important;
  }

  .edit-form {
    margin-top: -50px;
  }

  .specifications {
    margin-top: -30px;
  }

  .specifications-box {
    margin-top: -10px;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .specifications-box2 {
    margin-top: -10px;
  }

  .dose-box,
  .dose-uint {
    display: flex;
    align-items: end;
    justify-content: center;
  }

  /* .dose-count {
    width: 100px;
  } */
  ::v-deep .dose-count .el-form-item__label {
    width: auto !important;
    margin-left: 0 !important;
  }

  ::v-deep .dose-count .el-form-item__content {
    width: auto !important;
    margin-left: 0 !important;
  }

  .dose-unit {
    width: 70px;
  }

  ::v-deep .dose-unit .el-form-item__content,
  .dose-unit .el-form-item__label {
    width: auto !important;
    margin-left: 0 !important;
  }

  ::v-deep .dose-unit .el-input__inner {
    padding-right: 8px !important;
  }
</style>
<style scoped>
  .specifications {
    width: 100%;
    font-weight: bold;
    padding-bottom: 8px;

    padding: 20px;
    color: #333;
  }

  ::v-deep .price {
    position: relative !important;
  }

  .price .unit {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
  }

  .unit2 {
    position: absolute;
    right: -28px;
    top: 50%;
    transform: translateY(-50%);
  }

  .unit3 {
    position: absolute;
    right: -13px;
    top: 50%;
    transform: translateY(-50%);
  }

  .unit4 {
    position: absolute;
    right: -1px;
    top: 50%;
    transform: translateY(-50%);
  }

  .line {
    margin-top: -10px;
  }

</style>
<style>
  .appendUnit .el-input-group__append {
    padding-right: 5px !important;
    padding-left: 5px !important;
  }

  .specifications-box2 .el-input-group__append {
    padding: 0 5px !important;
  }

  .specifications-box2 .el-input-group__append {
    padding: 0 5px !important;
  }
</style>
