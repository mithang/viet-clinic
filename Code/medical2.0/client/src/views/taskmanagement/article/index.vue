<template>
  <el-card class="page-container" style="padding: 0px">
    <el-row>
      <article-form ref="articleForm" @typeclick="typeclickload"></article-form>
      <el-col :span="24">
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
          <el-form-item label="Thời hạn" style="margin-right: 3px">
            <div class="block" style="width: 50%">
              <el-date-picker
                v-model="TimeInterval"
                type="datetimerange"
                :picker-options="pickerOptions"
                :default-time="['00:00:00', '23:59:59']"
                range-separator="đến"
                start-placeholder="Ngày bắt đầu"
                end-placeholder="Ngày kết thúc"
                align="right"
                :clearable="false"
              >
              </el-date-picker>
            </div>
          </el-form-item>
          <el-form-item label="Trạng thái xét duyệt" style="margin-right: 3px">
            <el-select v-model="formInline.auditstatus"  clearable placeholder="Trạng thái xét duyệt">
              <el-option
                v-for="item in auditstatuslist"
                :value="item.auditstatus"
                :key="item.auditstatus"
                :label="item.auditstatusmc"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="Tiêu đề bài viết" style="margin-right: 3px">
            <el-input
              v-model="formInline.title"
              placeholder="Vui lòng nhập từ khóa để truy vấn"
            ></el-input>
          </el-form-item>

          <el-form-item style="margin-right: 3px">
            <el-button type="primary" @click="onSearch">Truy vấn</el-button>
            <el-button
              type="info"
              icon="el-icon-refresh-left"
              @click="resetCondition"
              :plain="true"
              >Đặt lại</el-button
            >
            <!-- <el-button @click="show3 = !show3">{{show3==true?"Thu gọn":"Mở rộng"}}</el-button> -->

            <el-button
              type="primary"
              icon="el-icon-plus"
              @click.native="onCreatePatient('Thêm mới','')"
              >Thêm mới</el-button
            >
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <el-table :data="articlelist" border style="width: 100%">
          <el-table-column
            label="Số thứ tự"
            type="index"
            :index="indexMethod"
            align="center"
          >
          </el-table-column>
        <el-table-column prop="id" label="Định danhID" v-if="false">
        </el-table-column>
          <el-table-column prop="title" label="Tiêu đề bài viết" align="center" width="width">
          </el-table-column>
          <el-table-column prop="abstracts" label="Tóm tắt bài viết" align="center" width="width">
          </el-table-column>
          <el-table-column prop="aricledate" label="Thời gian đăng" align="center" width="width">
          </el-table-column>
          <el-table-column prop="username" label="Người đăng" align="center" width="width">
          </el-table-column>
          <el-table-column prop="auditstatus" label="Trạng thái xét duyệt" align="center" width="width">
            <template slot-scope="scope">
              <el-label
                v-if="scope.row.auditstatus === '0'"
                style="color: #a7761a"
                >{{ getauditstatus(scope.row.auditstatus) }}</el-label
              >
              <el-label
                v-if="scope.row.auditstatus === '1'"
                style="color: #3eec5b"
                >{{ getauditstatus(scope.row.auditstatus) }}</el-label
              >
              <el-label
                v-if="scope.row.auditstatus === '2'"
                style="color: #f5314b"
                >{{ getauditstatus(scope.row.auditstatus) }}</el-label
              >
            </template>
          </el-table-column>
          <el-table-column label="Thao tác" align="center" header-align='center' width="120px">
            <template slot-scope="scope">
              <el-button @click="onCreatePatient('Sửa',scope.row.id)" type="text" size="small"
                >Xem chi tiết</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <el-pagination
          background
          @size-change="onSizeChange"
          @current-change="onCurrentChange"
          :current-page.sync="currentPage"
          :page-sizes="[20, 50, 100, patientTotal]"
          :page-size="20"
          layout="total, sizes, prev, pager, next, jumper"
          :total="patientTotal"
        >
        </el-pagination>
      </el-col>
    </el-row>
  </el-card>
</template>

