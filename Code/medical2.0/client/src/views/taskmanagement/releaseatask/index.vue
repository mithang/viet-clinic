<template>
<el-card class="page-container" style="padding: 0px;">
  <el-row>
    <el-col :span="24" style="height:40px;">
      <addtask-form ref='addtaskForm' @typeclick="typeclickload"></addtask-form>
      <el-button type="primary" style="float:right;margin-right:4.5%;" icon='el-icon-plus'  @click.native="onCreatePatient('')">Thêm mới</el-button>
    </el-col>
    <el-col :span="24">
     <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item label="Thời hạn"  style="margin-right: 3px;">
  <div class="block" style="width:50%">
    <el-date-picker
      v-model="TimeInterval"
      type="datetimerange"
      :picker-options="pickerOptions"
      :default-time="['00:00:00','23:59:59']"
      range-separator="đến"
      start-placeholder="Ngày bắt đầu"
      end-placeholder="Ngày kết thúc"
      align="right"
      :clearable="false">
    </el-date-picker>
  </div>
    </el-form-item>
    <!-- <el-form-item label="Phòng khám"  style="margin-right: 3px;">
    <el-select v-model="formInline.jgid" placeholder="Phòng khám">
      <el-option label="Tất cả" value="qb"></el-option>
       <el-option v-for="item in jglist" :value="item.jgid" :key="item.jgid"  :label="item.jgmc">
        </el-option>
    </el-select>
  </el-form-item> -->
    <el-form-item label="Loại nhiệm vụ"  style="margin-right: 3px;">
    <el-select v-model="formInline.tasktype" placeholder="Loại nhiệm vụ">
      <el-option label="Tất cả" value="qb"></el-option>
      <el-option label="Nhiệm vụ thường xuyên" value="0"></el-option>
      <el-option label="Hoạt động tuyên truyền" value="1"></el-option>
    </el-select>
  </el-form-item>
  <el-form-item label="Trạng thái nhiệm vụ"  style="margin-right: 3px;">
    <el-select v-model="formInline.taskstatus" placeholder="Trạng thái nhiệm vụ">
      <el-option label="Tất cả" value="qb"></el-option>
      <el-option label="Chờ đăng" value="0"></el-option>
      <el-option label="Đã đăng" value="1"></el-option>
    </el-select>
  </el-form-item>
    <el-form-item label="Tên nhiệm vụ" style="margin-right: 3px;">
    <el-input v-model="formInline.taskname" placeholder="请输入任务名称关键词"></el-input>
  </el-form-item>

  <el-form-item style="margin-right: 3px;">
    <el-button type="primary" @click="onSearch">Truy vấn</el-button>
    <el-button type="info" icon="el-icon-refresh-left" @click='resetCondition' :plain='true'>Đặt lại</el-button>
    <!-- <el-button @click="show3 = !show3">{{show3==true?"Thu gọn":"Mở rộng"}}</el-button> -->
  </el-form-item>
     </el-form>
   </el-col>
    <el-col :span="24">
      <taskdetails-form ref='taskdetailsForm' @typeclick="typeclickload"></taskdetails-form>
  <el-table
    :data="tableData"
    border
    style="width: 100%">
    <el-table-column
             label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
    <!-- <el-table-column
      prop="date"
      label="Ngày"
      width="150">
    </el-table-column> -->
    <el-table-column
      prop="id"
      v-if="show"
      label="id">
    </el-table-column>
    <el-table-column
      prop="tasktype"
      label="Loại nhiệm vụ"
      width="width">
    </el-table-column>
    <!-- <el-table-column
      prop="taskstatus"
      label="Trạng thái nhiệm vụ"
      width="width">
    </el-table-column> -->
    <el-table-column
      prop="taskname"
      label="任务标题"
      width="width">
    </el-table-column>
    <!-- <el-table-column
      prop="city"
      label="市区"
      width="120">
    </el-table-column> -->
    <el-table-column
      prop="taskdescribe"
      label="任务内容"
      width="width">
    </el-table-column>
    <el-table-column
      prop="createdTime"
      label="Thời gian đăng"
      width="width">
    </el-table-column>
    <el-table-column
      prop="taskdeadline"
      label="Thời hạn nhiệm vụ"
      width="width">
    </el-table-column>
    <el-table-column
      prop="taskstatus"
      label="Trạng thái nhiệm vụ"
      width="width">
      <template slot-scope="scope">
        <span v-if="scope.row.taskstatus==='0'" style="color:orange">
            Chờ đăng
        </span>
         <span v-else style="color:lightgreen">
            Đã đăng
        </span>
      </template>
    </el-table-column>
    <el-table-column
      prop="taskaccessory"
      v-if="show"
      label="taskaccessory">
    </el-table-column>
    <el-table-column
      label="Thao tác"
      width="width">
      <template slot-scope="scope">
        <el-button v-if="scope.row.taskstatus==='1'" @click="onCreatedetails(scope.row,scope.$index)" type="text" size="small">Xem</el-button>
        <el-button v-if="scope.row.taskstatus==='0'" @click="onUpdate(scope.row,scope.$index)" type="text" size="small">Sửa</el-button>
        <el-button v-if="scope.row.taskstatus==='0'" @click="onRelease(scope.row,scope.$index)" type="text" size="small" style="color:lightgreen">Đăng nhiệm vụ</el-button>
        <el-button v-if="scope.row.taskstatus==='0'" @click="onDelete(scope.row,scope.$index)" type="text" size="small" style="color:red;">Xóa nhiệm vụ</el-button>
        <!-- <el-button type="text" size="small">Sửa</el-button> -->
      </template>
    </el-table-column>
  </el-table>
    </el-col>
  </el-row>
  <el-row>
        <el-col :span='24'>
          <el-pagination
            background
            @size-change='onSizeChange'
            @current-change='onCurrentChange'
            :current-page.sync='currentPage'
            :page-sizes='[ 20, 50, 100, patientTotal]'
            :page-size='20'
            layout='total, sizes, prev, pager, next, jumper'
            :total='patientTotal'>
          </el-pagination>
        </el-col>
      </el-row>
