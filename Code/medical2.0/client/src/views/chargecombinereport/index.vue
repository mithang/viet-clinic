<template>
  <div id="report-container">
    <el-radio-group v-model="tabPosition">
      <el-radio-button label="main">Báo cáo tổng hợp</el-radio-button>
      <el-radio-button label="detail">Báo cáo chi tiết</el-radio-button>
    </el-radio-group>
    <div style="margin-top:8px;">
      <div v-if="tabPosition === 'main'">
        <div>
          <el-row>
            <el-col :span="7" class="flex-start">
              <div class="condition-name">Ngày thu phí：</div>
              <el-date-picker
              style="width:320px;"
                v-model="mainCondition.chargeDateRange"
                type="daterange"
                range-separator="đến"
                start-placeholder="Ngày bắt đầu"
                end-placeholder="Ngày kết thúc"
              ></el-date-picker>
            </el-col>
            <el-col :span="6"  class="flex-start">
              <div class="condition-name">Tên chi phí：</div>
              <el-select
                v-model="mainCondition.mainCostType"
                placeholder="Tên chi phí"
              >
                <el-option
                  v-for="mainCostType in mainCostTypes"
                  :key="mainCostType.value"
                  :label="mainCostType.name"
                  :value="mainCostType.value"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="6"   class="flex-start">
              <div class="condition-name">Người thu phí：</div>
              <el-select
                v-model="mainCondition.chargeOper"
                placeholder="Người thu phí"
              >
                <el-option
                  v-for="chargeOper in chargeOpers"
                  :key="chargeOper.id"
                  :label="chargeOper.name"
                  :value="chargeOper.id"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="5"  class="flex-start">
              <div class="condition-name">Tên bệnh nhân：</div>
              <el-input
                v-model="mainCondition.patientName"
                style="width:200px;"
                placeholder="Tên bệnh nhân"
              ></el-input>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="7"  class="flex-start">
              <div class="condition-name">Số phiếu thu phí：</div>
              <el-input
                style="width:320px;"
                v-model="mainCondition.chargeCode"
                placeholder="Số phiếu thu phí"
              ></el-input>
            </el-col>
            <el-col :span="6"  class="flex-start">
              <div class="condition-name">Khoa lập phiếu：</div>
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
            <el-col :span="6"  class="flex-start">
              <div class="condition-name">Bác sĩ kê đơn：</div>
              <el-select
                v-model="mainCondition.prescriptionOper"
                placeholder="Người thu phí"
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
            <el-col :span="5"  class="flex-start">
              <div class="condition-name">Trạng thái thu phí：</div>
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
        <div class="flex-end" style="margin-top:8px">
          <el-button type="primary">Truy vấn</el-button>
          <el-button>Xuất</el-button>
        </div>
        <el-table :data="data" style="width: 100%">
          <el-table-column prop="prop" label="Số thứ tự" fixed width="width">
          </el-table-column>
          <el-table-column prop="prop" fixed label="Tên bệnh nhân" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Giới tính" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Tuổi" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Số phiếu thu phí" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Tên chi phí" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Khoa lập phiếu" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Bác sĩ kê đơn" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Trạng thái chứng từ" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Số tiền phải thu (hoàn lại)" width="150px">
          </el-table-column>
          <el-table-column prop="prop" label="Số tiền thực thu" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Số tiền ưu đãi" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="现金支付" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="微信支付" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Thanh toán Alipay" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Thanh toán bằng thẻ ngân hàng" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Hoàn tiền mặt" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Người thu phí" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Ngày thu phí" width="width">
          </el-table-column>
        </el-table>
        <div class="flex-end">
          <el-pagination
            @size-change="getChargeMainData"
            @current-change="getChargeMainData"
            :page-sizes="[5, 10, 15, 20]"
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
            <el-col :span="7"  class="flex-start">
              <div class="condition-name">Ngày thu phí：</div>
              <el-date-picker
                style="width:320px;"
                v-model="detailCondition.chargeDateRange"
                type="daterange"
                range-separator="đến"
                start-placeholder="Ngày bắt đầu"
                end-placeholder="Ngày kết thúc"
              ></el-date-picker>
            </el-col>
            <el-col :span="6"   class="flex-start">
              <div class="condition-name">Phân loại thu phí：</div>
              <el-select
              style="width:250px;"
                v-model="detailCondition.detailCostType"
                placeholder="Phân loại thu phí"
              >
                <el-option
                  v-for="detailCostType in detailCostTypes"
                  :key="detailCostType.value"
                  :label="detailCostType.name"
                  :value="detailCostType.value"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="6"   class="flex-start">
              <div class="condition-name">Người thu phí：</div>
              <el-select
                v-model="detailCondition.chargeOper"
                placeholder="Người thu phí"
              >
                <el-option
                  v-for="chargeOper in chargeOpers"
                  :key="chargeOper.id"
                  :label="chargeOper.name"
                  :value="chargeOper.id"
                >
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="5"   class="flex-start">
              <div class="condition-name">Tên bệnh nhân：</div>
              <el-input style="width:200px;"
                v-model="detailCondition.patientName"
                placeholder="Tên bệnh nhân"
              ></el-input>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="7"   class="flex-start"> 
              <div class="condition-name">Số phiếu thu phí：</div>
              <el-input
                 style="width:320px;"
                v-model="detailCondition.chargeCode"
                placeholder="Số phiếu thu phí"
              ></el-input>
            </el-col>
            <el-col :span="6" class="flex-start">
              <div class="condition-name">Hạng mục thu phí：</div>
              <el-input
              style="width:250px;"
                v-model="detailCondition.chargeItem"
                placeholder="Hạng mục thu phí"
              ></el-input>
            </el-col>
            <el-col :span="6" class="flex-start">
              <div class="condition-name">Khoa lập phiếu：</div>
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
              <div class="condition-name">Bác sĩ kê đơn：</div>
              <el-select
                style="width:200px;"
                v-model="detailCondition.prescriptionOper"
                placeholder="Người thu phí"
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
            <el-col :span="7"  class="flex-start">
              <div class="condition-name">Trạng thái thu phí：</div>
              <el-select
                 style="width:320px;"
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
            <el-col :span="17">
              <div class="flex-end">
                <el-button type="primary">Truy vấn</el-button>
                <el-button>Xuất</el-button>
              </div>
            </el-col>
          </el-row>
        </div>

        <el-table :data="data" style="width: 100%">
          <el-table-column prop="prop" label="Số thứ tự" fixed width="width">
          </el-table-column>
          <el-table-column prop="prop" fixed label="Tên bệnh nhân" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Số phiếu thu phí" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Khoa" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Bác sĩ" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Hạng mục thu phí" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Phân loại thu phí" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Số lượng" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Trạng thái chứng từ" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Phải thu/Trả lại(VNĐ)" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Người thu phí" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Ngày thu phí" width="width">
          </el-table-column>
        </el-table>
        <div class="flex-end">
          <el-pagination
            @size-change="getChargeDetailData"
            @current-change="getChargeDetailData"
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
  </div>
