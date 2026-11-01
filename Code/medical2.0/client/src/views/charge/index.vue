<template>
  <div class="charge_container">
    <div class="charge_left">
      <div
        style="
          display: flex;
          justify-content: flex-end;
          margin-top: 10px;
        "
      >
        <el-button type="primary" @click="changeToRetail">Thu phí bán lẻ</el-button>
      </div>
      <el-divider></el-divider>
      <div
        style="display: flex; justify-content: flex-start;"
      >
        <el-input
          v-model="selectCondition"
          placeholder="Vui lòng nhập tên bệnh nhân/Số thẻ"
          prefix-icon="el-icon-search"
          style="margin-right: 8px"
          @keydown.enter.native="handleSearch"
        ></el-input>
        <el-button type="text" @click="handleSearch">Tìm kiếm</el-button>
      </div>
      <div>
        <el-tabs
          stretch
          v-model="chargeStatusActiveName"
          @tab-click="onChargeTabClick"
        >
          <el-tab-pane label="Chờ thu phí" name="1"></el-tab-pane>
          <el-tab-pane label="Đã thu phí" name="2"></el-tab-pane>
          <el-tab-pane label="Đã hoàn phí" name="3"></el-tab-pane>
        </el-tabs>
        <el-table
          :data="patientList"
          :show-header="false"
          highlight-current-row
          @row-click="selectPatientChange"
          style="width: 100%;height:80%;margin-top:-15px"
          empty-text="Không có bệnh nhân ở trạng thái này"
        >
          <el-table-column prop="name" label="Họ tên"> </el-table-column>
          <el-table-column
            prop="clinicDate"
            label="Thời gian khám"
            :formatter="clinicDateFormatter"
          >
          </el-table-column>
          <el-table-column prop="chargeStatus" label="Trạng thái thu phí">
          </el-table-column>
        </el-table>
        <div style="display: flex; justify-content: flex-end">
          <el-pagination
            layout="prev, next"
            :page-size="patientPageSize"
            prev-text="Trang trước"
            next-text="Trang sau"
            :current-page="patientCurrentPage"
            @prev-click="onPatientPage"
            @next-click="onPatientPage"
            :total="patientTotalCount"
          >
          </el-pagination>
        </div>
      </div>
    </div>
    <div class="charge_right">
      
      <div class="flex-space-between" style="margin-top: 10px">
        <div style="font-weight: bold">Thu phí</div>
        <div class="flex-space-between">
          <div v-if="chargeStatusActiveName === '3'" class="goods-back">Đã trả thuốc</div>
          <div style="margin-top: 5px; margin-right: 10px">
            Tổng phải thu: {{ formatVND(chargeTotalFee) }}
          </div>
          <!-- Nối API thật /toll/tollInfo: chỉ mở thu/hoàn phí khi đã chọn
               bệnh nhân có đơn thuốc; tab Đã hoàn phí không mở lại. -->
          <el-button
            :type="chargeStatusActiveName === '1' ? 'warning' : ''"
            :disabled="!selectedRegistration || chargeStatusActiveName === '3'"
            @click="chargeClick"
            >{{ chargeStatusActiveName === "2" ? "Hoàn phí" : "Thu phí" }}</el-button
          >
          <el-button v-if="chargeStatusActiveName === '2'">Xuất hóa đơn</el-button>
          <el-button @click="openChargeSummary">Xuất tổng kết ngày</el-button>
        </div>
      </div>
      <div class="patientinfo_form">
        <el-form
          ref="patientInfoForm"
          :model="patientInfoForm"
          :rules="isRetail ? patientInfoRules : patientInfoNoRules"
          label-width="80px"
        >
          <el-row>
            <el-col :span="8">
              <el-form-item label="Tên bệnh nhân" prop="name">
                <el-input v-model="patientInfoForm.name" disabled></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label="Giới tính" prop="gender">
                <el-select v-model="patientInfoForm.gender" disabled>
                  <el-option
                    v-for="gender in genderList"
                    :key="gender.id"
                    :label="gender.name"
                    :value="gender.id"
                  >
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="11">
              <div
                style="
                  display: flex;
                  flex-direction: row;
                  justify-content: flex-start;
                "
              >
                <el-form-item label="Tuổi" prop="age_year">
                  <el-input v-model="patientInfoForm.age_year" type="number" disabled>
                    <template slot="append">tuổi</template>
                  </el-input>
                </el-form-item>
                <div style="margin-left: -80px">
                  <el-form-item prop="age_month">
                    <el-input v-model="patientInfoForm.age_month" type="number" disabled>
                      <template slot="append">tháng</template>
                    </el-input>
                  </el-form-item>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-form-item label="Số điện thoại liên hệ" prop="telephone">
                <el-input v-model="patientInfoForm.telephone" disabled></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="11">
              <el-form-item label="Số CMND/CCCD" prop="idCardNo">
                <el-input v-model="patientInfoForm.idCardNo" disabled></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="5"></el-col>
          </el-row>
        </el-form>
      </div>
      <div v-if="isRetail">
        <!-- Bán lẻ trên trang này trước đây gọi các hàm không tồn tại
             (queryGoods/AddRetailDrug) nên không dùng được. Luồng bán lẻ
             thật đã nối API nằm ở trang Thu phí bán lẻ (retailCharge);
             nút dưới mở đúng trang đó khi route đã đăng ký, nếu chưa thì
             hiện hướng dẫn mở từ menu. -->
        <el-alert
          title="Thu phí bán lẻ thực hiện tại trang Thu phí bán lẻ (retailCharge)"
          type="info"
          :closable="false"
          show-icon
          style="margin: 10px 0"
        >
          <div>
            Trang này chỉ thu phí theo đơn thuốc của bệnh nhân đã khám.
            Bán lẻ không cần đăng ký khám thì mở trang Thu phí bán lẻ.
          </div>
        </el-alert>
        <el-button type="primary" @click="openRetailCharge">Mở trang Thu phí bán lẻ</el-button>
      </div>
      <div v-else>
        <el-card
          class="prescriptionCard"
          v-for="(prescriptionMain, index) in prescriptionMainList"
          :key="index"
        >
          <div v-if="prescriptionMain.prescriptionType === 1">
            
            <el-table :data="prescriptionMain.prescriptionDetail" style="width: 100%">
              <el-table-column prop="goodsName" label="Đơn thuốc tây" width="width">
              </el-table-column>
              <el-table-column prop="factoryName" label="Nhà sản xuất" width="width">
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá" width="width">
              </el-table-column>
              <el-table-column prop="quantity" label="Số lượng" width="width">
              </el-table-column>
              <el-table-column prop="unit" label="Đơn vị" width="width">
              </el-table-column>
              <el-table-column prop="totalCost" label="Tổng tiền" width="width">
              </el-table-column>
            </el-table>
            <div class="flex-end">
              <div>Tổng cộng{{prescriptionMain.prescriptionDetail.length }},Tổng cộng{{ getPrescriptionTotalCost(prescriptionMain.prescriptionDetail) }}VNĐ</div>
            </div>
          </div>
          <!--Thuốc đông y-->
          <div v-else-if="prescriptionMain.prescriptionType === 2">
            <el-table :data="prescriptionMain.prescriptionDetail" style="width: 100%">
              <el-table-column prop="goodsName" label="Đơn thuốc đông y" width="width">
              </el-table-column>
              <el-table-column prop="factoryName" label="Nhà sản xuất" width="width">
              </el-table-column>
              <el-table-column prop="tisanesMethod" label="Cách sắc thuốc" width="width">
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá" width="width">
              </el-table-column>
              <el-table-column prop="quantity" label="Số lượng" width="width">
              </el-table-column>
              <el-table-column prop="totalQuantity" label="Tổng số lượng" width="width">
              </el-table-column>
              <el-table-column prop="totalCost" label="Tổng tiền" width="width">
              </el-table-column>
            </el-table>
            <div class="flex-space-between">
              <div class="flex-start">
                <div style="">{{ prescriptionMain.quantity }}thang</div>
                <div>Cách dùng: {{ prescriptionMain.useMethod }}</div>
                <div>{{ yypc }}</div>
                <!--Tần suất sắc thuốc: 1 ngày 1 thang-->
                <div>{{ fypc }}</div>
                <!--Tần suất uống thuốc: 1 ngày 2 lần-->
                <div>{{ mcyl }}</div>
                <!--Liều dùng mỗi lần: một lần500ml-->
              </div>
              <div>Tổng cộng{{ pz }},Tổng cộng{{ zj }}VNĐ</div>
            </div>
          </div>
          <!--Phí phụ thêm-->
          <div v-if="prescriptionMain.prescriptionType === 3">
            <el-table :data="prescriptionMain.prescriptionDetail" style="width: 100%">
              <el-table-column prop="goodsName" label="Phí phụ thêm" width="width">
              </el-table-column>
              <el-table-column prop="quantity" label="Số lượng" width="width">
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá" width="width">
              </el-table-column>
              <el-table-column prop="totalCost" label="Tổng cộng" width="width">
              </el-table-column>
            </el-table>
            <div class="flex-end">
              <div>Tổng cộng{{ prescriptionMain.prescriptionDetail.length }},Tổng cộng{{ getPrescriptionTotalCost(prescriptionMain.prescriptionDetail) }}VNĐ</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
    <el-dialog
      title="Thu phí"
      :visible.sync="chargeDialogVisible"
      width="50%"
      :close-on-click-modal="false"
      :before-close="dialogBeforeClose"
    >
      <div>
        <el-divider></el-divider>
        <div id="chargeTotalFee_Container">
          <div id="chargeTotalFee_Title">Số tiền phải thu:</div>
          <div id="chargeTotalFee_Fee">{{ formatVND(chargeTotalFee) }}</div>
        </div>
        <el-form ref="chargeForm" :model="chargeForm" label-width="80px">
          <el-row>
            <el-col :span="5">
              <el-form-item label="Số tiền ưu đãi">
                <el-input
                  v-model="chargeForm.discountAmount"
                  placeholder="Số tiền ưu đãi"
                >
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label="Chiết khấu">
                <el-input v-model="chargeForm.discountRate" placeholder="Chiết khấu (10 = không giảm)">
                  <template slot="append">/10</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="10">
              <el-form-item label="Số tiền thực thu">
                <el-input v-model="chargeForm.actualAmount">
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <el-form-item label="Phương thức thanh toán">
                <!-- Giá trị khớp dict payType (payment_dict_vi.sql); trước đây
                     là label số 1/2/3/3 hard-code gồm Alipay/WeChat và trùng
                     label 3. Hộp thoại vẫn bị khóa xác nhận cho tới khi nối API. -->
                <el-radio-group v-model="chargeForm.paymentType">
                  <el-radio label="payType_0">Tiền mặt</el-radio>
                  <el-radio label="payType_5">Chuyển khoản ngân hàng</el-radio>
                  <el-radio label="payType_6">Ví điện tử</el-radio>
                  <el-radio label="payType_3">Thẻ ngân hàng</el-radio>
                </el-radio-group>
                <!-- Chỉ 4 phương thức trên khớp dict payType đang bật
                     (payment_dict_vi.sql); Alipay/WeChat/BHYT đã bị ẩn. -->
              </el-form-item>
            </el-col>
          </el-row>
          <!-- VietQR (G4): chỉ hiện khi chọn "Chuyển khoản ngân hàng".
               Mã QR vẽ từ cấu hình của chính phòng khám + số tiền thực thu
               + nội dung DK<mã đăng ký>; chỉ hiển thị, không tự động xác
               nhận thanh toán — thu ngân vẫn bấm "Xác nhận thu phí" sau
               khi nhận được tiền như quy trình hiện tại. -->
          <el-row v-if="chargeForm.paymentType === 'payType_5'">
            <el-col :span="24">
              <el-form-item label="VietQR">
                <div v-if="vietqrLoading">Đang tải cấu hình VietQR…</div>
                <div v-else-if="!vietqrConfigured">
                  <span>Phòng khám chưa cấu hình tài khoản nhận VietQR nên chưa hiển thị được mã QR. </span>
                  <el-button type="text" @click="openVietqrConfig">Cấu hình VietQR</el-button>
                </div>
                <div v-else-if="vietqrAmount <= 0">
                  <span>Nhập số tiền thực thu lớn hơn 0 để hiển thị mã QR.</span>
                </div>
                <div v-else>
                  <div id="vietqrCode"></div>
                  <div>
                    {{ vietqrConfig.bankName ? vietqrConfig.bankName + " — " : "" }}Số tài khoản: {{ vietqrConfig.accountNo }} — Chủ tài khoản: {{ vietqrConfig.accountName }}
                  </div>
                  <div>
                    Số tiền: {{ formatVND(vietqrAmount) }} — Nội dung: {{ vietqrContent }}
                  </div>
                  <div style="color: #909399">
                    Bệnh nhân quét mã để chuyển khoản đúng số tiền; thu ngân vẫn xác nhận đã nhận tiền rồi mới bấm "Xác nhận thu phí".
                  </div>
                  <el-button type="text" @click="openVietqrConfig">Đổi tài khoản VietQR</el-button>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="15">
              <el-form-item label="Ghi chú">
                <el-input v-model="chargeForm.note"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <el-divider></el-divider>
      </div>
      <div slot="footer">
        <el-button @click="chargeDialogVisible = false">Hủy</el-button>
        <el-button
          type="primary"
          :loading="chargeSubmitting"
          @click="confirmCharge"
          >Xác nhận thu phí</el-button
        >
      </div>
    </el-dialog>
    <!-- Cấu hình tài khoản nhận VietQR (G4): chỉ lưu ngân hàng + số tài
         khoản + tên chủ tài khoản của phòng khám, không lưu mật khẩu hay
         credential ngân hàng nào; mã QR chỉ hiển thị sau khi đã cấu hình. -->
    <el-dialog
      title="Cấu hình tài khoản VietQR"
      :visible.sync="vietqrDialogVisible"
      :close-on-click-modal="false"
      width="50%"
    >
      <el-form :model="vietqrForm" label-width="160px">
        <el-form-item label="Mã BIN ngân hàng">
          <el-input v-model="vietqrForm.bankBin" placeholder="6 chữ số, ví dụ 970422" maxlength="6"></el-input>
        </el-form-item>
        <el-form-item label="Tên ngân hàng">
          <el-input v-model="vietqrForm.bankName" placeholder="Tùy chọn, ví dụ MB Bank" maxlength="50"></el-input>
        </el-form-item>
        <el-form-item label="Số tài khoản nhận tiền">
          <el-input v-model="vietqrForm.accountNo" placeholder="Chỉ chữ số, 6–20 ký tự" maxlength="20"></el-input>
        </el-form-item>
        <el-form-item label="Tên chủ tài khoản">
          <el-input v-model="vietqrForm.accountName" placeholder="Tên in trên mã QR" maxlength="50"></el-input>
        </el-form-item>
      </el-form>
      <div style="color: #909399">
        Chỉ dùng để hiển thị mã QR cho bệnh nhân quét và chuyển khoản; hệ thống không tự động xác nhận thanh toán và không lưu mật khẩu ngân hàng.
      </div>
      <div slot="footer">
        <el-button @click="vietqrDialogVisible = false">Hủy</el-button>
        <el-button type="primary" :loading="vietqrSaving" @click="saveVietqrConfigForm">Lưu cấu hình</el-button>
      </div>
    </el-dialog>
    <el-dialog
      title="Chi tiết hoàn phí"
      :visible.sync="refundDialogVisible"
      :close-on-click-modal="false"
      width="50%"
    >
      <div>
        <el-divider></el-divider>
          <el-card
          class="prescriptionCard"
          v-for="(refundPrescriptionMain, index) in refundPrescriptionMainList"
          :key="index"
        >
          <div v-if="refundPrescriptionMain.prescriptionType === 1">
            <el-table :data="refundPrescriptionMain.prescriptionDetail" style="width: 100%">
              <el-table-column align="left" width="55px">
                <template slot="header">
                  <el-checkbox v-model="refundPrescriptionMain.isSelect"></el-checkbox>
                </template>
              </el-table-column>
              <el-table-column prop="goodsName" label="Đơn thuốc tây" width="width">
              </el-table-column>
              <el-table-column prop="factoryName" label="Nhà sản xuất" width="width">
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá" width="width">
              </el-table-column>
              <el-table-column prop="quantity" label="Số lượng" width="width">
              </el-table-column>
              <el-table-column prop="unit" label="Đơn vị" width="width">
              </el-table-column>
              <el-table-column prop="totalCost" label="Tổng tiền" width="width">
              </el-table-column>
            </el-table>
          </div>
          <!--Thuốc đông y-->
          <div v-else-if="refundPrescriptionMain.prescriptionType === 2">
            <el-table  :data="refundPrescriptionMain.prescriptionDetail" style="width: 100%">
              <el-table-column align="left"  width="55px">
                <template slot="header">
                  <el-checkbox v-model="refundPrescriptionMain.isSelect"></el-checkbox>
                </template>
              </el-table-column>
              <el-table-column prop="goodsName" label="Đơn thuốc đông y" width="width">
              </el-table-column>
              <el-table-column prop="factoryName" label="Nhà sản xuất" width="width">
              </el-table-column>
              <el-table-column prop="tisanesMethod" label="Cách sắc thuốc" width="width">
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá" width="width">
              </el-table-column>
              <el-table-column prop="quantity" label="Số lượng" width="width">
              </el-table-column>
              <el-table-column prop="totalQuantity" label="Tổng số lượng" width="width">
              </el-table-column>
              <el-table-column prop="totalCost" label="Tổng tiền" width="width">
              </el-table-column>
            </el-table>
          </div>
          <!--Phí phụ thêm-->
          <div v-if="refundPrescriptionMain.prescriptionType === 3">
            <el-table :data="refundPrescriptionMain.prescriptionDetail" style="width: 100%">
              <el-table-column align="left"  width="55px">
                <template slot="header">
                  <el-checkbox v-model="refundPrescriptionMain.isSelect"></el-checkbox>
                </template>
              </el-table-column>
              <el-table-column prop="goodsName" label="Phí phụ thêm" width="width">
              </el-table-column>
              <el-table-column prop="quantity" label="Số lượng" width="width">
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá" width="width">
              </el-table-column>
              <el-table-column prop="totalCost" label="Tổng cộng" width="width">
              </el-table-column>
            </el-table>
          </div>
        </el-card>
        <el-divider></el-divider>
      </div>
      <div slot="footer" class="flex-space-between">
        <div>Số tiền hoàn thực tế: {{ formatVND(getRefundTotalCost()) }}</div>
        <div>
          <el-button @click="refundDialogVisible = false">Hủy</el-button>
          <el-button
            type="primary"
            :loading="chargeSubmitting"
            @click="confirmRefund"
            >Xác nhận</el-button
          >
        </div>
      </div>
    </el-dialog>
    <!-- Tổng kết thu phí cuối ngày (G6): chỉ đọc, không ghi DB; CSV
         tạo phía client từ JSON API (UTF-8 BOM, tiêu đề tiếng Việt). -->
    <el-dialog
      title="Tổng kết thu phí cuối ngày"
      :visible.sync="chargeSummaryDialogVisible"
      :close-on-click-modal="false"
      width="80%"
    >
      <div style="display: flex; align-items: center; gap: 8px">
        <span>Ngày phòng khám:</span>
        <el-date-picker
          v-model="chargeSummaryDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="Chọn ngày"
        ></el-date-picker>
        <el-button type="primary" :loading="chargeSummaryLoading" @click="loadChargeSummary">Xem tổng kết</el-button>
        <el-button :disabled="!chargeSummaryData" @click="downloadChargeSummaryCsv">Tải CSV</el-button>
      </div>
      <div v-if="chargeSummaryData" style="margin-top: 12px">
        <div>
          Ngày {{ chargeSummaryData.date }} (giờ phòng khám Asia/Ho_Chi_Minh) —
          {{ chargeSummaryData.rows.length }} phiếu thu phí,
          {{ chargeSummaryData.refunds.length }} dòng hoàn phí đã ghi audit.
        </div>
        <el-table :data="chargeSummaryData.rows" style="width: 100%; margin-top: 8px">
          <el-table-column prop="tollNumber" label="Số phiếu thu phí"></el-table-column>
          <el-table-column prop="patientName" label="Bệnh nhân"></el-table-column>
          <el-table-column prop="paymentTypeName" label="Phương thức thanh toán"></el-table-column>
          <el-table-column prop="tollTypeName" label="Loại thu phí"></el-table-column>
          <el-table-column prop="amountReceivable" label="Phải thu (VNĐ)" :formatter="summaryMoneyFormatter"></el-table-column>
          <el-table-column prop="amountReceived" label="Thực thu (VNĐ)" :formatter="summaryMoneyFormatter"></el-table-column>
          <el-table-column prop="amountDiscounted" label="Ưu đãi (VNĐ)" :formatter="summaryMoneyFormatter"></el-table-column>
          <el-table-column prop="returnType" label="Hoàn phí"></el-table-column>
          <el-table-column prop="createDate" label="Thời gian tạo"></el-table-column>
        </el-table>
        <div style="margin-top: 12px; font-weight: bold">
          Tổng cộng: {{ chargeSummaryData.totals.chargeCount }} phiếu —
          Phải thu {{ formatVND(chargeSummaryData.totals.totalReceivable) }} —
          Thực thu {{ formatVND(chargeSummaryData.totals.totalReceived) }} —
          Ưu đãi {{ formatVND(chargeSummaryData.totals.totalDiscounted) }}
        </div>
        <div style="margin-top: 8px">
          <div style="font-weight: bold">Theo phương thức thanh toán:</div>
          <div v-for="item in chargeSummaryData.totals.byPaymentMethod" :key="item.paymentMethod">
            {{ item.paymentMethod }}: {{ item.count }} phiếu —
            Phải thu {{ formatVND(item.totalReceivable) }} —
            Thực thu {{ formatVND(item.totalReceived) }}
          </div>
        </div>
        <div style="margin-top: 8px">
          <div style="font-weight: bold">
            Hoàn phí (theo audit log): {{ chargeSummaryData.totals.refundCount }} dòng —
            Tổng hoàn {{ formatVND(chargeSummaryData.totals.refundTotal) }}
          </div>
          <el-table :data="chargeSummaryData.refunds" style="width: 100%; margin-top: 8px">
            <el-table-column prop="tollId" label="Phiếu hoàn"></el-table-column>
            <el-table-column prop="amountReceived" label="Số tiền hoàn (VNĐ)" :formatter="summaryMoneyFormatter"></el-table-column>
            <el-table-column prop="paymentType" label="Phương thức"></el-table-column>
            <el-table-column prop="createBy" label="Người thực hiện"></el-table-column>
            <el-table-column prop="createDate" label="Thời gian"></el-table-column>
          </el-table>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="chargeSummaryDialogVisible = false">Đóng</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRegistrationPages } from "@/api/outpatient/registration";