</el-card>
</template>

<script>
import AddtaskForm from './addtaskForm'
import TaskdetailsForm from './taskdetailsForm'
import listViewMixin from '@/mixins/listViewMixin'
import MainUI from '@/views/components/mainUI'
import History from '@/views/components/history'
import {selecttasklist,storageTask,release,deleteTask} from "@/api/taskmanagement/taskmanagement"
import { getjglist} from "@/api/toll/tollInfo";
  export default {
    extends: MainUI,
  mixins: [listViewMixin],
  components: {
    AddtaskForm,
    TaskdetailsForm,
    History
  },
    data() {
      return {
        listApi: (search) => {
          let flat = { limit: search.limit, offset: search.offset }
          if (search.params) {
            search.params.forEach(p => { flat[p.columnName] = p.value })
          }
          return selecttasklist(flat)
        },
        entityName: 'Task',
        permissionPrefix: 'task',
        tableData: [],//表格数据
        jglist:[],
        show:false,
        formInline: {
        jgid: '',
          kssj:'',
          jssj:'',
          tasktype:'',
          taskstatus:'',
          taskname:''
        },
        patientTotal: 0,
      patientList: [],
        TimeInterval:this.getInitializeDate(),
        pickerOptions: {
          shortcuts: [{
            text: 'Tuần gần nhất',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: 'Tháng gần nhất',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '3 tháng gần nhất',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
              picker.$emit('pick', [start, end]);
            }
          }]
        },
        YpjxcRc: {
        limit: 20,
        offset: 0,
        companyId: currentUser.company.id,
        orderby:"batch_no",
        jgid:null,
      },
      taskAll:[],
      zsids:'',

      }
    },
    methods: {

      //Xóa nhiệm vụ
      onDelete(row,index){
        this.setLoad();
        deleteTask(row).then((res)=>{
          if(res.code===100){
            this.$message.success("Xóa thành công！")
            this.loadData()
            this.resetLoad();
          }else{
            this.$message.error("Thực hiện thất bại")
          }
        }).catch((error)=>{
          this.$message.error(error)
        })
      },

      //Đăng nhiệm vụ
      onRelease(row,index){
        row.taskstatus = "1"
        release(row).then((res)=>{
          if(res.code===100){
            this.$message.success("发布成功")
            this.Getcliniclist()
          }
        }).catch((error)=>{
          this.$message.error(error)
        })
      },

      //修改任务
      onUpdate(row,index){
        this.$refs.addtaskForm.openUpdateworkbenchDialog(this.taskAll[index]);
      },

         //Đặt ngày mặc định
    getInitializeDate () {
     let date = new Date()//Lấy thời gian mới
     //Lấy năm hiện tại,và chuyển thành chuỗi
     let year = date.getFullYear().toString()
     //Lấy tháng hiện tại，vì tháng bắt đầu từ0Bắt đầu，tại đây cần cộng1，
    //Sử dụng biểu thức ba ngôi，Kiểm tra có nhỏ hơn không10，Nếu vậy，thì nối thêm vào trước chuỗi'0'
     let month = date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1).toString() : (date.getMonth() + 1).toString()
     //Lấy ngày，Kiểm tra có nhỏ hơn không10，Nếu vậy，thì nối thêm vào trước chuỗi'0'
     let day = date.getDate() < 10 ? '0' + date.getDate().toString() : date.getDate().toString()
     //Nối chuỗi，Nối thời gian bắt đầu và thời gian kết thúc
     let start = year + '-' + month + '-01'+' 00:00:00'    //Ngày đầu tiên của tháng hiện tại
     //let end=new Date(year, month, 0).getDate();
     let end = year + '-' + month + '-' + day +' 23:59:59'  //Trong ngày
     return [start,end] //Gán giá trị cho thành phầnDatePicker Dữ liệu đã liên kết
   },
       appendSearchParams() {
      if (this.TimeInterval) {
        this.search.params.push({ columnName: 'kssj', queryType: '=', value: this.TimeInterval[0] })
        this.search.params.push({ columnName: 'jssj', queryType: '=', value: this.TimeInterval[1] })
      }
      let tasktype = this.formInline.tasktype === "qb" ? "" : this.formInline.tasktype
      let taskstatus = this.formInline.taskstatus === "qb" ? "" : this.formInline.taskstatus
      if (tasktype) this.search.params.push({ columnName: 'tasktype', queryType: '=', value: tasktype })
      if (taskstatus) this.search.params.push({ columnName: 'taskstatus', queryType: '=', value: taskstatus })
      if (this.formInline.taskname) this.search.params.push({ columnName: 'taskname', queryType: 'like', value: this.formInline.taskname })
    },
    handleListResponse(responseData) {
      this.patientTotal = responseData.data.total
      this.taskAll = responseData.data.rows
      this.tableData = []
      if (responseData.data.total > 0) {
        responseData.data.rows.forEach((item) => {
          this.tableData.push({
            id: item.id,
            tasktype: item.tasktype === "0" ? "Nhiệm vụ thường xuyên" : "Hoạt động tuyên truyền",
            taskname: item.taskname,
            taskdescribe: item.taskdescribe,
            createdTime: item.createdTime,
            taskdeadline: item.taskdeadline,
            taskinitiatorname: item.taskinitiatorname,
            taskaccessory: item.taskaccessory,
            taskstatus: item.taskstatus,
          })
        })
      }
    },
      handleClick(row) {
      },
      onCreatePatient(types) {
      this.$refs.addtaskForm.openAddworkbenchDialog(types);
    },
    onCreatedetails(values){

      this.$refs.taskdetailsForm.openAddworkbenchDialog(values);
    },
    typeclickload(){
      this.loadData();
    },
    resetCondition(){
      this.YpjxcRc={
        limit: 20,
        offset: 0,
        companyId: currentUser.company.id,
        orderby:"batch_no",
        jgid:null,
      };
      this.jglist=[];
        this.formInline= {
        jgid: '',
          kssj:'',
          jssj:'',
          tasktype:'',
          taskstatus:'',
          taskname:''
        };
        this.onSearch();
    },
     Getcliniclist(){
       this.setLoad();
          this.jglist=[];
          getjglist(this.YpjxcRc).then((responseData)=>{
              if (responseData.code === 100){
                  if(responseData.data.length>0){
                     this.zsids='';
                      responseData.data.forEach((item)=>{
                          if(item.jgid!==currentUser.company.id){
                          this.jglist.push({
                              jgid:item.jgid,
                              jgmc:item.jgmc,
                           })
                           this.zsids+=item.jgid +',';
                         }
                      })
                      this.zsids=this.zsids.substring(0, this.zsids.lastIndexOf(','));
                      this.resetLoad();
                  }
              }
          })
      },

    },
    mounted(){
        this.Getcliniclist();
        this.pageInit();
    }
  }
</script>
