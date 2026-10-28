<template>
  <div id="report-container">
    <div class="flex-space-between"  id="condition">
      <div class="flex-start">
        <el-input
          v-model="condition.operName"
          prefix-icon="el-icon-search"
          style="width:250px;margin-right:8px;"
          placeholder="Vui lòng nhập tên người thu phí"
        ></el-input>
        <div>Ngày thu phí：</div>
        <el-date-picker
          v-model="condition.chargeDateRange"
          type="daterange"
          range-separator="đến"
          start-placeholder="Ngày bắt đầu"
          end-placeholder="Ngày kết thúc"
        ></el-date-picker>
        <el-radio style="margin-left:8px;margin-top:8px;" v-model="condition.mainOrDetail" label="1">Tổng hợp</el-radio>
        <el-radio style="margin-top:8px;" v-model="condition.mainOrDetail" label="2">Chi tiết</el-radio>
      </div>
      <div>
          <el-button type="primary">Truy vấn</el-button>
          <el-button>Xuất</el-button>
      </div>
    </div>
    <div>
        <el-table
            :data="data"
            style="width: 100%">
            <el-table-column
                prop="prop"
                label="Số thứ tự"
                width="width">
            </el-table-column>
            <el-table-column
                prop="prop"
                label="Người thu phí"
                width="width">
            </el-table-column>
            <el-table-column
                prop="prop"
                label="Ngày thu phí"
                width="width">
            </el-table-column>
            <el-table-column
                prop="prop"
                label="Phải thu/Trả lại(VNĐ)"
                width="width">
            </el-table-column>
            <el-table-column
                prop="prop"
                label="Số tiền thực thu"
                width="width">
            </el-table-column>
            <el-table-column
                prop="prop"
                label="Ưu đãi(VNĐ)"
                width="width">
            </el-table-column>
            <el-table-column
                prop="prop"
                label="Thanh toán tiền mặt"
                width="width">
            </el-table-column>
            <el-table-column
                prop="prop"
                label="Thanh toán chuyển khoản"
                width="width">
            </el-table-column>
            <el-table-column
                prop="prop"
                label="Thanh toán ví điện tử"
                width="width">
            </el-table-column>
            <el-table-column
                prop="prop"
                label="Thanh toán bằng thẻ ngân hàng"
                width="width">
            </el-table-column>
            <el-table-column
                prop="prop"
                label="Hoàn tiền mặt"
                width="width">
            </el-table-column>
        </el-table>
        <div class="flex-end">
          <el-pagination
            @size-change="getChargeAccountData"
            @current-change="getChargeAccountData"
            :page-sizes="[5, 10, 15, 20]"
            :page-size="chargeAccountTablePagesize"
            :current-page="chargeAccountTableCurrentPage"
            layout="total,sizes, prev, pager, next,jumper"
            :total="chargeAccountTableTotal"
          >
          </el-pagination>
        </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      condition: {
        operName: "",
        chargeDateRange: "",
        mainOrDetail:"1",
      },
      chargeAccountTablePagesize:10,
      chargeAccountTableCurrentPage:1,
      chargeAccountTableTotal:0,
    };
  },
  methods:{
      getChargeAccountData(){

      },
  }
};
</script>

<style scoped>
#report-container {
  width: 100%;
  height: 100%;
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
  width: 80px;
  text-align: right;
  vertical-align: center;
}

#condition{
  margin: 10px 10px;
}
</style>