</template>

<script>
export default {
  data() {
    return {
      tabPosition: "main",
      /*Điều kiện truy vấn báo cáo tổng hợp*/
      mainCondition: {
        chargeDateRange: "" /*Khoảng ngày thu phí*/,
        mainCostType: 0,
        chargeOper: null,
        patientName: "",
        chargeCode: "",
        prescriptionDept: null,
        prescriptionOper: null,
        chargeStatu: 0,
      },
      mainCostTypes: [
        {
          value: 0,
          name: "Tất cả",
        },
        {
          value: 1,
          name: "Bán lẻ thuốc",
        },
        {
          value: 2,
          name: "Đơn thuốc",
        },
        {
          value: 3,
          name: "Đăng ký khám",
        },
      ],
      detailCondition: {
        chargeDateRange: "" /*Khoảng ngày thu phí*/,
        detailCostType: 0,
        chargeOper: null,
        patientName: "",
        chargeCode: "",
        chargeItem: "",
        prescriptionDept: null,
        prescriptionOper: null,
        chargeStatu: 0,
      },
      detailCostTypes: [
        {
          value: 0,
          name: "Tất cả",
        },
        {
          value: 1,
          name: "Đơn thuốc tây",
        },
        {
          value: 2,
          name: "Đơn thuốc đông y",
        },
      ],

      chargeOpers: [
        {
          id: 1,
          name: "张三",
        },
        {
          id: 2,
          name: "李四",
        },
      ],
      prescriptionDepts: [
        {
          id: 1,
          name: "内科",
        },
        {
          id: 2,
          name: "外科",
        },
      ],
      prescriptionOpers: [
        {
          id: 1,
          name: "张三",
        },
        {
          id: 2,
          name: "李四",
        },
      ],
      chargeStatus: [
        {
          id: 0,
          name: "Tất cả",
        },
        {
          id: 1,
          name: "Thu phí",
        },
        {
          id: 2,
          name: "Hoàn phí",
        },
      ],
      chargeMainTablePagesize: 10 /*Kích thước trang bảng chính*/,
      chargeMainTableCurrentPage: 1 /*Trang bảng chính hiện tại*/,
      chargeMainTableTotal: 0 /*Tổng dữ liệu bảng chính*/,

      chargeDetailTablePagesize: 10 /*Kích thước trang chi tiết*/,
      chargeDetailTableCurrentPage: 1 /*Trang chi tiết hiện tại*/,
      chargeDetailTableTotal: 0 /*Tổng dữ liệu chi tiết*/,
    };
  },
  methods: {
    getChargeMainData() {},
    getChargeDetailData() {},
  },
};
</script>

<style scoped>
#report-container {
  width: 98%;
  height: 96%;
  margin:10px 10px 0;
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
  margin-top:5px;
}

.el-row{
  margin-top:8px;
}
</style>