import { allQueryMedicalRecord } from "@/api/outpatient/medicalRecord";
import { saveTollTollInfo } from "@/api/toll/tollInfo";
import { getVietqrConfig, saveVietqrConfig } from "@/api/vietqr";
import { getDayChargeSummary } from "@/api/chargeSummary";
import { buildVietqrPayload } from "@/utils/vietqr";
import QRCode from "qrcodejs2";
import { getLocalRouters } from "@/utils/auth";

// Tab trang thu phí -> recipel_info.charge_status, cùng quy ước với trang
// Thu phí (tollInfo): 0 = Chờ thu phí, 1 = Đã thu phí, -1 = Đã hoàn phí.
// registration/v2/list lọc theo recipel_info.charge_status nên sau khi thu
// phí, đơn thuốc chuyển 0 -> 1 và bệnh nhân tự chuyển tab 1 -> tab 2.
const TAB_CHARGE_STATUS = { "1": 0, "2": 1, "3": -1 };
const TAB_ORDER_COLUMN = {
  "1": "reception_end_date",
  "2": "charge_date",
  "3": "retreats_date",
};
const PAYMENT_TYPE_NAMES = {
  payType_0: "Tiền mặt",
  payType_5: "Chuyển khoản ngân hàng",
  payType_6: "Ví điện tử",
  payType_3: "Thẻ ngân hàng",
};