<script>
import MainUI from "@/views/components/mainUI";
import listViewMixin from '@/mixins/listViewMixin'
import History from "@/views/components/history";
import ArticleForm from "./articleForm";
import { listarticlepage } from "@/api/taskmanagement/article";
export default {
  extends: MainUI,
  mixins: [listViewMixin],
  components: {
    History,
    ArticleForm,
  },
  data() {
    return {
      listApi: listarticlepage,
      getApi: null,
      deleteApi: null,
      entityName: 'Article',
      permissionPrefix: 'article',
      articlelist: [],
      TimeInterval: this.getInitializeDate(),
      auditstatuslist: [
        { auditstatus: "0", auditstatusmc: "Chờ xét duyệt" },
        { auditstatus: "1", auditstatusmc: "Đã qua" },
        { auditstatus: "2", auditstatusmc: "Chưa qua" },
      ],
      formInline: {
        companyId: currentUser.company.id,
        title: "",
        lx: "",
        auditstatus: null,
      },
      patientTotal: 0,
    };
  },
  mounted() {
    this.pageInit()
  },
  methods: {
    appendSearchParams() {
      this.search.params.push({
        columnName: "company_id",
        queryType: "=",
        value: currentUser.company.id,
      })
      if (this.moreCodition) {
        this.search.params = this.search.params.concat(this.compositeCondition())
      } else {
        // Tham số truy vấn: 标题名称
        this.search.params.push({
          columnName: "title",
          queryType: "like",
          value: this.formInline.title,
        })
        if (this.formInline.auditstatus) {
          // Tham số truy vấn: Trạng thái xét duyệt
          this.search.params.push({
            columnName: "auditstatus",
            queryType: "=",
            value: this.formInline.auditstatus,
          })
        }
        if (this.TimeInterval) {
          // Tham số truy vấn: 发布开始时间
          this.search.params.push({
            columnName: "aricledate",
            queryType: ">=",
            value: this.TimeInterval[0],
          })
          // Tham số truy vấn: 发布结束时间
          this.search.params.push({
            columnName: "aricledate",
            queryType: "<=",
            value: this.TimeInterval[1],
          })
        }
      }
    },
    handleListResponse(responseData) {
      this.patientTotal = responseData.data.total
      this.articlelist = responseData.data.rows
    },

    //Đặt lại tìm kiếm
    resetCondition(){
        this.TimeInterval = this.getInitializeDate();
        this.formInline.title = "";
        this.formInline.auditstatus = null;
        this.loadData()
    },

    //Đặt ngày mặc định
    getInitializeDate() {
      let date = new Date(); //Lấy thời gian mới
      //Lấy năm hiện tại,và chuyển thành chuỗi
      let year = date.getFullYear().toString();
      //Lấy tháng hiện tại，vì tháng bắt đầu từ0Bắt đầu，tại đây cần cộng1，
      //Sử dụng biểu thức ba ngôi，Kiểm tra có nhỏ hơn không10，Nếu vậy，thì nối thêm vào trước chuỗi'0'
      let month =
        date.getMonth() + 1 < 10
          ? "0" + (date.getMonth() + 1).toString()
          : (date.getMonth() + 1).toString();
      //Lấy ngày，Kiểm tra có nhỏ hơn không10，Nếu vậy，thì nối thêm vào trước chuỗi'0'
      let day =
        date.getDate() < 10
          ? "0" + date.getDate().toString()
          : date.getDate().toString();
      //Nối chuỗi，Nối thời gian bắt đầu và thời gian kết thúc
      let start = year + "-" + month + "-01" + " 00:00:00"; //Ngày đầu tiên của tháng hiện tại
      //let end=new Date(year, month, 0).getDate();
      let end = year + "-" + month + "-" + day + " 23:59:59"; //Trong ngày
      return [start, end]; //Gán giá trị cho thành phầnDatePicker Dữ liệu đã liên kết
    },
    getauditstatus(status) {
      return status === "0" ? "Chờ xét duyệt" : status === "1" ? "Đã qua" : "Chưa qua";
    },
    typeclickload(){
        this.loadData()
    },

    getcontenttext(evt) {
      // let reft=await  this.$refs.quilleditorref;
      // this.content=reft.handleGetHtml();
    },
    onCreatePatient(types,id) {
      let viewlist={
          view:types,
          id:id
      };
      this.$refs.articleForm.openAddworkbenchDialog(viewlist);
    },
  },
};
</script>