export default {
  name: 'ChargeIndex',
  data() {
    let cardIdNo = (rule, value, callback) => {
      if (!value) {
        return callback(new Error("Số CMND/CCCD không được để trống"));
      } else {
        let reg = /((^\d{9}$)|(^\d{12}$)|(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$))/;
        if (!reg.test(value)) {
          callback(new Error("Vui lòng nhập đúng số CMND/CCCD"));
        } else {
          callback();
        }
      }
    };
    return {
      isRetail: false, // Bán lẻ mở trang retailCharge, không nhập tay ở đây
      selectCondition: "", // Điều kiện truy vấn bệnh nhân
      chargeStatusActiveName: "1", // Tab trạng thái thu phí
      chargeDialogVisible: false,
      chargeForm: {
        discountAmount: 0,
        discountRate: 0,
        actualAmount: 0,
        note: "",
        paymentType: "payType_0",
      },
      chargeSubmitting: false,
      // VietQR (G4): cấu hình của tenant đang đăng nhập; mặc định chưa
      // cấu hình thì không vẽ mã QR nào (không hard-code tài khoản).
      vietqrConfig: null,
      vietqrConfigured: false,
      vietqrLoading: false,
      vietqrDialogVisible: false,
      vietqrForm: {
        bankBin: "",
        bankName: "",
        accountNo: "",
        accountName: "",
      },
      vietqrSaving: false,
      refundDialogVisible: false,
      // Tổng kết ngày (G6): mặc định là hôm nay theo giờ phòng khám.
      chargeSummaryDialogVisible: false,
      chargeSummaryDate: "",
      chargeSummaryData: null,
      chargeSummaryLoading: false,
      patientList: [], // Lấy từ registration/v2/list, không còn dữ liệu mẫu
      patientTotalCount: 0, // Bệnh nhân tổng số
      patientPageSize: 15, // Số bệnh nhân hiển thị mỗi trang
      patientCurrentPage: 1, // Bệnh nhân trang hiện tại
      selectedRegistration: null, // Đăng ký khám đang chọn (dòng bảng trái)
      selectedPatient: null, // Bệnh nhân của đăng ký đang chọn
      medicalRecord: null, // Bệnh án của đăng ký đang chọn
      patientInfoForm: {
        name: "",
        gender: "",
        age_year: "",
        age_month: "",
        telephone: "",
        idCardNo: "",
      },
      patientInfoNoRules: null,
      patientInfoRules: {
        name: [
          { required: true, message: "Vui lòng nhập họ tên", trigger: "blur" },
          { min: 1, max: 50, message: "Độ dài trong khoảng1đến50ký tự", trigger: "blur" },
        ],
        gender: [{ required: true, message: "Vui lòng chọn giới tính", trigger: "blur" }],
        age_year: [
          { required: true, message: "Vui lòng nhập tuổi", trigger: "blur" },
        ],
        age_month: [
          { required: true, message: "Vui lòng nhập tuổi (tháng)", trigger: "blur" },
        ],
        telephone: [
          { required: true, message: "Vui lòng nhập số điện thoại liên hệ", trigger: "blur" },
          { min: 1, max: 20, message: "Độ dài trong khoảng1-20ký tự", trigger: "blur" },
        ],
        idCardNo: [
          {
            required: true,
            trigger: "blur",
            validator: cardIdNo,
          },
        ],
      },
      genderList: [],
      // Danh sách đơn thuốc chờ/đã thu phí, lấy từ medicalRecord/v2/allQuery
      // rồi lọc theo charge_status của tab; mỗi phần tử giữ thêm _recipelId
      // và trạng thái phát thuốc để chặn hoàn phí sai quy trình.
      prescriptionMainList: [],
      refundPrescriptionMainList: [],
      // Biến chân trang đơn thuốc đông y (trước đây không khai báo nên
      // luôn rỗng); nạp từ đơn đông y đầu tiên khi chọn bệnh nhân.
      yypc: "",
      fypc: "",
      mcyl: "",
      pz: 0,
      zj: 0,
    };
  },
  computed: {
    companyId() {
      try {
        return JSON.parse(sessionStorage.getItem("currentUser")).company.id;
      } catch (e) {
        return null;
      }
    },
    // Tổng phải thu của các đơn đang hiển thị (phải thu theo đơn, VND).
    chargeTotalFee() {
      return this.prescriptionMainList.reduce(
        (sum, item) => sum + this.toNum(item.totalCost),
        0
      );
    },
    // Số tiền mã VietQR mang theo = số tiền thực thu trong dialog (đã
    // trừ ưu đãi), làm tròn xuống VNĐ nguyên; <= 0 thì không vẽ mã.
    vietqrAmount() {
      return Math.floor(this.toNum(this.chargeForm.actualAmount));
    },
    // Nội dung chuyển khoản: mã đăng ký đang thu phí, để thu ngân đối
    // soát với đúng lượt khám; không dùng tên bệnh nhân (tránh dấu).
    vietqrContent() {
      return this.selectedRegistration && this.selectedRegistration.id
        ? "DK" + this.selectedRegistration.id
        : "";
    },
    // Payload EMV VietQR; rỗng khi chưa cấu hình, chưa chọn bệnh nhân
    // hoặc số tiền <= 0 — template chỉ vẽ mã khi payload khác rỗng.
    vietqrPayload() {
      try {
        if (!this.vietqrConfigured || !this.vietqrConfig || !this.vietqrContent) {
          return "";
        }
        return buildVietqrPayload(this.vietqrConfig, this.vietqrAmount, this.vietqrContent);
      } catch (e) {
        return "";
      }
    },
  },
  watch: {
    // Vẽ lại mã QR khi mở dialog thu phí, đổi phương thức sang chuyển
    // khoản, hoặc sửa số tiền thực thu (QR mang đúng số tiền đó).
    chargeDialogVisible(visible) {
      if (visible) {
        this.loadVietqrConfig();
      }
    },
    "chargeForm.paymentType"() {
      this.renderVietqr();
    },
    "chargeForm.actualAmount"() {
      this.renderVietqr();
    },
  },
  created() {
    this.selectPatientList();
  },
  methods: {
    toNum(value) {
      const num = parseFloat(value);
      return isNaN(num) ? 0 : num;
    },
    formatVND(value) {
      return this.toNum(value).toLocaleString("vi-VN") + " VNĐ";
    },
    handleSearch() {
      this.patientCurrentPage = 1;
      this.selectPatientList();
    },
    onChargeTabClick() {
      this.patientCurrentPage = 1;
      this.resetDetail();
      this.selectPatientList();
    },
    onPatientPage(page) {
      if (typeof page === "number") {
        this.patientCurrentPage = page;
      }
      this.selectPatientList();
    },
    /* Truy vấn bệnh nhân theo tab trạng thái thu phí (API thật). */
    selectPatientList(page) {
      if (typeof page === "number") {
        this.patientCurrentPage = page;
      }
      if (!this.companyId) {
        this.$message.error("Chưa có thông tin phòng khám trong phiên đăng nhập");
        return;
      }
      const condition = {
        limit: this.patientPageSize,
        offset: (this.patientCurrentPage - 1) * this.patientPageSize,
        companyId: this.companyId,
        // Bắt buộc giống tollInfo: backend lọc a.recipe_status = ? nên
        // thiếu trường này là list trả 0 dòng (recipe_status NULL).
        recipeStatus: "0",
        columnName: TAB_ORDER_COLUMN[this.chargeStatusActiveName],
        order: "desc",
        chargeStatus: TAB_CHARGE_STATUS[this.chargeStatusActiveName],
        updateDate: "2022-06-01 00:00:00",
        status: "registrationStatus_1",
        patientName: this.selectCondition.trim(),
        patientCode: this.selectCondition.trim(),
      };
      listRegistrationPages(condition)
        .then((responseData) => {
          if (responseData.code !== 100) {
            this.$message.error(responseData.msg || "Không tải được danh sách bệnh nhân");
            return;
          }
          const rows = (responseData.data && responseData.data.rows) || [];
          // Cùng cách lọc với tollInfo: bỏ đăng ký mà mọi đơn đã hủy.
          const filtered = rows.filter((item) => item.recipeStatus !== 1);
          const removed = rows.length - filtered.length;
          this.patientTotalCount =
            this.chargeStatusActiveName === "1"
              ? (responseData.data.total || 0) - removed
              : responseData.data.total || 0;
          this.patientList = filtered.map((row) => ({
            id: row.id,
            name: (row.patientId && row.patientId.name) || "",
            clinicDate: row.receptionEndDate || row.receptionStartDate || "",
            chargeStatus:
              this.chargeStatusActiveName === "1"
                ? "Chờ thu phí"
                : this.chargeStatusActiveName === "2"
                ? "Đã thu phí"
                : "Đã hoàn phí",
            _registration: row,
          }));
        })
        .catch((error) => {
          this.$message.error(error && error.message ? error.message : String(error));
        });
    },
    /* Nhấn vào danh sách bệnh nhân: nạp bệnh án + đơn thuốc thật. */
    selectPatientChange(row) {
      if (!row || !row._registration) {
        return;
      }
      this.isRetail = false;
      this.$nextTick(() => {
        if (this.$refs["patientInfoForm"]) {
          this.$refs["patientInfoForm"].clearValidate();
        }
      });
      this.selectedRegistration = row._registration;
      this.loadPrescriptions(row._registration.id);
    },
    loadPrescriptions(registrationId) {
      allQueryMedicalRecord(registrationId)
        .then((responseData) => {
          if (responseData.code !== 100) {
            this.$message.error(responseData.msg || "Không tải được đơn thuốc");
            return;
          }
          const data = responseData.data || {};
          const patient = data.patient || {};
          this.selectedPatient = patient;
          this.medicalRecord = data.medicalRecord || null;
          this.patientInfoForm = {
            name: patient.name || "",
            gender: (patient.gender && patient.gender.value) || "",
            age_year: patient.age || "",
            age_month: patient.month || "",
            telephone: patient.phone || "",
            idCardNo: patient.card || "",
          };
          this.genderList =
            patient.gender && patient.gender.value
              ? [{ id: patient.gender.value, name: patient.gender.name }]
              : [];
          const tabCharge = TAB_CHARGE_STATUS[this.chargeStatusActiveName];
          const evtList = data.recipelInfoEvtList || [];
          const mapped = evtList
            .filter((evt) => {
              const recipel = evt.recipelInfo || {};
              if (this.chargeStatusActiveName !== "3" && recipel.status === -1) {
                return false;
              }
              return recipel.chargeStatus === tabCharge;
            })
            .map((evt) => this.mapRecipel(evt));
          this.prescriptionMainList = mapped;
          this.refundPrescriptionMainList = mapped.map((item) => ({
            ...item,
            prescriptionDetail: item.prescriptionDetail.map((detail) => ({ ...detail })),
            isSelect: true,
          }));
          const chinese = mapped.filter((item) => item.prescriptionType === 2);
          this.pz = chinese.reduce((sum, item) => sum + this.toNum(item.quantity), 0);
          this.zj = chinese.reduce((sum, item) => sum + this.toNum(item.totalCost), 0);
          if (chinese.length > 0) {
            this.yypc = chinese[0]._frequencyName || "";
            this.fypc = chinese[0]._takeFrequencyName || "";
            this.mcyl = chinese[0]._singleDosage || "";
          } else {
            this.yypc = "";
            this.fypc = "";
            this.mcyl = "";
          }
          if (mapped.length === 0) {
            this.$message.info("Bệnh nhân này chưa có đơn thuốc ở trạng thái này");
          }
        })
        .catch((error) => {
          this.$message.error(error && error.message ? error.message : String(error));
        });
    },
    /* Đổi RecipelInfoEvt thật sang định dạng bảng của trang này. */
    mapRecipel(evt) {
      const recipel = evt.recipelInfo || {};
      const details = evt.recipelDetailEvtList || [];
      const typeValue = recipel.recipelType && recipel.recipelType.value;
      const prescriptionType =
        typeValue === "recipelType_0" ? 1 : typeValue === "recipelType_1" ? 2 : 3;
      const detailRows = details.map((detail) => {
        const drugStuff = detail.drugStuffId || {};
        const drug = drugStuff.drug || {};
        const stuff = drugStuff.stuff || {};
        const costItem = drugStuff.costItem || {};
        // Backend chỉ điền DrugStuffEvt khi chi tiết có stuffType hợp lệ;
        // đơn thiếu stuffType trả về đối tượng rỗng nên fallback sang các
        // trường con để không hiện dòng thuốc trống tên.
        const factoryObj = drug.factory;
        const factoryName =
          (factoryObj && factoryObj.name) ||
          (typeof factoryObj === "string" ? factoryObj : "") ||
          (stuff.factory && stuff.factory.name) ||
          "";
        const unit =
          (drugStuff.dosisUnit && drugStuff.dosisUnit.name) ||
          (drugStuff.preparationUnit && drugStuff.preparationUnit.name) ||
          (drugStuff.pack && drugStuff.pack.name) ||
          detail.unitType ||
          "";
        return {
          goodsName:
            drugStuff.name || drug.goodsName || stuff.name || costItem.itemName || "",
          factoryName: factoryName,
          tisanesMethod:
            (detail.chineseMedicineUse && detail.chineseMedicineUse.name) || "",
          price: detail.unitPrice,
          quantity: detail.total,
          unit: unit,
          totalQuantity: detail.minTotal || detail.total,
          totalCost: detail.allFee,
        };
      });
      const detailTotal = detailRows.reduce(
        (sum, detail) => sum + this.toNum(detail.totalCost),
        0
      );
      return {
        prescriptionType: prescriptionType,
        isSelect: true,
        quantity: recipel.dosage || details.length,
        useMethod: (recipel.recipelUse && recipel.recipelUse.name) || "",
        totalCost: recipel.fee != null ? recipel.fee : detailTotal,
        prescriptionDetail: detailRows,
        recipelId: recipel.id,
        _chargeStatus: recipel.chargeStatus,
        _dispensionStatus: recipel.dispensionStatus,
        _isDispension: recipel.isDispension,
        _frequencyName: (recipel.frequency && recipel.frequency.name) || "",
        _takeFrequencyName: (recipel.takeFrequency && recipel.takeFrequency.name) || "",
        _singleDosage: recipel.singleDosage || "",
      };
    },

    summaryMoneyFormatter(row, column, value) {
      return this.formatVND(value);
    },
    // Tổng kết ngày (G6): ngày mặc định là hôm nay theo giờ phòng khám
    // (Asia/Ho_Chi_Minh), không lấy ngày UTC của máy chủ.
    openChargeSummary() {
      if (!this.chargeSummaryDate) {
        const now = new Date(
          new Date().toLocaleString("en-US", { timeZone: "Asia/Ho_Chi_Minh" })
        );
        const y = now.getFullYear();
        const m = String(now.getMonth() + 1).padStart(2, "0");
        const d = String(now.getDate()).padStart(2, "0");
        this.chargeSummaryDate = y + "-" + m + "-" + d;
      }
      this.chargeSummaryDialogVisible = true;
      this.loadChargeSummary();
    },
    loadChargeSummary() {
      if (!this.chargeSummaryDate) {
        this.$message.error("Vui lòng chọn ngày tổng kết");
        return;
      }
      this.chargeSummaryLoading = true;
      getDayChargeSummary(this.chargeSummaryDate)
        .then((res) => {
          this.chargeSummaryData = res.data || res;
        })
        .catch(() => {
          this.$message.error("Không tải được tổng kết ngày đã chọn");
        })
        .finally(() => {
          this.chargeSummaryLoading = false;
        });
    },
    // CSV tạo phía client từ JSON API: UTF-8 BOM để Excel mở đúng
    // tiếng Việt; một dòng mỗi phiếu + các dòng tổng kết ở cuối.
    downloadChargeSummaryCsv() {
      if (!this.chargeSummaryData) {
        return;
      }
      const esc = (v) => {
        const text = v === null || v === undefined ? "" : String(v);
        return '"' + text.replace(/"/g, '""') + '"';
      };
      const lines = [];
      lines.push(
        [
          "Số phiếu thu phí",
          "Bệnh nhân",
          "Phương thức thanh toán",
          "Loại thu phí",
          "Phải thu (VNĐ)",
          "Thực thu (VNĐ)",
          "Ưu đãi (VNĐ)",
          "Hoàn phí",
          "Thời gian tạo",
        ].map(esc).join(",")
      );
      this.chargeSummaryData.rows.forEach((row) => {
        lines.push(
          [
            row.tollNumber,
            row.patientName,
            row.paymentTypeName,
            row.tollTypeName,
            row.amountReceivable,
            row.amountReceived,
            row.amountDiscounted,
            row.returnType,
            row.createDate,
          ].map(esc).join(",")
        );
      });
      const t = this.chargeSummaryData.totals;
      lines.push(["", "", "", "TỔNG CỘNG", t.totalReceivable, t.totalReceived, t.totalDiscounted, "", ""].map(esc).join(","));
      t.byPaymentMethod.forEach((item) => {
        lines.push(["", "", item.paymentMethod, "Theo phương thức", item.totalReceivable, item.totalReceived, item.totalDiscounted, "", ""].map(esc).join(","));
      });
      lines.push(["", "", "", "HOÀN PHÍ (AUDIT)", "", t.refundTotal, "", t.refundCount + " dòng", ""].map(esc).join(","));
      const blob = new Blob(["\uFEFF" + lines.join("\r\n")], {
        type: "text/csv;charset=utf-8",
      });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "tong-ket-thu-phi-" + this.chargeSummaryData.date + ".csv";
      a.click();
      URL.revokeObjectURL(url);
    },
    /* VietQR (G4): tải cấu hình của tenant; lỗi tải không chặn thu phí
       (lễ tân vẫn thu bằng các phương thức khác), chỉ ẩn mã QR.
       Vẽ mã trong .finally, SAU khi vietqrLoading về false: nếu vẽ
       trong .then, nhánh v-if="vietqrLoading" vẫn đang hiển thị nên
       div #vietqrCode chưa tồn tại, callback $nextTick trả về sớm và
       mã không bao giờ được vẽ lại (đã gặp thực tế ở nhánh lưu qua UI). */
    loadVietqrConfig() {
      this.vietqrLoading = true;
      return getVietqrConfig()
        .then((responseData) => {
          const data = (responseData && responseData.data) || {};
          this.vietqrConfigured = !!data.configured && !!data.config;
          this.vietqrConfig = data.config || null;
        })
        .catch(() => {
          this.vietqrConfigured = false;
          this.vietqrConfig = null;
        })
        .finally(() => {
          this.vietqrLoading = false;
          this.renderVietqr();
        });
    },
    /* Vẽ (lại) mã QR vào div #vietqrCode khi dialog đang mở, đã chọn
       chuyển khoản và đã có payload; các trường hợp còn lại xóa mã cũ. */
    renderVietqr() {
      this.$nextTick(() => {
        const el = document.getElementById("vietqrCode");
        if (!el) {
          return;
        }
        el.innerHTML = "";
        if (
          this.chargeDialogVisible &&
          this.chargeForm.paymentType === "payType_5" &&
          this.vietqrPayload
        ) {
          new QRCode(el, {
            text: this.vietqrPayload,
            width: 200,
            height: 200,
            correctLevel: QRCode.CorrectLevel.M,
          });
        }
      });
    },
    openVietqrConfig() {
      this.vietqrForm = {
        bankBin: (this.vietqrConfig && this.vietqrConfig.bankBin) || "",
        bankName: (this.vietqrConfig && this.vietqrConfig.bankName) || "",
        accountNo: (this.vietqrConfig && this.vietqrConfig.accountNo) || "",
        accountName: (this.vietqrConfig && this.vietqrConfig.accountName) || "",
      };
      this.vietqrDialogVisible = true;
    },
    saveVietqrConfigForm() {
      const form = {
        bankBin: (this.vietqrForm.bankBin || "").trim(),
        bankName: (this.vietqrForm.bankName || "").trim(),
        accountNo: (this.vietqrForm.accountNo || "").trim(),
        accountName: (this.vietqrForm.accountName || "").trim(),
      };
      if (!/^\d{6}$/.test(form.bankBin)) {
        this.$message.error("Mã BIN ngân hàng phải đúng 6 chữ số.");
        return;
      }
      if (!/^\d{6,20}$/.test(form.accountNo)) {
        this.$message.error("Số tài khoản phải là 6–20 chữ số.");
        return;
      }
      if (!form.accountName) {
        this.$message.error("Tên chủ tài khoản không được để trống.");
        return;
      }
      this.vietqrSaving = true;
      saveVietqrConfig(form)
        .then((responseData) => {
          if (responseData.code !== 100) {
            this.$message.error(responseData.msg || "Không lưu được cấu hình VietQR");
            return;
          }
          this.vietqrDialogVisible = false;
          this.$message.success("Đã lưu cấu hình VietQR");
          this.loadVietqrConfig();
        })
        .catch((error) => {
          this.$message.error(
            (error && (error.msg || error.message)) || "Không lưu được cấu hình VietQR"
          );
        })
        .finally(() => {
          this.vietqrSaving = false;
        });
    },
    /* Nhấn nút thu phí / hoàn phí. */
    chargeClick() {
      if (!this.selectedRegistration) {
        this.$message.warning("Vui lòng chọn bệnh nhân trước");
        return;
      }
      if (this.prescriptionMainList.length === 0) {
        this.$message.warning("Bệnh nhân này chưa có đơn thuốc ở trạng thái này");
        return;
      }
      if (this.chargeStatusActiveName === "1") {
        this.chargeForm.actualAmount = Math.max(
          0,
          this.chargeTotalFee - this.toNum(this.chargeForm.discountAmount)
        );
        this.chargeDialogVisible = true;
      } else if (this.chargeStatusActiveName === "2") {
        this.refundPrescriptionMainList = this.prescriptionMainList.map((item) => ({
          ...item,
          prescriptionDetail: item.prescriptionDetail.map((detail) => ({ ...detail })),
          isSelect: true,
        }));
        this.refundDialogVisible = true;
      }
    },
    getPrescriptionTotalCost(data) {
      let totalCost = 0;
      (data || []).forEach((element) => {
        if (element.totalCost) {
          totalCost += this.toNum(element.totalCost);
        }
      });
      return totalCost;
    },
    /* Tổng hoàn phí chỉ tính các đơn đã tick chọn. */
    getRefundTotalCost() {
      let totalCost = 0;
      this.refundPrescriptionMainList.forEach((element) => {
        if (element.isSelect && element.totalCost) {
          totalCost += this.toNum(element.totalCost);
        }
      });
      return totalCost;
    },
    /* Chuyển sang bán lẻ: mở trang bán lẻ thật, không nhập tay ở đây. */
    changeToRetail() {
      this.isRetail = true;
      this.patientInfoForm = {
        name: "",
        gender: "",
        age_year: "",
        age_month: "",
        telephone: "",
        idCardNo: "",
      };
    },
    openRetailCharge() {
      try {
        const routers = getLocalRouters() || [];
        const target = routers.find(
          (r) => r.code && String(r.code).indexOf("retailCharge") !== -1 && r.url
        );
        if (target) {
          this.$router.push(target.url);
          return;
        }
      } catch (e) {
        // Rơi xuống thông báo bên dưới.
      }
      this.$message.info(
        "Mở từ menu: Thu phí bán lẻ (retailCharge) — route chưa có trong phiên đăng nhập này"
      );
    },
    clinicDateFormatter(row) {
      const date = new Date(row.clinicDate);
      if (isNaN(date.getTime())) {
        return row.clinicDate || "";
      }
      const pad = (num) => String(num).padStart(2, "0");
      return (
        pad(date.getDate()) +
        "/" +
        pad(date.getMonth() + 1) +
        "/" +
        date.getFullYear() +
        " " +
        pad(date.getHours()) +
        ":" +
        pad(date.getMinutes())
      );
    },
    paymentTypeObj() {
      const value = this.chargeForm.paymentType || "payType_0";
      return { name: PAYMENT_TYPE_NAMES[value] || "Tiền mặt", value: value };
    },
    // Chiết khấu nhập theo thang /10 (10 = không giảm); để trống hoặc 0
    // nghĩa là không giảm, hệ số 1 — backend nhân hệ số này vào thực thu
    // của từng chi tiết đơn nên 0 sẽ làm thực thu thành 0 nếu gửi thẳng.
    discountFactor() {
      const rate = this.toNum(this.chargeForm.discountRate);
      return rate > 0 ? rate / 10 : 1;
    },
    buildTollInfo(amountStatus, receivable, received, discounted) {
      const patientId =
        (this.selectedPatient && this.selectedPatient.id) ||
        (this.selectedRegistration &&
          this.selectedRegistration.patientId &&
          this.selectedRegistration.patientId.id) ||
        "";
      return {
        company: { id: this.companyId },
        medical: this.medicalRecord,
        amountReceivable: receivable,
        amountReceived: received,
        amountDiscounted: discounted,
        discount: this.discountFactor(),
        paymentType: this.paymentTypeObj(),
        patient: { id: patientId },
        tollType: { name: "", value: "" },
        amountStatus: amountStatus,
      };
    },
    resetDetail() {
      this.selectedRegistration = null;
      this.selectedPatient = null;
      this.medicalRecord = null;
      this.prescriptionMainList = [];
      this.refundPrescriptionMainList = [];
      this.genderList = [];
      this.patientInfoForm = {
        name: "",
        gender: "",
        age_year: "",
        age_month: "",
        telephone: "",
        idCardNo: "",
      };
    },
    submitToll(model, dialog) {
      this.chargeSubmitting = true;
      saveTollTollInfo(model, "null")
        .then((responseData) => {
          if (responseData.code === 100) {
            this.$message.success("Thao tác thành công");
            if (dialog === "charge") {
              this.chargeDialogVisible = false;
            } else {
              this.refundDialogVisible = false;
            }
            this.resetDetail();
            this.selectPatientList();
          } else {
            this.$message.error(responseData.msg || "Thu phí thất bại");
          }
        })
        .catch((error) => {
          this.$message.error(error && error.message ? error.message : String(error));
        })
        .finally(() => {
          this.chargeSubmitting = false;
        });
    },
    /* Xác nhận thu phí: toàn bộ đơn đang hiển thị (đã lọc theo tab). */
    confirmCharge() {
      if (!this.selectedRegistration) {
        this.$message.warning("Vui lòng chọn bệnh nhân trước");
        return;
      }
      if (!this.medicalRecord || !this.medicalRecord.id) {
        this.$message.error("Đăng ký này chưa có bệnh án nên chưa thu phí được");
        return;
      }
      if (this.prescriptionMainList.length === 0) {
        this.$message.warning("Chưa có đơn thuốc để thu phí");
        return;
      }
      const total = this.chargeTotalFee;
      const model = {
        tollInfo: this.buildTollInfo(
          { name: "Đã thu phí", value: "amountStatus_1" },
          total,
          this.toNum(this.chargeForm.actualAmount),
          this.toNum(this.chargeForm.discountAmount)
        ),
        recipelInfos: this.prescriptionMainList.map((item) => ({
          recipelInfo: { id: item.recipelId },
        })),
        // Toàn bộ đơn hiển thị đều được thu nên trạng thái chung là 2
        // (cùng quy ước tollInfo); từng đơn chuyển charge_status 0 -> 1
        // trong TollInfoService.processNormalCharge.
        chargeStatus: "2",
      };
      this.submitToll(model, "charge");
    },
    /* Xác nhận hoàn phí: chỉ các đơn đã tick; chặn khi đã phát thuốc. */
    confirmRefund() {
      if (!this.selectedRegistration) {
        this.$message.warning("Vui lòng chọn bệnh nhân trước");
        return;
      }
      const selected = this.refundPrescriptionMainList.filter((item) => item.isSelect);
      if (selected.length === 0) {
        this.$message.warning("Vui lòng chọn ít nhất một đơn thuốc để hoàn phí");
        return;
      }
      // Cùng luật với tollInfo + backend processRefund: đơn đã phát thuốc
      // (dispensionStatus = 1 hoặc isDispension = "1") phải trả thuốc
      // trước, không hoàn phí trực tiếp.
      const dispensed = selected.some(
        (item) => item._dispensionStatus === 1 || item._isDispension === "1"
      );
      if (dispensed) {
        this.$message.error(
          "Có đơn thuốc đã phát thuốc nhưng chưa trả thuốc, vui lòng trả thuốc trước"
        );
        return;
      }
      const total = selected.reduce(
        (sum, item) => sum + this.toNum(item.totalCost),
        0
      );
      const model = {
        tollInfo: this.buildTollInfo(
          { name: "Đã hoàn phí", value: "amountStatus_2" },
          total,
          total,
          0
        ),
        recipelInfos: selected.map((item) => ({
          recipelInfo: { id: item.recipelId },
        })),
        // Hoàn đủ các đơn hiển thị -> 4, hoàn một phần -> 3.
        chargeStatus:
          selected.length === this.prescriptionMainList.length ? "4" : "3",
      };
      this.submitToll(model, "refund");
    },
  },
};
</script>



<style scoped>
.charge_container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: space-around;
}

.charge_left {
  width: 30%;
  height: 100%;
}

.charge_right {
  height: 100%;
  width: 67%;
}

.patientinfo_form {
  background: #f2f2f2;
  border-radius: 10px;
  margin: 10px 0px 10px 0px;
  padding: 10px 10px 0px 10px;
}

.first_column {
  font: bold black;
}

#chargeTotalFee_Container {
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

#chargeTotalFee_Fee {
  font-weight: 800;
  font-style: black;
  font-size: 25px;
  margin-top: -4px;
}

.flex-space-between {
  display: flex;
  justify-content: space-between;
}

.flex-start{
  display:flex;
  justify-content: flex-start;
}

.flex-end{
  display:flex;
  justify-content: flex-end;
}

.el-divider--horizontal{
  margin:5px 0;
}

.prescriptionCard{
  margin:5px 0;
}

.goods-back{
  position: absolute;
  top:20px;
  right: 200px;
  width: 60px;
  height: 25px;
  text-align: center;
  vertical-align: center;
  color:red;
  border: 1px solid red;
  border-radius: 45%;
  cursor: none;
  -webkit-transform: rotate(-45deg);
  float: right;
  z-index: 1;
}
</style>